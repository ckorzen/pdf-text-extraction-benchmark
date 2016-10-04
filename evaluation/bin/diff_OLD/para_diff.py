import diff
import util
import para_diff_rearrange as rearr
from collections import Iterable

def para_diff(actual, target, junk=[]):
    """ Finds the differences between the two given lists of paragraphs. """

    # 'actual' and 'target' may be arbitrarily nested list of words. 
    # In our case, both lists are once nested list of words of paragraphs.
    # example: 'actual_paras' = [['words', 'of', 'first', 'paragraph], [...]]

    # Flatten the list of words to be able to do a word-based diff. 
    actual_flatten = flatten(actual)
    target_flatten = flatten(target)

    # 'actual_flatten' and 'target_flatten' are now flat lists of tuples. Each
    # tuple (<word>, <flat_pos>, <pos_stack>) consists of:
    #   <word>      : The word
    #   <flat_pos>  : The pos of word in flat representation of original list.
    #   <pos_stack> : The position stack as list. The i-th element denotes the 
    #                 position of the word in the original list at level i.
    # example: flatten([['foo', 'bar'], ['baz']]) 
    #            = [('foo', 0, [0, 0]), ('bar', 1, [0, 1]), ('baz', 2, [1, 0])] 

    # Do a word-based diff on 'actual_flatten' and 'target_flatten'. 
    # The result is a list of diff.Common and diff.Replace objects denoting
    # the operations to perform to transform actual_flatten into target_flatten.
    # Both objects contain the related elements in 'actual_flatten' and 
    # 'target_flatten'
    #
    # examples: 
    # 
    # (= [('foo', 0, [0, 0]), ('bar', 1, [0, 1]), ('baz', 2, [1, 0])],
    #    [('foo', 0, [0, 0]), ('bar', 1, [0, 1]), ('baz', 2, [1, 0])])
    # denotes a common object including the related elements in actual_flatten 
    # and the elements in target_flatten. It implies that "foo bar baz" occurs
    # in both lists.
    #
    # (/ [('foo', 0, [0, 0]), ('bar', 1, [0, 1]), ('baz', 2, [1, 0])],
    #    [('doo', 0, [0, 0])])
    # denotes a replace object including the related elements in actual_flatten 
    # and the elements in target_flatten. It implies that "foo bar baz" in 
    # 'actual' is replaced by "doo" in 'target'.
    #
    # One of the element lists in diff.Replace objects my be empty, denoting 
    # either a insert or an deletion.
    diff_result = diff.diff(actual_flatten, target_flatten)

    # There could be phrases that occur in both, 'actual_flatten' and 
    # 'target_flatten' but their order doesn't correspond. Try to identify and
    # rearrange such phrases.
    rearrange_result = rearr.rearrange(diff_result, junk)
    
    # The rearrange result is now a flat list of diff.Common, diff.Replace and
    # rearr.Rearrange objects and doesn't meet any paragraph structures. 
    # So we need to split (and merge) the objects now to get operations per 
    # paragraph.

    para_result = []

    # Keep track of the previous actual item and the previous target item to
    # be able to decide where to split the objects.    
    prev_item_actual = None
    prev_item_target = None
    
    for item in rearrange_result:
        if isinstance(item, diff.Common):
            item = Commons(item, prev_item_actual, prev_item_target)
        elif isinstance(item, rearr.Rearrange):
            item = Rearranges(item, prev_item_actual, prev_item_target)
        elif isinstance(item, diff.Replace):
            item = Replaces(item, prev_item_actual, prev_item_target)

        # TODO: Obtain the previous actual item and the previous target item.
        if item and item.phrases:
            xxx = [x for x in item.phrases if not isinstance(x, Delete) and not util.ignore_phrase(x, junk)]
            if xxx:
                last_item = xxx[-1]
                if last_item.items_actual:
                    prev_item_actual = last_item.items_actual[-1]

            yyy = [y for y in item.phrases if not isinstance(y, Insert) and not util.ignore_phrase(y, junk)]
            if yyy:
                last_item = yyy[-1]
                if last_item.items_target:
                    prev_item_target = last_item.items_target[-1]

        para_result.append(item)

    # return merge(para_result)
    return para_result

#def merge(diff_result):
#    # TODO: Sort?

#    prev_actual_item = None
#    prev_target_item = None
#    for item in diff_result:
#        for phrase in item.phrases:
#            if phrase.items_actual and phrase.items_target:
#                actual_item = phrase.items_actual[0]
#                target_item = phrase.items_target[0]
#
#                if prev_actual_item and prev_target_item:
#                    has_diff_para_actual = has_diff_para(prev_actual_item, actual_item)
#                    has_diff_para_target = has_diff_para(prev_target_item, target_item)
#
#                    # Merge the item if there is a paragraph split between the
#                    # the items in actual, but not in target.
#                    if has_diff_para_actual and not has_diff_para_target:
#                        phrase.merge_ahead()
#
#            if phrase.items_actual and phrase.items_target:
#                prev_actual_item = phrase.items_actual[-1]
#                prev_target_item = phrase.items_target[-1]
#
#    return diff_result

