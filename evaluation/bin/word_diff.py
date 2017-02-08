import diff_utils
import logging

logging.basicConfig(
    format='%(asctime)s : %(levelname)s : %(module)s : %(message)s',
    level=logging.INFO
)
log = logging.getLogger(__name__)


def word_diff_from_strings(
        actual,
        target,
        refuse_common_threshold=0,
        compose_characters=False,
        para_delimiter="\n\s*\n\s*",
        word_delimiter="\s+",
        to_lower=False,
        specialchars_pattern="\W+",
        excludes=[]):
    """
    Does a word diff based on given strings.

    Args:
        actual: The actual string.
        target: The target string.
        prefer_replace_value:
        compose_characters:
        para_delimiter:
        word_delimiter:
        to_lower:
        specialchars_pattern:
        excludes:

    Returns:
        List of DiffCommonPhrases and DiffReplacePhrases.

    Note:
        This diff version doesn't know any paragraph boundaries. If you need
        diff phrases per paragraph you need to setup them from the given result
        on your own.
    """

    actual = "" if actual is None else actual
    target = "" if target is None else target

    # Transform actual to flat list of DiffWords.
    actual = diff_utils.string_to_diff_words(
        actual,
        flatten=True,
        compose_characters=compose_characters,
        para_delimiter=para_delimiter,
        word_delimiter=word_delimiter,
        to_lower=to_lower,
        specialchars_pattern=specialchars_pattern,
        excludes=excludes)

    # Transform target to flat list of DiffWords.
    target = diff_utils.string_to_diff_words(
        target,
        flatten=True,
        compose_characters=compose_characters,
        para_delimiter=para_delimiter,
        word_delimiter=word_delimiter,
        to_lower=to_lower,
        specialchars_pattern=specialchars_pattern,
        excludes=excludes)

    return word_diff(actual, target, refuse_common_threshold)


def word_diff(actual, target, refuse_common_threshold=0):
    """ Does a word diff based on flat lists of DiffWord objects. """

    actual = [] if actual is None else actual
    target = [] if target is None else target

    diff_result = DiffResult()

    # Provide some metadata.
    diff_result.num_words_actual = len(actual)
    diff_result.num_words_target = len(target)
    diff_result.first_word_actual = actual[0] if len(actual) > 0 else None
    diff_result.last_word_actual = actual[-1] if len(actual) > 0 else None
    diff_result.first_word_target = target[0] if len(target) > 0 else None
    diff_result.last_word_target = target[-1] if len(target) > 0 else None

    if len(actual) == 0 and len(target) == 0:
        return diff_result

    wdiff(actual, target, diff_result, refuse_common_threshold)
    return diff_result


# ==============================================================================

def wdiff(actual, target, result, refuse_common_threshold=0, pos1=0, pos2=0):
    """
    Compares the given lists of words recursively and appends computed phrases
    to given result list. pos1 denotes the position in actual and pos2
    denotes the position in target.
    """

    # Don't proceed if both word lists are empty.
    if len(actual) == 0 and len(target) == 0:
        return pos1, pos2

    # Create an index of all positions per actual word.
    actual_dict = dict()
    for i, actual_word in enumerate(actual):
        # word -> [list of positions]
        actual_dict.setdefault(actual_word.word, []).append(i)

    # Find the largest common substring in actual and target.
    #
    # Iterate through the target words. At each iteration step, overlap[i]
    # is the length of the largest suffix of actual[:i] equal to a suffix
    # of target[:j] (or unset when actual[i] != target[j]).
    #
    # At each iteration step, the new overlap (called _overlap until
    # the original overlap is no longer needed) is built from actual.
    #
    # If the length of overlap exceeds the largest substring seen so far
    # (length), we update the largest substring to the overlapping strings.
    overlap = {}

    # The start index of the longest common substring in actual.
    actual_start = 0
    # The start index of the longest common substring in target.
    target_start = 0
    # The length of the longest common substring.
    length = 0

    # Iterate through target words and fetch the positions in actual.
    for i, target_word in enumerate(target):
        _overlap = {}
        for j in actual_dict.get(target_word.word, []):
            # The expression "j and i" returns i if j > 0; 0 otherwise
            _overlap[j] = (j and overlap.get(j - 1, 0)) + 1
            # Check if this is the longest common substring seen so far.
            if _overlap[j] > length:
                length = _overlap[j]
                actual_start = j - length + 1
                target_start = i - length + 1
        overlap = _overlap

    # Explanations about the refuse_common_threshold parameter:
    # Consider the following two strings:
    # A B C X D E F
    # G H I X J K L
    # Normally, the strings would result in the phrases
    # Replace("A B C", "G H I")
    # Common("X", "X")
    # Replace("D E F", "J K L")
    # Sometimes, this is an unwanted behaviour and one would like to have
    # Replace("A B C X D E F", "G H I X J K L")
    # With the refuse_common_threshold you can define a length for common
    # phrases to refuse and to embed it into a replace as long as the common
    # phrase is *dominated* by replace phrases of at least the same length to
    # the left and to the right.

    # Decide whether to create a replace or a common phrase.
    create_replace = False
    if length == 0:
        # Create replace if common length is 0.
        create_replace = True
    elif length > refuse_common_threshold:
        # Create common if common length is larger than 0 and larger than
        # the refuse_common_threshold.
        create_replace = False
    else:
        # The common length is > 0 and <= refuse_common_threshold.
        # Check if the common phrase is dominated by replace phrases to the
        # right and to the left.
        actual_left_len = actual_start
        actual_right_len = len(actual) - (actual_start + length)
        actual_min_len = min(actual_left_len, actual_right_len)
        actual_dominated = 2 * length < actual_min_len # TODO

        target_left_len = target_start
        target_right_len = len(target) - (target_start + length)
        target_min_len = min(target_left_len, target_right_len)
        target_dominated = 2 * length < target_min_len # TODO

        # Create replace if the common phrase is dominated by replace phrases.
        create_replace = actual_dominated or target_dominated

    if create_replace:
        # Create a replace phrase.
        phrase = DiffReplacePhrase(pos1, actual, pos2, target)

        result.phrases.append(phrase)
        pos1 += len(actual)
        pos2 += len(target)
    else:
        # Run wdiff on left half.
        actual_left = actual[: actual_start]
        target_left = target[: target_start]
        pos1, pos2 = wdiff(
            actual_left, target_left, result,
            refuse_common_threshold, pos1, pos2)
        actual_middle = actual[actual_start:actual_start + length]
        target_middle = target[target_start:target_start + length]

        # Create the common phrase.
        phrase = DiffCommonPhrase(pos1, actual_middle, pos2, target_middle)
        result.phrases.append(phrase)
        pos1 += len(actual_middle)
        pos2 += len(target_middle)

        # Run wdiff on right half.
        actual_right = actual[actual_start + length:]
        target_right = target[target_start + length:]
        pos1, pos2 = wdiff(
            actual_right, target_right, result,
            refuse_common_threshold, pos1, pos2)

    return pos1, pos2

