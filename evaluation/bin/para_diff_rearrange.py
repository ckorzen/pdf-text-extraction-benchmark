import util
import diff
from collections import defaultdict
from queue import PriorityQueue

def rearrange(diff_result, junk=[]):
    rearranged_replaces = rearrange_replaces(diff_result)

    # Resolve the replaces.
    result = []
    for i in range(0, len(diff_result)):
        prev_item = diff_result[i - 1] if i > 0 else None
        item = diff_result[i]
        next_item = diff_result[i + 1] if i < len(diff_result) - 1 else None

        if isinstance(item, diff.Replace):
            result.extend(resolve_replace(prev_item, item, next_item, junk))
        else:
            result.append(item)
    return result

    
    #for item in diff_result:
    #    if item in rearranged_replaces:
    #        result.extend(rearranged_replaces[item])
    #    else:
    #        result.append(item)
    #return result

def rearrange_replaces(diff_result):
    rearranged_replaces = {}

    # Create index from each insertion.
    inserts_index = {}
    inserts = []
    for diff_item in diff_result:
        if isinstance(diff_item, diff.Replace):
            for item in diff_item.items_target:
                inserts_index.setdefault(item.string, []).append(item)
                item.match = None
            inserts.append(diff_item.items_target)

    # Create index from each insertion
    deletes = []
    for diff_item in diff_result:
        if isinstance(diff_item, diff.Replace):
            for item in diff_item.items_actual:
                item.candidates = inserts_index.get(item.string, [])
                # Make sure, that field 'match' exists.
                item.match = None
                # The run to which the delete belongs to.
                item.run = None
            deletes.append(diff_item.items_actual)

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
        for run_item in run.items:
            run_item.delete_item.run = run

        for run_item in run.items:
            insert_item = run_item.insert_item
            delete_item = run_item.delete_item
            
            if insert_item is not None:
                # The delete_item has a matched insert_item.
                prev_matched_delete_item = delete_item
                
                # First, concat all unmatched deletions (if any) to this 
                # deletion.
                for i, unmatched in enumerate(unmatched_delete_items):
                    unmatched.match = insert_item
                    insert_item.match = unmatched
                unmatched_delete_items = []

                # Then map the actual deleteion to this deletion.
                delete_item.match = insert_item
                insert_item.match = delete_item
            else:
                # The deleteion has no matched deletion.
                if prev_matched_delete_item:
                    # If there was a matched deleteion seen so far, add the 
                    # unmatched deleteion to the this matched deleteion.
                    delete_item.match = prev_matched_delete_item.match
                    prev_matched_delete_item.match.match = delete_item
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

    # Resolve the replaces.
    #for diff_item in diff_result:
    #    if isinstance(diff_item, diff.Replace):
    #        rearranged_replaces[diff_item] = resolve_replace(diff_item)

    #return rearranged_replaces

class Rearr(list):
    pass

class Repl(list):
    pass

def resolve_replace(prev_item, replace, next_item, junk=[]):
    items_actual = replace.items_actual

    i = 0
    phrases = []
    while i < len(items_actual):
        item = items_actual[i]
        if item.run:
            rearr = Rearr()
            while i < len(items_actual) and items_actual[i].run and items_actual[i].run == item.run:
                rearr.append(items_actual[i])
                i += 1
            phrases.append(rearr)
        else:
            repl = Repl()
            while i < len(items_actual) and not items_actual[i].run:
                repl.append(items_actual[i])
                i += 1
            phrases.append(repl)

    result = []
    current_rearrange = None
    current_replace = None
    last_rearrange = None
    for i in range(0, len(phrases)):
        prev_phrase = phrases[i - 1] if i > 0 else None
        phrase = phrases[i]
        next_phrase = phrases[i + 1] if i < len(phrases) - 1 else None

        if isinstance(phrase, Repl):
            # Replace
            if prev_phrase:
                last_target_item = prev_phrase[-1].match
                next_target_item = last_target_item.next
                if util.ignore_item(next_target_item, junk):
                    continue
            elif next_phrase:
                first_target_item = next_phrase[0].match
                prev_target_item = first_target_item.prev
                if util.ignore_item(prev_target_item, junk):
                    continue

            # Create new replace (consider remaining target items!).
            if not current_replace:
                current_replace = diff.Replace([], [], 0, 0)
                result.append(current_replace)
            current_replace.items_actual.extend(phrase)
            current_rearrange = None
        elif isinstance(phrase, Rearr):
            # Rearrange
            if not current_rearrange:
                current_rearrange = Rearrange()
                current_rearrange.items_actual.extend(phrase)
                # TODO: Which target items?
                current_rearrange.items_target.extend([x.match for x in phrase])
                #current_rearrange.run = item.run
                result.append(current_rearrange)
            else:
                if last_rearrange:
                    xxx = last_rearrange[-1].match
                    while True:
                        if xxx and util.ignore_item(xxx.next, junk):
                            xxx = xxx.next
                        else:
                            break

                    if xxx and xxx == phrase[0].match.prev:
                        # Append
                        current_rearrange.items_actual.extend(phrase)
                        # TODO: Which target items?
                        current_rearrange.items_target.extend([x.match for x in phrase])
                    else:
                        current_rearrange = Rearrange()
                        current_rearrange.items_actual.extend(phrase)
                        # TODO: Which target items?
                        current_rearrange.items_target.extend([x.match for x in phrase])
                        #current_rearrange.run = item.run
                        result.append(current_rearrange)

            last_rearrange = phrase

    # Don't forget remianing target items.
    x = [y for y in replace.items_target if not y.match]
    if x:
        if not current_replace:
            current_replace = diff.Replace([], [], 0, 0)
            result.append(current_replace)
        current_replace.items_target.extend(replace.items_target)

    return result

def find_longest_run(delete):
    ''' Finds the longest possible run in the given deletions (list of list of 
        tuples (text, flat_pos)). '''

    if not delete:
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
                      
    for delete_item in delete:
        if delete_item.match:
            # If the delete is already matched, the item shouldn't be a 
            # member of a run anymore.
            continue
            
        insert_item_candidates = delete_item.candidates

        prev_active_runs = active_runs
        active_runs = []
        
        prev_runs_by_end_items = runs_by_end_items
        runs_by_end_items = defaultdict(lambda: set())
        
        prev_end_items_by_runs = end_items_by_runs
        end_items_by_runs = defaultdict(lambda: set())
         
        # Iterate through the inserted positions.
        for insert_item in insert_item_candidates:
            # If the deletion is already matched, the item shouldn't be a 
            # member of a run anymore.
            if insert_item is not None and insert_item.match:
                continue
            
            # Obtain the position of the deletion (could be None).
            pos = None
            if insert_item is not None:
                pos = insert_item.item[1] # the flat position.
            
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

# ==============================================================================

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

# ______________________________________________________________________________

class Rearrange:
    def __init__(self):
        self.items_actual = []
        self.items_target = []
        self.needs_split_ahead_on_para_rearrange = False
        self.needs_split_behind_on_para_rearrange = False

    def __str__(self):
        return "(<> %s, %s)" % (self.items_actual, self.items_target)

    def __repr__(self):
        return self.__str__()#

# ==============================================================================

def has_diff_para(prev_item, item):
    if prev_item and item:
        prev_para = prev_item.item[2][0]
        para = item.item[2][0]
        return para != prev_para
    return True