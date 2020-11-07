package de.freiburg.iif.rtree;

import java.util.List;

import de.freiburg.iif.model.Geometric;
import de.freiburg.iif.model.HasRectangle;

/**
 * The interface that declares the methods of an R-tree.
 * 
 * @author Claudius Korzen
 * 
 * @param <T>
 *          the type of index entries.
 */
public interface RTree<T extends HasRectangle> {  
  /**
   * Returns the root node of this tree.
   * 
   * @return the root node of this tree.
   */
  public RTreeNode getRoot();

  /**
   * Inserts the given object into this r-tree.
   * 
   * @param object
   *          The object to insert into this r-tree.
   */
  public void insert(T object);

  /**
   * Inserts the given objects into this r-tree.
   * 
   * @param objects
   *          The objects to insert into this r-tree.
   */
  public void insert(List<? extends T> objects);
  
  /**
   * Clears this index.
   */
  public void clear();
  
  /**
   * Deletes the given object from this r-tree.
   * 
   * @param object
   *          The object to delete from this r-tree.
   * 
   * @return true if the object was deleted false if the object was not found.
   */
  public boolean delete(T object);

  /**
   * Finds all objects, that are contained by the given geometric object.
   * 
   * @param geometric
   *          The geometric object.
   * 
   * @return The list of objects, which are contained by the geometric object.
   */
  public List<T> containedBy(Geometric geometric);
  
  /**
   * Finds all objects, which contain the given geometric object.
   * 
   * @param geometric
   *          The geometric object.
   * 
   * @return The list of objects, which contain the geometric object.
   */
  public List<T> contain(Geometric geometric);

  /**
   * Finds all objects, that are overlapped by the given geometric object.
   * 
   * @param geometric
   *          The geometric object.
   * 
   * @return The list of objects, which are overlapped by the geometric object.
   */
  public List<T> overlappedBy(Geometric geometric);

  /**
   * Finds all objects, that are overlapped by the given geometric object with
   * at least the given overlap ratio.
   * 
   * @param geometric
   *          The geometric object.
   * @param minimumOverlapRatio
   *          the minimum overlap ratio.
   * @return The list of objects, which are overlapped by the geometric object.
   */
  public List<T> overlappedBy(Geometric geometric, float minimumOverlapRatio);

  /**
   * Returns the number of objects in this r-tree.
   * 
   * @return the number of objects in this r-tree.
   */
  public int size();

  /**
   * Returns the list of index entries.
   * 
   * @return the list of index entries.
   */
  public List<T> getIndexEntries();
  
  /**
   * Returns the index entries, that are instances of the given class.
   */
  public <S> List<S> getIndexEntriesByClass(Class<S> clazz);
}
