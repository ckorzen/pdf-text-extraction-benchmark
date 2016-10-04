import util
import diff
from collections import defaultdict
from queue import PriorityQueue

# TODO: Refactor!

def rearrange(diff_phrases, junk=[]):
    """ Tries to identify phrases in diff_result, which occur in both, 'actual' 
    and 'target' but are indeed declared as diff.Replace in given diff_result 
    because their order in actual and target doesn't correspond. """

    # Rearrange those phrases, which are declared as diff.Replace.
    rearranged_phrases = rearrange_phrases(diff_phrases)

    # Resolve the replaces.
    result = []
    for i in range(0, len(diff_phrases)):
        prev_phrase = diff_phrases[i - 1] if i > 0 else None
        phrase = diff_phrases[i]
        next_phrase = diff_phrases[i + 1] if i < len(diff_phrases) - 1 else None

        if isinstance(phrase, diff.DiffReplacePhrase):
            resolved = resolve_replace(prev_phrase, phrase, next_phrase, junk)
            
            # Ignore phrases with empty actual words *and* empty target words.
            if resolved is not None:
                for r in resolved:
                    if r is not None and (len(r.words_actual) > 0 or len(r.words_target) > 0):
                        result.append(r)
        else:
            result.append(phrase)
                    
    return result

def rearrange_phrases(diff_phrases):
    """ Tries to identify phrases in diff_result, which occur in both, 'actual' 
    and 'target' but are indeed declared as diff.Replace in given diff_result 
    because their order in actual and target doesn't correspond.
    Replaces such phrases such that their order corresponds. """

    rearranged_phrases = {}

    # Create index from all inserts (phrases in 'target' which could not be 
    # matched to a phrase in 'actual').
    inserts_index = {}
    inserts = []
    for diff_phrase in diff_phrases:
        if isinstance(diff_phrase, diff.DiffReplacePhrase):
            for word in diff_phrase.words_target:
                inserts_index.setdefault(word.text, []).append(word)
                # Initialize new field "match" that is used to mark this word
                # as matched/unmatched.                
                word.match = None
            inserts.append(diff_phrase.words_target)

    # Create index from all deletes (phrases in 'actual' which could not be 
    # matched to a phrase in 'target').
    deletes = []
    for diff_phrase in diff_phrases:
        if isinstance(diff_phrase, diff.DiffReplacePhrase):
            for word in diff_phrase.words_actual:
                # Fetch all match candidates and associate them with the word.
                word.candidates = inserts_index.get(word.text, [])
                # Initialize new field "match" that is used to mark this word
                # as matched/unmatched.
                word.match = None
                word.run = None
            deletes.append(diff_phrase.words_actual)

    # Fill queue with runs.
    runs = PriorityQueue()
    runs_by_words = {}
    for delete in deletes:
        # Compute the longest possible "run". That is the longest (sub)phrase 
        # for which a matching phrase was found.
        run = find_longest_run(delete)
        
        if run:
            # The queue is min-based, so put a negative priority.
            runs.put((-len(run), run))
            
            for item in run.items:
                # Register the run at each involved insert item.                
                runs_by_words.setdefault(item.insert_item, set()).add(run)

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
        prev_matched_delete_word = None
        # If there is no matched deletion yet, add a unmatched deletion 
        # to this queue.
        unmatched_delete_words = []

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
                prev_matched_delete_word = delete_item
                
                # First, concat all unmatched deletions (if any) to this 
                # deletion.
                for i, unmatched in enumerate(unmatched_delete_words):
                    unmatched.match = insert_item
                    insert_item.match = unmatched
                unmatched_delete_words = []

                # Then map the actual deletion to this deletion.
                delete_item.match = insert_item
                insert_item.match = delete_item
            else:
                # The deletion has no matched deletion.
                if prev_matched_delete_word:
                    # If there was a matched deletion seen so far, add the 
                    # unmatched deleteion to the this matched deletion.
                    delete_item.match = prev_matched_delete_word.match
                    prev_matched_delete_word.match.match = delete_item
                else:
                    # Otherwise queue the unmatched deletion.
                    unmatched_delete_words.append(delete_item)
            
            # All runs that includes the insert item are now obsolete.
            obsolete_runs.update(runs_by_words.get(insert_item, set()))

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
                    runs_by_item = runs_by_words.setdefault(insert_item, set())
                    runs_by_item.discard(obsolete_run)
                    runs_by_item.add(new_run)