# ==============================================================================

class Diff:
    def __init__(self, sign, items_actual, items_target):
        self.sign = sign
        self.items_actual = [DiffItem(x) for x in items_actual]
        self.items_target = [DiffItem(x) for x in items_target]
        self.prefix = []
        self.suffix = []

    def split_ahead(self):
        if not self.has_split_ahead():
            self.prefix.append(SplitParagraph())

    def split_behind(self):
        if not self.has_split_behind():
            self.suffix.append(SplitParagraph())

    def merge_ahead(self):
        if not self.has_merge_ahead():
            self.prefix.append(MergeParagraph())

    def merge_behind(self):
        if not self.has_merge_behind():
            self.suffix.append(MergeParagraph())

    def has_split_ahead(self):
        return any(isinstance(x, SplitParagraph) for x in self.prefix)

    def has_split_behind(self):
        return any(isinstance(x, SplitParagraph) for x in self.suffix)

    def has_merge_ahead(self):
        return any(isinstance(x, MergeParagraph) for x in self.prefix)

    def has_merge_behind(self):
        return any(isinstance(x, MergeParagraph) for x in self.suffix)

    @property
    def para_actual(self):
        return self.items_actual[0].para if self.items_actual else -1
    
    @property
    def para_target(self):
        return self.items_target[0].para if self.items_target else -1

    def __str__(self):
        parts = []
        parts.extend([str(x) for x in self.prefix])
        parts.append(self.to_string())
        parts.extend([str(x) for x in self.suffix])
        return ", ".join(parts)

    def to_string(self):
        return "(%s %s, %s)" % (self.sign, self.items_actual, self.items_target)

    def __repr__(self):
        return self.__str__()

class DiffItem:
    """ The super class for a diff item. """

    def __init__(self, item):
        """ Creates a new diff item. """
        self.item = item.item[0]
        self.pos = item.item[2]
        self.para = self.pos[0]

    @property
    def string(self):
        """ Returns the string of this diff item. """
        return self.get_string(self.item)

    def get_string(self, element):
        """ Returns the string of the given element. This may be the element itself
        (if it is of type 'str') or the first item within the element if the element
        is an iterable and non-empty. Returns 'None' if the element is neither a 
        string nor an iterable and non-empty."""
        if isinstance(element, str):
            return element
        elif isinstance(element, Iterable) and len(element) > 0:
            return self.get_string(element[0])

    def __str__(self):
        return str(self.item)

    def __repr__(self):
        return self.__str__()

# ______________________________________________________________________________

class Commons:
    def __init__(self, item, prev_item_actual, prev_item_target):
        self.phrases = self.split(item, prev_item_actual, prev_item_target)

    def split(self, common, prev_item_actual, prev_item_target):
        """ The diff.diff_result doesn't know paragraph boundaries. So a common
        may span over paragraph boundaries. Split them to get commons that live
        only in a single paragraph. """ 
        res = []
        start = 0
        split = False # Flag whether to split next sub_common in front.

        for i in range (0, len(common.items_actual)):
            item_actual = common.items_actual[i]
            item_target = common.items_target[i]

            has_diff_para_actual = has_diff_para(prev_item_actual, item_actual)
            has_diff_para_target = has_diff_para(prev_item_target, item_target)

            # To get common objects that live only in a single paragraph, split 
            # the common if there is a paragraph change in actual or in target.
            if has_diff_para_actual or has_diff_para_target:
                items_actual = common.items_actual[start : i]
                items_target = common.items_target[start : i]
                if items_actual and items_target:
                    res.append(Common(items_actual, items_target, split_ahead=split))
                    split = False
                if has_diff_para_target and not has_diff_para_actual:
                    split = True
                start = i

            prev_item_actual = item_actual
            prev_item_target = item_target

        items_actual = common.items_actual[start : ]
        items_target = common.items_target[start : ]
        if items_actual and items_target:
            res.append(Common(items_actual, items_target, split_ahead=split))

        return res

    def __str__(self):
        return ", ".join([str(x) for x in self.phrases])

    def __repr__(self):
        return self.__str__()

class Common(Diff):
    def __init__(self, items_actual, items_target, split_ahead=False):
        super(Common, self).__init__("=", items_actual, items_target)
        if split_ahead:
            self.split_ahead()

    def to_string(self):
        return "(%s %s)" % (self.sign, self.items_actual)

# ______________________________________________________________________________

