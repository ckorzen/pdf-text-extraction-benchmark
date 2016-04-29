# TODO: Introduce junk list.

from queue import PriorityQueue
from collections import defaultdict
import re
import copy

class Diff:
    """
    Class with methods to find the differences between two given lists of 
    strings.
    The lists may be arbitrarily nested to keep any special divisions into
    paragraphs, pages, etc (like '[["foo", "bar"], ["baz", "blub"]]' for 
    example). 
    
    The algorithm is able to rearrange elements in the target list to reflect 
    the order of elements in actual list.
    
    * Inspired by the simplediff lib by Paul Butler 
    (see https://github.com/paulgb/simplediff/) *  
    """

    def __init__(self, actual, target):
        """ Creates a new DiffObject. """
        self.actual = actual
        self.target = target
        self.actual_flatten = flatten(self.actual)
        self.target_flatten = flatten(self.target)

    def run(self, rearrange=False):
        """ Finds the differences between the two given lists of strings. If 
        the rearrange flag is set to True, the algorithm rearranges elements in 
        the target list to reflect the order of elements in actual list."""
        
        actual = self.actual_flatten
        target = self.target_flatten

        if rearrange:
            target, inter_diff_result = self.rearrange(actual, target)

        # FIXME
        xxx = copy.deepcopy(inter_diff_result)

        return self.diff(actual, target), xxx

    def diff(self, actual, target):
        """ Runs diff on the two given lists. The lists must be flattened. To
        flatten a list use the flatten() method below."""
        result = DiffResult()
        self._diff(actual, target, result)
        return result

    def _diff(self, actual, target, result, actual_pos=0, target_pos=0):
        """ Runs diff on the two given lists recursively. Appends the diff 
        items to the given result object. """

        # Map strings from actual to their positions in actual.
        actual_dict = dict()  
        for i, actual_item in enumerate(actual):
            actual_dict.setdefault(actual_item.string, []).append(i)

        # Find the largest substring common to 'actual' and 'target'.
        # 
        # We iterate over each value in 'target'. At each iteration, overlap[i] 
        # is the length of the largest suffix of actual[:i] equal to a suffix 
        # of target[:j] (or unset when actual[i] != target[j]).
        #
        # At each stage of iteration, the new overlap (called _overlap until 
        # the original overlap is no longer needed) is built from 'actual'.
        #
        # If the length of overlap exceeds the largest substring
        # seen so far (length), we update the largest substring
        # to the overlapping strings.
        overlap = {}
        
        # actual_start is the index of the beginning of the largest overlapping
        # substring in 'actual'. 'target_start' is the index of the beginning 
        # of the same substring in 'target'. length is the length that overlaps 
        # in both.
        # These track the largest overlapping substring seen so far, so 
        # naturally we start with a 0-length substring.
        actual_start = 0
        target_start = 0
        length = 0

        for i, target_item in enumerate(target):
            _overlap = {}
            for j in actual_dict.get(target_item.string, []):
                # now we are considering all values of i such that
                # actual[i] == target[j].
                _overlap[j] = (j and overlap.get(j - 1, 0)) + 1
                # Check if this is the largest substring seen so far.
                if _overlap[j] > length:
                    length = _overlap[j]
                    actual_start = j - length + 1
                    target_start = i - length + 1
            overlap = _overlap

        if length == 0:
            result.create_replace(actual, target, actual_pos, target_pos)
            actual_pos += len(actual)
            target_pos += len(target)
        else:
            # A common substring was found. Call diff recursively for the 
            # substrings to the left and to the right
            actual_left = actual[ : actual_start]
            target_left = target[ : target_start]
            actual_pos, target_pos = self._diff(actual_left, target_left, 
                result, actual_pos, target_pos)
            
            actual_middle = actual[actual_start : actual_start + length]
            target_middle = target[target_start : target_start + length]
            result.create_common(actual_middle, target_middle, 
                actual_pos, target_pos)
            actual_pos += length
            target_pos += length
            
            actual_right = actual[actual_start + length : ]
            target_right = target[target_start + length : ]
            actual_pos, target_pos = self._diff(actual_right, target_right, 
                result, actual_pos, target_pos)
        return (actual_pos, target_pos)

    def rearrange(self, actual, target):
        ''' Rearranges the items in 'actual' to reproduce the same order of the 
        items in 'target'.'''
        
        runs = PriorityQueue()
        runs_by_items = {}
                
        # First, do a normal diff without rearranging.
        diff_result = self.diff(actual, target)

        # Map each deleted string to its related position.
        deletes_index = {}
        for item in diff_result.delete_items:
            deletes_index.setdefault(item.source.string, []).append(item)
        
        # Map each inserted string to its matching (deleted) candidates.
        for item in diff_result.insert_items:
            candidates = deletes_index.get(item.source.string, [])
            # Set the candidates for the inserted item.
            item.set_match_candidates(candidates)
        
        prev_common = None
        for diff_item in diff_result.commons_and_replaces:
            if isinstance(diff_item, DiffCommon):
                prev_common = diff_item
            elif isinstance(diff_item, DiffReplace):
                run = self.find_longest_run(diff_item.insert)
                
                if run:
                    # The queue is min-based, so put a negative priority.
                    runs.put((-len(run.items), run))
                    
                    for item in run.items:
                        runs_by_items.setdefault(item.delete_item, set()).add(run)
                else:
                    # If no run was found, append the items to last common.
                    if prev_common:
                        for item in diff_item.insert.items:
                            item.match_to(prev_common.items[-1])
         
        # Process the queue.
        while not runs.empty():
            priority, run = runs.get()
            
            if not run or run.is_obsolete:
                continue
            
            # There may be insertions which could not be matched to a deleted 
            # position. Hence we do not know where to insert such insertions.
            # If there is such insertion within a run, concat it with a 
            # preceding or succeeding matched insertion.
            
            # The previous insert_item that could be matched.
            prev_matched_insert_item = None
            # If there is no matched insertion yet, add a unmatched insertion 
            # to this queue.
            unmatched_insert_items = []
            
            # Keep track of obsolete runs, i.e. runs that contain items that 
            # were matched in the meantime by another run.
            obsolete_runs = set()
            
            for run_item in run.items:
                delete_item = run_item.delete_item
                insert_item = run_item.insert_item
                
                if delete_item is not None:
                    # The insert_item has a matched delete_item.
                    prev_matched_insert_item = insert_item
                    
                    # First, concat all unmatched insertions (if any) to this 
                    # deletion.
                    for i, unmatched in enumerate(unmatched_insert_items):
                        unmatched.match_to(delete_item)
                        delete_item.match_to(unmatched)
                    unmatched_insert_items = []

                    # Then map the actual insertion to this deletion.
                    insert_item.match_to(delete_item)
                    delete_item.match_to(insert_item)
                else:
                    # The insertion has no matched deletion.
                    if prev_matched_insert_item:
                        # If there was a matched insertion seen so far, add the 
                        # unmatched insertion to the this matched insertion.
                        insert_item.match_to(prev_matched_insert_item.match)
                        prev_matched_insert_item.match.match_to(insert_item)
                    else:
                        # Otherwise queue the unmatched insertion.
                        unmatched_insert_items.append(insert_item)
                
                obsolete_runs.update(runs_by_items.get(delete_item, set()))
           
            # Recompute all obsolete runs.
            for obsolete_run in obsolete_runs:
                obsolete_run.is_obsolete = True
                new_run = self.find_longest_run(obsolete_run.insert)
                
                if not new_run:
                    continue
                
                runs.put((-len(new_run.items), new_run))
                        
                for item in new_run.items:
                    delete_item = item.delete_item
                    runs_by_item = runs_by_items.setdefault(delete_item, set())
                    runs_by_item.discard(obsolete_run)
                    runs_by_item.add(new_run)

        # The rearranged list follows from the commons and (updated) inserts.
        rearranged = diff_result.common_items + diff_result.insert_items
                
        rearranged.sort(key = lambda x: x.pos)

        return [item.source for item in rearranged], diff_result

    def find_longest_run(self, insert):
        ''' Finds the longest possible run in the given insert. '''

        if not insert or not insert.items:
            return DiffRun(insert)
        
        # The runs found so far.
        active_runs = []
        # The longest run found so far.
        longest_run = []
        
        # The runs by their end elements. For example, if the runs at indices
        # 1, 2, 3 end with '5' and the runs at indices 4 and 5 ends with '7' 
        # the map looks like: { 5: {1, 2, 3}, 7: {4, 5} }
        runs_by_end_items = defaultdict(lambda: set())
        # The end elements by runs. For the example above, this map looks like:
        # { 1: 5, 2: 5, 3: 5, 4: 7, 5: 7 }
        end_items_by_runs = defaultdict(lambda: set())
        # The queue of unmatched inserts (inserts with no associated deletion).
        unmatched_queue = DiffRun(insert)
                          
        for insert_item in insert.items:
            if insert_item.match:
                # If the insert is already matched, the item shouldn't be a 
                # member of a run anymore.
                continue
                
            delete_item_candidates = insert_item.match_candidates

            prev_active_runs = active_runs
            active_runs = []
            
            prev_runs_by_end_items = runs_by_end_items
            runs_by_end_items = defaultdict(lambda: set())
            
            prev_end_items_by_runs = end_items_by_runs
            end_items_by_runs = defaultdict(lambda: set())
             
            # Iterate through the deleted positions.
            for delete_item in delete_item_candidates:
                # If the deletion is already matched, the item shouldn't be a 
                # member of a run anymore.
                if delete_item is not None and delete_item.is_matched:
                    continue
                
                # Obtain the position of the deletion (could be None).
                pos = None
                if delete_item is not None:
                    pos = delete_item.source.pos_flat
                
                if pos is not None: # There is a matched deletion.
                    # Check, if there are runs with end element 'pos-1'
                    if pos - 1 in prev_runs_by_end_items:
                        # There are runs that end with 'pos-1'. 
                        run_indices = prev_runs_by_end_items[pos - 1]
                        
                        for run_index in run_indices:
                            # Append the element to the run.
                            run = prev_active_runs[run_index]
                            run.add(DiffRunItem(delete_item, insert_item)) 
                            
                            # Add the run to active runs.
                            active_runs.append(run)
                            new_run_index = len(active_runs) - 1
                            
                            # Check, if the current run is now the longest run. 
                            if len(run) > len(longest_run):
                                longest_run = run
                            
                            # Update the maps
                            runs_by_end_items[pos].add(new_run_index)
                            end_items_by_runs[new_run_index].add(pos)
                    else:                   
                        # There is no run that end with 'pos-1'.
                        # Create new run.
                        new_run = unmatched_queue
                        new_run.add(DiffRunItem(delete_item, insert_item))
                                            
                        # Append the run to active runs.
                        active_runs.append(new_run)
                        new_run_index = len(active_runs) - 1
                                                  
                        # Check, if the new run is the longest run.     
                        if len(new_run) > len(longest_run):
                            longest_run = new_run
                                    
                        # Update the maps.
                        runs_by_end_items[pos].add(new_run_index)
                        end_items_by_runs[new_run_index].add(pos)
                    # Clear the queue.
                    unmatched_queue = DiffRun(insert)
                else: # There is no matched deletion.
                    run_item = DiffRunItem(delete_item, insert_item)
                    unmatched_queue.add(run_item)
                    
                    for j, active_run in enumerate(prev_active_runs):
                        pos = max(prev_end_items_by_runs[j]) + 1
                            
                        # Append the element to run.
                        active_run.append(run_item)
                        active_runs.append(active_run)
                        new_run_index = len(active_runs) - 1
                          
                        runs_by_end_items[pos].add(new_run_index)
                        end_items_by_runs[new_run_index].add(pos)
                                                
                        if len(active_run) > len(longest_run):
                            longest_run = active_run

        if longest_run:
            return longest_run
        else:
            return unmatched_queue
