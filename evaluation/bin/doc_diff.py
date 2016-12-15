import re
import unicodedata
import para_diff

# TODO: Consider the new argument 'excludes'.

def doc_diff(actual, target, excludes=[], junk=[]):
    """ Given two texts, 'actual' and 'target', this method outputs a sequence
    of phrases which can be used to determine the operations required to 
    transform 'actual' into 'target'. A phrase is defined by a sequence of 
    words. The texts are seen as sequences of paragraphs (text blocks separated 
    by two or more newlines). Phrases lives within paragraphs and do *not* 
    exceed paragraph boundaries. We differ in the following phrases:
    
    * CommonPhrase: 
        A phrase that is common to both texts.
    * ReplacePhrase: 
        A phrase x in 'actual' to replace by a phrase y in 'target'.
        Note that x or y could be empty, i.e. a ReplacePhrase could indeed 
        represent a phrase to delete from 'actual' or a phrase to insert into 
        'target'.
    * RearrangePhrase: 
        A phrase that is common to both texts but their order in the texts 
        differ.
        
    On comparing, words are normalized, i.e. punctuation marks and any other
    special characters will be removed and all characters will be transformed 
    to lowercases. 
    You can exclude certain words from normalization by defining a list 
    'excludes' of related regular expression that matches the words you wish 
    to exclude.
    
    Let us define the following running example to use throughout the whole 
    documentation:
    
    actual                  target:
    --------------------    --------------------
    The quick, big fox      The big, quick fox
    eats the ice-cold 
    sandwich.               eats the ice-cold
                            sandwich.
    
    Note, that 'actual' consists of one paragraph and 'target' consists of two
    paragraphs.
    """
    # TODO: Explain 'junk'.


    # Split 'actual' and 'target' into paragraphs and normalize the words.
    #
    # Result is a list of lists of DocWords, where the inner lists represent 
    # the paragraphs (one list for each paragraph) with included words. Each 
    # DocWord includes the normalized and the unnormalized version (with 
    # trailing whitespaces) of a word:
    # paras_target = [[DocWord("the", "The "), DocWord("big", "big, "), ...],
    #                 [DocWord("eats", "eats "), DocWord("the", "the "), ...]]
    paras_actual = to_normalized_paras(actual, to_lower=True, excludes=excludes)
    paras_target = to_normalized_paras(target, to_lower=True, excludes=excludes)

    return {
        "num_paras_actual": len(paras_actual),
        "num_paras_target": len(paras_target),
        "num_words_actual": sum(len(x) for x in paras_actual),
        "num_words_target": sum(len(x) for x in paras_target),
        "phrases": para_diff.para_diff(paras_actual, paras_target, junk) 
    }

# ==============================================================================
# Normalize methods.

# The pattern defining the characters to remove on normalization: All symbols
# which are no "word characters" (\w) and no whitespaces (\s) 
SPECIAL_CHARS_PATTERN = re.compile("[^\w\s]")
    
def to_normalized_paras(text, to_lower=True, excludes=[]):
    """ Splits the text into paragraphs and normalizes the words of each 
    paragraph: Removes all symbols defined by "SPECIAL_CHARS_PATTERN" (see 
    above) and transforms all letters to lowercases, if the 'to_lower' flag is 
    set to True.
    You can exclude certain words from normalization by defining a list 
    'excludes' of related regular expression that matches the words you wish 
    to exclude.
    Returns a list of lists of DocWords, where the inner lists represent 
    the paragraphs (one list for each paragraph) with included words. Each 
    DocWord includes the normalized and the unnormalized version (with 
    trailing whitespaces) of a word. For the text 'target', the result is:
    [[DocWord("the", "The "), DocWord("big", "big, "), ...],
     [DocWord("eats", "eats "), DocWord("the", "the "), ...]] 
    
    >>> to_normalized_paras("Foo Bar")
    [[DocWord(foo, Foo ), DocWord(bar, Bar )]]
    >>> to_normalized_paras("Foo \\n\\n Bar")
    [[DocWord(foo, Foo 
    <BLANKLINE>
     )], [DocWord(bar, Bar )]]
    >>> to_normalized_paras("Foo [formula]")
    [[DocWord(foo, Foo ), DocWord(formula, [formula] )]]
    >>> to_normalized_paras("Foo-bar-baz", excludes=["\[formula\]"])
    [[DocWord(foo, Foo-), DocWord(bar, bar-), DocWord(baz, baz )]]
    >>> to_normalized_paras("Foo [formula]", excludes=["\[formula\]"])
    [[DocWord(foo, Foo ), DocWord([formula], [formula] )]]
    """
       
    # Compose the characters of text (merge decomposed characters).
    text = compose_characters(text)
          
    # Split the text into paragraphs:
    para_words = split_into_paragraphs(text)
               
    # Normalize the words of each paragraph (create DocWord objects).    
    return [normalize_words(words, to_lower, excludes) for words in para_words]

