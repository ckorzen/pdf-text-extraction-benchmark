import para_diff
import util
from collections import Counter

# The multiplication factor on computing the costs for paragraph operations. 
COST_FACTOR_PARA_OPS = 2
# The multiplication factor on computing the costs of word operations.
COST_FACTOR_WORD_OPS = 1

def doc_diff(actual, target, junk=[]):
    """ Aligns the given string 'actual' against the given string 'target'.
    Both strings are seen as sequences of paragraphs. Returns a sequence of 
    operations that are necessary to transform 'actual' into 'target'. The 
    operations under consideration are:

    * Split paragraph.
    * Merge paragraph.
    * Delete paragraph.
    * Rearrange paragraph.
    * Insert word.
    * Delete word.
    * Rearrange word. 
    """

    # 'actual' and 'target' are strings and may be arranged in paragraphs 
    # which are denoted by two newlines.

    # Extract the paragraphs from 'actual' and 'target' and format them (remove
    # special characters and transform all letters to lowercase letters). 
    actual_paras = util.to_formatted_paragraphs(actual, to_protect=junk)
    target_paras = util.to_formatted_paragraphs(target, to_protect=junk)
    
    # 'actual_paras' and 'target_paras' are lists of lists of words, where each
    # inner list includes the (normalized) words of a paragraph.
    # example: 'actual_paras' = [['words', 'of', 'first', 'paragraph], [...]] 

    # Run para diff to get the basic paragraph operations to perform to 
    # transform 'actual' into 'target'.
    diff_result = para_diff.para_diff(actual_paras, target_paras, junk)

    x = merge(apply_insert_delete_replace_rearrange(diff_result, junk))
    
    return x;

# ______________________________________________________________________________

def apply_insert_delete_replace_rearrange(diff_result, junk=[]):
    """ Iterates through 'actual_phrases' and decides which operations to apply 
    to transform 'actual' into 'target'. 'actual_phrases' is a list of list of 
    phrases per paragraph, 'insert_phrases' is a dictionary of dictionary of 
    phrases to insert. The outer dictionary maps to the paragraph number, the 
    inner dictionary maps to the position within the paragraph.""" 
    
    result = []
    diff_result = [x for x in diff_result if not util.ignore(x, junk)]
    
    for i in range(0, len(diff_result)):
        prev_item = diff_result[i - 1] if i > 0 else None
        item = diff_result[i]
        next_item = diff_result[i + 1] if i < len(diff_result) - 1 else None

        # Handle commons, rearranges, deletes individually.
        if isinstance(item, para_diff.Commons):
            result.extend(commons(prev_item, item, next_item))
        elif isinstance(item, para_diff.Rearranges):
            result.extend(rearranges(prev_item, item, next_item))
        elif isinstance(item, para_diff.Replaces):
            result.extend(replaces(prev_item, item, next_item))

    return result

def merge(diff_result):
    """ Iterates through the operations and merge them where necessary . """ 

    # Sort the diff_result by position in target.
    diff_result = sorted(diff_result, key=lambda x: x.pos_target)

    for i in range(0, len(diff_result)):
        prev_item = diff_result[i - 1] if i > 0 else None
        item = diff_result[i]
        next_item = diff_result[i + 1] if i < len(diff_result) - 1 else None

        if type(item) in [InsertParagraph, RearrangeParagraph, ReplaceParagraph]:
            if prev_item and not has_diff_para_target(prev_item, item) and not prev_item.has_merge_behind():
                item.merge_ahead()
            if next_item and not has_diff_para_target(item, next_item) and not next_item.has_merge_ahead():
                item.merge_behind()
        elif type(prev_item) in [DeleteParagraph]:
            prev_prev_item = diff_result[i - 2] if i > 1 else None
            if prev_prev_item and not has_diff_para_target(prev_prev_item, item) and not prev_prev_item.has_merge_behind():
                item.merge_ahead()

    return diff_result

# ______________________________________________________________________________

def commons(prev_item, commons, next_item):
    """ Processes the given diff common item. """
    return [Common(phrase) for phrase in commons.phrases]

