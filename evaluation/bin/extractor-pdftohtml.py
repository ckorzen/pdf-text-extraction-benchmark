import os
from lxml import etree
from extractor import Extractor

import util
import files_util

class PdfToHtmlExtractor(Extractor):     
    # The general structure of output from pdftohtml is:
    
    # <?xml version="1.0" encoding="UTF-8"?>
    # <!DOCTYPE pdf2xml SYSTEM "pdf2xml.dtd">
    # <pdf2xml producer="poppler" version="0.33.0">
    #   <page number="1" position="absolute" top="0" left="0" height="792" width="612">
    #     <text ...>This is a link: <a href="...">Hello World</a>.</text>
    #   </page>
    # </pdf2xml>
   
    # where each "<text>" node relates to a line in pdf. Note that a <text> node
    # may contain nested "<a>" and "<i>" nodes.
      
    def create_plain_output(self, raw_output_path):        
        
        formatted_lines = []       
        if not file_util.is_missing_or_empty_file(raw_output_path):
            xml = etree.parse(raw_output_path, etree.XMLParser(recover=True))
            
            # Find all <text> nodes.
            line_nodes = xml.xpath("/pdf2xml/page/text")
            
            for node in line_nodes:                
                if node is None:
                    continue

                # Extract the text, including text in <a> and <i> elements.
                text_nodes = node.xpath("text() | a/text() | i/text()")
                
                # Compose the text and remove special characters like ^M
                text = "".join(text_nodes)
                text = util.remove_control_characters(text)
                                
                formatted_lines.append(text)            
            return "\n".join(formatted_lines)
        else:
            return "" 

if __name__ == "__main__": 
    arg_parser = Extractor.get_argument_parser()
    args       = arg_parser.parse_args()
     
    PdfToHtmlExtractor(args).process()
