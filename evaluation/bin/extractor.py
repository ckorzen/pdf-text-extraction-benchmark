import os
import subprocess
import sys
from argparse import ArgumentParser

class Extractor:
    def __init__(self, args):
        '''
        Creates a new extractor with the command to execute 
        (and its related arguments), the path to the root directory of the pdf 
        files and an optional prefix with which the filename of a pdf must start
        to be considered on extraction.
        '''
        self.cmd = args.cmd
        self.pdfs_root_dir = args.input_dir
        self.output_dir = args.input_dir if args.output_dir is None else args.output_dir
        self.prefix = args.prefix
                
    def process(self):
        '''
        Scans the root directory of the pdf files recursively. A file is 
        considered on extraction if its name starts with the given prefix and
        ends with the suffix '.pdf' (case-insensitive).
        '''
                
        for current_dir, dirs, files in os.walk(self.pdfs_root_dir):
            for file in files:
                name = file.lower()
                if name.startswith(self.prefix) and name.endswith(".pdf"):
                    self.process_pdf_file(current_dir, file)
        return None
    
    def process_pdf_file(self, dir, pdf_name):
        '''
        Processes the given pdf file.
        '''
        pdf_path = os.path.join(dir, pdf_name)
        (output_dir, output_file) = self.get_output_file_path(dir, pdf_name)
        output_path = os.path.join(output_dir, output_file)
        
        # Create the output directories if they don't exist yet.
        if not os.path.exists(output_dir):
            os.makedirs(output_dir)
        
        # Plug in the arguments into the command pattern.
        cmd = self.cmd.replace('%IN', pdf_path).replace('%OUT', output_path)
        
        print("Extracting %s to %s" % (pdf_path, output_path))
        
        # Run the command.
        dev_null = open(os.devnull, 'w')
        std_out = subprocess.STDOUT
        return subprocess.call(cmd, shell=True, stdout=dev_null, stderr=std_out)
        
    def get_output_file_path(self, dir, pdf_name):
        '''
        Returns the path of the output file for the given pdf file.
        '''
        # Obtain the relative path of the directory within the root directory.
        rel_dir = os.path.relpath(dir, self.pdfs_root_dir)
        # Obtain the basename and the file extension of the pdf file.
        pdf_basename, pdf_extension = os.path.splitext(pdf_name)
        
        return (os.path.join(self.output_dir, rel_dir), pdf_basename + ".txt")
    
if __name__ == "__main__":
    parser = ArgumentParser()
    parser.add_argument("cmd", help="The command to execute.")
    parser.add_argument("input_dir", help="The input directory.")
    parser.add_argument("output_dir", help="The output directory.")
    parser.add_argument("-p", "--prefix", default="", help="the prefix of the files to evaluate")
    args = parser.parse_args()
    
    extractor = Extractor(args)
    extractor.process()