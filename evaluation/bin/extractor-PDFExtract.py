from extractor import Extractor
from lxml import etree
from os.path import isfile

import file_util

ns  = {'x': 'http://www.tei-c.org/ns/1.0'}
paras_xpath = """(./x:text/x:body/x:div/x:p 
                | ./x:text/x:body/x:div1/x:p 
                | ./x:text/x:body/x:div2/x:p)"""

class PDFExtractExtractor(Extractor):
    
     def create_plain_output(self, raw_output_path):        
        ''' Reads the given actual file. Override it if you have to do more 
        advanced stuff, like removing semantic markups, etc.'''

        if not file_util.is_missing_or_empty_file(raw_output_path):
            xml = etree.parse(raw_output_path, etree.XMLParser(recover=True))
            para_nodes = xml.xpath(paras_xpath, namespaces=ns)   
            return "\n\n".join([para_node.text for para_node in para_nodes if para_node is not None and para_node.text is not None])
        return ""

if __name__ == "__main__":
    arg_parser = Extractor.get_argument_parser()
    args       = arg_parser.parse_args()
     
    PDFExtractExtractor(args).process()
    
