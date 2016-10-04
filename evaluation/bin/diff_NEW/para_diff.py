import diff
import util
import para_diff_rearrange as rearr
from collections import Iterable

# TODO: Differ between Insert and Delete (instead of only Replace)?

""" Informell: Führt wdiff aus; ordnet wenn nötig Paragraphen um; entscheidet,
wo gesplittet und wo gemergt werden muss und teilt die Phrasen des wdiff 
Ergebnisses pro Paragraph ein. """

def para_diff(actual, target, junk=[]):
    """ Diff for paragraphs. Returns a list of phrases / operations to do to
    translate the paragraphs in 'actual' to the paragraphs in 'target'. 
    Allowed phrases / operations are: (1) Split paragraph, (2) Merge paragraph, 
    (3) Rearrange phrase, (4) Insert phrase, (5) Delete phrase and (6) common
    phrases (phrases which are common to actual and target). 
    Let us introduce a simple example to use throughout the documentation:
    
           actual                      target
    The quick fox jumps         The quick fox jumps
    over the lazy dog.          over the lazy dog. 
                                Pack my box with an
    Pack my box with an         thirty dozen liquor  
    twenty dozen liquor         jugs.
    jugs.
    
    The actual text consists of 2 paragraphs, target consists of only 1 
    paragraph. Further thex differ in the words twenty <-> thirty.
    
    Actually, 'actual' and 'target' have to be (arbitrarily nested) lists of 
    words. In our case, both lists have to be once nested lists of words of 
    paragraphs:
    actual = [['The', 'quick', 'fox' ...], ['Pack', 'my', ...]]
    target = [['The', 'quick', 'fox' ...]]
    """
    
    # Flatten both lists of words to be able to do a word-based diff.
    # 'actual_flatten' and 'target_flatten' are now flat lists of tuples. Each
    # tuple (<word>, <flat_pos>, <pos_stack>) consists of:
    #   <word>      : The word
    #   <flat_pos>  : The pos of word in flat representation of original list.
    #   <pos_stack> : The position stack as list. The i-th element denotes the 
    #                 position of the word in the original list at level i.
    # actual_flatten = [('The', 0, [0, 0]), ..., ('Pack', 9, [1, 0]), ...]
    # target_flatten = [('The', 0, [0, 0]), ..., ('Pack', 9, [0, 9]), ...] 
    actual_flatten = flatten(actual)
    target_flatten = flatten(target)
 
    # Do a word-based diff on 'actual_flatten' and 'target_flatten'. 
    # The result is a list of diff.DiffCommonPhrase and diff.DiffReplacePhrase 
    # objects denoting the operations to perform to transform actual_flatten 
    # into target_flatten.
    # 
    # [= [('The', 0, [0, 0]), ('quick', 1, [0, 1]), ('fox', 2, [0, 2])],
    #    [('The', 0, [0, 0]), ('quick', 1, [0, 1]), ('fox', 2, [0, 2])]]
    # denotes a DiffCommonPhrase consisting of the related elements in 
    # actual_flatten and target_flatten. It implies that "The quick brown fox" 
    # occurs in both texts.
    #
    # [/ [('twenty', 13, [1, 5])], [('thirty', 13, [0, 13])]]
    # denotes a DiffReplacePhrase consisting of the related elements in 
    # actual_flatten and target_flatten. It implies that we have to replace 
    # "twenty" in actual by "thirty" in target.
    # One of the element lists in a DiffReplacePhrase may be empty, denoting 
    # either an insertion or a deletion.
    # NOTE: The diff result does *not* respect any paragraph boundaries. Diff
    # considers both list as an sequence of words. For example, a 
    # diff.DiffCommonPhrase may extend one or more paragraph boundaries.
    diff_phrases = diff.diff(actual_flatten, target_flatten)
        
    # Assume, that there is a phrase that occur in both list, but its order in 
    # actual doesn't correspond to the order in target, for example:
    #
    #   The quick fox jumps        Pack my box with an 
    #   over the lazy dog.         twenty dozen liquor 
    #                              jugs.
    #   Pack my box with an
    #   twenty dozen liquor        The quick fox jumps
    #   jugs.                      over the lazy dog.
    #
    # The phrase "The quick fox ..." is rearranged in both texts. Diff won't 
    # find such a rearrangement. Instead it would state to delete the phrase
    # from actual and to insert it in target.
    # Try to find such phrases and to rearrange them.
    rearranged_phrases = rearr.rearrange(diff_phrases, junk)
    
    # Because diff doesn't know any paragraph boundaries (see above), we have
    # to divide the phrases to get phrases per paragraph.
    return divide_phrases_per_para(rearranged_phrases)
        
# ------------------------------------------------------------------------------
# Methods to divide diff phrases per paragraph.
    
