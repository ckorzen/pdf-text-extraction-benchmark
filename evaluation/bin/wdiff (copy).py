import string
import subprocess
import math
import sys
import difflib
import unicodedata
import re
import os

from collections import defaultdict
from queue import PriorityQueue
from os.path import isfile
from recordclass import recordclass
from lis import longest_increasing_subsequence
from lis import longest_increasing_continuous_subsequence
from lis import increasing_continuous_subsequences
from lis import increasing_continuous_subsequences_with_placeholders
from get_close_matches import get_matches

# Define some record classes for more human-readable tuples.
CommonWordItem  = recordclass('CommonWordItem', 'pos inner_pos word')
DeleteWordItem  = recordclass('DeleteWordItem', 'pos inner_pos word')
InsertWordItem  = recordclass('InsertWordItem', 'pos inner_pos word')
GroundtruthItem = recordclass('GroundtruthItem', 'pos word_item x')
MappingItem     = recordclass('MappingItem','positions insertion')
RunItem         = recordclass('RunItem', 'deletion insertion')

# ______________________________________________________________________________
# Wdiff.

def wdiff(in1, in2, normalize=True, ignore_cases=True, rearrange=False, 
        max_distance=0, junk=[]):
    ''' Does a wdiff on the given two input strings. Normalizes the inputs 
    (=removes punctuation marks) if the normalize flag is True. Transforms all 
    letters to lowercase letters if the ignore_cases flag is True. Tries to 
    rearrange words in in2 to establish the same order as in in1, if the 
    rearrange flag is True. Considers words with distance 1 as equal, if the 
    max_distance is set to 1. Ignores the given junk (given as list of regular
    expressions). '''
        
    if rearrange:
        wdiff = rearrange_and_wdiff
    else:
        wdiff = plain_wdiff
    
    return wdiff(in1, in2, normalize, ignore_cases, max_distance, junk)
# ______________________________________________________________________________
# Plain wdiff.

def plain_wdiff(in1, in2, normalize=True, ignore_cases=True, max_distance=0,
                junk=[]):
    ''' Does a plain wdiff on the given two input strings. Calls the tool 
    wdiff on command line and parses its output. Transforms all letters to 
    lowercase letters if the ignore_cases flag is True. Normalizes the inputs 
    (=removes punctuation marks) if the normalize flag is True. Considers words 
    with distance 1 as equal, if the max_distance is set to 1. Ignores the given
    junk (given as list of regular expressions) '''
    
    # Run wdiff on command line.
    wdiff_result = cmd_wdiff(in1, in2, normalize, ignore_cases)
    
    # Parse the output.    
    commons, inserts, deletions = [], [], []
    
    if wdiff_result:  
        ignore_line = False
        pos = 0         
        
        # Parse line by line.        
        for line in wdiff_result: 
            line = line.decode("utf-8").strip()
                                            
            # Skip empty lines.
            if not line:
                continue
                                    
            # Handle an insertion.
            if line.startswith(start_insert):
                if ignore_line:
                    continue
                                     
                words = line[len(start_insert):].split()
                word_items = []
                for w in words:   
                    word_items.append(InsertWordItem(pos, len(word_items), w))
                    pos += 1
                inserts.append(word_items)
            # Handle a deletion.
            elif line.startswith(start_delete):
                line = line[len(start_delete):]
                
                # Check, if we have to ignore this deletion.
                ignore_line = any(re.search(regex, line) for regex in junk)
                if ignore_line:
                    continue
            
                words = line.split()
                word_items = []
                for w in words:
                    word_items.append(DeleteWordItem(pos, len(word_items), w))
                deletions.append(word_items)                
                pos += 1
            # Handle a common.
            else:
                words = line.split()
                word_items = []
                for w in words:
                    word_items.append(CommonWordItem(pos, len(word_items), w))
                    pos += 1
                commons.append(word_items)
            ignore_line = False

    return commons, inserts, deletions

# ______________________________________________________________________________
# Rearrange and wdiff.

