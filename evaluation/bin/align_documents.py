import sys
import diff_align
from util import to_formatted_paragraphs
from collections import Counter
from collections import OrderedDict

# The multiplication factor on computing the costs for paragraph operations. 
COST_FACTOR_PARA_OPS = 2
# The multiplication factor on computing the costs of word operations.
COST_FACTOR_WORD_OPS = 1

def align_strings(actual, target, junk=[]):
    """ Aligns the given string 'actual' against the given string 'target'.
    Both strings are seen as sequences of paragraphs. Returns a sequence of 
    operations to apply to transform 'actual' into 'target'. The operations
    under consideration are:

    * Split paragraph.
    * Merge paragraph.
    * Delete paragraph.
    * Rearrange paragraph.
    * Insert word.
    * Delete word.
    * Rearrange word. 
    """

    # Split both string into paragraphs.
    actual_paras = to_formatted_paragraphs(actual, to_protect=junk)
    target_paras = to_formatted_paragraphs(target, to_protect=junk)

    # Align the paragraphs.
    return align_paragraphs(actual_paras, target_paras, junk)

def align_paragraphs(actual_paras, target_paras, junk=[]):
    """ Aligns the given lists of paragraphs. """

    # Run diff to identify the phrases to insert, to delete and to rearrange.
    diff_result = diff_align.diff(actual_paras, target_paras, junk)

    # Diff doesn't consider paragraph boundaries. So we need to group the diff 
    # items by actual paragraphs.
    diff_actual = split_into_actual_paras(diff_result)

    # 'diff_actual' doesn't contain the phrases to insert (the phrases of 
    # 'target' that don't occur in 'actual'). Create index of such phrases. 
    ins_idx = create_to_insert_index(diff_result)

    # Process the diff result: insert, delete, split and rearrange phrases.
    ops = apply_inserts_deletes_splits_rearranges(diff_actual, ins_idx)

    return merge(ops)

# ______________________________________________________________________________

def apply_inserts_deletes_splits_rearranges(diff_actual, ins_idx):
    """ Iterates through 'diff_actual' and decides which operations to apply.
    'diff_actual' is a list of list of diff items (per actual paragraph). 
    'ins_idx' is a dictionary of dictionary of diff insert items. The outer 
    dictionary maps to the paragraph number, the inner dictionary maps to the 
    position within the paragraph.""" 

    # The performed operations per paragraph.
    ops_per_para = []
    
    # Iterate through each actual paragraph.
    for actual_para in diff_actual:
        para_ops = []
        prev_op = None

        # Iterate through the diff items of the paragraph.
        for i, diff_item in enumerate(actual_para):
            # Obtain the next diff item of the paragraph.
            next_item = actual_para[i+1] if i < len(actual_para) - 1 else None

            ops = []
            if isinstance(diff_item, CommonWrapper):
                ops = common(diff_item, prev_op, next_item)
            elif isinstance(diff_item, RearrangeWrapper):
                ops = rearrange(diff_item, prev_op, next_item)
            elif isinstance(diff_item, DeleteWrapper):
                ops = delete(diff_item, prev_op, next_item)

            for op in ops: 
                # Ignore deletions (but append it to operations).
                if type(op) in [DeleteWords, DeleteParagraph]:
                    para_ops.append(op)
                    continue

                # Check, if there is an operation to insert between the 
                # previous op and the current op.
                ins_item = pop_insert(ins_idx, prev_op, op)
                ins_ops = insert(ins_item, prev_op, op)
                
                for ins_op in ins_ops:
                    if ins_op.is_splitted_ahead or (prev_op and prev_op.is_splitted_behind):
                        if para_ops:
                            ops_per_para.append(para_ops)
                        para_ops = []
                    para_ops.append(ins_op)
                    prev_op = ins_op

                # Introduce new paragraph if the item was splitted ahead.
                if op.is_splitted_ahead or (prev_op and prev_op.is_splitted_behind):
                    if para_ops:
                        ops_per_para.append(para_ops)
                    para_ops = []
                para_ops.append(op)
                prev_op = op

        # Check if there is a phrase to insert at the end of the paragraph.
        ins_item = pop_insert(ins_idx, prev_op, None)
        ins_ops = insert(ins_item, prev_op, None)
        
        for ins_op in ins_ops:
            if ins_op.is_splitted_ahead or (prev_op and prev_op.is_splitted_behind):
                if para_ops:
                    ops_per_para.append(para_ops)
                para_ops = []
            para_ops.append(ins_op)
            prev_op = ins_op

        # Append all pending diff items.
        if para_ops:
            ops_per_para.append(para_ops)

    # Append all phrases that remained in the insert index.
    for para_num in ins_idx:
        for key in ins_idx[para_num]:
            ins_ops = insert(ins_idx[para_num][key], None, None)
            ops_per_para.append(ins_ops)

    return ops_per_para

