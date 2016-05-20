package disassembler;

//import java.util.stream.*;

public class Datapath {

	IFID ifid;
	IDEX idex;
	EXMEM exmem;
	MEMWB memwb;
	Console console;
	Registers regs;
	MainMemory mem;

	public Datapath(){
		ifid = new IFID();
		idex = new IDEX();
		exmem = new EXMEM();
		memwb = new MEMWB();

		console = new Console();
		regs = new Registers();
		mem = new MainMemory();
	}




	/**
	 * This method takes in the cycle number and fetches
	 * the instruction from the console
	 * @param cycle
	 */
	public void if_stage(int cycle){

		//fetch next instruction out of instruction cache		
		//Put it in the WRITE version of the IF/ID pipeline register
		ifid.setWriteValue(0, console.getInstruction(cycle));

		id_stage();
	}




	/**
	 * This method takes the instruction out of the IFID pipeline
	 * and generates control signals, finds important parts of 
	 * the instruction for offset, function code, and registers. 
	 * All of this data is stored in the IDEX pipeline.
	 */
	private void id_stage(){

		//Read an instruction from the READ version of IF/ID pipeline register
		int instr = ifid.getReadValue(0);


		//set nop index (need this for printing)
		if(instr == 0){
			idex.setWriteValue(0, 0);  //set nop to 0
		} else{
			idex.setWriteValue(0, 1);  //set nop to 1
		}

		//do the decoding and register fetching

		//find registers
		int readReg1 = (instr & 0x3E00000) >>> 21;  //set readReg1  (srcRS)
		int readReg2 = (instr & 0x1F0000) >>> 16;   //set readReg2  (srcRT)

		//fetch the data in the regs and write to ID/EX pipeline register
		idex.setWriteValue(8, regs.getRegsData(readReg1));
		idex.setWriteValue(9, regs.getRegsData(readReg2));


		//function & set control signals
		findControlFunction(instr);


		//find offset and sign extend

		int offset_15_0 = instr & 0xFFFF;

		if(((offset_15_0 & 0x8000) >>> 15) == 1){   //if the 16th bit (leading bit) is 1,
			offset_15_0 = offset_15_0 + 0xFFFF0000; //then sign extend
		}

		idex.setWriteValue(10, offset_15_0);


		//instruction bits 20_16
		idex.setWriteValue(11, (instr & 0x1F0000) >>> 16); 


		//instruction bits 15_11
		idex.setWriteValue(12, (instr & 0xF800) >>> 11);

		ex_stage();

	}




	/**
	 * This method simulates the execute phase, and uses the ALU
	 * to evaluate the data. The data is taken out of the IDEX
	 * pipeline, and the new data is stored in the EXMEM pipeline.
	 */
	private void ex_stage(){

		//check for nop
		if(idex.getReadValue(0) == 1){
			exmem.setWriteValue(0, 1);
		} else {
			exmem.setWriteValue(0, 0);
		}


		//write control signals to pipeline
		exmem.setWriteValue(1, idex.getReadValue(4));  //memRead
		exmem.setWriteValue(2, idex.getReadValue(5));  //memWrite
		exmem.setWriteValue(3, idex.getReadValue(7));  //memToReg
		exmem.setWriteValue(4, idex.getReadValue(6));  //regWrite


		//regDst mux
		if(idex.getReadValue(1) == 0){ //if regDst is deasserted (0)

			//The destination reg is writeReg_20_16, send to pipeline
			exmem.setWriteValue(5, idex.getReadValue(11));	


		} else { //otherwise, regDst is asserted (1)

			//The destination reg is writeReg_15_11, send to pipeline
			exmem.setWriteValue(5, idex.getReadValue(12));
		}



		// aluSrc mux
		int rightOperand;
		if(idex.getReadValue(3) == 1){
			rightOperand = idex.getReadValue(10);   	//Offset value
		} else {
			rightOperand = idex.getReadValue(9);		//readReg2
		}


		//ALU Control determines operation

		//if aluOp is a lw or sw
		if(idex.getReadValue(2) == 0){

			//aluResult = readData1 + rightOperand
			exmem.setWriteValue(6, idex.getReadValue(8) + rightOperand);


			//Then check the funct code; aluOp must = 0b10
		} else {

			if(idex.getReadValue(13) == 0x22){   	//is a sub
				//the funct field is a sub
				exmem.setWriteValue(6, idex.getReadValue(8) - rightOperand);
			} else {		//for all others
				//the funct field is an add
				exmem.setWriteValue(6, idex.getReadValue(8) + rightOperand);
			}

		}


		//SWValue or WriteDataValue
		exmem.setWriteValue(7, idex.getReadValue(9));

		mem_stage();

	}




