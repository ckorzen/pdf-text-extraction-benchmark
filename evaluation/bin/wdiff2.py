import string
import subprocess
import math
import sys
import difflib
import unicodedata
import re
import util
import tempfile
import os

from collections import defaultdict
from queue import PriorityQueue
from os.path import isfile
from recordclass import recordclass
from lis2 import longest_increasing_subsequence
from lis2 import longest_increasing_continuous_subsequence
from lis2 import increasing_continuous_subsequences
from lis2 import longest_increasing_continuous_subsequences_with_placeholders
from get_close_matches import get_matches

# Define some record classes for more human-readable tuples.
CommonWordItem  = recordclass('CommonWordItem', 'pos inner_pos word')
DeleteWordItem  = recordclass('DeleteWordItem', 'pos inner_pos word')
InsertWordItem  = recordclass('InsertWordItem', 'pos inner_pos word')
GroundtruthItem = recordclass('GroundtruthItem', 'pos word_item x')
MappingItem     = recordclass('MappingItem','positions insertion')
RunItem         = recordclass('RunItem', 'deletion insertion')

# the default tag to mark the start of an insertion in wdiff result
start_insert = "[WDIFF-INSERT]"
# the default tag to mark the end of an insertion in wdiff result
end_insert = ""
# the default tag to mark the start of a deletion in wdiff result
start_delete = "[WDIFF-DELETE]"
# the default tag to mark the end of a deletion in wdiff result
end_delete = ""

# ______________________________________________________________________________
# Wdiff.

