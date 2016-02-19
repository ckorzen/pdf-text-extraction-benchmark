from collections import defaultdict
from recordclass import recordclass
from fuzzy_dict import fuzzy_dict
from queue import PriorityQueue

import unicodedata
import logging

logging.basicConfig(
    level=logging.DEBUG,
    format='%(asctime)s : %(levelname)s : %(module)s : %(message)s',
)
logger = logging.getLogger(__name__)

DiffCommonItem  = recordclass('DiffCommonItem', 'pos1 pos2 element')
DiffDeleteItem  = recordclass('DiffDeleteItem', 'pos1 pos2 element matched')
DiffInsertItem  = recordclass('DiffInsertItem', 'pos1 pos2 element matched')

MappingItem     = recordclass('MappingItem','deletions insertion')
RunItem         = recordclass('RunItem', 'deletion insertion')

# ______________________________________________________________________________
#

def diff(old, new, rearrange=False, ignore_cases=False, 
    ignore_whitespaces=False, translates={}, max_dist=0, min_sim=1.0):
    '''
    Finds the differences between the two given lists of strings. If the 
    rearrange flag is set to true, tries to rearrange the elements in 'new' as 
    much as possible to reproduce the same order of the elements in 'old'. 
    If the ignore_cases flag is set to True, the diff is done case-insensitive. 
    If the ignore_whitespace flag is set to True, all whitespaces will be 
    ignored.
    If there are translations given in the translates dict, they will be applied
    to 'old' and 'new' (e.g. you can specify a dict {".,!?": " "} to translate
    all occurrences of '.', ',', '!' and '?' to the whitespace ' '. If max_dist 
    or min_sim is given, the algorithms considers two elements as equal if the 
    distance between them is at most max_dist (if the similarity) of both 
    elements is at least min_sim).
    Returns a list of common elements, inserted elements (elements that occur
    in 'new' but not in 'old') and deleted elements (elements that occur in
    'old' but not in 'new'). Each common, insertion and deletion is given by
    a tuple (pos1, pos2, element) where 'pos1' denotes the position in 'old', 
    'pos2' denotes the position in 'new' and 'element' denotes the new 
    element.
       
    * Inspired by the simplediff lib by Paul Butler 
    (see https://github.com/paulgb/simplediff/) *  
    '''
    
    logger.debug("Diff. rearrange: %r, ignore_cases: %r, ignore_whitespaces: %r"
        "translates: %r, max_distance: %r, min_similarity: %r" % (rearrange, 
        ignore_cases, ignore_whitespaces, translates, max_dist, min_sim))
    logger.debug(" old: %r" % " ".join(old))
    logger.debug(" new: %r" % " ".join(new))
        
    if rearrange:
        # Try to rearrange the elements in 'new'
        new = rearrange_elements(old, new, ignore_cases=ignore_cases, 
            ignore_whitespaces=ignore_whitespaces, translates=translates, 
            max_dist=max_dist, min_sim=min_sim)
     
    # Do the diff.   
    commons, inserts, deletes = [], [], []
    _diff(old, new, 0, 0, commons, inserts, deletes, ignore_cases=ignore_cases, 
        ignore_whitespaces=ignore_whitespaces, translates=translates, 
        max_dist=max_dist, min_sim=min_sim)
       
    logger.debug(visualize_diff_result(commons, inserts, deletes)) 
                
    return commons, inserts, deletes
   
