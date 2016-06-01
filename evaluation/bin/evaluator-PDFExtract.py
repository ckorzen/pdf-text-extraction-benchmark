from evaluator import Evaluator
from lxml import etree
from os.path import isfile

ns  = {'x': 'http://www.tei-c.org/ns/1.0'}
paras_xpath = """(./x:text/x:body/x:div/x:p 
                | ./x:text/x:body/x:div1/x:p 
                | ./x:text/x:body/x:div2/x:p)"""

class PDFExtractEvaluator(Evaluator):
    
    def format_actual_file(self, file_path):
        ''' Reads the given actual file. Override it if you have to do more 
        advanced stuff, like removing semantic markups, etc.'''

        if isfile(file_path):
            xml = etree.parse(file_path, etree.XMLParser(recover=True))
            para_nodes = xml.xpath(paras_xpath, namespaces=ns)   
            return "\n\n".join([para_node.text for para_node in para_nodes])
        return ""

if __name__ == "__main__":
    PDFExtractEvaluator(Evaluator.get_argument_parser().parse_args()).evaluate() 