def divide_phrases_per_para(diff_phrases):
    """ Divides the given diff phrases to get phrases per paragraph. """
    result = []
        
    # Keep track of the previous actual word and of the previous target word.
    prev_word_actual, prev_word_target = None, None
    
    # Keep track of the previous actual word that has a counterpart in target
    # and of the previous target that has a counterpart in actual.
    prev_mated_actual, prev_mated_target = None, None
    
    # Iterate through the phrases and divide them.
    for phrase in diff_phrases:
        divided_phrases, \
            prev_word_actual, prev_word_target, \
            prev_mated_actual, prev_mated_target = \
        divide_phrase_per_para(phrase, \
            prev_word_actual, prev_word_target, \
            prev_mated_actual, prev_mated_target)
        
        result.extend(divided_phrases)

    return result
         
def divide_phrase_per_para(phrase, prev_word_actual, prev_word_target, 
        prev_mated_actual, prev_mated_target):
    """ Divides the given single diff phrase into phrases per paragraph. 
    Returns the list of (sub-)phrases, the last processed actual word and the
    last processed previous word. 
    
    Parameters
    ----------
    phrase : diff.DiffPhrase
        The phrase to divide.
    prev_word_actual : diff.DiffWord
        The previous actual word (with or without a counterpart in target).
        Is needed to decide if there is a paragraph break in actual text.
    prev_word_target : diff.DiffWord
        The previous target word (with or without a counterpart in actual).
        Is needed to decide if there is a paragraph break in target text.
    prev_mated_actual : diff.DiffWord
        The previous actual word that has a counterpart in target.
        Is needed to decide whether to insert a para_diff.SplitParagraph or 
        para_diff.MergeParagraph.
    prev_mated_target : diff.DiffWord
        The previous target word that has a counterpart in actual.
        Is needed to decide whether to insert a para_diff.SplitParagraph or 
        para_diff.MergeParagraph.
    """

    # The list of divided phrases.
    result = []
    # The number of actual words in the given phrase.
    num_words_actual = phrase.num_words_actual()
    # The number of target words in the given phrase.
    num_words_target = phrase.num_words_target()
    # The maximum of both numbers.
    max_num_words = max(num_words_actual, num_words_target)

    # ----------------------------------------------------------------------
    
    # The last position where the given phrase was divided.
    last_divide_pos = 0
    
    def divide_phrase(phrase, pos):
        """ Divides the given phrase at the given position. Returns a new
        phrase of same type as the given phrase containing the words in
        bounds of last_divide_pos and pos. """
            
        nonlocal last_divide_pos
        klass = type(phrase)

        # Thanks to python, we don't have to care about index bounds here.
        new_words_actual = phrase.words_actual[last_divide_pos : pos]
        new_words_target = phrase.words_target[last_divide_pos : pos]
            
        # Create new phrase, based on the type of given phrase.
        new_phrase = klass(new_words_actual, new_words_target)
            
        last_divide_pos = pos
          
        return new_phrase
    
    # ----------------------------------------------------------------------
    
    # Iterate through the words of phrases until *all* words were seen.
    for i in range(0, max_num_words):
        # Obtain the actual word (target word) to inspect. Because 'i' could be 
        # larger than the length of list of actual words (target words), the 
        # actual word (target word) could be None.  
        word_actual = phrase.words_actual[i] if i < num_words_actual else None
        word_target = phrase.words_target[i] if i < num_words_target else None
                                                                           
        # Decide if there is a paragraph break in actual words or target words.
        is_para_break_actual = is_para_break(prev_word_actual, word_actual)
        is_para_break_target = is_para_break(prev_word_target, word_target)
        
        # Decide if we have to divide the phrase.
        if is_para_break_actual or is_para_break_target:
            # Divide the phrase
            divided_phrase = divide_phrase(phrase, i)  
            if not divided_phrase.is_empty():
                result.append(divided_phrase)
        
        # Update the previous words.
        if word_actual is not None: 
            prev_word_actual = word_actual
        if word_target is not None:
            prev_word_target = word_target
        
        # Decide if we have to insert a SplitParagraph or a MergeParagraph.
        # For that, we inspect pairs of words with related counterparts (where
        # word_actual != None and word_target != None). 
        # Consider the following example: 
        #
        #   actual:         target:
        #   foo             foo
        # 
        #   bar baz         baz
        # 
        # We inspect the pairs (foo, foo) and (baz, baz), because they have 
        # related counterparts (they occur in actual and target).
        # If there is a paragraph break between the pairs in actual, but not in 
        # target, we have to add a MergeParagraph. If there is no paragraph
        # break between the pairs in actual, but in target, we have to add a 
        # SplitParagraph().
        # NOTE: Comparing with prev_word_actual / prev_word_target is not an 
        # option here, because there is a paragraph break between 'baz' and 
        # 'foo' in target, but no paragraph break between 'baz' and 'bar' in 
        # actual. This would lead to a wrong insertion of a SplitParagraph 
        # operation.
        
        # Check, if actual words have counterparts.
        if word_actual is not None and word_target is not None and \
            prev_mated_actual is not None and prev_mated_target is not None: 
            
            is_para_break_actual = is_para_break(prev_mated_actual, word_actual)
            is_para_break_target = is_para_break(prev_mated_target, word_target)
                
            if not is_para_break_actual and is_para_break_target:
                result.append(SplitParagraph(prev_mated_actual))
                    
            if is_para_break_actual and not is_para_break_target:
                result.append(MergeParagraph(prev_mated_actual))
               
        # Update the previous words that have a counterpart.    
        if word_actual is not None and word_target is not None:
            prev_mated_actual = word_actual 
            prev_mated_target = word_target 
                           
    if last_divide_pos == 0:
        # We didn't have divided the phrase. So we can add the 
        # entire phrase to result list (without creating a new object).
        result.append(phrase)
    else:
        # Create new phrase from the rest of the phrase.
        divided_phrase = divide_phrase(phrase, None)
            
        if not divided_phrase.is_empty():
            result.append(divided_phrase)
        
    return result, prev_word_actual, prev_word_target, \
                   prev_mated_actual, prev_mated_target
     