# ______________________________________________________________________________

def merge(ops_per_para):
    """ Iterates through the phrases and merge them where necessary . """ 

    merged_paragraphs = []
    merged_paragraph  = []
    prev_op = None

    for para_ops in ops_per_para:
        # Sort the phrases by their positions in target.
        para_ops.sort(key=lambda x: x.pos_target_start)

    ops_per_para.sort(key=lambda x: x[0].pos_target_start)

    for para_ops in ops_per_para:
        is_first_op_in_para = True
        for op in para_ops:
            # Ignore deletions from merging.
            if isinstance(op, DeleteWords) or isinstance(op, DeleteParagraph):
                merged_paragraph.append(op)
                continue

            # Continue if there is no previous op so far.
            if not prev_op:
                merged_paragraph.append(op)
                prev_op = op
                is_first_op_in_para = False
                continue

            # Obtain the paragraph number of previous op and current op.
            para_num = op.pos_target_start[0]
            prev_para_num = prev_op.pos_target_start[0]

            # Introduce new paragraph, if paragraph numbers differ.
            if para_num != prev_para_num:
                if merged_paragraph:
                    merged_paragraphs.append(merged_paragraph)
                merged_paragraph = []

            # On rearranging a phrase, it could happen that the phrase at 
            # target position must be split to include the phrase. Check, 
            # if this scenario apply here.
            if isinstance(op, RearrangeParagraph):
                if para_num == prev_para_num and \
                        prev_op.pos_target_end > op.pos_target_start:
                    # Remove last element.
                    merged_paragraph.pop()
                    # Split prev op and insert
                    left, right = prev_op.split(op.pos_target_start)
                    right.num_para_splits += 1
                    right.num_para_merges += 2
                    merged_paragraph.extend([left, op, right])
                    continue

            # Merge if paragraph numbers are equal and items are split.
            if para_num == prev_para_num and is_first_op_in_para:
                prev_op.is_merged_behind = True
                op.is_merged_ahead = True
                op.num_para_merges += 1
            
            merged_paragraph.append(op)

            prev_op = op
            is_first_op_in_para = False

    if merged_paragraph:
        merged_paragraphs.append(merged_paragraph)

    return merged_paragraphs

# ______________________________________________________________________________

def common(item, prev_phrase, next_item):
    """ Processes the given diff common item. """

    ops = []

    # Split the item by target paragraphs.
    sub_items = item.split_by_target_paras()
    
    prev_op = None
    for item in sub_items:
        op = Common(item)

        if prev_op:
            prev_op.is_splitted_behind = True
            op.is_splitted_ahead = True
            op.num_para_splits = 1

        ops.append(op)
        prev_op = op

    return ops

