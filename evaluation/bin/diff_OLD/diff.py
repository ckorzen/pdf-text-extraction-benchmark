from collections import Iterable
from itertools import zip_longest

def diff(actual, target):
    """ Compares the given lists of elements (strings or any iterables where 
    the first element is a string) and outputs a sequence of operations to 
    perform to transform 'actual' into 'target'. An operation is either a 
    'Common'-object, that identifies common phrases (sequence of strings) 
    between the two lists and 'Replace'-objects that identifies phrases to
    replace, to insert or to delete."""
    result = []
    
    _diff(actual, target, result)

    return result

def _diff(actual, target, result, pos1=0, pos2=0):
    """ Compares the given lists of elements recursively and puts the computed
    operation of this run into the result list. 'pos1' denotes the position in
    'actual' and 'pos2' denotes the position in 'target'. Both values have no
    special meaning but can be used for sorting purposes for example."""
    
    # Create an index of all positions of an string in 'actual'.
    actual_dict = dict()  
    for i, actual_item in enumerate(actual):
        actual_dict.setdefault(get_string(actual_item), []).append(i)

    # Find the largest substring common to 'actual' and 'target'.
    # 
    # We iterate over each value in 'target'. At each iteration, overlap[i] 
    # is the length of the largest suffix of actual[:i] equal to a suffix 
    # of target[:j] (or unset when actual[i] != target[j]).
    #
    # At each stage of iteration, the new overlap (called _overlap until 
    # the original overlap is no longer needed) is built from 'actual'.
    #
    # If the length of overlap exceeds the largest substring seen so far 
    # (length), we update the largest substring to the overlapping strings.
    overlap = {}

    # 'actual_start' is the index of the beginning of the largest overlapping
    # substring in 'actual'. 'target_start' is the index of the beginning 
    # of the same substring in 'target'. 'length' is the length that overlaps 
    # in both.
    # These track the largest overlapping substring seen so far, so 
    # naturally we start with a 0-length substring.
    actual_start = 0
    target_start = 0
    length = 0

    for i, target_item in enumerate(target):
        _overlap = {}
        for j in actual_dict.get(get_string(target_item), []):
            # now we are considering all values of i such that
            # actual[i] == target[j].
            _overlap[j] = (j and overlap.get(j - 1, 0)) + 1
            # Check if this is the largest substring seen so far.
            if _overlap[j] > length:
                length = _overlap[j]
                actual_start = j - length + 1
                target_start = i - length + 1
        overlap = _overlap

    if length == 0:
        # No common substring found. Create a diff replace.
        if actual or target:
            result.append(Replace(actual, target, pos1, pos2))
            pos1 += len(actual)
            pos2 += len(target)
    else:
        # A common substring was found. Call diff recursively for the 
        # substrings to the left and to the right
        actual_left = actual[ : actual_start]
        target_left = target[ : target_start]
        pos1, pos2 = _diff(actual_left, target_left, result, pos1, pos2)
        
        actual_middle = actual[actual_start : actual_start + length]
        target_middle = target[target_start : target_start + length]
        result.append(Common(actual_middle, target_middle, pos1, pos2))
        pos1 += length
        pos2 += length
        
        actual_right = actual[actual_start + length : ]
        target_right = target[target_start + length : ]
        pos1, pos2 = _diff(actual_right, target_right, result, pos1, pos2)

    return pos1, pos2

# ==============================================================================

def get_string(element):
    """ Returns the string of the given element. This may be the element itself
    (if it is of type 'str') or the first item within the element if the element
    is an iterable and non-empty. Returns 'None' if the element is neither a 
    string nor an iterable and non-empty."""
    if isinstance(element, str):
        return element
    elif isinstance(element, Iterable) and len(element) > 0:
        return get_string(element[0])

# ==============================================================================

class Diff:
    """ The super class for a diff phrase (sequence of diff items). """

    def __init__(self, sign, items_actual=[], items_target=[], pos_actual=[], 
            pos_target=[]):
        """ Creates a new diff phrase. """
        self.sign = sign

	    # TODO: Do we still need prev and next for each item? If yes, for what? 
        self.items_actual = []
        for i, item in enumerate(items_actual):
            if not isinstance(item, DiffItem):
                item = DiffItem(self, item, pos_actual + i, pos_target)
            if self.items_actual:
                prev_item = self.items_actual[-1]
                prev_item.next = item 
                item.prev = prev_item
            self.items_actual.append(item) 

        self.items_target = []
        for i, item in enumerate(items_target):
            if not isinstance(item, DiffItem):
                item = DiffItem(self, item, pos_actual, pos_target + i)
            if self.items_target:
                prev_item = self.items_target[-1]
                prev_item.next = item 
                item.prev = prev_item
            self.items_target.append(item)

    def __str__(self):
        return "(%s %s, %s)" % (self.sign, self.items_actual, self.items_target)

    def __repr__(self):
        return self.__str__()

class DiffItem:
    """ The super class for a diff item. """

    def __init__(self, parent, item, pos_actual, pos_target):
        """ Creates a new diff item. """
        self.pos = [pos_actual, pos_target]
        self.item = item
        self.parent = parent
        self.prev = None
        self.next = None

    @property
    def string(self):
        """ Returns the string of this diff item. """
        return get_string(self.item)

    def __str__(self):
        return str(self.item) + " " + str(self.pos)

    def __repr__(self):
        return self.__str__()

# ______________________________________________________________________________

class Common(Diff):
    """ A phrase of diff common items. """ 
    def __init__(self, items_actual, items_target, pos_actual, pos_target):
        super(Common, self).__init__("=", items_actual, items_target, 
            pos_actual, pos_target)

class Replace(Diff):
    """ A phrase of diff replace items. """ 
    def __init__(self, items_actual, items_target, pos_actual, pos_target):
        super(Replace, self).__init__("/", items_actual, items_target, 
            pos_actual, pos_target)
