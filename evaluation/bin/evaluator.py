import os
import os.path
import util
import time_util
import file_util
import ast
import diff_utils

from dotdict import dotdict
from datetime import datetime
from collections import Counter
from argparse import ArgumentParser
from multiprocessing import Pool, Value, Lock

import doc_diff
import diff_utils
import doc_diff_visualize

import outputter as out

class Evaluator:
    """ 
    Evaluates a PDF extraction system.
    """
    
    total_num_tool_files = 0
    total_num_processed_tool_files = 0
    
    def __init__(self, args):
        """
        Creates a new evaluator based on the given args.
        """
        self.args = self.validate_and_prepare_args(args)
        self.out  = out.Outputter(width=140, mute=self.args.no_output)
    
    # --------------------------------------------------------------------------
    
    def validate_and_prepare_args(self, args):
        """
        Reads and validates the given command line arguments. 
        Returns a dotdict filled with prepared arguments.
        """
        
        # 'args' is given as Namespace object. Transform it to dotdict.
        args = dotdict(args)
        
        # Read key/value pairs from the tool info file.
        #tool_info = util.read_tool_info(args.tool_root)
        #args.update(tool_info)

        # The junk is given as string. Split it to get a list of junk tokens.
        if args.junk is not None:
            args.junk = args.junk.split()
        else:
            args.junk = []
        
        # The dir filter is given as string. Split it to get a list of filters.    
        if args.dir_filter is not None:
            args.dir_filter = args.dir_filter.split()
        else:
            args.dir_filter = []   
        
        # Set num_threads to None if it is <= 0.
        if args.num_threads is not None and args.num_threads < 0:
            args.num_threads = None
        
        return args
         
    def init_global_vars(self, num_tool_files, num_processed_tool_files):
        """ 
        Initializes some global variables to be able to use them within the 
        threads.
        """
        global total_num_processed_tool_files
        global total_num_tool_files
        
        total_num_processed_tool_files = num_processed_tool_files 
        total_num_tool_files = num_tool_files
           
    # --------------------------------------------------------------------------
    # Processing methods.
    
    def process(self):
        # Tools are given as comma separated string.
        tools = self.args.tools.split()
    
        results = {}
        for tool in tools:
            print(tool)
            self.args.tool = tool
            self.args.tool_root = os.path.join(self.args.tool_root_dir, tool)
            results[tool] = self.process_tool(tool)
        return results


    def process_tool(self, tool):
        """ 
        Evaluates the given tool. 
        """
        
        self.handle_process_start()
        
        self.handle_collect_tool_files_start() 
        # Collect the tool files to process.
        tool_files = self.collect_tool_files()
        self.handle_collect_tool_files_end(tool_files)
        
        # Define a counter to count the processed tool files.
        num_processed_tool_files = Value('i', 0)
        
        # Define a pool to process the files *in parallel*.
        pool = Pool(
            processes   = self.args.num_threads,
            initializer = self.init_global_vars,
            initargs    = (len(tool_files), num_processed_tool_files)
        )
        self.handle_evaluation_start() 
        # Start the evaluation. 
        stats = pool.map(self.process_tool_file, tool_files)
        result = self.handle_evaluation_end(stats)

        pool.close()
        pool.join()

        self.handle_process_end()
        
        return result
    
    def process_tool_file(self, tool_file_path):
        """ 
        Processes the given tool file.
        """
        global total_num_processed_tool_files
        global total_num_tool_files
        
        # Define some evaluation status codes:
        # 0: 	Ok
        # 11: 	Tool file is missing / empty.
        # 12: 	Tool file is corrupt / incomplete.
        # 21: 	GT file is missing / empty. 
        # 22:   GT file is corrupt / incomplete.
        # 31: 	PDF file is missing.
        # 99:   Timeout on extraction.

        stats = dotdict()
        stats.code = 0
        
        #### Process the tool file.
        stats.tool_file_path = tool_file_path
        
        num_ops_snapshot = self.define_num_ops_snapshot_path(tool_file_path)
        if self.args.recompute_num_ops_snapshot or \
                file_util.is_missing_or_empty_file(num_ops_snapshot):
            tool_output = ""
            if file_util.is_missing_or_empty_file(tool_file_path):
                stats.code = 11
            else:
                # Read the tool file.
                # Tool file may include the status code of extraction process.
                tool_file_lines = []
                with open(tool_file_path) as f:
                    for line in f:
                        if line.startswith("##status"):
                            stats.code = int(line.split("\t")[1])
                        else:
                            tool_file_lines.append(line)
                tool_output = "".join(tool_file_lines)

            if stats.code != 0:
                return stats

            #### Process the gt file.
            gt_file_path = self.get_gt_file_path(tool_file_path)
            stats.gt_file_path = gt_file_path
            gt = ""
            source_tex_file = None
            # Don't proceed, if the gt file is empty.
            if file_util.is_missing_or_empty_file(gt_file_path):
                stats.code = 21
            else:
                # Read the gt file.
                # Gt file may have source path to the tex file in header.
                gt_lines = []
                source_tex_file = None
                with open(gt_file_path) as f: 
                    for line in f: 
                        if line.startswith("##source"):
                            _, source_tex_file = line.split("\t")
                        else:
                            gt_lines.append(line)
                gt = "".join(gt_lines)

            if stats.code != 0:
                return stats

            #### Process the PDF file.
            pdf_file_path  = self.get_pdf_file_path(tool_file_path)
            stats.pdf_file_path = pdf_file_path
            # Don't proceed, if the pdf file doesn't exist.
            if file_util.is_missing_or_empty_file(pdf_file_path):
                stats.code = 31
            
            if stats.code != 0:
                return stats

            # Compute evaluation result.
            diff_result = self.process_strings(gt, tool_output)
            diff_result.source_tex_file = source_tex_file
            if num_ops_snapshot is not None:
                diff_utils.serialize_diff_result(diff_result, num_ops_snapshot)
        else:
            diff_result = diff_utils.deserialize_diff_result(num_ops_snapshot)

        # Lock the counter, because += operation is not atomic
        with total_num_processed_tool_files.get_lock():
            total_num_processed_tool_files.value += 1
        
        stats.counter = total_num_processed_tool_files.value

        # Handle the result.
        self.handle_evaluation_result(stats, diff_result)

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
        
        return doc_diff.doc_diff_from_strings(
            actual,
            gt,
            rearrange_phrases=True,
            min_rearrange_length=10,
            refuse_common_threshold=1,
            excludes=self.args.junk,
            junk=self.args.junk)
        
    # --------------------------------------------------------------------------
    # Handler methods.
    
    def handle_process_start(self):
        """ 
        This method is called when processing was started.
        """
        # Otput some statistics.
        tool_name = self.args.tool
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
        
    def handle_collect_tool_files_start(self):
        """
        This method is called when collecting the tool files was started.
        """
        self.out.print_text("Collecting tool files ...")
        self.out.print_gap() 
        
    def handle_collect_tool_files_end(self, tool_files):
        """
        This method is called when collecting the tool files was finished.
        """
        self.out.print_columns("# Found tool files: ", len(tool_files))
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
            "        NL+",  # number of para merges.
            "        NL-",  # number of para splits.
            "        P+",   # number of para deletes.
            "        P-",   # number of para inserts.
            "        P<>",  # number of para rearranges.
            "        W+",   # number of word deletes.
            "        W-",   # number of word inserts.
            "        W~"    # number of word replaces.
        )
    
    def handle_evaluation_result(self, stats, diff_result):
        """ This method is called whenever a tool file was evaluated."""
        
        global total_num_processed_tool_files
        global total_num_tool_files
        
        if not stats:
            return
        
        # Don't proceed if status code != 0.
        if stats.code != 0:
            return
        
        # ----------------------------------------------------------------------
        # Create table row.
        
        tool_file_path = stats.tool_file_path
        basename = self.get_basename_from_tool_file_path(tool_file_path)
        
        # Compute delta values.
        num_ops = diff_result.num_ops
        num_ops_abs = diff_result.num_ops_abs
        percental_num_ops = diff_result.num_ops_rel
        prev_num_ops = self.deserialize_prev_num_ops(tool_file_path) 
        
        # Make the num_ops available for table footer.
        stats.num_ops = num_ops
        stats.num_ops_abs = num_ops_abs
        stats.percental_num_ops = percental_num_ops
        stats.prev_num_ops = prev_num_ops
        stats.num_paras_target = diff_result.num_paras_target
        stats.num_words_target = diff_result.num_words_target
        
        delta_vals = self.compute_delta_values(num_ops, prev_num_ops, percental_num_ops, num_ops_abs)
              
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
                "%d/%d" % (stats.counter, total_num_tool_files),
                basename,
                self.print_cell(para_merges, percental_para_merges, para_merges_delta),
                self.print_cell(para_splits, percental_para_splits, para_splits_delta),
                self.print_cell(para_deletes, percental_para_deletes, para_deletes_delta),
                self.print_cell(para_inserts, percental_para_inserts, para_inserts_delta),
                self.print_cell(para_rearr, percental_para_rearr, para_rearr_delta),
                self.print_cell(word_deletes, percental_word_deletes, word_deletes_delta),
                self.print_cell(word_inserts, percental_word_inserts, word_inserts_delta),
                self.print_cell(word_replaces, percental_words_replaces, word_replaces_delta)
            )

        # ----------------------------------------------------------------------

        # Visualize the evaluation result.
        vis_file_path = self.define_visualization_file_path(tool_file_path)
        self.visualize_evaluation_result(diff_result, vis_file_path)

        # Serialize the num ops.
        serial_file_path = self.define_serialization_file_path(tool_file_path)
        self.serialize_num_ops(num_ops, serial_file_path)

    def handle_evaluation_end(self, stats):
        """ 
        This method is called when all pdf files were processed.
        """
        result = {}
        end_time = self.args.end_time = datetime.now()
        end_time_str = time_util.format_time(end_time)

        # Iterate through the stats to create the summary.
        num_ops = []
        percental_num_ops = []
        num_ops_abs = []
        prev_num_ops = []
        
        # Count the errors by frequencies.
        error_codes_by_freqs = Counter()
        
        total_num_paras_target = 0
        total_num_words_target = 0

        for stat in stats:
            if not stat:
                continue
            
            if stat.num_ops:
                num_ops.append(stat.num_ops)
                num_ops_abs.append(stat.num_ops_abs)
                total_num_paras_target += stat.num_paras_target
                total_num_words_target += stat.num_words_target
            else: 
                # There are some groundtruth file containing only "[formula]".
                # Theses files are definitely corrupt. So add it to missing
                # gt files. TODO
                stat.code = stat.code or 22 

            if stat.code != 0:
                error_codes_by_freqs[stat.code] += 1
                continue
               
            if stat.percental_num_ops:
                percental_num_ops.append(stat.percental_num_ops)

            if stat.prev_num_ops:
                prev_num_ops.append(stat.prev_num_ops)

        # ----------------------------------------------------------------------
        # Print table footer row.
        
        # Compute summarized delta values.     
        delta_vals = self.compute_delta_values(num_ops, prev_num_ops, percental_num_ops, num_ops_abs, total_num_paras_target=total_num_paras_target, total_num_words_target=total_num_words_target) 
                      
        # Compute runtimes
        runtime = self.args.runtime = self.args.end_time - self.args.start_time
        runtime_str = time_util.format_time_delta(runtime)
        avg_runtime = self.args.avg_runtime = runtime / len(num_ops)
        avg_runtime_str = time_util.format_time_delta(avg_runtime)
        
        # Compute total number of errors.
        total_num_errors = sum([x for x in error_codes_by_freqs.values()])

        # Create tex row.
        tex_row = self.create_tex_table_row(delta_vals)
        result["delta_vals"] = delta_vals
        result["error_codes"] = error_codes_by_freqs
        
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
                self.print_cell(para_merges, percental_para_merges, para_merges_delta),
                self.print_cell(para_splits, percental_para_splits, para_splits_delta),
                self.print_cell(para_deletes, percental_para_deletes, para_deletes_delta),
                self.print_cell(para_inserts, percental_para_inserts, para_inserts_delta),
                self.print_cell(para_rearr, percental_para_rearr, para_rearr_delta),
                self.print_cell(word_deletes, percental_word_deletes, word_deletes_delta),
                self.print_cell(word_inserts, percental_word_inserts, word_inserts_delta),
                self.print_cell(word_replaces, percental_words_replaces, word_replaces_delta)
            )
            self.out.print_gap()
        
        # ----------------------------------------------------------------------
         
        self.out.set_column_widths(30, -1)
        self.out.print_columns("End time: ", end_time_str)
        self.out.print_columns("Total runtime: ", runtime_str)
        self.out.print_columns("Avg. runtime: ", avg_runtime_str)
        self.out.print_columns("# evaluated files: ", len(num_ops))
        self.out.print_columns("# errors: ", total_num_errors)
        if total_num_errors > 0:
            self.out.print_columns("# errors broken down by error code:")
            for error_code, freq in sorted(error_codes_by_freqs.items()):
                self.out.print_columns(error_code, freq)
        self.out.print_columns("TeX Table Row: ", tex_row)
        
        return result
                
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
    
    def compute_delta_values(self, num_ops, prev_num_ops, percental_num_ops, num_ops_abs, total_num_paras_target=1, total_num_words_target=1):
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
            