# ------------------------------------------------------------------------------
# Some util methods.
  
def flatten(hierarchy):
    """ Flattens the given hierarchy of elements to a flat list. Keeps track of 
    the position in the hierarchy of each string. 
    The list consists of tuples of form (<word>, <flat_pos>, <pos_stack>) where:
        <word>      : The word
        <flat_pos>  : The pos of word in flat representation of original list.
        <pos_stack> : The position stack as list. The i-th element denotes the 
                      position of the word in the original list at level i.
    
    >>> flatten([1, 2, 3])
    [(1, 0, [0]), (2, 1, [1]), (3, 2, [2])]
    >>> flatten([[1], [2], [3]])
    [(1, 0, [0, 0]), (2, 1, [1, 0]), (3, 2, [2, 0])]
    >>> flatten([[1], [2, 3]])
    [(1, 0, [0, 0]), (2, 1, [1, 0]), (3, 2, [1, 1])]
    """
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

def is_para_break(prev_word, word):
    """ Checks if there is a paragraph break between the given words.
    Returns True if both words are not None and there is a paragraph break in 
    between.
    Returns False if both words are not None and there is no paragraph break in 
    between.
    Returns None if at least one of both words is None.
    """
    
    if prev_word is None or word is None:
        return
    
    # Obtain the paragraph numbers of both words.
    prev_para_num = get_para_num(prev_word)
    para_num      = get_para_num(word)
    
    if prev_para_num is not None and para_num is not None:
        return prev_para_num != para_num
        
def get_para_num(word):
    """ Returns the number of paragraph, in which the word is located. """
    
    if word is not None:
        # Obtain the wrapped element: ("foo", 0, [0, 0])
        wrapped = word.wrapped
        if isinstance(wrapped, Iterable) and len(wrapped) > 2:
            # Obtain the position stack of word: [0, 0]
            pos_stack = wrapped[2]
            if isinstance(pos_stack, Iterable) and len(pos_stack) > 0:
                # Obtain the paragraph number (first number in pos. stack): 0
                return pos_stack[0]

# ------------------------------------------------------------------------------
# Some util classes.

class SplitParagraph(diff.DiffWord):
    """ A diff operation that implies to split a paragraph. """
    
    def __init__(self, prefix_word):
        """ Creates a new split paragraph operation, located after the given
        word."""
        
        # To place the split operation after the given word (and before the
        # following word), we enrich its pos. stack by 0:
        # Assume that the given word has pos. stack [1, 0] and the following
        # word either [1,1] or [2,0]. The pos. stack of split operation is then
        # [1,0,0]. This guarantees, that the split operation keeps located 
        # between the words, even in case of sorting.
        pos_stack = list(prefix_word.wrapped[2])
        pos_stack.append(0)
        
        # Define a global pos for the operation between the given word and the
        # following word. 
        global_pos = prefix_word.wrapped[1]
        global_pos += 0.1
        
        # The wrapped attribute must be of the same scheme as the 'wrapped' 
        # attribute of a 'normal' DiffWord, which is 
        # [('hello', 'Hello '), global_pos, pos_stack].
        self.wrapped = [('‖', '‖ '), global_pos, pos_stack]
        self.text    = "‖"
        self.pos     = list(prefix_word.pos)

class MergeParagraph(diff.DiffWord):
    """ A diff operation that implies to merge a paragraph. """
    
    def __init__(self, prefix_word):
        """ Creates a new merge paragraph operation, located after the given
        word."""
        
        # To place the merge operation after the given word (and before the
        # following word), we enrich its pos. stack by 0:
        # Assume that the given word has pos. stack [1, 0] and the following
        # word either [1,1] or [2,0]. The pos. stack of merge operation is then
        # [1,0,0]. This guarantees, that the merge operation keeps located 
        # between the words, even in case of sorting.
        pos_stack = list(prefix_word.wrapped[2])
        pos_stack.append(0)
        
        # Define a global pos for the operation between the given word and the
        # following word. 
        global_pos = prefix_word.wrapped[1]
        global_pos += 0.1
        
        # The wrapped attribute must be of the same scheme as the 'wrapped' 
        # attribute of a 'normal' DiffWord, which is 
        # [('hello', 'Hello '), global_pos, pos_stack].
        self.wrapped = [('==', '== '), global_pos, pos_stack]
        self.text    = "=="
        self.pos     = list(prefix_word.pos)
