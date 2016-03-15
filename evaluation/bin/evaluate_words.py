from diff import diff
import util

default_ignore_cases = True
default_rearrange    = True
default_max_dist     = 0
default_junk         = []

def evaluate_words_extraction(gt, actual, args):
    """
    Computes precision and recall of words extraction. For that, run diff on 
    the set of words of groundtruth (gt) and the actual extraction result 
    (actual). The precision of actual follows from the percentage of the number 
    of common words to the number of extracted words. The recall follows from
    the percentage of the number of common words to the number of all words in
    the groundtruth.  
    We only want to evaluate the accuracy of words extraction, but not to 
    evaluate the correct order of extracted words. Thus, we try tro rearrange 
    the words in the actual result such that the order of words corresponds to 
    the order in the groundtruth. You can disable the rearrange step by setting 
    the rearrange flag to False. 
    Per default, the evaluation is done case-insensitively. To make it case-
    sensitive, set the ignore_cases flag to False.
    Per default, the evaluation is based on exact matches of words. To match 
    words with a defined distance as well, adjust max_dist.
    """

    ignore_cases = util.to_bool(args.ignore_cases, default_ignore_cases)
    rearrange    = util.to_bool(args.rearrange, default_rearrange)
    junk         = util.to_list(args.junk, default_junk)
    max_dist     = util.to_int(args.max_dist, default_max_dist)
    visual_path  = args.visual_path
    
    return _evaluate_words_extraction(gt, actual, 
        ignore_cases, rearrange, junk, max_dist, visual_path)
    
def _evaluate_words_extraction(gt, actual, ignore_cases=default_ignore_cases,
        rearrange=default_rearrange, junk=default_junk, 
        max_dist=default_max_dist, visual_path=None):
    """
    Computes precision and recall of words extraction. For that, run diff on 
    the set of words of groundtruth (gt) and the actual extraction result 
    (actual). The precision of actual follows from the percentage of the number 
    of common words to the number of extracted words. The recall follows from
    the percentage of the number of common words to the number of all words in
    the groundtruth.  
    We only want to evaluate the accuracy of words extraction, but not to 
    evaluate the correct order of extracted words. Thus, we try tro rearrange 
    the words in the actual result such that the order of words corresponds to 
    the order in the groundtruth. You can disable the rearrange step by setting 
    the rearrange flag to False. 
    Per default, the evaluation is done case-insensitively. To make it case-
    sensitive, set the ignore_cases flag to False.
    Per default, the evaluation is based on exact matches of words. To match 
    words with a defined distance as well, adjust max_dist.
        
    >>> _evaluate_words_extraction("foo bar", "")
    (0.0, 0.0)
    >>> _evaluate_words_extraction("", "foo bar")
    (0.0, 0.0)
    >>> _evaluate_words_extraction("foo bar", "foo bar")
    (1.0, 1.0)
    >>> _evaluate_words_extraction("foo bar", "foo")
    (1.0, 0.5)
    >>> _evaluate_words_extraction("foo bar", "foo bar baz xxx")
    (0.5, 1.0)
    >>> _evaluate_words_extraction("foo bar", "bar foo")
    (1.0, 1.0)
    >>> _evaluate_words_extraction("foo bar", "bar foo", rearrange=False)
    (0.5, 0.5)
    """

    gt_words     = util.to_formatted_words(gt, ignore_cases, to_protect=junk)
    actual_words = util.to_formatted_words(actual, ignore_cases, to_protect=junk)

    diff_result  = diff(gt_words, actual_words,
        rearrange = rearrange,
        junk = junk,
        max_dist = max_dist,
        visual_path = visual_path)
   
    return util.compute_precision_recall(diff_result), diff_result
    
