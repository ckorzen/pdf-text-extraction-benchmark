import word_diff
import unicodedata
import re
import pickle

# TODO: Read the tex line number and column number.
# TODO: Some special characters are omitted on visualization.

# ==============================================================================
# Normalize methods.


def string_to_diff_words(
        string, flatten=True, compose_characters=False,
        para_delimiter="\n\s*\n\s*", word_delimiter="\s+", to_lower=True,
        specialchars_pattern="(?<!\d)\W+|\W+(?!\d)", excludes=[]):
    """
    Splits the given string into paras and normalizes the words of each
    paragraph: Removes all symbols defined by "SPECIAL_CHARS_PATTERN" (see
    above) and transforms all letters to lowercases, if the 'to_lower' flag is
    set to True.
    You can exclude certain words from normalization by defining a list
    'excludes' of related regular expression that matches the words you wish
    to exclude.
    Returns a list of lists of DiffWords, where the inner lists represent
    the paras (one list for each paragraph) with included words. Each
    DiffWord includes the normalized and the unnormalized version (with
    trailing whitespaces) of a word. For the text 'target', the result is:
    [[DiffWord("the", "The "), DiffWord("big", "big, "), ...],
     [DiffWord("eats", "eats "), DiffWord("the", "the "), ...]]
    """

    if string is None or len(string) == 0:
        return []

    # Compose characters.
    if compose_characters:
        string = compose_characters(string)

    # Split the string into DiffWords, broken down into paragraphs.
    paras = split_into_paras_and_words(
        string, para_delimiter, word_delimiter, to_lower, specialchars_pattern,
        excludes)

    if flatten:
        paras = flatten_list(paras)

    return paras


def compose_characters(string):
    """
    Composes the characters of given string. Unicode can hold "decomposed"
    characters, e.g. characters with accents where the accent is a character on
    its own (for example, the character "ä" could be actually two characters:
    "a" and the two dots. Tries to compose these characters to a single one
    using unicodedata.
    """

    if string is None or len(string) == 0:
        return string

    # Compute the list of unicode code points from text.
    codepoints = [ord(i) for i in string]

    # Unicodedata has issues to compose
    # "LATIN SMALL LETTER DOTLESS I" (that is an 'i' without the dot) /
    # "LATIN SMALL LETTER DOTLESS J" (that is an 'j' without the dot)
    # with accents. So replace all occurrences of these chars by "i" resp. "j".
    # Map "LATIN SMALL LETTER DOTLESS I" to "LATIN SMALL LETTER I"
    # Map "LATIN SMALL LETTER DOTLESS J" to "LATIN SMALL LETTER J"
    mappings = {0x0131: 0x0069, 0x0237: 0x0061}
    for i, codepoint in enumerate(codepoints):
        if codepoint in mappings:
            codepoints[i] = mappings[codepoint]

    # Normalize (= compose the characters) using unicodedata.
    # Valid values for normalization are:
    # NFD: Normal Form D(ecomposition)
    #   Translates each character into its decomposed form
    # NFC: Normal Form C(omposition)
    #   First applies a NFD, then composes pre-combined characters again
    # NFKD: Normal Form KD
    #   Applies the compatibility decomposition, i.e. replaces all
    #   compatibility characters with their equivalents
    # NFKC: The normal form KC
    #   First applies the compatibility decomposition, followed by the NFD.
    #
    # TODO: Check, if "NFKC" fixes the issues with "LATIN SMALL LETTER DOTLESS
    #       I" above.
    return unicodedata.normalize("NFC", "".join([chr(i) for i in codepoints]))


