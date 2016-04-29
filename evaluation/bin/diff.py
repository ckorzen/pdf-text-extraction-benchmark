def diff(actual, target):
    """ Finds the differences between the two given lists of strings. The lists
    may be nested arbitrarily to image any textual structures. 

    >>> diff([["foo", "bar"], "baz"], ["foo", "bar", "baz"]).all_items
    [(=, foo, [0, 0]), (=, bar, [1, 1]), (=, baz, [2, 2])]

    >>> diff(["foo", "bar", "baz"], ["foo", ["bar"]]).all_items
    [(=, foo, [0, 0]), (=, bar, [1, 1]), (-, baz, [2, 2])]

    >>> diff([["foo", "baz"]], ["foo", "bar", "baz"]).all_items
    [(=, foo, [0, 0]), (+, bar, [1, 1]), (=, baz, [1, 2])]
    """

    return diff_flat(flatten(actual), flatten(target))

def diff_flat(actual, target):
    """ Runs diff on the two given (flat!) lists. To flatten a list use the 
    flatten() method below. """

    result = DiffResult()
    _diff(actual, target, result)
    return result

def _diff(actual, target, result, positions = [0, 0]):
    """ Runs diff on the two given lists recursively. Appends the diff 
    items to the given result object. position[0] represents the current 
    position in 'actual' and positions[1] represents the current position in 
    'target'."""

    # Make copy of positions.
    positions = positions[:]

    # Map the strings from actual to their positions in actual.
    actual_dict = dict()  
    for i, actual_item in enumerate(actual):
        actual_dict.setdefault(actual_item.string, []).append(i)

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
        for j in actual_dict.get(target_item.string, []):
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
            result.append(DiffReplace(positions, actual, target))
            positions[0] += len(actual)
            positions[1] += len(target)
    else:
        # A common substring was found. Call diff recursively for the 
        # substrings to the left and to the right
        actual_left = actual[ : actual_start]
        target_left = target[ : target_start]
        positions = _diff(actual_left, target_left, result, positions)
        
        actual_middle = actual[actual_start : actual_start + length]
        target_middle = target[target_start : target_start + length]
        result.append(DiffCommon(positions, actual_middle, target_middle))
        positions[0] += length
        positions[1] += length
        
        actual_right = actual[actual_start + length : ]
        target_right = target[target_start + length : ]
        positions = _diff(actual_right, target_right, result, positions)
    return positions

# ______________________________________________________________________________

def flatten(hierarchy):
    """ Flattens the given hierarchy of strings to a flat list. Keeps track of 
    the position in the hierarchy of each string."""
    flattened = []
    flatten_recursive(hierarchy, flattened)
    return flattened

def flatten_recursive(hierarchy, result, pos_stack=[]):
    """ Flattens given (sub-)hierarchy and stores the result to given list."""

    for i, element in enumerate(hierarchy):
        new_pos_stack = pos_stack + [i]
        
        if isinstance(element, list):
            flatten_recursive(element, result, new_pos_stack)
        else:
            result.append(DiffInputItem(element, len(result), new_pos_stack))

# ==============================================================================

class DiffInputItem:
    def __init__(self, string, pos_flat, pos_stack): 
        self.string = string
        self.pos_flat = pos_flat
        self.pos_stack = pos_stack

# ______________________________________________________________________________

class DiffCommon:
    def __init__(self, positions, actual, target):
        self.items = []
        self.positions = positions
        for i, (a, b) in enumerate(zip(actual, target)):
            item_positions = [x + i for x in positions]
            self.items.append(DiffCommonItem(item_positions, a, b))

class DiffCommonItem:
    def __init__(self, positions, source_actual, source_target):
        self.positions = positions
        self.source_actual = source_actual
        self.source_target = source_target

    def __str__(self):
        return "(=, %s, %s)" % (self.source_target.string, self.positions)

    def __repr__(self):
        return self.__str__()

# ______________________________________________________________________________

class DiffReplace:
    def __init__(self, positions, actual, target):
        self.positions = positions
        self.delete = DiffDelete(positions, actual) if actual else None
        self.insert = DiffInsert(positions, target) if target else None

# ______________________________________________________________________________

class DiffInsert:
    def __init__(self, positions, target):
        self.items = []
        self.positions = positions
        for i, item in enumerate(target):
            item_positions = [positions[0], positions[1] + i]
            self.items.append(DiffInsertItem(item_positions, item))

class DiffInsertItem:
    def __init__(self, positions, source):
        self.positions = positions
        self.source = source
   
    def __str__(self):
        return "(+, %s, %s)" % (self.source.string, self.positions)

    def __repr__(self):
        return self.__str__()

# ______________________________________________________________________________

class DiffDelete:
    def __init__(self, positions, actual):
        self.items = []
        self.positions = positions
        for i, item in enumerate(actual):
            item_positions = [positions[0] + i, positions[1]]
            self.items.append(DiffDeleteItem(item_positions, item))

class DiffDeleteItem:
    def __init__(self, positions, source):
        self.positions = positions
        self.source = source

    def __str__(self):
        return "(-, %s, %s)" % (self.source.string, self.positions)

    def __repr__(self):
        return self.__str__()

# ______________________________________________________________________________

class DiffResult:
    def __init__(self):
        self.inserts, self.insert_items = [], []
        self.deletes, self.delete_items = [], []
        self.commons, self.common_items = [], []
        self.all, self.all_items = [], []
        self.commons_and_replaces = []

    def append(self, item):
        if isinstance(item, DiffCommon):
            self.commons_and_replaces.append(item)
            self.commons.append(item)
            self.common_items.extend(item.items)
            self.all.append(item)
            self.all_items.extend(item.items)
        elif isinstance(item, DiffReplace):
            self.commons_and_replaces.append(item)
            if item.delete:
                self.deletes.append(item.delete)
                self.delete_items.extend(item.delete.items)
                self.all.append(item.delete)
                self.all_items.extend(item.delete.items)
            if item.insert:
                self.inserts.append(item.insert)
                self.insert_items.extend(item.insert.items)
                self.all.append(item.insert)
                self.all_items.extend(item.insert.items)