def rearrange(item, prev_phrase, next_item):
    """ Processes the given diff rearrange item. """

    ops = []

    # Split the item by target paragraphs.
    sub_items = item.split_by_target_paras()
    
    if item:
        is_first = prev_phrase is None
        is_last = next_item is None
        # Decide if we have to split the item from preceding phrases.
        need_front_split = not is_first and not prev_phrase.is_splitted_behind
        # Decide if we have to split the item from successive phrases.
        need_back_split  = not is_last

        if is_first and is_last:
            # The item is the only item of the paragraph. Only paragraph 
            # operations are allowed.
            prev_op = None
            for sub_item in sub_items:
                op = RearrangeParagraph(sub_item)
                ops.append(op)
                if prev_op:
                    prev_op.is_splitted_behind = True
                    op.is_splitted_ahead = True
                    op.num_para_splits = 1
                prev_op = op
        else:
            # Compute the costs for various variants and take the variant 
            # with lowest costs.

            # Variant 1: Paragraph operations. Cut off the item from context 
            # and rearrange the item.
            para_ops = {
                "num_para_splits":   need_front_split + need_back_split,
                "num_para_reorders": len(sub_items)
            }
            costs_para_ops = get_costs_para_ops(para_ops)

            # Variant 2: Word operations. Rearrange the words of the item. 
            # This variant is only allowed within paragraph boundaries.
            is_word_ops_allowed = False
            if prev_phrase:
                prev_phrase_para_num = prev_phrase.pos_target_start[0] 
                item_para_num = item.pos_target[0][0]
                is_word_ops_allowed = (prev_phrase_para_num == item_para_num)
            elif next_item and hasattr(next_item, "pos_target") and next_item.pos_target: 
                item_para_num = item.pos_target[0][0]
                next_para_num = next_item.pos_target[0][0]
                is_word_ops_allowed = (item_para_num == next_para_num)

            costs_word_ops = float('inf')
            if is_word_ops_allowed:
                word_ops = {
                    "num_word_replaces": len(item.words)
                }
                costs_word_ops = get_costs_word_ops(word_ops)

            if costs_para_ops < costs_word_ops:
                prev_op = None
                for sub_item in sub_items:
                    op = RearrangeParagraph(sub_item)
                    ops.append(op)
                    if prev_op:
                        prev_op.is_splitted_behind = True
                        op.is_splitted_ahead = True
                        op.num_para_splits += 1
                    prev_op = op
                if need_front_split:
                    prev_phrase.is_splitted_behind = True
                    ops[0].is_splitted_ahead = True
                    ops[0].num_para_splits += 1
                if need_back_split:
                    ops[-1].is_splitted_behind = True
                    ops[-1].num_para_splits += 1
            else:
                prev_op = None
                for sub_item in sub_items:
                    op = RearrangeWords(sub_item)
                    ops.append(op)
                    if prev_op:
                        prev_op.is_splitted_behind = True
                        op.is_splitted_ahead = True
                        op.num_para_splits = 1
                    prev_op = op

    return ops

def delete(item, prev_phrase, next_item):
    """ Processes the given diff delete item. """

    ops = []

    if item:
        is_first = prev_phrase is None
        is_last = next_item is None
        # Decide if we have to split the item from preceding phrases.
        need_front_split = not is_first and not prev_phrase.is_splitted_behind
        # Decide if we have to split the item from successive phrases.
        need_back_split  = not is_last

        if is_first and is_last:
            # The item is the only item of the paragraph. Only paragraph 
            # operations are allowed.
            ops.append(DeleteParagraph(item))
        else:
            # Compute the costs for various variants and take the variant 
            # with lowest costs.

            # Variant 1: Paragraph operations. Cut off the item from context 
            # and delete the item.
            para_ops = {
                "num_para_splits":  need_front_split + need_back_split,
                "num_para_deletes": 1
            }
            costs_para_ops = get_costs_para_ops(para_ops)

            # Variant 2: Word operations. Delete the words of the item.
            word_ops = {
                "num_word_deletes": len(item.words)
            }
            costs_word_ops = get_costs_word_ops(word_ops)

            if costs_para_ops < costs_word_ops:
                op = DeleteParagraph(item)
                ops.append(op)
                if need_front_split:
                    prev_phrase.is_splitted_behind = True
                    op.is_splitted_ahead = True
                    op.num_para_splits += 1
                if need_back_split:
                    op.is_splitted_behind = True
                    op.num_para_splits += 1
            else:
                ops.append(DeleteWords(item))

    return ops

