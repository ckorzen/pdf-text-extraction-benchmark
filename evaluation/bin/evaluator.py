import argparse
import logging
import os

from evaluate_words import evaluate_words_extraction

logging.basicConfig(
    level=logging.DEBUG,
    format='%(asctime)s : %(levelname)s : %(module)s : %(message)s',
)
logger = logging.getLogger(__name__)

class Evaluator:
    ''' The base class of an evaluator that can be used to evaluate the accuracy
    of various features. '''
    
    def __init__(self, args):
        ''' Creates a new evaluator with the given args. '''
        self.args = args
        
    def process(self):
        ''' Starts the evaluation. '''
        
        # Evaluate the extraction of words, if either no type is defined or
        # the type "word" is given explicitly.
        if not self.args.type or self.args.type == "word":
            (precision, recall) = self.evaluate(evaluate_words_extraction)
                                
    def evaluate(self, evaluation_method):
        '''
        Scans the given root of groundtruth files for groundtruth files that 
        matches the given prefix and suffix. Tries to find the actual file 
        related to the groundtruth. Evaluates the accuracy of a certain feature 
        from both files using the given evaluation method, i.e. computes the 
        precision and recall values for the feature.
        '''    
        logger.info("Evaluating using method %s." % evaluation_method.__name__)
        
        prefix = self.args.prefix
        suffix = self.args.word_suffix
        groundtruth_root = self.args.gt_path
        
        results = []
        
        # Scan the groundtrith root for groundtruth files.
        for current_dir, dirs, files in os.walk(groundtruth_root):
            # Only consider the files that matches the given prefix and suffix.
            files = [fi for fi in files if fi.startswith(prefix) \
                                       and fi.endswith(suffix)]            
            for file in files:
                # Compose the absolute path of groundtruth file.
                gt_file_path = os.path.join(current_dir, file)
                
                logger.debug("Groundtruth file: %s" % gt_file_path)
                            
                # Try to find the related actual file.
                actual_file_path = self.get_actual_file_path(gt_file_path)
                             
                logger.debug("Detected actual file: %s" % actual_file_path)
                  
                if gt_file_path and actual_file_path:                          
                    # Read and format the groundtruth file.
                    gt = self.format_groundtruth_file(gt_file_path)
                    # Read and format the actual file.
                    actual = self.format_actual_file(actual_file_path)
                
                    # Compute precision/recall values.
                    (p, r) = evaluation_method(gt, actual, rearrange=False)
                    
                    logger.debug("Precision: %.2f, Recall: %.2f" % (p, r))                                   
                    results.append((p, r))
        
        n = len(results)           
        avg_precision = sum(r[0] for r in results) / n if n > 0 else 0.0
        avg_recall = sum(r[1] for r in results) / n if n > 0 else 0.0
                    
        logger.info("Avg. precision: %.2f, Avg. recall: %.2f" 
                        % (avg_precision, avg_recall))
        return (avg_precision, avg_recall)
        
    def get_actual_file_path(self, gt_file_path):
        '''
        Given the path to a groundtruth file, this method returns the path to 
        the related actual file of the tool under review.
        Returns None if no such actual file exists.
        '''
        groundtruth_root = self.args.gt_path
        gt_rel_path = os.path.relpath(gt_file_path, groundtruth_root)
            
        # Groundtruth file may have an extended file extension like 
        # "cond-mat0001228.full.txt" or "cond-mat0001228.body.txt"
        # But actual files don't have these extension.
        # For a file name "cond-mat0001228.full.txt", find "cond-mat0001228.txt"
           
        # Find the first dot in the gt path.
        index_first_dot = gt_rel_path.find('.')
                
        if index_first_dot >= 0:
            # ext_file_extension = ".full.txt"
            ext_file_extension = gt_rel_path[index_first_dot:]
            # Find the last dot in the extended file extension.
            index_last_dot = ext_file_extension.rfind('.')
            if index_last_dot >= 0:
                # ext_file_extension = ".txt"
                file_ext = ext_file_extension[index_last_dot:]
                # Replace the extended file extension by the simple one.
                gt_rel_path = gt_rel_path.replace(ext_file_extension, file_ext)
            
        return os.path.join(self.args.actual_path, gt_rel_path)
        
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
            
    def get_argument_parser():
        parser = argparse.ArgumentParser()
        parser.add_argument("actual_path",  \
            help="The path to the files to evaluate")
        parser.add_argument("gt_path", \
            help="The path to the groundtruth files")
        parser.add_argument("-t", "--type", \
            help="the type to evaluate (word, para, body)")
        parser.add_argument("--max_distance", type=int, default=0, \
            help="the max. distance between words.")
        parser.add_argument("--min_similarity", type=float, default=1, \
            help="the min. similarity between words.")
        parser.add_argument("-p", "--prefix", default="", \
            help="the prefix of the files to evaluate")
        parser.add_argument("--word_suffix", default="", \
            help="the suffix of gt files to consider on word evaluation")
        parser.add_argument("--para_suffix", default="", \
            help="the suffix of gt files to consider on para evaluation")
        parser.add_argument("--body_suffix", default="", \
            help="the suffix of gt files to consider on body evaluation")
        parser.add_argument("-o", "--output", default="", \
            help="the path to the result file to create")
        return parser

if __name__ == "__main__":      
    Evaluator(Evaluator.get_argument_parser().parse_args()).process()