def _diff(old, new, pos1, pos2, commons, inserts, deletes, ignore_cases=False, 
        ignore_whitespaces=False, translates={}, max_dist=0, min_sim=1.0):
    '''
    Finds the differences between the two given lists of strings recursively. 
    pos1 and pos2 are the current positions in 'old' and 'new'. 
    If the ignore flag is set to True, the diff is done case-insensitive. 
    If the ignore_whitespace flag is set to True, all whitespaces will be 
    ignored.
    If there are translations given in the translates dict, they will be applied
    to 'old' and 'new' (e.g. you can specify a dict {".,!?": " "} to translate
    all occurrences of '.', ',', '!' and '?' to the whitespace ' '.
    If max_dist or min_sim is given, the algorithms considers two elements 
    as equal if the distance between them is at most max_dist (if the similarity
    of both elements is at least min_sim).
    Fills the common elements, inserted elements and deleted elements into the 
    given lists 'commons', 'inserts' and 'deletes'.
    '''

    # Create an index from values in 'old', where each value is mapped to its 
    # position in 'old':
    # { element1: [1, 5, 7], element2: [4, 6, ], ... } 
    old_index = fuzzy_dict()  
    for i, item in enumerate(old):
        string = format_str(item, ignore_cases, ignore_whitespaces, translates)
        if string in old_index:
            old_index[string].append(i)
        else:
            old_index[string] = [i]

    # Find the largest substring common to 'old' and 'new'.
    # 
    # We iterate over each value in 'new'. At each iteration, overlap[i] is the
    # length of the largest suffix of old[:i] equal to a suffix of 
    # new[:index2] (or unset when old[i] != new[index2]).
    #
    # At each stage of iteration, the new overlap (called _overlap until 
    # the original overlap is no longer needed) is built from 'old'.
    #
    # If the length of overlap exceeds the largest substring
    # seen so far (length), we update the largest substring
    # to the overlapping strings.
    overlap = defaultdict(lambda: 0)
    
    # start_old is the index of the beginning of the largest overlapping
    # substring in old. start_new is the index of the beginning of the 
    # same substring in new. length is the length that overlaps in both.
    # These track the largest overlapping substring seen so far, so naturally
    # we start with a 0-length substring.
    start_old = 0
    start_new = 0
    length = 0

    for index2, item in enumerate(new):
        string = format_str(item, ignore_cases, ignore_whitespaces, translates)
        _overlap = defaultdict(lambda: 0)
        # Get the 'closest' match to the string.
        for index1 in old_index.get(string, max_dist, min_sim, []):
            # now we are considering all values of index1 such that
            # old[index1] == new[index2].
            _overlap[index1] = (index1 and overlap[index1 - 1]) + 1
            # Check if this is the largest substring seen so far.
            if _overlap[index1] > length:
                length = _overlap[index1]
                start_old = index1 - length + 1
                start_new = index2 - length + 1
        overlap = _overlap

    if length == 0:
        # No common substring was found. Return an insert and delete...
        for item in old:
            deletes.append(DiffDeleteItem(pos1, pos2, item, False))
            pos1 += 1
        for item in new:
            inserts.append(DiffInsertItem(pos1, pos2, item, False))
            pos2 += 1
    else:
        # A common substring was found. Call diff recursively for the substrings
        # to the left and to the right
        left1 = old[ : start_old]
        left2 = new[ : start_new]
        pos1, pos2 = _diff(left1, left2, pos1, pos2, commons, inserts, deletes,
            ignore_cases=ignore_cases, ignore_whitespaces=ignore_whitespaces, 
            translates=translates, max_dist=max_dist, min_sim=min_sim)
        
        for item in new[start_new : start_new + length]:
            commons.append(DiffCommonItem(pos1, pos2, item))
            pos1 += 1
            pos2 += 1
        
        right1 = old[start_old + length : ]
        right2 = new[start_new + length : ]
        pos1, pos2 = _diff(right1, right2, pos1, pos2, commons, inserts, deletes,
            ignore_cases=ignore_cases, ignore_whitespaces=ignore_whitespaces, 
            translates=translates, max_dist=max_dist, min_sim=min_sim)
        
    return (pos1, pos2)

# ______________________________________________________________________________
# Rearrange