def insert(item, prev_phrase, next_item):
    """ Processes the given diff insert item. """

    ops = []

    if item:
        is_first = prev_phrase is None
        is_last = next_item is None
        has_back_split = prev_phrase is not None and prev_phrase.is_splitted_behind
        # Decide if we have to split the item from successive phrases.
        need_split = not is_first and not is_last and not has_back_split

        if is_first and is_last:
            # The item is the only item of the paragraph. Only paragraph 
            # operations are allowed.
            ops.append(InsertParagraph(item))
        else:
            # Compute the costs for various variants and take the variant 
            # with lowest costs.

            # Variant 1: Paragraph operations. Insert whole item.
            para_ops = {
                "num_para_splits":  need_split,
                "num_para_inserts": 1
            }
            costs_para_ops = get_costs_para_ops(para_ops)

            # Variant 2: Word operations. Insert all words of the item.
            word_ops = {
                "num_word_inserts": len(item.words)
            }
            costs_word_ops = get_costs_word_ops(word_ops)

            if costs_para_ops < costs_word_ops:
                op = InsertParagraph(item)
                ops.append(op)
                if prev_phrase:
                    prev_phrase.is_splitted_behind = True
                op.is_splitted_ahead = True
                op.is_splitted_behind = True
                if need_split:
                    op.num_para_splits += 1
            else:
                ops.append(InsertWords(item))

    return ops

def get_costs_para_ops(para_ops):
    return COST_FACTOR_PARA_OPS * sum(para_ops[op] for op in para_ops)

def get_costs_word_ops(word_ops):
    return COST_FACTOR_WORD_OPS * sum(word_ops[op] for op in word_ops)

# ______________________________________________________________________________

def split_into_actual_paras(diff_result):
    """ Groups the items of the given diff result by actual paragraphs."""

    paragraphs = []
    paragraph_items = []

    prev_item = None
    for item in diff_result:
        if isinstance(item, diff_align.Insert):
            continue
        elif isinstance(item, diff_align.Common):
            item = CommonWrapper(diff_item=item)
        elif isinstance(item, diff_align.Delete):
            item = DeleteWrapper(diff_item=item)
        elif isinstance(item, diff_align.Rearrange):
            item = RearrangeWrapper(diff_item=item)

        prev_actual_para_num = prev_item.actual_para_num if prev_item else -1
        actual_para_num      = item.actual_para_num

        # Start new paragraph if the paragraph index of the current item 
        # is different to the paragraph index of the previous item.
        if prev_actual_para_num != actual_para_num:
            paragraph_items = []
            paragraphs.append(paragraph_items)

        # Add the item to the current paragraph. Split the item into subitems 
        # if it is spanned over multiple target paragraphs.
        sub_items = item.split_by_actual_paras()

        paragraph_items.append(sub_items[0])
        prev_item = sub_items[0]
        for i in range(1, len(sub_items)):
            paragraph_items = [sub_items[i]]
            paragraphs.append(paragraph_items)
            prev_item = sub_items[i]

    return paragraphs

