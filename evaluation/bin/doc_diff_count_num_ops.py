from collections import Counter

import diff
import util
import para_diff_rearrange as rearr
import doc_diff_choose_para_or_word as choose

def count_num_ops(diff_phrases, junk=[]):
    """ Counts the num of operations to apply for given phrases. """
    
    counter = Counter()
    
    if diff_phrases:
        # Count the number of operation for each single phrase.
        for phrase in diff_phrases: 
            counter.update(count_num_ops_phrase(phrase, junk))
                
        # Remove zero and negative counts.
        counter += Counter()                  
     
    return counter 
    
def count_num_ops_phrase(phrase, junk=[]):
    """ Counts the num of operations to apply for given phrase. """

    counter = Counter()

    if util.ignore_phrase(phrase, junk):
        num_ops, vis = choose.apply_ignored_phrase(phrase)
        counter.update(num_ops)     
    elif isinstance(phrase, diff.DiffCommonPhrase):
        num_ops, vis = choose.apply_common_phrase(phrase)
        counter.update(num_ops)
    elif isinstance(phrase, diff.DiffReplacePhrase):
        num_ops, vis = choose.apply_replace_phrase(phrase)
        counter.update(num_ops)
    elif isinstance(phrase, rearr.DiffRearrangePhrase):
        num_ops, vis = choose.apply_rearrange_phrase(phrase)
        counter.update(num_ops)
        
    return counter
