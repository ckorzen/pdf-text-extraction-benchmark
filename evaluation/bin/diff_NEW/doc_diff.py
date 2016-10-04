import diff
import para_diff
import para_diff_rearrange as rearr
import util
from collections import Counter

# The multiplication factor on computing the costs for paragraph operations. 
COST_FACTOR_PARA_OPS = 5
# The multiplication factor on computing the costs of word operations.
COST_FACTOR_WORD_OPS = 1

# ------------------------------------------------------------------------------

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

    # Filter junk from phrases
    return [item for item in diff_result if not ignore_diff_item(item, junk)]

# ------------------------------------------------------------------------------

def count_num_ops(diff_phrases):
    """ Counts the num of operations to apply for given phrases. """
    
    counter = Counter()
    for phrase in diff_phrases:        
        if isinstance(phrase, diff.DiffCommonPhrase):
            num_ops, visualization = apply_common_phrase(phrase) 
            counter.update(num_ops)
        elif isinstance(phrase, diff.DiffReplacePhrase):
            num_ops, visualization = apply_replace_phrase(phrase) 
            counter.update(num_ops)
        elif isinstance(phrase, rearr.DiffRearrangePhrase):
            num_ops, visualization = apply_rearrange_phrase(phrase) 
            counter.update(num_ops)
        elif isinstance(phrase, para_diff.SplitParagraph):
            num_ops, visualization = apply_split_para(phrase) 
            counter.update(num_ops)
        elif isinstance(phrase, para_diff.MergeParagraph):
            num_ops, visualization = apply_merge_para(phrase) 
            counter.update(num_ops) 
            
    # Remove zero and negative counts.
    counter += Counter()                  
            
    return counter 
  
# ------------------------------------------------------------------------------

def visualize_diff_phrases(diff_phrases):
    """ Visualizes the given diff phrases."""
        
    vis_instructions = []
      
    # Collect the visualization instructions per phrase.
    for phrase in diff_phrases:                            
        if isinstance(phrase, diff.DiffCommonPhrase):
            num_ops, vis_instructs = apply_common_phrase(phrase) 
            vis_instructions.extend(vis_instructs)
        elif isinstance(phrase, diff.DiffReplacePhrase):
            num_ops, vis_instructs = apply_replace_phrase(phrase) 
            vis_instructions.extend(vis_instructs)
        elif isinstance(phrase, rearr.DiffRearrangePhrase):
            num_ops, vis_instructs = apply_rearrange_phrase(phrase)
            vis_instructions.extend(vis_instructs)
        elif isinstance(phrase, para_diff.SplitParagraph):
            num_ops, vis_instructs = apply_split_para(phrase)
            vis_instructions.extend(vis_instructs)
        elif isinstance(phrase, para_diff.MergeParagraph):
            num_ops, vis_instructs = apply_merge_para(phrase)
            vis_instructions.extend(vis_instructs)
        
    # Sort the instructions by the pos. stacks of words.
    # instr is a instruction ([words], vis_function)
    # instr[0] is the list of DiffWords: [(('foo', 'Foo '), 0, [0, 0]), ...]
    # instr[0][0].wrapped is the first word item: (('foo', 'Foo '), 0, [0, 0])
    # instr[0][0].wrapped[2] is the pos. stack of word: [0, 0]
    vis_instructions.sort(key=lambda instr: instr[0][0].wrapped[2])
    
    return apply_visualization_instructions(vis_instructions)
    
def apply_visualization_instructions(vis_instructions):
    """ Applies the given visualization instructions."""
    
    paragraphs = []
    current_paragraph = []
    prev_words = None
    for instr in vis_instructions:
        # Obtain the words to visualize and the visualization function.
        words, vis_function = instr
        # Compose the text to show from given words.
        text = get_unnormalized_text(words)
        # Apply the visualization function, if any.
        vis = vis_function(text) if vis_function else text
        
        # Check if there is a paragraph break between previous phrase and 
        # current phrase.
        if is_para_break(prev_words, words):
            if len(current_paragraph) > 0:
                # Finalize the current paragraph and introduce a new one.
                paragraphs.append("".join(current_paragraph))
                current_paragraph = []
        
        # Append the visualization to current paragraph.
        current_paragraph.append(vis)
        
        prev_words = words

    # Finalize the rest of visualization.
    if len(current_paragraph) > 0:
        paragraphs.append("".join(current_paragraph))
      
    # Compose the whole visualization string.  
    return "\n\n".join(paragraphs) + "\n"
    
# ------------------------------------------------------------------------------
  
