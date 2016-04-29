import sys
import diff_align
from util import to_formatted_paragraphs
from collections import Counter
from collections import OrderedDict

# The multiplication factor on computing the costs of paragraph operations. 
para_ops_cost_factor = 2
# The multiplication factor on computing the costs of word operations.
word_ops_cost_factor = 1

def align_strings(actual, target, junk=[]):
    """ Aligns the given string 'actual' against the given string 'target'.
    Both strings are seen as sequences of paragraphs. This method counts the 
    number of operations needed to transform 'actual' into 'target'. Operations
    under consideration are:

    * Split paragraph.
    * Merge paragraph.
    * Delete paragraph.
    * Reorder paragraph.
    * Insert word.
    * Delete word.
    * Replace word. 
    """

    # Split both string into paragraphs.
    actual_paras = to_formatted_paragraphs(actual, to_protect=junk)
    target_paras = to_formatted_paragraphs(target, to_protect=junk)

    # Align the paragraphs.
    alignment, alignment_ops = align_paragraphs(actual_paras, target_paras, junk)

    return alignment, alignment_ops

def align_paragraphs(actual_paras, target_paras, junk=[]):
    """ Aligns the given lists of paragraphs. """

    # Run diff to identify the phrases to insert, to delete and to rearrange.
    diff = diff_align.diff(actual_paras, target_paras, junk)

    # Diff doesn't consider paragraph boundaries. So we need to group the diff 
    # items by actual paragraphs.
    diff_actual = split_into_actual_paras(diff)

    # 'diff_actual' doesn't contain the phrases to insert (the phrases of 
    # 'target' that don't occur in 'actual'). Create index of such phrases. 
    ins_idx = create_to_insert_index(diff)

    # Process the diff result: insert, delete, split and rearrange phrases.
    phrases, ops = insert_delete_split_rearrange(diff_actual, ins_idx)

    # Merge phrases.
    merged_phrases, merge_ops = merge_phrases(phrases)

    return merged_phrases, ops + merge_ops

# ______________________________________________________________________________

def insert_delete_split_rearrange(diff_actual, ins_idx):
    """ Iterates through the diff result and applies inserts, deletes, splits 
    and rearranges to 'actual'. """ 

    # The aligned phrases of 'actual'.
    all_phrases = []
    # The executed operations.
    all_ops = Counter()

    for para in diff_actual:
        para_phrases = []
        para_ops = Counter()
        prev_phrase = None
        for i, diff_item in enumerate(para):
            # Obtain the last *aligned* item in paragraph.
            # prev_align_item = all_align_items[-1] if all_align_items else None
            # Obtain the next (still unaligned) item in paragraph.
            next_item = para[i + 1] if i < len(para) - 1 else None

            # Apply the diff items.
            phrases, ops = [], {}
            if isinstance(diff_item, CommonWrapper):
                phrases, ops = common(diff_item, prev_phrase, next_item)
            elif isinstance(diff_item, RearrangeWrapper):
                phrases, ops = rearrange(diff_item, prev_phrase, next_item)
            elif isinstance(diff_item, DeleteWrapper):
                phrases, ops = delete(diff_item, prev_phrase, next_item)

            # Register the executed operations.
            para_ops.update(ops)

            if isinstance(diff_item, DeleteWrapper):
                para_phrases.extend(phrases)
            else:
                # Append the phrases to result list.
                for phrase in phrases: 
                    # Check, if there is a phrase to insert between the previous 
                    # phrase and the current phrase.
                    ins_item = pop_insert(ins_idx, prev_phrase, phrase)
                    ins_phrases, ins_ops = insert(ins_item, prev_phrase, phrase)
                    
                    para_phrases.extend(ins_phrases)
                    para_phrases.append(phrase)
                    para_ops.update(ins_ops)

                    prev_phrase = phrase

        # Check if there is a phrase to insert at the end of the paragraph.
        ins_item = pop_insert(ins_idx, prev_phrase, None)
        ins_phrases, ins_ops = insert(ins_item, prev_phrase, None)
        para_phrases.extend(ins_phrases)
        para_ops.update(ins_ops)

        all_ops.update(para_ops)

        # Identify the last phrase in the paragraph and brand it to clarify 
        # that all successive phrases belong to another paragraph (needed on 
        # merge step).
        if para_phrases:
            # Sort the phrases by their positions in target.
            para_phrases.sort(key=lambda x: x.pos_target)
            # Brand the last phrase of paragraph.
            para_phrases[-1].has_back_split = True
            # Register the phrases of the paragraph.
            all_phrases.extend(para_phrases)

    # Append all phrases that remained in the insert index.
    for para_num in ins_idx:
        for key in ins_idx[para_num]:
            ins_phrases, ins_ops = insert(ins_idx[para_num][key], None, None)
            all_phrases.extend(ins_phrases)
            all_ops.update(ins_ops)

    return all_phrases, all_ops