def compose_characters(text):
    """ Composes the characters of given text. Unicode can hold "decomposed" 
    characters, e.g. characters with accents where the accent is a character on 
    its own (for example, the character "ä" could be actually two characters: 
    "a" and the two dots. Tries to compose these characters to a single one 
    using unicodedata."""
        
    # Compute the list of unicode code points from text.
    codepoints = [ord(i) for i in text]
    
    # Unicodedata has issues to compose 
    # "LATIN SMALL LETTER DOTLESS I" (that is an 'i' without the dot) / 
    # "LATIN SMALL LETTER DOTLESS J" (that is an 'j' without the dot)
    # with accents. So replace all occurrences of these chars by "i" resp. "j".
    # Map "LATIN SMALL LETTER DOTLESS I" to "LATIN SMALL LETTER I"
    # Map "LATIN SMALL LETTER DOTLESS J" to "LATIN SMALL LETTER J"        
    mappings = { 0x0131: 0x0069, 0x0237: 0x0061 } 
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
    # TODO: Check, if "NFKC" fixes the issues with "LATIN SMALL LETTER DOTLESS I"
    #       above.
    return unicodedata.normalize("NFC", "".join([chr(i) for i in codepoints]))
 
def split_into_paragraphs(text):
    """ Splits the given text into paragraphs. Returns a list of list of words, 
    where each inner list represents a paragraph with its words. Each word is
    represented by a tuple (<word>, <word_with_ws>), where 'word' represents 
    the stripped word and 'word_with_ws' represents the word *with* trailing 
    whitespaces. 
    
    >>> split_into_paragraphs("Foo Bar")
    [[('Foo', 'Foo '), ('Bar', 'Bar ')]]
    >>> split_into_paragraphs("foo \\n\\n bar")
    [[('foo', 'foo \\n\\n ')], [('bar', 'bar ')]]
    """
    
    # The list of paragraphs.
    paragraphs = []
    # The words of the current paragraph.
    words_per_paragraph = []
    
    # Split the text on whitespaces and keep the whitespaces: 
    # ["The", " ", "quick,", " ", "big", " ", "fox", ...]
    words_and_ws = re.split(r'(\s+)', text)
    
    # We want to merge each word with its trailing whitepaces. So make sure 
    # that the list contains an even number of elements. 
    # (In other words: Make sure that the last word ends with an whitespace)
    if len(words_and_ws) % 2 != 0:
        words_and_ws.append(" ")
    
    # Iterate through the elements with even indices (that are the words).
    for i in range(0, len(words_and_ws), 2):
        # Obtain the word.
        word = words_and_ws[i]
        # Obtain the related trailing whitespaces (that is the next element).
        ws = words_and_ws[i + 1]
                
        # Ignore empty words.
        if len(word) == 0:
            continue
        
        # NEW(2016-12-13): word now may contain (line-number, column-number)
        # pair defining the line number and the column number of the word in
        # the tex file. Parse this pair.
        # Format is <word>(linenumber,columnnumber)
        line_num = -1
        column_num = -1
        start_index = word.rfind("(")
        if start_index > 0:
            end_index = word.find(")", start_index)
            if end_index > 0:
                line_column = word[start_index + 1 : end_index]
                # line_column is now "linenumber,columnnumber"
                comma_index = line_column.find(",")
                if comma_index > 0:
                    # There is a pair (line_num, column_num) given. Parse it.
                    line_num_str = line_column[ : comma_index]
                    column_num_str = line_column[comma_index + 1 : ]
                    line_num = str_to_int(line_num_str)
                    column_num = str_to_int(column_num_str)
                    # Crop the word by the given pair.
                    word = word [ : start_index]
        
        # Append tuple consisting of (1) the single word and (2) the word with 
        # trailing whitespaces.
        words_per_paragraph.append((word, word + ws, line_num, column_num))
                        
        # Check if the trailing whitespaces introduces a new paragraph.
        # If so, introduce a new paragraph (a new list of words).
        if re.search("\n\s*\n", ws) is not None:
            if len(words_per_paragraph) > 0:
                paragraphs.append(words_per_paragraph)
                words_per_paragraph = []
    
    # Dont't forget the remaining word tuples.                
    if len(words_per_paragraph) > 0:
        paragraphs.append(words_per_paragraph)   
                
    return paragraphs  
    