def rearranges(prev_item, rearranges, next_item):
    """ Processes the given diff rearrange item. """
    
    ops = []

    for i in range(0, len(rearranges.phrases)):
        prev_phrase = None
        if i > 0:
            prev_phrase = rearranges.phrases[i - 1]
        elif prev_item and prev_item.phrases:
            prev_phrase = prev_item.phrases[-1]

        next_phrase = None
        if i < len(rearranges.phrases) - 1:
            next_phrase = rearranges.phrases[i + 1]
        elif next_item and next_item.phrases:
            next_phrase = next_item.phrases[0]

        phrase = rearranges.phrases[i]

        items_actual = phrase.items_actual
        items_target = phrase.items_target

        is_first = has_diff_para_actual(prev_phrase, phrase)
        is_last = has_diff_para_actual(phrase, next_phrase)

        if is_first and is_last:
            ops.append(RearrangeParagraph(phrase))
        else:
            need_split_ahead = (not is_first and not phrase.has_split_ahead()) or rearranges.needs_split_ahead_on_para_rearrange
            need_split_behind = not is_last and not phrase.has_split_behind() or rearranges.needs_split_behind_on_para_rearrange

            # Variant 1: Paragraph operations. Cut off the phrase from context 
            # and rearrange the phrase.
            para_ops = {
                "num_para_splits": need_split_ahead + need_split_behind,
                "num_para_rearr": 1
            }
            costs_para_ops = get_costs_para_ops(para_ops)

            # Variant 2: Word operations.
            #word_ops = {
            #    "num_word_rearr": len(phrase.items_actual)
            #}
            #costs_word_ops = get_costs_word_ops(word_ops)

            #if costs_para_ops < costs_word_ops:
            ops.append(RearrangeParagraph(phrase, need_split_ahead, need_split_behind))
            #else:
            #    ops.append(RearrangeWords(phrase))
    return ops

def replaces(prev_item, replaces, next_item):
    """ Processes the given diff replace item. """

    ops = []

    for i in range(0, len(replaces.phrases)):
        prev_phrase = None
        if i > 0:
            prev_phrase = replaces.phrases[i - 1]
        elif prev_item and prev_item.phrases:
            prev_phrase = prev_item.phrases[-1]

        next_phrase = None
        if i < len(replaces.phrases) - 1:
            next_phrase = replaces.phrases[i + 1]
        elif next_item and next_item.phrases:
            next_phrase = next_item.phrases[0]

        phrase = replaces.phrases[i]

        if isinstance(phrase, para_diff.Replace):
            ops.append(replace(prev_phrase, phrase, next_phrase))
        elif isinstance(phrase, para_diff.Insert):
            ops.append(insert(prev_phrase, phrase, next_phrase))
        elif isinstance(phrase, para_diff.Delete):
            ops.append(delete(prev_phrase, phrase, next_phrase))
    return ops

def replace(prev_phrase, phrase, next_phrase):
    items_actual = phrase.items_actual
    items_target = phrase.items_target

    para_ops = { "num_para_replaces": 1 }
    costs_para_ops = get_costs_para_ops(para_ops)

    word_ops = { "num_word_replaces": min(len(items_actual), len(items_target)) }
    costs_word_ops = get_costs_word_ops(word_ops)

    if costs_para_ops < costs_word_ops:
        return ReplaceParagraph(phrase)
    else:
        return ReplaceWords(phrase)

def insert(prev_phrase, phrase, next_phrase):
    """ Processes the given diff insert item. """
    items_target = phrase.items_target

    is_first = has_diff_para_target(prev_phrase, phrase)
    is_last = has_diff_para_target(phrase, next_phrase)

    if is_first and is_last:
        return InsertParagraph(phrase)

    # Variant 1: Paragraph operations.
    para_ops = { "num_para_inserts": 1 }
    costs_para_ops = get_costs_para_ops(para_ops)

    # Variant 2: Word operations.
    word_ops = { "num_word_inserts": len(items_target) }
    costs_word_ops = get_costs_word_ops(word_ops)

    if costs_para_ops < costs_word_ops:
        return InsertParagraph(phrase)
    else:
        return InsertWords(phrase)

