from collections import defaultdict
from recordclass import recordclass
from fuzzy_dict import fuzzy_dict
from queue import PriorityQueue

import re
import logging

logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s : %(levelname)s : %(module)s : %(message)s',
)
logger = logging.getLogger(__name__)

DiffInputItem   = recordclass('DiffInputItem', 'string global_pos outline rearranged')

DiffResult      = recordclass('DiffResult', 'commons replaces num_reorders')
DiffReplaceItem = recordclass('DiffReplaceItem', 'deletes inserts')

DiffCommonItem  = recordclass('DiffCommonItem', 'pos1 pos2 element, old_element')
DiffDeleteItem  = recordclass('DiffDeleteItem', 'pos1 pos2 element matched')
DiffInsertItem  = recordclass('DiffInsertItem', 'pos1 pos2 element matched')

MappingItem     = recordclass('MappingItem','deletions insertion')
RunItem         = recordclass('RunItem', 'deletion insertion')

def diff(old, new, rearrange=False, junk=[], max_dist=0, min_sim=1):
    '''
    This method finds the differences between the two given lists of strings.
    The lists may be arbitrarily nested to keep any special divisions into
    paragraphs, pages, etc (like '[["foo", "bar"], ["baz", "blub"]]' for 
    example). 
    
    If the rearrange flag is set to True, the algorithm applies a kind of a 
    greedy heuristic to rearrange the strings in 'new' to reproduce the order 
    of the strings in 'old' as much as possible.
    
    If max_dist or min_sim is given, the algorithm considers two strings as 
    equal if the distance between them is at most max_dist 
    (if the similarity of both elements is at least min_sim).
    
    Returns an object of type DiffResult containing a list of common strings (C) 
    and a list of replacements (R). A replacement is defined by a list of 
    consecutive deleted strings (D) and a list of consecutive inserted strings 
    (I).
    The elements of C are tuples of form (pos1, pos2, element, old_element').
    The fields pos1 and pos2 represents the position in the rearranged string
    and can be used on sorting the diff elements. The fields 'element' and 
    'old_element' are tuples of form (pos_1, ... , pos_i, global_pos, string).
    pos_1, ..., pos_i represent the position of the element in the i-times
    nested input list, global_pos represents the position of the element in the
    flattened input list and string represents the actual string to compare.
    The elements of D and I are tuples of form (pos1, pos2, element); the
    meanings of fields are equivalent to above. 
    
    * Inspired by the simplediff lib by Paul Butler 
    (see https://github.com/paulgb/simplediff/) *  
    '''

    # Flatten the old list.
    old = flatten(old)
    # Flatten the new list.
    new = flatten(new)

    return diff_flattened_lists(old, new, rearrange, junk, max_dist, min_sim)

def diff_flattened_lists(old, new, rearrange=False, junk=[], max_dist=0, 
        min_sim=1):
    """ Finds the differences between the two already flattened lists of 
    strings."""
    
    num_reorders = 0
    if rearrange:
        # Try to rearrange the elements in 'new'
        new, num_reorders = rearrange_elements(old, new, max_dist, min_sim)
        
    # Do the diff.
    commons, replaces = [], []
    _diff(old, new, 0, 0, commons, replaces, junk, max_dist, min_sim)

    return DiffResult(commons, replaces, num_reorders)