def normalize_words(word_tuples, to_lower=True, excludes=[]):
    """ Normalizes all '<word>' parts of given list of word tuples 
    (<word>, <word_with_whitespaces>)). Removes all punctuation marks and 
    special characters. If a special character is an inner symbol of a word,
    the word is splitted into two words ("ice-cold" will be resolved to "ice" 
    and "cold", but not to "icecold"). Transforms each letter to lowercases if
    the flag 'to_lower' is set to True.  
    You can exclude certain words from normalization by defining a list 
    'excludes' of related regular expression that matches the words you wish 
    to exclude.
    Returns list of DocWords, where each DocWord includes the normalized version
    and the unnormalized version of a word. 
    
    >>> normalize_words([('Foo', 'Foo '), ('Bar', 'Bar')])
    [DocWord(foo, Foo ), DocWord(bar, Bar)]
    >>> normalize_words([('ice-cold', 'ice-cold ')])
    [DocWord(ice, ice-), DocWord(cold, cold )]
    >>> normalize_words([('[form]', '[form] ')], True, ["\[form\]"])
    [DocWord([form], [form] )]
    """
    result = []
                        
    for word, word_with_ws, line_num, column_num in word_tuples:
        # Check if we have to exclude the word from formatting. 
        # The word could be nested within a prefix and/or a suffix, that could 
        # contain non-special characters like "foo[formula]bar"; or the prefix/
        # suffix could contain only special characters like "([formula])". 
        # In the first case, we want to separate the prefix "foo" and the suffix 
        # "bar" from [formula] to handle them separately. 
        # In the second case, we do *not* want to separate the prefix/suffix, 
        # but handle the word as a whole.
        prefix, word_to_exclude, suffix = is_exclude_word(word, excludes)
                                        
        if word_to_exclude is not None:
            # There is a word to exclude. 
                        
            # Check if there is a prefix which we have to consider.
            consider_prefix = False
            if prefix is not None and len(prefix) > 0:           
                consider_prefix = not contains_only_special_chars(prefix)
            
            # Check if there is a suffix which we have to consider.
            consider_suffix = False
            if suffix is not None and len(suffix) > 0:           
                consider_suffix = not contains_only_special_chars(suffix)
            
            if consider_prefix:
                # Extract the equivalent prefix from word_with_ws.
                prefix_with_ws = word_with_ws[ : len(prefix)]
                
                if (len(prefix_with_ws.strip()) > 0 and line_num > 0 and column_num > 0):               
                    # Normalize the prefix and append it to result list.
                    # FIXME: Don't hardcode the style.
                    paras = to_normalized_paras("%s(%s,%s)" % (prefix_with_ws, line_num, column_num), to_lower, excludes)
                    # 'paras' is list of lists. We only need the first list.
                    if len(paras) > 0:
                        result.extend(paras[0])
            
            # Given the word to exclude, extract the equivalent from 
            # unnormalized word:
            #
            # "foo[formula]bar__"
            #     ^       ^  
            #     S       E
            #
            # "([formula])__"
            #  ^         ^
            #  S         E
            # 
            # We need to find the start position S end the end position E.
            # S is defined by |prefix|.
            # E is defined by |word_with_ws| - (|suffix| + k) where k is the 
            # length of the trailing whitespaces (the "__" parts).
 
            # Compute the length of prefix and suffix.
            len_prefix = len(prefix) if consider_prefix else 0
            len_suffix = len(suffix) if consider_suffix else 0
            
            # Compute the length of trailing whitepaces.
            len_trailing_ws = len(word_with_ws) - len(word_with_ws.strip())
            
            # Define start and end position of the cut. 
            start = len_prefix
            # If there is no suffix we append the trailing whitespaces to the 
            # cut, otherwise we append it to the suffix.
            if len_prefix == 0:
                end = len(word_with_ws)
            else:
                end = len(word_with_ws) - (len_suffix + len_trailing_ws)
            
            # Extract the word and append new DocWord for the word to exclude.
            cut = word_with_ws[start : end]
            result.append(DocWord(word_to_exclude, cut, line_num, column_num))
          
            if consider_suffix:
                # Extract the equivalent prefix from word_with_ws.
                suffix_with_ws = word_with_ws[end : ]
            
                if (len(suffix_with_ws.strip()) > 0 and line_num > 0 and column_num > 0):
                    # Normalize the suffix and append it to result list.
                    # FIXME: Don't hardcode the style.
                    paras = to_normalized_paras("%s(%s,%s)" % (suffix_with_ws, line_num, column_num), to_lower, excludes)
                    # 'paras' is list of lists. We only need the first list.
                    if len(paras) > 0:
                        result.extend(paras[0])
        else:
            # We don't have to exclude the word from normalization.
            
            # Split words on every *inner* special characters, e.g. split 
            # "ice-cold" into "ice" and "cold", but don't split "sandwich." 
            prev_split = -1          
                          
            # Iterate the *inner* symbols = iterate word[1 : len(word) - 1]
            for i in range(1, len(word) - 1):
                if is_special_character(word, i):
                    # Cut the word.
                    word_fragment = word[prev_split + 1 : i]
                    # Remove all special characters (the word could still 
                    # contain special characters at begin and/or end, because
                    # we iterate only the inner characters.
                    word_fragment = filter_special_chars(word_fragment)
                    # Cut the equivalent from unnormalized word.
                    word_with_ws_fragment = word_with_ws[prev_split + 1 : i + 1]                     
                    if to_lower:
                        # Transform the word to lowercases.
                        word_fragment = word_fragment.lower()
                                
                    if len(word_fragment) > 0:
                        # Append new DocWord to result list.
                        doc_word = DocWord(word_fragment, word_with_ws_fragment,
                            line_num, column_num)
                        result.append(doc_word)
                            
                    prev_split = i
             
            # Don't forget the rest of the word.
            #
            # Cut the word.
            word_fragment = word[prev_split + 1 : ]
            # Remove all special characters (the word could still 
            # contain special characters at begin and/or end, because
            # we iterated only the inner characters.
            word_fragment = filter_special_chars(word_fragment)
            # Cut the equivalent from unnormalized word.
            word_with_ws_fragment = word_with_ws[prev_split + 1 : ]                        
            if to_lower:
                # Transform the word to lowercases.
                word_fragment = word_fragment.lower()
           
            # TODO: Use len(word_fragment) > 0 or len(word_with_ws_fragment) > 0?
            # First ignore all words which consists only of special_characters
            # Second takes *all* words into account.         
            if len(word_fragment) > 0:
                # Append new DocWord to result list.
                result.append(DocWord(word_fragment, word_with_ws_fragment,
                    line_num, column_num))                                    
    return result

