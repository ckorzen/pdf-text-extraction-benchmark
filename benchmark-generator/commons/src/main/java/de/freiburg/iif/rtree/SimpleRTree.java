package de.freiburg.iif.rtree;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.freiburg.iif.model.Geometric;
import de.freiburg.iif.model.HasRectangle;
import de.freiburg.iif.model.Rectangle;
import de.freiburg.iif.model.simple.SimpleRectangle;
import de.freiburg.iif.rtree.util.RTreeUtil;

/**
 * A basic implementation of a 2-dimensional R-tree. The implementation is close
 * to the remarks in the publication "R-TREES. A DYNAMIC INDEX STRUCTURE FOR
 * SPATIAL SEARCHING" by Antonin Guttmann
 * (http://www-db.deis.unibo.it/courses/SI-LS/papers/Gut84.pdf).
 * 
 * @author Claudius Korzen.
 * 
 * @param <T>
 *          the type of spatial objects to store in this tree.
 */
public class SimpleRTree<T extends HasRectangle> implements RTree<T> {
  /**
   * The root node of this R-tree.
   */
  protected RTreeNode root;

  /**
   * The minimum number of entries in a node; has to be <= maxNumEntriesPerNode
   * / 2.
   */
  protected final int minNumEntriesPerNode;

  /**
   * The maximum number of entries that will fit in one node.
   */
  protected final int maxNumEntriesPerNode;

  /**
   * The indexed entries.
   */
  protected List<T> indexEntries;

  /**
   * Creates a new r-tree with at least 2 entries per node and at most 50
   * entries per node.
   */
  public SimpleRTree() {
    this(2, 50);
  }

  /**
   * Creates a new r-tree filled with the given entries.
   * 
   * @param indexEntries
   *          the indexEntries to add.
   */
  public SimpleRTree(List<? extends T> indexEntries) {
    this();
    insert(indexEntries);
  }

  /**
   * Creates a new r-tree with at least <minNumEntriesPerNode> entries per node
   * and at most <maxNumEntriesPerNode> entries per node.
   * 
   * @param minNumEntriesPerNode
   *          the minimum number of entries per node.
   * @param maxNumEntriesPerNode
   *          the maximum number of entries per node.
   */
  public SimpleRTree(int minNumEntriesPerNode, int maxNumEntriesPerNode) {
    if (minNumEntriesPerNode > (maxNumEntriesPerNode / 2d)) {
      throw new IllegalArgumentException("minNumEntriesPerNode must be smaller "
          + "than or equal to (maxNumEntriesPerNode / 2)");
    }

    this.minNumEntriesPerNode = minNumEntriesPerNode;
    this.maxNumEntriesPerNode = maxNumEntriesPerNode;
    this.indexEntries = new ArrayList<T>();
    this.root = new RTreeNode();
  }

  @Override
  public void insert(List<? extends T> indexEntries) {
    for (T entry : indexEntries) {
      insert(entry);
    }
  }

  /**
   * Inserts a new index entry into this R-tree.
   * 
   * @param indexEntry
   *          The index entry to insert.
   */
  public void insert(T indexEntry) {
    if (indexEntry == null) {
      return;
    }

    if (indexEntry.getRectangle() == null) {
      return;
    }

    // Cache the index entry.
    this.indexEntries.add(indexEntry);

    // I1: Find position for new record: Invoke chooseLeaf to select a leaf node
    // in which to place the given index entry.
    RTreeNode leaf = chooseLeaf(root, indexEntry);

    // I2: Add the index entry to the leaf node.
    RTreeNode splitNode = addToNode(leaf, indexEntry);

    // If there was no room for the entry in the leaf, the node was split into
    // "node" and "splitNode". If the leaf wasn't split, splitNode is null.

    // I3: Propagate changes upward: Invoke adjustTree on the leaf node, also
    // passing splittedNode if a split was performed.
    // I4: Grow tree taller. If node split propagation caused the root to split,
    // create a new root whose children are the two resulting nodes.
    adjustTree(leaf, splitNode);
  }

  @Override
  public void clear() {
    this.root.clear();
    this.indexEntries.clear();
  }

  public boolean delete(T object) {
    throw new UnsupportedOperationException("Not yet implemented.");
  }

  @Override
  public List<T> containedBy(Geometric geometric) {
    List<T> result = new ArrayList<T>();

    containedBy(root, geometric.getRectangle(), result);

    return result;
  }
  
  @Override
  public List<T> contain(Geometric geometric) {
    List<T> result = new ArrayList<T>();

    contain(root, geometric.getRectangle(), result);

    return result;
  }

  @Override
  public List<T> overlappedBy(Geometric geometric) {
    return overlappedBy(geometric, 0);
  }

