import unicodedata
import re
import string

def update_file_extension(path, new_file_extension):
    ''' Returns the given path where the actual file extension is replaced by 
    the given new file extension.''' 
    
    # Find the last dot in the path.
    index_last_dot = path.rfind('.')
    
    if index_last_dot < 0:
        basename = path
    else:
        basename = path[ : index_last_dot]
    
    return basename + new_file_extension    

def to_str(arg, default):
    ''' Parses the given arg as string. If parsing fails, returns the given 
    default value. '''
    try: 
        return str(arg)
    except:
        return default

def to_float(arg, default):
    ''' Parses the given arg as int. If parsing fails, returns the given 
    default value. '''
    try: 
        return float(arg)
    except:
        return default

def to_int(arg, default):
    ''' Parses the given arg as int. If parsing fails, returns the given 
    default value. '''
    try: 
        return int(arg)
    except:
        return default

def to_bool(arg, default):
    ''' Parses the given arg as bool. If parsing fails, returns the given 
    default value. '''
    arg_int = to_int(arg, None)
    if arg_int is not None:
        return arg_int != 0
    else:
        arg_str = to_str(arg, None)
        if arg_str is not None:
            return arg_str.lower() != "false" 
        else:
            return default
        
def to_list(arg, default, separator=" "):
    ''' Splits the given arg using the given separator as delimiter and wraps 
    all elements in a list. '''
    arg_str = to_str(arg, None)
    if arg_str:
        return arg_str.split(sep=separator)
    else:
        return default

def ignore_phrase(phrase, junk=[]):
    if phrase:
        for target_word in phrase.words_target:           
            if ignore_word(target_word, junk):
                return True

    return False

def ignore_word(word, junk=[]):
    if word:
        if any(re.search(regex, word.text) for regex in junk):
            return True

    return False

def split_into_paragraphs(text):
    return re.split("\n\s*\n", text)

def to_formatted_paragraphs2(text, to_lowercases=True, to_protect=[]):
    words_by_paragraph = []
    paragraphs = split_into_paragraphs(text)
    for paragraph in paragraphs:
        words = to_formatted_words(paragraph, to_lowercases, to_protect)
        words_by_paragraph.append(words)
    return words_by_paragraph

