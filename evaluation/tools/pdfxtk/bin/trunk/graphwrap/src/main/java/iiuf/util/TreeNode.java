package iiuf.util;

/**
   Tree node base class.

   (c) 1999, 2000, 2001, IIUF, DIUF<p>
   
   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
*/
abstract public class TreeNode {
  protected TreeNode p;
  protected Object   value;

  public abstract TreeNode[] getChilds();
  
  private static final String PREFIX = "  ";
  
  /**
     Creates a new tree node with the given value.
     @param value The value of this node.
  */
  public TreeNode(Object value_) {
    value = value_;
  }

  /**
     Gets the value associated with this node.
     
     @return The associated value.
  */
  public final Object getValue() {
    return value;
  }

  public String toString(int level, Object NIL) {
    String result = Strings.repeat(PREFIX, level);
    level++;
    result += "<node " + toString() + ">\n";
    TreeNode[] childs = getChilds();
    for(int i = 0; i < childs.length; i++) {
      result += 
	childs[i] == NIL ? 
	Strings.repeat(PREFIX, level) + "<node/>\n" : 
	childs[i].toString (level, NIL);
    }
    level--;
    return result + Strings.repeat(PREFIX, level) + "</node>\n";
  }
}

/*
  $Log: TreeNode.java,v $
  Revision 1.1  2002/07/11 12:00:11  ohitz
  Initial checkin

  Revision 1.2  2001/01/04 16:28:42  schubige
  Header update for 2001 and DIUF

  Revision 1.1  1999/10/07 11:02:13  schubige
  Added red black and binary tree classes
  
*/