def rearrange_elements(old, new, ignore_cases=False, ignore_whitespaces=False,
        translates={}, max_dist=0, min_sim=1.0):
    ''' Tries to rearrange the elements in 'new' as much as possible to 
    reproduce the same order of the elements in 'old'. If the ignore_cases flag 
    is set to True, the diff is done case-insensitive. If the ignore_whitespace 
    flag is set to True, all whitespaces will be ignored.
    If there are translations given in the translates dict, they will be applied
    to 'old' and 'new' (e.g. you can specify a dict {".,!?": " "} to translate
    all occurrences of '.', ',', '!' and '?' to the whitespace ' '. If max_dist 
    or min_sim is given, the algorithms considers two elements as equal if the 
    distance between them is at most max_dist (if the similarity of both 
    elements is at least min_sim).'''
    
    logger.debug("Rearrange. ignore_cases: %r, ignore_whitespaces: %r, "
        "translates: %r, max_dist: %r, min_sim: %r" % 
        (ignore_cases, ignore_whitespaces, translates, max_dist, min_sim))
        
    # First, do a normal diff without rearranging.
    commons, inserts, deletes = diff(old, new, rearrange=False, 
        ignore_cases=ignore_cases, ignore_whitespaces=ignore_whitespaces, 
        translates=translates, max_dist=max_dist, min_sim=min_sim)
    
    logger.debug("#commons: %d, #inserts: %d, #deletes: %d"
        % (len(commons), len(inserts), len(deletes)))
             
    # Map each deleted element to all deleted items that holds the element.
    # { element1: [item1, item2], element2: [...], ... }
    deletes_index = fuzzy_dict()
    for item in deletes:
        if item.element in deletes_index:
            deletes_index[item.element].append(item)
        else:
            deletes_index[item.element] = [item]
                  
    # Separate the insertions and associate each element of each insertion to 
    # its positions in the deletes_index:
    # [ [(deletions, insertion1a), (deletions, insertion1b), ...],
    #   [(deletions, insertion2a), (deletions, insertion2b), ...] ... ]
    inserts_mappings = []
    insert_mapping = []
    for insert in inserts:
        if insert_mapping and insert_mapping[-1].insertion.pos1 != insert.pos1:
            inserts_mappings.append(insert_mapping)
            insert_mapping = []
    
        deletions = deletes_index.get(insert.element, max_dist, min_sim, [None])
        insert_mapping.append(MappingItem(deletions, insert))
    inserts_mappings.append(insert_mapping)
         
    # For each mapping, compute the longest "run", that is a sequence of words 
    # which occurs in the insertion in the same order as in the groundtruth.
    run = find_longest_run_in_mappings(inserts_mappings)
     
    while run:
        logger.debug("run: %s", visualize_run(run))
    
        # There may be insertions which could not be matched to a deleted 
        # position. Hence we do not know where to insert such insertions.
        # If there is such insertion within a run, concat it with a preceding
        # or succeeding matched insertion.
        
        # The last matched insertion.
        last_matched_insertion = None
        # If there is no matched insertion yet, add a unmatched insertion to
        # this queue.
        unmatched_insertions = []
        
        for matching in run:
            deletion, insertion = matching
            
            if deletion is not None:
                # The insertion has a matched deletion.
                last_matched_insertion = insertion
                
                # First, concat all unmatched insertions (if any) to this 
                # deletion.
                for i, unmatched in enumerate(unmatched_insertions):
                    unmatched.pos1 = deletion.pos1
                    unmatched.pos2 = deletion.pos2 + i
                    unmatched.matched = True

                # Then map the actual insertion to this deletion.
                insertion.pos1 = deletion.pos1
                insertion.pos2 = deletion.pos2 + len(unmatched_insertions)            
                
                unmatched_insertions = []
                deletion.matched = insertion.matched = True
            else:
                # The insertion has no matched deletion.
                if last_matched_insertion:
                    # If there was a matched insertion seen so far, add the 
                    # unmatched insertion to the this matched insertion.
                    insertion.pos1 = last_matched_insertion.pos1
                    insertion.pos2 = last_matched_insertion.pos2 + 1
                    last_matched_insertion = insertion
                else:
                    # Otherwise queue the unmatched insertion.
                    unmatched_insertions.append(insertion)
                insertion.matched = True  

        # Find another run.
        run = find_longest_run_in_mappings(inserts_mappings)
     
    # The rearranged list follows from the commons and the (updated) inserts.       
    rearranged = commons + inserts
    rearranged.sort()
        
    # Remove all the meta stuff.
    rearranged = [item.element for item in rearranged]
        
    logger.debug("rearranged to: %s", " ".join(rearranged))
                  
    return rearranged