  @Override
  public List<T> overlappedBy(Geometric geometric, float minimumOverlapRatio) {
    List<T> result = new ArrayList<T>();
    
    overlappedBy(root, geometric.getRectangle(), minimumOverlapRatio, result);

    return result;
  }

  @Override
  public int size() {
    return this.indexEntries.size();
  }

  // ___________________________________________________________________________

  /**
   * Finds all index entries whose rectangle are completely contained in the
   * given search rectangle.
   * 
   * @param node
   *          the current node to inspect.
   * @param rect
   *          The search rectangle.
   * @param found
   *          The list to fill in the contained index entries.
   */
  @SuppressWarnings("unchecked")
  protected void containedBy(RTreeNode node, Rectangle rect, List<T> found) {
    // S1: Search subtrees.
    // If the root node is not a leaf, check each entry to determine, whether
    // the rectangle of the entry overlaps the search rectangle. For all
    // overlapping entries, invoke search() on the tree, whose root node is
    // pointed to by the entry.
    if (!node.isLeaf()) {
      for (HasRectangle child : node.getEntries()) {
        if (rect.overlaps(child.getRectangle())) {
          containedBy((RTreeNode) child, rect, found);
        }
      }
    } else {
      for (HasRectangle child : node.getEntries()) {
        if (rect.contains(child.getRectangle())) {
          found.add((T) child);
        }
      }
    }
  }
  
  /**
   * Finds all index entries whose rectangle contain the given search rectangle.
   * 
   * @param node
   *          the current node to inspect.
   * @param rect
   *          The search rectangle.
   * @param found
   *          The list to fill in the contained index entries.
   */
  @SuppressWarnings("unchecked")
  protected void contain(RTreeNode node, Rectangle rect, List<T> found) {
    // S1: Search subtrees.
    // If the root node is not a leaf, check each entry to determine, whether
    // the rectangle of the entry overlaps the search rectangle. For all
    // overlapping entries, invoke search() on the tree, whose root node is
    // pointed to by the entry.
    if (!node.isLeaf()) {
      for (HasRectangle child : node.getEntries()) {
        if (child.getRectangle().contains(rect)) {
          contain((RTreeNode) child, rect, found);
        }
      }
    } else {
      for (HasRectangle child : node.getEntries()) {
        if (child.getRectangle().contains(rect)) {
          found.add((T) child);
        }
      }
    }
  }

  /**
   * Finds all index entries whose rectangle are overlapped by the given search
   * rectangle.
   * 
   * @param node
   *          The current node to inspect.
   * @param rect
   *          The search rectangle.
   * @param minimumOverlapRatio
   *          the minimum overlap ratio.
   * @param found
   *          The list to fill in the overlapped index entries.
   */
  @SuppressWarnings("unchecked")
  protected void overlappedBy(RTreeNode node, Rectangle rect,
      float minimumOverlapRatio, List<T> found) {
    // S1: Search subtrees.
    // If the root node is not a leaf, check each entry to determine, whether
    // the rectangle of the entry overlaps the search rectangle. For all
    // overlapping entries, invoke search() on the tree, whose root node is
    // pointed to by the entry.
    
    if (!node.isLeaf()) {
      for (HasRectangle child : node.getEntries()) {
        if (rect.overlaps(child.getRectangle())) {
          overlappedBy((RTreeNode) child, rect, minimumOverlapRatio, found);
        }
      }
    } else {
      for (HasRectangle child : node.getEntries()) {
        if (minimumOverlapRatio == 0) {
          if (rect.overlaps(child.getRectangle())) {
            found.add((T) child);
          }
        } else {          
          float ratio = child.getRectangle().computeOverlapRatio(rect);
                    
          if (ratio >= minimumOverlapRatio) {
            found.add((T) child);
          }
        }
      }
    }
  }