def is_exclude_word(word, excludes=[]):
    """ Checks if the given word matches any of the patterns given in 
    'excludes' (if we have to exlude the word from normalization). Returns 
    tuple (prefix, word_to_exclude, suffix) where 'word_to_exclude' is the word 
    to exclude (is None if the word doesn't match any patterns. If there is a 
    prefix in front of the word to exclude it is given by 'prefix'. If there is 
    a suffix in behind the word, it is given by 'suffix'.
    
    >>> is_exclude_word("foo")
    (None, None, None)
    
    >>> is_exclude_word("[formula]", ["\[formula\]"])
    (None, '[formula]', None)
    
    >>> is_exclude_word("foo[formula]bar", ["\[formula\]"])
    ('foo', '[formula]', 'bar')
    """
 
    if len(excludes) == 0:
        return None, None, None
 
    # Compose the regular expression 
    # (= all patterns given by 'excludes' joined with "OR") 
    exclude_pattern = re.compile("|".join(excludes))
 
    # Check, if the word matches the given pattern.
    exclude_match = exclude_pattern.search(word)
    
    if not exclude_match:
        return None, None, None
              
    match_start = exclude_match.start()
    match_end = exclude_match.end()
        
    # Check for prefix and suffix.
    prefix = None
    suffix = None
        
    if match_start > 0:
        # There is a prefix. Extract it.
        prefix = word[ : match_start]
            
    # Extract the word to exclude.
    word_to_exclude = word[match_start : match_end]
                        
    if match_end < len(word):
        # There is suffix. Extract it.
        suffix = word[match_end : ]
                
    return prefix, word_to_exclude, suffix

