from diff import diff
import util

default_ignore_cases       = True
default_max_dist           = 0
default_junk               = []

def evaluate_body_extraction(gt, actual, args):
    """
    Evaluates body extraction. For that, run diff on the set of groundtruth 
    words (gt) and the words of the actual extraction result (actual), without
    rearranging (because the order of words should matter). Based on the diff 
    result, computes the edit distance based on the words).   
    """
    
    ignore_cases = util.to_bool(args.ignore_cases, default_ignore_cases)
    junk         = util.to_list(args.junk, default_junk)
    max_dist     = util.to_int(args.max_dist, default_max_dist)
    
    return _evaluate_body_extraction(gt, actual, ignore_cases, junk, max_dist)

def _evaluate_body_extraction(gt, actual, ignore_cases=default_ignore_cases, 
        junk=default_junk, max_dist=default_max_dist, visual_path=None):
    """
    Computes distance and similarity of body extraction. For that, run diff on 
    the set of words (gt) and the actual extraction result 
    (actual). 
    
    >>> _evaluate_body_extraction("foo bar", "")
    (2, 0.0)
    >>> _evaluate_body_extraction("", "foo bar")
    (2, 0.0)
    >>> _evaluate_body_extraction("foo bar", "foo bar")
    (0, 1.0)
    >>> _evaluate_body_extraction("foo bar", "foo")
    (1, 0.5)
    >>> _evaluate_body_extraction("foo bar", "foo bar baz xxx")
    (2, 0.5)
    >>> _evaluate_body_extraction("foo bar", "bar foo")
    (2, 0.0)
    """
    gt_words     = util.to_formatted_words(gt, ignore_cases, junk)
    actual_words = util.to_formatted_words(actual, ignore_cases, junk)
    
    diff_result = diff(gt_words, actual_words, 
        rearrange=False,
        max_dist=max_dist,
        junk=junk)
                
    return util.compute_distance_similarity(diff_result), diff_result