def split_into_paras_and_words(
        string,
        para_delimiter="\n\s*\n\s*",
        word_delimiter="[\W\s]",
        to_lower=True,
        # specialchars_pattern: special chars that are not surrounded by digits
        specialchars_pattern="(?<!\d)[\W\s]|[\W\s](?!\d)",
        excludes=[]):
    """
    Splits the given string into paragraphs and words.
    For example, the call
      split_into_paras_and_words(
        "A-B[formula]C \n\n X Y/Z",
        para_delimiter="\n\s*\n\s*",
        word_delimiter="\s+",
        to_lower=True,
        specialchars_pattern="\W+",
        excludes=["\[formula\]"])
    results in:
        [["a", "b", "[formula]", "c"], ["x", "y", "z"]]
    """

    if string is None or len(string) == 0:
        return []

    # The identified paragraphs to return.
    result = []
    # The previous DiffWord.
    prev_diff_word = None
    # The pattern to identify words to exclude.
    excludes_pattern = "|".join(excludes)

    # Split the string into paragraphs (keeping the delimiters):
    # "A-B[formula]C \n\n X Y/Z" -> ["A-B[formula]C", "X Y/Z"]
    paras = split(string, para_delimiter)
    for i in range(0, len(paras), 2):
        para = paras[i]  # -> "A-B[formula]C "
        para_delim = paras[i+1]  # -> "\n\n "

        diff_words_per_para = []

        def create_diff_word(word, word_delim, exclude=False):
            """
            Creates a new diff word from given word and given word delimiter.
            Appends the word to the list of para words.
            """
            nonlocal prev_diff_word
            nonlocal diff_words_per_para
            nonlocal to_lower
            nonlocal specialchars_pattern

            norm = word
            # Don't normalize the word, if exclude is True.
            if not exclude:
                # Normalize the word.
                if to_lower:
                    norm = norm.lower()
                if len(specialchars_pattern) > 0:
                    norm = filter_special_chars(norm, specialchars_pattern)

            # Create new diff word, if the word is non-empty.
            if norm is not None and len(norm) > 0:
                diff_word = word_diff.DiffWord(norm)
                diff_word.unnormalized = word
                diff_word.whitespaces = word_delim
                diff_word.unnormalized_with_whitespaces = word + word_delim
                diff_word.exclude = exclude

                # Update prev and next pointers.
                diff_word.prev, diff_word.next = None, None
                if prev_diff_word is not None:
                    prev_diff_word.next = diff_word
                    diff_word.prev = prev_diff_word
                diff_words_per_para.append(diff_word)
                prev_diff_word = diff_word

        # Split the paragraph into words based on whitespaces:
        # "X Y/Z" -> ["X", "Y/Z"]
        words = split(para, word_delimiter)
        for j in range(0, len(words), 2):
            word = words[j]
            word_delim = words[j+1]

            # Split the word into non-excludes and excludes:
            # "A-B[formula]C" -> ["A-B", "[formula]", "C"]
            exclude_words = split(word, excludes_pattern)
            # Process the non-excludes and excludes.
            for k in range(0, len(exclude_words), 2):
                non_exclude = exclude_words[k]
                exclude = exclude_words[k+1]

                # Split each non_exclude on special-characters:
                # "A-B" -> ["A", "B"]
                special_words = split(non_exclude, specialchars_pattern)
                for l in range(0, len(special_words), 2):
                    non_special_word = special_words[l]
                    special_chars = special_words[l+1]
                    # Create diff word from non_special_word.
                    create_diff_word(non_special_word, special_chars)

                # Create diff word from word to exclude.
                create_diff_word(exclude, "", True)

            if len(diff_words_per_para) > 0:
                # Append the word delimiter to the last word.
                last_word = diff_words_per_para[-1]
                last_word.whitespaces += word_delim
                last_word.unnormalized_with_whitespaces += word_delim

        if len(diff_words_per_para) > 0:
            # Append the para delimiter to the last word.
            last_word = diff_words_per_para[-1]
            last_word.whitespaces += para_delim
            last_word.unnormalized_with_whitespaces += para_delim
            result.append(diff_words_per_para)

    return result

# ==============================================================================


def split(string, delim):
    """
    Splits the given string on given delimiter and returns a list of substrings
    that contains the delimiters as well. The number of elements in the list is
    always even.
    """

    if string is None:
        return None

    if len(string) == 0:
        return ["", ""]

    if delim is None or len(delim) == 0:
        return [string, ""]

    # Put the delimiter into parentheses to get the delimiters as well:
    # "A \n\n B \n\n C" -> ["A", "\n\n", "B", "\n\n", "C"].
    res = re.split("(%s)" % delim, string)

    # Make sure, that there is an even number of elements in list.
    if len(res) % 2 != 0:
        res.append("")

    return res


