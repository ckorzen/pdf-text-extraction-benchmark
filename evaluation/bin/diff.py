from collections import defaultdict
from recordclass import recordclass
from fuzzy_dict import fuzzy_dict
from lis2 import longest_increasing_continuous_subsequences_with_gaps

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

MappingItem     = recordclass('MappingItem','positions insertion')
RunItem         = recordclass('RunItem', 'deletion insertion')

# ______________________________________________________________________________
#

def diff(list1, list2, rearrange=False, normalize=False, ignore_cases=False, 
            ignore_whitespaces=False, max_distance=0, min_similarity=1.0):
    '''
    Finds the differences between the two given lists of strings. Normalizes
    the strings if the normalize flag is set to True. Ignores cases if the 
    ignore_cases flag is set to True. Allows fuzzy matches if max_distance or
    min_similarity is given. 
    Returns a list of tuples of type DiffCommonItem, a list of tuples of type 
    DiffDeleteItem and a list of tuples of type DiffInsertItem. Each common, 
    insertion and deletion is represented by the related positions in the two 
    lists and the associated element from one of the two lists.
       
    * Inspired by the simplediff lib by Paul Butler 
    (see https://github.com/paulgb/simplediff/) *  
    '''
    
    logger.debug("Diff. rearrange: %r, normalize: %r, ignore_cases: %r, "
        "ignore_whitespaces: %r, max_distance: %r, min_similarity: %r" % 
        (rearrange, normalize, ignore_cases, ignore_whitespaces, max_distance, 
        min_similarity))
        
    logger.debug("input 1: %r" % " ".join(list1))
    logger.debug("input 2: %r" % " ".join(list2))
        
    if rearrange:
        # Rearrange the elements in list2.
        list2 = rearrange_elements(list1, list2, normalize=normalize, 
            ignore_cases=ignore_cases, ignore_whitespaces=ignore_whitespaces, 
            max_dist=max_distance, min_sim=min_similarity)
     
    # Do the diff.   
    commons, inserts, deletes = [], [], []
    _diff(list1, list2, 0, 0, commons, inserts, deletes, 
        normalize=normalize, ignore_cases=ignore_cases, 
        max_distance=max_distance, min_similarity=min_similarity)
       
    logger.debug("visualization: %s" % 
        visualize_wdiff_result(commons, inserts, deletes)) 
                
    return commons, inserts, deletes
   