def wdiff(in1, in2, normalize=True, ignore_cases=True, rearrange=False, 
          max_dist=0, junk=[]):
    ''' Does a wdiff (diff based on words) on the given two input strings. 
    Normalizes the inputs (=removes punctuation marks) if the normalize flag is 
    True. Transforms all letters to lowercase letters if the ignore_cases flag 
    is True. Tries to rearrange words in in2 to establish the same order as in 
    in1, if the rearrange flag is True. Considers words with distance 1 as 
    equal, if the max_distance is set to 1. Ignores the given junk (given as 
    list of regular expressions). '''
        
    if rearrange:
        in2 = rearrange_words(in1, in2, normalize, ignore_cases, max_dist, junk)
        
    c, i, d = _wdiff(in1, in2, normalize, ignore_cases, junk)
    
    util.visualize_wdiff_result("visualization.txt", c, i,
    
    return (c, i, d)

def _wdiff(in1, in2, normalize=True, ignore_cases=True, junk=[]):
    ''' Does a plain wdiff on the given two input strings. Calls the tool 
    wdiff on command line and parses its output. Transforms all letters to 
    lowercase letters if the ignore_cases flag is True. Normalizes the inputs 
    (=removes punctuation marks) if the normalize flag is True. Ignores the 
    given junk (given as list of regular expressions) '''
    
    # Run wdiff on command line.
    wdiff_result = bash_wdiff(in1, in2, normalize, ignore_cases)

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
                # Check if we have to ignore this insertion due to a previous
                # deletion to ignore.
                if ignore_line:
                    continue
                  
                # Split the line into words.                   
                words = line[len(start_insert):].split()
                word_items = []
                for w in words:   
                    word_items.append(InsertWordItem(pos, len(word_items), w))
                    pos += 1
                inserts.append(word_items)
            # Handle a deletion.
            elif line.startswith(start_delete):
                line = line[len(start_delete):]
                
                # Check, if the line matches any regular expressions in junk.
                # If so, ignore this deletion and the next insertion.
                ignore_line = any(re.search(regex, line) for regex in junk)
                if ignore_line:
                    continue
            
                # Split the line into words.
                words = line.split()
                word_items = []
                for w in words:
                    word_items.append(DeleteWordItem(pos, len(word_items), w))
                deletions.append(word_items)                
                pos += 1
            # Handle a common.
            else:
                # Split the line into words.
                words = line.split()
                word_items = []
                for w in words:
                    word_items.append(CommonWordItem(pos, len(word_items), w))
                    pos += 1
                commons.append(word_items)
            
            # Revoke any ignores.
            ignore_line = False
    return commons, inserts, deletions

def bash_wdiff(in1, in2, normalize=True, ignore_cases=True):
    ''' Runs wdiff in a bash shell based on the given two inputs, which could be
    either strings or paths. Normalizes both inputs, if the normalize flags is
    set to True. Translates the inputs to lowercase letters, if the ignore_cases
    flag is set to True.'''
    
    # Check if the given inputs are file paths.
    if isfile(in1) and isfile(in2):
        # Read the files.
        with open(in1, 'r') as file1:
            in1 = file1.read().replace('\n', ' ')
        with open(in2, 'r') as file2:
            in2 = file2.read().replace('\n', ' ')

    # Wdiff only allows file paths as input, so write the inputs to files and
    # pass its paths to wdiff. We could use process substitution instead, but 
    # this may result in issues, if the strings are to long.
    def write_to_file(text):
        ''' Writes the given text to a temporary file.'''
        target = tempfile.NamedTemporaryFile(delete=False)
        target.write(bytes(text, 'UTF-8'))
        return target
    
    # Write formatted strings to files.
    target1 = write_to_file(util.format_string(in1, normalize, ignore_cases))
    target2 = write_to_file(util.format_string(in2, normalize, ignore_cases))
        
    # Compose the command to execute.   
    args = ["wdiff"]
    # Define the start marker for a deletion.
    args.append("-w '\n%s'" % start_delete)
    # Define the end marker for a deletion.
    args.append("-x '\n%s'" % end_delete)
    # Define the start marker for an insertion.
    args.append("-y '\n%s'" % start_insert)
    # Define the end marker for an insertion.
    args.append("-z '\n%s'" % end_insert)
    # Do not extend fields through newlines
    args.append("-n")
    
    # Append the paths to files.
    args.append(target1.name) 
    args.append(target2.name)
             
    # Run the command.            
    result = util.bash_command(args)
    
    # TODO: Delete the files!
    
    return result.stdout
   
        
# ______________________________________________________________________________
# Rearrange.

def rearrange_words(in1, in2, normalize=True, ignore_cases=True, max_dist=0,
                    junk=[]):
    ''' Rearranges words in in2 such that the order of the words in in2 is equal  
    to the order of words in in1. Transforms all letters to lowercase letters if
    the ignore_cases flag is True. Normalizes the inputs (=removes punctuation 
    marks) if the normalize flag is True. Considers words with distance 1 as 
    equal, if the max_distance is set to 1. Ignores the given junk (given as 
    list of regular expressions). 
    
    >>> s1 = "The red fox jumps over the hedge"
    >>> s2 = "The hedge jumps over the red fox"
    >>> rearrange_words(s1, s2)
    'the red fox jumps over the hedge'
    '''
                
    # Do a plain wdiff (diff based on words). Will return a list of common 
    # words (word which occur in in1 and in2), a list of inserted words (words 
    # that occur only in in2) and a list of deleted words (words that only occur
    # in in1). Each list is a list of lists of consecutive WordItems: 
    # [[(pos1, inner_pos1, word1), (pos2, inner_pos2, word2)], [..]].
    # "pos" relates to the position in in1. "inner_pos" describes the 
    # position of the word in the inner list.
    commons, inserts, deletes = _wdiff(in1, in2, normalize, ignore_cases)
    
    # Create flattened versions of the word lists.
    flattened_commons = [word for common in commons for word in common]
    flattened_inserts = [word for insert in inserts for word in insert]
    flattened_deletes = [word for delete in deletes for word in delete]
                        
    # Create an index of all missing words, where each word is mapped to its 
    # position in in1: { word1: [(pos1, word_item1), (pos2, word_item2)], ... }
    groundtruth_index = defaultdict(lambda: [])  
    for i, item in enumerate(flattened_deletes):
        groundtruth_index[item.word].append(GroundtruthItem(i, item, 1))
               
    # Map each inserted word to its positions in the groundtruth:
    # [[(positions1, word1, index1), (positions2, word2, index2), ...], ...]       
    actual_mappings = []
    for insert in inserts:
        mapping = []
        for item in insert:
            positions = get_positions(item.word, groundtruth_index, max_dist)
            mapping.append(MappingItem(positions, item))        
        actual_mappings.append(mapping)
        
    # For each mapping, compute the "runs" in it. A run is a sequence of words 
    # which occurs in the insertion in the same order as in the groundtruth.
    # Each run item contains the actual item and the index of the item in the 
    # mapping.
    runs_queue = PriorityQueue()
    for i, mapping in enumerate(actual_mappings):             
        run = find_most_suitable_run(mapping)         
                        
        if run:
            runs_queue.put((-len(run), run, i))

    while not runs_queue.empty():
        # Get the run with highest priority.
        priority, run, mapping_index = runs_queue.get() 
        
        for run_element in run:
            groundtruth_item, actual_item = run_element[0]
            
            # Substitute the groundtruth item by the actual item. 
            if groundtruth_item.word_item:        
                actual_item.pos       = groundtruth_item.word_item.pos
                actual_item.inner_pos = groundtruth_item.word_item.inner_pos
            
        for run_item in run:
            # Unwrap the item and its index in the mapping.
            item, index = run_item
            # Unwrap the groundtruth item and the actual item.
            groundtruth_item, actual_item = item
            
            # Disable the word, such that it can't be a member of a run anymore.
            mapping = actual_mappings[mapping_index]
            if (actual_item.inner_pos < len(mapping)): # TODO: Why we need this condition?
                mapping[actual_item.inner_pos] = False
            
            # Substitute the groundtruth item by the actual item. 
            if groundtruth_item.word_item and groundtruth_item.x == 1:        
                actual_item.pos       = groundtruth_item.word_item.pos
                actual_item.inner_pos = groundtruth_item.word_item.inner_pos
                groundtruth_item.x    = 0
                              
        # Find run in the remaining words and insert it into queue, if there is 
        # any other run.
        run = find_most_suitable_run(mapping)             
        if run:
            runs_queue.put((-len(run), run, mapping_index))
    
    # Combine the common and inserted word items and sort them by positions.
    result = flattened_commons + flattened_inserts
    result.sort()
          
    # Join the result words.
    return " ".join([item.word for item in result])
   
def get_positions(word, positions, max_distance=0):
    ''' Given a words, finds all occurences of this word in the given positions
    index, with respect to the given max. distance. '''
    result = []
    if word in positions:
        return positions[word]
    elif max_distance > 0:
        known_words = get_matches(word, positions.keys(), allow_distance_one=True)
        for known_word in known_words:
            result += positions[known_word];
        result.sort()
     
    # Append a placeholder if the word doesn't occur in the index.   
    if not result:
        result = [GroundtruthItem(-1, None, 1)]
        
    return result  
    
def find_most_suitable_run(elements):    
    if not elements:
        return
              
    # Flatten the elements.
    template = []          
    for i, element in enumerate(elements):
        if not element:
            continue
            
        positions, item = element
        if not positions:
            continue            
           
        # Insert the positions in reverse order into the single list x. This 
        # guarantuees, that no two elements are chosen from a single list.      
        for pos in reversed(positions):
            if pos.x: # Needed to avoid that a MissingItem isn't chosen twice.
                template.append(RunItem(pos, item))
    
    if not template:
        return
       
    run = longest_increasing_continuous_subsequences_with_placeholders(template)
    
    revised_run = []
    word_queue = []
    
    for item in run:
        word_queue.append(item)
        if item[0].deletion.pos != -1:
            words = []
            for word in word_queue:
                words.append(word[0].insertion.word)
                word[0].insertion.word = ""
            item[0].insertion.word = " ".join(words)
            revised_run.append(item)
            word_queue = []
                           
    if word_queue:
        if revised_run:
            words = []
            words.append(revised_run[-1][0].insertion.word)
            for word in word_queue:
                words.append(word[0].insertion.word)
                word[0].insertion.word = ""
            revised_run[-1][0].insertion.word = " ".join(words)
        else:
            revised_run = word_queue
        
    return revised_run


      
if __name__ == "__main__":
    wdiff("The red [\cite] fox jumps over the green dog and laughs", "the green dog and laughs The red [3] fox jumps over", rearrange=True)
