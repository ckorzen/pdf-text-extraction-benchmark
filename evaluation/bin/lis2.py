from collections import defaultdict
import numbers

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
    # increasing subsequence of length j ending at X[k] on the range k <= i
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

def increasing_continuous_subsequences(x, debug=False):
    """ 
    Returns all increasing continuous subsequences in the given list.
    """ 
       
    n = len(x)
    active_lists = []
    end_elements = {}
    
    for i in range(n):
        repr_value = get_repr_value(x[i])
        
        if repr_value - 1 in end_elements:
            list_index = end_elements[repr_value - 1]
            del end_elements[repr_value - 1]
            end_elements[repr_value] = list_index
            active_lists[list_index].append(x[i])
        else:
            new_list = [x[i]]
            active_lists.append(new_list)
            end_elements[repr_value] = len(active_lists) - 1
                                 
    return active_lists

def longest_increasing_continuous_subsequence(x, debug=False):
    """ 
    Returns the longest increasing continuous subsequence in the given list.
    
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
           
        if repr_value is None:
            continue
            
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

def longest_run_in_lists(lists):  
    most_longest_run = []
    
    for l in lists:
        longest_run = longest_run_in_list(l)
        if len(longest_run) > len(most_longest_run):
            most_longest_run = longest_run
            
    return most_longest_run

def longest_run_in_list(lis):    
    active_lists = []
    longest_list = []
    only_unmatches_so_far = True
    only_unmatches_index = -1
    
    # The lists by their end elements. For example, if the lists at indices
    # 1, 2, 3 end with '5' and the lists at indices 4 and 5 ends with '7' the 
    # map looks like: { 5: {1, 2, 3}, 7: {4, 5} }
    lists_by_end_elements = defaultdict(lambda: set())
    
    # The end elements by lists. For the example above, this map looks like:
    # { 1: 5, 2: 5, 3: 5, 4: 7, 5: 7 }
    end_elements_by_lists = defaultdict(lambda: set())
    
    def append_to_list(element, list_index):
        pos = element.deletion.pos1 if element is not None else None
        
        # Append the element to the current list.
        active_lists[list_index].append(element)
                    
        # Update the maps
        end_elements = end_elements_by_lists[list_index]
        for end_element in end_elements:
            lists_by_end_elements[end_element].discard(list_index)
        lists_by_end_elements[pos].add(list_index)
        end_elements_by_lists[list_index] = set([pos])
                                        
        # Check, if the current list is now the longest list. 
        if len(current_list) > len(longest_list):
            longest_list = current_list
    
    def introduce_new_list(element):
        pos = element.deletion.pos1 if element is not None else None
            
        new_list = [element]
        active_lists.append(new_list)
        list_index = len(active_lists) - 1
                    
        # Update the maps.
        lists_by_end_elements[pos].add(list_index)
        end_elements_by_lists[list_index] = set([pos])
                    
        # Check, if the new list is the longest list.     
        if len(new_list) > len(longest_list):
            longest_list = new_list
        return new_list, list_index
    
        
    for element in lis:
        # Obtain the position in the groundtruth.
        pos = element.deletion.pos1 if element is not None else None
        
        if pos is not None: # There is a position in groundtruth
            # Check, if there are lists with end element "pos - 1"
            if pos - 1 in lists_by_end_elements:
                # There are lists that end with 'pos - 1'. 
                list_indices = lists_by_end_elements[pos - 1].copy()
                
                # Append the element to all of them.
                for list_index in list_indices:
                    append_to_list(element, list_index)
            else:
                if only_unmatches_so_far and only_unmatches_index > -1:
                    append_to_list(element, only_unmatches_index)
                else:
                    # There is no list that ends with 'pos - 1'. Create a new one.
                    introduce_new_list(element)
            only_unmatches_so_far = False
        else: # There is no position in groundtruth
            if only_unmatches_so_far and only_unmatches_index == -1:
                only_unmatches_index = introduce_new_list(element)                    
            else:
                for j, active_list in enumerate(active_lists):
                    # Append the element to list.
                    active_list.append(element)
                    
                    pos = active_list[-1].deletion.pos1 + 1 if active_list[-1] is not None else None
                    lists_to_end_elements[j].add(pos)
                    end_elements_to_lists[pos].add(j)
                                        
                    if len(active_list) > len(longest_list):
                        longest_list = active_list
                

def longest_increasing_continuous_subsequences_with_gaps(x):
    """ 
    Returns all increasing continuous subsequences in the given list.
    
    >>> increasing_continuous_subsequences_with_placeholders([1, 2, 3])
    [1, 2, 3]
    >>> increasing_continuous_subsequences_with_placeholders([1, 2, -1, 3])
    [1, 2, -1, 3]
    >>> increasing_continuous_subsequences_with_placeholders([1, 2, -1, 4])
    [1, 2, -1, 4]
    >>> increasing_continuous_subsequences_with_placeholders([1, 2, -1, 5])
    [1, 2, -1]
    >>> increasing_continuous_subsequences_with_placeholders([5, -1, 2, -1, 3, 6, 10, -1, 8, 9])
    [5, -1, -1, 6, -1, 8, 9]
    """ 
           
    n = len(x)
    active_lists = []
    longest_list = []
    lists_to_end_elements = defaultdict(lambda: set())
    end_elements_to_lists = defaultdict(lambda: set())
    
    preceding_placeholders = None
    
    for i in range(n):
        # Obtain an representing integer value for the current item.
        repr_value = get_repr_value(x[i])
                  
        if repr_value is not None:
            # The item isn't a "special" item.
            if repr_value - 1 in end_elements_to_lists:
                list_indices = end_elements_to_lists[repr_value - 1].copy()
                for list_index in list_indices:
                    active_lists[list_index].append(x[i])
                    
                    if len(active_lists[list_index]) > len(longest_list):
                        longest_list = active_lists[list_index]
                    
                    previous_end_elements = lists_to_end_elements[list_index]
                    for end_element in previous_end_elements:
                        end_elements_to_lists[end_element].discard(list_index)    
                    
                    lists_to_end_elements[list_index] = set([repr_value])
                    end_elements_to_lists[repr_value].add(list_index)
            else:
                if not preceding_placeholders:
                    new_list = [x[i]]
                
                    active_lists.append(new_list)
                    
                    if len(new_list) > len(longest_list):
                        longest_list = new_list
                else:
                    preceding_placeholders.append(x[i])
                    
                    if len(preceding_placeholders) > len(longest_list):
                        longest_list = preceding_placeholders
                    
                    preceding_placeholders = None
                
                list_index = len(active_lists) - 1
                lists_to_end_elements[list_index] = set([repr_value])
                end_elements_to_lists[repr_value].add(list_index)
        else:
            if not active_lists:
                if not preceding_placeholders:
                    preceding_placeholders = []
                    active_lists.append(preceding_placeholders)    
                preceding_placeholders.append(x[i])
                
                if len(preceding_placeholders) > len(longest_list):
                        longest_list = preceding_placeholders
            else:
                for j, active_list in enumerate(active_lists):
                    repr_value = 0
                    prev_repr_value = get_repr_value(active_list[-1])
                    if prev_repr_value is not None:
                        repr_value = prev_repr_value + 1
                    active_list.append(x[i])
                    
                    if len(active_list) > len(longest_list):
                        longest_list = active_list
                    
                    lists_to_end_elements[j].add(repr_value)
                    end_elements_to_lists[repr_value].add(j) 
                          
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
    