def to_formatted_words(text, to_lowercases=True, to_protect=[]):
    ''' 
    Transforms the given string to list of (formatted) words. Removes all 
    punctuation marks. Transforms all letters to lowercases if the 
    to_lowercases flag is set to True. 
    Single phrases can be protected by defining regular expressions in the 
    'to_protect' list. All phrases which match at least one of the regular 
    expressions won't be formatted.
    '''
   
    # Make sure, that the given element is indeed a string.
    text = str(text)
    # Define the characters to ignore: all non-alphanumeric characters and all
    # greek characters because the may decoded in different ways. 
    # For example, ∆ (code 8710) != Δ (code 916)
    CHARS_TO_IGNORE_PATTERN = re.compile("[\W]")
                       
    # Unicode can hold "decomposed" characters, i.e. characters with accents 
    # where the accents are characters on its own (for example, the character 
    # "ä" could be actually two characters: "a" and the two dots.
    # Try to compose these characters to a single one using unicodedata.  
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
            
    # Normalize (compose the characters). NFC = Normal form C(omposition)
    text = unicodedata.normalize("NFC", "".join([chr(i) for i in codepoints])) 
    
    words = text.split()
    
    # Split the string on whitespaces, but keep the whitespace to reproduce
    # the original scheme of text.

    # Returns list ["word1", " " , "word2", "  ", ...]
    #words_and_ws = re.split(r'( \t)', text)
    # Make sure that the list contains a even number of elements. 
    #if len(words_and_ws) % 2 != 0:
    #    words_and_ws.append("")
        
    # Join each pair of the list.
    #words = [i+j for i,j in zip(words_and_ws[::2], words_and_ws[1::2])]
    #words = words_and_ws[::2]
    
    # Filter all empty words
    #words = [word for word in words if len(word.strip()) > 0]
    
    # Compose a single regular expression from the given expression.
    to_protect_regex = "|".join(to_protect)
    
    result = []
    for i in range(0, len(words)):
        word = words[i]
                
        # Check, if we have to protect the word from formatting.
        match = re.search(to_protect_regex, word)
        
        if to_protect_regex and match:            
            # Protect the word (don't format it)
            start = match.start()
            end   = match.end()
            if start > 0:
                # There is some preceding string. Format it.
                prefix = word[:start]
                result.extend(to_formatted_words(prefix, to_lowercases, to_protect))
            # Append the word to protect as it is.
            result.append(word[start : end])
            if end < len(word) - 1:
                # There is some succeeding string. Format it.
                suffix = word[end:]
                result.extend(to_formatted_words(suffix, to_lowercases, to_protect))
        else:
            # We don't have to protect the word from formatting. Let's go.
            
            # Keep track of the original and normalized (sub-) words.
            sub_words = []
            norm_sub_words = []    
            
            prev_punct_index = 0           
            word_chars = list(word)
            # Iterate through the characters to detect non-characters and to 
            # split the word at these positions.
            for index in range(0, len(word_chars)):
                char = word_chars[index]
                if CHARS_TO_IGNORE_PATTERN.match(char):
                    # Don't split the word if the special char lies between
                    # two digits (i.e. keep things like '1.23')
                    if char == "." and index > 0 and index < len(word_chars) - 1:
                        prev_char = word_chars[index - 1]
                        next_char = word_chars[index + 1]
                        if prev_char.isdigit() and next_char.isdigit():
                            continue

                    sub_word = word[prev_punct_index : index + 1]
                    norm_sub_word = "".join([x for x in list(sub_word) if not CHARS_TO_IGNORE_PATTERN.match(x)])
                    if norm_sub_word:
                        sub_words.append(sub_word)                        
                        norm_sub_words.append(norm_sub_word)    
                        prev_punct_index = index + 1
           
            # Don't forget the rest of string.
            sub_word = word[prev_punct_index : len(word_chars)]
            norm_sub_word = "".join([x for x in list(sub_word) if not CHARS_TO_IGNORE_PATTERN.match(x)])
            
            if norm_sub_word:
                sub_words.append(sub_word)                        
                norm_sub_words.append(norm_sub_word)    
                            
            if to_lowercases:
                # Transform all words to lowercases.                
                norm_sub_words = [x.lower() for x in norm_sub_words]

            # Append a whitespace to last word.
            if sub_words:
                sub_words[-1] = sub_words[-1] + " "

            result.extend(list(zip(norm_sub_words, sub_words)))  
                      
    return result  
    
# ------------------------------------------------------------------------------
# Format methods.

SPECIAL_CHARS_PATTERN = re.compile("[^\w\s]")
    
def to_formatted_paragraphs(text, to_lower=True, excludes=[]):
    """ Splits the text into paragraphs and formats the words of each paragraph.
    Removes all non-alphanumeric characters and transforms all letters to 
    lowercases if the to_lower flag is True.
    Certain words can be excluded from formatting by defining according regular 
    expressions that matches the words to exclude.
    Returns a list of list of word tuples, each tuple consisting of the 
    formatted word and the original word (including trailing whitespaces).
         
    >>> to_formatted_paragraphs("Foo Bar")
    [[('foo', 'Foo '), ('bar', 'Bar')]]
    >>> to_formatted_paragraphs("Foo \\n\\n Bar")
    [[('foo', 'Foo \\n\\n ')], [('bar', 'Bar')]]
    >>> to_formatted_paragraphs("Foo [formula]")
    [[('foo', 'Foo '), ('formula', 'formula')]]
    >>> to_formatted_paragraphs("Foo-bar-baz", excludes=["\[formula\]"])
    [[('foo', 'Foo'), ('bar', 'bar'), ('baz', 'baz')]]
    >>> to_formatted_paragraphs("Foo [formula]", excludes=["\[formula\]"])
    [[('foo', 'Foo '), ('[formula]', '[formula]')]]
    """
       
    # Compose the text (merge decomposed characters).
    text = compose(text)
          
    # Obtain the words per paragraphs:
    words_per_para = get_words_per_paragraph(text)
               
    # Format the word of each paragraph.
    res = []
    for words in words_per_para:
        res.append(_to_formatted_paragraph(words, to_lower, excludes))
    
    return res
    
