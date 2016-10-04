import os
from lxml import etree
from extractor import Extractor

class PdfToHtmlExtractor(Extractor):     
    ### The general structure of output from pdftohtml is:
    
    # <?xml version="1.0" encoding="UTF-8"?>
    # <!DOCTYPE pdf2xml SYSTEM "pdf2xml.dtd">
    # <pdf2xml producer="poppler" version="0.33.0">
    #   <page number="1" position="absolute" top="0" left="0" height="792" width="612">
    #     <text ...>This is a link: <a href="...">Hello World</a>.</text>
    #   </page>
    # </pdf2xml>
   
    # where each "<text>" node relates to a line in pdf. Note that a <text> node
    # may contain nested "<a>" and "<i>" nodes.
      
    def format_output(self, output_path):
        """ 
        Formats the given file.
        """
        
        formatted_lines = []       
        if not self.is_missing_or_empty(output_path):
            xml = etree.parse(output_path, etree.XMLParser(recover=True))
            
            # Find all <text> nodes.
            line_nodes = xml.xpath("/pdf2xml/page/text")
            
            for node in line_nodes:                
                if node is None:
                    continue

                # Extract the text nodes (including the tex nodes in <a> and 
                # <i> nodes.
                
                text_nodes = node.xpath("text() | a/text() | i/text()")
                
                formatted_lines.append("".join(text_nodes))            
            return "\n".join(formatted_lines)
        else:
            return "" 

if __name__ == "__main__":      
    PdfToHtmlExtractor(Extractor.get_argument_parser().parse_args()).process()
