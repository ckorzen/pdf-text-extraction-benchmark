def longest_increasing_subsequence(x):
    """ 
    Returns the longest increasing subsequence (LIS) in the given list of 
    integers.
    The implementation follows the method from the Wikipedia article about LIS:
    https://en.wikipedia.org/wiki/Longest_increasing_subsequence
    """    
    n = len(x)
    # stores the index of the predecessor of X[k] in the LIS ending at X[k].
    p = [0] * n
     
    # stores the index k of the smallest value X[k] such that there is an 
    # increasing subsequence of length j ending at X[k] on the range k ≤ i
    m = [0] * (n + 1) 
    
    # the length of the longest increasing subsequence found so far
    l = 0
    
    for i in range(n):
       # Binary search for the largest positive j <= l such that x[m[j]] < x[i]
       lo = 1
       hi = l
       while lo <= hi:
           mid = (lo + hi) // 2
           if x[m[mid]] < x[i]: ###
               lo = mid + 1
           else:
               hi = mid - 1
 
       # After searching, lo is 1 greater than the length of the longest prefix 
       # of x[i]
       newL = lo
       
       # The predecessor of x[i] is the last index of the subsequence of length 
       # newL - 1
       p[i] = m[newL - 1]
       m[newL] = i
 
       if (newL > l):
            # If we found a subsequence longer than any we have found yet, 
            # update L
            l = newL
 
    # Reconstruct the longest increasing subsequence
    s = [0] * l
    # Keep track of the indexes.
    indexes = [-1] * l
    k = m[l]
    for i in range(l - 1, -1, -1):
        s[i] = x[k]
        indexes[i] = k
        k = p[k]
    return s, indexes

def longest_increasing_continuous_subsequence(x):
    """ 
    Returns the longest increasing continuous subsequence in the given list of 
    integers.
    
    >>> longest_increasing_continuous_subsequence([1, 2, 3, 4, 5])
    [1, 2, 3, 4, 5]
    >>> longest_increasing_continuous_subsequence([2, 1, 3, 2, 4])
    [2, 3, 4]
    >>> longest_increasing_continuous_subsequence([1, 5, 4, 3, 2])
    [1, 2]
    """ 
       
    n = len(x)
    active_lists = []
    longest_list = []
    end_elements = {}
    
    for i in range(n):
        repr_value = get_repr_value(x[i])
    
        if repr_value - 1 in end_elements:
            list_index = end_elements[repr_value - 1]
            del end_elements[repr_value - 1]
            end_elements[repr_value] = list_index
            active_lists[list_index].append(x[i])
            if len(active_lists[list_index]) > len(longest_list):
                longest_list = active_lists[list_index]
        else:
            new_list = [x[i]]
            active_lists.append(new_list)
            end_elements[repr_value] = len(active_lists) - 1
            if len(new_list) > len(longest_list):
                longest_list = new_list
            
    return longest_list
  
def get_repr_value(element):
    """Returns the first value in the flattened given element.
    
    >>> get_repr_value(1)
    1
    >>> get_repr_value([1, 2, 3])
    1
    >>> get_repr_value([[[2, 1], [3, 4]], [5]])
    2
    >>> get_repr_value(((3, 2), (3, 4)))
    3
    """    
    
    # choose this criterion to also allow recordclass
    if not hasattr(element, "__len__") or isinstance(element, str): 
        return element
    
    for item in element:
        return get_repr_value(item)
    
def classlookup(cls):
    c = list(cls.__bases__)
    for base in c:
        c.extend(classlookup(base))
    return c