  /**
   * Choose a leaf node in which to place the given index entry.
   * 
   * @param node
   *          the root node of the current subtree.
   * @param indexEntry
   *          the index entry.
   * @return the leaf node.
   */
  protected RTreeNode chooseLeaf(RTreeNode node, T indexEntry) {
    // (CL1: Initialize: Set the node to be the root node) => done by calling
    // chooseLeaf(root, indexEntry) in the insert method.

    if (node == null) {
      return null;
    }

    // CL2: Leaf check.
    if (node.isLeaf()) {
      // If the node is a leaf, return the node.
      return node;
    }

    // The node isn't a leaf.

    // CL3: Choose subtree.
    float leastEnlargement = Float.MAX_VALUE;
    RTreeNode leastEnlargementNode = null;

    Rectangle indexRect = indexEntry.getRectangle();
    
    for (HasRectangle childNode : node.getEntries()) {
      Rectangle childRect = childNode.getRectangle();
      float enlargement = RTreeUtil.computeEnlargement(childRect, indexRect);

      // Choose the child node whose rectangle needs least enlargement to
      // include indexEntry's rectangle.
      if (enlargement < leastEnlargement) {
        leastEnlargement = enlargement;
        leastEnlargementNode = (RTreeNode) childNode;
      }

      // Resolve ties by choosing the entry with the rectangle of smallest area.
      if (enlargement == leastEnlargement) {
        float area1 = childRect.getArea();
        float area2 = leastEnlargementNode.getRectangle().getArea();

        if (area1 < area2) {
          leastEnlargementNode = (RTreeNode) childNode;
        }
      }
    }

    // CL4: Descend until a leaf node is reached.
    return chooseLeaf(leastEnlargementNode, indexEntry);
  }

  /**
   * Adds the given entry to the given node.
   * 
   * @param node
   *          the node to process.
   * @param entry
   *          the entry to add.
   * 
   * @return the split node, if a split was necessary. Null otherwise.
   */
  protected RTreeNode addToNode(RTreeNode node, HasRectangle entry) {
    RTreeNode splitNode = null;

    if (node.getEntries().size() < maxNumEntriesPerNode) {
      // If the leaf has room for another entry, install the index entry.
      node.addEntry(entry);
    } else {
      // Otherwise, invoke splitNode to obtain the leaf node and the splitted
      splitNode = splitNode(node, entry);
    }

    return splitNode;
  }

  /**
   * Divides a overloaded node (node with more than maxNumEntriesPerNode
   * entries) into two nodes.
   * 
   * @param node
   *          the overloaded node.
   * @param entry
   *          the entry, which caused the split.
   * @return the split node.
   */
  protected RTreeNode splitNode(RTreeNode node, HasRectangle entry) {
    // Create two temporary nodes.
    RTreeNode node1 = new RTreeNode();
    RTreeNode node2 = new RTreeNode();

    // LS1: Pick the first entry for each group.
    // Apply pickSeeds to choose two entries to be the first elements of the
    // groups. Assign each to a group.
    List<HasRectangle> seeds = pickSeeds(node);

    node1.addEntry(seeds.get(0));
    node2.addEntry(seeds.get(1));

    node.removeEntry(seeds.get(0));
    node.removeEntry(seeds.get(1));

    // LS2: Check if done.
    while (!node.getEntries().isEmpty()) {
      // If one group has so few entries that all the rest must be assigned to
      // it in order for it to have the minimum number of entries, assign them.
      if (node.getEntries().size() < minNumEntriesPerNode
          && node1.getEntries().size() < minNumEntriesPerNode) {
        for (HasRectangle childNode : node.getEntries()) {
          node1.addEntry(childNode);
        }
        break;
      } else if (node.getEntries().size() < minNumEntriesPerNode
          && node2.getEntries().size() < minNumEntriesPerNode) {
        for (HasRectangle childNode : node.getEntries()) {
          node2.addEntry(childNode);
        }
        break;
      } else {
        // LS3: Select entry to assign.
        HasRectangle first = node.getEntries().iterator().next();
        node.getEntries().remove(first);
        assignToNode(node1, node2, first);
      }
    }

    // Assign the indexEntry.
    assignToNode(node1, node2, entry);

    // add the Index of the first split node to this index // TODO
    node.setEntries(node1.getEntries());

    // return the partner node
    return node2;
  }

  /**
   * Selects two entries to be the first elements of the groups using a linear
   * algorithm.
   * 
   * @param node
   *          the node to process.
   * 
   * @return a list of the two picked seeds.
   */
  protected List<HasRectangle> pickSeeds(RTreeNode node) {
    float hlsX = -Float.MAX_VALUE, lhsX = Float.MAX_VALUE;
    float hlsY = -Float.MAX_VALUE, lhsY = Float.MAX_VALUE;
    HasRectangle hlsXEntry = null, lhsXEntry = null;
    HasRectangle hlsYEntry = null, lhsYEntry = null;

    // LPS1: Find extreme rectangles along all dimensions: Along each dimension,
    // find the entry whose rectangle has the highest low side and the one with
    // the lowest high side.
    for (HasRectangle indexEntry : node.getEntries()) {
      Rectangle rect = indexEntry.getRectangle();

      if (rect.getMinX() > hlsX) {
        hlsX = rect.getMinX();
        hlsXEntry = indexEntry;
      }

      if (rect.getMaxX() < lhsX) {
        lhsX = rect.getMaxX();
        lhsXEntry = indexEntry;
      }

      if (rect.getMinY() > hlsY) {
        hlsY = rect.getMinY();
        hlsYEntry = indexEntry;
      }

      if (rect.getMaxY() < lhsY) {
        lhsY = rect.getMaxY();
        lhsYEntry = indexEntry;
      }
    }

    // Record the separations.
    // LPS2: Adjust for the shape of the rectangle cluster: Normalize the
    // separations by dividing by the width of the entire set along the
    // corresponding dimension
    float separationX = (hlsX - lhsX) / node.getRectangle().getWidth();
    float separationY = (hlsY - lhsY) / node.getRectangle().getHeight();

    List<HasRectangle> retValue = new ArrayList<HasRectangle>(2);

    // LPS3: Choose the pair with the greatest normalized separation along any
    // dimension.
    if (separationX > separationY) {
      retValue.add(lhsXEntry);
      retValue.add(hlsXEntry);
    } else {
      retValue.add(lhsYEntry);
      retValue.add(hlsYEntry);
    }

    return retValue;
  }