def rearrange_and_wdiff(in1, in2, normalize=True, ignore_cases=True, 
                        max_distance=0, junk=[]):
    ''' Does a permutation tolerant wdiff, such that the order of the words in 
    both string doesn't matter. Transforms all letters to 
    lowercase letters if the ignore_cases flag is True. Normalizes the inputs 
    (=removes punctuation marks) if the normalize flag is True. Considers words 
    with distance 1 as equal, if the max_distance is set to 1. Ignores the given
    junk (given as list of regular expressions) '''

    # Do a plain wdiff (diff based on words). Will return a list of common 
    # words, a list of additional words and a list of missing words. 
    # Each list is a list of lists of consecutive WordItems: 
    # [[(pos1, inner_pos1, word1), (pos2, inner_pos2, word2)], [..]].
    # "pos" relates to the position in in1. "inner_pos" describes the 
    # position of the word in the inner list.
    commons, inserts, deletions = plain_wdiff(in1, in2, normalize, ignore_cases)

    visualize_wdiff_result("visualization-raw.txt", commons, inserts, deletions)

    # Create a list of all groundtruth words, that are all common and all 
    # deleted words. 
    groundtruth  = [word for common in commons for word in common]
    groundtruth += [word for delete in deletions for word in delete] 
    groundtruth.sort()
    
    # Create an index of all groundtruth words, where each word is mapped to 
    # its position in the groundtruth:
    # { word1: [(pos1, word_item1), (pos2, word_item2)], ... }
    groundtruth_index = defaultdict(lambda: [])  
    for i, word_item in enumerate(groundtruth):
        item = GroundtruthItem(i, word_item, 1)
        groundtruth_index[word_item.word].append(item)
        
    # Map each inserted word to its positions in the groundtruth:
    # [[(positions1, word1, index1), (positions2, word2, index2), ...], ...]
    insertions_word_mappings = [0] * len(inserts)
    for i, insertion in enumerate(inserts):
        mapping = [0] * len(insertion)
        last_mapping_item = None
        for j, word_item in enumerate(insertion):
            # ***
            # EXPERIMENTAL: If tolerance == 1, also take whitespaces into 
            # account. For example, for the words "foobar" and "foo bar" the 
            # distance is 1.
            # if tolerance == 1:
            #    # "If there is a previous word which has no positions..."
            #    if last_resolution_item and not last_resolution_item.positions_in_str:
            #        # "... check if there are positions for the concatenation of the 
            #        # previous word and the current word."
            #        merged = last_resolution_item.insertion_word.word + word_item.word
            #        positions = get_positions(merged, missings_str_index, 0)
            #        if positions:
            #            last_resolution_item.positions_in_str = positions
            #            last_resolution_item.insertion_word.word = merged
            #            # Erase the current word such that it has no effect anymore.
            #            word_item.word = ""
            #            continue
            # ***
            
            word = word_item.word
            positions = get_positions(word, groundtruth_index, max_distance)
                                                                     
            last_mapping_item = MappingItem(positions, word_item)
            mapping[j] = last_mapping_item                   
        insertions_word_mappings[i] = mapping  
                   
    # For each mapping, compute the longest "run", that is a sequence of words 
    # which occurs in the insertion in the same order as in the groundtruth.
    runs_queue = PriorityQueue()
    for i, mapping in enumerate(insertions_word_mappings):             
        run = find_run(mapping)         
                        
        if run:
            runs_queue.put((-len(run), run, i))
                                   
    # Process the runs ordered by their priority.
    groundtruth_replacements = [0] * len(groundtruth)
    while not runs_queue.empty():
        # Get the run with highest priority.
        priority, longest_run, mapping_index = runs_queue.get() 
                                                                    
        for run_element in longest_run:
            deletion, insertion = run_element
                                                                        
            # Allocate the word to the related position in the missing string
            # if there is no such word yet.
            if not groundtruth_replacements[deletion.pos]:
                groundtruth_replacements[deletion.pos] = insertion
                            
            # Disable the word, such that it can't be a member of a run anymore.
            mapping = insertions_word_mappings[mapping_index]
            if (insertion.inner_pos < len(mapping)):
                mapping[insertion.inner_pos] = False 
            
        # Find run in the remaining words and insert it into queue, if there is 
        # any other run.
        run = find_run(mapping)             
        if run:
            runs_queue.put((-len(run), run, mapping_index))
    
    rearranged = []            
    for i, word_item in enumerate(groundtruth):
        if groundtruth_replacements[i]:
            groundtruth_replacements[i].pos = groundtruth[i].pos
            groundtruth_replacements[i].inner_pos = groundtruth[i].inner_pos
            groundtruth_replacements[i].word = groundtruth[i].word
        elif type(groundtruth[i]) == CommonWordItem: 
            rearranged.append(groundtruth[i])
                                          
    rearranged += [item for insert in inserts for item in insert]
    rearranged.sort()
            
    # Join the words.
    in2 = " ".join([item.word for item in rearranged])
                                                                 
    # Run a simple wdiff with the rearranged string. 
    c, i, d = plain_wdiff(in1, in2, normalize, ignore_cases, junk=junk)
    
    visualize_wdiff_result("visualization-rearranged.txt", c, i, d)
    
    return (c, i, d)
    
    
def get_positions(word, positions, max_distance=0):
    result = []
    if word in positions:
        return positions[word]
    elif max_distance > 0:
        known_words = get_matches(word, positions.keys(), allow_distance_one=True)
        for known_word in known_words:
            result += positions[known_word];
        result.sort()
    return result  

