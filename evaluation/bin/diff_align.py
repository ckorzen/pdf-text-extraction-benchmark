import util
import diff as df
from collections import defaultdict
from queue import PriorityQueue

def diff(actual, target, junk=[]):
    """ An extended version of diff. This version is able to align 'target'
    to 'actual'. For each deletion, the deleteions are searched for a matching 
    position. If a matching position is found, the deletion is matched to this
    position."""
    return diff_and_align(actual, target, junk)

def diff_and_align(actual, target, junk=[]):
    # Run plain diff.
    diff_result = df.diff(actual, target)

    # Compute the elements to append to the results for deletes and inserts.
    aligned_deletes, remaining_inserts = align(diff_result)

    result = []
    for item in diff_result.commons_and_replaces:
        if isinstance(item, df.DiffCommon):
            result.append(Common(item))
        elif isinstance(item, df.DiffReplace):
            if not util.ignore(item, junk):
                result.extend(remaining_inserts.get(item.insert, []))
                result.extend(aligned_deletes.get(item.delete, []))

    return result

def align(diff_result):
    aligned_deletes = {}
    remaining_inserts = {}

    # Map each insert to its position.
    inserts = []
    inserts_index = {}
    for insert in diff_result.inserts:
        insert = DiffInsertWrapper(insert)
        for item in insert.items:
            string = item.wrapped.source.string
            inserts_index.setdefault(string, []).append(item)
        inserts.append(insert)

    # Map each deleteion to all matching deletions.
    deletes = []
    for delete in diff_result.deletes:
        delete = DiffDeleteWrapper(delete)
        for item in delete.items:
            candidates = inserts_index.get(item.wrapped.source.string, [])
            # Set the matching candidates for the delete item.
            item.set_match_candidates(candidates)
        deletes.append(delete)

    # Fill queue with runs.
    runs = PriorityQueue()
    runs_by_items = {}
    for delete in deletes:
        run = find_longest_run(delete)
        
        if run:
            # The queue is min-based, so put a negative priority.
            runs.put((-len(run), run))
            
            for item in run.items:
                runs_by_items.setdefault(item.insert_item, set()).add(run)

    # Process the queue of runs.
    while not runs.empty():
        priority, run = runs.get()

        if not run or run.is_obsolete:
            continue

        # There may be deleteions which could not be matched to a insertd 
        # position. Hence we do not know where to delete such deleteions.
        # If there is such deleteion within a run, concat it with a 
        # preceding or succeeding matched deleteion.
        
        # The previous delete_item that could be matched.
        prev_matched_delete_item = None
        # If there is no matched deleteion yet, add a unmatched deleteion 
        # to this queue.
        unmatched_delete_items = []

        # Keep track of obsolete runs, i.e. runs that contain items that 
        # were matched in the meantime by another run.
        obsolete_runs = set()

        # Associate the run with the delete.
        run.delete.register_run(run)

        for run_item in run.items:
            insert_item = run_item.insert_item
            delete_item = run_item.delete_item
            
            if insert_item is not None:
                # The delete_item has a matched insert_item.
                prev_matched_delete_item = delete_item
                
                # First, concat all unmatched deleteions (if any) to this 
                # deletion.
                for i, unmatched in enumerate(unmatched_delete_items):
                    unmatched.match_to(insert_item)
                    insert_item.match_to(unmatched)
                unmatched_delete_items = []

                # Then map the actual deleteion to this deletion.
                delete_item.match_to(insert_item)
                insert_item.match_to(delete_item)
            else:
                # The deleteion has no matched deletion.
                if prev_matched_delete_item:
                    # If there was a matched deleteion seen so far, add the 
                    # unmatched deleteion to the this matched deleteion.
                    delete_item.match_to(prev_matched_delete_item.match)
                    prev_matched_delete_item.match.match_to(delete_item)
                else:
                    # Otherwise queue the unmatched deleteion.
                    unmatched_delete_items.append(delete_item)
            
            obsolete_runs.update(runs_by_items.get(insert_item, set()))

        # Recompute all obsolete runs.
        for obsolete_run in obsolete_runs:
            obsolete_run.is_obsolete = True
            new_run = find_longest_run(obsolete_run.delete)
            
            if new_run:
                runs.put((-len(new_run), new_run))
                    
                for item in new_run.items:
                    insert_item = item.insert_item
                    runs_by_item = runs_by_items.setdefault(insert_item, set())
                    runs_by_item.discard(obsolete_run)
                    runs_by_item.add(new_run)

    for delete in deletes:
        aligned_deletes[delete.wrapped] = resolve_delete(delete)
    for insert in inserts:
        remaining_inserts[insert.wrapped] = resolve_insert(insert)

    return aligned_deletes, remaining_inserts

def resolve_delete(delete):
    result = []

    prev_run = None
    prev_delete = []
    for item in delete.items:
        run = item.run
        if run:
            if len(prev_delete) > 0:
                result.append(prev_delete)
                prev_delete = Delete()

            if run != prev_run:
                result.append(Rearrange(run))
            prev_run = run
        else:
            if not prev_delete:
                prev_delete = Delete()
            prev_delete.append(item)

    if len(prev_delete) > 0:
        result.append(prev_delete)
    return result

def resolve_insert(insert):
    result = []

    prev_insert = None
    for item in insert.items:
        if item.is_matched:
            prev_insert = None
        else:
            if not prev_insert:
                prev_insert = Insert()
                result.append(prev_insert)
            prev_insert.append(item)
    return result