  /**
   * Assigns an index entry to an appropriate split node.
   * 
   * The index entry is added to the group whose covering bounds will have to be
   * enlarged least, the group with the smaller volume, or the one with fewer
   * entries.
   * 
   * @param node1
   *          the first node.
   * @param node2
   *          the second node.
   * @param entry
   *          the index entry to add.
   */
  protected void assignToNode(RTreeNode node1, RTreeNode node2,
      HasRectangle entry) {
    Rectangle rect1 = node1.getRectangle();
    Rectangle rect2 = node2.getRectangle();
    Rectangle entryRect = entry.getRectangle();

    float enlargement1 = RTreeUtil.computeEnlargement(rect1, entryRect);
    float enlargement2 = RTreeUtil.computeEnlargement(rect2, entryRect);

    if (enlargement1 > enlargement2) {
      // assign it to node 2
      addToNode(node2, entry);
    } else if (enlargement1 < enlargement2) {
      // assign it to node 1
      addToNode(node1, entry);
    } else {
      float area1 = SimpleRectangle.fromUnion(node1, entry).getArea();
      float area2 = SimpleRectangle.fromUnion(node2, entry).getArea();

      if (area1 > area2) {
        addToNode(node2, entry);
      } else if (area1 < area2) {
        addToNode(node1, entry);
      } else {
        if (node1.getEntries().size() < node2.getEntries().size()) {
          addToNode(node1, entry);
        } else {
          addToNode(node2, entry);
        }
      }
    }
  }

  /**
   * Adjusts the tree. Ascends from a leaf node to the root, adjusting covering
   * rectangles and propagating node splits as necessary.
   * 
   * @param node
   *          the node to inspect.
   * @param splitNode
   *          the (optional) second node, if the leaf was split previously.
   */
  protected void adjustTree(RTreeNode node, RTreeNode splitNode) {
    // AT1: Initialize: Set node to the leaf. If the leave was split previously,
    // set the splitNode to be the resulting node.

    Iterator<HasRectangle> entryItr = node.getEntries().iterator();

    // AT3: Adjust covering rectangle in parent entry.
    if (entryItr.hasNext()) {
      node.setRectangle(entryItr.next().getRectangle());
    }

    // Merge the bounds with each child
    while (entryItr.hasNext()) {
      Rectangle childRectangle = entryItr.next().getRectangle();
      node.setRectangle(SimpleRectangle.fromUnion(node, childRectangle));
    }

    // AT2: Check if done. If node is the root, stop.
    if (node.isRoot()) {
      if (splitNode != null) {
        // I4: Grow tree taller: If node split propagation caused the root to
        // split, create a new root whose children are the two resulting nodes.
        // move old root
        this.root = new RTreeNode();
        addToNode(this.root, node);
        addToNode(this.root, splitNode);
      }

      return;
    }

    RTreeNode newSplitNode = null;
    RTreeNode parent = node.getParent();
    if (splitNode != null) {
      // AT4: Propagate a node split upwards and move up to next level.
      newSplitNode = addToNode(parent, splitNode);
    }
    // AT5: Move up to next level.
    adjustTree(parent, newSplitNode);
  }

  @Override
  public String toString() {
    return root.toString();
  }

  @Override
  public RTreeNode getRoot() {
    return this.root;
  }

  @Override
  public List<T> getIndexEntries() {
    return indexEntries;
  }

  @Override
  public <S> List<S> getIndexEntriesByClass(Class<S> clazz) {
    List<S> result = new ArrayList<>();
    for (T entry : indexEntries) {
      if (entry != null && clazz.isInstance(entry)) {
        result.add(clazz.cast(entry));
      }
    }
    return result;
  }
}
