import logging
import sys
import re

from diff import diff

logger = logging.getLogger(__name__)

def evaluate_body_extraction(gt, actual):
    """
    Computes the accuracy (i.e. the precision and recall) of body 
    extraction.
    """
    
    # Do a wdiff (rearrange if necessary).
    gt = re.split("\n\s*\n", gt)
    actual = re.split("\n\s*\n", actual)
        
    # Do a wdiff (rearrange if necessary).
    commons, insertions, deletions = diff(gt, actual, 
        normalize=True, ignore_cases=True, min_similarity=1.0)
 
    # Compute the precision/recall values. 
    tp = sum(len(common) for common in commons)
    fp = sum(len(insertion) for insertion in insertions)
    fn = sum(len(deletion) for deletion in deletions)
        
    precision = tp / (tp + fp) if tp + fp > 0 else 0.0
    recall    = tp / (tp + fn) if tp + fn > 0 else 0.0
               
    return (precision, recall)   
