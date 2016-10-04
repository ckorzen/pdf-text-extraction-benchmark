import sys
import argparse
import logging
import os
import os.path
import util

from datetime import datetime

from doc_diff import doc_diff
from doc_diff import count_diff_items
from doc_diff import visualize_diff_result

class Evaluator:
    ''' The base class of an evaluator. Can be used to derive a specific 
    evaluator to evaluate the quality of body text extraction of a specific
    PDF extracttion system. '''
    
    def __init__(self, args):
        ''' Creates a new base evaluator based on the given args. '''
        self.args = args
        
        # TODO: Remove this line.
        self.args.junk = self.args.junk.split()
        
    def evaluate(self):
        ''' Starts the evaluation. Scans the root of groundtruth files given in 
        args for groundtruth files that matches the given prefix and suffix. 
        Tries to identify the actual file that belongs to the groundtruth file. 
        '''
        
        # Determine the root directory of groundtruth files. 
        gt_root = self.args.gt_path
        # Determine the prefix of groundtruth files to consider. 
        gt_prefix = self.args.prefix
        # Determine the suffix of groundtruth files to consider.
        gt_suffix = self.get_groundtruth_files_suffix()
        # Determine the groundtruth files.
        gt_dir_files = self.get_groundtruth_files(root, prefix, suffix)
              
        # Handle the evaluation start.  
        self.handle_evaluation_start()
        
        # Evaluate with given groundtruth files.
        self.evaluate_gt_dir_files(gt_dir_files)
        
        # Handle the evaluation end.
        self.handle_evaluation_end()
        
    def evaluate_gt_dir_files(self, gt_dir_files):
        """ Processes the evaluation with given groundtruth files."""
        
        # Iterate through groundtruth files and evaluate them individually.  
        for gt_dir_file in gt_dir_files:
            evaluation_result = self.evaluate_gt_dir_file(gt_dir_file)
            # Handle the result.
            self.handle_evaluation_result(evaluation_result)
    
    def evaluate_gt_dir_file(self, gt_dir_file):
        """ Processes the evaluation with given groundtruth file. Returns the
        evaluation result for the given groundtruth file."""
        
        gt_dir, gt_file = gt_dir_file
        
        # Compose the absolute path to the groundtruth file.
        gt_file_path = os.path.join(gt_dir, gt_file)
        # Compose the absolute path to the associated actual file.
        actual_file_path = self.get_actual_path(gt_file_path)
        
        # Read and format the groundtruth file.
        gt = self.format_groundtruth_file(gt_file_path)
        # Read and format the actual file.
        actual = self.format_actual_file(actual_file_path)
        
        if not gt or not actual:
            return
        
        # Compute current values.
        return self.evaluate_strings(gt_path, gt, actual_path, actual)
                    
    def evaluate_strings(self, gt_path, gt, actual_path, actual):
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

        return doc_diff(actual, gt, self.args.junk)

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
        ''' Reads the given groundtruth file. Override it if you have to do more 
        advanced stuff, like removing semantic markups, etc.'''
        
        # Make sure, that the file exists.
        if os.path.isfile(file_path):
            file = open(file_path)
            str = file.read()
            file.close()
            return str
        else:
            return ""
       
    # --------------------------------------------------------------------------
    # Serialization / Deserialization.
    
    def deserialize(self, gt_dir_file):
        ''' Deserializes the related results file for the given groundtruth 
        file.'''
        
        gt_dir, gt_file = gt_dir_file
        
        # Compose the absolute path to the groundtruth file.
        gt_file_path = os.path.join(gt_dir, gt_file)
        # Compose the absolute path to the actual file.
        actual_path = self.get_actual_path(gt_file_path)
        # Compose the path to the serialization file.
        serialization_path = self.get_serialization_path(actual_path)
        
        best_para_splits     = sys.maxsize
        best_para_merges     = sys.maxsize
        best_para_rearranges = sys.maxsize
        best_para_inserts    = sys.maxsize
        best_para_deletes    = sys.maxsize
        best_word_replaces   = sys.maxsize
        best_word_inserts    = sys.maxsize
        best_word_deletes    = sys.maxsize
        latest_para_splits   = float("NaN")
        latest_para_merges   = float("NaN")
        latest_para_rearranges = float("NaN")
        latest_para_inserts  = float("NaN")
        latest_para_deletes  = float("NaN")
        latest_word_replaces = float("NaN")
        latest_word_inserts  = float("NaN")
        latest_word_deletes  = float("NaN")

        if os.path.isfile(serialization_path):
            with open(serialization_path, "r") as f:
                for line in f:
                    # Each line is of form: time <TAB> precision <TAB> recall
                    fields = line.rstrip().split("\t")
                      
                    para_splits   = int(fields[1])
                    para_merges   = int(fields[2])
                    para_rearranges = int(fields[3])
                    para_inserts  = int(fields[4])
                    para_deletes  = int(fields[5])
                    word_inserts  = int(fields[6])
                    word_deletes  = int(fields[7])
                    word_replaces = int(fields[8])

                    # Update the values.
                    best_para_splits   = max(para_splits, best_para_splits)
                    best_para_merges   = max(para_merges, best_para_merges)
                    best_para_rearranges = max(para_rearranges, best_para_rearranges)
                    best_para_inserts  = max(para_inserts, best_para_inserts)
                    best_para_deletes  = max(para_deletes, best_para_deletes)
                    best_word_inserts  = max(word_inserts, best_word_inserts)
                    best_word_deletes  = max(word_deletes, best_word_deletes)
                    best_word_replaces = max(word_replaces, best_word_replaces)

                    latest_para_splits   = para_splits
                    latest_para_merges   = para_merges
                    latest_para_rearranges = para_rearranges
                    latest_para_inserts  = para_inserts
                    latest_para_deletes  = para_deletes
                    latest_word_inserts  = word_inserts
                    latest_word_deletes  = word_deletes
                    latest_word_replaces = word_replaces
            
        best   = (best_para_splits, best_para_merges, best_para_rearranges, 
                  best_para_inserts, best_para_deletes, best_word_inserts, 
                  best_word_deletes, best_word_replaces)
        latest = (latest_para_splits, latest_para_merges, latest_para_rearranges, 
                  latest_para_inserts, latest_para_deletes, latest_word_inserts,
                  latest_word_deletes, latest_word_replaces)
        return (latest, best)
                
    # --------------------------------------------------------------------------
    # Util methods.
       
    def get_groundtruth_files_suffix(self):
        """ Returns the suffix of groundtruth files to consider on parsing the 
        input directory. """
        return ".body.txt"
        
    def get_groundtruth_files(self, root, prefix, suffix):
        """ Scans the given root directory to find groundtruth files that 
        matches the given prefix and suffix. Returns a list of tuples of form
        (dir, file), sorted by files."""
        
        result = []
        
        # Scan the given root directory for groundtruth files.
        for current_dir, dirs, files in os.walk(root):
            # Only consider the files that matches the given prefix and suffix.
            gt_files = sorted([f for f in files \
                if f.startswith(prefix) and f.endswith(suffix)])
                
            # Append tuples (dir, file) to the result list.
            result.extend([(current_dir, f) for f in gt_files])
        
        return result
      
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
        
    # --------------------------------------------------------------------------
    # Handler methods.
    
    def handle_evaluation_start(self):
        # TODO: Print the header of table.
        print("Evaluation start")
        
    def handle_evaluation_result(self, result):
        print(result)
        
    def handle_evaluation_end(self):
        print("Evaluation end")