def _diff(list1, list2, pos1, pos2, commons, inserts, deletes, normalize=False, 
            ignore_cases=False, ignore_whitespaces=False, 
            max_distance=0, min_similarity=1.0):
    '''
    Finds the differences between the two given lists. Fills the given lists of
    commons, insertions and deletions. pos1 and pos2 are the current positions
    in list1 and list2.
    '''

    # Create an index from values in list1, where each value is mapped to its 
    # position in list1.
    list1_index = fuzzy_dict()  
    for i, item in enumerate(list1):
        string = to_formatted_string(item, normalize, ignore_cases)
        if string in list1_index:
            list1_index[string].append(i)
        else:
            list1_index[string] = [i]

    # Find the largest substring common to list1 and list2.
    # 
    # We iterate over each value in in2. At each iteration, overlap[i] is the
    # length of the largest suffix of list1[:i] equal to a suffix of 
    # list2[:index2] (or unset when list1[i] != list2[index2]).
    #
    # At each stage of iteration, the new overlap (called _overlap until 
    # the original overlap is no longer needed) is built from list1.
    #
    # If the length of overlap exceeds the largest substring
    # seen so far (length), we update the largest substring
    # to the overlapping strings.

    overlap = defaultdict(lambda: 0)
    # start_list1 is the index of the beginning of the largest overlapping
    # substring in list1. start_list2 is the index of the beginning of the 
    # same substring in list2. length is the length that overlaps in both.
    # These track the largest overlapping substring seen so far, so naturally
    # we start with a 0-length substring.
    start_list1 = 0
    start_list2 = 0
    length = 0

    for index2, item in enumerate(list2):
        string = to_formatted_string(item, normalize, ignore_cases)
        _overlap = defaultdict(lambda: 0)
        for index1 in list1_index.get(string, max_distance, min_similarity, []):
            # now we are considering all values of index1 such that
            # list1[index1] == list2[index2].
            _overlap[index1] = (index1 and overlap[index1 - 1]) + 1
            if _overlap[index1] > length:
                # this is the largest substring seen so far, so store its
                # indices
                length = _overlap[index1]
                start_list1 = index1 - length + 1
                start_list2 = index2 - length + 1
        overlap = _overlap

    if length == 0:
        # If no common substring is found, we return an insert and delete...
        for item in list1:
            deletes.append(DiffDeleteItem(pos1, pos2, item, False))
            pos1 += 1
        for item in list2:
            inserts.append(DiffInsertItem(pos1, pos2, item, False))
            pos2 += 1
    else:
        # ...otherwise, the common substring is unchanged and we recursively
        # diff the text before and after that substring
        l1 = list1[ : start_list1]
        l2 = list2[ : start_list2]
        pos1, pos2 = _diff(l1, l2, pos1, pos2, commons, inserts, deletes)
        
        for item in list2[start_list2 : start_list2 + length]:
            commons.append(DiffCommonItem(pos1, pos2, item))
            pos1 += 1
            pos2 += 1
        
        r1 = list1[start_list1 + length : ]
        r2 = list2[start_list2 + length : ]
        pos1, pos2 = _diff(r1, r2, pos1, pos2, commons, inserts, deletes)
        
    return (pos1, pos2)

# ______________________________________________________________________________
# Rearrange

def rearrange_elements(groundtruth, actual, 
        normalize=False, ignore_cases=False, ignore_whitespaces=False, 
        max_dist=0, min_sim=1.0):
    ''' Tries to rearrange the elements in actual such that its order 
    corresponds to the order in groundtruth. Transforms all letters to 
    lowercase letters if the ignore_cases flag is True. Normalizes the inputs 
    (=removes punctuation marks) if the normalize flag is True. Ignores all 
    whitespaces if the ignore_whitespaces flag is set to True. Allows fuzzy 
    words matching if max_distance or min_similarity is given.'''
    
    logger.debug("Rearrange elements. normalize: %r, ignore_cases: %r, "
        "ignore_whitespaces: %r, max_distance: %r, min_similarity: %r" % 
        (normalize, ignore_cases, ignore_whitespaces, max_dist, min_sim))
    
    # Do a diff. Will return a list of common elements, a list of inserted 
    # elements and a list of deleted elements. Each list is of form:
    # [(pos_in_gt1, pos_in_actual1, word1), (pos_in_gt2, pos_in_actual2, word2)]
    commons, inserts, deletes = diff(groundtruth, actual, rearrange=False, 
        normalize=normalize, ignore_cases=ignore_cases, 
        ignore_whitespaces=ignore_whitespaces, max_distance=max_dist, 
        min_similarity=min_sim)
             
    # Map each deleted element to all deleted items that holds the element.
    # { element1: [item1, item2], element2: [...], ... }
    deletes_index = fuzzy_dict()
    for item in deletes:
        if item.element in deletes_index:
            deletes_index[item.element].append(item)
        else:
            deletes_index[item.element] = [item]
                  
    # Associate each inserted element to its positions in the deletes_index:
    # [(positions1, element1), (positions2, element2), ...]
    inserts_mappings = []
    for item in inserts:
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
    
        positions = deletes_index.get(item.element, max_dist, min_sim, [None])
        inserts_mappings.append(MappingItem(positions, item))
                  
    # Compute runs as long as there are any. A run is a continuously increasing
    # sequence of elements.          
    run = find_run(inserts_mappings)
    while run:
        logger.debug("run: %s", visualize_run(run))
    
        last_mapped_insertion = None
        unmapped_insertions = []
        for mapping in run:
            deletion, insertion = mapping
            
            if deletion is not None:
                # There is a mapped deletion.
                # First, map all unmapped elements to this deletion (if any)
                for i, unmapped in enumerate(unmapped_insertions):
                    unmapped.pos1 = deletion.pos1
                    unmapped.pos2 = deletion.pos2 + i
                    unmapped.matched = True

                # Then map the actual insertion to the deletion.
                insertion.pos1 = deletion.pos1
                insertion.pos2 = deletion.pos2 + len(unmapped_insertions)            
                last_mapped_insertion = insertion
                unmapped_insertions = []
                deletion.matched = insertion.matched = True
            else:
                # There is no mapped deletion.
                if last_mapped_insertion:
                    # Add the insertion to the last matched insertion.
                    insertion.pos1 = last_mapped_insertion.pos1
                    insertion.pos2 = last_mapped_insertion.pos2 + 1
                    last_mapped_insertion = insertion
                else:
                    unmapped_insertions.append(insertion)
                insertion.matched = True  

        run = find_run(inserts_mappings)
            
    result = commons + inserts
    result.sort()
        
    result = [item.element for item in result]
        
    logger.debug("rearranged to: %s", " ".join(result))
                  
    return result

