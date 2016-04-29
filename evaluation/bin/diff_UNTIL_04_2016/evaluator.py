import argparse
import logging
import os
import os.path
import util

from datetime import datetime
from restore_document_structure import DocumentStructureRestorer

logging.basicConfig(
    level=logging.DEBUG,
    format='%(asctime)s : %(levelname)s : %(module)s : %(message)s',
)
logger = logging.getLogger(__name__)

class Evaluator:
    ''' The base class of our evaluation that can be used to evaluate the 
    accuracy of the features defined above. '''
    
    def __init__(self, args):
        ''' Creates a new evaluator with the given args. '''
        self.args = args
        
        self.args.junk = self.args.junk.split()

    def evaluate(self):
        '''
        Starts the evaluation. Scans the root of groundtruth files given in 
        args for groundtruth files that matches the given prefix and suffix. 
        Tries to identify the actual file that belongs to the groundtruth file. 
        Computes the precision/recall values that result from both files and 
        compares them to previous results.
        '''
        
        # The root directory to scan for groundtruth files. 
        groundtruth_root = self.args.gt_path
        
        # The prefix of files to consider on evaluation. 
        prefix = self.args.prefix
        # The suffix of files to consider on evaluation (depends on feature).
        suffix = self.get_groundtruth_files_suffix()
        
        latest_values = []
        best_values = []
        current_values = []

        print("%-25s %-10s %-10s %-10s %-10s %-10s" % ("filename", "#splits", "#merges", "#reorders", "#missing", "#spurious"))
        print("-" * 85)

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
                
                serialization_path = self.get_serialization_path(actual_path)
                latest, best = self.deserialize(serialization_path)
                
                latest_values.append(latest)
                best_values.append(best)

                if self.args.recap:
                    self.recap(actual_path, latest)
                else:
                    # Evaluate.
                    current = self.evaluate_by_paths(gt_path, actual_path)
                    current_values.append(current)

                    comparison_string = self.create_comparison_string(
                        current, latest, best)

                    # logger.info("Result for %s: %s" % (actual_path, comparison_string))
                    print("%-25s %s" % (os.path.basename(actual_path), comparison_string))

                    # print("Result for %s: %s" % (actual_path, comparison_string))
                    # self.visualize(self.get_visualization_path(actual_path))
                    self.serialize(current, serialization_path)

        if self.args.recap:
            logger.info("Total: %s" % self.create_recap_string(
                latest_values))
        else:
            print("-" * 85)
            print("%-25s %s" % ("Total: ", self.create_comparison_string(
                   current_values, latest_values, best_values)))
            print("-" * 85)
            # logger.info("Total: %s" % self.create_comparison_string(
            #    current_values, latest_values, best_values))

    def recap(self, actual_path, values):
        ''' Recaps the evaluation results for given files.'''
        recap = self.create_recap_string(values)
        logger.info("Result for %s: %s" % (actual_path, recap))

    def evaluate_by_paths(self, gt_path, actual_path):
        ''' Evaluates the files given by the paths.'''
                        
        # Read and format the groundtruth file.
        gt = self.format_groundtruth_file(gt_path)
        # Read and format the actual file.
        actual = self.format_actual_file(actual_path)

        # Evaluate.
        return self.evaluate_by_strings(gt_path, gt, actual_path, actual)

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

        restorer = DocumentStructureRestorer(gt, actual, self.args.junk)
        return restorer.restore()

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

    def deserialize(self, path):
        ''' Deserializes the related results file for the given file/feature-
        pair. Computes (a) the latest precision/recall and (b) the best 
        precision and best recall achieved so far for the actual path and 
        feature.'''
        
        best_para_splits     = float('inf')
        best_para_merges     = float('inf')
        best_para_reorders   = float('inf')
        best_para_missings   = float('inf')
        best_para_spurious   = float('inf')
        latest_para_splits   = float('inf')
        latest_para_merges   = float('inf')
        latest_para_reorders = float('inf')
        latest_para_missings = float('inf')
        latest_para_spurious = float('inf')

        if os.path.isfile(path):
            with open(path, "r") as f:
                for line in f:
                    # Each line is of form: time <TAB> precision <TAB> recall
                    fields = line.rstrip().split("\t")
                      
                    para_splits   = int(fields[1])
                    para_merges   = int(fields[2])
                    para_reorders = int(fields[3])
                    para_missings = int(fields[4])
                    para_spurious = int(fields[5])

                    # Update the values.
                    best_para_splits   = max(para_splits, best_para_splits)
                    best_para_merges   = max(para_merges, best_para_merges)
                    best_para_reorders = max(para_reorders, best_para_reorders)
                    best_para_missings = max(para_missings, best_para_missings)
                    best_para_spurious = max(para_spurious, best_para_spurious)

                    latest_para_splits   = para_splits
                    latest_para_merges   = para_merges
                    latest_para_reorders = para_reorders
                    latest_para_missings = para_missings
                    latest_para_spurious = para_spurious
            
        best   = (best_para_splits, best_para_merges, best_para_reorders, 
                  best_para_missings, best_para_spurious)
        latest = (latest_para_splits, latest_para_merges, latest_para_reorders, 
                  latest_para_missings, latest_para_spurious)
        return (latest, best)

    def serialize(self, values, path):
        ''' Serializes the given result to the related results file. '''
        with open(path, "a") as f:
            # Append the result to file.
            values_str = "\t".join(str(x) for x in values)
            f.write("%s\t%s\n" % (datetime.now(), values_str)) 

    def create_comparison_string(self, current, latest, best):
        ''' Returns a string where the current precision/recall values are 
        compared to the latest and the best values. '''
    
        if current and isinstance(current, list):
            # If 'current' is a list, compute p/r by average values.
            para_splits   = sum(x[0] for x in current) / len(current)
            para_merges   = sum(x[1] for x in current) / len(current)
            para_reorders = sum(x[2] for x in current) / len(current)
            para_missings = sum(x[3] for x in current) / len(current)
            para_spurious = sum(x[4] for x in current) / len(current)
        else:
            para_splits   = current[0] if current else 0
            para_merges   = current[1] if current else 0
            para_reorders = current[2] if current else 0
            para_missings = current[3] if current else 0
            para_spurious = current[4] if current else 0
        
        if latest and isinstance(latest, list):
            latest_para_splits   = sum(x[0] for x in latest) / len(latest)
            latest_para_merges   = sum(x[1] for x in latest) / len(latest)
            latest_para_reorders = sum(x[2] for x in latest) / len(latest)
            latest_para_missings = sum(x[3] for x in latest) / len(latest)
            latest_para_spurious = sum(x[4] for x in latest) / len(latest)
        else:
            latest_para_splits   = latest[0] if latest else 0
            latest_para_merges   = latest[1] if latest else 0
            latest_para_reorders = latest[2] if latest else 0
            latest_para_missings = latest[3] if latest else 0
            latest_para_spurious = latest[4] if latest else 0
            
        if best and isinstance(best, list):
            best_para_splits   = sum(x[0] for x in best) / len(best)
            best_para_merges   = sum(x[1] for x in best) / len(best)
            best_para_reorders = sum(x[2] for x in best) / len(best)
            best_para_missings = sum(x[3] for x in best) / len(best)
            best_para_spurious = sum(x[4] for x in best) / len(best)
        else:
            best_para_splits   = best[0] if best else 0
            best_para_merges   = best[1] if best else 0
            best_para_reorders = best[2] if best else 0
            best_para_missings = best[3] if best else 0
            best_para_spurious = best[4] if best else 0
        
        # Compute the difference to the latest values.
        para_splits_delta   = para_splits   - latest_para_splits
        para_merges_delta   = para_merges   - latest_para_merges
        para_reorders_delta = para_reorders - latest_para_reorders
        para_missings_delta = para_missings - latest_para_missings
        para_spurious_delta = para_spurious - latest_para_spurious
                
        # Format the delta parts.
        para_splits_delta_str   = "±0" if para_splits_delta == 0 else "%+d" % para_splits_delta
        para_merges_delta_str   = "±0" if para_merges_delta == 0 else "%+d" % para_merges_delta
        para_reorders_delta_str = "±0" if para_reorders_delta == 0 else "%+d" % para_reorders_delta
        para_missings_delta_str = "±0" if para_missings_delta == 0 else "%+d" % para_missings_delta
        para_spurious_delta_str = "±0" if para_spurious_delta == 0 else "%+d" % para_spurious_delta
        
        # Compose the string.   
        parts = []
        parts.append(("%s" % para_splits).rjust(3))
        parts.append(("(%s)" % para_splits_delta_str).ljust(3))
        parts.append(("%s" % para_merges).rjust(5))
        parts.append(("(%s)" % para_merges_delta_str).ljust(3))
        parts.append(("%s" % para_reorders).rjust(5))
        parts.append(("(%s)" % para_reorders_delta_str).ljust(3))
        parts.append(("%s" % para_missings).rjust(5))
        parts.append(("(%s)" % para_missings_delta_str).ljust(3))
        parts.append(("%s" % para_spurious).rjust(5))
        parts.append(("(%s)" % para_spurious_delta_str).ljust(3))
        #parts.append("%s (%s)" % (para_merges_str, para_merges_delta_str))
        #parts.append("%s (%s)" % (para_reorders_str, para_reorders_delta_str))
        #parts.append("%s (%s)" % (para_missings_str, para_missings_delta_str))
        #parts.append("%s (%s)" % (para_spurious_str, para_spurious_delta_str))
        
        return " ".join(parts)

    def create_recap_string(self, latest):
        ''' Returns a string where the current precision/recall values are 
        compared to the latest and the best values. '''
            
        if latest and isinstance(latest, list):
            # If 'latest' is a list, compute p/r by average values.
            latest_para_splits   = sum(x[0] for x in latest) / len(latest)
            latest_para_merges   = sum(x[1] for x in latest) / len(latest)
            latest_para_reorders = sum(x[2] for x in latest) / len(latest)
            latest_para_missings = sum(x[3] for x in latest) / len(latest)
            latest_para_spurious = sum(x[4] for x in latest) / len(latest)
        else:
            latest_para_splits   = latest[0] if latest else 0
            latest_para_merges   = latest[1] if latest else 0
            latest_para_reorders = latest[2] if latest else 0
            latest_para_missings = latest[3] if latest else 0
            latest_para_spurious = latest[4] if latest else 0

        # Compose the string.   
        parts = []
        parts.append("#para_splits: %s"    % (latest_para_splits))
        parts.append("#para_merges: %s"    % (latest_para_merges))
        parts.append("#para_reorders: %s"  % (latest_para_reorders))
        parts.append("#missing_paras: %s"  % (latest_para_missings))
        parts.append("#spurious_paras: %s" % (latest_para_spurious))
        
        return ", ".join(parts)

    # __________________________________________________________________________

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

    def get_groundtruth_files_suffix(self):
        """ Returns the suffix of groundtruth files to consider on parsing the 
        input directory. """
        return ".full.txt"
        
    def get_visualization_path(self, actual_path):
        ''' Returns the path to the file, where the visualization of the 
        evaluation of the given file/feature - pair should be stored. '''
        return util.update_file_extension(actual_path, ".visualization.txt")

    def get_serialization_path(self, actual_path):
        ''' Returns the path to the file, where the evaluation results for the
        given file/feature-pair should be stored. '''
        return util.update_file_extension(actual_path, ".results.txt")

    def get_argument_parser():
        ''' Creates an parser to parse the command line arguments. '''
        parser = argparse.ArgumentParser()
        
        def add_arg(names, default=None, help=None, nargs=None, choices=None):
            parser.add_argument(names, default=default, help=help, nargs=nargs, 
                choices=choices)
            
        add_arg("actual_path", help="The path to the files to evaluate.")
        add_arg("gt_path", help="The path to the groundtruth files.")
        add_arg("--rearrange", help="Toogle rearranging of words.")
        add_arg("--ignore_cases", help="Toggle case-sensitivity.")
        add_arg("--remove_spaces", help="Toggle removing of whitespaces.")
        add_arg("--max_dist", help="The max. distance between words.")
        add_arg("--min_sim", help="The min. similarity between words.")
        add_arg("--junk", help="The junk to ignore.")
        add_arg("--prefix", help="The prefix of evaluation files", default="")
        add_arg("--output", help="The path to the result file", default="")
        add_arg("--recap", help="Recap latest evaluation results", default="")

        return parser