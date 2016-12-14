import os
import os.path
import subprocess

import util
import file_util
from dotdict import dotdict

import outputter as out

from argparse import ArgumentParser
from multiprocessing import Pool, Value, Lock

class Extractor:
    """ Extracts from PDF with given extraction tool."""
    
    counter_processed_pdf_files = 0
    num_total_pdf_files = 0
    
    def __init__(self, args):
        """ Creates a new extractor based on the given args. """           
        self.args = self.prepare_arguments(args)
        self.out  = out.Outputter(width=140)
    
    # --------------------------------------------------------------------------
    
    def prepare_arguments(self, args):
        """ Prepares the given arguments such that they fits our needs."""
        
        # 'args' is given as Namespace object. Transform it to dotdict.
        prepared = dotdict(args)
        
        # Read key/value pairs from the tool info file.
        tool_info = util.read_tool_info(prepared.output_dir)
        prepared.update(tool_info)
        
        # The dir filter is given as string. Split it to get a list of filters.    
        dir_filter = prepared.dir_filter
        if dir_filter is not None:
            prepared.dir_filter = dir_filter.split()
        else:
            prepared.dir_filter = []
        
        # Ensure that there is a criterion given on collectin pdf files.
        collect_criterion = prepared.criterion
        if collect_criterion is None:
            prepared.criterion = "ALL"
        elif collect_criterion != "ALL" and collect_criterion != "NON_EXISTING":
            prepared.criterion = "ALL"
        
        # Set num_threads to None if it is <= 0.
        num_threads = prepared.num_threads
        if num_threads is not None:
            prepared.num_threads = num_threads if num_threads > 0 else None
        
        return prepared
        
    def init_global_vars(self, counter_processed_pdfs, num_total_pdfs):
        """ Initializes global variables to be able to use them within the 
        threads."""
        global counter_processed_pdf_files
        global num_total_pdf_files
        
        counter_processed_pdf_files = counter_processed_pdfs 
        num_total_pdf_files = num_total_pdfs
     
    # --------------------------------------------------------------------------
    # Process.
                   
    def process(self):
        """ Starts the extraction process. """
        
        self.handle_process_start()
        global counter_processed_pdf_files
        global num_total_pdf_files
        
        # Collect the pdf files to process.
        self.handle_collect_pdf_files_start()
        pdf_files = self.collect_pdf_files()        
        self.handle_collect_pdf_files_end(pdf_files)
                            
        # Define a counter to count already processed pdf files.
        counter_processed_pdfs = Value('i', 0)
        
        counter_processed_pdf_files = counter_processed_pdfs
        num_total_pdf_files = len(pdf_files)        
                            
        # Start the extraction. 
        self.handle_extraction_start()
        if self.args.num_threads == 1:
            # Sequential processing.
            stats = [self.process_pdf_file(f) for f in pdf_files]
        else:
            # Define a pool to process the files *in parallel*.
            pool = Pool(
                processes   = self.args.num_threads,
                initializer = self.init_global_vars,
                initargs    = (counter_processed_pdfs, len(pdf_files))
            )
        
            # Parallel processing.
            stats = pool.map(self.process_pdf_file, pdf_files)
            
            pool.close()
            pool.join()
            
        self.handle_extraction_end(stats)    
        self.handle_process_end()
                                    
    def process_pdf_file(self, pdf_path):
        """ Processes the given pdf file. """
        global counter_processed_pdf_files
        global num_total_pdf_files
        
        # Define the output paths.
        raw_output_path   = self.define_raw_output_path(pdf_path, create=True)
        plain_output_path = self.define_plain_output_path(pdf_path, create=True)
                
        # The given command holds placeholder for input path and output path.
        # Plug in these arguments.
        cmd = self.args.cmd
        cmd = cmd.replace('%IN', pdf_path)
        cmd = cmd.replace('%OUT', raw_output_path)

        # Run the command.        
        start = util.time_in_ms()
        with open(os.devnull, 'w') as FNULL:
            subprocess.call(cmd, shell=True, stdout=FNULL, stderr=subprocess.STDOUT)
            #subprocess.call(cmd, shell=True)
        end = util.time_in_ms()
        runtime = end - start
        
        # The output of the given tool may be not plain txt, but XML, HTML, etc.
        # Derive a plain txt file from the output. 
        self.create_plain_output_file(raw_output_path, plain_output_path)   
        
        # Output some debug informations.
        # We need a lock to increment the counter, because += operation is not 
        # atomic
        with counter_processed_pdf_files.get_lock():
            counter_processed_pdf_files.value += 1   
        
        stats = { 
            "pdf_path":          pdf_path,
            "raw_output_path":   raw_output_path,
            "plain_output_path": plain_output_path,
            "cmd":               cmd,
            "counter":           counter_processed_pdf_files.value,
            "runtime":           runtime 
        }
        
        self.handle_extraction_result(stats)
        
        return stats
    
    def create_plain_output_file(self, raw_output_path, plain_output_path):
        """ Creates a plain txt output file from given "raw" output file, which
        could be a XML or HTML file. Stores the result to given 
        plain_output_path. """
        
        # Create the plain output.
        plain_output = self.create_plain_output(raw_output_path)
        
        # Store the output to file.
        with open(plain_output_path, "w") as plain_output_file: 
            plain_output_file.write(plain_output)
    
    def create_plain_output(self, raw_output_path):
        """ Creates a plain txt output from given raw output file. Override
        it if you have to do more advanced stuff."""
             
        if not file_util.is_missing_or_empty_file(raw_output_path):
            with open(raw_output_path, "r", errors='ignore') as raw_output_file: 
                return raw_output_file.read()
        else:
            return ""
    
    # --------------------------------------------------------------------------
    # Handler methods.
    
    def handle_process_start(self):
        """ This method is called when processing was started."""
        
        # Output some statistics.
        tool_name = self.args.tool_name
        self.out.print_heading("Extraction using the tool %s" % tool_name)
                
        self.out.set_column_widths(30, -1)
        self.out.print_columns("Input Directory: ", self.args.input_dir)
        self.out.print_columns("Output Directory: ",self.args.output_dir)
        self.out.print_columns("Command To Execute: ", self.args.cmd)
        self.out.print_columns("Directory Filter: ", self.args.dir_filter)
        self.out.print_columns("Prefix: ", self.args.prefix)
        self.out.print_columns("Max. Threads: ", self.args.num_threads)
        self.out.print_gap()  
    
    def handle_collect_pdf_files_start(self):
        """ This method is called when collecting the pdf files was started. """
        self.out.print_text("Collecting PDF files ...")
        self.out.print_gap()
    
    def handle_collect_pdf_files_end(self, pdf_files):
        """ This method is called when collecting the pdf was finished."""
        self.out.print_columns("# Collected PDF files: ", len(pdf_files))
        self.out.print_gap()
    
    def handle_extraction_start(self):
        """ This method is called when the actual extraction was started."""
        
        self.out.print_text("Extracting ...")
        self.out.set_column_widths(15, 50, 50, -1)
        self.out.print_columns_headers("#", "Input", "Target", "Runtime")
        
    def handle_extraction_result(self, stats):
        """ This method is called whenever a pdf file was processed."""
    
        global num_total_pdf_files
        counter     = stats["counter"]
        pdf_path    = stats["pdf_path"]
        output_path = stats["plain_output_path"]
        runtime     = stats["runtime"]
           
        self.out.print_columns("%d/%d" % (counter, num_total_pdf_files), 
            pdf_path, output_path, "%dms" % runtime)
    
    def handle_extraction_end(self, stats):
        """ This method is called when all pdf files were processed."""
        
        # Output some statistics.        
        sum_runtimes = 0
        avg_runtime = 0
        
        if len(stats) > 0:
            sum_runtimes = sum([x["runtime"] for x in stats])
            avg_runtime  = sum_runtimes / len(stats)
            
        self.out.print_columns_footers()
        self.out.print_gap()
        
        self.out.set_column_widths(30, -1)
        self.out.print_columns("Time needed:",  "%.1fs" % (sum_runtimes / 1000))
        self.out.print_columns("Time needed avg:", "%.1fms" % avg_runtime)
    
    def handle_process_end(self):
        """ This method is called when processing was finished."""
        pass
     
    # --------------------------------------------------------------------------
    # Path definitions. 
                
    def define_raw_output_path(self, pdf_path, create=False):
        """ Defines the path to the output file where the "raw" output of a 
        tool should be stored. Creates the related parent directory if it 
        doesn't exist yet. Returns the defined path or None, if the parent 
        directory doesn't exist and could not be created. """
        
        return self.define_output_path(pdf_path, ".raw.txt", create=create)
    
    def define_plain_output_path(self, pdf_path, create=False):
        """ Defines the path to the output file where the "plain" output of a 
        tool should be stored, i.e. the output in plain txt format without any 
        XML- or HTML-markups. Creates the related parent directory if it 
        doesn't exist yet. Returns the defined path or None, if the parent 
        directory doesn't exist and could not be created. """
        
        return self.define_output_path(pdf_path, ".txt", create=create)
       
    def define_output_path(self, pdf_path, extension, create=False):
        """ Defines the path to a output file with given extension. Creates the 
        related parent directory if it doesn't exist yet. Returns the defined 
        path or None, if the parent directory doesn't exist and could not be 
        created."""
        
        # Obtain the directory and name of pdf file from pdf_path.
        pdf_dir, pdf_name = os.path.split(pdf_path)
        # Obtain the path to the pdf dir relative to the pdf root dir.
        rel_pdf_dir = os.path.relpath(pdf_dir, self.args.input_dir)
        
        # Split the pdf_name into basename and extension.
        pdf_basename, pdf_extension = os.path.splitext(pdf_name)
        
        # Compose the output path.
        output_dir  = os.path.join(self.args.output_dir, rel_pdf_dir)
        output_name = "%s%s" % (pdf_basename, extension) 
        output_path = os.path.join(output_dir, output_name)
        
        if create:
            # Try to create the directory if it doesn't exist.
            try:
                os.makedirs(output_dir, exist_ok=True)
            except Error:
                # Return None if the directory couldn't be created.
                return None
        
        return output_path

    # --------------------------------------------------------------------------
    # Collecting pdf files.
    
    def collect_pdf_files(self):
        """ Scans the pdf root directory given in self.args for pdf files.
        The following directory structure is assumed:
        
        /input_dir
        |-- 0001
            |-- astro-ph0001001.pdf
            |-- astro-ph0001002.pdf
            |-- astro-ph0001003.pdf
            ...
        |-- 0002
            |-- astro-ph0002001.pdf
            |-- astro-ph0002001.pdf
            ...
        ... 
        
        On collecting, the given directory filter and given file prefix filter 
        is considered. Returns a list of the collected files."""
        
        input_dir  = self.args.input_dir
        dir_filter = self.args.dir_filter
        prefix     = self.args.prefix
        suffix     = ".pdf" 
        
        files = file_util.collect_files(input_dir, dir_filter, prefix, suffix)
        
        if self.args.criterion == "NON_EXISTING":
            # Filter all files where the output file doesn't exist.
        
            filtered = []
            for f in files:
                output_file = self.define_plain_output_path(f)
                if not os.path.exists(output_file):
                    filtered.append(f)
            return filtered    
        
        return files
            
    # --------------------------------------------------------------------------                 
    
    def get_argument_parser():
        """ Creates an parser to parse the command line arguments. """
        parser = ArgumentParser()
        
        parser.add_argument("cmd", 
            help="The command to execute."
        )
        parser.add_argument("input_dir", 
            help="The input directory."
        )
        parser.add_argument("output_dir", 
            help="The output directory."
        )
        parser.add_argument("--dir_filter", 
            help="The directories to consider on collecting pdf files",
            default=""
        )
        parser.add_argument("-p", "--prefix", 
            help="The prefix to consider on collecting pdf files.",
            default=""
        )
        parser.add_argument("--criterion", 
            help="The criterion on collecting pdf files: ALL or NON_EXISTING",
            default="ALL"
        )
        parser.add_argument("--num_threads", 
            help="The number of threads to use on extraction.",
            type=int
        )
        
        return parser
        
if __name__ == "__main__": 
    arg_parser = Extractor.get_argument_parser()
    args       = arg_parser.parse_args() 
    extractor  = Extractor(args)
    extractor.process()
