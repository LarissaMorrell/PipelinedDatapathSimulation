package disassembler;

public class EXMEM implements PipelineInterface {
	
	private int write[];
	private int read[];
	
	
	/**
	 * EXMEM
	 * write/read = {(0)nop, (1)memRead, (2)memWrite, (3)memToReg, (4)regWrite,
	 * `             (5)writeRegNum, (6)aluResult, (7)swValue}
	 */
	
	
	public EXMEM(){
		this.write = new int[] {0, 0, 0, 0, 0, 0, 0, 0};
		this.read = new int[] {0, 0, 0, 0, 0, 0, 0, 0};
	
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
	 * values of the control signals
	 */
	
	@Override
	public String controlString() {
		String ctrl = "MemRead = " + getWriteValue(1) 
				+ ", memWrite = " + getWriteValue(2) 
				+ ", MemToReg = " + getWriteValue(3) 
				+ ", regWrite = " + getWriteValue(4);
		
		return ctrl;
	}



}