def split_into_target_paras(diff_result):
    """ Groups the items of the given diff result by target paragraphs."""

    paragraphs = []
    paragraph_items = []

    prev_item = None
    for item in diff_result:
        if isinstance(item, diff_align.Delete):
            continue
        elif isinstance(item, diff_align.Common):
            item = CommonWrapper(diff_item=item)
        elif isinstance(item, diff_align.Insert):
            item = InsertWrapper(diff_item=item)
        elif isinstance(item, diff_align.Rearrange):
            item = RearrangeWrapper(diff_item=item)

        prev_target_para_num = prev_item.target_para_num if prev_item else -1
        target_para_num      = item.target_para_num

        # Start new paragraph if the paragraph index of the current item 
        # is different to the paragraph index of the previous item.
        if prev_target_para_num != target_para_num:
            paragraph_items = []
            paragraphs.append(paragraph_items)

        # Add the item to the current paragraph. Split the item into subitems 
        # if it is spanned over multiple target paragraphs.
        sub_items = item.split_by_target_paras()

        paragraph_items.append(sub_items[0])
        prev_item = sub_items[0]
        for i in range(1, len(sub_items)):
            paragraph_items = [sub_items[i]]
            paragraphs.append(paragraph_items)
            prev_item = sub_items[i]

    return paragraphs

def create_to_insert_index(diff_result):
    """ Creates an index of all diff insert items in the given diff result."""
    target_paragraphs = split_into_target_paras(diff_result)
    index = OrderedDict()
    for para in target_paragraphs:
        for item in para:
            if isinstance(item, InsertWrapper):
                para_num = item.pos_target[0][0]
                inner_num = item.pos_target[0][1]
                inner_index = index.setdefault(para_num, OrderedDict())
                inner_index[inner_num] = item
    return index

def pop_insert(to_insert_index, from_phrase, to_phrase):
    """ Pops the first insert item from given index that is positioned between 
    the target position of 'from_phrase' and 'to_phrase'."""
    para_num = -1
    if from_phrase:
        para_num = from_phrase.pos_target_start[0]
    elif to_phrase:
        para_num = to_phrase.pos_target_start[0]
    if to_insert_index:
        # Get all inserts in the paragraph.
        inner_index = to_insert_index.get(para_num, None)
        if inner_index:
            inner_list = [v for k, v in inner_index.items()]
            # Obtain only the first missing in specified range.
            inner_from = inner_list[0].pos_target[0][1]
            inner_to   = inner_list[-1].pos_target[0][1]
            if from_phrase:
                inner_from = from_phrase.pos_target_start[1]
            if to_phrase:
                inner_to = to_phrase.pos_target_start[1]

            for i in range(inner_from, inner_to + 1):
                if i in inner_index:
                    return inner_index.pop(i)

# ______________________________________________________________________________

class BaseWrapper:
    def __init__(self, words=[], pos_actual=[], pos_target=[], diff_item=None):
        self.words = words
        self.pos_actual = pos_actual
        self.pos_target = pos_target
        if diff_item:
            self.words = diff_item.words
            if hasattr(diff_item, 'positions_actual'):
                self.pos_actual = diff_item.positions_actual
            if hasattr(diff_item, 'positions_target'):
                self.pos_target = diff_item.positions_target
        self.target_para_num = self.pos_target[0][0] if self.pos_target else -1
        self.actual_para_num = self.pos_actual[0][0] if self.pos_actual else -1

    def sub(self, start_index, end_index):
        words = self.words[start_index : end_index]
        pos_actual, pos_target = [], []
        if self.pos_actual:
            pos_actual = self.pos_actual[start_index : end_index]
        if self.pos_target:
            pos_target = self.pos_target[start_index : end_index]

        return self.__class__(words, pos_actual, pos_target)

    def split_by_actual_paras(self):
        return self.split_by_paras([x[0] for x in self.pos_actual])

    def split_by_target_paras(self):
        return self.split_by_paras([x[0] for x in self.pos_target])

    def split_by_paras(self, para_nums):
        sub_items = []

        prev_split_index = 0
        prev_para_num = para_nums[0]
        for i in range(1, len(para_nums)):
            para_num = para_nums[i]

            if para_num != prev_para_num:
                sub_items.append(self.sub(prev_split_index, i))
                prev_split_index = i

            prev_para_num = para_num

        # Don't forget to append the remaining sub items.
        sub_items.append(self.sub(prev_split_index, len(para_nums)))

        return sub_items

    def __repr__(self):
        return self.__str__()

