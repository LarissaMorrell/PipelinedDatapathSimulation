package disassembler;

public class MEMWB implements PipelineInterface{
	
	private int write[];
	private int read[];
	

	/**
	 * MEMWB
	 * write/read = {(0)nop, (1)memToReg, (2)regWrite, (3)lwDataValue, 
	 *               (4)aluResult, (5)writeRegNum}
	 */
	
	public MEMWB(){
		this.write = new int[] {0, 0, 0, 0, 0, 0};
		this.read = new int[] {0, 0, 0, 0, 0, 0};
		
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
		String ctrl = "MemToReg = " + write[1] 
				+ ", RegWrite = " +  write[2];
		
		return ctrl;
	}




	

}