	/**
	 * This method takes the data stored in the EXMEM pipeline
	 * and either reads the data out of memory or stores 
	 * data into memory. Data is next passed on through the 
	 * MEMWB pipeline.
	 */
	private void mem_stage(){

		//check for nop
		if(exmem.getReadValue(0) == 1){
			memwb.setWriteValue(0, 1);
		} else {
			memwb.setWriteValue(0, 0);
		}


		memwb.setWriteValue(1, exmem.getReadValue(3)); //memToReg to the MEMWB pipeline
		memwb.setWriteValue(2, exmem.getReadValue(4)); //regWrite to the MEMWB pipeline

		memwb.setWriteValue(5, exmem.getReadValue(5)); //writeRegNum to the MEMWB pipeline
		memwb.setWriteValue(4, exmem.getReadValue(6)); //aluResult to the MEMWB pipeline


		//if memRead is 1
		if(exmem.getReadValue(1) == 1){

			//use the aluResult(6) as an index, to get 
			//the value in memory
			short lwDataValue = mem.getMainMem(exmem.getReadValue(6));

			//store that value in the pipeline
			memwb.setWriteValue(3, lwDataValue);
		}



		//if memWrite is 1
		if(exmem.getReadValue(2) == 1){

			//main_mem[aluResult] = swValue
			mem.setMainMem(exmem.getReadValue(6), (short)exmem.getReadValue(7));	
		}


		wb_stage();

	}




	/**
	 * This gets the data out of the MEMWB pipeline and then performs
	 * the operations based upon the regWrite and memToReg controls
	 */
	private void wb_stage(){

		if(memwb.getReadValue(2) == 1){   //regWrite

			//regs[writeRegNum] = aluResult
			regs.setRegsData(memwb.getReadValue(5), memwb.getReadValue(4));

		}

		if(memwb.getReadValue(1) == 1){	  //memToReg

			//regs[writeRegNum] = lwDataValue
			regs.setRegsData(memwb.getReadValue(5), memwb.getReadValue(3));
		}



	}



	/**
	 * This method prints the pipelines to the user by retrieving 
	 * Strings concatenated in the pipelines. Both versions of the 
	 * read and write pipelines are outputted.
	 */
	public void print_out_everything(){

		//IFID (Write Version)
		System.out.println(regs.regToString() 
				+ "\nIF/ID Write Stage:\n  instr = " 
				+ Integer.toHexString(ifid.getWriteValue(0)) + "\n");

		//IFID (Read Version)
		System.out.println("IF/ID Read Stage:\n  instr = " 
				+ Integer.toHexString(ifid.getReadValue(0)) + "\n");





		//IDEX (Write Version)
		System.out.print("ID/EX Write Stage:\n  Control: ");

		if(idex.getWriteValue(0) == 0){     //nop
			System.out.println("000000000\n");

		} else{

			System.out.println(idex.controlString() 
					+ "\n  Reg1Data = " + Integer.toHexString(idex.getWriteValue(8))  
					+ ", Reg2Data = " + Integer.toHexString(idex.getWriteValue(9)) 
					+ "\n  SEOffset = " + Integer.toHexString(idex.getWriteValue(10))
					+ ", WriteReg_20_16 = " + idex.getWriteValue(11)
					+ ", WriteReg_15_11 = " + idex.getWriteValue(12)  
					+ ", Function = " + Integer.toHexString(idex.getWriteValue(13)) + "\n");
		}

		//IDEX (Read Version)
		System.out.print("ID/EX Read Stage:\n  Control: ");

		if(idex.getReadValue(0) == 0){     //nop
			System.out.println("000000000\n");

		} else{

			System.out.println(idex.controlString() 
					+ "\n  Reg1Data = " + Integer.toHexString(idex.getReadValue(8))  
					+ ", Reg2Data = " + Integer.toHexString(idex.getReadValue(9)) 
					+ "\n  SEOffset = " + Integer.toHexString(idex.getReadValue(10))
					+ ", WriteReg_20_16 = " + idex.getReadValue(11)
					+ ", WriteReg_15_11 = " + idex.getReadValue(12)  
					+ ", Function = " + Integer.toHexString(idex.getReadValue(13)) + "\n");
		}





		//EXMEM (Write Version)
		System.out.print("EX/MEM Write Stage:\n  Control: ");

		if(exmem.getWriteValue(0) == 0){		//if the instr is nop
			System.out.println("000000000\n");

		} else{
			System.out.println(exmem.controlString() 
					+ "\n  ALUResult = " + Integer.toHexString(exmem.getWriteValue(6))
					+ ", SWValue = " + Integer.toHexString(exmem.getWriteValue(7))
					+ ", WriteRegNum = " + exmem.getWriteValue(5) + "\n");
		}


		//EXMEM (Read Version)
		System.out.print("EX/MEM Read Stage:\n  Control: ");

		if(exmem.getReadValue(0) == 0){		//if the instr is nop
			System.out.println("000000000\n");

		} else{
			System.out.println(exmem.controlString() 
					+ "\n  ALUResult = " + Integer.toHexString(exmem.getReadValue(6))
					+ ", SWValue = " + Integer.toHexString(exmem.getReadValue(7))
					+ ", WriteRegNum = " + exmem.getReadValue(5) + "\n");
		}






		//MEMWR (Write Version)
		System.out.print("MEM/WB Write Stage:\n  Control: ");

		if(memwb.getWriteValue(0) == 0){		//if the instr is nop
			System.out.println("000000000\n");
		} else{
			System.out.println(memwb.controlString() 
					+ "\n  LWDataValue = " + Integer.toHexString(memwb.getWriteValue(3))
					+ ", ALUResult = " + Integer.toHexString(memwb.getWriteValue(4))
					+ ", WriteRegNum = " + memwb.getWriteValue(5) + "\n");
		}

		//MEMWR (Read Version)
		System.out.print("MEM/WB Read Stage:\n  Control: ");

		if(memwb.getReadValue(0) == 0){		//if the instr is nop
			System.out.println("000000000\n");

		} else{
			System.out.println(memwb.controlString() 
					+ "\n  LWDataValue = " + Integer.toHexString(memwb.getReadValue(3))
					+ ", ALUResult = " + Integer.toHexString(memwb.getReadValue(4))
					+ ", WriteRegNum = " + memwb.getReadValue(5) + "\n");
		}

	}

	
	