# ______________________________________________________________________________

def merge_phrases(phrases):
    """ Iterates through the phrases and merge them where necessary . """ 

    merged_phrases = []
    merge_ops = Counter()

    # Sort the phrases by their positions in target.
    phrases.sort(key=lambda x: x.pos_target)

    if phrases:
        prev_phrase = phrases[0]
        merged_phrases.append(prev_phrase)

        # Identify belonging phrases.
        for i in range(1, len(phrases)):
            phrase = phrases[i]

            if isinstance(phrase.item, DeleteWrapper):
                merged_phrases.append(phrase)
                continue

            # Obtain the paragraph number of previous phrase and current phrase.
            para_num = phrase.pos_target[0]
            prev_para_num = prev_phrase.pos_target[0]
            
            # On rearranging a phrase, it could happen that the phrase at target
            # position must be split to include the phrase. Check, if this 
            # scenario apply here.
            if isinstance(phrase.item, RearrangeWrapper):
                # Applies only on rearranging a whole phrase.
                if phrase.ops[0].startswith("para"):
                    # Applies only when a next phrase exists.
                    next_phrase = phrases[i+1] if i < len(phrases) - 1 else None
                    next_para_num = next_phrase.pos_target[0] if next_phrase else -1

                    # Check, if the previous and the next phrase belong to the 
                    # same paragraph.
                    if prev_para_num == next_para_num:
                        # Check, if the phrases weren't split already.
                        if not prev_phrase.has_back_split:
                            prev_phrase.has_back_split = True
                            prev_phrase.ops.append("para_split")
                            prev_phrase.ops.append("para_merge")
                            merge_ops.update({ "num_para_splits": 1,
                                               "num_para_merges": 1 })

            # Merge the phrases when they have the same paragraph number and
            # are separated by a split.<f
            if para_num == prev_para_num and prev_phrase.has_back_split:
                #prev_phrase.extend(phrase)
                merged_phrases.append(phrase)
                prev_phrase.ops.append("para_merge")
                merge_ops.update({ "num_para_merges": 1 })
            else:
                merged_phrases.append(phrase)
                prev_phrase = phrase

    return merged_phrases, merge_ops

# ______________________________________________________________________________

def common(item, prev_phrase, next_item):
    """ Processes the given diff common item. """

    phrases = []
    ops = Counter()

    # Split the item by target paragraphs.
    sub_items, sub_ops = item.split_by_target_paras()
    
    if len(sub_items) == 1:
        ops.update(sub_ops)
        phrases.append(Phrase(sub_items[0], ["common"]))
    else:
        ops.update(sub_ops)
        prev_phrase = Phrase(sub_items[0], ["para_split"])
        phrases.append(prev_phrase)
        for i in range(1, len(sub_items)):
            phrase = Phrase(sub_items[i], ["para_split"])
        
            phrases.append(phrase)

            # The previous align item has now a back split.
            prev_phrase.has_back_split = True
            prev_phrase = phrase

    return phrases, ops