def delete(prev_phrase, phrase, next_phrase):
    """ Processes the given diff delete item. """
    items_actual = phrase.items_actual

    is_first = has_diff_para_actual(prev_phrase, phrase)
    is_last = has_diff_para_actual(phrase, next_phrase)

    if is_first and is_last:
        return DeleteParagraph(phrase)
    else:
        need_split_ahead = not is_first and not phrase.has_split_ahead()
        need_split_behind = not is_last and not phrase.has_split_behind()

        # Variant 1: Paragraph operations. Cut off the item from context 
        # and rearrange the item.
        para_ops = {
            "num_para_splits": need_split_ahead + need_split_behind,
            "num_para_deletes": 1
        }
        costs_para_ops = get_costs_para_ops(para_ops)

        # Variant 2: Word operations.
        word_ops = {
            "num_word_deletes": len(items_actual)
        }
        costs_word_ops = get_costs_word_ops(word_ops)

        if costs_para_ops < costs_word_ops:
            return DeleteParagraph(phrase, need_split_ahead, need_split_behind)
        else:
            return DeleteWords(phrase)

def get_costs_para_ops(para_ops):
    return COST_FACTOR_PARA_OPS * sum(para_ops[op] for op in para_ops)

def get_costs_word_ops(word_ops):
    return COST_FACTOR_WORD_OPS * sum(word_ops[op] for op in word_ops)

def has_diff_para_actual(prev_item, item):
    if prev_item and item and prev_item.items_actual and item.items_actual:
        return item.items_actual[0].para != prev_item.items_actual[-1].para
    return True

def has_diff_para_target(prev_item, item):
    if prev_item and item and prev_item.items_target and item.items_target:
        return item.items_target[0].para != prev_item.items_target[-1].para
    return True

# ==============================================================================

def count_diff_items(diff_result):
    counter = Counter()

    for item in diff_result:
        for prefix_item in item.prefix:
            if isinstance(prefix_item, para_diff.SplitParagraph):
                counter["num_para_splits"] += 1
            if isinstance(prefix_item, para_diff.MergeParagraph):
                counter["num_para_merges"] += 1
        for suffix_item in item.suffix:
            if isinstance(suffix_item, para_diff.SplitParagraph):
                counter["num_para_splits"] += 1
            if isinstance(suffix_item, para_diff.MergeParagraph):
                counter["num_para_merges"] += 1

        if isinstance(item, InsertWords):
            # items_target contains items for each word that results from 
            # replacing all non-chars by whitespace. For example, for the string
            # "5.1.1" items_target would contain three words. In fact, this 
            # string should count only as a single word. Hence, join the 
            # the *original* item strings (with non-chars symbols) and split
            # them only on whitespaces
            string = "".join([x.item[1] for x in item.items_target])            
            counter["num_word_inserts"] += len(string.split())
        if isinstance(item, InsertParagraph):
            counter["num_para_inserts"] += 1
        if isinstance(item, DeleteWords): 
            string = "".join([x.item[1] for x in item.items_actual])
            counter["num_word_deletes"] += len(string.split())
        if isinstance(item, DeleteParagraph):
            counter["num_para_deletes"] += 1
        if isinstance(item, ReplaceWords): 
            string = "".join([x.item[1] for x in item.items_actual])
            counter["num_word_replaces"] += len(string.split())
        if isinstance(item, ReplaceParagraph):
            counter["num_para_replaces"] += 1
        if isinstance(item, RearrangeWords):
            string = "".join([x.item[1] for x in item.items_actual])
            counter["num_word_rearr"] += len(string.split())
        if isinstance(item, RearrangeParagraph):
            counter["num_para_rearr"] += 1
        
    counter += Counter() # remove zero and negative counts
    return counter

