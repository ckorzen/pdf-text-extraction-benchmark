from evaluator import Evaluator
from lxml import etree
from os.path import isfile

text_blocks_xpath = "(/PDFResult/page/text-block)"

class pdfXtkEvaluator(Evaluator):
    
    def format_actual_file(self, file_path):
        ''' Reads the given actual file. Override it if you have to do more 
        advanced stuff, like removing semantic markups, etc.'''

        if isfile(file_path):
            xml = etree.parse(file_path, etree.XMLParser(recover=True))
            
            text_block_nodes = xml.xpath(text_blocks_xpath)     
            text_blocks = [x.text for x in text_block_nodes if x.text != "[empty:empty]" and x.text != "[empty:spaces]"]
            return "\n\n".join(text_blocks)
        return ""

if __name__ == "__main__":
    pdfXtkEvaluator(Evaluator.get_argument_parser().parse_args()).evaluate()