def rearrange(item, prev_phrase, next_item):
    """ Processes the given diff rearrange item. """

    phrases = []
    ops = Counter()

    # Split the item by target paragraphs.
    sub_items, sub_ops = item.split_by_target_paras()
    ops.update(sub_ops)

    if item:
        is_first = prev_phrase is None
        is_last = next_item is None
        # Decide if we have to split the item from preceding phrases.
        need_front_split = not is_first and not prev_phrase.has_back_split
        # Decide if we have to split the item from successive phrases.
        need_back_split  = not is_last

        if is_first and is_last:
            # The item is the only item of the paragraph. Only paragraph 
            # operations are allowed.
            for sub_item in sub_items:
                phrases.append(Phrase(sub_item, ["para_reorder"], has_back_split=True))
                ops.update({ "num_para_reorders": 1 }) 
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
                prev_phrase_para_num = prev_phrase.pos_target[0] 
                item_para_num = item.pos_target[0][0]
                is_word_ops_allowed = (prev_phrase_para_num == item_para_num)
            elif next_item and hasattr(next_item, "pos_target"): 
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
                ops_done = []
                if len(sub_items) > 1 or para_ops["num_para_splits"] > 0:
                    ops_done.append("para_split")
                ops_done.append("para_reorder")
                for sub_item in sub_items:
                    phrases.append(Phrase(sub_item, ops_done, has_back_split=True))
                ops.update(para_ops)
            else:
                ops_done = ["para_split"] if len(sub_items) > 1 else []
                ops_done.append("word_replace")
                for sub_item in sub_items:
                    phrases.append(Phrase(sub_item, ops_done))
                ops.update(word_ops)

    return phrases, ops

def delete(item, prev_phrase, next_item):
    """ Processes the given diff delete item. """

    phrases = []
    ops = {}

    if item:
        is_first = prev_phrase is None
        is_last = next_item is None
        # Decide if we have to split the item from preceding phrases.
        need_front_split = not is_first and not prev_phrase.has_back_split
        # Decide if we have to split the item from successive phrases.
        need_back_split  = not is_last

        if is_first and is_last:
            # The item is the only item of the paragraph. Only paragraph 
            # operations are allowed.
            phrases.append(Phrase(item, ["para-delete"]))
            ops = { "num_para_deletes": 1 } 
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
                ops = para_ops
                # The previous phrase has now a back split.
                if prev_phrase:
                    prev_phrase.has_back_split = True
                phrases.append(Phrase(item, ["para_delete"]))
            else:
                ops = word_ops
                phrases.append(Phrase(item, ["word_delete"]))

    return phrases, ops

def insert(item, prev_phrase, next_item):
    """ Processes the given diff insert item. """

    phrases = []
    ops = {}

    if item:
        is_first = prev_phrase is None
        is_last = next_item is None
        has_back_split = prev_phrase is not None and prev_phrase.has_back_split
        # Decide if we have to split the item from successive phrases.
        need_split = not is_first and not is_last and not has_back_split

        if is_first and is_last:
            # The item is the only item of the paragraph. Only paragraph 
            # operations are allowed.
            phrases.append(Phrase(item, ["para_insert"], has_back_split=True))
            ops = { "num_para_inserts": 1 } 
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
                # The previous phrase has now a back split.
                if prev_phrase:
                    prev_phrase.has_back_split = True
                    if para_ops["num_para_splits"] > 0:
                        prev_phrase.ops.append("para_split")
                phrases.append(Phrase(item, ["para_insert"], has_back_split=True))
                ops = para_ops
            else:
                phrases.append(Phrase(item, ["word_insert"]))
                ops = word_ops

    return phrases, ops

def get_costs_para_ops(para_ops):
    return para_ops_cost_factor * sum(para_ops[op] for op in para_ops)