def visualize_diff_result(diff_result):
    def red_font(text):
        return "\033[31m%s\033[0m" % text

    def green_font(text):
        return "\033[32m%s\033[0m" % text

    def blue_font(text):
        return "\033[34m%s\033[0m" % text

    def red_background(text):
        return "\033[41m%s\033[0m" % text

    def green_background(text):
        return "\033[42m%s\033[0m" % text

    def blue_background(text):
        return "\033[44m%s\033[0m" % text

    paras = []
    para = []
    prev_item = None
    for item in diff_result:
        if prev_item and has_diff_para_target(prev_item, item):
            if para:
                paras.append("".join(para))
            para = []

        if any([isinstance(x, para_diff.SplitParagraph) for x in item.prefix]):
            para.append(red_font("‖ "))
        if any([isinstance(x, para_diff.MergeParagraph) for x in item.prefix]):
            para.append(red_font("== "))

        #words_actual = " ".join([x.item.string for x in item.items_actual])
        #words_target = " ".join([x.item.string for x in item.items_target])
        words_actual = "".join([x.item[1] for x in item.items_actual])
        words_target = "".join([x.item[1] for x in item.items_target])

        if isinstance(item, Common):
            para.append(words_target)
        if isinstance(item, InsertWords):
            para.append(green_font(words_target))
        if isinstance(item, InsertParagraph):
            para.append(green_background(words_target))
        if isinstance(item, DeleteWords): 
            para.append(red_font(words_actual))
        if isinstance(item, DeleteParagraph):
            para.append(red_background(words_actual))
        if isinstance(item, ReplaceWords): 
            para.append("%s%s" % (red_font(words_actual), green_font(words_target)))
        if isinstance(item, ReplaceParagraph):
            para.append("%s%s" % (red_background(words_actual), green_background(words_target)))
        if isinstance(item, RearrangeWords):
            para.append(blue_font(words_target))
        if isinstance(item, RearrangeParagraph):
            para.append(blue_background(words_target))

        if any([isinstance(x, para_diff.SplitParagraph) for x in item.suffix]):
            para.append(red_font("‖ "))
        if any([isinstance(x, para_diff.MergeParagraph) for x in item.suffix]):
            para.append(red_font("== "))

        prev_item = item

    if para:
        paras.append("".join(para))

    return "\n\n".join(paras) + "\n"

def visualize_diff_result_debug(diff_result):
    paras = []
    para = []
    prev_item = None
    for item in diff_result:
        if prev_item and has_diff_para_target(prev_item, item):
            if para:
                paras.append(" ".join(para))
            para = []

        para.extend([str(x) for x in reversed(item.prefix)])

        #words_actual = " ".join([x.item.string for x in item.items_actual])
        #words_target = " ".join([x.item.string for x in item.items_target])
        words_actual = " ".join([x.string for x in item.items_actual])
        words_target = " ".join([x.string for x in item.items_target])

        if isinstance(item, Common):
            para.append(words_target)
        if isinstance(item, InsertWords):
            para.append("[+ %s]" % words_target)
        if isinstance(item, InsertParagraph):
            para.append("[++ %s]" % words_target)
        if isinstance(item, DeleteWords): 
            para.append("[- %s]" % words_actual)
        if isinstance(item, DeleteParagraph):
            para.append("[-- %s]" % words_actual)
        if isinstance(item, ReplaceWords): 
            para.append("[/ %s, %s]" % (words_actual, words_target))
        if isinstance(item, ReplaceParagraph):
            para.append("[// %s, %s]" % (words_actual, words_target))
        if isinstance(item, RearrangeWords):
            para.append("[<> %s, %s]" % (words_actual, words_target))
        if isinstance(item, RearrangeParagraph):
            para.append("[<><> %s, %s]" % (words_actual, words_target))

        para.extend([str(x) for x in item.suffix])

        prev_item = item

    if para:
        paras.append(" ".join(para))

    return "\n\n".join(paras)

# ==============================================================================