	/**
	 * This method copies the write pipeline arrays to the read
	 * array in the same pipeline
	 */
	public void copy_write_to_read(){

		System.arraycopy(ifid.getWrite(), 0, ifid.getRead(), 0, ifid.getWrite().length);
		System.arraycopy(idex.getWrite(), 0, idex.getRead(), 0, idex.getWrite().length);
		System.arraycopy(exmem.getWrite(), 0, exmem.getRead(), 0, exmem.getWrite().length);
		System.arraycopy(memwb.getWrite(), 0, memwb.getRead(), 0, memwb.getWrite().length);

	}

	
	
	
	/**
	 * This method takes in the instruction and uses bit-mapping
	 * to determine what the function is and then generates the 
	 * control signals. All of this data is stored in the IDEX 
	 * pipeline write array.
	 * @param instr
	 */
	private void findControlFunction(int instr){

		//If OP code is 0, then we have RFormat class
		if(instr != 0 && (instr >>> 26) == 0){

			//use bit-mapping to find the last digits 
			//in the R-formatted word which will be our function
			switch(instr & 0x3f){  //find the funct

			case 0x20:     	//add
				idex.setWriteValue(13, 0x20);  	//function
				//set control
				break;

			case 0x22:		//sub
				idex.setWriteValue(13, 0x22);	
				break;

			}

			//for all that are R-Formatted, controls are the same
			idex.setWriteValue(1, 1);   //regDst
			idex.setWriteValue(2, 0b10);//aluOp
			idex.setWriteValue(3, 0); 	//aluSrc
			idex.setWriteValue(4, 0); 	//memRead
			idex.setWriteValue(5, 0); 	//memWrite
			idex.setWriteValue(6, 1);	//regWrite
			idex.setWriteValue(7, 0);  //memToReg




		} else { 	//then it must be an I-formatted word

			/**IDEX
			 * write/read[ ] = {(0)nop, (1)regDst, (2)aluOp, (3)aluSrc, (4)memRead, (5)memWrite, 
			 *                  (6)regWrite, (7)memToReg, (8)readReg1, (9)readReg1, (10)seOffset, 
			 *                  (11)writeReg_20_16, (12)write_Reg_15_11, (13)function}
			 */

			switch (instr >>> 26){

			case 0x20:		//lb
				idex.setWriteValue(13, 0x20);//funct
				idex.setWriteValue(1, 0);   //regDst
				idex.setWriteValue(2, 0); 	//aluOp
				idex.setWriteValue(3, 1); 	//aluSrc
				idex.setWriteValue(4, 1); 	//memRead
				idex.setWriteValue(5, 0); 	//memWrite
				idex.setWriteValue(6, 1);	//regWrite
				idex.setWriteValue(7, 1);  //memToReg
				break;

			case 0x23:		//lw
				idex.setWriteValue(13, 0x23);//funct
				idex.setWriteValue(1, 0);   //regDst
				idex.setWriteValue(2, 0); 	//aluOp
				idex.setWriteValue(3, 1); 	//aluSrc
				idex.setWriteValue(4, 1); 	//memRead
				idex.setWriteValue(5, 0); 	//memWrite
				idex.setWriteValue(6, 1);	//regWrite
				idex.setWriteValue(7, 1);  //memToReg
				break;

			case 0x28:		//sb
				idex.setWriteValue(13, 0x28);//funct
				idex.setWriteValue(1, 0);   //regDst (Doesn't matter)
				idex.setWriteValue(2, 0); 	//aluOp
				idex.setWriteValue(3, 1); 	//aluSrc
				idex.setWriteValue(4, 0); 	//memRead
				idex.setWriteValue(5, 1); 	//memWrite
				idex.setWriteValue(6, 0);	//regWrite
				idex.setWriteValue(7, 0);  //memToReg (Doesn't matter)
				break;

			default:
				//	System.out.println("nop in id() ");
			}


		}


	}

}
