from evaluator import Evaluator
from lxml import etree
from os.path import isfile

blocks_xpath = """/DOCUMENT/PAGE/BLOCK"""
token_xpath  = """./TEXT/TOKEN"""

class PdfToXmlEvaluator(Evaluator):
    
    def format_actual_file(self, file_path):
        ''' Reads the given actual file. Override it if you have to do more 
        advanced stuff, like removing semantic markups, etc.'''

        if isfile(file_path):
            xml = etree.parse(file_path, etree.XMLParser(recover=True))
            block_nodes = xml.xpath(blocks_xpath)
            blocks = []
            for block_node in block_nodes:
                token_nodes = block_node.xpath(token_xpath)
                blocks.append(" ".join([x.text for x in token_nodes if x.text is not None]))      
            return "\n\n".join(blocks)
        return ""

if __name__ == "__main__":
    PdfToXmlEvaluator(Evaluator.get_argument_parser().parse_args()).evaluate() 
