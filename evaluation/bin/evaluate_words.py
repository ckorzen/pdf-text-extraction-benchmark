import logging
import sys

#from wdiff import wdiff
from diff import diff

logger = logging.getLogger(__name__)

def evaluate_words_extraction(gt, actual, rearrange=True, ignore_cases=True,
    ignore_whitespaces=False, translates={}, max_dist=1):
    """
    Computes the accuracy  (i.e. the precision and recall) of words extraction.
    Per default, the evaluation is based on exact matches of words, but you
    can set tolerance to 1 to allow to consider words with distance 1 as well.
        
    >>> evaluate_words_extraction("foo bar", "")
    (0.0, 0.0)
    >>> evaluate_words_extraction("", "foo bar")
    (0.0, 0.0)
    >>> evaluate_words_extraction("foo bar", "foo bar")
    (1.0, 1.0)
    >>> evaluate_words_extraction("foo bar", "foo")
    (1.0, 0.5)
    >>> evaluate_words_extraction("foo bar", "foo bar baz xxx")
    (0.5, 1.0)
    >>> evaluate_words_extraction("foo bar", "bar foo")
    (1.0, 1.0)
    >>> evaluate_words_extraction("foo bar", "bar foo", rearrange=False)
    (0.5, 0.5)
    """
      
    gt_words = gt.split()
    actual_words = actual.split()
    translates = {"!,.:;?“”\"'’": " "}
    junk = ["\[formula\]", "\[\\\cite=(.+)\]", "\[table\]"]
    #junk=[]
    
    commons, insertions, deletions = diff(gt_words, actual_words, 
        rearrange=rearrange, ignore_cases=ignore_cases, max_dist=max_dist,
        ignore_whitespaces=ignore_whitespaces, translates=translates, junk=junk)
        
    # Do a wdiff (rearrange if necessary).
    #commons, insertions, deletions = wdiff(gt, actual, normalize=normalize, 
    #    ignore_cases=ignore_cases, rearrange=rearrange, max_dist=max_dist, junk=junk)
 
    # Compute the precision/recall values. 
    tp = sum(len(common) for common in commons)
    fp = sum(len(insertion) for insertion in insertions)
    fn = sum(len(deletion) for deletion in deletions)
        
    precision = tp / (tp + fp) if tp + fp > 0 else 0.0
    recall    = tp / (tp + fn) if tp + fn > 0 else 0.0
                     
    return (precision, recall)   
    
if __name__ == "__main__":
    ignore_cases = sys.argv[3] == "True" if len(sys.argv) > 3 else False
    max_dist = int(sys.argv[4]) if len(sys.argv) > 4 else 0
    rearrange = sys.argv[5] == "True" if len(sys.argv) > 5 else False

    s1 = "The fox and the cow!!"
    s2 = "The cow, and the red FOX"
    evaluate_words_extraction(s1, s2)

