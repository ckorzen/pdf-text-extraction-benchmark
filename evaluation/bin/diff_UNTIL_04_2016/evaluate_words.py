import diff
import argparse
import logging
import os
import os.path
import util
import operator

from diff_new import Diff
from diff_new import DiffCommon
from diff_new import DiffReplace
from recordclass import recordclass
from datetime import datetime
from evaluator import Evaluator

logging.basicConfig(
    level=logging.DEBUG,
    format='%(asctime)s : %(levelname)s : %(module)s : %(message)s',
)
logger = logging.getLogger(__name__)

class WordsEvaluator(Evaluator):
    ignore_cases = True
    rearrange    = True
    max_dist     = 0
    junk         = []

    def __init__(self, args):
        ''' Creates a new words evaluator with the given args. '''
        self.args = args
        self.current_precision_recall = None
        self.all_precision_recall_values = []
        self.current_latest_precision_recall = None
        self.all_latest_precision_recall_values = []
        self.current_best_precision_recall = None
        self.all_best_precision_recall_values = []
        self.current_diff_result = None
        self.feature = "word"
        
    def deserialize(self, path):
        ''' Deserializes the related results file for the given file/feature-
        pair. Computes (a) the latest precision/recall and (b) the best 
        precision and best recall achieved so far for the actual path and 
        feature.'''

        # Latest precision and recall.
        latest_p, latest_r = 0.0, 0.0                
        # Best precision and recall.
        best_p, best_r = 0.0, 0.0
        
        if os.path.isfile(path):
            with open(path, "r") as f:
                for line in f:
                    # Each line is of form: time <TAB> precision <TAB> recall
                    fields = line.rstrip().split("\t")
                      
                    precision = float(fields[1])
                    recall = float(fields[2])
                    
                    # Update the values.
                    best_p   = max(precision, best_p)
                    best_r   = max(recall, best_r)
                    latest_p = precision
                    latest_r = recall
            
            latest = (latest_p, latest_r)
            best = (best_p, best_r)
                        
            self.current_latest_precision_recall = latest 
            self.all_latest_precision_recall_values.append(latest)
            self.current_best_precision_recall = best
            self.all_best_precision_recall_values.append(best)

    def serialize(self, path):
        ''' Serializes the given result to the related results file. '''
        
        result = self.current_precision_recall
        
        with open(path, "a") as f:
            # Append the result to file.
            f.write("%s\t%f\t%f\n" % (datetime.now(), result[0], result[1])) 

    def recap(self, gt_path, actual_path):
        recap = self.create_recap_string(self.current_latest_precision_recall)
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
    
        gt_words     = util.to_formatted_words_by_paragraph(gt, ignore_cases, junk)
        actual_words = util.to_formatted_words_by_paragraph(actual, ignore_cases, junk)

        #diff_result = diff.diff(gt_words, actual_words,
        #    rearrange = rearrange,
        #    junk = junk,
        #    max_dist = max_dist)
        
        diff = Diff(gt_words, actual_words)
        diff_result = diff.run(rearrange=True)
        
        p_r = util.compute_precision_recall(diff_result, junk)
        
        self.current_precision_recall = p_r
        self.all_precision_recall_values.append(p_r)
        self.current_diff_result = diff_result
        
        comparison_string = self.create_comparison_string(
            self.current_precision_recall, 
            self.current_latest_precision_recall, 
            self.current_best_precision_recall)
        logger.info("Result for %s: %s" % (actual_path, comparison_string))

    def finish_evaluation(self):
        if self.args.recap:
            logger.info("Total: %s" % self.create_recap_string(
                self.all_latest_precision_recall_values))
        else:
            logger.info("Total: %s" % self.create_comparison_string(
                self.all_precision_recall_values,
                self.all_latest_precision_recall_values,
                self.all_best_precision_recall_values))

    def visualize(self, to_path):
        ''' Visualizes the given evaluation result. This will print the content
        of the groundtruth file, where all deletions (parts which were not 
        present in the actual file) are highlighted in red and all insertions
        (parts which were only present in the actual file) are highlighted in 
        green.'''
        
        junk = util.to_list(self.args.junk, self.junk)
        
        # Merge all commons, insertions and deletions.
        diff_result = self.current_diff_result
        full = diff_result.commons + diff_result.replaces
        full.sort(key = lambda x: x.pos)
            
        # Define the codes to highlight deletions and insertions.
        visualization_delete_start = "\033[30;41m"
        visualization_delete_end = "\033[0m"
        visualization_insert_start = "\033[30;42m"
        visualization_insert_end = "\033[0m"
             
        snippets = []
        
        for i, item in enumerate(full):
            if isinstance(item, DiffReplace):
                if util.ignore(item, junk):
                    continue
                
                for delete_item in item.delete.items:
                    snippets.append(visualization_delete_start)
                    snippets.append(delete_item.source.string)
                    snippets.append(visualization_delete_end)
                    snippets.append(" ")
                for insert_item in item.insert.items:
                    snippets.append(visualization_insert_start)
                    snippets.append(insert_item.source.string)
                    snippets.append(visualization_insert_end)
                    snippets.append(" ")
            elif isinstance(item, DiffCommon):
                for common_item in item.items:
                    snippets.append(common_item.source.string)
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
            p = sum(x[0] for x in current) / len(current)
            r = sum(x[1] for x in current) / len(current)
        else:
            p = current[0] if current else 0.0
            r = current[1] if current else 0.0
        
        if latest and isinstance(latest, list):
            # If 'latest' is a list, compute p/r by average values.
            latest_p = sum(x[0] for x in latest) / len(latest)
            latest_r = sum(x[1] for x in latest) / len(latest)
        else:
            latest_p = latest[0] if latest else 0.0
            latest_r = latest[1] if latest else 0.0
            
        if best and isinstance(best, list):
            # If 'best' is a list, compute p/r by average values.
            best_p = sum(x[0] for x in best) / len(best)
            best_r = sum(x[1] for x in best) / len(best)
        else:
            best_p = best[0] if best else 0.0
            best_r = best[1] if best else 0.0
        
        # Check, if the current p/r are the best values achieved so far.
        is_best_p = p > best_p
        is_best_r = r > best_r
        # Compute the difference to the latest values.
        p_delta   = p - latest_p
        r_delta   = r - latest_r
        
        def bold(text):
            return "\033[1m%s\033[0m" % text
        
        def red(text):
            return "\033[0;31m%+.4f\033[0m" % text
            
        def green(text):
            return "\033[0;32m%+.4f\033[0m" % text
        
        # Print p/r in bold, if they are the best values.
        p_str = bold(p) if is_best_p else p 
        r_str = bold(r) if is_best_r else r
        
        # Format the delta parts.
        p_delta_str = red(p_delta) if p_delta < 0 else green(p_delta)
        p_delta_str = "±0.0" if p_delta == 0 else p_delta_str
        r_delta_str = red(r_delta) if r_delta < 0 else green(r_delta)
        r_delta_str = "±0.0" if r_delta == 0 else r_delta_str
                      
        # Compose the string.   
        parts = []
        parts.append("p: %s (%s)" % (p_str, p_delta_str))
        parts.append("r: %s (%s)" % (r_str, r_delta_str))                  
        
        return ", ".join(parts)

    def create_recap_string(self, latest):
        ''' Returns a string where the current precision/recall values are 
        compared to the latest and the best values. '''
            
        if latest and isinstance(latest, list):
            # If 'latest' is a list, compute p/r by average values.
            latest_p = sum(x[0] for x in latest) / len(latest)
            latest_r = sum(x[1] for x in latest) / len(latest)
        else:
            latest_p = latest[0] if latest else 0.0
            latest_r = latest[1] if latest else 0.0
        return "p: %s, r: %s" % (latest_p, latest_r)
        
    def count_edit_operations(diff_result):
        return
        insert_indexes = set()
        delete_indexes = set()
        for replace_item in diff_result.replaces:
            for delete_item in replace_item.deletes:
                delete_indexes.add(delete_item.element.outline[0])
            for insert_item in replace_item.inserts:
                insert_indexes.add(insert_item.element.outline[0])

        diff_result.commons.sort()
        
        num_split_operations = 0
        num_merge_operations = 0
        num_reorder_operations = 0
        
        prev_common_item = None
        for common_item in diff_result.commons:
            element_old = common_item.old_element
            element_new = common_item.element
            
            insert_indexes.discard(element_old.outline[0])
            delete_indexes.discard(element_old.outline[0])
            
            if prev_common_item is not None:
                prev_element_old = prev_common_item.old_element
                prev_element_new = prev_common_item.element
                
                deltas_old = tuple(map(operator.sub, element_old.outline, prev_element_old.outline))
                deltas_new = tuple(map(operator.sub, element_new.outline, prev_element_new.outline))

                if deltas_new[0] == 0 and deltas_new[1] < 0:
                    num_split_operations += 1
                    num_reorder_operations += 1                
                elif deltas_old[0] != 0 and deltas_new[0] == 0:
                    num_split_operations += 1
                if deltas_old[0] == 0 and deltas_new[0] != 0:
                    num_merge_operations += 1
                if deltas_old[0] < 0 or deltas_new[0] < 0:
                    num_reorder_operations += 1 

            prev_common_item = common_item

        print(num_split_operations, num_merge_operations, num_reorder_operations, len(insert_indexes), len(delete_indexes))
        
