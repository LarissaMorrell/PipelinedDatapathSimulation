package disassembler;

public interface PipelineInterface {
	
	public void setWriteValue(int index, int value); 
	
	public int getWriteValue(int index);  

	public int[] getWrite();
	
	public int getReadValue(int index);

	public int[] getRead();
	
	public String controlString();

}
