package edu.isi.bmkeg.lapdf.model;

import java.io.Serializable;


/**
 * This interface denotes a block on a page (which could be the whole page)
 * 
 * @author burns
 *
 */
public interface Block {

	public static final String MIDLINE = "MIDLINE";
	public static final String LEFT = "LEFT";
	public static final String RIGHT = "RIGHT";
	
	public PageBlock getPage();

	public void setPage(PageBlock page);
    
    public Block getContainer();

	public void setContainer(Block block);

	public String readLeftRightMidLine();

	public boolean isFlush(String condition, int value);


}
