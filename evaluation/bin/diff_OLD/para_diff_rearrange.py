import util
import diff
from collections import defaultdict
from queue import PriorityQueue

def rearrange(diff_result, junk=[]):
    """ Tries to identify phrases in diff_result, which occur in both, 'actual' 
    and 'target' but are indeed declared as diff.Replace in given diff_result 
    because their order in actual and target doesn't correspond.
    'diff_result' is a list of diff.Common and diff.Replace objects denoting
    the operations to perform to transform actual into target. """

    # Rearrange those phrases, which are declared as diff.Replace.
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

def rearrange_replaces(diff_result):
    """ Tries to identify phrases in diff_result, which occur in both, 'actual' 
    and 'target' but are indeed declared as diff.Replace in given diff_result 
    because their order in actual and target doesn't correspond.
    Replaces such phrases such that their order corresponds. """

    rearranged_replaces = {}

    # Create index from all inserts (phrases in 'target' which could not be 
    # matched to a phrase in 'actual').
    inserts_index = {}
    inserts = []
    for diff_item in diff_result:
        if isinstance(diff_item, diff.Replace):
            for item in diff_item.items_target:
                inserts_index.setdefault(item.string, []).append(item)
                # Initialize new field "match" that is used to mark this item
                # as matched/unmatched.                
                item.match = None
            inserts.append(diff_item.items_target)

    # Create index from all deletes (phrases in 'actual' which could not be 
    # matched to a phrase in 'target').
    deletes = []
    for diff_item in diff_result:
        if isinstance(diff_item, diff.Replace):
            for item in diff_item.items_actual:
                # Fetch all match candidates and associate them with the item.
                item.candidates = inserts_index.get(item.string, [])
                # Initialize new field "match" that is used to mark this item
                # as matched/unmatched.
                item.match = None
                item.run = None
            deletes.append(diff_item.items_actual)

    # Fill queue with runs.
    runs = PriorityQueue()
    runs_by_items = {}
    for delete in deletes:
        # Compute the longest possible "run". That is the longest (sub)phrase 
        # for which a matching phrase was found.
        run = find_longest_run(delete)
        
        if run:
            # The queue is min-based, so put a negative priority.
            runs.put((-len(run), run))
            
            for item in run.items:
                # Register the run at each involved insert item.                
                runs_by_items.setdefault(item.insert_item, set()).add(run)

    # Process the queue of runs.
    while not runs.empty():
        # Fetch the longest run (the longest subphrase that could be matched).
        priority, run = runs.get()

        # The run could be empty or obsolete. A run is obsolete if an insert 
        # item that is a match member of the run was matched to an delete item 
        # of another run in the meanwhile. 
        if not run or run.is_obsolete:
            continue

        # There may be delete items which could not be matched to an insert 
        # item. Hence we do not know where to put such deletions.
        # If there is such deletion within a run, concat it with a 
        # preceding or succeeding matched deletion.
        
        # The previous delete_item that could be matched.
        prev_matched_delete_item = None
        # If there is no matched deletion yet, add a unmatched deletion 
        # to this queue.
        unmatched_delete_items = []

        # Keep track of obsolete runs.
        obsolete_runs = set()

        for run_item in run.items:
            # Register the run at each involved delete item.                
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

                # Then map the actual deletion to this deletion.
                delete_item.match = insert_item
                insert_item.match = delete_item
            else:
                # The deletion has no matched deletion.
                if prev_matched_delete_item:
                    # If there was a matched deletion seen so far, add the 
                    # unmatched deleteion to the this matched deletion.
                    delete_item.match = prev_matched_delete_item.match
                    prev_matched_delete_item.match.match = delete_item
                else:
                    # Otherwise queue the unmatched deletion.
                    unmatched_delete_items.append(delete_item)
            
            # All runs that includes the insert item are now obsolete.
            obsolete_runs.update(runs_by_items.get(insert_item, set()))

        # Recompute all obsolete runs.
        for obsolete_run in obsolete_runs:
            # Mark the run as obsolete such that it won't be considered anymore.            
            obsolete_run.is_obsolete = True
            # Compute new run.
            new_run = find_longest_run(obsolete_run.delete)
            
            if new_run:
                runs.put((-len(new_run), new_run))
                    
                for item in new_run.items:
                    insert_item = item.insert_item
                    # Register new run and unregister obsolete run.
                    runs_by_item = runs_by_items.setdefault(insert_item, set())
                    runs_by_item.discard(obsolete_run)
                    runs_by_item.add(new_run)