def _to_formatted_paragraph(para_words, to_lower=True, excludes=[]):
    """ Formats the given list of words. Splits the words at each special 
    character (and removes the special characters). 
    
    >>> _to_formatted_paragraph([('Foo', 'Foo '), ('Bar', 'Bar')])
    [('foo', 'Foo '), ('bar', 'Bar')]
    >>> _to_formatted_paragraph([('[form]', '[form]')], True, ["\[form\]"])
    [('[form]', '[form]')]
    """
    result = []
        
    for word, word_with_ws in para_words:         
        # Check if we have to exclude the word from formatting. The word may be 
        # nested within a prefix and/or a suffix, like "foo[formula]bar", with
        # prefix "foo", the word "[formula]" to exclude and suffix "bar". 
        prefix, exclude, suffix = _check_exclude_word(word, excludes)
                  
        if exclude:
            # We have to exclude the word. Check if there is a prefix
            if prefix is not None and len(prefix) > 0:
                # Format the prefix. to_formatted_paragraphs returns list of 
                # list. We only need the first element.
                paras = to_formatted_paragraphs(prefix, to_lower, excludes)
                if len(paras) > 0:
                    result.extend(paras[0])
            
            # Given the word to exclude, cut the related part from word_with_ws
            # "foo[formula]bar" -> "[formula]".
            len_prefix = len(prefix) if prefix is not None else 0
            len_suffix = len(suffix) if suffix is not None else 0
            start = len_prefix
            end = len(word_with_ws) - len_suffix
            result.append((exclude, word_with_ws[start : end]))
            
            if len(suffix) > 0:
                # Format the suffix. to_formatted_paragraphs returns list of 
                # list. We only need the first element.
                paras = to_formatted_paragraphs(suffix, to_lower, excludes)
                if len(paras) > 0:
                    result.extend(paras[0])
        else:
            # We don't have to exclude the word from formatting. Let's go.
            # Words may consists special characters, like "ice-cold". 
            # Split them on special characters into "ice" and "cold".
            prev_split = -1          
                          
            word_chars = list(word)
            # Iterate through the characters to detect characters to exclude.
            for i in range(0, len(word_chars)):
                if _check_exclude_char(word_chars, i):
                    word_fragment = word[prev_split + 1 : i]
                    # Cut the word_with_ws *with* special char (i + 1)                     
                    word_with_ws_fragment = word_with_ws[prev_split + 1 : i + 1]
                                                  
                    if to_lower:
                        word_fragment = word_fragment.lower()
                                
                    if len(word_fragment) > 0 or len(word_with_ws_fragment) > 0:
                        result.append((word_fragment, word_with_ws_fragment))
                            
                    prev_split = i
             
            # Don't forget the rest of the word.  
            word_fragment = word[prev_split + 1 : ]
            word_with_ws_fragment = word_with_ws[prev_split + 1 : ]
            
            if to_lower:
                word_fragment = word_fragment.lower()
                    
            if len(word_fragment) > 0 or len(word_with_ws_fragment) > 0:
                result.append((word_fragment, word_with_ws_fragment)) 
                                                                
    return result
    
# ------------------------------------------------------------------------------
       
def compose(text):
    """ Composes the characters of given text. Unicode can hold "decomposed" 
    characters, e.g. characters with accents where the accent is a character on 
    its own (for example, the character "ä" could be actually two characters: 
    "a" and the two dots. Tries to compose these characters to a single one 
    using unicodedata."""

    # Make sure, that the given text is indeed a string.
    text = str(text)
        
    # Obtain the list of unicode code points from text.
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
            
    # Normalize (= compose the characters). NFC = Normal form C(omposition)
    return unicodedata.normalize("NFC", "".join([chr(i) for i in codepoints]))
  
# ------------------------------------------------------------------------------
    
