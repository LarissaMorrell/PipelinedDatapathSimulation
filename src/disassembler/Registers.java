package disassembler;

public class Registers {
	
	private static int[] regs = new int[32];
	
	public Registers(){
		regs[0] = 0;

		for(int i = 1; i < (regs.length); i++){     
			regs[i] = 0x100 + i;
		}
	}
	
	
	public int getRegsData(int index){
		
		return regs[index];
	}
	
	public void setRegsData(int index, int data){
		
		regs[index] = data;
		
	}
	
	
	
	public String regToString(){
		
		String regString = "Registers: ";
		
		for(int i = 0; i < regs.length; i++){
			regString += "(" + i + ") " + Integer.toHexString(regs[i]) + "    ";
			
			
			if(i%4 == 0){
				regString += "\n           ";
			}
		}
		
		return regString;
	}
	
	
	
	
	
	

}
