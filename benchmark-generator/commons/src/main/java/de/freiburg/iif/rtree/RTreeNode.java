package de.freiburg.iif.rtree;

import java.util.Collection;
import java.util.HashSet;

import de.freiburg.iif.model.HasRectangle;
import de.freiburg.iif.model.Rectangle;
import de.freiburg.iif.model.simple.SimpleRectangle;

/**
 * The implementation of a simple node in an R-Tree.
 * 
 * @author Claudius Korzen.
 *
 */
public class RTreeNode implements HasRectangle {
  /**
   * The pointers to the child entries.
   */
  protected Collection<HasRectangle> entries;

  /**
   * The pointer to the node's parent.
   */
  protected RTreeNode parent;

  /**
   * The minimum bounding rectangle of all child nodes.
   */
  protected Rectangle mbr;

  /**
   * Flag indicating, if this node is a leaf.
   */
  protected boolean isLeaf = true;

  /**
   * Creates a new R-tree node.
   */
  public RTreeNode() {
    this.entries = new HashSet<HasRectangle>();
  }

  /**
   * Returns the entries of this node.
   * 
   * @return the entries of this node.
   */
  public Collection<HasRectangle> getEntries() {
    return this.entries;
  }

  /**
   * Sets the entries of this node.
   * 
   * @param entries
   *          the index entries to set.
   */
  public void setEntries(Collection<? extends HasRectangle> entries) {
    this.entries.clear();
    this.isLeaf = true;

    for (HasRectangle entry : entries) {
      addEntry(entry);
    }
  }

  /**
   * Adds a new index entry to this node and recomputes the minimum bounding
   * rectangle.
   * 
   * @param entry
   *          the index entry to add.
   */
  public void addEntry(HasRectangle entry) {
    if (entry == null) {
      return;
    }
    // TODO: Don't allow to add node entries , if there already index objects 
    // (and vice versa).

    if (entry instanceof RTreeNode) {
      // If the entry to add is another node, this node cannot be a leaf.
      this.isLeaf = false;
      // Further, set the parent of the childNode to this node.
      RTreeNode childNode = (RTreeNode) entry;
      childNode.setParent(this);
    } else {
      // Entry is an index entry. Hence, this node must be a leaf.
      this.isLeaf = true;
    }

    this.entries.add(entry);

    // Adjust the minimum bounding rectangle.
    if (getRectangle() == null) {
      setRectangle(entry.getRectangle());
    } else {
      setRectangle(
          SimpleRectangle.fromUnion(getRectangle(), entry.getRectangle()));
    }
  }

  /**
   * Removes the given entry from this node.
   * 
   * @param entry
   *          the index entry to remove.
   */
  public void removeEntry(HasRectangle entry) {
    this.entries.remove(entry);
    this.isLeaf = getEntries().isEmpty();
  }

  /**
   * Clears the index entries of this node.
   * 
   * @param entry
   *          the index entry to remove.
   */
  public void clear() {
    this.entries.clear();
    this.isLeaf = true;
  }

  /**
   * Returns the minimum bounding rectangle of this node.
   * 
   * @return the minimum bounding rectangle of this node.
   */
  public Rectangle getRectangle() {
    return this.mbr;
  }

  /**
   * Sets the minimum bounding rectangle of this node.
   * 
   * @param mbr
   *          the minimum bounding rectangle to set.
   */
  protected void setRectangle(Rectangle mbr) {
    this.mbr = mbr;
  }

  /**
   * Indicates if this is a leaf node.
   * 
   * @return true if this is a leaf node.
   */
  public boolean isLeaf() {
    return this.isLeaf;
  }

  /**
   * Returns true if this node is a root node.
   * 
   * @return true if this node is a root node.
   */
  public boolean isRoot() {
    return getParent() == null;
  }

  /**
   * Returns the parent of this node.
   * 
   * @return the parent of this node.
   */
  public RTreeNode getParent() {
    return this.parent;
  }

  /**
   * Sets the parent of this node.
   * 
   * @param parent
   *          the parent to set.
   */
  public void setParent(RTreeNode parent) {
    this.parent = parent;
  }

  @Override
  public String toString() {
    StringBuffer sb = new StringBuffer();

    // Get the level of this node.
    int level = 0;
    RTreeNode parent = getParent();
    while (parent != null) {
      level++;
      parent = parent.getParent();
    }

    // Insert a number of tabs for each parent
    for (int i = 0; i < level; i++) {
      sb.append("\t");
    }
    sb.append(getClass().getName() + " (0x" + Long.toHexString(hashCode()) + ")"
        + " [rectangle=" + mbr + ", entries.size=" + entries.size() + ", "
        + "isRoot=" + isRoot() + ", isLeaf=" + isLeaf());

    if (getParent() != null) {
      sb.append(", parent=0x" + Long.toHexString(getParent().hashCode()) + "]");
    }

    if (!isLeaf()) {
      for (HasRectangle child : getEntries()) {
        sb.append("\n   ");
        sb.append(child.toString());
      }
    }

    return sb.toString();
  }
}
