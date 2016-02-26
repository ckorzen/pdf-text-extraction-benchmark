import unicodedata
import re

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

def to_formatted_words(string, to_lowercases=True, 
        ignores=["!", ",", ".", ":", ";", "?", "“", "”", "\"", "'", "’"]):
    ''' 
    Formats the given string to list of words. Transforms all letters to 
    lowercases if the to_lowercases flag is set to True. Removes all occurrences
    of the characters given by ignores.
    '''
    
    # Make sure, that the given element is indeed a string.
    string = str(string)
                       
    # Unicode can hold "decomposed" characters, i.e. characters with accents 
    # where the accents are characters on its own (for example, the character 
    # "ä" could be actually two characters: "a" and the two dots.
    # Try to compose these characters to a single one using unicodedata.  
    codepoints = [ord(i) for i in string]
             
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
    string = unicodedata.normalize("NFC", "".join([chr(i) for i in codepoints])) 
                        
    if ignores:
        ignores = "".join(ignores)
        string = string.translate({ord(c): " " for c in ignores})                
                    
    if to_lowercases:
        # Transform the string to lowercase letters.
        string = string.lower()
    
    return string.split()

def to_formatted_paragraphs(string, to_lowercases=True, remove_whitespaces=True,
        ignores=["!", ",", ".", ":", ";", "?", "“", "”", "\"", "'", "’"]):
    ''' Formats the given string to paragraphs for paragraph evaluation. ''' 
    
    paragraphs = re.split("\n\s*\n", string)
    words = [to_formatted_words(paragraph) for paragraph in paragraphs]
    
    if remove_whitespaces:
        return ["".join(x) for x in words]
    else:
        return [" ".join(x) for x in words]

def compute_precision_recall(diff_result, junk=[]):
    ''' Computes precision and recall from the given diff result. '''
    
    fp = 0
    fn = 0
    for deletes, inserts in diff_result.replaces:
        num_deletes = len(deletes)
        num_inserts = len(inserts)
        
        min_num = min(num_deletes, num_inserts)
        fp += num_inserts
        fn += num_deletes
                                 
    tp = len(diff_result.commons)
                
    precision = tp / (tp + fp) if tp + fp > 0 else 0.0
    recall    = tp / (tp + fn) if tp + fn > 0 else 0.0
                     
    return (precision, recall)

def compute_distance_similarity(diff_result, junk=[]): 
    ''' Computes distance and similarity from the given diff result. '''
       
    distance = 0
    num_deletes = 0
    num_inserts = 0
    num_commons = len(diff_result.commons)
    for replace in diff_result.replaces:
        num_deletes += len(replace.deletes)
        num_inserts += len(replace.inserts)
        distance    += max(len(replace.inserts), len(replace.deletes))
        
    length_gt = num_commons + num_deletes
    length_actual = num_commons + num_inserts
    
    similarity = 1 - (distance / max(length_gt, length_actual))
    
    return (distance, similarity)
