import os
import re
import sys
import operator
import argparse
from cfuzzyset import cFuzzySet as FuzzySet
from collections import Counter

class Evaluator:
    def __init__(self, args):
        '''
        Creates a new evaluator with the given paths to 
        (1) the root directory of the result files to evaluate and
        (2) the root directory of the groundtruth to use on evaluation.
        '''
        self.args = args

    def process(self):
        '''
        Scans the root directory of the result files recursively. Each of these 
        files are taken and compared to the related groundtruth file to 
        evaluate the extraction results.
        '''   
        print("Args: %s\n" % (self.args,));
        
        # Check, if there is path to a result file given.
        result_file = None;                      
        if self.args.output:
            result_file = open(self.args.output, 'w')
            # Write the header to result file.
            result_file.write("#path\tp/r of word extraction\t"
                                + "p/r of para extraction\t"
                                + "p/r of body extraction\n")
        
        word_results = []
        para_results = []
        body_results = []
        
        # Do the evaluation.
        if not self.args.type or self.args.type == "word":
            word_results = self.evaluate_words_extraction()
        if not self.args.type or self.args.type == "para":
            para_results = self.evaluate_para_extraction()
        if not self.args.type or self.args.type == "body":
            body_results = self.evaluate_body_extraction()
        
        # Take the average of the precision/recall tuples.
        word_avg = 0.0
        para_avg = 0.0
        body_avg = 0.0
        
        if len(word_results) > 0:
            word_avg = (sum(r[0] for r in word_results) / len(word_results),
                        sum(r[1] for r in word_results) / len(word_results))
        if len(para_results) > 0:
            para_avg = (sum(r[0] for r in para_results) / len(para_results),
                        sum(r[1] for r in para_results) / len(para_results))
        if len(body_results) > 0:
            body_avg = (sum(r[0] for r in body_results) / len(body_results),
                        sum(r[1] for r in body_results) / len(body_results))

        if result_file:
            # Write the average values to file.
            result_file.write("\nOverall:\t%s\t%s\t%s\n\n" % 
                (word_avg, para_avg, body_avg))

        return (word_avg, para_avg, body_avg)

    def get_groundtruth_file_path(self, actual_file_path):
        '''
        Returns the related groundtruth file for the given result file.
        Returns None if no such groundtruth file exists.
        '''
        rel_path = os.path.relpath(actual_file_path, self.args.actual_path)
        return os.path.join(self.args.gt_path, rel_path)
        
    def get_actual_file_path(self, gt_file_path):
        '''
        Returns the related actual file for the given groundtruth file.
        Returns None if no such actual file exists.
        '''
        rel_path = os.path.relpath(gt_file_path, self.args.gt_path)
            
        # Groundtruth file may have an extended file extension like 
        # "cond-mat0001228.full.txt" or "cond-mat0001228.body.txt"
        # But actual files don't have these extension.
        # For a file name "cond-mat0001228.full.txt", find "cond-mat0001228.txt"
           
        # Find the first dot in the path.
        index_first_dot = rel_path.find('.')
                
        if index_first_dot >= 0:
            # ext_file_extension = ".full.txt"
            ext_file_extension = rel_path[index_first_dot:]
            # Find the last dot in the extended file extension.
            index_last_dot = ext_file_extension.rfind('.')
            if index_last_dot >= 0:
                # ext_file_extension = ".txt"
                file_extension = ext_file_extension[index_last_dot:]
                # Replace the extended file extension by the simple one.
                rel_path = rel_path.replace(ext_file_extension, file_extension)
            
        return os.path.join(self.args.actual_path, rel_path)
        
    def format_actual_file(self, file_path):
        '''
        Reads the given result file and formats it to a proper format.
        '''
        if os.path.isfile(file_path):
            file = open(file_path)
            str = file.read()
            file.close()
            return str
        else:
            return ""

    def format_groundtruth_file(self, file_path):
        '''
        Reads the given groundtruth file and formats it to a proper format.
        '''
        if os.path.isfile(file_path):
            file = open(file_path)
            str = file.read()
            file.close()
            return str
        else:
            return ""

    def evaluate_words_extraction(self):
        '''
        Computes the precision and recall for the results of words extraction.
        Per default, the computation is based on exact matches of words, but you
        can set max_distance > 0 to allow fuzzy matches as well.
        '''  
        # Pattern to split the text into words, without any punctuation marks.        
        pattern = re.compile("[-\s.,!?;:\(\){}\[\]<>\"%&/=\@*\+~#|]")
        max_dist = self.args.max_distance        
        results = []
        prefix = self.args.prefix
        suffix = self.args.word_suffix
        
        # Iterate the groundtruth files.
        for current_dir, dirs, files in os.walk(self.args.gt_path):
            # Only consider the files that matches the prefix and the suffix.
            files = [fi for fi in files if fi.startswith(prefix) \
                                       and fi.endswith(suffix)]
            
            for file in files:
                gt_file_path = os.path.join(current_dir, file)
                                
                # Format the gt file.
                gt = self.format_groundtruth_file(gt_file_path).lower()
                
                if gt:
                    # Find the related actual file.
                    actual_path = self.get_actual_file_path(gt_file_path)

                    if actual_path:
                        actual = self.format_actual_file(actual_path).lower()  
                    
                        # Obtain the words.
                        words = list(filter(None, pattern.split(actual)))
                        gt_words = list(filter(None, pattern.split(gt)))
                               
                        # Compute precision recall values.
                        results.append(self.precision_recall(
                                        words, gt_words, max_dist))
                                        
        return results

    def evaluate_para_extraction(self):
        '''
        Evaluates the results of paragraphs extraction, given the actual result
        and the related groundtruth.
        '''     
        min_sim = self.args.min_similarity       
        prefix = self.args.prefix
        suffix = self.args.para_suffix
        results = []
                
        # Iterate the groundtruth files.
        for current_dir, dirs, files in os.walk(self.args.gt_path):
            # Only consider the files that matches the prefix and the suffix.
            files = [fi for fi in files if fi.startswith(prefix) \
                                       and fi.endswith(suffix)]
            
            for file in files:
                gt_file_path = os.path.join(current_dir, file)
                                
                # Format the gt file.
                gt = self.format_groundtruth_file(gt_file_path).lower()
                
                if gt:
                    # Find the related actual file.
                    actual_path = self.get_actual_file_path(gt_file_path)

                    if actual_path:
                        actual = self.format_actual_file(actual_path).lower()  
                    
                        # Obtain the words.
                        actual_paras = actual.split('\n\n');
                        gt_paras = gt.split('\n\n');
                               
                        # Compute precision recall values.
                        results.append(self.precision_recall(
                                        actual_paras, gt_paras, None, min_sim))
        return results

    def evaluate_body_extraction(self):
        '''
        Evaluates the results of body extraction, given the actual result and
        the related groundtruth.
        '''   
        max_distance = self.args.max_distance       
        prefix = self.args.prefix
        suffix = self.args.body_suffix
        results = []
                
        # Iterate the groundtruth files.
        for current_dir, dirs, files in os.walk(self.args.gt_path):
            # Only consider the files that matches the prefix and the suffix.
            files = [fi for fi in files if fi.startswith(prefix) \
                                       and fi.endswith(suffix)]
            
            for file in files:
                gt_file_path = os.path.join(current_dir, file)
                                
                # Format the gt file.
                gt = self.format_groundtruth_file(gt_file_path).lower()
                
                if gt:
                    # Find the related actual file.
                    actual_path = self.get_actual_file_path(gt_file_path)

                    if actual_path:
                        actual = self.format_actual_file(actual_path).lower()  
                    
                        # Obtain the words.
                        actual_body = actual.split('\n\n');
                        gt_body = gt.split('\n\n');

                        # Compute precision recall values.
                        results.append(self.precision_recall(
                                       actual_body, gt_body, max_distance))
        return results
    
    def precision_recall(self, actuals, gt, max_dist=None, min_similarity=None):        
        if len(actuals) == 0 and len(gt) == 0:
            return (1.0, 1.0)
                            
        # tp = the number of items that occur in actual and gt
        # fn = the number of items that occur only in gt
        # tp = the number of items that occur only in actual
        tp = fn = fp = 0
        
        fuzzy_gt = FuzzySet(gt)
        counter_gt = Counter(gt)
                
        # Iterate the actual items.
        for actual in actuals:
            # Obtain the "best" matches in the groundtruth. 
            fuzzy_matches = fuzzy_gt.get(actual)
                
            if fuzzy_matches:
                fuzzy_match = fuzzy_matches[0]
                fuzzy_match_similarity = fuzzy_match[0]
                fuzzy_match_text = fuzzy_match[1]
                
                if max_dist is not None:
                    max_length = max(len(actual), len(fuzzy_match_text))
                    min_similarity = (max_length - max_dist) / max_length  
                        
                if fuzzy_match_text in counter_gt \
                        and fuzzy_match_similarity >= min_similarity:
                    tp += 1
                    counter_gt += Counter({fuzzy_match_text: -1})
                else:
                    fp += 1

        # Iterate the remaining groundtruth item to obtain the fn value.
        for actual in counter_gt:
            fn += counter_gt[actual]

        precision = tp / (tp + fp) if (tp + fp) > 0 else 0.0
        recall = tp / (tp + fn) if (tp + fn) > 0 else 0.0
        return (round(precision,2), round(recall, 2))

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
