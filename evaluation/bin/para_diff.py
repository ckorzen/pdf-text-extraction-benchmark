import diff
import para_diff_rearrange as rearr
import util

def para_diff(actual, target, junk=[]):
    """ Finds the differences between the two given lists of paragraphs. """

    # Run word-based diff.
    diff_result = diff.diff(flatten(actual), flatten(target))
    
    # Rearrange phrases.
    rearrange_result = rearr.rearrange(diff_result, junk)
    
    para_result = []
    prev_item_actual = None
    prev_item_target = None
    for item in rearrange_result:
        if isinstance(item, diff.Common):
            item = Commons(item, prev_item_actual, prev_item_target)
        elif isinstance(item, rearr.Rearrange):
            item = Rearranges(item, prev_item_actual, prev_item_target)
        elif isinstance(item, diff.Replace):
            item = Replaces(item, prev_item_actual, prev_item_target)

        # TODO
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

    return merge(para_result)

def merge(diff_result):
    # TODO: Sort?

    prev_actual_item = None
    prev_target_item = None
    for item in diff_result:
        for phrase in item.phrases:
            if phrase.items_actual and phrase.items_target:
                actual_item = phrase.items_actual[0]
                target_item = phrase.items_target[0]

                if prev_actual_item and prev_target_item:
                    has_diff_para_actual = has_diff_para(prev_actual_item, actual_item)
                    has_diff_para_target = has_diff_para(prev_target_item, target_item)

                    if has_diff_para_actual and not has_diff_para_target:
                        phrase.merge_ahead()

            if phrase.items_actual and phrase.items_target:
                prev_actual_item = phrase.items_actual[-1]
                prev_target_item = phrase.items_target[-1]

    return diff_result

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
        self.string = item.item[0]
        self.pos = item.item[2]
        self.para = self.pos[0]

    def __str__(self):
        return self.string

    def __repr__(self):
        return self.__str__()

# ______________________________________________________________________________