# ______________________________________________________________________________

def flatten(hierarchy):
    """ Flattens the given hierarchy of strings to a flat list. Keeps track of 
    the position in the hierarchy of each string."""
    flattened = []
    flatten_recursive(hierarchy, flattened)
    return flattened

def flatten_recursive(hierarchy, result, pos_stack=[]):
    """ Flattens given (sub-)hierarchy and stores the result to given list."""

    for i, element in enumerate(hierarchy):
        new_pos_stack = pos_stack + [i]
        
        if isinstance(element, list):
            flatten_recursive(element, result, new_pos_stack)
        else:
            result.append(DiffInputItem(element, len(result), new_pos_stack))

# ______________________________________________________________________________

class DiffInputItem:
    def __init__(self, string, pos_flat, pos_stack): 
        self.string = string
        # The initial position of the string in the input.
        # The position of the string after diff (may be reassigned).
        self.pos_flat = pos_flat
        self.pos_stack = pos_stack
        self.new_pos_flat = pos_flat
        self.new_pos_stack = pos_stack
        self.pos_updated = False

    def update_pos(self, item):
        self.new_pos_flat = item.pos_flat
        self.new_pos_stack = item.pos_stack
        self.pos_updated = True

    def __str__(self):
        return "(%s, was: %s, is: %s, u: %s)" % (self.string, self.pos_stack, self.new_pos_stack, self.pos_updated)
        
    def __repr__(self):
        return self.__str__()
        
    def __lt__(self, other):
        return self.pos_flat < other.pos_flat

