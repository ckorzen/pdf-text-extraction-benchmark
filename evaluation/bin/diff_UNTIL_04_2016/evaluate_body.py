import diff
import argparse
import logging
import os
import os.path
import util
import operator

from recordclass import recordclass
from datetime import datetime
from evaluator import Evaluator

logging.basicConfig(
    level=logging.DEBUG,
    format='%(asctime)s : %(levelname)s : %(module)s : %(message)s',
)
logger = logging.getLogger(__name__)

class BodyEvaluator(Evaluator):
    ignore_cases = True
    rearrange    = True
    max_dist     = 0
    junk         = []

    def __init__(self, args):
        ''' Creates a new words evaluator with the given args. '''
        self.args = args
        self.current_num_operations = None
        self.all_num_operations = []
        self.current_latest_num_operations = None
        self.all_latest_num_operations = []
        self.current_best_num_operations = None
        self.all_best_num_operations = []
        self.current_diff_result = None
        self.feature = "body"
        
    def deserialize(self, path):
        ''' Deserializes the related results file for the given file/feature-
        pair. Computes (a) the latest precision/recall and (b) the best 
        precision and best recall achieved so far for the actual path and 
        feature.'''

        # Latest num operations.
        latest_num_reorders = 0
        best_num_reorders = 0
        
        if os.path.isfile(path):
            with open(path, "r") as f:
                for line in f:
                    # Each line is of form: time <TAB> precision <TAB> recall
                    fields = line.rstrip().split("\t")
                    
                    num_reorders = int(fields[1])
                    
                    # Update the values.
                    best_num_reorders = min(num_reorders, best_num_reorders)
                    latest_num_reorders = num_reorders
            
            latest = latest_num_reorders
            best   = best_num_reorders
                        
            self.current_latest_num_operations = latest 
            self.all_latest_num_operations.append(latest)
            self.current_best_num_operations = best
            self.all_best_num_operations.append(best)

    def serialize(self, path):
        ''' Serializes the given result to the related results file. '''
        
        result = self.current_num_operations
        
        with open(path, "a") as f:
            # Append the result to file.
            f.write("%s\t%d\n" % (datetime.now(), result)) 

    def recap(self, gt_path, actual_path):
        recap = self.create_recap_string(self.current_latest_num_operations)
        logger.info("Result for %s: %s" % (actual_path, recap))

    def evaluate_by_strings(self, gt_path, gt, actual_path, actual):
        """
        Computes precision and recall of words extraction. For that, run diff 
        on the set of words of groundtruth (gt) and the actual extraction 
        result (actual). The precision of actual follows from the percentage of
        the number of common words to the number of extracted words. The recall 
        follows from the percentage of the number of common words to the number 
        of all words in the groundtruth.  
        We only want to evaluate the accuracy of words extraction, but not to 
        evaluate the correct order of extracted words. Thus, we try tro 
        rearrange the words in the actual result such that the order of words 
        corresponds to the order in the groundtruth. You can disable the 
        rearrange step by setting the rearrange flag to False. 
        Per default, the evaluation is done case-insensitively. To make it 
        case-sensitive, set the ignore_cases flag to False.
        Per default, the evaluation is based on exact matches of words. To 
        match words with a defined distance as well, adjust max_dist.
        """

        args = self.args
        ignore_cases = util.to_bool(args.ignore_cases, self.ignore_cases)
        rearrange    = util.to_bool(args.rearrange, self.rearrange)
        junk         = util.to_list(args.junk, self.junk)
        max_dist     = util.to_int(args.max_dist, self.max_dist)
    
        gt_words     = util.to_formatted_words(gt, ignore_cases, junk)
        actual_words = util.to_formatted_words(actual, ignore_cases, junk)

        diff_result = diff.diff(gt_words, actual_words,
            rearrange = rearrange,
            junk = junk,
            max_dist = max_dist)
        
        self.current_num_operations = diff_result.num_reorders
        self.all_num_operations.append(diff_result.num_reorders)
        self.current_diff_result = diff_result
        
        comparison_string = self.create_comparison_string(
            self.current_num_operations, 
            self.current_latest_num_operations, 
            self.current_best_num_operations)
        logger.info("Result for %s: %s" % (actual_path, comparison_string))

    def finish_evaluation(self):
        if self.args.recap:
            logger.info("Total: avg %s" % self.create_recap_string(
                self.all_latest_num_operations))
        else:
            logger.info("Total: avg %s" % self.create_comparison_string(
                self.all_num_operations,
                self.all_latest_num_operations,
                self.all_best_num_operations))

    def visualize(self, to_path):
        ''' Visualizes the given evaluation result. This will print the content
        of the groundtruth file, where all deletions (parts which were not 
        present in the actual file) are highlighted in red and all insertions
        (parts which were only present in the actual file) are highlighted in 
        green.'''
                
        # Merge all commons, insertions and deletions.
        full = []
        full.extend(self.current_diff_result.commons)
        for replace in self.current_diff_result.replaces: # replaces
            full.extend(replace.inserts)
        full.sort()
            
        # Define the codes to highlight reorders.
        visualization_reorder_start = "\033[30;42m"
        visualization_reorder_end = "\033[0m"
             
        snippets = []
                           
        for i, item in enumerate(full):
            if item.element.rearranged:
                snippets.append(visualization_reorder_start)
            snippets.append("%s" % item.element.string)
            if item.element.rearranged:
                snippets.append(visualization_reorder_end)
            snippets.append(" ")   
                
        # Store the visualization to file.
        visualization_string = "".join(snippets)
        with open(to_path, "w") as f:
            f.write(visualization_string)
     
    def create_comparison_string(self, current, latest, best):
        ''' Returns a string where the current precision/recall values are 
        compared to the latest and the best values. '''
    
        if current and isinstance(current, list):
            # If 'current' is a list, compute p/r by average values.
            reorder = sum(x for x in current) / len(current)
        else:
            reorder = current if current else 0
        
        if latest and isinstance(latest, list):
            # If 'latest' is a list, compute p/r by average values.
            latest_reorder = sum(x for x in latest) / len(latest)
        else:
            latest_reorder = latest if latest else 0
            
        if best and isinstance(best, list):
            # If 'best' is a list, compute p/r by average values.
            best_reorder = sum(x for x in best) / len(best)
        else:
            best_reorder = best if best else 0
        
        # Check, if the current values are the best values achieved so far.
        is_best_reorder = reorder < best_reorder
        
        # Compute the difference to the latest values.
        reorder_delta = reorder - latest_reorder
        
        def bold(text):
            return "\033[1m%s\033[0m" % text
        
        def red(text):
            return "\033[0;31m%+d\033[0m" % text
            
        def green(text):
            return "\033[0;32m%+d\033[0m" % text
        
        # Print values in bold, if they are the best values.
        reorder_str = bold(reorder) if is_best_reorder else reorder
        
        # Format the delta parts.
        reorder_delta_str = green(reorder_delta) if reorder_delta < 0 else red(reorder_delta)
        reorder_delta_str = "±0.0" if reorder_delta == 0 else reorder_delta_str
        
        # Compose the string.   
        parts = []
        parts.append("#reorders: %3s (%s)" % (reorder_str, reorder_delta_str))
        
        return ", ".join(parts)

    def create_recap_string(self, latest):
        ''' Returns a string where the current precision/recall values are 
        compared to the latest and the best values. '''
            
        if latest and isinstance(latest, list):
            # If 'latest' is a list, compute p/r by average values.
            latest_reorder = sum(x for x in latest) / len(latest)
        else:
            latest_reorder = latest if latest else 0.0
            
        # Compose the string.   
        return "#reorders: %s" % latest_reorder

if __name__ == "__main__":
    gt = ["Hello", "World", "how", "are", "you", "XXX"]
    actual = ["World", "Hello"]
    
    diff_result = diff.diff(gt, actual,
        rearrange = True,
        junk = [],
        max_dist = 0)
        
    print(BodyEvaluator({}).count_num_operations(diff_result))
