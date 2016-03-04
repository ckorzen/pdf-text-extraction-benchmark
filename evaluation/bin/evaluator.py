import argparse
import logging
import os
import os.path
import util

from evaluate_words import evaluate_words_extraction
from evaluate_paragraphs import evaluate_paragraphs_extraction
from evaluate_body import evaluate_body_extraction
from datetime import datetime

logging.basicConfig(
    level=logging.DEBUG,
    format='%(asctime)s : %(levelname)s : %(module)s : %(message)s',
)
logger = logging.getLogger(__name__)

# The valid features for evaluation. Each feature is mapped to the 
# corresponding evaluation method and the suffix of files to use on evaluation.
features = { 
    'word':      (evaluate_words_extraction, ".full.txt"), 
    'paragraph': (evaluate_paragraphs_extraction, ".full.txt"),
    'body':      (evaluate_body_extraction, ".body.txt")
}

class Evaluator:
    ''' The base class of our evaluation that can be used to evaluate the 
    accuracy of the features defined above. '''
    
    def __init__(self, args):
        ''' Creates a new evaluator with the given args. '''
        self.args = args
        
    def process(self):
        ''' Starts the evaluation. '''
        
        for feature in self.args.feature:                    
            # Evaluate the feature.
            results, latests, bests = self.evaluate_feature(feature)
            
            comparison = self.get_comparison_string(results, latests, bests)
            logger.info("Total: %s" % comparison) 
                                        
    def evaluate_feature(self, feature):
        '''
        Evaluates the given feature. Scans the root of groundtruth files given 
        in global args for groundtruth files that matches the given prefix and 
        suffix. Tries to identify the actual file that belongs to the 
        groundtruth file. Computes the precision/recall values that result
        from both files and compares them to previous results. Creates a 
        visualization of the evaluation.
        feature.
        '''    
        
        # The precision/recall values of this evaluation.
        current_p_r = []
        # The precision/recall values of the latest evaluation.
        latest_p_r  = []
        # The best precision/recall values for each file.
        best_p_r    = []
        # The global arguments.
        args = self.args
        # The prefix of files to consider on evaluation. 
        prefix = args.prefix
        # The suffix of files to consider on evaluation (depends on feature).
        suffix = features[feature][1]
        # The evaluation method to use (depends on feature).
        method = features[feature][0]
        # The root directory to scan for groundtruth files. 
        groundtruth_root = args.gt_path
        
        # Scan the given root directory for groundtruth files to evaluate.
        for current_dir, dirs, files in os.walk(groundtruth_root):
            # Only consider the files that matches the given prefix and suffix.
            files = [fi for fi in files if fi.startswith(prefix) \
                                       and fi.endswith(suffix)]            
            for file in files:
                # Compose the absolute path of the groundtruth file.
                gt_path = os.path.join(current_dir, file)
                # Compose the absolute path of the actual file.
                actual_path = self.get_actual_path(gt_path)
                
                # Evaluate the file.
                # 'current' includes the tuple (precision, recall)
                # 'items' includes the diff between groundtruth and actual.
                current, items = self.evaluate(gt_path, actual_path, method)
                
                if current:
                    current_p_r.append(current)
                                                          
                # Obtain the latest and the best result for the file.
                # 'latest' and 'best' are tuples of form (precision, recall).
                latest, best = self.deserialize_result(actual_path, feature)
                
                if latest and best:
                    latest_p_r.append(latest)
                    best_p_r.append(best)
                
                comparison = self.get_comparison_string(current, latest, best)
                logger.info("Result for %s: %s" % (actual_path, comparison))
                
                # Visualize the result.
                self.visualize_result(items, actual_path, feature)
                
                # Serialize the result (to compare it in upcoming evaluations).
                self.serialize_result(current, actual_path, feature)
                    
        return current_p_r, latest_p_r, best_p_r
    
    def evaluate(self, gt_path, actual_path, evaluation_method): 
        ''' Reads the content of the files given by gt_path and actual_path 
        and compares them using the given evaluation method.'''
                        
        # Read and format the groundtruth file.
        gt = self.format_groundtruth_file(gt_path)
        # Read and format the actual file.
        actual = self.format_actual_file(actual_path)
                                     
        # Evaluate.                                                    
        return evaluation_method(gt, actual, self.args)
        
    def get_actual_path(self, gt_file_path):
        '''
        Returns the path to the actual file produced by the tool under review
        that belongs to the given groundtruth file path.
        Returns None if no such actual file exists.
        '''
        groundtruth_root = self.args.gt_path
        gt_rel_path = os.path.relpath(gt_file_path, groundtruth_root)
            
        # Groundtruth file may have an extended file extension like 
        # "cond-mat0001228.full.txt" or "cond-mat0001228.body.txt"
        # But actual files don't have these extension.
        # For file name "cond-mat0001228.full.txt", find "cond-mat0001228.txt"
           
        # Find the first dot in the gt path.
        index_first_dot = gt_rel_path.find('.')
                
        if index_first_dot >= 0:
            # ext_file_extension = ".full.txt"
            ext_file_extension = gt_rel_path[index_first_dot : ]
            # Find the last dot in the extended file extension.
            index_last_dot = ext_file_extension.rfind('.')
            if index_last_dot >= 0:
                # ext_file_extension = ".txt"
                file_ext = ext_file_extension[index_last_dot : ]
                # Replace the extended file extension by the simple one.
                gt_rel_path = gt_rel_path.replace(ext_file_extension, file_ext)
            
        return os.path.join(self.args.actual_path, gt_rel_path)
    
    def get_results_path(self, actual_path, feature):
        ''' Returns the path to the file, where the evaluation results for the
        given file/feature-pair should be stored. '''
        file_extension = ".results." + feature + ".txt"
        return util.update_file_extension(actual_path, file_extension)
    
    def get_visualization_path(self, actual_path, feature):
        ''' Returns the path to the file, where the visualization of the 
        evaluation of the given file/feature - pair should be stored. '''
        file_extension = ".visualization." + feature + ".txt"
        return util.update_file_extension(actual_path, file_extension)
            
    def format_actual_file(self, file_path):
        ''' Reads the given actual file. Override it if you have to do more 
        advanced stuff, like removing semantic markups, etc.'''
        
        # Make sure, that the file exists.
        if os.path.isfile(file_path):
            file = open(file_path)
            str = file.read()
            file.close()                        
            return str
        else:
            return ""

    def format_groundtruth_file(self, file_path):
        ''' Reads the given actual file. Override it if you have to do more 
        advanced stuff, like removing semantic markups, etc.'''
        
        # Make sure, that the file exists.
        if os.path.isfile(file_path):
            file = open(file_path)
            str = file.read()
            file.close()
            return str
        else:
            return ""
    
    def serialize_result(self, result, actual_path, feature):
        ''' Serializes the given result to the related results file. '''
        
        # Obtain the path to the results file. 
        results_path = self.get_results_path(actual_path, feature)
        
        with open(results_path, "a") as f:
            # Append the result to file.
            f.write("%s\t%f\t%f\n" % (datetime.now(), result[0], result[1]))  
    
    def deserialize_result(self, actual_path, feature):
        ''' Deserializes the related results file for the given file/feature-
        pair. Computes (a) the latest precision/recall and (b) the best 
        precision and best recall achieved so far for the actual path and 
        feature.'''

        # Latest precision and recall.
        latest_p, latest_r = 0.0, 0.0                
        # Best precision and recall.
        best_p, best_r = 0.0, 0.0

        # Obtain the path to the results file to read.        
        results_path = self.get_results_path(actual_path, feature)
        
        if os.path.isfile(results_path):
            with open(results_path, "r") as f:
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
                    
            return (latest_p, latest_r), (best_p, best_r) 
        return None, None
     
    def get_comparison_string(self, current, latest, best):
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
    
    def visualize_result(self, items, actual_path, feature):
        ''' Visualizes the given evaluation result. This will print the content
        of the groundtruth file, where all deletions (parts which were not 
        present in the actual file) are highlighted in red and all insertions
        (parts which were only present in the actual file) are highlighted in 
        green.'''
        
        # Merge all commons, insertions and deletions.
        full = [(x, "=") for x in items[0]] # commons
        for replace in items[1]: # replaces
            full += [(x, "-") for x in replace[0]] # deletes
            full += [(x, "+") for x in replace[1]] # inserts
        full.sort()
        
        # Define the codes to highlight deletions and insertions.               
        visualization_delete_start = "\033[30;41m"
        visualization_delete_end = "\033[0m"
        visualization_insert_start = "\033[30;42m"
        visualization_insert_end = "\033[0m"
         
        snippets = []
                        
        for i, item in enumerate(full):
            if item[1] == "+":
                snippets.append(visualization_insert_start)
            elif item[1] == "-":
                snippets.append(visualization_delete_start)
            snippets.append("%s" % item[0].element)
            if item[1] == "+":
                snippets.append(visualization_insert_end)
            elif item[1] == "-":
                snippets.append(visualization_delete_end)
            snippets.append(" ")   
            
        # Store the visualization to file.
        visualization_string = "".join(snippets)
        visualization_path = self.get_visualization_path(actual_path, feature)
        with open(visualization_path, "w") as f:
            f.write(visualization_string)
             
    def get_argument_parser():
        ''' Creates an parser to parse the command line arguments. '''
        parser = argparse.ArgumentParser()
        
        def add_arg(names, default=None, help=None, nargs=None, choices=None):
            parser.add_argument(names, default=default, help=help, nargs=nargs, 
                choices=choices)
        
        add_arg("actual_path", help="The path to the files to evaluate.")
        add_arg("gt_path", help="The path to the groundtruth files.")
        add_arg("--feature", nargs="+", choices=features.keys(), 
            help="The features to evaluate.")
        add_arg("--rearrange", help="Toogle rearranging of words.")
        add_arg("--ignore_cases", help="Toggle case-sensitivity.")
        add_arg("--remove_spaces", help="Toggle removing of whitespaces.")
        add_arg("--max_dist", help="The max. distance between words.")
        add_arg("--min_sim", help="The min. similarity between words.")
        add_arg("--junk", help="The junk to ignore.")
        add_arg("--prefix", help="The prefix of evaluation files", default="")
        add_arg("--output", help="The path to the result file", default="")
        return parser

if __name__ == "__main__": 
    ''' The main method. '''     
    Evaluator(Evaluator.get_argument_parser().parse_args()).process()
