import os
from lxml import etree
from extractor import Extractor

blocks_xpath = """/DOCUMENT/PAGE/BLOCK"""
token_xpath  = """./TEXT/TOKEN"""

class PdfToXmlExtractor(Extractor):           
    def format_output(self, output_path):
        """ 
        Formats the given file.
        """
        
        formatted_lines = []       
        if not self.is_missing_or_empty(output_path):
            xml = etree.parse(output_path, etree.XMLParser(recover=True))
            block_nodes = xml.xpath(blocks_xpath)
            blocks = []
            for block_node in block_nodes:
                token_nodes = block_node.xpath(token_xpath)
                blocks.append(" ".join([x.text for x in token_nodes if x.text is not None]))      
            return "\n\n".join(blocks)
        return ""
        
if __name__ == "__main__":      
    PdfToXmlExtractor(Extractor.get_argument_parser().parse_args()).process()
