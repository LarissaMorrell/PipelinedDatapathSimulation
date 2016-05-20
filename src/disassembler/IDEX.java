package disassembler;

public class IDEX implements PipelineInterface {

	private int write[];
	private int read[];


	/**
	 * write/read[ ] = {(0)nop, (1)regDst, (2)aluOp, (3)aluSrc, (4)memRead, (5)memWrite, 
	 *                  (6)regWrite, (7)memToReg, (8)readReg1, (9)readReg2, (10)seOffset, 
	 *                  (11)writeReg_20_16, (12)write_Reg_15_11, (13)function}
	 */
	
	public IDEX(){
		this.write = new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		this.read = new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

	}



	/**
	 * These 2 methods are the setters and getters for a 
	 * value in the WRITE array (the only value)
	 */

	@Override
	public void setWriteValue(int index, int value) {
		write[index] = value;
	}

	@Override
	public int getWriteValue(int index) {
		return write[index];
	}


	/**
	 * The next 2 methods are the setters and getters 
	 * for the entire WRITE array
	 */

	@Override
	public int[] getWrite() {
		return write;
	}



	/**
	 * The next 2 methods are the setters and getters 
	 * for the entire READ array
	 */

	@Override
	public int[] getRead() {
		return read;
	}

	
	@Override
	public int getReadValue(int index) {
		return read[index];
	}




	/**
	 * This method concatenates a string that has the 
	 * values of all the control signals
	 */

	@Override
	public String controlString() {

		String ctrl = "RegDst = " + write[1]
				+ ", ALUOp = " + Integer.toBinaryString(write[2])
				+ ", ALUSrc = " + write[3]
				+ ", MemRead = " + write[4]
				+ "\n           MemWrite = " + write[5]
				+ ", RegWrite = " + write[6]
				+ ", MemToReg = " + write[7];

		return ctrl;
	}

}