def contains_only_special_chars(text):
    """ Returns True, if the given text consists only of special characters,
    False otherwise.""" 
           
    if len(text) == 0:
        return False
            
    for i in range(0, len(text)):
        if not is_special_character(text, i):
            return False
            
    return True 
    
def is_special_character(text, i): 
    """ 
    Returns True if the i-th character in the given text is a special character,
    False otherwise. The character is *not* a special character if it is a dot
    and it is surrounded by two digits, like in "1.23".
    
    >>> is_special_character(["a", "-", "b"], 0)
    False
    >>> is_special_character(["a", "-", "b"], 1)
    True
    >>> is_special_character(["1", ".", "2"], 1)
    False
    """
    
    if text is None or len(text) == 0:
        return False
    
    if i < 0:
        return False
    
    if i >= len(text):
        return False
    
    char = text[i]
    
    # The character is *not* a special character if it doesn't match the defined
    # pattern.         
    if not SPECIAL_CHARS_PATTERN.match(char):
        return False 
    
    # The character is *not* a special character if it is a dot and it is 
    # surrounded by two digits.
    if char == "." and i > 0 and i < len(text) - 1:
        prev_char = text[i - 1]
        next_char = text[i + 1]
        if prev_char.isdigit() and next_char.isdigit():
            return False
    
    return True

def filter_special_chars(text):
    """ Replaces all special characters from the given text by empty string."""
    chars = [x for i, x in enumerate(text) if not is_special_character(text, i)]
    return "".join(chars)
    
def str_to_int(s, default=-1):
    try:
        return int(s)
    except ValueError:
        return default

# ==============================================================================
# Some helper classes.

class DocWord:
    """ A word represented by the normalized and the unnormalized version of 
    the word. """
    
    def __init__(self, normalized, unnormalized, line_num, column_num):
        self.normalized   = normalized
        self.unnormalized = unnormalized
        self.line_num     = line_num
        self.column_num   = column_num
        
    def __str__(self):
        return self.normalized
        
    def __repr__(self):
        return "DocWord(%s, %s, %s, %s)" % (self.normalized, self.unnormalized,
            self.line_num, self.column_num)
        
        
if __name__ == "__main__":
    print(doc_diff("Reducing(5,16) quasi-ergodicity(5,33)", "XXX"))