def find_run(elements):
    """ 
    Given a list of tuples of form ([positions], word). Returns a longest run,
    i.e. a sequence of increasing positions, where the order of words are kept. 
    
    Example: 
    Given the tuples [([1, 3, 7], "foo"), ([2, 4, 5], "bar"), ([3, 4], "baz")]. 
    A longest run would be [(1, "foo"), (2, "bar"), (3, "baz")].
    """  
        
    # Insert the positions in reverse order into the single list x. This 
    # guarantuees, that no two elements are chosen from a single list. 
    x = []
        
    if elements:
        for element in elements:
            if element:
                positions, item = element
                            
                if positions:
                    for pos in reversed(positions):
                        if pos.x: # Needed to avoid that a MissingItem isn't chosen twice.
                            x.append(RunItem(pos, item))
                else:
                    x.append(RunItem(GroundtruthItem(-1, None, 1), item))
    
    lis2 = increasing_continuous_subsequences_with_placeholders(x)
                
    # sort the runs by number of deletions
    xxx = []
    lis = []
    for li in lis2:
        num_deletions = 0
        for l in li:
            if isinstance(l.deletion.word_item, DeleteWordItem):
                num_deletions += 1
        xxx.append((num_deletions, li))

    xxx.sort(reverse=True)
    if xxx:
        lis = xxx[0][1]
    
    for run_item in lis:
        run_item.deletion.x = 0            
    
    # *****
    revised_run = []
    word_queue = []
    
    for item in lis:
        word_queue.append(item)
        if item.deletion.pos != -1:
            words = []
            for word in word_queue:
                words.append(word.insertion.word)
                word.insertion.word = ""
            item.insertion.word = " ".join(words)
            revised_run.append(item)
            word_queue = []
                           
    if word_queue:
        if revised_run:
            words = []
            words.append(revised_run[-1].insertion.word)
            for word in word_queue:
                words.append(word.insertion.word)
                word.insertion.word = ""
            revised_run[-1].insertion.word = " ".join(words)
        else:
            revised_run = word_queue
    # ****       
            
    return revised_run

# ______________________________________________________________________________
# 
def cmd_wdiff(in1, in2, normalize=True, ignore_cases=True):
    ''' Calls the tool wdiff on the two given inputs. The input may be either 
    strings or paths to text files to compare.'''
    
    # If the inputs are file paths, read the files.
    if isfile(in1) and isfile(in2):
        with open(in1, 'r') as file1:
            in1 = file1.read().replace('\n', ' ')
        with open(in2, 'r') as file2:
            in2 = file2.read().replace('\n', ' ')
    
    in1 = format_string(in1, normalize=normalize, ignore_cases=ignore_cases)
    in2 = format_string(in2, normalize=normalize, ignore_cases=ignore_cases)
       
    in1_filename = "in1.txt"
    in2_filename = "in2.txt"
          
    in1_file = open(in1_filename, "w")
    in1_file.write(in1)
    in1_file.close()      
    
    in2_file = open(in2_filename, "w")
    in2_file.write(in2)
    in2_file.close()
           
    args = ["wdiff"]
    args.append("-w '\n%s'" % start_delete)
    args.append("-y '\n%s'" % start_insert)
    args.append("-x '\n%s'" % end_delete)
    args.append("-z '\n%s'" % end_insert)
    args.append("-n") # do not extend fields through newlines
    # use process substitution because wdiff expects filepaths.
    args.append(in1_filename) 
    args.append(in2_filename)
        
    result = bash_command(args).stdout
    
    #os.remove(in1_filename)
    #os.remove(in2_filename)
    
    return result
   
def bash_command(cmd_args):
    ''' Runs the given cmd in a bash shell. '''
    cmd = " ".join(cmd_args) 
    return subprocess.Popen(['/bin/bash', '-c', cmd], stdout=subprocess.PIPE)

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
    # "ä" are then two characters: "a" and the two dots.
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
     
def visualize_wdiff_result(path, commons, inserts, deletions):
    # ********
    complete = [(word_item.pos, word_item.inner_pos, 'c', word_item.word) 
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
    # ******** 

# ______________________________________________________________________________
  
# the default tag to mark the start of an insertion in wdiff result
start_insert = "[WDIFF-INSERT]"
# the default tag to mark the end of an insertion in wdiff result
end_insert = ""
# the default tag to mark the start of a deletion in wdiff result
start_delete = "[WDIFF-DELETE]"
# the default tag to mark the end of a deletion in wdiff result
end_delete = ""

if __name__ == "__main__":
    rearrange_and_wdiff("The fast fox jumps over the fox", "The jumps over fast fox the fox")