def find_run(elements):
    """ 
    Given a list of tuples of form ([positions], word). Returns a longest run,
    i.e. a sequence of increasing positions, where the order of words are kept. 
    
    Example: 
    Given the tuples [([1, 3, 7], "foo"), ([2, 4, 5], "bar"), ([3, 4], "baz")]. 
    A longest run would be [(1, "foo"), (2, "bar"), (3, "baz")].
    """  
        
    if not elements:
        return
              
    # Flatten the elements.
    templates = []
    template = []          
    last_item = None
    for element in elements:           
        positions, item = element            

        if item.matched:
            # If the item is already matched, the item shouldn't be a member of 
            # a run anymore.
            continue
         
        # Seperate the individual insertions.
        if last_item and last_item.pos1 != item.pos1:
            templates.append(template)
            template = []
          
        # Insert the positions in reverse order into the single list x. This 
        # guarantuees, that no two elements are chosen from a single list.      
        for pos in reversed(positions):
            if pos is None:
                template.append(RunItem(None, item))                
            elif not pos.matched:
                template.append(RunItem(pos, item))                
        last_item = item
    templates.append(template)
        
    if not template:
        return
        
    return longest_increasing_continuous_subsequences_with_gaps(template)
                    
# ______________________________________________________________________________
# Util methods.
 
def as_list(x):
    if isinstance(x, list):
        return x
    else:
        return [x]
 
def to_formatted_string(element, normalize=False, ignore_cases=False, 
        ignore_whitespaces=False):
    ''' 
    Formats the given string. Removes all punctuation marks if the normalize 
    flag is True. Transforms the string to lowercase letters if the ignore_cases
    flag is True.
    '''
    
    # Make sure, that the element to process is a string.
    string = str(element)
                       
    # Unicode can hold "decomposed" characters, i.e. characters with accents 
    # where the accents are characters on its own (for example, the character 
    # "ä" are then two characters: "a" and the two dots.
    # Try to compose these characters using unicodedata.  
    codepoints = [ord(i) for i in string]
             
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
        # Remove all punctuations marks. TODO Which punctuation marks?
        txt = txt.translate({ord(c): " " for c in "!,.:;?“”\"'’"})                
    if ignore_cases:
        # transform the string to lowercase letters.
        txt = txt.lower()
        
    if ignore_whitespaces:
        return "".join(txt.split())
    else:    
        return " ".join(txt.split())

def visualize_run(run):
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
   
def visualize_wdiff_result(commons, inserts, deletes, path=None):
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
    s2 = "The red fox [3] [4] [5] over jumps over the blue sea"
    diff(s1.split(), s2.split(), rearrange=True)
