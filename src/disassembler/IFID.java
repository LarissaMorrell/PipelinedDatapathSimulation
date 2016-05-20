package disassembler;

public class IFID implements PipelineInterface{
	 int write[];
	 int read[];

	public IFID(){

		this.write = new int[] {0};
		this.read = new int[] {0};

	}






	/**
	 * These 2 methods are the setters and getters for a 
	 * value in the WRITE array (the only value)
	 */


	public void setWriteValue(int index, int value){
		write[index] = value;
	}

	public int getWriteValue(int index){
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
	 * This method is the getter for a 
	 * value in the READ array (the only value)
	 */

	@Override
	public int getReadValue(int index){
		return read[index];
	}
	
	
	
	
	/**
	 * The next 2 methods are the setters and getters 
	 * for the entire READ array
	 */

	@Override
	public int[] getRead() {
		return read;
	}

	public void setRead(int[] read) {
		this.read = read;
	}
	
	
	
	/**
	 * This method is part of the interface, but not 
	 * needed for this class.
	 */

	@Override
	public String controlString() {
		return null;
	}


}
