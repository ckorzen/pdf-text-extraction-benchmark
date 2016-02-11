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
           if x[m[mid]] < x[i]:
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