def get_words_per_paragraph(text, keep_whitespaces=True):
    """ Returns a list of list of words, where each inner list represents the
    list of words of a paragraph. If keep_original is True, the word is a tuple 
    (<word>, <word_with_ws>), where 'word' represents the stripped word and 
    'word_with_ws' represents the word *with* trailing whitespaces. 
    
    >>> get_words_per_paragraph("Foo Bar")
    [[('Foo', 'Foo '), ('Bar', 'Bar')]]
    >>> get_words_per_paragraph("foo \\n\\n bar")
    [[('foo', 'foo \\n\\n ')], [('bar', 'bar')]]
    """
    
    paragraphs = []
    words_per_paragraph = []
    
    # Split the text on whitespaces, but keep track of the whitespace: 
    # ["word1", " " , "word2", "  ", ...]
    words_and_ws = re.split(r'(\s+)', text)
    
    # Make sure that the list contains an even number of elements. 
    # (or: Make sure that last word ends with an whitespace)
    if len(words_and_ws) % 2 != 0:
        words_and_ws.append(" ")
    
    # Iterate through each second element of list (that are the words)
    for i in range(0, len(words_and_ws), 2):
        word = words_and_ws[i]
        # Obtain the whitespace string (the following element)
        ws = words_and_ws[i + 1]
        
        if len(word) > 0:
            # Merge the word and whitespace string.
            merged = word + ws
            
            if keep_whitespaces:
                words_per_paragraph.append((word, merged))
            else:
                words_per_paragraph.append(word)
                        
            # Check if the word introduces a new paragraph
            if re.search("\n\s*\n", ws) is not None:
                if len(words_per_paragraph) > 0:
                    paragraphs.append(words_per_paragraph)
                    words_per_paragraph = []
    
    # Dont't forget the remaining words.                
    if len(words_per_paragraph) > 0:
        paragraphs.append(words_per_paragraph)   
                
    return paragraphs    
 
# ------------------------------------------------------------------------------
    
def _check_exclude_word(word, excludes=[]):
    """ Checks if we have to exclude the given word from formatting. Returns 
    tuple (prefix, word, suffix) where 'word' is the word to exclude (is empty 
    if we don't have to exclude the word from formatting. prefix is the leading
    part of the word to exclude (empty if there is no such prefix), suffix is 
    the trailing part of the word to exclude (empty if there is no such suffix).
    
    >>> _check_exclude_word("foo")
    ('', '', '')
    
    >>> _check_exclude_word("[formula]", ["\[formula\]"])
    ('', '[formula]', '')
    
    >>> _check_exclude_word("foo[formula]bar", ["\[formula\]"])
    ('foo', '[formula]', 'bar')
    
    """
 
    if len(excludes) > 0:
        exclude_pattern = re.compile("|".join(excludes))
 
        # Check, if we have to protect the word from formatting.
        exclude = exclude_pattern.search(word)
                
        if exclude:          
            # Exclude the word (don't format it)
            
            # Check for leading and trailing strings.
            prefix, suffix = "", ""
            start, end = exclude.start(), exclude.end()
            if start > 0:
               # There is some prefix. Format it.
               prefix = word[ : start]
            
            # Append the word to protect as it is.
            exclude_word = word[start : end]
            
            if end < len(word) - 1:
                # There is some succeeding string. Format it.
                suffix = word[end : ]
                
            return prefix, exclude_word, suffix
            
    return "", "", ""

def _check_exclude_char(word_chars, i): 
    """ Returns True if we have to exclude the i-th character in the given 
    array of characters, False otherwise.
    
    >>> _check_exclude_char(["a", "-", "b"], 0)
    False
    >>> _check_exclude_char(["a", "-", "b"], 1)
    True
    >>> _check_exclude_char(["1", ".", "2"], 1)
    False
    """
    
    if word_chars is None:
        return False
    
    if i < 0:
        return False
    
    if i > len(word_chars) - 1:
        return False
    
    char = word_chars[i]
             
    if not SPECIAL_CHARS_PATTERN.match(char):
        return False 
    
    # Don't split the word if the special char lies between
    # two digits (i.e. keep things like '1.23')
    if char == "." and i > 0 and i < len(word_chars) - 1:
        prev_char = word_chars[i - 1]
        next_char = word_chars[i + 1]
        if prev_char.isdigit() and next_char.isdigit():
            return False
    
    return True
