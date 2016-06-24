from evaluator import Evaluator
from lxml import etree
from os.path import isfile

sections_xpath = "(/pdf/section)"
line_xpath = "(./line)"

class pdfextractEvaluator(Evaluator):
    
    def format_actual_file(self, file_path):
        ''' Reads the given actual file. Override it if you have to do more 
        advanced stuff, like removing semantic markups, etc.'''

        if isfile(file_path):
            xml = etree.parse(file_path, etree.XMLParser(recover=True))
            
            sections = []
            section_nodes = xml.xpath(sections_xpath) 
            
            for node in section_nodes:
                line_nodes = node.xpath(line_xpath)                              
                lines = [x.text.replace("\n", " ").strip() for x in line_nodes]                
                sections.append("\n".join(lines))
            return "\n\n".join(sections)
        return ""

if __name__ == "__main__":
    pdfextractEvaluator(Evaluator.get_argument_parser().parse_args()).evaluate()