def _diff(old, new, pos1, pos2, commons, replaces, junk=[], max_dist=0, min_sim=1):
    '''
    Finds the differences between the two given lists of strings recursively. 
    pos1 and pos2 are the current positions in 'old' and 'new'. 
    If max_dist or min_sim is given, the algorithms considers two elements 
    as equal if the distance between them is at most max_dist (if the 
    similarity of both elements is at least min_sim).
    Fills the common elements and replaced elements into the given lists 
    'commons' and 'replaces'.
    '''

    # Create an index from values in 'old', where each value is mapped to its 
    # position in 'old':
    # { element1: [1, 5, 7], element2: [4, 6, ], ... } 
    dict_old = fuzzy_dict()  
    for i, input_old in enumerate(old):
        if input_old.string in dict_old:
            dict_old[input_old.string].append(i)
        else:
            dict_old[input_old.string] = [i]

    # Find the largest substring common to 'old' and 'new'.
    # 
    # We iterate over each value in 'new'. At each iteration, overlap[i] is the
    # length of the largest suffix of old[:i] equal to a suffix of 
    # new[:i_new] (or unset when old[i] != new[i_new]).
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

    for i_new, input_new in enumerate(new):
        _overlap = defaultdict(lambda: 0)
        # Get the 'closest' match to the string.
        for i_old in dict_old.get(input_new.string, max_dist, min_sim, []):
            # now we are considering all values of index1 such that
            # old[index1] == new[i_new].
            _overlap[i_old] = (i_old and overlap[i_old - 1]) + 1
            # Check if this is the largest substring seen so far.
            if _overlap[i_old] > length:
                length = _overlap[i_old]
                start_old = i_old - length + 1
                start_new = i_new - length + 1
        overlap = _overlap

    if length == 0:
        # No common substring was found. Return an insert and delete...
        deletes, inserts = [], []
        ignore = False
        for input_old in old:
            ignore = ignore or any(re.search(regex, input_old.string) for regex in junk)
        
        if not ignore:
            for input_old in old:
                deletes.append(DiffDeleteItem(pos1, pos2, input_old, False))
                pos1 += 1
            for input_new in new:
                inserts.append(DiffInsertItem(pos1, pos2, input_new, False))
                pos2 += 1
            if old or new:
                replaces.append(DiffReplaceItem(deletes, inserts))
        
    else:
        # A common substring was found. Call diff recursively for the 
        # substrings to the left and to the right
        left1 = old[ : start_old]
        left2 = new[ : start_new]
        pos1, pos2 = _diff(left1, left2, pos1, pos2, commons, replaces, 
            junk=junk, max_dist=max_dist, min_sim=min_sim)
        
        i = 0
        for input_new in new[start_new : start_new + length]:
            input_old = old[start_old + i]
            commons.append(DiffCommonItem(pos1, pos2, input_new, input_old))
            pos1 += 1
            pos2 += 1
            i += 1
        
        right1 = old[start_old + length : ]
        right2 = new[start_new + length : ]
        pos1, pos2 = _diff(right1, right2, pos1, pos2, commons, replaces, 
            junk=junk, max_dist=max_dist, min_sim=min_sim)
        
    return (pos1, pos2)

# ______________________________________________________________________________
# Rearrange

def rearrange_elements(old, new, max_dist=0, min_sim=1.0):
    ''' Tries to rearrange the elements in 'new' as much as possible to 
    reproduce the same order of the elements in 'old'. If the ignore_cases flag 
    is set to True, the diff is done case-insensitive. If the ignore_whitespace 
    flag is set to True, all whitespaces will be ignored.
    If there are translations given in the translates dict, they will be applied
    to 'old' and 'new' (e.g. you can specify a dict {".,!?": " "} to translate
    all occurrences of '.', ',', '!' and '?' to the whitespace ' '. If max_dist 
    or min_sim is given, the algorithms considers two elements as equal if the 
    distance between them is at most max_dist (if the similarity of both 
    elements is at least min_sim).
    Let's define a running example to clarify the function of the method:
    old = ["The", "fox", "and", "the", "cow"]
    new = ["The", "cow", "and", "the", "red", "fox"]  
    '''
                      
    # First, do a normal diff without rearranging:
    # commons = [
    #    (pos1=0, pos2=0, element='The'), 
    #    (pos1=2, pos2=2, element='and'), 
    #    (pos1=3, pos2=3, element='the')
    # ]
    # inserts = [
    #    (pos1=2, pos2=1, element='cow', matched=False), 
    #    (pos1=5, pos2=4, element='red', matched=False),
    #    (pos1=5, pos2=5, element='fox', matched=False)
    # ]
    # deletes = [
    #    (pos1=1, pos2=1, element='fox', matched=False), 
    #    (pos1=4, pos2=4, element='cow', matched=False)
    # ]
    diff_result = diff_flattened_lists(old, new, rearrange=False, 
        max_dist=max_dist, min_sim=min_sim)

    # Map each deleted element to its deleted items:
    # { 
    #   'cow': [(pos1=4, pos2=4, element='cow', matched=False)], 
    #   'fox': [(pos1=1, pos2=1, element='fox', matched=False)],
    # }
    deletes_index = fuzzy_dict()
    for replace in diff_result.replaces:
        for item in replace.deletes:
            if item.element.string in deletes_index:
                deletes_index[item.element.string].append(item)
            else:
                deletes_index[item.element.string] = [item]
                                            
    # Separate the insertions and associate each element of each insertion to 
    # its positions in the deletes_index:
    # [
    #   # Insertion 1
    #   [
    #     ([(pos1=4, pos2=4, element='cow', matched=False)], (pos1=2, pos2=1, element='cow', matched=False))
    #   ], 
    #   # Insertion 2
    #   [
    #     ([None], (pos1=5, pos2=4, element='fox', matched=False)),
    #     ([(pos1=1, pos2=1, element='fox', matched=False)], (pos1=5, pos2=5, element='fox', matched=False))
    #   ]
    # ]
     
    inserts_mappings = []
    for replace in diff_result.replaces:
        insert_mapping = []
        for insert in replace.inserts:
            deletions = deletes_index.get(insert.element.string, max_dist, min_sim, [None])
            insert_mapping.append(MappingItem(deletions, insert))    
        inserts_mappings.append(insert_mapping) 
     
                         
    # For each mapping, compute the longest "run", that is a sequence of words 
    # in an insertion that occurs in the same order as in the groundtruth. Try
    # to keep unmatched insertions at the original position.
    # [
    #   (None, (pos1=5, pos2=4, element='red', matched=False)), 
    #   ((pos1=1, pos2=1, element='fox', matched=False), (pos1=5, pos2=5, element='fox', matched=False))
    # ]
    run = find_longest_run_in_mappings(inserts_mappings)
    num_reorders = 0
    
    while run:    
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
                    unmatched.element.rearranged = True
                    num_reorders += 1

                # Then map the actual insertion to this deletion.
                insertion.pos1 = deletion.pos1
                insertion.pos2 = deletion.pos2 + len(unmatched_insertions)            
                
                unmatched_insertions = []
                deletion.matched = insertion.matched = True
                insertion.element.rearranged = True
                num_reorders += 1
            else:
                # The insertion has no matched deletion.
                if last_matched_insertion:
                    # If there was a matched insertion seen so far, add the 
                    # unmatched insertion to the this matched insertion.
                    insertion.pos1 = last_matched_insertion.pos1
                    insertion.pos2 = last_matched_insertion.pos2 + 1
                    last_matched_insertion = insertion
                    insertion.element.rearranged = True
                    num_reorders += 1
                else:
                    # Otherwise queue the unmatched insertion.
                    unmatched_insertions.append(insertion)
                insertion.matched = True  

        # Find another run.
        run = find_longest_run_in_mappings(inserts_mappings)

    # The rearranged list follows from the commons and the (updated) inserts.       
    rearranged = diff_result.commons
    
    for replace in diff_result.replaces:
        rearranged.extend(replace.inserts)

    rearranged.sort()

    # Remove all the meta stuff.
    rearranged = [item.element for item in rearranged]

    return rearranged, num_reorders

