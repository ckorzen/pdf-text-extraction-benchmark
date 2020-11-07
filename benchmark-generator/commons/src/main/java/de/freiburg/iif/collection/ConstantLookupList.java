package de.freiburg.iif.collection;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Implementation of a list that allows a constant lookup and a traversing of 
 * the list in either direction.
 *
 * @author Claudius Korzen
 *
 * @param <E>
 *          the type of elements in this list.
 */
public class ConstantLookupList<E> extends LinkedList<E> {
  /**
   * The serial version id.
   */
  protected static final long serialVersionUID = 1L;

  /**
   * Map that keeps track of the element's indexes in this list.
   */
  protected Map<E, Integer> map;

  /**
   * Creates a new empty list.
   */
  public ConstantLookupList() {
    super();
    this.map = new HashMap<>();
  }

  @Override
  public boolean addAll(Collection<? extends E> elements) {
    int index = size();
    for (E element : elements) {
      this.map.put(element, index++);
    }
    return super.addAll(elements);
  }

  @Override
  public boolean addAll(int index, Collection<? extends E> elements) {
    int idx = index;
    for (E element : elements) {
      this.map.put(element, idx++);
    }

    // Update the indices of all elements to the right of the insertion point.
    for (int i = index; i < size(); i++) {
      E element = get(i);
      this.map.put(element, map.get(element) + elements.size());
    }

    return super.addAll(index, elements);
  }

  @Override
  public boolean add(E element) {
    this.map.put(element, size());
    return super.add(element);
  }

  @Override
  public void add(int index, E element) {
    this.map.put(element, index);

    // Update the indices of all elements to the right of the insertion point.
    for (int i = index; i < size(); i++) {
      E el = get(i);
      this.map.put(el, map.get(el) + 1);
    }

    super.add(index, element);
  }

  // ___________________________________________________________________________

  @Override
  public boolean removeAll(Collection<?> elements) {
    boolean removed = true;
    for (Object obj : elements) {
      int index = map.get(obj);
      removeFromMap(obj);
      removed = (super.remove(index) != null ? true : false) || removed;
    }
    return removed;
  }

  @Override
  public boolean remove(Object element) {
    int index = map.get(element);
    removeFromMap(element);
    E removed = super.remove(index);
    return removed != null ? true : false;
  }

  @Override
  public E remove(int index) {
    removeFromMap(get(index));
    return super.remove(index);
  }

  /**
   * Removes the given element from map and updates the indices.
   */
  protected void removeFromMap(Object element) {
    int index = indexOf(element);
    this.map.remove(element);

    // Update the indices
    for (int i = index + 1; i < size(); i++) {
      map.put(get(i), i - 1);
    }
  }

  // ___________________________________________________________________________

  @Override
  public E set(int index, E element) {
    this.map.remove(get(index));
    this.map.put(element, index);
    return super.set(index, element);
  }

  @Override
  public int indexOf(Object obj) {
    if (map.containsKey(obj)) {
      return map.get(obj);
    }
    return -1;
  }

  @Override
  public void clear() {
    super.clear();
    this.map.clear();
  }

  /**
   * Returns the element that follows the given element.
   */
  public E next(E element) {
    int index = indexOf(element);
    if (index + 1 > 0 && index + 1 < size()) {
      return get(index + 1);
    }
    return null;
  }

  /**
   * Returns the element that is followed by the given element.
   */
  public E previous(E element) {
    int index = indexOf(element);
    if (index - 1 > -1) {
      return get(index - 1);
    }
    return null;
  }

  /**
   * Replaces the given element by the another given element.
   */
  public boolean replace(E el1, List<E> elements) {
    int index = indexOf(el1);
    if (index > -1 && !elements.isEmpty()) {
      set(index, elements.get(0));
      for (int i = 1; i < elements.size(); i++) {
        add(index + i, elements.get(i));
      }
    }
    return true;
  }
}
