import os
import os.path

import util
import time_util
import file_util

from dotdict import dotdict
import outputter as out

from datetime import datetime
from collections import Counter
from argparse import ArgumentParser
from multiprocessing import Pool, Value, Lock

from doc_diff import doc_diff
from doc_diff_count_num_ops import count_num_ops
from doc_diff_visualize import visualize_diff_phrases
from doc_diff_visualize2 import visualize_diff_phrases as visualize_diff_phrases2

class Evaluator:
    """ 
    Evaluates the given PDF extraction tool.
    """
    
    counter_processed_gt_files = 0
    num_total_gt_files = 0
    
    def __init__(self, args):
        """
        Creates a new evaluator based on the given args.
        """
        self.args = self.validate_and_prepare_args(args)
        self.out  = out.Outputter(width=140)
    
    # --------------------------------------------------------------------------
    
    def validate_and_prepare_args(self, args):
        """
        Prepares the given arguments such that they fits our needs. Returns
        a dotdict filled with the arguments.
        """
        
        # 'args' is given as Namespace object. Transform it to dotdict.
        prepared = dotdict(args)
        
        # Read key/value pairs from the tool info file.
        tool_info = util.read_tool_info(prepared.tool_root)
        prepared.update(tool_info)
                
        # The junk is given as string. Split it to get a list of junk tokens.
        junk = prepared.junk
        if junk is not None:
            prepared.junk = junk.split()
        else:
            prepared.junk = []
        
        # The dir filter is given as string. Split it to get a list of filters.    
        dir_filter = prepared.dir_filter
        if dir_filter is not None:
            prepared.dir_filter = dir_filter.split()
        else:
            prepared.dir_filter = []   
        
        # Set num_threads to None if it is <= 0.
        num_threads = prepared.num_threads
        if num_threads is not None:
            prepared.num_threads = num_threads if num_threads > 0 else None
        
        return prepared
         
    def init_global_vars(self, counter_processed_files, num_total_files):
        """ 
        Initializes some global variables to be able to use them within the 
        threads.
        """
        global counter_processed_gt_files
        global num_total_gt_files
        
        counter_processed_gt_files = counter_processed_files 
        num_total_gt_files = num_total_files
           
    # --------------------------------------------------------------------------
    # Processing methods.
                
    def process(self):
        """ 
        Starts the evaluation process. 
        """
        
        self.handle_process_start()
        
        # Collect the tool files to process.
        self.handle_collect_gt_files_start()
        gt_files = self.collect_gt_files()
        self.handle_collect_gt_files_end(gt_files)
                
        # Define a counter to count already processed gt files.
        counter_processed_files = Value('i', 0)
        
        # Define a pool to process the files *in parallel*.
        pool = Pool(
            processes   = self.args.num_threads,
            initializer = self.init_global_vars,
            initargs    = (counter_processed_files, len(gt_files))
        )
        
        # Start the evaluation. 
        self.handle_evaluation_start()
        stats = pool.map(self.process_gt_file, gt_files)
        self.handle_evaluation_end(stats)
                       
        pool.close()
        pool.join()
                          
        self.handle_process_end()
    
    def process_gt_file(self, gt_file_path):
        """ 
        Processes the given groundtruth file.
        """
        global counter_processed_gt_files
        global num_total_gt_files

        stats = dotdict()
        stats.gt_file_path = gt_file_path
        # Don't proceed, if the gt file is empty.
        if file_util.is_missing_or_empty_file(gt_file_path):
            stats.is_gt_file_missing = True
            return stats
        
        # Obtain the path to related tool file.
        tool_file_path = self.get_tool_file_path(gt_file_path)
        stats.tool_file_path = tool_file_path
        # Don't proceed, if the tool file doesn't exist.
        if file_util.is_missing_or_empty_file(tool_file_path):
            stats.is_tool_file_missing = True
            return stats
        
        # Obtain the path to related pdf file.
        pdf_file_path  = self.get_pdf_file_path(gt_file_path)
        stats.pdf_file_path = pdf_file_path            
        # Don't proceed, if the pdf file doesn't exist.
        if file_util.is_missing_or_empty_file(pdf_file_path):
            stats.is_pdf_file_missing = True
            return stats
                        
        # Read gt file and tool file.
        
        # The gt file could contain headers starting with "##" containing some
        # metadata.
        gt_lines = []
        source_tex_file = None
        if not file_util.is_missing_or_empty_file(gt_file_path):
            with open(gt_file_path) as f: 
                for line in f: 
                    if line.startswith("##source"):
                        _, source_tex_file = line.split("\t")
                    else:
                        gt_lines.append(line)
        gt = "".join(gt_lines)
                
        #gt = file_util.read_file(gt_file_path)
        tool_output = file_util.read_file(tool_file_path)
                                                                             
        # Compute evaluation result.
        evaluation_result = self.process_strings(gt, tool_output)
        evaluation_result["source_tex_file"] = source_tex_file
        
        # Lock the counter, because += operation is not atomic
        with counter_processed_gt_files.get_lock():
            counter_processed_gt_files.value += 1
        
        stats.counter = counter_processed_gt_files.value
                                
        # Handle the result.
        self.handle_evaluation_result(stats, evaluation_result)
                                      
        return stats
    
    def process_strings(self, gt, actual):
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
        
        return doc_diff(actual, gt, self.args.junk, self.args.junk)
       
    # --------------------------------------------------------------------------
    # Handler methods.
    
    def handle_process_start(self):
        """ 
        This method is called when processing was started.
        """
        # Otput some statistics.
        tool_name = self.args.tool_name
        self.out.print_heading("Evaluation of tool %s" % tool_name)                
        
        self.out.set_column_widths(30, -1)  
        self.out.print_columns("Tool root: ", self.args.tool_root)
        self.out.print_columns("Groundtruth root: ", self.args.gt_root)
        self.out.print_columns("Pdf root: ", self.args.pdf_root)
        self.out.print_columns("Directory Filter:", self.args.dir_filter)
        self.out.print_columns("Prefix:", self.args.prefix)
        self.out.print_columns("Suffix:", self.args.suffix)
        self.out.print_columns("Rearrange: ", self.args.rearrange)
        self.out.print_columns("Case-insensitive:", self.args.ignore_cases)
        self.out.print_columns("Ignore whitespace:", self.args.remove_spaces)
        self.out.print_columns("Junk: ", self.args.junk)
        self.out.print_gap()
        
    def handle_collect_gt_files_start(self):
        """
        This method is called when collecting the gt files was started.
        """
        self.out.print_text("Collecting groundtruth files ...")
        self.out.print_gap() 
        
    def handle_collect_gt_files_end(self, gt_files):
        """
        This method is called when collecting the gt files was finished.
        """
        self.out.print_columns("# Found groundtruth files: ", len(gt_files))
        self.out.print_gap()    
        
    def handle_evaluation_start(self):
        """ 
        This method is called when the actual evaluation was started.
        """
        self.out.print_text("Evaluating ...")  
        self.out.print_gap() 
                
        start_time = datetime.now()        
        self.args.start_time = start_time
        start_time_str = time_util.format_time(start_time)
        self.out.print_columns("Start time: ", start_time_str)
     
        self.out.set_column_widths(12, 15, 14, 14, 14, 14, 14, 14, 14, 14)
        self.out.print_columns_headers(
            "#", 
            "File", 
            " #p. splits", 
            " #p. merges", 
            " #p. rearr.", 
            " #p. inserts", 
            " #p. deletes", 
            " #w. inserts", 
            " #w.deletes", 
            " #w.replaces"
        )
    
    def handle_evaluation_result(self, stats, evaluation_result):    
        """ This method is called whenever a tool file was evaluated."""             
        
        global counter_processed_gt_files
        global num_total_gt_files
        
        if not stats:
            return
        
        # Don't proceed, if gt file is missing/empty.
        if stats.is_gt_file_missing:
            return
        
        # Don't proceed, if pdf file is missing/empty.
        if stats.is_pdf_file_missing:
            return
        
        # Don't proceed, if tool file is missing/empty.
        if stats.is_tool_file_missing:
            return
        
        # ----------------------------------------------------------------------
        # Create table row.
        
        gt_file_path = stats.gt_file_path
        gt_basename = self.get_gt_basename_from_file_path(gt_file_path)
        
        # Compute delta values.
        num_ops, percental_num_ops = count_num_ops(evaluation_result, self.args.junk)
        prev_num_ops = self.deserialize_prev_num_ops(gt_file_path) 
        
        # Make the num_ops available for table footer.
        stats.num_ops = num_ops
        stats.percental_num_ops = percental_num_ops
        stats.prev_num_ops = prev_num_ops
        
        delta_vals = self.compute_delta_values(num_ops, prev_num_ops, percental_num_ops)
              
        if delta_vals: 
            para_splits, percental_para_splits, para_splits_delta = delta_vals["para_splits"]
            para_merges, percental_para_merges, para_merges_delta = delta_vals["para_merges"]
            para_rearr, percental_para_rearr, para_rearr_delta = delta_vals["para_rearranges"]
            para_inserts, percental_para_inserts, para_inserts_delta = delta_vals["para_inserts"]
            para_deletes, percental_para_deletes, para_deletes_delta = delta_vals["para_deletes"]        
            word_inserts, percental_word_inserts, word_inserts_delta = delta_vals["word_inserts"]
            word_deletes, percental_word_deletes, word_deletes_delta = delta_vals["word_deletes"]
            word_replaces, percental_words_replaces, word_replaces_delta = delta_vals["word_replaces"]
                    
            # Compose the row.   
            self.out.print_columns(
                "%d/%d" % (stats.counter, num_total_gt_files),
                gt_basename,
                self.print_cell(para_splits, percental_para_splits, para_splits_delta),
                self.print_cell(para_merges, percental_para_merges, para_merges_delta),
                self.print_cell(para_rearr, percental_para_rearr, para_rearr_delta),
                self.print_cell(para_inserts, percental_para_inserts, para_inserts_delta),
                self.print_cell(para_deletes, percental_para_deletes, para_deletes_delta),
                self.print_cell(word_inserts, percental_word_inserts, word_inserts_delta),
                self.print_cell(word_deletes, percental_word_deletes, word_deletes_delta),
                self.print_cell(word_replaces, percental_words_replaces, word_replaces_delta)
            )
        
        # ----------------------------------------------------------------------
                    
        # Visualize the evaluation result.
        vis_file_path = self.define_visualization_file_path(gt_file_path)
        self.visualize_evaluation_result(evaluation_result, vis_file_path)
            
        # Serialize the num ops.
        serial_file_path = self.define_serialization_file_path(gt_file_path)
        self.serialize_num_ops(num_ops, serial_file_path)
    
    def handle_evaluation_end(self, stats):
        """ 
        This method is called when all pdf files were processed.
        """
        end_time = self.args.end_time = datetime.now()        
        end_time_str = time_util.format_time(end_time)

        # Iterate through the stats to create the summary.
        num_ops = []
        percental_num_ops = []
        prev_num_ops = []
        
        # Count missing files.
        missing_gt_files = []
        missing_pdf_files = []
        missing_tool_files = []
        
        for stat in stats:
            if not stat:
                continue
                        
            # Keep track of missing gt files.
            if stat.is_gt_file_missing:
                missing_gt_files.append(stat)
                continue
                
            # Keep track of missing pdf files.
            if stat.is_pdf_file_missing:
                missing_pdf_files.append(stat)
                continue
                
            # Keep track of missing tool files.
            if stat.is_tool_file_missing:
                missing_tool_files.append(stat)
                continue
            
            if stat.num_ops:
                num_ops.append(stat.num_ops)
            else:
                # There are some groundtruth file containing only "[formula]".
                # Theses files are definitely corrupt. So add it to missing
                # gt files. TODO
                missing_gt_files.append(stat)
                continue
            
            if stat.percental_num_ops:
                percental_num_ops.append(stat.percental_num_ops)
                
            if stat.prev_num_ops:
                prev_num_ops.append(stat.prev_num_ops)
        
        # ----------------------------------------------------------------------
        # Print table footer row.
        
        # Compute summarized delta values.     
        delta_vals = self.compute_delta_values(num_ops, prev_num_ops, percental_num_ops) 
                      
        if delta_vals: 
            para_splits, percental_para_splits, para_splits_delta = delta_vals["para_splits"]
            para_merges, percental_para_merges, para_merges_delta = delta_vals["para_merges"]
            para_rearr, percental_para_rearr, para_rearr_delta = delta_vals["para_rearranges"]
            para_inserts, percental_para_inserts, para_inserts_delta = delta_vals["para_inserts"]
            para_deletes, percental_para_deletes, para_deletes_delta = delta_vals["para_deletes"]        
            word_inserts, percental_word_inserts, word_inserts_delta = delta_vals["word_inserts"]
            word_deletes, percental_word_deletes, word_deletes_delta = delta_vals["word_deletes"]
            word_replaces, percental_words_replaces, word_replaces_delta = delta_vals["word_replaces"]
                    
            # Compose the row.   
            self.out.print_columns_footers(
                "Total:",
                "",
                self.print_cell(para_splits, percental_para_splits, para_splits_delta),
                self.print_cell(para_merges, percental_para_merges, para_merges_delta),
                self.print_cell(para_rearr, percental_para_rearr, para_rearr_delta),
                self.print_cell(para_inserts, percental_para_inserts, para_inserts_delta),
                self.print_cell(para_deletes, percental_para_deletes, para_deletes_delta),
                self.print_cell(word_inserts, percental_word_inserts, word_inserts_delta),
                self.print_cell(word_deletes, percental_word_deletes, word_deletes_delta),
                self.print_cell(word_replaces, percental_words_replaces, word_replaces_delta)
            )
            self.out.print_gap()
        
        # ----------------------------------------------------------------------
        
        # Compute runtimes
        runtime = self.args.runtime = self.args.end_time - self.args.start_time
        runtime_str = time_util.format_time_delta(runtime)
        
        avg_runtime = self.args.avg_runtime = runtime / len(num_ops)
        avg_runtime_str = time_util.format_time_delta(avg_runtime)
                        
        # Create tex row.                
        tex_row = self.create_tex_table_row(delta_vals)
        
        self.out.set_column_widths(30, -1)
        self.out.print_columns("End time: ", end_time_str)
        self.out.print_columns("Total runtime: ", runtime_str)
        self.out.print_columns("Avg. runtime: ", avg_runtime_str)
        self.out.print_columns("# evaluated files: ", len(num_ops))
        self.out.print_columns("# corrupt gt files: ", len(missing_gt_files))
        self.out.print_columns("# missing pdf files: ", len(missing_pdf_files))
        self.out.print_columns("# missing tool files: ", len(missing_tool_files))
        self.out.print_columns("TeX Table Row: ", tex_row)
                
    def handle_process_end(self):
        """ This method is called when processing was finished."""
        pass
    
    # --------------------------------------------------------------------------
    
    def print_cell(self, value, percental_value, delta):
        """ Creates textual content for a table cell from given value and
        given delta. """
        parts = []
        
        delta_str = "±0.0" if delta == 0 else "%+.1f" % delta
        percental_str = "%.1f%%" % (percental_value * 100)
        
        parts.append(("%s" % value).rjust(6))
        parts.append(("(%s)" % percental_str).rjust(8))
        return "".join(parts)
                       
    # --------------------------------------------------------------------------
    # Computing delta values.
    
    def compute_delta_values(self, num_ops, prev_num_ops, percental_num_ops):
        """ Computes delta values. """
                   
        if not num_ops:
            return
     
        def get(dictionary, key, default):
            if dictionary is None:
                return default 
            return dictionary.get(key, default)
                    
        # num_ops may be a single counter (if the values are computed for a 
        # single result) or a list of counters (if the values are computed 
        # for the footer).
        if isinstance(num_ops, list):
            num = len(num_ops)
            
            num_para_splits  = [get(x, "num_para_splits", 0) for x in num_ops]
            para_splits      = round(sum(num_para_splits) / num, 1)
            num_para_merges  = [get(x, "num_para_merges", 0) for x in num_ops]
            para_merges      = round(sum(num_para_merges) / num, 1)
            num_para_rearr   = [get(x, "num_para_rearranges", 0) for x in num_ops]
            para_rearr       = round(sum(num_para_rearr) / num, 1)
            num_para_inserts = [get(x, "num_para_inserts", 0) for x in num_ops]
            para_inserts     = round(sum(num_para_inserts) / num, 1)
            num_para_deletes = [get(x, "num_para_deletes", 0) for x in num_ops]
            para_deletes     = round(sum(num_para_deletes) / num, 1)
            num_word_inserts = [get(x, "num_word_inserts", 0) for x in num_ops]
            word_inserts     = round(sum(num_word_inserts) / num, 1)
            num_word_deletes = [get(x, "num_word_deletes", 0) for x in num_ops]
            word_deletes     = round(sum(num_word_deletes) / num, 1)
            num_word_replacs = [get(x, "num_word_replaces", 0) for x in num_ops]
            word_replaces    = round(sum(num_word_replacs) / num, 1)
            
            percental_num_para_splits  = [get(x, "num_para_splits", 0) for x in percental_num_ops]
            percental_para_splits      = sum(percental_num_para_splits) / num
            percental_num_para_merges  = [get(x, "num_para_merges", 0) for x in percental_num_ops]
            percental_para_merges      = sum(percental_num_para_merges) / num
            percental_num_para_rearr   = [get(x, "num_para_rearranges", 0) for x in percental_num_ops]
            percental_para_rearr       = sum(percental_num_para_rearr) / num
            percental_num_para_inserts = [get(x, "num_para_inserts", 0) for x in percental_num_ops]
            percental_para_inserts     = sum(percental_num_para_inserts) / num
            percental_num_para_deletes = [get(x, "num_para_deletes", 0) for x in percental_num_ops]
            percental_para_deletes     = sum(percental_num_para_deletes) / num
            percental_num_word_inserts = [get(x, "num_word_inserts", 0) for x in percental_num_ops]
            percental_word_inserts     = sum(percental_num_word_inserts) / num
            percental_num_word_deletes = [get(x, "num_word_deletes", 0) for x in percental_num_ops]
            percental_word_deletes     = sum(percental_num_word_deletes) / num
            percental_num_word_replacs = [get(x, "num_word_replaces", 0) for x in percental_num_ops]
            percental_word_replaces    = sum(percental_num_word_replacs) / num
        else:
            para_splits   = num_ops['num_para_splits']
            para_merges   = num_ops['num_para_merges']
            para_rearr    = num_ops['num_para_rearranges']
            para_inserts  = num_ops['num_para_inserts']
            para_deletes  = num_ops['num_para_deletes']
            word_inserts  = num_ops['num_word_inserts']
            word_deletes  = num_ops['num_word_deletes']
            word_replaces = num_ops['num_word_replaces']
            
            percental_para_splits   = percental_num_ops['num_para_splits']
            percental_para_merges   = percental_num_ops['num_para_merges']
            percental_para_rearr    = percental_num_ops['num_para_rearranges']
            percental_para_inserts  = percental_num_ops['num_para_inserts']
            percental_para_deletes  = percental_num_ops['num_para_deletes']
            percental_word_inserts  = percental_num_ops['num_word_inserts']
            percental_word_deletes  = percental_num_ops['num_word_deletes']
            percental_word_replaces = percental_num_ops['num_word_replaces']
            
        if prev_num_ops and isinstance(prev_num_ops, list):
            num = len(prev_num_ops)
            
            num_para_splits    = [get(x, "num_para_splits", 0) for x in prev_num_ops]
            last_para_splits   = round(sum(num_para_splits) / num, 1)
            num_para_merges    = [get(x, "num_para_merges", 0) for x in prev_num_ops]
            last_para_merges   = round(sum(num_para_merges) / num, 1)
            num_para_rearr     = [get(x, "num_para_rearranges", 0) for x in prev_num_ops]
            last_para_rearr    = round(sum(num_para_rearr) / num, 1)
            num_para_inserts   = [get(x, "num_para_inserts", 0) for x in prev_num_ops]
            last_para_inserts  = round(sum(num_para_inserts) / num, 1)
            num_para_deletes   = [get(x, "num_para_deletes", 0) for x in prev_num_ops]
            last_para_deletes  = round(sum(num_para_deletes) / num, 1)
            num_word_inserts   = [get(x, "num_word_inserts", 0) for x in prev_num_ops]
            last_word_inserts  = round(sum(num_word_inserts) / num, 1)
            num_word_deletes   = [get(x, "num_word_deletes", 0) for x in prev_num_ops]
            last_word_deletes  = round(sum(num_word_deletes) / num, 1)
            num_word_replaces  = [get(x, "num_word_replaces", 0) for x in prev_num_ops]
            last_word_replaces = round(sum(num_word_replaces) / num, 1)
        else:
            last_para_splits   = prev_num_ops['num_para_splits'] if prev_num_ops else 0
            last_para_merges   = prev_num_ops['num_para_merges'] if prev_num_ops else 0
            last_para_rearr    = prev_num_ops['num_para_rearranges'] if prev_num_ops else 0
            last_para_inserts  = prev_num_ops['num_para_inserts'] if prev_num_ops else 0
            last_para_deletes  = prev_num_ops['num_para_deletes'] if prev_num_ops else 0
            last_word_inserts  = prev_num_ops['num_word_inserts'] if prev_num_ops else 0
            last_word_deletes  = prev_num_ops['num_word_deletes'] if prev_num_ops else 0
            last_word_replaces = prev_num_ops['num_word_replaces'] if prev_num_ops else 0
        
        # Compute the difference to the last values.
        para_splits_delta   = para_splits   - last_para_splits
        para_merges_delta   = para_merges   - last_para_merges
        para_rearr_delta    = para_rearr    - last_para_rearr
        para_inserts_delta  = para_inserts  - last_para_inserts
        para_deletes_delta  = para_deletes  - last_para_deletes
        word_inserts_delta  = word_inserts  - last_word_inserts
        word_deletes_delta  = word_deletes  - last_word_deletes
        word_replaces_delta = word_replaces - last_word_replaces
           
        return {
            "para_splits":     (para_splits, percental_para_splits, para_splits_delta),
            "para_merges":     (para_merges, percental_para_merges, para_merges_delta),
            "para_rearranges": (para_rearr, percental_para_rearr, para_rearr_delta),
            "para_inserts":    (para_inserts, percental_para_inserts, para_inserts_delta),
            "para_deletes":    (para_deletes, percental_para_deletes, para_deletes_delta),
            "word_inserts":    (word_inserts, percental_word_inserts, word_inserts_delta),
            "word_deletes":    (word_deletes, percental_word_deletes, word_deletes_delta),
            "word_replaces":   (word_replaces, percental_word_replaces, word_replaces_delta),
        }
        
    # -------------------------------------------------------------------------
    # Serialize and deserialize.
    
    def serialize_num_ops(self, num_ops, file_path):
        """ 
        Serializes the given num_ops. 
        """     
        # Append line in serialization file.
        with open(file_path, "a+") as f:
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
     
    def deserialize_prev_num_ops(self, gt_file_path):
        """ 
        Deserializes the previous stats related to the given stats. 
        """
        serial_file_path = self.define_serialization_file_path(gt_file_path)
        
        # Abort if there is no such serialization file.
        if file_util.is_missing_or_empty_file(serial_file_path):
            return
        
        # Iterate through the lines of file to get only the last line.
        prev_num_ops = []
        with open(serial_file_path, "r") as f:
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

    def visualize_evaluation_result(self, evaluation_result, file_path):
        """ 
        Visualizes the given evaluation result. 
        """
        with open(file_path, "w+") as f:
            f.write(visualize_diff_phrases(evaluation_result, self.args.junk))
       
        visualize_diff_phrases2(evaluation_result, self.args.junk) 
       
    # --------------------------------------------------------------------------
    # Create tex table row.
   
    def create_tex_table_row(self, delta_vals):
        """ Creates a row in the tex table in our paper for given values. """ 
        if not delta_vals:
            return
            
        para_splits, percental_para_splits, para_splits_delta = delta_vals["para_splits"]
        para_merges, percental_para_merges, para_merges_delta = delta_vals["para_merges"]
        para_rearr, percental_para_rearr, para_rearr_delta = delta_vals["para_rearranges"]
        para_inserts, percental_para_inserts, para_inserts_delta = delta_vals["para_inserts"]
        para_deletes, percental_para_deletes, para_deletes_delta = delta_vals["para_deletes"]        
        word_inserts, percental_word_inserts, word_inserts_delta = delta_vals["word_inserts"]
        word_deletes, percental_word_deletes, word_deletes_delta = delta_vals["word_deletes"]
        word_replaces, percental_word_replaces, word_replaces_delta = delta_vals["word_replaces"]
        
        parts = []
                
        parts.append("& \TDD{%.1f}{%.1f}" % (para_merges, percental_para_merges * 100))
        parts.append("\t\t")
        parts.append("& \TDD{%.1f}{%.1f}" % (para_splits, percental_para_splits * 100))
        parts.append("\t\t")
        parts.append("& \TDD{%.1f}{%.1f}" % (para_deletes, percental_para_deletes * 100))
        parts.append("\t\t")
        parts.append("& \TDD{%.1f}{%.1f}" % (para_inserts, percental_para_inserts * 100))
        parts.append("\t\t")
        parts.append("& \TDD{%.1f}{%.1f}" % (para_rearr, percental_para_rearr * 100))
        parts.append("\t\t\t")
        parts.append("& \TDD{%.1f}{%.1f}" % (word_deletes, percental_word_deletes * 100))
        parts.append("\t\t")
        parts.append("& \TDD{%.1f}{%.1f}" % (word_inserts, percental_word_inserts * 100))
        parts.append("\t\t")
        parts.append("& \TDD{%.1f}{%.1f}" % (word_replaces, percental_word_replaces * 100))
        parts.append("\t\t")
        parts.append("\TDeol")
        
        return "".join(parts)
    
    # --------------------------------------------------------------------------
    # Path definitions.                  
    
    def get_tool_file_path(self, gt_file_path):
        """ 
        Returns the path to tool file related to the given groundtruth file.

        Example:
        Input:  gt_file_path = output/groundtruth/0001/cond-mat0001419.body.txt
        Output: tool_file_path = output/pdftotext/0001/cond-mat0001419.txt         
        """
        
        # Obtain the directory and name of gt file from gt_file_path.
        gt_dir, gt_file_name = os.path.split(gt_file_path)
        # Obtain the path to the gt dir relative to the input dir.
        rel_gt_dir = os.path.relpath(gt_dir, self.args.gt_root)

        # Split the gt_file_name into basename and extension.
        gt_basename, gt_extension = self.split_gt_ext(gt_file_name)

        # Compose the tool file path.
        tool_dir = os.path.join(self.args.tool_root, rel_gt_dir)
        tool_file_name = "%s%s" % (gt_basename, ".txt")
        tool_file_path = os.path.join(tool_dir, tool_file_name)
            
        return tool_file_path
    
    def get_pdf_file_path(self, gt_file_path):
        """ 
        Returns the path to pdf file related to the given groundtruth file.

        Example:
        Input:  gt_file_path = output/groundtruth/0001/cond-mat0001419.body.txt
        Output: pdf_file_path = input/pdf/0001/cond-mat0001419.pdf         
        """    
        
        # Obtain the directory and name of gt file from gt_file_path.
        gt_dir, gt_file_name = os.path.split(gt_file_path)
        # Obtain the path to the gt dir relative to the input dir.
        rel_gt_dir = os.path.relpath(gt_dir, self.args.gt_root)
        
        # Split the gt_file_name into basename and extension.
        gt_basename, gt_extension = self.split_gt_ext(gt_file_name)
        
        # Compose the pdf_file_path.
        pdf_dir = os.path.join(self.args.pdf_root, rel_gt_dir)
        pdf_file_name = "%s%s" % (gt_basename, ".pdf")
        pdf_file_path = os.path.join(pdf_dir, pdf_file_name)
        
        return pdf_file_path
    
    def get_gt_basename_from_file_path(self, gt_file_path):
        """ 
        Returns the basename of groundtruth file from given file *path*.
        
        >>> self.get_gt_basename_from_file_path("/xxx/yyy/cond-mat01.body.txt")
        cond-mat01
        """
        gt_dir, gt_file_name = os.path.split(gt_file_path)
        return self.get_gt_basename_from_file_name(gt_file_name)
    
    def get_gt_basename_from_file_name(self, gt_file_name):
        """ 
        Returns the basename of groundtruth file from given file *name*.
        
        >>> self.get_gt_basename_from_file_name("cond-mat01.body.txt")
        cond-mat01
        """
        gt_basename, gt_ext = self.split_gt_ext(gt_file_name)
        return gt_basename
       
    def split_gt_ext(self, gt_file_path):
        """ 
        Splits the given gt_file_path into basename and extension. For path 
        "cond-mat0001419.body.txt" this method returns "cond-mat0001419" and
        ".body.txt".
        """
        return gt_file_path.split(".", 1)
       
    def define_visualization_file_path(self, gt_file_path):
        """ 
        Defines the path to the file, where the visualization of the 
        evaluation result for given gt file should be stored. 
        """
        tool_path = self.get_tool_file_path(gt_file_path)
        return util.update_file_extension(tool_path, ".visualization.txt")
       
    def define_serialization_file_path(self, gt_file_path):      
        """
        Defines the path to the file, where the evaluation result for given 
        gt file should be serialized.
        """
        tool_path = self.get_tool_file_path(gt_file_path)
        return util.update_file_extension(tool_path, ".results.txt")
                    
    # --------------------------------------------------------------------------
    # Collecting groundtruth files.
           
    def collect_gt_files(self):
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
        
        input_dir  = self.args.gt_root
        dir_filter = self.args.dir_filter
        prefix     = self.args.prefix
        suffix     = self.args.suffix
        
        return file_util.collect_files(input_dir, dir_filter, prefix, suffix)
                            
    # --------------------------------------------------------------------------
    # Argument parser.
    
    def get_argument_parser():
        """ Creates an parser to parse the command line arguments. """
        parser = ArgumentParser()
                    
        parser.add_argument("tool_root", 
            help="The root of actual files."
        )
        parser.add_argument("gt_root", 
            help="The root of groundtruth files."
        )
        parser.add_argument("pdf_root", 
            help="The root of pdf files."
        )
        parser.add_argument("-p", "--prefix", 
            help="The prefix of groundtruth files", 
            default=""
        )
        parser.add_argument("--suffix", 
            help="The suffix of groundtruth files", 
            default=""
        )
        parser.add_argument("--rearrange", 
            help="Toogle the rearranging of words."
        )
        parser.add_argument("--ignore_cases", 
            help="Toggle case-sensitivity."
        )
        parser.add_argument("--remove_spaces", 
            help="Toggle removing of whitespaces."
        )
        parser.add_argument("--junk", 
            help="The junk to ignore."
        )
        parser.add_argument("--dir_filter", 
            help="The directories to consider", 
            default=""
        )
        parser.add_argument("--num_threads", 
            help="The number of threads to use on extraction.",
            type=int
        )
        
        return parser
        
if __name__ == "__main__": 
    args = Evaluator.get_argument_parser().parse_args() 
    Evaluator(args).process()