def find_longest_run_in_mappings(mappings):
    longest_run = []
    for mapping in mappings:
        run = find_longest_run_in_mapping(mapping)
        if run and len(run) > len(longest_run):
            longest_run = run
    return longest_run

def find_longest_run_in_mapping(mapping): # Find run in a single(!) element.
    ''' Finds the longest possible run in the given mapping. The mapping is of 
    the form: [([deletions1], insert1), ([deletions2], insert2), ...] where each
    list of deletions is of form [(pos1, pos2, element), ...]. Note that 
    deletions could be [None] if there is no matched deletion for the insert.
    '''
            
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
            
            # Obtain the position of the deletion (could be None).
            pos = deletion.pos1 if deletion is not None else None
                                      
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

    if longest_run:
        return longest_run
    else:
        return unmatched_queue  
                      
# ______________________________________________________________________________
# Util methods.

def visualize_run(run):
    ''' Visualizes the given run. '''
    
    snippets = []
    run_insertion_start = "\033[30;42m"
    run_insertion_end = "\033[0m"
    run_deletion_end = "\033[0m"
    
    for item in run:
        if item.insertion:
            snippets.append(run_insertion_start)
            snippets.append(item.insertion.string)
            snippets.append(run_insertion_end)
            snippets.append(" ")
    
    snippets.append("<=> ")
            
    for item in run:
        if item.deletion:
            snippets.append(run_deletion_start)
            snippets.append(item.deletion.string)
            snippets.append(run_deletion_end)
            snippets.append(" ")
        
    
    return "".join(snippets)
  
def flatten(elements):
    """ Flattens the given arbitrarily nested list of elements to flat list of 
    objects of type DiffInputItem.

    >>> flatten(['A', 'B', 'C'])
    [('A', 0, [0]), ('B', 1, [1]), ('C', 2, [2])]
    >>> flatten(['A', ['B', 'C']])
    [('A', 0, [0]), ('B', 1, [1, 0]), ('C', 2, [1, 1])]
    >>> flatten([['A', 'B'], 'C'])
    [('A', 0, [0, 0]), ('B', 1, [0, 1]), ('C', 3, [1])]
    """
    
    result = []
    flatten_recursive(elements, 0, [], result)
    return result

def flatten_recursive(elements, global_pos, outline, res):
    """ Flattens the given arbitrarily nested list of elements recursively and
    stores the items to given result list."""

    for i, elem in enumerate(elements):
        new_outline = outline + [i]
        
        if isinstance(elem, list):
            global_pos = flatten_recursive(elem, global_pos, new_outline, res)
        else:
            res.append(DiffInputItem(elem, global_pos, new_outline, False))
            global_pos += 1
    return global_pos

