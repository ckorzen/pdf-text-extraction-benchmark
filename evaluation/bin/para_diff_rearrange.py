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
    rearrange_phrases(diff_phrases)
       
    return resooolve(diff_phrases)
       
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
                inserts_index.setdefault(str(word), []).append(word)
                # Initialize new field "match" that is used to mark this word
                # as matched/unmatched.                
                word.match = None
            inserts.append((diff_phrase, diff_phrase.words_target))

    # Create index from all deletes (phrases in 'actual' which could not be 
    # matched to a phrase in 'target').
    deletes = []
    for diff_phrase in diff_phrases:
        if isinstance(diff_phrase, diff.DiffReplacePhrase):
            for word in diff_phrase.words_actual:
                # Fetch all match candidates and associate them with the word.
                word.candidates = inserts_index.get(str(word), [])
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
                pos = insert_word.wrapped.global_pos # the flat position.
            
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

    #if len(longest_run) > 3: # TODO
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
        
        self.sub_phrases = []
        self.chunks = []
        
        # Need to adapt the pos array.
        if len(words_actual) > 0 and len(words_target) > 0:
            self.pos = list(words_target[0].pos)

    def get_str_pattern(self):
        return "[~ %s, %s]"
        
class DiffRun:
    """ A run that holds a list of matchings between an delete and consecutive
    insertd items."""
    
    def __init__(self, delete):
        """" Creates a new run based on the given delete. """
        self.matched_phrase = None
        self.delete = delete
        self.items = []
        self.is_obsolete = False
        
    def add(self, item):
        """ Adds the given run item to this run. """
        if not self.matched_phrase:
            self.matched_phrase = item.insert_item.phrase
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
        
 
class Chunk:
    """"""
    
    def __init__(self):
        self.words = []
     
    def append(self, word):
        self.words.append(word)
        
    def is_empty(self):
        return len(self.words) == 0   
        
    @property
    def phrase(self):
        for word in self.words:
            match = word.match if word is not None else None
            if match is not None:
                return match.phrase
        return None
    
    @property
    def pos(self):
        for word in self.words:
            match = word.match if word is not None else None
            if match is not None:
                return match.pos
        return None
        
    @property
    def text(self):
        return " ".join(word.text for word in self.words)
            
replace_rearrange_dict = dict()     


        
        
def resooolve(phrases):
    """ Resolves the replace phrases in the given diff phrases."""
    result = []

    # Obtain all replace phrases.
    replaces = [x for x in phrases if isinstance(x, diff.DiffReplacePhrase)] 
    # Obtain all other phrases.
    other    = [x for x in phrases if not isinstance(x, diff.DiffReplacePhrase)] 

    # Resolve the replaces and append it to result list.
    result.extend(resooolve_replaces(replaces))
    # Append all other phrases to result list.
    result.extend(other)
        
    # Sort the result by position.
    result.sort(key=lambda phrase: phrase.pos)
                                 
    return result
        
def resooolve_replaces(replaces):
    replace_rearrange_dict = {}    
 
    for replace in replaces:
        # Find the chunks in actual words of given replace. That are fragments 
        # to rearrange to another replace phrase. A fragment extends until a
        # word is reached that should be rearranged to another phrase.
        # Example:
        # The red fox jumps over the red fox.
        # ^   ^             ^            ^
        # A   A             B            C
        #
        # The words "The", "red", "over" "fox" should be rearranged to the 
        # phrases A/B/C. Other words are unmatched. We will find the chunks
        # "The red fox jums", that is rearranged to A, the chunk "over the red",
        # that is rearranged to B and the chunk "fox", that is rearranged to C.
        chunks = chunkify(replace)
                      
        # Proceed with computed chunks. 
        for chunk in chunks:
            # Only consider chunks which could be matched to another phrase.
            if chunk.phrase is None:
                continue

            # Check, if there is already a rearrange phrase for the phrase.
            rearrange = replace_rearrange_dict.get(chunk.phrase, None)
            if rearrange is None:            
                # Create a new rearrange phrase.
                rearrange = DiffRearrangePhrase([], [])
                replace_rearrange_dict[chunk.phrase] = rearrange
                
                rearrange.pos = chunk.phrase.pos
                rearrange.chunks.append(chunk)
                rearrange.words_target.extend(chunk.phrase.words_target)
                
                # Delete all words of the chunk from the phrase.
                replace.words_actual = [x for x in replace.words_actual if x not in chunk.words]

                chunk.phrase.words_target = []
            else:    
                # Register the chunk in rearrange phrase.
                rearrange.chunks.append(chunk)
                
                # Delete all words of the chunk from the phrase.
                replace.words_actual = [x for x in replace.words_actual if x not in chunk.words]
        
    # Pick up all remaining non-empty replace phrases.
    rearranges = list(replace_rearrange_dict.values())
    replaces = [x for x in replaces if not x.is_empty()]
    
    for rearrange in rearranges:
        # Bring the chunks into correct order.
        rearrange.chunks.sort(key=lambda chunk: chunk.pos)
        # Fill the actual words from chunks.
        rearrange.words_actual = [word for chunk in rearrange.chunks for word in chunk.words]
        
        # Compute sub phrases.
        words_actual = [x.wrapped for x in rearrange.words_actual]
        words_target = [x.wrapped for x in rearrange.words_target]   
        rearrange.sub_phrases = diff.diff(words_actual, words_target)

    return rearranges + replaces  
    
    
def chunkify(replace):
    chunks = []
    chunk  = Chunk()
   
    prev_match_phrase = None
    prev_pos_in_match_phrase = None
                                                                                                            
    for word in replace.words_actual:
        # We need to know if the word should be rearranged to another 
        # phrase than the previous word. 
                
        match = word.match if word is not None else None 
        match_phrase = match.phrase if match is not None else None
        pos_in_match_phrase = match.pos if match is not None else None
                                
        differ_match_phrase = False
        if prev_match_phrase and match_phrase:
            differ_match_phrase = prev_match_phrase != match_phrase
                            
        # Introduce new chunk, if previous and current matched phrase exist
        # and if they differ from each other or if pos differ.
        if match_phrase and not chunk.is_empty():
            chunks.append(chunk)
            chunk = Chunk()
                    
        chunk.append(word)
            
        if match_phrase:
            prev_match_phrase = match_phrase
        if pos_in_match_phrase:
            prev_pos_in_match_phrase = pos_in_match_phrase
      
    if not chunk.is_empty():                                               
        chunks.append(chunk) 
                                
    return chunks         
