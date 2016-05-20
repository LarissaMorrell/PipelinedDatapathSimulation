package disassembler;

public class MainMemory {

	private short main_Mem[] = new short[1024];

	public MainMemory(){

		//Create the "Main Memory" as an array, and load values
		short mmVal = 0;

		for(short i = 0; i < main_Mem.length; i++){ 
			main_Mem[i] = mmVal; 
			mmVal++;

			//If the value of the next value is larger than 
			//0xFF then reset the mmVal back to 0
			if(mmVal > 0xFF){ 
				mmVal = 0; 
			}
		}
	}
	
	
	public void setMainMem(int index, short data){
		
		main_Mem[index] = data;
	}
	
	public short getMainMem(int index){
		
		return main_Mem[index];
	}
	
	

}
