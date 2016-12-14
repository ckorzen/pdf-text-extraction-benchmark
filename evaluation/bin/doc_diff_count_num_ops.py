from collections import Counter

import diff
import util
import para_diff_rearrange as rearr
import doc_diff_choose_para_or_word as choose

def count_num_ops(evaluation_result, junk=[]):
    """ Counts the num of operations to apply for given phrases. """
    
    all_num_ops = Counter()
    all_num_ops_abs = Counter()
    
    num_paras_target = evaluation_result.get("num_paras_target", 0)
    num_words_actual = evaluation_result.get("num_words_actual", 0)
    diff_phrases = evaluation_result.get("phrases", None)
        
    if diff_phrases:
        # Count the number of operation for each single phrase.
        for phrase in diff_phrases:
            num_ops, num_ops_abs = count_num_ops_phrase(phrase, junk)
            all_num_ops.update(num_ops)
            all_num_ops_abs.update(num_ops_abs)
                
        # Remove zero and negative counts.
        all_num_ops += Counter()                  
        all_num_ops_abs += Counter()                  
    
    # Computes percentages.
    percental_num_ops = {
        "num_para_splits": all_num_ops_abs["num_para_splits_abs"] / num_paras_target,
        "num_para_merges": all_num_ops_abs["num_para_merges_abs"] / num_paras_target,
        "num_para_inserts": all_num_ops_abs["num_para_inserts_abs"] / num_words_actual,
        "num_para_deletes": all_num_ops_abs["num_para_deletes_abs"] / num_words_actual,
        "num_para_rearranges": all_num_ops_abs["num_para_rearranges_abs"] / num_words_actual,
        "num_word_inserts": all_num_ops_abs["num_word_inserts_abs"] / num_words_actual,
        "num_word_deletes": all_num_ops_abs["num_word_deletes_abs"] / num_words_actual,
        "num_word_replaces": all_num_ops_abs["num_word_replaces_abs"] / num_words_actual
    }
              
    return all_num_ops, percental_num_ops 
    
def count_num_ops_phrase(phrase, junk=[]):
    """ Counts the num of operations to apply for given phrase. """

    all_num_ops = Counter()
    all_num_ops_abs = Counter()

    # Check for RearrangePhrase *before* phrase to ignore, because 
    # RearrangePhrase has sub_phrases. The decision, if we have to ignore a 
    # phrase should be make on the sub_phrases, not the rearrange phrase itself
    # (for example, a very long RearrangePhrase could contain a junk word 
    # and hence, the whole phrase would be ignored.
    if isinstance(phrase, rearr.DiffRearrangePhrase):    
        op_type, num_ops, num_ops_abs, vis = choose.apply_rearrange_phrase(phrase, junk)
        all_num_ops.update(num_ops)
        all_num_ops_abs.update(num_ops_abs)
    elif util.ignore_phrase(phrase, junk):
        op_type, num_ops, num_ops_abs, vis = choose.apply_ignored_phrase(phrase)
        all_num_ops.update(num_ops)     
        all_num_ops_abs.update(num_ops_abs)
    elif isinstance(phrase, diff.DiffCommonPhrase):
        op_type, num_ops, num_ops_abs, vis = choose.apply_common_phrase(phrase)
        all_num_ops.update(num_ops)
        all_num_ops_abs.update(num_ops_abs)
    elif isinstance(phrase, diff.DiffReplacePhrase):
        op_type, num_ops, num_ops_abs, vis = choose.apply_replace_phrase(phrase)
        all_num_ops.update(num_ops)
        all_num_ops_abs.update(num_ops_abs)
        
    return all_num_ops, all_num_ops_abs
