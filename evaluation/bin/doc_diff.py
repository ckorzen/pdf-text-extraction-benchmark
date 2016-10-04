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
    actual_paras = util.to_formatted_paragraphs(actual, excludes=junk)
    target_paras = util.to_formatted_paragraphs(target, excludes=junk)
               
    # 'actual_paras' and 'target_paras' are lists of lists of words, where each
    # inner list includes the (normalized) words of a paragraph.
    # example: 'actual_paras' = [['words', 'of', 'first', 'paragraph], [...]] 

    # Run para diff to get the basic paragraph operations to perform to 
    # transform 'actual' into 'target'.
    return para_diff.para_diff(actual_paras, target_paras, junk)

    # Filter junk from phrases
    #return [item for item in diff_result if not ignore_diff_item(item, junk)]

# ------------------------------------------------------------------------------

def count_num_ops(diff_phrases, junk=[]):
    """ Counts the num of operations to apply for given phrases. """
    
    counter = Counter()
    for phrase in diff_phrases: 
        if ignore_diff_item(phrase, junk):
            num_ops, vis_instructs = apply_ignored_phrase(phrase) 
            counter.update(num_ops)     
        elif isinstance(phrase, diff.DiffCommonPhrase):
            num_ops, vis_instructs = apply_common_phrase(phrase) 
            counter.update(num_ops)
        elif isinstance(phrase, diff.DiffReplacePhrase):
            num_ops, vis_instructs = apply_replace_phrase(phrase) 
            counter.update(num_ops)
        elif isinstance(phrase, rearr.DiffRearrangePhrase):
            num_ops, vis_instructs = apply_rearrange_phrase(phrase) 
            counter.update(num_ops)
        elif isinstance(phrase, para_diff.SplitParagraph):
            num_ops, vis_instructs = apply_split_para(phrase) 
            counter.update(num_ops)
        elif isinstance(phrase, para_diff.MergeParagraph):
            num_ops, vis_instructs = apply_merge_para(phrase) 
            counter.update(num_ops) 
            
    # Remove zero and negative counts.
    counter += Counter()                  
            
    return counter 
  
# ------------------------------------------------------------------------------

def visualize_diff_phrases(diff_phrases, junk=[]):
    """ Visualizes the given diff phrases. """
        
    vis_instructions = []
      
    # Collect the visualization instructions per phrase.
    for phrase in diff_phrases:      
        if ignore_diff_item(phrase, junk):
            num_ops, vis_instructs = apply_ignored_phrase(phrase) 
            vis_instructions.extend(vis_instructs)                           
        elif isinstance(phrase, diff.DiffCommonPhrase):
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
        
    # Sort the instructions by defined positions.
    vis_instructions.sort(key=lambda instr: instr[1])
    
    return apply_visualization_instructions(vis_instructions)
    
def apply_visualization_instructions(vis_instructions):
    """ Applies the given visualization instructions."""
    
    visualization_parts = []
        
    prev_instr = None
    for instr in vis_instructions:
        # Obtain the words to visualize and the visualization function.
        words, pos, vis_function = instr
        # Compose the text to show from given words.
        text = get_unnormalized_text(words)
                
        # Apply the visualization function, if any.
        vis = vis_function(text) if vis_function else text
                
        # TODO
        #vis = vis_function(str(pos) + " " + text) if vis_function else str(pos) + " " + text
       
        visualization_parts.append(vis)
                
        prev_instr = instr
      
    # Compose the whole visualization string.  
    return "".join(visualization_parts) + "\n"
    
# ------------------------------------------------------------------------------
  
def apply_split_para(split):
    """ Decides which kind of operations to perform on applying the given split 
    paragraph operation. Returns the number of operations and a list of 
    instructions to visualize the operation. Each instruction consists of a 
    list of words to display, the position where to place the words in 
    visualization and a visualization function to apply to mark the words. 
    """
    
    # Place the split at position defined by the pos. stack (split.wrapped[2])
        
    return Counter({ "num_para_splits": 1 }), [([split], split.pos, red)]
    
def apply_merge_para(merge):
    """ Decides which kind of operations to perform on applying the given merge 
    paragraph operation. Returns the number of operations and a list of 
    instructions to visualize the operation. Each instruction consists of a 
    list of words to display, the position where to place the words in 
    visualization and a visualization function to apply to mark the words. 
    """
        
    # Place the merge at position defined by the pos. stack (merge.wrapped[2])
    return Counter({ "num_para_merges": 1 }), [([merge], merge.pos, red)]
  
def apply_ignored_phrase(phrase):
    """ Decides which kind of operations to perform on applying the given 
    ignored phrase. Returns the number of operations and a list of instructions 
    to visualize the phrase. Each instruction consists of a list of words to 
    display, the position where to place the words in visualization and a 
    visualization function to apply to mark the words. 
    """
    # Place the phrase at position defined by pos. stack of first actual word.
        
    instr = []
    if phrase.num_words_actual() > 0:
        instr = [(phrase.words_actual, phrase.pos, gray)]
         
    return Counter(), instr
  
def apply_common_phrase(phrase):
    """ Decides which kind of operations to perform on applying the given 
    common phrase. Returns the number of operations and a list of instructions 
    to visualize the phrase. Each instruction consists of a list of words to 
    display, the position where to place the words in visualization and a 
    visualization function to apply to mark the words. 
    """        
    return Counter(), [(phrase.words_target, phrase.pos, None)]
        
