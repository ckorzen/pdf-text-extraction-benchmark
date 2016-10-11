from collections import Iterable

def diff(actual, target, prefer_replaces=False):
    """ Compares the given lists of words (strings or any iterables where 
    the first element is a string) and outputs a list of DiffPhrase objects 
    wheer each of them represents either a sequence of common words in actual 
    and target or a sequence of words to replace. 
    If prefer_replaces is True, common phrases which are dominated by replace
    phrases will be transformed to replace phrases, too. 
    Example: Consider the texts
      A = The red  fox hunts the cow.
      B = A   blue fox sits  on   a   chair.
      
    The only common phrase is [= "fox"]. This phrase is bordered by the replace 
    phrases [/ "The red", "A blue"] and [/ "hunts the cow", "sits on a chair"].
    We say that a common phrase is dominated by replace phrases, if there are 
    a replace phrase to the left and a replace phrase to the right, whose 
    minimal length is larger or equal to the length of the common phrase.
    
    Note: This diff version doesn't know any paragraph boundaries. If you need
    diff phrases per paragraph you need to setup them from the given result on
    your own.
    
    >>> diff(["foo", "bar"], ["foo", "bar"]) 
    [[= [foo, bar], [foo, bar]]]
    
    >>> diff(["foo", "bar"], ["foo"])
    [[= [foo], [foo]], [/ [bar], []]]
    
    >>> diff(["foo"], ["foo", "bar"])
    [[= [foo], [foo]], [/ [], [bar]]]
    """
    
    result = []
    _diff(actual, target, result, prefer_replaces)
    
    return result
           

def _diff(actual, target, result, prefer_replaces=False, pos1=0, pos2=0):
    """ Compares the given lists of elements recursively and puts the computed
    operation of this run into the result list. 'pos1' denotes the position in
    'actual' and 'pos2' denotes the position in 'target'. Both values have no
    special meaning but can be used for sorting purposes for example."""
        
    # Create an index of all positions of an string in 'actual'.
    actual_dict = dict()  
    for i, actual_word in enumerate(actual):
        # word -> [list of positions]
        actual_dict.setdefault(str(actual_word), []).append(i)

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

    for i, target_word in enumerate(target):
        _overlap = {}
        
        # Iterate through target words and fetch the positions in actual.
        for j in actual_dict.get(str(target_word), []):
            # The expression "j and i" returns i if j > 0, 0 otherwise
            _overlap[j] = (j and overlap.get(j - 1, 0)) + 1
            # Check if this is the largest substring seen so far.
            if _overlap[j] > length:
                length = _overlap[j]
                actual_start = j - length + 1
                target_start = i - length + 1
        overlap = _overlap

    create_replace = (length == 0)
    if prefer_replaces:
        # Compute minimal length of left replace. 
        len_left_actual = actual_start
        len_left_target = target_start
        min_len_left = min(len_left_actual, len_left_target)
        
        # Compute minimal length of right replace.
        len_right_actual = len(actual) - (actual_start + length)
        len_right_target = len(target) - (target_start + length)
        min_len_right = min(len_right_actual, len_right_target)
        
        # Define condition when to create the replace.
        if length <= 1:
            create_replace = length <= min_len_left and length <= min_len_right
        
    if create_replace:
        if actual or target:
            # No common substring found. Create a replace phrase.
            actual_words = create_actual_diff_words(actual, pos1, pos2)
            target_words = create_target_diff_words(target, pos1, pos2)
            result.append(DiffReplacePhrase(actual_words, target_words))
            
            pos1 += len(actual)
            pos2 += len(target)
    else:
        # A common substring was found. Call diff recursively for the 
        # substrings to the left and to the right
        actual_left = actual[ : actual_start]
        target_left = target[ : target_start]
        pos1, pos2 = _diff(actual_left, target_left, result, 
            prefer_replaces, pos1, pos2)
        
        actual_middle = actual[actual_start : actual_start + length]
        target_middle = target[target_start : target_start + length]
        
        # Create the common phrase.
        actual_words = create_actual_diff_words(actual_middle, pos1, pos2)
        target_words = create_target_diff_words(target_middle, pos1, pos2)
        result.append(DiffCommonPhrase(actual_words, target_words))
        
        pos1 += length
        pos2 += length
        
        actual_right = actual[actual_start + length : ]
        target_right = target[target_start + length : ]
        pos1, pos2 = _diff(actual_right, target_right, result, 
            prefer_replaces, pos1, pos2)

    return pos1, pos2

# ------------------------------------------------------------------------------
# Some util methods.

def create_actual_diff_words(words, pos_a, pos_t):
    """ Transforms the given list of words into list of diff words. """
    return [DiffWord(word, pos_a + i, pos_t) for i, word in enumerate(words)]
        
def create_target_diff_words(words, pos_a, pos_t):
    """ Transforms the given list of words into list of diff words. """
    return [DiffWord(word, pos_a, pos_t + i) for i, word in enumerate(words)]

# ------------------------------------------------------------------------------
# Some util classes.

class DiffWord:
    """ A wrapper for a single word, used to be able to enrich a word with some
    diff metadata (positions, etc.). """

    def __init__(self, word, pos_actual, pos_target):
        """ Creates a new word wrapper for given word. pos_actual and 
        pos_target are the current positions in actual and target on creating 
        this wrapper. """
        self.wrapped = word
        self.pos     = [pos_actual, pos_target]
            
    def __str__(self):
        return str(self.wrapped)

    def __repr__(self):
        return "DiffWord(%s, %s)" % (repr(self.wrapped), self.pos)

class DiffPhrase:
    """ A wrapper for a sequence of DiffWords. It represents the super class
    for DiffCommonPhrase and DiffReplacePhrase. """

    def __init__(self, words_actual=[], words_target=[]):
        """ Creates a new wrapper for given lists of words. """
        self.words_actual = words_actual
        self.words_target = words_target
        self.pos = [-1, -1]
        if len(words_actual) > 0:
            self.pos = words_actual[0].pos
        elif len(words_target) > 0:
            self.pos = words_target[0].pos
        
        # Needed for rearrange
        for word in words_actual:
            word.phrase = self
        for word in words_target:
            word.phrase = self

    def num_words_actual(self):
        return len(self.words_actual)

    def num_words_target(self):
        return len(self.words_target)

    def is_empty(self):
        return self.num_words_actual() == 0 and self.num_words_target() == 0
    
    def get_str_pattern(self):
        """ Returns the pattern to use on formatting the string representation
        of this phrase."""
        return "[? %s, %s]"
    
    def __str__(self):
        actual_word_texts = [str(x) for x in self.words_actual]
        target_word_texts = [str(x) for x in self.words_target]
        return self.get_str_pattern() % (actual_word_texts, target_word_texts)
        
    def __repr__(self):
        return self.get_str_pattern() % (self.words_actual, self.words_target)

class DiffCommonPhrase(DiffPhrase):
    """ A phrase of words, that are common in actual and target. """ 
    def __init__(self, words_actual, words_target):
        super(DiffCommonPhrase, self).__init__(words_actual, words_target)

    def get_str_pattern(self):
        return "[= %s, %s]"
    
class DiffReplacePhrase(DiffPhrase):
    """ A phrase of words which need to replace between actual and target. """ 
    def __init__(self, words_actual, words_target):
        super(DiffReplacePhrase, self).__init__(words_actual, words_target)
        
    def get_str_pattern(self):
        return "[/ %s, %s]"
