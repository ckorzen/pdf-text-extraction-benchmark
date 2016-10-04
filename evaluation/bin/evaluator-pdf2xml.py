from evaluator import Evaluator
from lxml import etree
from os.path import isfile
from os.path import getsize

p_xpath = """/html/body/p"""

class Pdf2XmlEvaluator(Evaluator):
    
    def format_actual_file(self, file_path):
        ''' Reads the given actual file. Override it if you have to do more 
        advanced stuff, like removing semantic markups, etc.'''

        if isfile(file_path) and getsize(file_path) > 0:
            xml = etree.parse(file_path, etree.XMLParser(recover=True))
            p_nodes = xml.xpath(p_xpath)
            return "\n\n".join([x.text for x in p_nodes])
        return ""

if __name__ == "__main__":
    Pdf2XmlEvaluator(Evaluator.get_argument_parser().parse_args()).evaluate() 
