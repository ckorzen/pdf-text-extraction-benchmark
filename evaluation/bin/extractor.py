import os
import os.path
import subprocess
import sys

from argparse import ArgumentParser
from multiprocessing import Pool, Value, Lock
from time import time

class Extractor:
    ''' The base class of an extractor. '''
    
    counter, num_pdf_files = 0, 0
    
    def __init__(self, args):
        '''
        Creates a new extractor with the command to execute 
        (and its related arguments), the path to the root directory of the pdf 
        files and an optional prefix with which the filename of a pdf must start
        to be considered on extraction.
        '''
        self.args = args
        self.cmd = args.cmd
        self.pdfs_root_dir = args.input_dir
        self.output_dir = args.input_dir 

        if args.output_dir is not None:
            self.output_dir = args.output_dir
        self.prefix = args.prefix
                    
    def init_counters(self, c, n):
        global counter, num_pdf_files
        counter = c 
        num_pdf_files = n
                   
    def process(self, parallel=True):
        '''
        Scans the root directory of the pdf files recursively. A file is 
        considered on extraction if its name starts with the given prefix and
        ends with the suffix '.pdf' (case-insensitive).
        '''
                
        pdf_dir_files = self.get_pdf_files(self.pdfs_root_dir, self.prefix)
        
        runtimes = []
        counter = Value('i', 0)
        if self.args.processing_type == "sequential":
            # Process the files in *parallel*.
            self.init_counters(counter, len(pdf_dir_files))
            
            for pdf_dir_file in pdf_dir_files:
                runtimes.append(self.process_pdf_file(pdf_dir_file))   
        else:
            # Process the files *sequentially*.
            pool = Pool(
                initializer=self.init_counters,
                initargs=(counter, len(pdf_dir_files))
            )
                
            runtimes = pool.map(self.process_pdf_file, pdf_dir_files)
            pool.close()
            pool.join()
                
        sum_runtimes = sum(runtimes)
        avg_runtime  = sum_runtimes / len(runtimes)
                        
        print("Time needed for extraction: %ds" % sum_runtimes)
        print("Time needed for extraction on avg.: %.2fs" % avg_runtime)
            
    def process_pdf_file(self, pdf_dir_file):
        '''
        Processes the given pdf file.
        '''
        pdf_dir, pdf_file = pdf_dir_file
        
        pdf_path = os.path.join(pdf_dir, pdf_file)
        output_dir, output_file = self.get_output_file_path(pdf_dir, pdf_file)
        output_path = os.path.join(output_dir, output_file)
        
        # Create the output directories if they don't exist yet.
        if not os.path.exists(output_dir):
            os.makedirs(output_dir, exist_ok=True)
        
        # Plug in the arguments into the command pattern.
        cmd = self.cmd.replace('%IN', pdf_path).replace('%OUT', output_path)
        
        global counter, num_pdf_files
        # += operation is not atomic, so we need to get a lock:
        with counter.get_lock():
            counter.value += 1
            print("%d/%d Extracting %s to %s" 
                % (counter.value, num_pdf_files, pdf_path, output_path))
        
        
        # Extract.
        start = time()
        subprocess.call(cmd, shell=True)
        end = time()
        
        # Write plain txt file.
        self.create_plain_file(pdf_dir, pdf_file)
    
        return end - start
    
    def create_plain_file(self, pdf_dir, pdf_file):
        output_dir, output_file = self.get_output_file_path(pdf_dir, pdf_file)
        output_path = os.path.join(output_dir, output_file)
        target_dir, target_file = self.get_plain_file_path(pdf_dir, pdf_file)
        target_path = os.path.join(target_dir, target_file)
        
        output = self.format_output(output_path)
                    
        with open(target_path, "w") as target_file: 
            target_file.write(output)
    
    def format_output(self, output_path):        
        if not self.is_missing_or_empty(output_path):
            with open(output_path, "r") as output_file: 
                return output_file.read()
        else:
            return ""
        
     
    # --------------------------------------------------------------------------
    # Util methods. 
                
    def get_output_file_path(self, dir, pdf_name):
        '''
        Returns the path of the output file for the given pdf file.
        '''
        # Obtain the relative path of the directory within the root directory.
        rel_dir = os.path.relpath(dir, self.pdfs_root_dir)
        # Obtain the basename and the file extension of the pdf file.
        pdf_basename, pdf_extension = os.path.splitext(pdf_name)
        
        return (os.path.join(self.output_dir, rel_dir), pdf_basename + ".raw.txt")
    

    def get_plain_file_path(self, dir, pdf_name):
        '''
        Returns the path of the output file for the given pdf file.
        '''
        # Obtain the relative path of the directory within the root directory.
        rel_dir = os.path.relpath(dir, self.pdfs_root_dir)
        # Obtain the basename and the file extension of the pdf file.
        pdf_basename, pdf_extension = os.path.splitext(pdf_name)
        
        return (os.path.join(self.output_dir, rel_dir), pdf_basename + ".txt")

    
    def get_pdf_files(self, root, prefix):
        """ Scans the given root directory to find pdf files that 
        matches the given prefix."""
        
        result = []

        # Scan the given root directory for pdf files.        
        for current_dir, dirs, files in os.walk(root):
            pdf_files = sorted([f for f in files \
                 if f.lower().startswith(prefix) and f.lower().endswith(".pdf")])
                
            # Extend result list.
            result.extend([(current_dir, f) for f in pdf_files])
        
        return result
    
    def is_missing_or_empty(self, file_path):
        """ Returns true, if the given file_path doesn't exist or if the 
        content of file is empty. """
        
        return not os.path.isfile(file_path) or os.path.getsize(file_path) == 0
    
    def get_argument_parser():
        """ 
        Creates an parser to parse the command line arguments. 
        """
        parser = ArgumentParser()
        parser.add_argument("cmd", help="The command to execute.")
        parser.add_argument("input_dir", help="The input directory.")
        parser.add_argument("output_dir", help="The output directory.")
        parser.add_argument("-p", "--prefix", default="", help="the prefix of the files to evaluate")
        parser.add_argument("--processing_type", default="parallel", help="'sequential' or 'parallel'")
        args = parser.parse_args()

        return parser