def find_longest_run_in_mappings(mappings):
    longest_run = []
    for mapping in mappings:
        run = find_longest_run_in_mapping(mapping)
        if run and len(run) > len(longest_run):
            longest_run = run
    return longest_run

def find_longest_run_in_mapping(mapping): # Find run in a single(!) element.        
    logger.debug("Find run. ")
    for m in mapping:
        logger.debug(" * %r" % m)
    
    if not mapping:
        return []
        
    # The runs found so far.
    active_runs = []
    # The longest run found so far.
    longest_run = []
        
    # The runs by their end elements. For example, if the runs at indices
    # 1, 2, 3 end with '5' and the runs at indices 4 and 5 ends with '7' the 
    # map looks like: { 5: {1, 2, 3}, 7: {4, 5} }
    runs_by_end_elements = defaultdict(lambda: set())
    # The end elements by runs. For the example above, this map looks like:
    # { 1: 5, 2: 5, 3: 5, 4: 7, 5: 7 }
    end_elements_by_runs = defaultdict(lambda: set())
     
    # The queue of unmatched inserts (inserts with no associated deletion).
    unmatched_queue = []
                      
    for item in mapping:           
        deletions, insert = item            

        if insert.matched:
            # If the insert is already matched, the item shouldn't be a member 
            # of a run anymore.
            continue
        
        prev_active_runs = active_runs
        active_runs = []
        
        prev_runs_by_end_elements = runs_by_end_elements
        runs_by_end_elements = defaultdict(lambda: set())
        
        prev_end_elements_by_runs = end_elements_by_runs
        end_elements_by_runs = defaultdict(lambda: set())
         
        # Iterate through the deleted positions. 
        for deletion in deletions:          
            # If the deletion is already matched, the item shouldn't be a member
            # of a run anymore.
            if deletion is not None and deletion.matched:
                continue
            
            # Obtain the psoition of the deletion.
            pos = deletion.pos1 if deletion is not None else None
              
            logger.debug("%r -> pos: %r" % (deletion, pos))
                        
            if pos is not None: # There is a matched deletion.
                # Check, if there are runs with end element 'pos-1'
                if pos - 1 in prev_runs_by_end_elements:
                    # There are runs that end with 'pos-1'. 
                    run_indices = prev_runs_by_end_elements[pos - 1]
                                                                 
                    for run_index in run_indices:
                        # Append the element to the run.
                        run = prev_active_runs[run_index]
                        run.append(RunItem(deletion, insert)) 
                        
                        # Add the run to active runs.
                        active_runs.append(run)
                        new_run_index = len(active_runs) - 1
                        
                        # Check, if the current run is now the longest run. 
                        if len(run) > len(longest_run):
                            longest_run = run
                        
                        # Update the maps
                        runs_by_end_elements[pos].add(new_run_index)
                        end_elements_by_runs[new_run_index].add(pos)
                else:                   
                    # There is no run that end with 'pos-1'.
                    # Create new run.
                    new_run = unmatched_queue + [RunItem(deletion, insert)]
                                        
                    # Append the run to active runs.
                    active_runs.append(new_run)
                    new_run_index = len(active_runs) - 1
                                              
                    # Check, if the new run is the longest run.     
                    if len(new_run) > len(longest_run):
                        longest_run = new_run
                                
                    # Update the maps.
                    runs_by_end_elements[pos].add(new_run_index)
                    end_elements_by_runs[new_run_index].add(pos)
                # Clear the queue.
                unmatched_queue = []
            else: # There is no matched deletion.
                run_item = RunItem(deletion, insert)
                unmatched_queue.append(run_item)
                                
                for j, active_run in enumerate(prev_active_runs):
                    pos = max(prev_end_elements_by_runs[j]) + 1
                        
                    # Append the element to run.
                    active_run.append(run_item)
                    active_runs.append(active_run)
                    new_run_index = len(active_runs) - 1
                      
                    runs_by_end_elements[pos].add(new_run_index)
                    end_elements_by_runs[new_run_index].add(pos)
                                            
                    if len(active_run) > len(longest_run):
                        longest_run = active_run
            
            logger.debug("Active runs:")
            for i, run in enumerate(active_runs):
                debug_run = [item.insertion.element for item in run]
                logger.debug(" %d: %r" % (i, debug_run))
            debug_queue = [item.insertion.element for item in unmatched_queue]
            logger.debug("Queue: %r" % (debug_queue))
                   
    if longest_run:
        logger.debug("Returning run of length %d" % len(longest_run))
        return longest_run
    else:
        logger.debug("Returning queue of length %d" % len(unmatched_queue))
        return unmatched_queue  
                      