def apply_split_para(split):
    """ Decides which kind of operations to perform on applying the given split 
    paragraph operation. Returns the number of operations and a list of 
    instructions to visualize the operation. Each instruction consists of a 
    list of words to display and a visualization function to apply to mark the 
    words. 
    """
    return Counter({ "num_para_splits": 1 }), [([split], red)]
    
def apply_merge_para(merge):
    """ Decides which kind of operations to perform on applying the given merge 
    paragraph operation. Returns the number of operations and a list of 
    instructions to visualize the operation. Each instruction consists of a 
    list of words to display and a visualization function to apply to mark the 
    words. 
    """
    return Counter({ "num_para_merges": 1 }), [([merge], red)]
  
def apply_common_phrase(phrase):
    """ Decides which kind of operations to perform on applying the given 
    common phrase. Returns the number of operations and a list of instructions 
    to visualize the phrase. Each instruction consists of a list of words to 
    display and a visualization function to apply to mark the words. 
    """
    return Counter(), [(phrase.words_actual, None)]
        
def apply_rearrange_phrase(phrase):
    """ Decides which kind of operations to perform on applying the given 
    rearrange phrase. Returns the number of operations and a list of 
    instructions to visualize the phrase. Each instruction consists of a list 
    of words to display and a visualization function to apply to mark the words. 
    """
    
    # Compute the number of operations if we would choose paragraph operations.
    para_ops = { 
        "num_para_rearranges": 1 
    }
    costs_para_ops = get_costs_para_ops(para_ops)

    # Compute the number of operations if we would choose word operations.
    word_ops = { 
        "num_word_deletes": phrase.num_words_actual(),
        "num_word_inserts": phrase.num_words_target() 
    }
    costs_word_ops = get_costs_word_ops(word_ops)
    
    if costs_word_ops < costs_para_ops:
        return Counter(word_ops), [(phrase.words_actual, red), 
            (phrase.words_target, green)]
    else:
        return Counter(para_ops), [(phrase.words_actual, blue_bg)]
        
def apply_replace_phrase(phrase):
    """ Decides which kind of operations to perform on applying the given 
    replace phrase. Returns the number of operations and a list of instructions 
    to visualize the phrase. Each instruction consists of a list of words to 
    display and a visualization function to apply to mark the words. 
    """
    
    if phrase is None:
        # Nothing to do.
        return Counter(), []
    elif phrase.is_empty():
        # Nothing to do.
        return Counter(), []
    elif phrase.num_words_actual() == 0:
        # The replace phrase represents an insertion.
        return apply_insert_phrase(phrase)
    elif phrase.num_words_target() == 0:
        # The replace phrase represents a deletion.
        return apply_delete_phrase(phrase)
    else:
        # The replace phrase represents indeed a 'real' substitution.
        return apply_substitute_phrase(phrase)

def apply_insert_phrase(phrase):
    """ Decides which kind of operations to perform on applying the given 
    replace phrase that represents indeed an insertion. Returns the number of 
    operations and a list of instructions to visualize the phrase. Each 
    instruction consists of a list of words to display and a visualization 
    function to apply to mark the words. 
    """
    
    # Compute the number of operations if we would choose paragraph operations.
    para_ops = { 
        "num_para_inserts": 1 
    }
    costs_para_ops = get_costs_para_ops(para_ops)

    # Compute the number of operations if we would choose word operations.
    word_ops = { 
        "num_word_inserts": phrase.num_words_target()
    }
    costs_word_ops = get_costs_word_ops(word_ops)
       
    if costs_word_ops < costs_para_ops:    
        return Counter(word_ops), [(phrase.words_target, green)]
    else:
        return Counter(para_ops), [(phrase.words_target, green_bg)]
       
def apply_delete_phrase(phrase):
    """ Decides which kind of operations to perform on applying the given 
    replace phrase that represents indeed a deletion. Returns the number of 
    operations and a list of instructions to visualize the phrase. Each 
    instruction consists of a list of words to display and a visualization 
    function to apply to mark the words. 
    """
    
    # Compute the number of operations if we would choose paragraph operations.
    para_ops = { 
        "num_para_deletes": 1 
    }
    costs_para_ops = get_costs_para_ops(para_ops)

    # Compute the number of operations if we would choose word operations.
    word_ops = { 
        "num_word_deletes": phrase.num_words_actual()
    }
    costs_word_ops = get_costs_word_ops(word_ops)
        
    if costs_word_ops < costs_para_ops:
        return Counter(word_ops), [(phrase.words_actual, red)]
    else:
        return Counter(para_ops), [(phrase.words_actual, red_bg)]
    
