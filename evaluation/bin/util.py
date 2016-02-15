import string
import subprocess
import math
import sys
import difflib
import unicodedata
import re
import util

from collections import defaultdict
from os.path import isfile
from recordclass import recordclass
from lis2 import longest_increasing_subsequence
from lis2 import longest_increasing_continuous_subsequence
from lis2 import increasing_continuous_subsequences
from lis2 import longest_increasing_continuous_subsequences_with_placeholders
from get_close_matches import get_matches

# ______________________________________________________________________________
#
      
def visualize_wdiff_result(path, commons, inserts, deletions):
    complete  = [(word_item.pos, word_item.inner_pos, 'c', word_item.word) 
                    for common in commons for word_item in common]
    complete += [(word_item.pos, word_item.inner_pos, 'i', word_item.word) 
                    for insert in inserts for word_item in insert]
    complete += [(word_item.pos, word_item.inner_pos, 'd', word_item.word) 
                    for delete in deletions for word_item in delete]
    complete.sort()
                   
    visualization = open(path, "w")
    visualization_delete_start = "\033[30;41m"
    visualization_delete_end = "\033[0m"
    visualization_insert_start = "\033[30;42m"
    visualization_insert_end = "\033[0m"
        
    for item in complete:
        if item[2] == 'i':
            visualization.write(visualization_insert_start)
        elif item[2] == 'd':
            visualization.write(visualization_delete_start)
        visualization.write("%s(%d)" % (item[3], item[0]))
        if item[2] == 'i':
            visualization.write(visualization_insert_end)
        elif item[2] == 'd':
            visualization.write(visualization_delete_end)
        visualization.write(" ")
    visualization.write("\n")
    visualization.close()
    
 # ______________________________________________________________________________
#

def format_string(txt, normalize=True, ignore_cases=True):
    ''' 
    Formats the given string. Removes all punctuation marks if the normalize 
    flag is True. Transforms the string to lowercase letters if the ignore_cases
    flag is True.
    '''
                       
    # Unicode can hold "decomposed" characters, i.e. characters with accents 
    # where the accents are characters on its own (for example, the character 
    # "ae" are then two characters: "a" and the two dots.
    # Try to compose these characters using unicodedata.  
    codepoints = [ord(i) for i in txt]
             
    # Unicodedata has issues to compose "LATIN SMALL LETTER DOTLESS I" / 
    # "LATIN SMALL LETTER DOTLESS J" with accents. So replace them by
    # "i" resp. "j".       
    
    # Map "LATIN SMALL LETTER DOTLESS I" to "LATIN SMALL LETTER I"
    # Map "LATIN SMALL LETTER DOTLESS J" to "LATIN SMALL LETTER J"        
    mappings = { 0x0131: 0x0069, 0x0237: 0x0061 } 
    
    for i, codepoint in enumerate(codepoints):
        if codepoint in mappings:
            codepoints[i] = mappings[codepoint]                
    txt = unicodedata.normalize("NFC", "".join([chr(i) for i in codepoints])) 
                        
    if normalize:
        # Remove all punctuations marks.
        txt = txt.translate({ord(c): " " for c in "!,.:;?“”\"'’"})                
    if ignore_cases:
        # transform the string to lowercase letters.
        txt = txt.lower()
    return " ".join(txt.split())
               
def bash_command(cmd_args):
    ''' Runs the given list of arguments in a bash shell. '''
    cmd = " ".join(cmd_args) 
    return subprocess.Popen(['/bin/bash', '-c', cmd], stdout=subprocess.PIPE)