def get_costs_word_ops(word_ops):
    return word_ops_cost_factor * sum(word_ops[op] for op in word_ops)

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
        sub_items, ops = item.split_by_actual_paras()

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
        sub_items, ops = item.split_by_target_paras()

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
        para_num = from_phrase.pos_target[0]
    elif to_phrase:
        para_num = to_phrase.pos_target[0]
    if to_insert_index:
        # Get all inserts in the paragraph.
        inner_index = to_insert_index.get(para_num, None)
        if inner_index:
            inner_list = [v for k, v in inner_index.items()]
            # Obtain only the first missing in specified range.
            inner_from = inner_list[0].pos_target[0][1]
            inner_to   = inner_list[-1].pos_target[0][1]
            if from_phrase:
                inner_from = from_phrase.pos_target[1]
            if to_phrase:
                inner_to = to_phrase.pos_target[1]

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
        ops = Counter()

        prev_split_index = 0
        prev_para_num = para_nums[0]
        for i in range(1, len(para_nums)):
            para_num = para_nums[i]

            if para_num != prev_para_num:
                sub_items.append(self.sub(prev_split_index, i))
                ops.update({ 'num_para_splits': 1 })
                prev_split_index = i

            prev_para_num = para_num

        # Don't forget to append the remaining sub items.
        sub_items.append(self.sub(prev_split_index, len(para_nums)))

        return sub_items, ops

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

class Phrase:
    """ A phrase. """
    def __init__(self, item, ops, has_back_split=False):
        self.item = item
        self.words = item.words
        self.ops = ops
        self.has_back_split = has_back_split
        self.pos_actual = [sys.maxsize]
        if hasattr(item, "pos_actual") and item.pos_actual:
            self.pos_actual = item.pos_actual[0]
        self.pos_target = [sys.maxsize]
        if hasattr(item, "pos_target") and item.pos_target:
            self.pos_target = item.pos_target[0]

    def extend(self, other):
        self.words.extend(other.words)
        self.has_back_split = other.has_back_split

    def __str__(self):
        return "(%s, %s, %s)" % (self.pos_target, self.words, self.ops)

    def __repr__(self):
        return self.__str__()

def visualize(alignment):
    def visualize_common(text):
        return text

    def visualize_para_insert(text):
        return "\033[42m%s\033[0m" % text # green background color

    def visualize_para_delete(text):
        return "\033[41m%s\033[0m" % text # red background color

    def visualize_para_reorder(text):
        return "\033[44m%s\033[0m" % text # blue font color

    def visualize_para_split(text):
        return "\033[31m| %s" % text

    def visualize_para_merge(text):
        print(text)
        return "\033[4m%s" % text # Underlined

    def visualize_word_insert(text):
        return "\033[32m%s\033[0m" % text # green font color

    def visualize_word_delete(text):
        return "\033[31m%s\033[0m" % text # red font color

    def visualize_word_replace(text):
        return "\033[34m%s\033[0m" % text # blue font color

    parts = []
    for phrase in alignment:
        print(phrase)
        words = " ".join(phrase.words)
        if "common" in phrase.ops:
            words = visualize_common(words)
        if "para_insert" in phrase.ops:
            words = visualize_para_insert(words)
        if "para_delete" in phrase.ops:
            words = visualize_para_delete(words)
        if "para_split" in phrase.ops:
            words = visualize_para_split(words)
        if "para_merge" in phrase.ops:
            words = visualize_para_merge(words)
        if "para_reorder" in phrase.ops:
            words = visualize_para_reorder(words)
        if "word_insert" in phrase.ops:
            words = visualize_word_insert(words)
        if "word_delete" in phrase.ops:
            words = visualize_word_delete(words)
        if "word_replace" in phrase.ops:
            words = visualize_word_replace(words)
        parts.append(words)

    return " ".join(parts)

if __name__ == "__main__":
    a = "Hello xxx xxx xxx xxx xxx xxx xxx xxx xxx yyy yyy yyy yyy yyy yyy yyy yyy yyy you."
    b = "Hello yyy yyy yyy yyy yyy yyy yyy yyy yyy xxx xxx xxx xxx xxx xxx xxx xxx xxx you."

    alignment, ops = align_strings(a, b)

    print(visualize(alignment))

