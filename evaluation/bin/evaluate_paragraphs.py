from diff import diff
from util import to_formatted_paragraphs
import util

default_ignore_cases       = True
default_remove_spaces      = True
default_rearrange          = True
default_min_sim            = 0.8
default_remove_whitespaces = True
default_junk               = []

def evaluate_paragraphs_extraction(gt, actual, args):
    """
    Computes precision and recall of paragraph extraction. For that, run diff on 
    the set of paragraphs (gt) and the actual extraction result 
    (actual). The precision of actual follows from the percentage of the number 
    of common paragraphs to the number of extracted paragraphs. The recall 
    follows from the percentage of the number of common paragraphs to the number
    of all paragraphs in the groundtruth.  
    We only want to evaluate the accuracy of paragraphs extraction, but not to 
    evaluate the correct order of extracted paragraphs. Thus, we try to 
    rearrange the paragraphs in the actual result such that the order of 
    paragraphs corresponds to the order in the groundtruth. You can disable the 
    rearrange step by setting the rearrange flag to False. 
    Per default, the evaluation is done case-insensitively. To make it case-
    sensitive, set the ignore_cases flag to False.
    """
    
    ignore_cases  = util.to_bool(args.ignore_cases, default_ignore_cases)
    remove_spaces = util.to_bool(args.remove_spaces, default_remove_spaces)
    rearrange     = util.to_bool(args.rearrange, default_rearrange)
    junk          = util.to_list(args.junk, default_junk)
    min_sim       = util.to_float(args.min_sim, default_min_sim)
    visual_path   = args.visual_path
                           
    return _evaluate_paragraphs_extraction(gt, actual, ignore_cases, 
        remove_spaces, rearrange, junk, min_sim, visual_path)
    
def _evaluate_paragraphs_extraction(gt, actual, 
        ignore_cases=default_ignore_cases, remove_spaces=default_remove_spaces, 
        rearrange=default_rearrange, junk=default_junk, min_sim=default_min_sim,
        visual_path=None):
    """
    Computes precision and recall of paragraph extraction. For that, run diff on 
    the set of paragraphs (gt) and the actual extraction result 
    (actual). The precision of actual follows from the percentage of the number 
    of common paragraphs to the number of extracted paragraphs. The recall 
    follows from the percentage of the number of common paragraphs to the number
    of all paragraphs in the groundtruth.  
    We only want to evaluate the accuracy of paragraphs extraction, but not to 
    evaluate the correct order of extracted paragraphs. Thus, we try to 
    rearrange the paragraphs in the actual result such that the order of 
    paragraphs corresponds to the order in the groundtruth. You can disable the 
    rearrange step by setting the rearrange flag to False. 
    Per default, the evaluation is done case-insensitively. To make it case-
    sensitive, set the ignore_cases flag to False.
        
    >>> _evaluate_paragraphs_extraction("foo \\n\\n bar", "")
    (0.0, 0.0)
    >>> _evaluate_paragraphs_extraction("", "foo \\n\\n bar")
    (0.0, 0.0)
    >>> _evaluate_paragraphs_extraction("foo \\n \\n bar", "foo \\n \\n bar")
    (1.0, 1.0)
    >>> _evaluate_paragraphs_extraction("foo \\n \\n bar", "foo")
    (1.0, 0.5)
    >>> _evaluate_paragraphs_extraction("foo bar", "foo bar \\n \\n baz xxx")
    (0.5, 1.0)
    >>> _evaluate_paragraphs_extraction("foo \\n \\n bar", "bar \\n \\n foo")
    (1.0, 1.0)
    >>> _evaluate_paragraphs_extraction("foo \\n \\n bar", "bar \\n \\n foo", \
            rearrange=False)
    (0.5, 0.5)
    """
       
    gt_paras = to_formatted_paragraphs(gt, ignore_cases, remove_spaces)
    actual_paras = to_formatted_paragraphs(actual, ignore_cases, remove_spaces)
                            
    diff_result = diff(gt_paras, actual_paras,
        rearrange = rearrange,
        junk = junk,
        min_sim = min_sim,
        visual_path = visual_path)
                    
    return util.compute_precision_recall(diff_result)
