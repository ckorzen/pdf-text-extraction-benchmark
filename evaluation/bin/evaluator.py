import sys
import argparse
import os
import os.path
import util

from datetime import datetime
from collections import Counter
from multiprocessing import Pool

#from doc_diff import doc_diff
#from doc_diff import count_diff_items
#from doc_diff import visualize_diff_stats

from doc_diff import doc_diff
from doc_diff_count_num_ops import count_num_ops
from doc_diff_visualize import visualize_diff_phrases

table_width = 138
first_column_width = 27
other_column_width = 14

# Sometimes, multiprocessing throws "maximum recursion depth exceeded" errors.
#sys.setrecursionlimit(10000)

# TODO: Put all the arguments, computed stats into self.args

from collections import Mapping, Container
from sys import getsizeof
 
class Evaluator:
    """ 
    Evaluates the quality of text extraction of a given PDF extraction tool.
    """
    
    def __init__(self, args):
        """ 
        Creates a new evaluator based on the given args.
        """
        self.args = args
        # Read some infos from external file and append it to the args.
        self.read_external_info(self.args)
                
        # The junk tokens are given as string. Split it to get a list of tokens.
        self.args.junk = self.args.junk.split()
        
        self.args.dir_filter = self.args.dir_filter.split() if self.args.dir_filter else []
        
    def read_external_info(self, args):
        """ Reads some informations from external file to display in output. """
        external_info_file_path = self.get_external_info_file_path()
                
        if not self.is_missing_or_empty(external_info_file_path):
            with open(external_info_file_path) as external_info_file:
                for line in external_info_file:
                    fields = line.strip().split("\t")
                    if len(fields) == 2:
                        setattr(args, fields[0], fields[1])
                     
    # --------------------------------------------------------------------------
        
    def evaluate(self):
        """ 
        Starts the evaluation.
        """
        
        # Obtain the evaluation files. 
        files = self.collect_files()
                       
        # Handle the evaluation start.  
        self.handle_evaluation_start(files)
        
        # Evaluate with given groundtruth files.
        evaluation_stats = []
        if self.args.processing_type == "sequential":
            # Process the groundtruth files *sequentially*.
            for f in files:
                evaluation_stats.append(self.evaluate_file(f))
        else:
            # Process the groundtruth files *in parallel*. 
            pool = Pool()
            evaluation_stats = pool.map(self.evaluate_file, files)
                          
        # Handle the evaluation statss.
        self.handle_evaluation_end(evaluation_stats)
       
    # --------------------------------------------------------------------------
    # Handler methods.
    
    def handle_evaluation_start(self, files):
        """ 
        Handles the start of the evaluation.
        """  
        
        self.args.files      = files
        self.args.start_time = datetime.now()
              
        self.print_overview_table_header()
    
    # Keep diff_phrases separated from stats because it tooks too much memory.    
    def handle_evaluation_result(self, stats, diff_phrases):    
        """ 
        Handles a single evaluation stats.
        """               
                
        # Don't proceed if there is no stats or if the tool output is corrupt.
        if (stats
            and not stats.get("missing_gt_file", False)
            and not stats.get("missing_pdf_file", False)
            and not stats.get("missing_tool_file", False)):
            
            # Add row for the stats in overview table.
            self.print_overview_table_row(stats) 
            
            # Visualize the evaluation stats.
            self.visualize(stats, diff_phrases)
            
            # Serialize the evaluation stats.            
            self.serialize(stats)
                
    def handle_evaluation_end(self, stats):
        """ 
        Handles the end of evaluation.
        """
        
        self.args.end_time = datetime.now()
        
        self.print_overview_table_footer(stats)
        
    # --------------------------------------------------------------------------
        
    def evaluate_file(self, f):
        """ 
        Evaluates the given file and returns the stats.
        """
        
        gt_path   = f["gt_path"]
        pdf_path  = f["pdf_path"]
        tool_path = f["tool_path"]
        
        stats = dict()
        stats["file"] = f
               
        # Don't proceed, if the gt file doesn't exist.
        if self.is_missing_or_empty(gt_path):
            stats["missing_gt_file"] = True
            return stats
            
        # Don't proceed, if the pdf file doesn't exist.
        if self.is_missing_or_empty(pdf_path):
            stats["missing_pdf_file"] = True
            return stats
                
        # Read and format the groundtruth file.
        gt = self.format_gt_file(gt_path)
        # Read and format the tool's output.
        tool_output = self.format_tool_file(tool_path)
                
        # Don't proceed, if the gt is empty.                        
        if not gt:
            stats["missing_gt_file"] = True
            return stats
                  
        # Don't proceed, if the tool output is empty.
        if not tool_output:
            stats["missing_tool_file"] = True
            return stats
                                         
        # Compute evaluation stats.
        diff_phrases          = self.evaluate_strings(gt, tool_output)
        stats["num_ops"]      = count_num_ops(diff_phrases, self.args.junk)
        stats["prev_num_ops"] = self.deserialize(stats)
                
        # Trigger the event here to interact with user immediately, even in 
        # case of parallel processing.
        self.handle_evaluation_result(stats, diff_phrases)
                                      
        return stats
                    
    def evaluate_strings(self, gt, actual):
        """
        Computes precision and recall of words extraction. For that, run diff 
        on the set of words of groundtruth (gt) and the actual extraction 
        stats (actual). The precision of actual follows from the percentage of
        the number of common words to the number of extracted words. The recall 
        follows from the percentage of the number of common words to the number 
        of all words in the groundtruth.  
        We only want to evaluate the accuracy of words extraction, but not to 
        evaluate the correct order of extracted words. Thus, we try tro 
        rearrange the words in the actual stats such that the order of words 
        corresponds to the order in the groundtruth. You can disable the 
        rearrange step by setting the rearrange flag to False. 
        Per default, the evaluation is done case-insensitively. To make it 
        case-sensitive, set the ignore_cases flag to False.
        Per default, the evaluation is based on exact matches of words. To 
        match words with a defined distance as well, adjust max_dist.
        """

        return doc_diff(actual, gt, self.args.junk)

    # --------------------------------------------------------------------------
    # Serialization / Deserialization.
    
    def format_tool_file(self, tool_path):
        """ 
        Reads the given tool file. Override it if you have to do more 
        advanced stuff, like removing semantic markups, etc.
        """
        
        tool_output = ""        
        if not self.is_missing_or_empty(tool_path):
            with open(tool_path) as tool_file: 
                tool_output = tool_file.read()
        
        return tool_output
        
    def format_gt_file(self, gt_path):
        """ 
        Reads the given groundtruth file. Override it if you have to do more 
        advanced stuff, like removing semantic markups, etc.
        """
        
        gt_output = ""
        if not self.is_missing_or_empty(gt_path):
            with open(gt_path) as gt_file: 
                gt_output = gt_file.read()
        
        return gt_output
    
    # --------------------------------------------------------------------------
       
    def serialize(self, stats):
        """
        Serializes the given evaluation stats.
        """
        
        serialization_path = self.define_serialization_path(stats)
        num_ops = stats["num_ops"]

        # Create parent directories, if not existent.
        if not os.path.exists(os.path.dirname(serialization_path)):
            os.makedirs(os.path.dirname(serialization_path), exist_ok=True)
    
        # Append line in serialization file.
        with open(serialization_path, "a") as f:
            fields = []
            fields.append(str(datetime.now()))
            fields.append(str(num_ops.get('num_para_splits', 0)))
            fields.append(str(num_ops.get('num_para_merges', 0)))
            fields.append(str(num_ops.get('num_para_rearranges', 0)))
            fields.append(str(num_ops.get('num_para_inserts', 0)))
            fields.append(str(num_ops.get('num_para_deletes', 0)))
            fields.append(str(num_ops.get('num_word_inserts', 0)))
            fields.append(str(num_ops.get('num_word_deletes', 0)))
            fields.append(str(num_ops.get('num_word_replaces', 0)))

            f.write("\t".join(fields))
            f.write("\n")
       
    def deserialize(self, stats):
        """ 
        Deserializes the previous stats related to the given stats.
        """
         
        serialization_path = self.define_serialization_path(stats)
        
        # Abort if there is no such serialization file.
        if self.is_missing_or_empty(serialization_path):
            return
        
        # Iterate through the lines of file to get only the last line.
        prev_num_ops = []
        with open(serialization_path, "r") as f:
            for line in f:                
                # Ignore first field (date).
                prev_num_ops = [int(x) for x in line.strip().split("\t")[1 : ]]
          
        # Abort if list doesn't contain at least the expected number of fields.
        if len(prev_num_ops) < 8:
            return
          
        counter = Counter()
        counter["num_para_splits"]     = prev_num_ops[0]
        counter["num_para_merges"]     = prev_num_ops[1]
        counter["num_para_rearranges"] = prev_num_ops[2]
        counter["num_para_inserts"]    = prev_num_ops[3]
        counter["num_para_deletes"]    = prev_num_ops[4]
        counter["num_word_inserts"]    = prev_num_ops[5]
        counter["num_word_deletes"]    = prev_num_ops[6]
        counter["num_word_replaces"]   = prev_num_ops[7]
                
        return counter
        
    def visualize(self, stats, diff_phrases):
        """ 
        Visualizes the given evaluation stats.
        """
        visualization_path = self.define_visualization_path(stats)
        
        # Create parent directories, if not existent.
        if not os.path.exists(os.path.dirname(visualization_path)):
            os.makedirs(os.path.dirname(visualization_path), exist_ok=True)
        
        with open(visualization_path, "w") as f:
            f.write(visualize_diff_phrases(diff_phrases, self.args.junk))
       
    # --------------------------------------------------------------------------
    # Methods to print the overview table.
    
    def print_overview_table_header(self):
        """ 
        Prints the header of overview table to stdout.
        """
        
        def hr():
            print("-" * table_width) 
        
        def hrr():
            print("=" * table_width)
            
        def tr(*columns):
            pattern = "%-*s" * len(columns)
            
            params = [first_column_width, columns[0]]
            for i in range(1, len(columns)):
                params.append(other_column_width)
                params.append(columns[i])
                                
            print(pattern % tuple(params))
                        
        tr("")
        tr("Evaluation of tool %s:" % self.args.tool_name)  
        hr()       
        tr("tool root: ", self.args.tool_root)
        tr("groundtruth root: ", self.args.gt_root)
        tr("pdf root: ", self.args.pdf_root)
        tr("dirs:", self.args.dir_filter)
        tr("prefix:", self.args.prefix)
        tr("suffix:", self.args.suffix)
        tr("rearrange: ", self.args.rearrange)
        tr("case-insensitive:", self.args.ignore_cases)
        tr("ignore whitespace:", self.args.remove_spaces)
        tr("junk: ", self.args.junk)
        tr("processing type: ", self.args.processing_type)
        tr("")
        tr("# files to evaluate:",  len(self.args.files))
        tr("start time: ", self.args.start_time)
        hr()
        tr("")
        tr("")        
        tr("statss: ", "#p. splits", "#p. merges", "#p. rearr.", "#p. inserts", 
            "#p. deletes", "#w. inserts", "w.deletes", "w.replaces")
        hr()
              
    def print_overview_table_row(self, stats):
        """
        Prints a row in overview table for given stats.
        """
        
        gt_file         = stats["file"]["gt_file"]
        num_ops         = stats["num_ops"]
        prev_num_ops    = stats["prev_num_ops"]
        
        delta_values = self.compute_delta_values(num_ops, prev_num_ops)
        row = self.create_overview_table_row(gt_file, delta_values)
        if row:
            print(row)
        
    def print_overview_table_footer(self, statss):
        """ 
        Prints the footer of overview table to stdout.
        """
        print("-" * 138)
               
        # Iterate through the statss to create the summary.
        num_ops = []
        prev_num_ops = []
        
        missing_gt_files = []
        missing_pdf_files = []
        missing_tool_files = []
        
        for stats in statss:
            # Ignore None statss.
            if not stats:
                continue
                        
            # Keep track of statss with missing gt file.
            if stats.get("missing_gt_file", False):
                missing_gt_files.append(stats)
                continue
                
            # Keep track of statss with missing pdf file.
            if stats.get("missing_pdf_file", False):
                missing_pdf_files.append(stats)
                continue
                
            # Keep track of statss with missing tool file.
            if stats.get("missing_tool_file", False):
                missing_tool_files.append(stats)
                continue
            
            if stats.get("num_ops", None):
                num_ops.append(stats["num_ops"])
            else:
                # There are some groundtruth file containing only "[formula]".
                # Theses files are definitely corrupt. So add it to missing
                # gt files.
                missing_gt_files.append(stats)
                continue
                
            if stats.get("prev_num_ops", None):
                prev_num_ops.append(stats["prev_num_ops"])
             
        delta_values = self.compute_delta_values(num_ops, prev_num_ops) 
        row = self.create_overview_table_row("Total:", delta_values)
                
        tex_row = self.create_tex_table_row(delta_values)
        if row:
            print(row)
          
        # -------------------
            
        def hr():
            print("-" * table_width) 
        
        def hrr():
            print("=" * table_width)
            
        def tr(*columns):
            pattern = "%-*s" * len(columns)
            
            params = [first_column_width, columns[0]]
            for i in range(1, len(columns)):
                params.append(other_column_width)
                params.append(columns[i])
                                
            print(pattern % tuple(params))
        
        time_needed = self.args.end_time.timestamp() - self.args.start_time.timestamp()
        time_needed_avg = time_needed / len(num_ops) if len(num_ops) > 0 else 0
        
        hr()
        tr("")
        tr("")
        tr("Summary")  
        hr()
        tr("tool root: ", self.args.tool_root)
        tr("groundtruth root: ", self.args.gt_root)
        tr("pdf root: ", self.args.pdf_root)
        tr("dirs:", self.args.dir_filter)
        tr("prefix:", self.args.prefix)
        tr("suffix:", self.args.suffix)
        tr("rearrange: ", self.args.rearrange)
        tr("case-insensitive:", self.args.ignore_cases)
        tr("ignore whitespace:", self.args.remove_spaces)
        tr("junk: ", self.args.junk)
        tr("processing type: ", self.args.processing_type)
        tr("")
        #tr("# files to evaluate:",  len(files))
        tr("start time: ", self.args.start_time)
        tr("end time: ", self.args.end_time)
        tr("total time needed: ",  "%.2fs" % time_needed)
        tr("avg time needed: ",  "%.2fs" % time_needed_avg)
        tr("# evaluated files: ", len(num_ops))
        tr("# corrupt gt files: ", len(missing_gt_files))
        tr("# missing pdf files: ", len(missing_pdf_files))
        tr("# missing tool files: ", len(missing_tool_files))
        tr("TeX Table Row: ", tex_row)
        hr()
    
    # TODO: Change arguments to: def create_overview_table_row(self, stats(s)): 
    def compute_delta_values(self, curr, prev):
        """ 
        Creates a row in overview table for given pair of current value(s)
        and latest value(s).
        """
           
        if not curr:
            return
             
        def get(dictionary, key, default):
            if dictionary is None:
                return default 
            return dictionary.get(key, default)
                    
        # Current may be a single counter (if the row is created for a single 
        # stats) or a list of counters (if the row is created for the footer).
        if isinstance(curr, list):
            num = len(curr)
            
            num_para_splits   = [get(x, "num_para_splits", 0) for x in curr]
            para_splits       = round(sum(num_para_splits) / num, 1)
            num_para_merges   = [get(x, "num_para_merges", 0) for x in curr]
            para_merges       = round(sum(num_para_merges) / num, 1)
            num_para_rearr    = [get(x, "num_para_rearranges", 0) for x in curr]
            para_rearr        = round(sum(num_para_rearr) / num, 1)
            num_para_inserts  = [get(x, "num_para_inserts", 0) for x in curr]
            para_inserts      = round(sum(num_para_inserts) / num, 1)
            num_para_deletes  = [get(x, "num_para_deletes", 0) for x in curr]
            para_deletes      = round(sum(num_para_deletes) / num, 1)
            num_word_inserts  = [get(x, "num_word_inserts", 0) for x in curr]
            word_inserts      = round(sum(num_word_inserts) / num, 1)
            num_word_deletes  = [get(x, "num_word_deletes", 0) for x in curr]
            word_deletes      = round(sum(num_word_deletes) / num, 1)
            num_word_replaces = [get(x, "num_word_replaces", 0) for x in curr]
            word_replaces     = round(sum(num_word_replaces) / num, 1)
        else:
            para_splits     = curr['num_para_splits']
            para_merges     = curr['num_para_merges']
            para_rearr      = curr['num_para_rearranges']
            para_inserts    = curr['num_para_inserts']
            para_deletes    = curr['num_para_deletes']
            word_inserts    = curr['num_word_inserts']
            word_deletes    = curr['num_word_deletes']
            word_replaces   = curr['num_word_replaces']
            
        if prev and isinstance(prev, list):
            num = len(prev)
            
            num_para_splits    = [get(x, "num_para_splits", 0) for x in prev]
            last_para_splits   = round(sum(num_para_splits) / num, 1)
            num_para_merges    = [get(x, "num_para_merges", 0) for x in prev]
            last_para_merges   = round(sum(num_para_merges) / num, 1)
            num_para_rearr     = [get(x, "num_para_rearranges", 0) for x in prev]
            last_para_rearr    = round(sum(num_para_rearr) / num, 1)
            num_para_inserts   = [get(x, "num_para_inserts", 0) for x in prev]
            last_para_inserts  = round(sum(num_para_inserts) / num, 1)
            num_para_deletes   = [get(x, "num_para_deletes", 0) for x in prev]
            last_para_deletes  = round(sum(num_para_deletes) / num, 1)
            num_word_inserts   = [get(x, "num_word_inserts", 0) for x in prev]
            last_word_inserts  = round(sum(num_word_inserts) / num, 1)
            num_word_deletes   = [get(x, "num_word_deletes", 0) for x in prev]
            last_word_deletes  = round(sum(num_word_deletes) / num, 1)
            num_word_replaces  = [get(x, "num_word_replaces", 0) for x in prev]
            last_word_replaces = round(sum(num_word_replaces) / num, 1)
        else:
            last_para_splits   = prev['num_para_splits'] if prev else 0
            last_para_merges   = prev['num_para_merges'] if prev else 0
            last_para_rearr    = prev['num_para_rearranges'] if prev else 0
            last_para_inserts  = prev['num_para_inserts'] if prev else 0
            last_para_deletes  = prev['num_para_deletes'] if prev else 0
            last_word_inserts  = prev['num_word_inserts'] if prev else 0
            last_word_deletes  = prev['num_word_deletes'] if prev else 0
            last_word_replaces = prev['num_word_replaces'] if prev else 0
        
        # Compute the difference to the last values.
        para_splits_delta     = para_splits   - last_para_splits
        para_merges_delta     = para_merges   - last_para_merges
        para_rearr_delta      = para_rearr    - last_para_rearr
        para_inserts_delta    = para_inserts  - last_para_inserts
        para_deletes_delta    = para_deletes  - last_para_deletes
        word_inserts_delta    = word_inserts  - last_word_inserts
        word_deletes_delta    = word_deletes  - last_word_deletes
        word_replaces_delta   = word_replaces - last_word_replaces
           
        return {
            "para_splits":     (para_splits,   para_splits_delta),
            "para_merges":     (para_merges,   para_merges_delta),
            "para_rearranges": (para_rearr,    para_rearr_delta),
            "para_inserts":    (para_inserts,  para_inserts_delta),
            "para_deletes":    (para_deletes,  para_deletes_delta),
            "word_inserts":    (word_inserts,  word_inserts_delta),
            "word_deletes":    (word_deletes,  word_deletes_delta),
            "word_replaces":   (word_replaces, word_replaces_delta),
        }
              
    def create_overview_table_row(self, prefix, delta_values):
        if not delta_values:
            return
            
        def create_delta_str(delta):
            """ Creates a nice-looking str for given dleta value."""
            return "±0.0" if delta == 0 else "%+.1f" % delta
        
        def create_table_cell(value, delta):
            """ Creates textual content for a table cell from given value and
            given delta. """
            parts = []
            parts.append(("%s" % value).rjust(6))
            parts.append(("(%s)" % create_delta_str(delta)).rjust(8))
            return "".join(parts)
        
        para_splits,   para_splits_delta   = delta_values["para_splits"]
        para_merges,   para_merges_delta   = delta_values["para_merges"]
        para_rearr,    para_rearr_delta    = delta_values["para_rearranges"]
        para_inserts,  para_inserts_delta  = delta_values["para_inserts"]
        para_deletes,  para_deletes_delta  = delta_values["para_deletes"]        
        word_inserts,  word_inserts_delta  = delta_values["word_inserts"]
        word_deletes,  word_deletes_delta  = delta_values["word_deletes"]
        word_replaces, word_replaces_delta = delta_values["word_replaces"]
        
        # Compose the row.   
        parts = []
        parts.append(prefix.ljust(25)) # Append the prefix for row. 
        parts.append(create_table_cell(para_splits, para_splits_delta))
        parts.append(create_table_cell(para_merges, para_merges_delta))
        parts.append(create_table_cell(para_rearr, para_rearr_delta))
        parts.append(create_table_cell(para_inserts, para_inserts_delta))
        parts.append(create_table_cell(para_deletes, para_deletes_delta))
        parts.append(create_table_cell(word_inserts, word_inserts_delta))
        parts.append(create_table_cell(word_deletes, word_deletes_delta))
        parts.append(create_table_cell(word_replaces, word_replaces_delta))
        
        return "".join(parts)
        
    def create_tex_table_row(self, delta_values):
        if not delta_values:
            return
            
        para_splits,   para_splits_delta   = delta_values["para_splits"]
        para_merges,   para_merges_delta   = delta_values["para_merges"]
        para_rearr,    para_rearr_delta    = delta_values["para_rearranges"]
        para_inserts,  para_inserts_delta  = delta_values["para_inserts"]
        para_deletes,  para_deletes_delta  = delta_values["para_deletes"]        
        word_inserts,  word_inserts_delta  = delta_values["word_inserts"]
        word_deletes,  word_deletes_delta  = delta_values["word_deletes"]
        word_replaces, word_replaces_delta = delta_values["word_replaces"]
        
        parts = []
        parts.append("& \TD{%.1f}" % para_merges)
        parts.append("\t\t")
        parts.append("& \TD{%.1f}" % para_splits)
        parts.append("\t\t")
        parts.append("& \TD{%.1f}" % para_deletes)
        parts.append("\t\t")
        parts.append("& \TD{%.1f}" % para_inserts)
        parts.append("\t\t")
        parts.append("& \TD{%.1f}" % para_rearr)
        parts.append("\t\t\t")
        parts.append("& \TD{%.1f}" % word_deletes)
        parts.append("\t\t")
        parts.append("& \TD{%.1f}" % word_inserts)
        parts.append("\t\t")
        parts.append("& \TD{%.1f}" % word_replaces)
        parts.append("\t\t")
        parts.append("\TDeol")
        
        return "".join(parts)
                    
    # --------------------------------------------------------------------------
    # Util methods.
      
    def get_external_info_file_path(self):
        return os.path.join(self.args.tool_root, "info.txt")
               
    def collect_files(self):
        """ 
        Scans the root directory of groundtruth files to find groundtruth 
        files that matches the given prefix and suffix. 
        Checks for each groundtruth file, if there is a related pdf file and a 
        related tool file. 
        Returns a list of dictionaries, where each dictionary contains 
        (1) the path of parent directory
        (2) the filename
        (3) the full path (the parent directory joined with the filename)
        of the gt file, pdf file and tool file.
        """
        
        stats_files = []
        gt_root = self.args.gt_root
        prefix = self.args.prefix
        suffix = self.args.suffix
        dir_filter = self.args.dir_filter
                
        # Scan the given root directory of groundtruth files.
        for curr_dir, dirs, files in os.walk(gt_root):
            for gt_file in files:
                cur_dir_parts = curr_dir.split(os.path.sep)
            
                # Continue if dir_filter is set and they don't match the current
                # directory.
                if dir_filter and not self.have_commons(cur_dir_parts, dir_filter):
                    continue 
                        
                # Consider those files that matches the given prefix and suffix.
                if not gt_file.startswith(prefix):
                    continue
                if not gt_file.endswith(suffix):
                    continue;
                              
                # Obtain paths to pdf file.
                pdf_dir, pdf_file = self.get_pdf_paths(curr_dir, gt_file)
                # Obtain paths to tool file.
                tool_dir, tool_file = self.get_tool_paths(curr_dir, gt_file)
            
                file_dict = dict()
                file_dict["gt_dir"]    = curr_dir
                file_dict["gt_file"]   = gt_file
                file_dict["gt_path"]   = os.path.join(curr_dir, gt_file)
                file_dict["pdf_dir"]   = pdf_dir
                file_dict["pdf_file"]  = pdf_file
                file_dict["pdf_path"]  = os.path.join(pdf_dir, pdf_file)
                file_dict["tool_dir"]  = tool_dir
                file_dict["tool_file"] = tool_file
                file_dict["tool_path"] = os.path.join(tool_dir, tool_file)
                                
                stats_files.append(file_dict)
        return stats_files
      
    def get_tool_paths(self, gt_dir, gt_file):
        """ 
        Returns the path to the parent directory and the filename of the 
        tool file, related to the given groundtruth file.
        """
        # Example:
        # gt_root   = output/groundtruth/
        # tool_root = output/pdftotext/ 
        # gt_dir    = output/groundtruth/0001
        # gt_file   = cond-mat0001419.body.txt
        #
        # We want to find:
        # tool_dir = output/pdftotext/0001
        # tool_file = cond-mat0001419.txt         

        # Find the relative gt dir: /0001
        rel_gt_dir = os.path.relpath(gt_dir, self.args.gt_root)
        # Compose the tool dir: tool_root + rel_gt_dir = output/pdftotext/0001
        tool_dir = os.path.join(self.args.tool_root, rel_gt_dir)
        
        # Compute the basename of gt file: cond-mat0001419
        gt_base_name = self.get_gt_base_name(gt_file)
        # Append the correct file extension: cond-mat0001419.txt
        tool_file = gt_base_name + ".txt"
            
        return tool_dir, tool_file
    
    def get_pdf_paths(self, gt_dir, gt_file):
        """ 
        Returns the path to the parent directory and the filename of the 
        pdf file, related to the given groundtruth file.
        """
        # Example:
        # gt_root   = output/groundtruth/
        # pdf_root  = input/pdf/ 
        # gt_dir    = output/groundtruth/0001
        # gt_file   = cond-mat0001419.body.txt
        #
        # We want to find:
        # pdf_dir   = input/pdf/0001
        # pdf_file  = cond-mat0001419.pdf     
        
        # Find the relative gt dir: /0001
        rel_gt_dir = os.path.relpath(gt_dir, self.args.gt_root)
        # Compose the pdf dir: pdf_root + rel_gt_dir = input/pdf/0001
        pdf_dir = os.path.join(self.args.pdf_root, rel_gt_dir)
        
        # Compute the basename of gt file: cond-mat0001419
        gt_base_name = self.get_gt_base_name(gt_file)
        # Append the correct file extension: cond-mat0001419.pdf
        pdf_file = gt_base_name + ".pdf"
        
        return pdf_dir, pdf_file
       
    def get_gt_base_name(self, gt_file):
        """ 
        Returns the base name of given gt file name. For gt file 
        "cond-mat0001419.body.txt" this method returns "cond-mat0001419".
        """
        index_first_dot = gt_file.find(".")
        if index_first_dot > 0:
            return gt_file[0 : index_first_dot]
       
    def define_visualization_path(self, stats):
        """ 
        Defines the path to the file, where the visualization of the 
        given evaluation stats should be stored.
        """
        tool_path = stats["file"]["tool_path"]
        return util.update_file_extension(tool_path, ".visualization.txt")
       
    def define_serialization_path(self, stats):        
        """ 
        Defines the path to the file, where the serialization of the 
        given evaluation stats should be stored.
        """
        tool_path = stats["file"]["tool_path"]
        return util.update_file_extension(tool_path, ".statss.txt")
        
    def is_missing_or_empty(self, file_path):
        """ Returns true, if the given file_path doesn't exist or if the 
        content of file is empty. """
        
        return not os.path.isfile(file_path) or os.path.getsize(file_path) == 0
        
    def have_commons(self, list1, list2):
        return len(list(set(list1) & set(list2))) > 0
        
    # --------------------------------------------------------------------------
        
    def get_argument_parser():
        """ 
        Creates an parser to parse the command line arguments. 
        """
        parser = argparse.ArgumentParser()
        
        def add_arg(names, default=None, help=None, nargs=None, choices=None):
            parser.add_argument(names, default=default, help=help, nargs=nargs, 
                choices=choices)
            
        add_arg("tool_root", help="The root of actual files.")
        add_arg("gt_root", help="The root of groundtruth files.")
        add_arg("pdf_root", help="The root of pdf files.")
        add_arg("--prefix", help="The prefix of groundtruth files", default="")
        add_arg("--suffix", help="The suffix of groundtruth files", default="")
        add_arg("--rearrange", help="Toogle the rearranging of words.")
        add_arg("--ignore_cases", help="Toggle case-sensitivity.")
        add_arg("--remove_spaces", help="Toggle removing of whitespaces.")
        add_arg("--junk", help="The junk to ignore.")
        add_arg("--processing_type", help="'sequential' or 'parallel'", default="parallel")
        add_arg("--dir_filter", help="The directories to consider", default="")
        return parser
        
if __name__ == "__main__":
    Evaluator(Evaluator.get_argument_parser().parse_args()).evaluate() 