# ______________________________________________________________________________
# Util methods.
  
def format_str(element, ignore_cases=False, ignore_whitespaces=False,
        translates={}):
    ''' 
    Formats the given element to string. Transforms all letters to lowercase
    if the ignore_cases flag is set to True. Removes all whitespaces if the 
    ignore_whitespaces flag is set to True. Applies all translations given in 
    translate dict.
    '''
    
    # Make sure, that the element to process is a string.
    string = str(element)
                       
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
    txt = unicodedata.normalize("NFC", "".join([chr(i) for i in codepoints])) 
                        
    if translates:
        # Apply the given translations.
        for source, target in translates.items():
            txt = txt.translate({ord(c): target for c in source})                
    if ignore_cases:
        # Transform the string to lowercase letters.
        txt = txt.lower()
        
    if ignore_whitespaces:
        # Remove all whitespaces.
        return "".join(txt.split())
    else:    
        # Replace all runs of whitespaces by a single whitespace.
        return " ".join(txt.split())

# ______________________________________________________________________________
#

def visualize_run(run):
    ''' Visualizes the given run. '''
    
    snippets = []
    run_insertion_start = "\033[30;42m"
    run_insertion_end = "\033[0m"
    run_deletion_start = "\033[30;41m"
    run_deletion_end = "\033[0m"
    
    for item in run:
        if item.insertion:
            snippets.append(run_insertion_start)
            snippets.append(item.insertion.element)
            snippets.append(run_insertion_end)
            snippets.append(" ")
    
    snippets.append("<=> ")
            
    for item in run:
        if item.deletion:
            snippets.append(run_deletion_start)
            snippets.append(item.deletion.element)
            snippets.append(run_deletion_end)
            snippets.append(" ")
        
    
    return "".join(snippets)
   
def visualize_diff_result(commons, inserts, deletes, path=None):
    ''' Visualizes the given diff result. '''
    
    full = commons + inserts + deletes
    full.sort()
                   
    visualization_delete_start = "\033[30;41m"
    visualization_delete_end = "\033[0m"
    visualization_insert_start = "\033[30;42m"
    visualization_insert_end = "\033[0m"
     
    snippets = []
        
    for item in full:
        if isinstance(item, DiffInsertItem):
            snippets.append(visualization_insert_start)
        elif isinstance(item, DiffDeleteItem):
            snippets.append(visualization_delete_start)
        snippets.append("%s" % item.element)
        if isinstance(item, DiffInsertItem):
            snippets.append(visualization_insert_end)
        elif isinstance(item, DiffDeleteItem):
            snippets.append(visualization_delete_end)
        snippets.append(" ")
        
    visualization_string = "".join(snippets)
    if path:
        visualization = open(path, "w")
        visualization.write(visualization_string)
        visualization.close()
    
    return visualization_string

  
if __name__ == "__main__":
    s1 = "The red fox jumps over the blue sea"
    s2 = "The red [3] fox [4] jumps over the ble sea"
    diff(s1.split(), s2.split(), rearrange=True, max_dist=1)
