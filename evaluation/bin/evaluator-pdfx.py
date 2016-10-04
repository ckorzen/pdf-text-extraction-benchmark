from evaluator import Evaluator
from lxml import etree
from os.path import isfile

ns  = {'x': 'http://www.w3.org/2001/XMLSchema-instance'}

# The text max contain <xref> nodes representing cites. To ignore them, take the 
# text()

xpaths = """(///article-title
           | ///h1
           | ///h2
           | ///h3
           | ///h4
           | ///region[@class='DoCO:TextChunk'])"""

class PdfXEvaluator(Evaluator):
    def format_actual_file(self, file_path):
        ''' Reads the given actual file. Override it if you have to do more 
        advanced stuff, like removing semantic markups, etc.'''

        if isfile(file_path):
            xml = etree.parse(file_path, etree.XMLParser(recover=True))
            nodes = xml.xpath(xpaths)
            paras = []
            for node in nodes:
                paras.append("".join([x for x in node.itertext()]))         
            return "\n\n".join(paras)
        return ""

if __name__ == "__main__":
    PdfXEvaluator(Evaluator.get_argument_parser().parse_args()).evaluate() 
