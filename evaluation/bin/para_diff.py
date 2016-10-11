import diff
import para_diff_rearrange as rearr

# TODO: Modify docs (e.g. "DocWord" is new).
# TODO: Format Code.
# TODO: Subphrases in rearrange phrases

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
    flatten_actual = flatten(actual)
    flatten_target = flatten(target)
  
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
    diff_phrases = diff.diff(flatten_actual, flatten_target, prefer_replaces=True)

    # Because diff doesn't know any paragraph boundaries (see above), we have
    # to divide the phrases to get phrases per actual paragraphs.                                                     
    diff_phrases = divide_phrases_into_paras(diff_phrases, "words_actual")
                                                                             
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
    diff_phrases = rearr.rearrange(diff_phrases, junk)
        
    # Decide where we have to split resp. merge the paragraphs.
    diff_phrases = split(diff_phrases)
    diff_phrases = merge(diff_phrases)
    
    return diff_phrases
    
# ------------------------------------------------------------------------------
# Methods to divide diff phrases per paragraph.
    
def divide_phrases_into_paras(phrases, list_str):
    """ Divides the given diff phrases to get phrases per actual paragraphs. """
    result = []
        
    # Keep track of the previous word.
    prev_word = None
        
    # Iterate through the phrases and divide them.
    for phrase in phrases:    
        paras, prev_word = divide_phrase_into_paras(phrase, list_str, prev_word)
        
        result.extend(paras)

    return result
         
def divide_phrase_into_paras(diff_phrase, list_name, prev_diff_word=None):
    """ Divides the given single diff phrase into phrases per actual paragraphs. 
    Returns the list of (sub-)phrases and the last processed actual word.
    """

    # The list of divided phrases.
    result = []

    # ----------------------------------------------------------------------
    
    # The last position where the given phrase was divided.
    last_divide_pos = 0
    
    def divide_diff_phrase(diff_phrase, pos):
        """ Divides the given phrase at the given position. Returns a new
        phrase of same type as the given phrase containing the words in
        bounds of last_divide_pos and pos. """
            
        nonlocal last_divide_pos
        klass = type(diff_phrase)

        # Thanks to python, we don't have to care about index bounds here.
        new_diff_words_actual = diff_phrase.words_actual[last_divide_pos : pos]
        new_diff_words_target = diff_phrase.words_target[last_divide_pos : pos]
            
        # Create new phrase, based on the type of given phrase.
        new_diff_phrase = klass(new_diff_words_actual, new_diff_words_target)
            
        last_divide_pos = pos
          
        return new_diff_phrase
    
    # ----------------------------------------------------------------------
    
    # Iterate through the predefined list of words.
    diff_words = getattr(diff_phrase, list_name)
    
    for i in range(0, len(diff_words)):
        diff_word = diff_words[i]                                                                       
        
        # Decide if we have to divide the phrase.
        if is_para_break_before(diff_word):
            # Divide the phrase
            divided_phrase = divide_diff_phrase(diff_phrase, i)  
            if not divided_phrase.is_empty():
                result.append(divided_phrase)
        
        # Update the previous words.
        prev_diff_word = diff_word
                           
    if last_divide_pos == 0:
        # We didn't have divided the phrase. So we can add the 
        # entire phrase to result list (without creating a new object).
        result.append(diff_phrase)
    else:
        # Create new phrase from the rest of the phrase.
        divided_phrase = divide_diff_phrase(diff_phrase, None)
            
        if not divided_phrase.is_empty():
            result.append(divided_phrase)
        
    return result, prev_diff_word

def split(diff_phrases):
    result = []
    
    for phrase in diff_phrases:    
        paras, _ = divide_phrase_into_paras(phrase, "words_target")

        for i in range(0, len(paras)):
            para = paras[i]
            para.split_before = False
            
            # Count split operation only if there is at least 1 equivalent
            # actual words. 
            if i > 0 and para.num_words_actual() > 0:
                para.split_before = True
                
            result.append(para)
            
        # Process the sub phrases of a rearrange phrase.
        if isinstance(phrase, rearr.DiffRearrangePhrase):
            phrase.sub_phrases = split(phrase.sub_phrases)
        
    return result

def merge(diff_phrases):
    """ Divides the given diff phrases to get phrases per actual paragraphs. """
     
    prev_word_actual = None
    prev_word_target = None
                    
    for phrase in diff_phrases:         
        phrase.merge_before = False
        
        # Process the sub phrases of a rearrange phrase.
        if isinstance(phrase, rearr.DiffRearrangePhrase):
            phrase.sub_phrases = merge(phrase.sub_phrases)
                    
        if phrase.num_words_actual() == 0 or phrase.num_words_target() == 0:
            continue
            
        first_word_actual = phrase.words_actual[0]
        first_word_target = phrase.words_target[0]
            
        is_para_break_actual = is_para_break(prev_word_actual, first_word_actual)
        is_para_break_target = is_para_break(prev_word_target, first_word_target)
                
        if (is_para_break_actual and is_para_break_target is False):
            phrase.merge_before = True
        
        prev_word_actual = phrase.words_actual[-1]
        prev_word_target = phrase.words_target[-1]
            
    return diff_phrases
                  
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
    
    # Set pointers to prev and next.
    for i in range(0, len(flattened)):
        prev = flattened[i - 1] if i > 0 else None
        curr = flattened[i]
        next = flattened[i + 1] if i < len(flattened) - 1 else None
    
        curr.prev = prev
        curr.next = next
    
    return flattened

def flatten_recursive(hierarchy, result, pos_stack=[]):
    """ Flattens given (sub-)hierarchy and stores the result to given list. """

    for i, element in enumerate(hierarchy):
        new_pos_stack = pos_stack + [i]
        
        if isinstance(element, list):
            flatten_recursive(element, result, new_pos_stack)
        else:
            result.append(ParaWord(element, len(result), new_pos_stack))

# TODO: Merge is_para_break_before and is_para_break 
def is_para_break_before(diff_word):
    """ Checks if there is a paragraph break between the given words.
    Returns True if both words are not None and there is a paragraph break in 
    between.
    Returns False if both words are not None and there is no paragraph break in 
    between.
    Returns None if at least one of both words is None.
    """
        
    if diff_word is None:
        return
    
    para_word = diff_word.wrapped
    
    if para_word is None:
        return
        
    prev_para_word = para_word.prev
        
    if prev_para_word is None:
        return  
            
    return prev_para_word.get_para_num() != para_word.get_para_num()

def is_para_break(prev_diff_word, diff_word):
    """ Checks if there is a paragraph break between the given words.
    Returns True if both words are not None and there is a paragraph break in 
    between.
    Returns False if both words are not None and there is no paragraph break in 
    between.
    Returns None if at least one of both words is None.
    """
        
    if diff_word is None or prev_diff_word is None:
        return
    
    para_word = diff_word.wrapped
    prev_para_word = prev_diff_word.wrapped
    
    if para_word is None or prev_para_word is None:
        return
                    
    return prev_para_word.get_para_num() != para_word.get_para_num()
        
# ------------------------------------------------------------------------------
# Some util classes.

# TODO: Comments
class ParaWord:
    def __init__(self, word, global_pos, pos_stack):
        self.wrapped    = word
        self.global_pos = global_pos
        self.pos_stack  = pos_stack
        self.prev_word  = None
        self.next_word  = None
    
    def get_para_num(self):
        return self.pos_stack[0]
    
    def __str__(self):
        return str(self.wrapped)
        
    def __repr__(self):
        return "ParaWord(%s, %s, %s)" % (repr(self.wrapped), self.global_pos, self.pos_stack)
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
            
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
