import logging
import sys

from wdiff import wdiff

logger = logging.getLogger(__name__)

def evaluate_words_extraction(gt, actual, normalize=True, ignore_cases=True, 
        max_dist=1, rearrange=True):
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
    
    #ignores = []
    junk = ["\[formula\]", "\[\\\cite=(.+)\]", "\[table\]"]
        
    # Do a wdiff (rearrange if necessary).
    commons, insertions, deletions = wdiff(gt, actual, normalize=normalize, 
        ignore_cases=ignore_cases, rearrange=rearrange, max_dist=max_dist, junk=junk)
 
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

    print(evaluate_words_extraction(sys.argv[1], sys.argv[2], ignore_cases=ignore_cases, max_dist=max_dist, rearrange=rearrange))