def resolve_replace(prev_word, replace, next_word, junk=[]):
    """ Resolves the given replace to a sequence of diff.Replace and 
    rearrange.Rearrange objects. """

    # __________________________________________________________________________
    # Identify the words that couldn't be rearranged and all words that could
    # be rearranged in any run. The result is a list of all "unrearranged"
    # words and a list of list of all rearranged words.
    #    
    # simplified example: 
    # Let words_actual = "foo bar baz boo far faz" and lets say "bar baz" and 
    # "boo far" could be rearranged to (different) positions. We compute:
    # 
    # unrearranged_words = ["foo", "faz"] and
    # rearranged_words_lists = [["bar", "baz"], ["boo", "far"]]

    i = 0
    unrearranged_words = []
    rearranged_words_lists = []
    while i < len(replace.words_actual):
        word = replace.words_actual[i]

        if word.run:
            # The word has a run and thus could be rearranged. Proceed as long
            # next words belongs to the same run.
            rearranged_words = [word]
            while i + 1 < len(replace.words_actual):
                next_word = replace.words_actual[i + 1]

                # Abort if next word has no run or belongs to another run.
                if not next_word.run or next_word.run != word.run:
                    break

                rearranged_words.append(next_word)
                i += 1
            # Append the rearranged words to result if it is non-empty.            
            if rearranged_words:         
                rearranged_words_lists.append(rearranged_words)
        else:
            # The word has no run and thus couldn't be rearranged. Add it to 
            # unrearranged words.
            unrearranged_words.append(word)
        i += 1

    # __________________________________________________________________________
    # Split the (unmatched) target words into sequences of junk and non-junk 
    # words. 
    #    
    # simplified example: 
    # Let words_target = "foo bar JUNK1 baz JUNK2" with junk words "JUNK1" and 
    # "JUNK2". We compute:
    # 
    # junk_target_words = ["JUNK1", "JUNK2"] and 
    # non_junk_target_words = ["foo", "bar", "baz"]

    # Obtain all unmatched target words.
    unmatched_target_words = [x for x in replace.words_target if not x.match]

    junk_target_words = []
    non_junk_target_words = []
        
    for i in range(0, len(unmatched_target_words)):
        word = unmatched_target_words[i]

        if util.ignore_word(word, junk):
            # The word is a junk word.
            junk_target_words.append(word)
        else:
            # The word is not a junk word.
            non_junk_target_words.append(word)

    # __________________________________________________________________________
    # Setup the sequence of diff.Replace and rearrange.Rearrange objects.

    result = []
     
    if not junk_target_words:    
        # There are no junk words. So create a single replace object with the
        # unrearranged_words as actual words and the non_junk_target_words as 
        # the target words.        
        replace = diff.DiffReplacePhrase(unrearranged_words, non_junk_target_words)
        result.append(replace)
    else:
        # There is at least one junk word. We will assign each unrearranged 
        # actual word to a junk word. Because the junk words will be ignored 
        # from further process, we can ignore all unrearranged words.
        for i in range(0, len(junk_target_words)):
            replace = diff.DiffReplacePhrase([], [junk_target_words[i]])
            result.append(replace)

        # Create a single replace object that considers the remaining non junk
        # target words.
        if non_junk_target_words:
            replace = diff.DiffReplacePhrase([], non_junk_target_words)
            result.append(replace)

    # Iterate through the rearranged lists and setup the rearrange.Rearrange 
    # objects.
    for i in range(0, len(rearranged_words_lists)):  
        rearrange_words = rearranged_words_lists[i]
        rearrange = DiffRearrangePhrase(rearrange_words, [x.match for x in rearrange_words])
        result.append(rearrange)

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
    runs_by_end_words = defaultdict(lambda: set())
    # The end elements by runs. For the example above, this map looks like:
    # { 1: 5, 2: 5, 3: 5, 4: 7, 5: 7 }
    end_words_by_runs = defaultdict(lambda: set())
    # The queue of unmatched deletes (deletes with no associated deletion).
    unmatched_queue = DiffRun(delete)
                      
    for delete_word in delete:
        if delete_word.match:
            # If the delete is already matched, the word shouldn't be a 
            # member of a run anymore.
            continue
            
        insert_word_candidates = delete_word.candidates

        prev_active_runs = active_runs
        active_runs = []
        
        prev_runs_by_end_words = runs_by_end_words
        runs_by_end_words = defaultdict(lambda: set())
        
        prev_end_words_by_runs = end_words_by_runs
        end_words_by_runs = defaultdict(lambda: set())
         
        # Iterate through the inserted positions.
        for insert_word in insert_word_candidates:
            # If the deletion is already matched, the word shouldn't be a 
            # member of a run anymore.
            if insert_word is not None and insert_word.match:
                continue
            
            # Obtain the position of the deletion (could be None).
            pos = None
            if insert_word is not None:
                pos = insert_word.wrapped[1] # the flat position.
            
            if pos is not None: # There is a matched deletion.
                # Check, if there are runs with end element 'pos-1'
                if pos - 1 in prev_runs_by_end_words:
                    # There are runs that end with 'pos-1'. 
                    run_indices = prev_runs_by_end_words[pos - 1]
                    
                    for run_index in run_indices:
                        # Append the element to the run.
                        run = prev_active_runs[run_index]
                        run.add(DiffRunItem(insert_word, delete_word)) 
                        
                        # Add the run to active runs.
                        active_runs.append(run)
                        new_run_index = len(active_runs) - 1
                        
                        # Check, if the current run is now the longest run. 
                        if len(run) > len(longest_run):
                            longest_run = run
                        
                        # Update the maps
                        runs_by_end_words[pos].add(new_run_index)
                        end_words_by_runs[new_run_index].add(pos)
                else:                   
                    # There is no run that end with 'pos-1'.
                    # Create new run.
                    new_run = unmatched_queue
                    new_run.add(DiffRunItem(insert_word, delete_word))
                                        
                    # Append the run to active runs.
                    active_runs.append(new_run)
                    new_run_index = len(active_runs) - 1
                                              
                    # Check, if the new run is the longest run.     
                    if len(new_run) > len(longest_run):
                        longest_run = new_run
                                
                    # Update the maps.
                    runs_by_end_words[pos].add(new_run_index)
                    end_words_by_runs[new_run_index].add(pos)
                # Clear the queue.
                unmatched_queue = DiffRun(delete)
            else: # There is no matched deletion.
                run_item = DiffRunItem(insert_word, delete_word)
                unmatched_queue.add(run_item)
                
                for j, active_run in enumerate(prev_active_runs):
                    pos = max(prev_end_words_by_runs[j]) + 1
                        
                    # Append the element to run.
                    active_run.append(run_item)
                    active_runs.append(active_run)
                    new_run_index = len(active_runs) - 1
                      
                    runs_by_end_words[pos].add(new_run_index)
                    end_words_by_runs[new_run_index].add(pos)
                                            
                    if len(active_run) > len(longest_run):
                        longest_run = active_run

    #if len(longest_run) > 5: # TODO
    if longest_run:
        return longest_run
    else:
        return unmatched_queue

# ------------------------------------------------------------------------------
# Some util classes.
        
class DiffRearrangePhrase(diff.DiffPhrase):
    """ A phrase of words, that are common in actual and target. """ 
    def __init__(self, words_actual, words_target):
        super(DiffRearrangePhrase, self).__init__(words_actual, words_target)

    def get_str_pattern(self):
        return "[~ %s, %s]"
        
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