# ______________________________________________________________________________

class DiffCommon:
    def __init__(self, actual, target, actual_pos, target_pos):
        self.items = []
        self.pos = (actual_pos, target_pos)
        for a, b in zip(actual, target):
            self.items.append(DiffCommonItem(a, b, actual_pos, target_pos))
            actual_pos += 1
            target_pos += 1

class DiffCommonItem:
    def __init__(self, actual_source, target_source, actual_pos, target_pos):
        #self.source = target_source
        self.source = actual_source
        self.source_matched = target_source
        self.pos = (actual_pos, target_pos)
        actual_source.update_pos(target_source)
        target_source.update_pos(actual_source)
# ______________________________________________________________________________

class DiffInsert:
    def __init__(self, input_items, actual_pos, target_pos):
        self.items = []
        for i, item in enumerate(input_items):
            self.items.append(DiffInsertItem(item, actual_pos, target_pos + i))

class DiffInsertItem:
    def __init__(self, source, actual_pos, target_pos):
        self.source = source
        self.match_candidates = []
        self.match = None
        self.source_matched = None
        self.pos = (actual_pos, target_pos)

    def set_match_candidates(self, candidates):
        self.match_candidates = candidates

    def match_to(self, delete_item):
        self.pos = delete_item.pos
        self.match = delete_item
        self.source_matched = delete_item.source
        self.source.update_pos(delete_item.source)
        delete_item.source.update_pos(self.source)