class Rearranges:
    def __init__(self, item, prev_item_actual, prev_item_target):
        self.phrases = self.split(item, prev_item_actual, prev_item_target)
        self.needs_split_ahead_on_para_rearrange = item.needs_split_ahead_on_para_rearrange
        self.needs_split_behind_on_para_rearrange = item.needs_split_behind_on_para_rearrange

    def split(self, rearrange, prev_item_actual, prev_item_target):
        """ The diff.diff_result doesn't know paragraph boundaries. So a 
        rearrange may span over paragraph boundaries. Split them to get 
        rearranges that live only in a single paragraph. """
        res = []
        start = 0
        split = False # Flag whether to split next sub_common in front.
        for i in range (0, len(rearrange.items_actual)):
            item_actual = rearrange.items_actual[i]
            item_target = rearrange.items_target[i]

            has_diff_para_actual = has_diff_para(prev_item_actual, item_actual)
            has_diff_para_target = has_diff_para(prev_item_target, item_target)

            if has_diff_para_actual or has_diff_para_target:
                items_actual = rearrange.items_actual[start : i]
                items_target = rearrange.items_target[start : i]
                if items_actual and items_target:
                    res.append(Rearrange(items_actual, items_target, split_ahead=split))
                    split = False
                if has_diff_para_target and not has_diff_para_actual:
                    split = True
                start = i

            prev_item_actual = item_actual
            prev_item_target = item_target

        items_actual = rearrange.items_actual[start : ]
        items_target = rearrange.items_target[start : ]
        if items_actual and items_target:
            res.append(Rearrange(items_actual, items_target, split_ahead=split))

        return res

    def __str__(self):
        return ", ".join([str(x) for x in self.phrases])

    def __repr__(self):
        return self.__str__()

class Rearrange(Diff):
    def __init__(self, items_actual, items_target, split_ahead=False):
        super(Rearrange, self).__init__("<>", items_actual, items_target)
        if split_ahead:
            self.split_ahead()

    def to_string(self):
        return "(%s %s)" % (self.sign, self.items_actual)

# ______________________________________________________________________________

class Replaces:
    def __init__(self, item, prev_item_actual, prev_item_target):
        self.phrases = self.split(item, prev_item_actual, prev_item_target)

    def split(self, replace, prev_item_actual, prev_item_target):
        result = []

        # Split the actual items on paragraph boundaries.
        items_actual = replace.items_actual
        num_items_actual = len(items_actual)
        para_splits_actual = []
        for i in range(0, num_items_actual):
            item_actual = items_actual[i]

            if has_diff_para(prev_item_actual, item_actual):
                para_splits_actual.append(i)

            prev_item_actual = item_actual
        
        # Split the target items on paragraph boundaries.
        items_target = replace.items_target
        num_items_target = len(items_target)
        para_splits_target = []
        for i in range(0, num_items_target):
            item_target = items_target[i]

            if has_diff_para(prev_item_target, item_target):
                para_splits_target.append(i)

            prev_item_target = item_target

        paras_actual = partition(items_actual, para_splits_actual)
        paras_target = partition(items_target, para_splits_target)
        
        # The replace is only indeed a replace, if there is no paragraph split.
        # Otherwise, introduce Deletes and Replaces.
        if len(paras_actual) == 1 and len(paras_target) == 1:
            if not paras_actual[0] and paras_target[0]:
                result.append(Insert(paras_target[0]))
            elif paras_actual[0] and not paras_target[0]:
                result.append(Delete(paras_actual[0]))
            elif paras_actual[0] and paras_target[0]:
                result.append(Replace(paras_actual[0], paras_target[0]))
        else:
            for i in range(0, len(paras_target)):    
                if paras_target[i]:                
                    result.append(Insert(paras_target[i]))
            for i in range(0, len(paras_actual)):
                if paras_actual[i]:
                    result.append(Delete(paras_actual[i]))

        return result

class Replace(Diff):
    def __init__(self, items_actual, items_target):
        super(Replace, self).__init__("/", items_actual, items_target)

# ______________________________________________________________________________

class Insert(Diff):
    def __init__(self, items_target):
        super(Insert, self).__init__("+", [], items_target)

    def to_string(self):
        return "(%s %s)" % (self.sign, self.items_target)

class Delete(Diff):
    def __init__(self, items_actual):
        super(Delete, self).__init__("-", items_actual, [])

    def to_string(self):
        return "(%s %s)" % (self.sign, self.items_actual)

class SplitParagraph:
    def __str__(self):
        return "(‖)"

    def __repr__(self):
        return self.__str__()

class MergeParagraph:
    def __str__(self):
        return "(==)"

    def __repr__(self):
        return self.__str__()

# ==============================================================================

def flatten(hierarchy):
    """ Flattens the given hierarchy of strings to a flat list. Keeps track of 
    the position in the hierarchy of each string. """
    flattened = []
    flatten_recursive(hierarchy, flattened)
    return flattened

def flatten_recursive(hierarchy, result, pos_stack=[]):
    """ Flattens given (sub-)hierarchy and stores the result to given list. """

    for i, element in enumerate(hierarchy):
        new_pos_stack = pos_stack + [i]
        
        if isinstance(element, list):
            flatten_recursive(element, result, new_pos_stack)
        else:
            result.append((element, len(result), new_pos_stack))

def has_diff_para(prev_item, item):
    if prev_item and item:
        prev_para = prev_item.para if hasattr(prev_item, "para") else prev_item.item[2][0]
        para = item.para if hasattr(item, "para") else item.item[2][0]
        return para != prev_para
    return True

def partition(alist, indices):
    return [alist[i:j] for i, j in zip([0]+indices, indices+[None])]