def apply_substitute_phrase(phrase):
    """ Decides which kind of operations to perform on applying the given 
    replace phrase that represents indeed an substitution. Returns the number 
    of operations and a list of instructions to visualize the phrase. Each 
    instruction consists of a list of words to display and a visualization 
    function to apply to mark the words. 
    """
    
    # TODO: Should we consider paragraph operations here?
    num_words_actual = phrase.num_words_actual()
    num_words_target = phrase.num_words_target()
    min_num_words = min(num_words_actual, num_words_target)
    
    word_ops = {
        "num_word_replaces": min_num_words,
        "num_word_inserts":  num_words_target - min_num_words,
        "num_word_deletes":  num_words_actual - min_num_words
    }
    costs_word_ops = get_costs_word_ops(word_ops)
        
    return Counter(word_ops), [(phrase.words_actual, red), 
        (phrase.words_target, green)]
       
# ------------------------------------------------------------------------------
# Some util methods.
       
def get_costs_para_ops(para_ops):
    """ Returns the total costs for the given operations. """ 
    return COST_FACTOR_PARA_OPS * sum(para_ops[op] for op in para_ops)

def get_costs_word_ops(word_ops):
    """ Returns the total costs for the given operations. """
    return COST_FACTOR_WORD_OPS * sum(word_ops[op] for op in word_ops)

def get_unnormalized_text(words):
    """ Returns the (unnormalized) text composed from the given words."""
    return "".join([x.wrapped[0][1] for x in words])
 
def ignore_diff_item(item, junk):
    """ Checks if we have to ignore the given diff item, which may be a 
    DiffPhrase, a SplitParagraph or a MergeParagraph. """
    
    # Ignore the item if it is a phrase and it contains junk.
    return isinstance(item, diff.DiffPhrase) and util.ignore_phrase(item, junk)
    
def is_para_break(prev_words, words):
    """ Checks if there is a paragraph break between the two given 
    list of words. Returns True if there is a paragraph break, False otherwise.
    """
    
    # Abort if one of the lists is None
    if prev_words is None or words is None:
        return False
        
    # Abort if one of the lists is empty.
    if len(prev_words) == 0 or len(words) == 0:
        return False
    
    # Obtain the last word of previous phrase and first word of current phrase.
    prev_word = prev_words[-1]
    word = words[0]
    
    # Obtain the paragraph numbers of both words.
    prev_para_num = para_diff.get_para_num(prev_word)
    para_num      = para_diff.get_para_num(word)
    
    if prev_para_num is not None and para_num is not None:
        return prev_para_num != para_num
        
# ------------------------------------------------------------------------------
# Methods to colorize.

def red(text):
    return colorize(text, "\033[31m%s\033[0m")

def green(text):
    return colorize(text, "\033[32m%s\033[0m")

def blue(text):
    return colorize(text, "\033[34m%s\033[0m")

def red_bg(text):
    return colorize(text, "\033[41m%s\033[0m")

def green_bg(text):
    return colorize(text, "\033[42m%s\033[0m")

def blue_bg(text):
    return colorize(text, "\033[44m%s\033[0m")

def colorize(text, color_pattern):
    """ Applies the given color_pattern to the *trunk* of the given text, that
    is the stripped version of the text. All leading and trailing whitespaces
    won't be colorized."""
    
    if len(text) == 0:
        return ""
        
    num_chars = len(text)
    
    # Obtain the number of leading whitespaces.
    lstripped = text.lstrip()
    num_leading_whitespaces = num_chars - len(lstripped)
    
    # Obtain the number of trailing whitespaces.
    stripped = lstripped.rstrip()
    num_trailing_whitespaces = len(lstripped) - len(stripped)   
    
    # Create whitespace prefix and suffix of correct length.
    whitespace_prefix = " " * num_leading_whitespaces
    whitespace_suffix = " " * num_trailing_whitespaces
    
    # Colorize the trunk of text.
    colored = color_pattern % stripped
    
    return "%s%s%s" % (whitespace_prefix, colored, whitespace_suffix) 

# ------------------------------------------------------------------------------

if __name__ == "__main__":
    a = """
    foo foo foo foo foo foo foo
    
    bar bar bar bar bar bar bar
    """
    b = """
    bar bar bar bar bar bar bar
    
    foo foo foo foo foo foo foo
    """
    
    diff_phrases = doc_diff(a, b)    
    
    num_ops = count_num_ops(diff_phrases)
    visualization = visualize_diff_phrases(diff_phrases)
    print(visualization)