#            percental_num_para_splits  = [get(x, "num_para_splits", 0) for x in percental_num_ops]
#            percental_para_splits      = sum(percental_num_para_splits) / num
#            percental_num_para_merges  = [get(x, "num_para_merges", 0) for x in percental_num_ops]
#            percental_para_merges      = sum(percental_num_para_merges) / num
#            percental_num_para_rearr   = [get(x, "num_para_rearranges", 0) for x in percental_num_ops]
#            percental_para_rearr       = sum(percental_num_para_rearr) / num
#            percental_num_para_inserts = [get(x, "num_para_inserts", 0) for x in percental_num_ops]
#            percental_para_inserts     = sum(percental_num_para_inserts) / num
#            percental_num_para_deletes = [get(x, "num_para_deletes", 0) for x in percental_num_ops]
#            percental_para_deletes     = sum(percental_num_para_deletes) / num
#            percental_num_word_inserts = [get(x, "num_word_inserts", 0) for x in percental_num_ops]
#            percental_word_inserts     = sum(percental_num_word_inserts) / num
#            percental_num_word_deletes = [get(x, "num_word_deletes", 0) for x in percental_num_ops]
#            percental_word_deletes     = sum(percental_num_word_deletes) / num
#            percental_num_word_replacs = [get(x, "num_word_replaces", 0) for x in percental_num_ops]
#            percental_word_replaces    = sum(percental_num_word_replacs) / num
            
            percental_num_para_splits  = [get(x, "num_para_splits", 0) for x in num_ops_abs]
            percental_para_splits      = round(sum(percental_num_para_splits) / total_num_paras_target, 4)
            percental_num_para_merges  = [get(x, "num_para_merges", 0) for x in num_ops_abs]
            percental_para_merges      = round(sum(percental_num_para_merges) / total_num_paras_target, 4)
            percental_num_para_rearr   = [get(x, "num_para_rearranges", 0) for x in num_ops_abs]
            percental_para_rearr       = round(sum(percental_num_para_rearr) / total_num_words_target, 4)
            percental_num_para_inserts = [get(x, "num_para_inserts", 0) for x in num_ops_abs]
            percental_para_inserts     = round(sum(percental_num_para_inserts) / total_num_words_target, 4)
            percental_num_para_deletes = [get(x, "num_para_deletes", 0) for x in num_ops_abs]
            percental_para_deletes     = round(sum(percental_num_para_deletes) / total_num_words_target, 4)
            percental_num_word_inserts = [get(x, "num_word_inserts", 0) for x in num_ops_abs]
            percental_word_inserts     = round(sum(percental_num_word_inserts) / total_num_words_target, 4)
            percental_num_word_deletes = [get(x, "num_word_deletes", 0) for x in num_ops_abs]
            percental_word_deletes     = round(sum(percental_num_word_deletes) / total_num_words_target, 4)
            percental_num_word_replaces= [get(x, "num_word_replaces", 0) for x in num_ops_abs]
            percental_word_replaces    = round(sum(percental_num_word_replaces) / total_num_words_target, 4)
            
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
     
    def deserialize_prev_num_ops(self, tool_file_path):
        """ 
        Deserializes the previous stats related to the given stats. 
        """
        serial_file_path = self.define_serialization_file_path(tool_file_path)
        
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

    def visualize_evaluation_result(self, diff_result, file_path):
        """ 
        Visualizes the given evaluation result. 
        """
        with open(file_path, "w+") as f:
            f.write(diff_result.vis)

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
    
    def get_gt_file_path(self, tool_file_path):
        """ 
        Returns the path to tool file related to the given groundtruth file.

        Example:
        Input:  tool_file_path = output/pdftotext/0001/cond-mat0001419.final.txt
        Output: gt_file_path = output/groundtruth/0001/cond-mat0001419.body.txt         
        """
        
        # Obtain the directory and name of tool file.
        tool_dir, tool_file_name = os.path.split(tool_file_path)
        # Obtain the path to the tool dir relative to the input dir.
        rel_tool_dir = os.path.relpath(tool_dir, self.args.tool_root)

        # Split the tool_file_name into basename and extension.
        basename = self.get_basename_from_tool_file_name(tool_file_name)

        # Compose the tool file path.
        gt_dir = os.path.join(self.args.gt_root, rel_tool_dir)
        gt_file_name = "%s%s" % (basename, ".body.txt")
        gt_file_path = os.path.join(gt_dir, gt_file_name)
            
        return gt_file_path
    
    def get_pdf_file_path(self, tool_file_path):
        """ 
        Returns the path to pdf file related to the given tool file.

        Example:
        Input:  tool_file_path = output/pdftotext/0001/cond-mat0001419.final.txt
        Output: pdf_file_path = input/pdf/0001/cond-mat0001419.pdf         
        """    
        
        # Obtain the directory and name of tool file.
        tool_dir, tool_file_name = os.path.split(tool_file_path)
        # Obtain the path to the tool dir relative to the input dir.
        rel_tool_dir = os.path.relpath(tool_dir, self.args.tool_root)
        
        # Split the tool_file_name into basename and extension.
        basename = self.get_basename_from_tool_file_name(tool_file_name)
        
        # Compose the pdf_file_path.
        pdf_dir = os.path.join(self.args.pdf_root, rel_tool_dir)
        pdf_file_name = "%s%s" % (basename, ".pdf")
        pdf_file_path = os.path.join(pdf_dir, pdf_file_name)
        
        return pdf_file_path
    
    def get_basename_from_tool_file_path(self, tool_file_path):
        """ 
        Returns the basename of groundtruth file from given file *path*.
        
        >>> self.get_basename_from_tool_file_path("/xxx/yyy/cond-mat01.final.txt")
        cond-mat01
        """
        tool_dir, tool_file_name = os.path.split(tool_file_path)
        return self.get_basename_from_tool_file_name(tool_file_name)
    
    def get_basename_from_tool_file_name(self, tool_file_name):
        """ 
        Returns the basename of groundtruth file from given file *name*.
        
        >>> self.get_basename_from_tool_file_name("cond-mat01.final.txt")
        cond-mat01
        """
        return tool_file_name.replace(self.args.suffix, "")
           
    def define_visualization_file_path(self, tool_file_path):
        """ 
        Defines the path to the file, where the visualization of the 
        evaluation result for given gt file should be stored. 
        """
        return tool_file_path.replace(self.args.suffix, ".visualization.txt")
       
    def define_serialization_file_path(self, tool_file_path):      
        """
        Defines the path to the file, where the evaluation result for given 
        gt file should be serialized.
        """
        return tool_file_path.replace(self.args.suffix, ".results.txt")
        
    def define_rearranged_snapshot_path(self, tool_file_path):      
        """
        Defines the path to the file, where the diff_result with computed 
        rearranges should be serialized.
        """
        return tool_file_path.replace(self.args.suffix, ".phrases")
        
    def define_num_ops_snapshot_path(self, tool_file_path):      
        """
        Defines the path to the file, where the diff_result with computed 
        num_ops should be serialized.
        """
        return tool_file_path.replace(self.args.suffix, ".num_ops")
                    
    # --------------------------------------------------------------------------
    # Collecting groundtruth files.
           
    def collect_tool_files(self):
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
        
        input_dir  = self.args.tool_root
        dir_filter = self.args.dir_filter
        prefix     = self.args.prefix
        suffix     = self.args.suffix

        return file_util.collect_files(input_dir, dir_filter, prefix, suffix)
                            
    # --------------------------------------------------------------------------
    # Argument parser.
    
    def get_argument_parser():
        """ Creates an parser to parse the command line arguments. """
        parser = ArgumentParser()
                    
        parser.add_argument("tool_root_dir", 
            help="The root of input."
        )
        parser.add_argument("tools", 
            help="The name of tools to evaluate."
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
        parser.add_argument("--no_output", 
            help="Flag to switch off the output.",
        )
        parser.add_argument("--recompute_rearranged_snapshot", 
            help="Force the (re-)computation of rearranges.",
            type=ast.literal_eval,
            default=False
        )
        parser.add_argument("--recompute_num_ops_snapshot", 
            help="Force the (re-)computation of num_ops.",
            type=ast.literal_eval,
            default=False
        )
        
        return parser
        
if __name__ == "__main__": 
    args = Evaluator.get_argument_parser().parse_args() 
    Evaluator(args).process()