class CommonWrapper(BaseWrapper):
    """ Wraps a diff common item. """
    def __init__(self, words=[], pos_actual=[], pos_target=[], diff_item=None):
        super(CommonWrapper, self).__init__(
            words, pos_actual, pos_target, diff_item)

    def __str__(self):
        return "(%s, %s, %s)" % ("=", self.words, self.pos_target)

class DeleteWrapper(BaseWrapper):
    """ Wraps a diff delete item. """

    def __init__(self, words=[], pos_actual=[], pos_target=[], diff_item=None):
        super(DeleteWrapper, self).__init__(
            words, pos_actual, pos_target, diff_item)

    def __str__(self):
        return "(%s, %s, %s)" % ("-", self.words, self.pos_actual)

class RearrangeWrapper(BaseWrapper):
    """ Wraps a diff rearrange item. """

    def __init__(self, words=[], pos_actual=[], pos_target=[], diff_item=None):
        super(RearrangeWrapper, self).__init__(
            words, pos_actual, pos_target, diff_item)

    def __str__(self):
        return "(%s, %s, %s)" % ("<>", self.words, self.pos_target)

class InsertWrapper(BaseWrapper):
    """ Wraps a diff insert item. """

    def __init__(self, words=[], pos_actual=[], pos_target=[], diff_item=None):
        super(InsertWrapper, self).__init__(
            words, pos_actual, pos_target, diff_item)

    def __str__(self):
        return "(%s, %s, %s)" % ("+", self.words, self.pos_target)

# ______________________________________________________________________________

class Operation:
    """ An operation. """
    def __init__(self, item=None, words=None, pos_actual=None, pos_target=None):
        self.words = words
        self.pos_actual = pos_actual
        self.pos_target = pos_target
        self.pos_actual_start = pos_actual[0] if pos_actual else [sys.maxsize]
        self.pos_target_start = pos_target[0] if pos_target else [sys.maxsize]
        self.pos_actual_end = pos_actual[-1] if pos_actual else [sys.maxsize]
        self.pos_target_end = pos_target[-1] if pos_target else [sys.maxsize]

        if item:
            self.words = item.words
            self.pos_actual = item.pos_actual
            self.pos_target = item.pos_target
            self.pos_actual_start = item.pos_actual[0] if item.pos_actual else [sys.maxsize]
            self.pos_target_start = item.pos_target[0] if item.pos_target else [sys.maxsize]
            self.pos_actual_end = item.pos_actual[-1] if item.pos_actual else [sys.maxsize]
            self.pos_target_end = item.pos_target[-1] if item.pos_target else [sys.maxsize]

        self.is_splitted_ahead  = False
        self.is_splitted_behind = False
        self.num_para_splits    = 0
        self.is_merged_ahead    = False
        self.is_merged_behind   = False
        self.num_para_merges    = 0

    def split(self, pos_target_start):
        for i, pos in enumerate(self.pos_target):
            if pos < pos_target_start:
                continue

            left = self.__class__(None, self.words[:i], self.pos_actual[:i], self.pos_target[:i])
            right = self.__class__(None, self.words[i:], self.pos_actual[i:], self.pos_target[i:])

            return left, right

    def __str__(self):
        return "(%s, %s)" % (self.words, self.pos_target_start)

    def __repr__(self):
        return self.__str__()

class Common(Operation):
    def __init__(self, item=None, words=None, pos_actual=None, pos_target=None):
        super(Common, self).__init__(item, words, pos_actual, pos_target)

class InsertWords(Operation):
    def __init__(self, item=None, words=None, pos_actual=None, pos_target=None):
        super(InsertWords, self).__init__(item, words, pos_actual, pos_target)

