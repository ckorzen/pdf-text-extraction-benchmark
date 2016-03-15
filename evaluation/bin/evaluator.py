import argparse
import logging
import os

from evaluate_words import evaluate_words_extraction
from evaluate_paragraphs import evaluate_paragraphs_extraction
from evaluate_body import evaluate_body_extraction

logging.basicConfig(
    level=logging.DEBUG,
    format='%(asctime)s : %(levelname)s : %(module)s : %(message)s',
)
logger = logging.getLogger(__name__)

# The valid features for evaluation. Each feature is mapped to the corresponding
# evaluation method and the suffix of files to use on evaluation.
features = { 
    'word':      (evaluate_words_extraction, ".full.txt"), 
    'paragraph': (evaluate_paragraphs_extraction, ".full.txt"),
    'body':      (evaluate_body_extraction, ".body.txt")
}

class Evaluator:
    ''' The base class of our evaluation that can be used to evaluate the 
    accuracy of the features above. '''
    
    def __init__(self, args):
        ''' Creates a new evaluator with the given args. '''
        self.args = args
        
    def process(self):
        ''' Starts the evaluation. '''
                
        # Process each given feature.
        for feature in self.args.feature:
            logger.info("Evaluating feature %s." % feature)
                    
            results = self.evaluate(feature)
        
            # Deserialize the results of the latest evaluation to be able to 
            # compare the results with the results of this evaluation.
            latest_results = deserialize_latest_results(results)
            
            # Serialize the results to be able to compare the results in 
            # upcoming evaluations.
            serialize_evaluation_results(results)
        
            # p_delta = p - pp;
            #r_delta = r - rr;
            
            #logger.info("Result: precision: %.4f, recall: %.4f" % (p, r))
            
            # Compute average precision and recall.
            #n = len(results)           
            #avg_precision = sum(r[0] for r in results) / n if n > 0 else 0.0
            #avg_recall = sum(r[1] for r in results) / n if n > 0 else 0.0
                                        
    def evaluate(self, feature):
        '''
        Scans the given root of groundtruth files for groundtruth files that 
        matches the given prefix and suffix. Tries to find the actual file 
        related to the groundtruth. Evaluates the accuracy of the given feature 
        from both files, i.e. computes the precision and recall values for the 
        feature.
        '''    

        results = []
        
        args = self.args        
        prefix = args.prefix
        suffix = features[feature][1]
        evaluation_method = features[feature][0]
        groundtruth_root = args.gt_path
        
        # Scan the groundtruth root for groundtruth files to evaluate.
        for current_dir, dirs, files in os.walk(groundtruth_root):
            # Only consider the files that matches the given prefix and suffix.
            files = [fi for fi in files if fi.startswith(prefix) \
                                       and fi.endswith(suffix)]            
            for file in files:
                # Compose the absolute path of groundtruth file.
                gt_path = os.path.join(current_dir, file)
                
                logger.debug("Groundtruth file: %s" % gt_path)
                            
                # Try to find the related actual file.
                actual_path = self.get_actual_path(gt_path)
                # Compose the path to the visualization file.               
                args.visual_path = self.get_visual_path(feature, actual_path)
                                           
                logger.debug("Detected actual file: %s" % actual_path)
                  
                if gt_path and actual_path:                          
                    # Read and format the groundtruth file.
                    gt = self.format_groundtruth_file(gt_path)
                    # Read and format the actual file.
                    actual = self.format_actual_file(actual_path)
                    
                    # Evaluate.                                                    
                    results.append(evaluation_method(gt, actual, args))
                    
        return results
        
    def get_actual_path(self, gt_file_path):
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
    
    def get_visual_path(self, feature, actual_file_path):
        if not actual_file_path:
            return
    
        # Find the first dot in the path.
        index_last_dot = actual_file_path.rfind('.')
        
        if index_last_dot >= 0:
            basename = actual_file_path[:index_last_dot] 
            return basename + ".result." + feature + ".txt"
            
        
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
   
    def deserialize_latest_results(self, results):
        print(results)
            
    def get_argument_parser():
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