def resolve_replace(prev_item, replace, next_item, junk=[]):
    """ Resolves the given replace to a sequence of diff.Replace and 
    rearrange.Rearrange objects. """

    # __________________________________________________________________________
    # Identify the items that couldn't be rearranged and all items that could
    # be rearranged in any run. The result is a list of all "unrearranged"
    # items and a list of list of all rearranged items.
    #    
    # simplified example: 
    # Let items_actual = "foo bar baz boo far faz" and lets say "bar baz" and 
    # "boo far" could be rearranged to (different) positions. We compute:
    # 
    # unrearranged_items = ["foo", "faz"] and
    # rearranged_items_lists = [["bar", "baz"], ["boo", "far"]]

    i = 0
    unrearranged_items = []
    rearranged_items_lists = []
    while i < len(replace.items_actual):
        item = replace.items_actual[i]

        if item.run:
            # The item has a run and thus could be rearranged. Proceed as long
            # next items belongs to the same run.
            rearranged_items = [item]
            while i + 1 < len(replace.items_actual):
                next_item = replace.items_actual[i + 1]

                # Abort if next item has no run or belongs to another run.
                if not next_item.run or next_item.run != item.run:
                    break

                rearranged_items.append(next_item)
                i += 1
            # Append the rearranged items to result if it is non-empty.            
            if rearranged_items:         
                rearranged_items_lists.append(rearranged_items)
        else:
            # The item has no run and thus couldn't be rearranged. Add it to 
            # unrearranged items.
            unrearranged_items.append(item)
        i += 1

    # __________________________________________________________________________
    # Split the (unmatched) target items into sequences of junk and non-junk 
    # items. 
    #    
    # simplified example: 
    # Let items_target = "foo bar JUNK1 baz JUNK2" with junk words "JUNK1" and 
    # "JUNK2". We compute:
    # 
    # junk_target_items = ["JUNK1", "JUNK2"] and 
    # non_junk_target_items = ["foo", "bar", "baz"]

    # Obtain all unmatched target items.
    unmatched_target_items = [x for x in replace.items_target if not x.match]

    junk_target_items = []
    non_junk_target_items = []
        
    for i in range(0, len(unmatched_target_items)):
        item = unmatched_target_items[i]

        if util.ignore_item(item, junk):
            # The item is a junk item.
            junk_target_items.append(item)
        else:
            # The item is not a junk item.
            non_junk_target_items.append(item)

    # __________________________________________________________________________
    # Setup the sequence of diff.Replace and rearrange.Rearrange objects.

    result = []
     
    if not junk_target_items:    
        # There are no junk items. So create a single replace object with the
        # unrearranged_items as actual items and the non_junk_target_items as 
        # the target items.        
        replace = diff.Replace([], [], 0, 0)
        replace.items_actual.extend(unrearranged_items)          
        replace.items_target.extend(non_junk_target_items)
        result.append(replace)
    else:
        # There is at least one junk item. We will assign each unrearranged 
        # actual item to a junk item. Because the junk items will be ignored 
        # from further process, we can ignore all unrearranged items.
        for i in range(0, len(junk_target_items)):
            replace = diff.Replace([], [], 0, 0)
            replace.items_target.append(junk_target_items[i])
            result.append(replace)

        # Create a single replace object that considers the remaining non junk
        # target items.
        if non_junk_target_items:
            replace = diff.Replace([], [], 0, 0)
            replace.items_target.extend(non_junk_target_items)
            result.append(replace)

    # Iterate through the rearranged lists and setup the rearrange.Rearrange 
    # objects.
    for i in range(0, len(rearranged_items_lists)):  
        rearrange_items = rearranged_items_lists[i]
        rearrange = Rearrange()
        rearrange.items_actual.extend(rearrange_items)
        # Setup the target items (that are the matched items).            
        rearrange.items_target.extend([x.match for x in rearrange_items])
        result.append(rearrange)

    return result

# TODO: Refactor!
def resolve_replace2(prev_item, replace, next_item, junk=[]):
    items_actual = replace.items_actual

    i = 0
    phrases = []

    # For a replace, distinguish between (sub)phrases that could be rearranged 
    # and that couldn't be rearranged.
    # (simplified) example: Let 'items_actual' = "foo bar baz boo" and say the 
    # phrase "bar baz" could be rearranged and the phrase "boo" could be 
    # rearranged (to another position).
    # So, compute the phrases ["foo", "bar baz", "boo"].
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

    # Setup the diff.Replace and diff.Rearrange objects.


    result = []
    current_rearrange = None
    current_replace = None
    last_rearrange = None
    for i in range(0, len(phrases)):
        prev_phrase = phrases[i - 1] if i > 0 else None
        phrase = phrases[i]
        next_phrase = phrases[i + 1] if i < len(phrases) - 1 else None

        if isinstance(phrase, Repl):
            # Process the phrases that couldn't be rearranged:
            if prev_phrase:
                # prev_phrase is definitely a rearrange!
                # Check if the rearrange position of prev phrase is followed by
                # a junk item.
                last_target_item = prev_phrase[-1].match
                next_target_item = last_target_item.next
                if util.ignore_item(next_target_item, junk):
                    continue
            elif next_phrase:
                # next_phrase is definitely a rearrange!
                # Check if the rearrange position of prev phrase is preceded by
                # a junk item.
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

    # Don't forget remaining target items.
    x = [y for y in replace.items_target if not y.match]
    if x:
        if not current_replace:
            current_replace = diff.Replace([], [], 0, 0)
            result.append(current_replace)
        current_replace.items_target.extend(replace.items_target)

    return result

def find_longest_run(delete):
    """ Computes the longest possible "run" (subphrase) of given delete that 
    could be matched to insertion position. """

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

    if len(longest_run) > 5: # TODO
    #if longest_run:
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