class InsertParagraph(Operation):
    def __init__(self, item=None, words=None, pos_actual=None, pos_target=None):
        super(InsertParagraph, self).__init__(item, words, pos_actual, pos_target)

class DeleteWords(Operation):
    def __init__(self, item=None, words=None, pos_actual=None, pos_target=None):
        super(DeleteWords, self).__init__(item, words, pos_actual, pos_target)

class DeleteParagraph(Operation):
    def __init__(self, item=None, words=None, pos_actual=None, pos_target=None):
        super(DeleteParagraph, self).__init__(item, words, pos_actual, pos_target)

class RearrangeWords(Operation):
    def __init__(self, item=None, words=None, pos_actual=None, pos_target=None):
        super(RearrangeWords, self).__init__(item, words, pos_actual, pos_target)

class RearrangeParagraph(Operation):
    def __init__(self, item=None, words=None, pos_actual=None, pos_target=None):
        super(RearrangeParagraph, self).__init__(item, words, pos_actual, pos_target)

def visualize_ops(ops_per_para):
    def visualize_op(op, prev_op):
        words = " ".join(op.words)

        prev_is_splitted_behind = prev_op and prev_op.is_splitted_behind
        prev_is_merged_behind   = prev_op and prev_op.is_merged_behind

        if isinstance(op, InsertWords):
            # Highlight with green font color.
            words = "\033[32m%s\033[0m" % words
        if isinstance(op, InsertParagraph):
            # Highlight with green background.
            words = "\033[42m%s\033[0m" % words
        if isinstance(op, DeleteWords):
            # Highlight with red font color.
            words = "\033[31m%s\033[0m" % words
        if isinstance(op, DeleteParagraph):
            # Highlight with red font color.
            words = "\033[41m%s\033[0m" % words
        if isinstance(op, RearrangeWords):
            # Highlight with blue font color.
            words = "\033[34m%s\033[0m" % words
        if isinstance(op, RearrangeParagraph):
            # Highlight with blue font color.
            words = "\033[44m%s\033[0m" % words
        if op.is_splitted_ahead and not prev_is_splitted_behind:
            words = "\033[1;31m‖\033[0m %s" % words
        if op.is_splitted_behind:
            words = "%s \033[1;31m‖\033[0m" % words
        if op.is_merged_ahead and not prev_is_merged_behind:
            words = "\033[1;31m==\033[0m %s" % words
        if op.is_merged_behind:
            words = "%s \033[1;31m==\033[0m" % words
        return words

    visualized_paras = []
    prev_op = None
    for para in ops_per_para:
        visualized_para = []
        for op in para:
            visualized_para.append(visualize_op(op, prev_op))
            prev_op = op
        visualized_paras.append(" ".join(visualized_para))

    return "\n\n".join(visualized_paras)

def count_ops(ops_per_para):
    counter = Counter()

    for para in ops_per_para:
        for op in para:
            counter["num_para_splits"] += op.num_para_splits
            counter["num_para_merges"] += op.num_para_merges

            if isinstance(op, InsertWords):
                counter["num_word_inserts"] += len(op.words)
            if isinstance(op, InsertParagraph):
                counter["num_para_inserts"] += 1
            if isinstance(op, DeleteWords): 
                counter["num_word_deletes"] += len(op.words)
            if isinstance(op, DeleteParagraph):
                counter["num_para_deletes"] += 1
            if isinstance(op, RearrangeWords):
                counter["num_word_rearranges"] += len(op.words)
            if isinstance(op, RearrangeParagraph):
                counter["num_para_rearranges"] += 1
    counter += Counter() # remove zero and negative counts
    return counter

if __name__ == "__main__":
    a = """ABC [formula] DEF"""
    b = """ABC XXX YYY ZZZ  DEF"""

    junk = ["\[formula\]"]

    print(visualize_ops(align_strings(a, b, junk)))
    print(count_ops(align_strings(a, b, junk)))
