package at.ac.tuwien.dbai.pdfwrap.model.document;

public class OpTuple
{
	protected int opIndex;
	protected int argIndex;
	
	public OpTuple(int opIndex, int argIndex)
	{
		this.opIndex = opIndex;
		this.argIndex = argIndex;
	}
	
	public int getOpIndex() {
		return opIndex;
	}
	public void setOpIndex(int opIndex) {
		this.opIndex = opIndex;
	}
	public int getArgIndex() {
		return argIndex;
	}
	public void setArgIndex(int argIndex) {
		this.argIndex = argIndex;
	}
	
}
