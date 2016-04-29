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

class ParagraphsEvaluator(Evaluator):
    ignore_cases = True
    rearrange    = True
    max_dist     = 0
    junk         = []

    def __init__(self, args):
        ''' Creates a new words evaluator with the given args. '''
        self.args = args
        self.current_operations = None
        self.current_num_operations = None
        self.all_num_operations = []
        self.current_latest_num_operations = None
        self.all_latest_num_operations = []
        self.current_best_num_operations = None
        self.all_best_num_operations = []
        self.current_diff_result = None
        self.feature = "paragraph"
        
    def deserialize(self, path):
        ''' Deserializes the related results file for the given file/feature-
        pair. Computes (a) the latest precision/recall and (b) the best 
        precision and best recall achieved so far for the actual path and 
        feature.'''

        # Latest num operations.
        latest_num_splits = 0
        latest_num_merges = 0
        latest_num_reorders = 0
        latest_num_fn = 0
        latest_num_fp = 0
        best_num_splits = 0
        best_num_merges = 0
        best_num_reorders = 0
        best_num_fn = 0
        best_num_fp = 0
        
        if os.path.isfile(path):
            with open(path, "r") as f:
                for line in f:
                    # Each line is of form: time <TAB> precision <TAB> recall
                    fields = line.rstrip().split("\t")
                      
                    num_splits = int(fields[1])
                    num_merges = int(fields[2])
                    num_reorders = int(fields[3])
                    num_fn = int(fields[4])
                    num_fp = int(fields[5])
                    
                    # Update the values.
                    best_num_splits = min(num_splits, best_num_splits)
                    best_num_merges = min(num_merges, best_num_merges)
                    best_num_reorders = min(num_reorders, best_num_reorders)
                    best_num_fn = min(num_fn, best_num_fn)
                    best_num_fp = min(num_fp, best_num_fp)
                    latest_num_splits = num_splits
                    latest_num_merges = num_merges
                    latest_num_reorders = num_reorders
                    latest_num_fn = num_fn
                    latest_num_fp = num_fp
            
            latest = (latest_num_splits, 
                      latest_num_merges, 
                      latest_num_reorders, 
                      latest_num_fn,
                      latest_num_fp)
            best   = (best_num_splits, 
                      best_num_merges, 
                      best_num_reorders, 
                      best_num_fn,
                      best_num_fp)
                        
            self.current_latest_num_operations = latest 
            self.all_latest_num_operations.append(latest)
            self.current_best_num_operations = best
            self.all_best_num_operations.append(best)

    def serialize(self, path):
        ''' Serializes the given result to the related results file. '''
        
        result = self.current_num_operations
        
        with open(path, "a") as f:
            # Append the result to file.
            f.write("%s\t%d\t%d\t%d\t%d\t%d\n" % (datetime.now(), result[0], 
                result[1], result[2], result[3], result[4])) 

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
    
        gt_words     = util.to_formatted_words_by_paragraph(gt, ignore_cases, junk)
        actual_words = util.to_formatted_words_by_paragraph(actual, ignore_cases, junk)

        diff_result = diff.diff(gt_words, actual_words,
            rearrange = rearrange,
            junk = junk,
            max_dist = max_dist)
        
        self.current_operations = self.compute_operations(diff_result)
        num_operations = tuple([len(x) for x in self.current_operations])

        self.current_num_operations = num_operations
        self.all_num_operations.append(num_operations)
        self.current_diff_result = diff_result
        
        comparison_string = self.create_comparison_string(
            self.current_num_operations, 
            self.current_latest_num_operations, 
            self.current_best_num_operations)
        logger.info("Result for %s: %s" % (actual_path, comparison_string))

    def finish_evaluation(self):
        if self.args.recap:
            logger.info("Total: %s" % self.create_recap_string(
                self.all_latest_num_operations))
        else:
            logger.info("Total: %s" % self.create_comparison_string(
                self.all_num_operations,
                self.all_latest_num_operations,
                self.all_best_num_operations))

    def visualize(self, to_path):
        ''' Visualizes the given evaluation result. This will print the content
        of the groundtruth file, where all deletions (parts which were not 
        present in the actual file) are highlighted in red and all insertions
        (parts which were only present in the actual file) are highlighted in 
        green.'''

        split_positions = set(self.current_operations[0])
        merge_positions = set(self.current_operations[1])
        reorder_positions = set(self.current_operations[2])
        fns = set(self.current_operations[3])
        fps = set(self.current_operations[4])
        
        all_diff_items = []
        all_diff_items.extend(self.current_diff_result.commons)
        for replace_item in self.current_diff_result.replaces:
            all_diff_items.extend(replace_item.deletes)
            all_diff_items.extend(replace_item.inserts)

        all_diff_items.sort()
        
        def red(text):
            return "\033[30;41m%s\033[0m" % text
            
        def green(text):
            return "\033[30;42m%s\033[0m" % text
        
        def yellow(text):
            return "\033[30;43m%s\033[0m" % text
        
        def blue(text):
            return "\033[30;44m%s\033[0m" % text
        
        def turquoise(text):
            return "\033[30;46m%s\033[0m" % text
                
        snippets = []
        i = 0
        while i < len(all_diff_items):
            diff_item = all_diff_items[i]
            
            if tuple(diff_item.element.outline) in split_positions: 
                snippets.append(yellow("||"))
                snippets.append(diff_item.element.string)
            
            elif tuple(diff_item.element.outline) in merge_positions: 
                snippets.append(turquoise("=="))
                snippets.append(diff_item.element.string)
            
            elif tuple(diff_item.element.outline) in reorder_positions: 
                snippets.append(blue(diff_item.element.string))
            
            elif diff_item.element.outline[0] in fns: 
                fn_strings = []
                while i < len(all_diff_items) and all_diff_items[i].element.outline[0] == diff_item.element.outline[0]: 
                    fn_strings.append(all_diff_items[i].element.string)
                    i += 1
                if fn_strings:
                    snippets.append(red(" ".join(fn_strings)))
            elif diff_item.element.outline[0] in fps: 
                fp_strings = []
                while i < len(all_diff_items) and all_diff_items[i].element.outline[0] == diff_item.element.outline[0]: 
                    fp_strings.append(all_diff_items[i].element.string)
                    i += 1
                if fp_strings:
                    snippets.append(green(" ".join(fp_strings)))
            else:
                snippets.append(diff_item.element.string)
            
            i += 1
        # Store the visualization to file.
        visualization_string = " ".join(snippets)
        with open(to_path, "w") as f:
            f.write(visualization_string)

    def create_comparison_string(self, current, latest, best):
        ''' Returns a string where the current precision/recall values are 
        compared to the latest and the best values. '''
    
        if current and isinstance(current, list):
            # If 'current' is a list, compute p/r by average values.
            split   = sum(x[0] for x in current) / len(current)
            merge   = sum(x[1] for x in current) / len(current)
            reorder = sum(x[2] for x in current) / len(current)
            fn      = sum(x[3] for x in current) / len(current)
            fp      = sum(x[4] for x in current) / len(current)
        else:
            split   = current[0] if current else 0
            merge   = current[1] if current else 0
            reorder = current[2] if current else 0
            fn      = current[3] if current else 0
            fp      = current[4] if current else 0
        
        if latest and isinstance(latest, list):
            # If 'latest' is a list, compute p/r by average values.
            latest_split   = sum(x[0] for x in latest) / len(latest)
            latest_merge   = sum(x[1] for x in latest) / len(latest)
            latest_reorder = sum(x[2] for x in latest) / len(latest)
            latest_fn      = sum(x[3] for x in latest) / len(latest)
            latest_fp      = sum(x[4] for x in latest) / len(latest)
        else:
            latest_split   = latest[0] if latest else 0
            latest_merge   = latest[1] if latest else 0
            latest_reorder = latest[2] if latest else 0
            latest_fn      = latest[3] if latest else 0
            latest_fp      = latest[4] if latest else 0
            
        if best and isinstance(best, list):
            # If 'best' is a list, compute p/r by average values.
            best_split   = sum(x[0] for x in best) / len(best)
            best_merge   = sum(x[1] for x in best) / len(best)
            best_reorder = sum(x[2] for x in best) / len(best)
            best_fn      = sum(x[3] for x in best) / len(best)
            best_fp      = sum(x[4] for x in best) / len(best)
        else:
            best_split   = best[0] if best else 0
            best_merge   = best[1] if best else 0
            best_reorder = best[2] if best else 0
            best_fn      = best[3] if best else 0
            best_fp      = best[4] if best else 0
        
        # Check, if the current values are the best values achieved so far.
        is_best_split   = split   < best_split
        is_best_merge   = merge   < best_merge
        is_best_reorder = reorder < best_reorder
        is_best_fn      = fn      < best_fn
        is_best_fp      = fp      < best_fp
        
        # Compute the difference to the latest values.
        split_delta   = split   - latest_split
        merge_delta   = merge   - latest_merge
        reorder_delta = reorder - latest_reorder
        fn_delta      = fn      - latest_fn
        fp_delta      = fp      - latest_fp
        
        def bold(text):
            return "\033[1m%s\033[0m" % text
        
        def red(text):
            return "\033[0;31m%+d\033[0m" % text
            
        def green(text):
            return "\033[0;32m%+d\033[0m" % text
        
        # Print values in bold, if they are the best values.
        split_str = bold(split) if is_best_split else split 
        merge_str = bold(merge) if is_best_merge else merge
        reorder_str = bold(reorder) if is_best_reorder else reorder
        fn_str = bold(fn) if is_best_fn else fn
        fp_str = bold(fp) if is_best_fp else fp
        
        # Format the delta parts.
        split_delta_str   = green(split_delta) if split_delta < 0 else red(split_delta)
        split_delta_str   = "±0.0" if split_delta == 0 else split_delta_str
        merge_delta_str   = green(merge_delta) if merge_delta < 0 else red(merge_delta)
        merge_delta_str   = "±0.0" if merge_delta == 0 else merge_delta_str
        reorder_delta_str = green(reorder_delta) if reorder_delta < 0 else red(reorder_delta)
        reorder_delta_str = "±0.0" if reorder_delta == 0 else reorder_delta_str
        fn_delta_str      = green(fn_delta) if fn_delta < 0 else red(fn_delta)
        fn_delta_str      = "±0.0" if fn_delta == 0 else fn_delta_str
        fp_delta_str      = green(fp_delta) if fp_delta < 0 else red(fp_delta)
        fp_delta_str      = "±0.0" if fp_delta == 0 else fp_delta_str
                      
        # Compose the string.   
        parts = []
        parts.append("#splits: %3s (%s)" % (split_str, split_delta_str))
        parts.append("#merges: %3s (%s)" % (merge_str, merge_delta_str))
        parts.append("#reorders: %3s (%s)" % (reorder_str, reorder_delta_str))
        parts.append("#fn: %3s (%s)" % (fn_str, fn_delta_str))
        parts.append("#fp: %3s (%s)" % (fp_str, fp_delta_str))
        
        return ", ".join(parts)

    def create_recap_string(self, latest):
        ''' Returns a string where the current precision/recall values are 
        compared to the latest and the best values. '''
            
        if latest and isinstance(latest, list):
            # If 'latest' is a list, compute p/r by average values.
            latest_split   = sum(x[0] for x in latest) / len(latest)
            latest_merge   = sum(x[1] for x in latest) / len(latest)
            latest_reorder = sum(x[2] for x in latest) / len(latest)
            latest_fn      = sum(x[3] for x in latest) / len(latest)
            latest_fp      = sum(x[4] for x in latest) / len(latest)
        else:
            latest_split   = latest[0] if latest else 0.0
            latest_merge   = latest[1] if latest else 0.0
            latest_reorder = latest[2] if latest else 0.0
            latest_fn      = latest[3] if latest else 0.0
            latest_fp      = latest[4] if latest else 0.0
            
        # Compose the string.   
        parts = []
        parts.append("#splits: %s"   % latest_split)
        parts.append("#merges: %s"   % latest_merges)
        parts.append("#reorders: %s" % latest_reorders)
        parts.append("#fn: %s"       % latest_fn)
        parts.append("#fp: %s)"      % latest_fp)

    def compute_operations(self, diff_result):
        insert_indexes = set()
        delete_indexes = set()
        for replace_item in diff_result.replaces:
            for delete_item in replace_item.deletes:
                delete_indexes.add(delete_item.element.outline[0])
            for insert_item in replace_item.inserts:
                insert_indexes.add(insert_item.element.outline[0])

        diff_result.commons.sort()
        
        split_operations = []
        merge_operations = []
        reorder_operations = []
        fn = []
        fp = []
        
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
                    split_operations.append(tuple(element_new.outline))
                    reorder_operations.append(tuple(element_new.outline))
                elif deltas_old[0] != 0 and deltas_new[0] == 0:
                    split_operations.append(tuple(element_new.outline))
                if deltas_old[0] == 0 and deltas_new[0] != 0:
                    merge_operations.append(tuple(element_new.outline))
                if deltas_old[0] < 0 or deltas_new[0] < 0:
                    reorder_operations.append(tuple(element_new.outline)) 

            prev_common_item = common_item

        fn = list(delete_indexes)
        fp = list(insert_indexes)
        
        return (split_operations, merge_operations, reorder_operations, fn, fp)
            
            
if __name__ == "__main__":
    gt = [["Hello"], ["World"]]
    actual = [["Hello"], ["World"], ["XXX"]]

    diff_result = diff.diff(gt, actual,
        rearrange = True,
        junk = [],
        max_dist = 0)
        
    print(ParagraphsEvaluator({}).count_num_operations(diff_result))
