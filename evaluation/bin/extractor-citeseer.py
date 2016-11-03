from extractor import Extractor
from lxml import etree

import file_util

title_xpath = """./algorithm[@name="SVM HeaderParse"]/title"""
body_xpath = """./body"""

class CiteSeerExtractor(Extractor):            
    
    def create_plain_output(self, raw_output_path):
        """ 
        Formats the given file.
        """
                       
        if file_util.is_missing_or_empty_file(raw_output_path):
            return ""
        
        # Read in the xml.
        xml = etree.parse(raw_output_path, etree.XMLParser(recover=True))

        paragraphs = []
        
        # Extract the title as separate paragraph.
        title_node = xml.find(title_xpath)
        if title_node is not None and title_node.text is not None:
            paragraphs.append(title_node.text)   
        
        body_node = xml.find(body_xpath)
        if body_node is not None and body_node.text is not None:
            paragraphs.append(body_node.text)
                                   
        return "\n\n".join(paragraphs)
    
if __name__ == "__main__": 
    arg_parser = Extractor.get_argument_parser()
    args       = arg_parser.parse_args()
     
    CiteSeerExtractor(args).process()
