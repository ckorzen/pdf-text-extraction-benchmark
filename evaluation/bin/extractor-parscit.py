import os
import os.path
import subprocess
from lxml import etree

import file_util

from extractor import Extractor

title_xpath = """./algorithm[@name='ParsHed']//title"""
variant_xpath = """./algorithm[@name='SectLabel']//variant"""

class ParscitExtractor(Extractor):
    def process_pdf_file(self, pdf_path):
        # Parscit only works on *txt* files, but we have *pdf* files as input. 
        # Introduce a preprocessing step, where the input pdf files are 
        # serialized to txt (using pdftotext, see below). 
                
        # Define the target file for preprocessing step.
        pre_file_path = self.define_pre_file_path(pdf_path, create=True)
                
        cmd = "pdftotext %s %s" % (pdf_path, pre_file_path)
        with open(os.devnull, 'w') as FNULL:
            subprocess.call(cmd, shell=True, stdout=FNULL, stderr=subprocess.STDOUT)
        
        # Run the routine on pdftotext file.
        return super(ParscitExtractor, self).process_pdf_file(pre_file_path)
    
    def define_pre_file_path(self, pdf_path, create=False):
        """ Defines the path to the output file where the txt file of the 
        preprocessing step be stored. Creates the related parent directory if it 
        doesn't exist yet. Returns the defined path or None, if the parent 
        directory doesn't exist and could not be created. """
        
        # Call the super variant (and not the overwriting variant below), 
        # because we have a pdf path here.
        supper = super(ParscitExtractor, self)
        return supper.define_output_path(pdf_path, ".pre", create=create)
        
    def define_output_path(self, pdf_path, extension, create=False):
        """ Overwrite the method such that the correct input dir is used. """
                
        # Obtain the directory and name of pdf file from pdf_path.
        pdf_dir, pdf_name = os.path.split(pdf_path)
        # Obtain the path to the pdf dir relative to the pdf root dir.
        rel_pdf_dir = os.path.relpath(pdf_dir, self.args.output_dir)
        
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
        
    def create_plain_output(self, raw_output_path):
        """ 
        Formats the given file.
        """
                       
        if file_util.is_missing_or_empty_file(raw_output_path):
            return 11, None
        
        try:
            # Read in the xml.
            xml = etree.parse(raw_output_path, etree.XMLParser(recover=True))
        except:
            return 12, None

        paragraphs = []
        
        # Extract the title as separate paragraph.
        title_node = xml.find(title_xpath)
        if title_node is not None and title_node.text is not None:
            paragraphs.append(title_node.text)   
        
        variant_node = xml.find(variant_xpath)
        paragraphs.extend(self.find_paragraphs(variant_node))
                            
        return 0, "\n\n".join(paragraphs)
      
    def find_paragraphs(self, variant_node):
        """ Finds paragraphs in the given "chapter" node (body/div). """ 
        
        paragraphs = []
        paragraph = []
    
        def introduce_new_paragraph():
            """ Appends the current paragraph to the list of paragraphs if it is
            non-empty and introduces a new paragraph. """
            nonlocal paragraph
            nonlocal paragraphs
            
            if len(paragraph) > 0:
                paragraphs.append("".join(paragraph))
                paragraph = []
    
        def append_to_paragraph(text):
            """ Appends the given text to the current paragraph.""" 
            nonlocal paragraph
            paragraph.append(text)
    
        def iterate(node):
            """ Iterates through the child nodes of the given node recursively
            and decides where to split the text into paragraphs."""
            if node is None:
                return
            
            for sub_node in node.xpath("child::node()"):
                if sub_node is None:
                    continue
                # sub_node could be either text or a node.
                if isinstance(sub_node, etree._ElementUnicodeResult):
                    text = sub_node.strip("\n")
                    if len(text) > 0:
                        append_to_paragraph(text)
                elif isinstance(sub_node, etree._Element):                                        
                    if sub_node.tag == "bodyText":
                        # Put headings into separated paragraph.
                        introduce_new_paragraph()
                        iterate(sub_node)
                        introduce_new_paragraph()
                    elif sub_node.tag == "listItem":
                        iterate(sub_node)
                    elif sub_node.tag == "equation":
                        # Put formulas into separated paragraphs.
                        introduce_new_paragraph()
                        iterate(sub_node)
                        introduce_new_paragraph()
                        
        iterate(variant_node)
        introduce_new_paragraph() # Append the remaining paragraph.
        
        return paragraphs
                
    # --------------------------------------------------------------------------
    
            
if __name__ == "__main__":
    arg_parser = Extractor.get_argument_parser()
    args       = arg_parser.parse_args()
     
    ParscitExtractor(args).process()