def find_longest_run(delete):
    ''' Finds the longest possible run in the given delete. '''

    if not delete or not delete.items:
        return None
    
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
    # The queue of unmatched deletes (deletes with no associated deletion).
    unmatched_queue = DiffRun(delete)
                      
    for delete_item in delete.items:
        if delete_item.match:
            # If the delete is already matched, the item shouldn't be a 
            # member of a run anymore.
            continue
            
        insert_item_candidates = delete_item.match_candidates

        prev_active_runs = active_runs
        active_runs = []
        
        prev_runs_by_end_items = runs_by_end_items
        runs_by_end_items = defaultdict(lambda: set())
        
        prev_end_items_by_runs = end_items_by_runs
        end_items_by_runs = defaultdict(lambda: set())
         
        # Iterate through the insertd positions.
        for insert_item in insert_item_candidates:
            # If the deletion is already matched, the item shouldn't be a 
            # member of a run anymore.
            if insert_item is not None and insert_item.is_matched:
                continue
            
            # Obtain the position of the deletion (could be None).
            pos = None
            if insert_item is not None:
                pos = insert_item.wrapped.source.pos_flat
            
            if pos is not None: # There is a matched deletion.
                # Check, if there are runs with end element 'pos-1'
                if pos - 1 in prev_runs_by_end_items:
                    # There are runs that end with 'pos-1'. 
                    run_indices = prev_runs_by_end_items[pos - 1]
                    
                    for run_index in run_indices:
                        # Append the element to the run.
                        run = prev_active_runs[run_index]
                        run.add(DiffRunItem(insert_item, delete_item)) 
                        
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
                    new_run.add(DiffRunItem(insert_item, delete_item))
                                        
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
                unmatched_queue = DiffRun(delete)
            else: # There is no matched deletion.
                run_item = DiffRunItem(insert_item, delete_item)
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

class Common:
    def __init__(self, common):
        self.words = [x.source_target.string for x in common.items]
        self.positions_actual = [x.source_actual.pos_stack for x in common.items]
        self.positions_target = [x.source_target.pos_stack for x in common.items]

    def extend(self, common):
        self.words.extend([x.source_target.string for x in common.items])
        self.positions_actual.extend([x.source_actual.pos_stack for x in common.items])
        self.positions_target.extend([x.source_target.pos_stack for x in common.items])

    def __str__(self):
        return "(=, %s, %s, %s)" % (self.words, self.positions_actual, 
            self.positions_target)

    def __repr__(self):
        return self.__str__()

# ______________________________________________________________________________

class Rearrange:
    def __init__(self, run):
        self.words = []
        self.positions_actual = []
        self.positions_target = []

        for item in run.items:
            self.words.append(item.delete_item.wrapped.source.string)
            self.positions_actual.append(item.delete_item.wrapped.source.pos_stack)
            self.positions_target.append(item.insert_item.wrapped.source.pos_stack)

    def __str__(self):
        return "(<> %s %s %s)" % (self.words, self.positions_actual, 
            self.positions_target)

    def __repr__(self):
        return self.__str__()

# ______________________________________________________________________________

class Delete:
    def __init__(self):
        self.words = []
        self.positions_actual = []

    def append(self, delete_item):
        self.words.append(delete_item.wrapped.source.string)
        self.positions_actual.append(delete_item.wrapped.source.pos_stack)

    def __len__(self):
        return len(self.words)

    def __str__(self):
        return "(- %s %s)" % (self.words, self.positions_actual)

    def __repr__(self):
        return self.__str__()

# ______________________________________________________________________________

class Insert:
    def __init__(self):
        self.words = []
        self.positions_target = []

    def append(self, insert_item):
        self.words.append(insert_item.wrapped.source.string)
        self.positions_target.append(insert_item.wrapped.source.pos_stack)

    def __str__(self):
        return "(+ %s %s)" % (self.words, self.positions_target)

    def __repr__(self):
        return self.__str__()

# ==============================================================================

class DiffCommonWrapper:
    def __init__(self, diff_common):
        self.wrapped = diff_common

class DiffCommonItemWrapper:
    def __init__(self, item):
        self.wrapped = item

# ______________________________________________________________________________

class DiffDeleteWrapper:
    def __init__(self, diff_delete):
        self.wrapped = diff_delete
        self.items = []
        self.settled_runs = []
        for item in diff_delete.items:
            self.items.append(DiffDeleteItemWrapper(item))

    def register_run(self, run):
        self.settled_runs.append(run)
        for run_item in run.items:
            run_item.delete_item.run = run

class DiffDeleteItemWrapper:
    def __init__(self, item):
        self.wrapped = item
        self.match = None
        self.match_candidates = None
        self.run = None

    def set_match_candidates(self, candidates):
        self.match_candidates = candidates

    def match_to(self, item):
        self.match = item

# ______________________________________________________________________________

class DiffInsertWrapper:
    def __init__(self, diff_insert):
        self.wrapped = diff_insert
        self.items = []
        for item in diff_insert.items:
            self.items.append(DiffInsertItemWrapper(item))

class DiffInsertItemWrapper:
    def __init__(self, item):
        self.wrapped = item
        self.is_matched = False

    def match_to(self, item):
        self.is_matched = True

# ______________________________________________________________________________

class DiffRun:
    """ A run that holds a list of matchings between an delete and consecutive
    insertd items."""
    
    def __init__(self, delete):
        """" Creates a new run based on the given delete. """
        self.delete = delete
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
    """ A member item of a DiffRun that holds a matching between a insertd item
    and a deleteed item. """
    
    def __init__(self, insert_item, delete_item):
        """ Creates a new DiffRunItem. """
        self.delete_item = delete_item
        self.insert_item = insert_item

if __name__ == "__main__":
    actual = """Hello World how are you"""
    target = "how are you Hello World"
    actual_paras = util.to_formatted_paragraphs(actual)
    target_paras = util.to_formatted_paragraphs(target)

    print(diff(actual_paras, target_paras))