def is_special_character(string, i, pattern):
    """
    Returns True if the i-th character in the given string is a special
    character, False otherwise. The character is *not* a special character if
    it is a dot and it is surrounded by two digits, like in "1.23".

    >>> is_special_character(["a", "-", "b"], 0)
    False
    >>> is_special_character(["a", "-", "b"], 1)
    True
    >>> is_special_character(["1", ".", "2"], 1)
    False
    """

    if string is None or len(string) == 0:
        return False

    if pattern is None or len(pattern) == 0:
        return False

    if i < 0:
        return False

    if i >= len(string):
        return False

    char = string[i]

    # The character is *not* a special character if it doesn't match the
    # defined pattern.
    if not re.match(pattern, char):
        return False

    # The character is *not* a special character if it is a dot and it is
    # surrounded by two digits.
    if char == "." and i > 0 and i < len(string) - 1:
        prev_char = string[i - 1]
        next_char = string[i + 1]
        if prev_char.isdigit() and next_char.isdigit():
            return False
    return True


def filter_special_chars(text, pattern):
    """ Replaces all special characters from the given text by empty string."""
    if text is None:
        return

    if pattern is None:
        return text

    chars = []
    for i, x in enumerate(text):
        if not is_special_character(text, i, pattern):
            chars.append(x)
    return "".join(chars)

# ==============================================================================


def flatten_list(words):
    """
    Examines the given (nested) list of words, that must be given as strings or
    DiffWord objects. The list may be nested by depth 1, where the elements at
    level 0 represent the paras and the elements at level 1 represent the
    words of a paragraph. Returns a flat list of DiffWord objects, where each
    object is enriched with the following properties:
        para       : The index of paragraph in which the word is included.
        pos_in_para: The position of word in the paragraph.
        pos_in_text: The position of word in the whole text.
    """
    if words is None or len(words) == 0:
        return []

    flat = []

    def process_element(element, para=0, pos_in_para=0):
        # The element is a DiffWord. Add some metadata.
        if isinstance(element, word_diff.DiffWord):
            element.para = para
            element.pos_in_para = pos_in_para
            element.pos_in_text = len(flat)
            element.pos = (para, pos_in_para)
            flat.append(element)
        elif isinstance(element, str):
            # The element is a string. Create a DiffWord.
            word = word_diff.DiffWord(element)
            word.para = para
            word.pos_in_para = pos_in_para
            word.pos_in_text = len(flat)
            word.pos = (para, pos_in_para)
            flat.append(word)

    # Iterate through the elements of given list.
    for i, element in enumerate(words):
        if isinstance(element, list):
            # The element is a list. Process its subelements.
            for j, subelement in enumerate(element):
                process_element(subelement, i, j)
        else:
            # The list isn't nested -> para is 0 for each element.
            process_element(element, 0, i)

    return flat

# ==============================================================================


def ignore_phrase(phrase, junk=[]):
    """
    Returns true if the given phrase contains a target word that matches
    any pattern given in junk.
    """
    if phrase is None:
        return False

    if not isinstance(phrase, word_diff.DiffPhrase):
        return False

    return ignore_words(phrase.words_target, junk)


# def ignore_words(words, junk=[]):
#    """
#    Returns true if the given word matches any pattern given in junk.
#    """

#    for word in words:
#        if ignore_word(word, junk):
#            return True

#    return False

def ignore_words(words, junk=[]):
    """
    Returns true if the given word matches any pattern given in junk.
    """

    if words is None or len(words) == 0:
        return False

    num_ignore_words = 0
    for word in words:
        if ignore_word(word, junk):
            num_ignore_words += 1

    if len(words) == num_ignore_words:
        return True
    if len(words) < 10:
        return num_ignore_words > 0
    return num_ignore_words / (len(words) - num_ignore_words) >= 0.5


def ignore_word(word, junk=[]):
    """
    Returns true if the given word matches any pattern given in junk.
    """

    if word:
        if any(re.search(regex, str(word)) for regex in junk):
            return True

    return False


def serialize_diff_result(diff_result, path):
    """ Serializes the diff result to given path. """
    serialization_file = open(path, "wb")
    pickle.dump(diff_result, serialization_file)
    serialization_file.close()


def deserialize_diff_result(path):
    """ Deserializes the diff result from given path. """
    serialization_file = open(path, "rb")
    diff_result = pickle.load(serialization_file)
    serialization_file.close()
    return diff_result