class Diff:
    def __init__(self, sign, item, split_ahead=False, split_behind=False):
        self.sign = sign
        self.items_actual = item.items_actual
        self.items_target = item.items_target
        self.pos_actual = self.items_actual[0].pos if self.items_actual else []
        self.pos_target = self.items_target[0].pos if self.items_target else []
        self.prefix = item.prefix
        self.suffix = item.suffix
        if split_ahead and not self.has_split_ahead():
            self.split_ahead()
        if split_behind and not self.has_split_behind():
            self.split_behind()

    def split_ahead(self):
        self.prefix.append(para_diff.SplitParagraph())

    def split_behind(self):
        self.suffix.append(para_diff.SplitParagraph())

    def merge_ahead(self):
        self.prefix.append(para_diff.MergeParagraph())

    def merge_behind(self):
        self.suffix.append(para_diff.MergeParagraph())

    def has_split_ahead(self):
        return any(isinstance(x, para_diff.SplitParagraph) for x in self.prefix)

    def has_split_behind(self):
        return any(isinstance(x, para_diff.SplitParagraph) for x in self.suffix)

    def has_merge_ahead(self):
        return any(isinstance(x, para_diff.MergeParagraph) for x in self.prefix)

    def has_merge_behind(self):
        return any(isinstance(x, para_diff.MergeParagraph) for x in self.suffix)

    def __str__(self):
        text = "(%s %s, %s)" % (self.sign, self.items_actual, self.items_target)

        parts = []
        parts.extend([str(x) for x in self.prefix])
        parts.append(text)
        parts.extend([str(x) for x in self.suffix])
        return ", ".join(parts)

    def __repr__(self):
        return self.__str__()

class Common(Diff):
    def __init__(self, item, split_ahead=False, split_behind=False):
        super(Common, self).__init__("=", item, split_ahead, split_behind)

class InsertWords(Diff):
    def __init__(self, item, split_ahead=False, split_behind=False):
        super(InsertWords, self).__init__("+", item, split_ahead, split_behind)

class InsertParagraph(Diff):
    def __init__(self, item, split_ahead=False, split_behind=False):
        super(InsertParagraph, self).__init__("++", item, split_ahead, split_behind)

class DeleteWords(Diff):
    def __init__(self, item, split_ahead=False, split_behind=False):
        super(DeleteWords, self).__init__("-", item, split_ahead, split_behind)

class DeleteParagraph(Diff):
    def __init__(self, item, split_ahead=False, split_behind=False):
        super(DeleteParagraph, self).__init__("--", item, split_ahead, split_behind)

class ReplaceWords(Diff):
    def __init__(self, item, split_ahead=False, split_behind=False):
        super(ReplaceWords, self).__init__("/", item, split_ahead, split_behind)

class ReplaceParagraph(Diff):
    def __init__(self, item, split_ahead=False, split_behind=False):
        super(ReplaceParagraph, self).__init__("//", item, split_ahead, split_behind)

class RearrangeWords(Diff):
    def __init__(self, item, split_ahead=False, split_behind=False):
        super(RearrangeWords, self).__init__("<>", item, split_ahead, split_behind)

class RearrangeParagraph(Diff):
    def __init__(self, item, split_ahead=False, split_behind=False):
        super(RearrangeParagraph, self).__init__("<><>", item, split_ahead, split_behind)


if __name__ == "__main__":
    #diff_result = doc_diff("The red fox jumps over the blue sea", "The red fox jumps over the blue sea")
    #diff_result = doc_diff("The red fox jumps", "The red fox jumps over the blue sea")
    #diff_result = doc_diff("The red fox jumps over the blue sea", "The red fox jumps")
    #diff_result = doc_diff("The red fox jumps over the blue sea", "The blue fox jumps over the red sea")
    #diff_result = doc_diff("The blue fox jumps over the red sea", "The red fox jumps over the blue sea")
    diff_result = doc_diff("The blue lazy fox jumps over the red sea", "The red fox jumps over the blue sea")
    #diff_result = doc_diff("The blue fox jumps over the red sea", "The red lazy fox jumps over the blue sea")
    #diff_result = doc_diff("The blue lazy fox jumps over the large red sea", "The red fox jumps over the blue sea")
    #diff_result = doc_diff("The red fox jumps over the blue sea", "The blue lazy fox jumps over the large red sea")
    
    #diff_result = doc_diff("The red fox jumps over the blue sea", "The red fox [formula] the blue sea", ["\[formula\]"])
    #diff_result = doc_diff("The red fox jumps over the blue sea", "The red fox [formula] xxx the blue sea", ["\[formula\]"])    
    
    vis = visualize_diff_result(diff_result)

    #print(diff_result)
    print(vis)
