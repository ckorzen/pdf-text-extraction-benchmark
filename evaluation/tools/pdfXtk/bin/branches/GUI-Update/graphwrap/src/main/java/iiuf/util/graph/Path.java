package iiuf.util.graph;

import java.util.LinkedList;

/**
   Path encapsualtion, a path is a sequence of nodes.<p>

   (c) 2001, DIUF<p>
   
   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
*/
public class Path {
  /** 
      Path representing the parent, null if lastPathComponent represents
      the root. 
  */
  private Path                parentPath;
  /** Last path component. */
  transient private GraphNode lastPathComponent;
  
  /**
     Constructs a path from an array of GraphNodes.
     @param path  an array of GraphNodes representing the path to a node
  */
  public Path(GraphNode[] path) {
    if(path == null || path.length == 0)
      throw new IllegalArgumentException("path in Path must be non null and not empty.");
    lastPathComponent = path[path.length - 1];
    if(path.length > 1)
      parentPath = new Path(path, path.length - 1);
  }

  /**
     Constructs a path from a linked list of GraphNodes.
     @param path a linked list of GraphNodes representing the path to a node.
  */
  public Path(LinkedList path) {
    if(path == null || path.size() == 0)
      throw new IllegalArgumentException("path in Path must be non null and not empty.");
    lastPathComponent = (GraphNode)path.get(path.size() - 1);
    if(path.size() > 1)
      parentPath = new Path(path, path.size() - 1);
  }
  
  /**
     Constructs a Path containing only a single element.
     
     @param singlePath  a GraphNode representing the path to a node.
     @see #Path(GraphNode[])
  */
  public Path(GraphNode singlePath) {
    if(singlePath == null)
      throw new IllegalArgumentException("path in Path must be non null.");
    lastPathComponent = singlePath;
    parentPath = null;
  }
  
  /**
     Constructs a new Path, which is the path identified by
     <code>parent</code> ending in <code>lastElement</code>.
  */
  protected Path(Path parent, GraphNode lastElement) {
    if(lastElement == null)
      throw new IllegalArgumentException("path in Path must be non null.");
    parentPath = parent;
    lastPathComponent = lastElement;
  }
  
  /**
     Constructs a new Path with the identified path components of
     length <code>length</code>.
  */
  protected Path(GraphNode[] path, int length) {
    lastPathComponent = path[length - 1];
    if(length > 1)
      parentPath = new Path(path, length - 1);
  }
  
  /**
     Constructs a new Path with the identified path components of
     length <code>length</code>.
  */
  protected Path(LinkedList path, int length) {
    lastPathComponent = (GraphNode)path.get(length - 1);
    if(length > 1)
      parentPath = new Path(path, length - 1);
  }
  
  /**
     Returns an ordered array of GraphNodes containing the components of this
     Path. The first element (index 0) is the root.
     
     @return an array of GraphNodes representing the Path
     @see #Path(GraphNode[])
  */
  public GraphNode[] getPath() {
    int         i = getPathCount();
    GraphNode[] result = new GraphNode[i--];
    
    for(Path path = this; path != null; path = path.parentPath)
      result[i--] = path.lastPathComponent;
    return result;
  }
  
  /**
    Returns the last component of this path. 
    
    @return the GraphNode at the end of the path
    @see #Path(GraphNode[])
  */
  public GraphNode getLastPathComponent() {
    return lastPathComponent;
  }
  
  /**
     Returns the number of elements in the path.
     
     @return an int giving a count of items the path
  */
  public int getPathCount() {
    int        result = 0;
    for(Path path = this; path != null; path = path.parentPath)
      result++;
    return result;
  }
  
  /**
     Returns the path component at the specified index.
     
     @param element  an int specifying an element in the path, where
     0 is the first element in the path
     @return the GraphNode at that index location
     @throws IllegalArgumentException if the index is beyond the length
     of the path
     @see #Path(GraphNode[])
  */
  public GraphNode getPathComponent(int element) {
    int          pathLength = getPathCount();
    
    if(element < 0 || element >= pathLength)
      throw new IllegalArgumentException("Index " + element + " is out of the specified range");
    
    Path         path = this;
    
    for(int i = pathLength-1; i != element; i--)
      path = path.parentPath;
    return path.lastPathComponent;
  }
  
  /**
     Tests two Paths for equality by checking each element of the
     paths for equality. Two paths are considered equal if they are of
     the same length, and contain
     the same elements (<code>.equals</code>).
     
     @param o the GraphNode to compare
  */
  public boolean equals(GraphNode o) {
    if(o == this)
      return true;
    if(o instanceof Path) {
      Path            oPath = (Path)o;
      
      if(getPathCount() != oPath.getPathCount())
	return false;
      for(Path path = this; path != null; path = path.parentPath) {
	if (!(path.lastPathComponent.equals
	      (oPath.lastPathComponent))) {
	  return false;
	}
	oPath = oPath.parentPath;
      }
      return true;
    }
    return false;
  }
  
  /**
     Returns the hashCode for the object. The hash code of a Path
     is defined to be the hash code of the last component in the path.
     
     @return the hashCode for the object
  */
  public int hashCode() { 
    return lastPathComponent.hashCode();
  }
  
  /**
     Returns true if <code>aPath</code> is a
     descendant of this
     Path. A Path P1 is a descendent of a Path P2
     if P1 contains all of the components that make up 
     P2's path.
     For example, if this object has the path [a, b],
     and <code>aPath</code> has the path [a, b, c], 
     then <code>aPath</code> is a descendant of this object.
     However, if <code>aPath</code> has the path [a],
     then it is not a descendant of this object.
     
     @return true if <code>aPath</code> is a descendant of this path
  */
  public boolean isDescendant(Path aPath) {
    if(aPath == this)
      return true;
    
    if(aPath != null) {
      int                 pathLength = getPathCount();
      int                 oPathLength = aPath.getPathCount();
      
      if(oPathLength < pathLength)
	// Can't be a descendant, has fewer components in the path.
	return false;
      while(oPathLength-- > pathLength)
	aPath = aPath.getParentPath();
      return equals(aPath);
    }
    return false;
  }
  
  /**
     Returns a new path containing all the elements of this object
     plus <code>child</code>. <code>child</code> will be the last element
     of the newly created Path.
     This will throw a NullPointerException
     if child is null.
  */
  public Path pathByAddingChild(GraphNode child) {
    if(child == null)
      throw new NullPointerException("Null child not allowed");
    
    return new Path(this, child);
  }
  
  /**
    Returns a path containing all the elements of this object, except
    the last path component.
  */
  public Path getParentPath() {
    return parentPath;
  }
  
  /**
     Returns a string that displays and identifies this
     object's properties.
   
     @return a String representation of this object
  */
  public String toString() {
    StringBuffer tempSpot = new StringBuffer("[");
    
    for(int counter = 0, maxCounter = getPathCount();counter < maxCounter;
	counter++) {
      if(counter > 0)
	tempSpot.append(", ");
      tempSpot.append(getPathComponent(counter));
    }
    tempSpot.append("]");
    return tempSpot.toString();
  }
}
/*
  $Log: Path.java,v $
  Revision 1.1  2002/07/11 12:00:11  ohitz
  Initial checkin

  Revision 1.1  2001/05/11 11:33:05  schubige
  fns demo final
  
*/