class Commons:
    def __init__(self, item, prev_item_actual, prev_item_target):
        self.phrases = self.split(item, prev_item_actual, prev_item_target)

    def split(self, common, prev_item_actual, prev_item_target):
        res = []
        start = 0
        split = False # Flag whether to split next sub_common in front.

        for i in range (0, len(common.items_actual)):
            item_actual = common.items_actual[i]
            item_target = common.items_target[i]

            has_diff_para_actual = has_diff_para(prev_item_actual, item_actual)
            has_diff_para_target = has_diff_para(prev_item_target, item_target)

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
        for i in range(1, num_items_actual):
            prev_item_actual = items_actual[i - 1]
            item_actual = items_actual[i]

            if has_diff_para(prev_item_actual, item_actual):
                para_splits_actual.append(i)
        
        # Split the target items on paragraph boundaries.
        items_target = replace.items_target
        num_items_target = len(items_target)
        para_splits_target = []
        for i in range(1, num_items_target):
            item_target = items_target[i]
            prev_item_target = items_target[i - 1]

            if has_diff_para(prev_item_target, item_target):
                para_splits_target.append(i)

        if not items_actual and items_target:
            # Insert
            paras_target = partition(items_target, para_splits_target)
            result.extend([Insert(para) for para in paras_target])
        elif items_actual and not items_target:
            # Delete
            paras_actual = partition(items_actual, para_splits_actual)
            result.extend([Delete(para) for para in paras_actual])
        else:
            paras_actual = partition(items_actual, para_splits_actual)
            paras_target = partition(items_target, para_splits_target)

            common_para_num = min(len(paras_actual), len(paras_target))
            for i in range(0, common_para_num):
                result.append(Replace(paras_actual[i], paras_target[i]))
            for i in range(common_para_num, len(paras_actual)):
                result.append(Replace(paras_actual[i], []))
            for i in range(common_para_num, len(paras_target)):
                result.append(Replace([], paras_target[i]))

        return result

    def split2(self, replace, prev_item_actual, prev_item_target):
        result = []

        items_actual = replace.items_actual
        items_target = replace.items_target
        num_items_actual = len(items_actual)
        num_items_target = len(items_target)

        common_len = min(num_items_actual, num_items_target)
     
        if common_len > 0:
            # First 'common-len'-th words are handled as Replace
            replace_items_actual = items_actual[ : common_len]
            replace_items_target = items_target[ : common_len]

            para_splits_actual = []
            para_splits_target = []
            for i in range(1, common_len):
                item_actual = replace_items_actual[i]
                item_target = replace_items_target[i]
                prev_item_actual = replace_items_actual[i - 1]
                prev_item_target = replace_items_target[i - 1]

                if has_diff_para(prev_item_actual, item_actual):
                    para_splits_actual.append(i)
                if has_diff_para(prev_item_target, item_target):
                    para_splits_target.append(i)

            paras_actual = partition(replace_items_actual, para_splits_actual)
            paras_target = partition(replace_items_target, para_splits_target)

            if len(paras_actual) == 1 and len(paras_target) == 1:
                result.append(Replace(replace_items_actual, replace_items_target))
            else:
                common_para_num = min(len(paras_actual), len(paras_target))
                # If actual = A / B / C and target = W X / Y Z then align them as follows: 
                # Replace ('A', 'W X')
                # Replace ('B', 'Y Z')
                # Delete ('C')
                for i in range(0, common_para_num):
                    result.append(Replace(paras_actual[i], paras_target[i]))
                for i in range(common_para_num, len(paras_actual)):
                    result.append(Replace(paras_actual[i], []))
                for i in range(common_para_num, len(paras_target)):
                    result.append(Replace([], paras_target[i]))

            if num_items_actual - common_len > 0:
                # Remaining actual words are handled as Delete
                delete_items = items_actual[common_len : ]

                para_splits = []
                for i in range(1, num_items_actual- common_len):
                    delete_item = delete_items[i]
                    prev_delete_item = delete_items[i - 1]

                    if has_diff_para(prev_delete_item, delete_item):
                        para_splits.append(i)

                paras = partition(delete_items, para_splits)
                result.extend([Replace(para, []) for para in paras])

            if num_items_target - common_len > 0:
                # Remaining actual words are handled as Insert
                insert_items = items_target[common_len : ]

                para_splits = []
                for i in range(1, num_items_target- common_len):
                    insert_item = insert_items[i]
                    prev_insert_item = insert_items[i - 1]

                    if has_diff_para(prev_insert_item, insert_item):
                        para_splits.append(i)

                paras = partition(insert_items, para_splits)
                result.extend([Replace([], para) for para in paras])
        elif num_items_actual > 0:
            para_splits = []
            for i in range(1, num_items_actual):
                delete_item = items_actual[i]
                prev_delete_item = items_actual[i - 1]

                if has_diff_para(prev_delete_item, delete_item):
                    para_splits.append(i)

            paras = partition(items_actual, para_splits)
            result.extend([Delete(para) for para in paras])
        elif num_items_target > 0:
            para_splits = []
            for i in range(1, num_items_target):
                insert_item = items_target[i]
                prev_insert_item = items_target[i - 1]

                if has_diff_para(prev_insert_item, insert_item):
                    para_splits.append(i)

            paras = partition(items_target, para_splits)
            result.extend([Insert(para) for para in paras])

        return result

    def __str__(self):
        return ", ".join([str(x) for x in self.phrases])

    def __repr__(self):
        return self.__str__()

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
            result.append((element, len(result), new_pos_stack))

def has_diff_para(prev_item, item):
    if prev_item and item:
        prev_para = prev_item.para if hasattr(prev_item, "para") else prev_item.item[2][0]
        para = item.para if hasattr(item, "para") else item.item[2][0]
        return para != prev_para
    return True

def partition(alist, indices):
    return [alist[i:j] for i, j in zip([0]+indices, indices+[None])]