# ------------------------------------------------------------------------------
# Some util classes.


class DiffWord:
    """ A word that represents an element of a DiffPhrase. """

    def __init__(self, word):
        """ Creates a new DiffWord object. """
        self.word = word

    def __str__(self):
        """ Returns this word in string representation. """
        return self.word

    def __repr__(self):
        """ Returns this word in string representation. """
        return self.__str__()


class DiffPhrase:
    """
    A phrase of DiffWords. It represents the super class for DiffCommonPhrase
    and DiffReplacePhrase (see below).
    """

    def __init__(self, pos_actual, words_actual, pos_target, words_target):
        """ Creates a new phrase of diff words. """

        # Prepare the actual words.
        self.pos_actual = pos_actual
        self.words_actual = []
        for i, word in enumerate(words_actual):
            word.pos_actual = pos_actual + i
            word.pos_target = pos_target
            word.phrase = self
            self.words_actual.append(word)

        # Prepare the target words.
        self.pos_target = pos_target
        self.words_target = []
        for i, word in enumerate(words_target):
            word.pos_actual = pos_actual
            word.pos_target = pos_target + i
            word.phrase = self
            self.words_target.append(word)

    def subphrase(self, start, end):
        """
        Creates a subphrase from this phrase, starting at given start index
        (inclusive) and ending at given end index (exclusively).
        This method is needed in para_diff to divide a phrase in order to meet
        paragraph boundaries.
        """
        words_actual = self.words_actual[start:end]
        words_target = self.words_target[start:end]
        pos_actual = self.pos_actual
        if len(words_actual) > 0:
            pos_actual = words_actual[0].pos_actual
        pos_target = self.pos_target
        if len(words_target) > 0:
            pos_target = words_target[0].pos_target
        # Call the constructor of the concrete subtype.
        return type(self)(pos_actual, words_actual, pos_target, words_target)

    @property
    def first_word_actual(self):
        """ Returns the first actual word. """
        return self.words_actual[0] if len(self.words_actual) > 0 else None

    @property
    def first_word_target(self):
        """ Returns the first target word. """
        return self.words_target[0] if len(self.words_target) > 0 else None

    @property
    def last_word_actual(self):
        """ Returns the last actual word. """
        return self.words_actual[-1] if len(self.words_actual) > 0 else None

    @property
    def last_word_target(self):
        """ Returns the last target word. """
        return self.words_target[-1] if len(self.words_target) > 0 else None

    @property
    def num_words_actual(self):
        """ Returns the number of actual words. """
        return len(self.words_actual)

    @property
    def num_words_target(self):
        """ Returns the number of target words. """
        return len(self.words_target)

    def is_empty(self):
        """ Returns True if both, words_actual and words_target are empty. """
        return self.num_words_actual == 0 and self.num_words_target == 0

    def get_str_pattern(self):
        """ Returns the pattern to use on creating string representation. """
        return "[? %s, %s]"

    def __str__(self):
        """ Returns this phrase in string representation. """
        actual_word_texts = [str(x) for x in self.words_actual]
        target_word_texts = [str(x) for x in self.words_target]
        return self.get_str_pattern() % (actual_word_texts, target_word_texts)

    def __repr__(self):
        """ Returns this phrase in string representation. """
        return self.get_str_pattern() % (self.words_actual, self.words_target)


class DiffCommonPhrase(DiffPhrase):
    """ A phrase of common words. """

    def get_str_pattern(self):
        """ Returns the pattern to use on creating string representation. """
        return "[= %s, %s]"


class DiffReplacePhrase(DiffPhrase):
    """ A phrase of words to replace. """

    def get_str_pattern(self):
        """ Returns the pattern to use on creating string representation. """
        return "[/ %s, %s]"


class DiffResult:
    """ An object that represents the result of a diff run. """

    def __init__(self, phrases=None):
        self.phrases = phrases if phrases is not None else []
        
    def __getstate__(self):
        """Return state values to be pickled."""
        return (self.num_ops, self.num_ops_rel, self.vis)

    def __setstate__(self, state):
        """Restore state from the unpickled state values."""
        if type(state) == dict:
            self.num_ops = state["num_ops"]
            self.num_ops_rel = state["num_ops_rel"]
            self.vis = state["vis"]
        else:
            self.num_ops, self.num_ops_rel, self.vis = state
        