# ______________________________________________________________________________

class DiffDelete:
    def __init__(self, input_items, actual_pos, target_pos):
        self.items = []
        for i, item in enumerate(input_items):
            self.items.append(DiffDeleteItem(item, actual_pos + i, target_pos))

class DiffDeleteItem:
    def __init__(self, input_item, actual_pos, target_pos):
        self.source = input_item
        self.num_matches = 0
        self.is_matched = False
        self.source_matched = None
        self.pos = (actual_pos, target_pos)
        
    def match_to(self, insert_item):
        self.num_matches += 1
        self.is_matched = True
        self.source_matched = insert_item.source
        self.source.update_pos(insert_item.source)
        insert_item.source.update_pos(self.source)

# ______________________________________________________________________________

class DiffReplace:
    def __init__(self, actual_items, target_items, actual_pos, target_pos):
        self.delete = DiffDelete(actual_items, actual_pos, target_pos)
        self.insert = DiffInsert(target_items, actual_pos, target_pos)
        self.pos = (actual_pos, target_pos)

# ______________________________________________________________________________

class DiffResult:
    """ The result of a Diff pass."""
    
    def __init__(self):
        """ Creates a new DiffResult."""
        self.replaces = []
        self.commons = []
        self.commons_and_replaces = []

        self.common_items = []
        self.insert_items = []
        self.delete_items = []
        
        self.all_items = []
       
    def create_common(self, actual, target, actual_pos, target_pos):
        if not actual and not target:
            return
        
        common = DiffCommon(actual, target, actual_pos, target_pos)
        
        self.commons.append(common)
        self.commons_and_replaces.append(common)
        self.common_items.extend(common.items)
        self.all_items.extend(common.items)
        
    def create_replace(self, actual, target, actual_pos, target_pos):
        if not actual and not target:
            return
            
        replace = DiffReplace(actual, target, actual_pos, target_pos)
        
        self.replaces.append(replace)
        self.commons_and_replaces.append(replace)
        self.insert_items.extend(replace.insert.items)
        self.delete_items.extend(replace.delete.items)
        self.all_items.extend(replace.insert.items)
        self.all_items.extend(replace.delete.items)

# ______________________________________________________________________________
 
class DiffRun:
    """ A run that holds a list of matchings between an insert and consecutive
    deleted items."""
    
    def __init__(self, insert):
        """" Creates a new run based on the given insert. """
        self.insert = insert
        self.items = []
        self.is_obsolete = False
        
    def add(self, item):
        """ Adds the given run item to this run. """
        self.items.append(item)
    
    def __len__(self):
        return len(self.items)
    
    def __lt__(self, other):
        """ Define an order on DiffRun to be able to put a run into a queue. """
        return len(self.items) < len(other.items)
    
class DiffRunItem:
    """ A member item of a DiffRun that holds a matching between a deleted item
    and a inserted item. """
    
    def __init__(self, delete_item, insert_item):
        """ Creates a new DiffRunItem. """
        self.insert_item = insert_item
        self.delete_item = delete_item