def apply_rearrange_phrase(phrase):
    """ Decides which kind of operations to perform on applying the given 
    rearrange phrase. Returns the number of operations and a list of 
    instructions to visualize the phrase. Each instruction consists of a list 
    of words to display, the position where to place the words in 
    visualization and a visualization function to apply to mark the words. 
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
        return Counter(word_ops), [
            (phrase.words_actual, phrase.pos, red), 
            (phrase.words_target, phrase.pos + [0], green)]
    else:
        return Counter(para_ops), [
            (phrase.words_actual, phrase.pos, blue_bg)]
        
def apply_replace_phrase(phrase):
    """ Decides which kind of operations to perform on applying the given 
    replace phrase. Returns the number of operations and a list of instructions 
    to visualize the phrase. Each instruction consists of a list of words to 
    display, the position where to place the words in visualization and a 
    visualization function to apply to mark the words. 
    """
    
    if phrase is None:
        # Nothing to do.
        return Counter(), None, []
    elif phrase.is_empty():
        # Nothing to do.
        return Counter(), None, []
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
    instruction consists of a list of words to display, the position where to 
    place the words in visualization and a visualization function to apply to 
    mark the words. 
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
        return Counter(word_ops), [(phrase.words_target, phrase.pos, green)]
    else:
        return Counter(para_ops), [(phrase.words_target, phrase.pos, green_bg)]
       
def apply_delete_phrase(phrase):
    """ Decides which kind of operations to perform on applying the given 
    replace phrase that represents indeed a deletion. Returns the number of 
    operations and a list of instructions to visualize the phrase. Each 
    instruction consists of a list of words to display, the position where to 
    place the words in visualization and a visualization function to apply to 
    mark the words. 
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
        return Counter(word_ops), [(phrase.words_actual, phrase.pos, red)]
    else:
        return Counter(para_ops), [(phrase.words_actual, phrase.pos, red_bg)]
    
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
                                     
    return Counter(word_ops), [
        (phrase.words_actual, phrase.pos, red), 
        (phrase.words_target, phrase.pos + [0], green)]
       
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
    
def is_para_break(prev_instr, instr):
    """ Checks if there is a paragraph break between the two given 
    visualiation instructions. Returns True if there is a paragraph break, 
    False otherwise.
    """
    
    # Abort if one of the lists is None
    if prev_instr is None or instr is None:
        return False
        
    # Obtain the paragraph numbers of both words.
    prev_para_num = prev_instr[1][0]
    para_num      = instr[1][0]
    
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

def gray(text):
    return colorize(text, "\033[90m%s\033[0m")

def red_bg(text):
    return colorize(text, "\033[41m%s\033[0m")

def green_bg(text):
    return colorize(text, "\033[42m%s\033[0m")

def blue_bg(text):
    return colorize(text, "\033[44m%s\033[0m")

def gray_bg(text):
    return colorize(text, "\033[100m%s\033[0m")

def colorize(text, color_pattern):
    """ Applies the given color_pattern to the *trunk* of the given text, that
    is the stripped version of the text. All leading and trailing whitespaces
    won't be colorized."""
    
    if len(text) == 0:
        return ""
                
    # less has issues on displaying color codes if wrapped text contains 
    # newlines ("\n"): Only the first line is colored. As a workaround, wrap
    # each line with color_pattern.
    lines = text.split("\n")
    colored_lines = []
    for line in lines:
        colored_lines.append(color_pattern % line if len(line) > 0 else "")
        
    return "\n".join(colored_lines)

# ------------------------------------------------------------------------------

if __name__ == "__main__":
    a = """
arXiv:cond-mat/0001220v1 [cond-mat.stat-mech] 17 Jan 2000

Reducing quasi-ergodicity in a double well potential
by Tsallis Monte Carlo simulation
Masao Iwamatsu†∗and Yutaka Okabe†
∗
Department of Computer Engineering, Hiroshima City University
Hiroshima 731-3194, Japan
and
†
Department of Physics, Tokyo Metropolitan University
Hachioji, Tokyo 192-0397, Japan

Abstract
A new Monte Carlo scheme based on the system of Tsallis’s generalized statistical mechanics is applied to a simple double well potential
to calculate the canonical thermal average of potential energy. Although we observed serious quasi-ergodicity when using the standard
Metropolis Monte Carlo algorithm, this problem is largely reduced by
the use of the new Monte Carlo algorithm. Therefore the ergodicity is
guaranteed even for short Monte Carlo steps if we use this new canonical Monte Carlo scheme.
PACS: 02.70.Lq; 05.70.-a
Key words: Monte Carlo; Tsallis statistics; double well potential

∗

Permanent address. E-mail:iwamatsu@ce.hiroshima-cu.ac.jp

1

^L1

Introduction

The ergodic hypothesis
"""

    b = """    
Reducing quasi-ergodicity in a double well potential by Tsallis Monte Carlo simulation

and Yutaka Okabe[formula] *Department of Computer Engineering, Hiroshima City University Hiroshima 731-3194, Japan and [formula]Department of Physics, Tokyo Metropolitan University Hachioji, Tokyo 192-0397, Japan

Introduction

XXX YYY ZZZ AAA BBB CCC DDD

The ergodic hypothesis
"""

result = doc_diff(a, b, junk=["\[formula\]"])

print(visualize_diff_phrases(result, junk=["\[formula\]"]))

