import os
from lxml.etree import XMLParser
from xml.etree import ElementTree
from evaluator import Evaluator


class PdfToHtmlEvaluator(Evaluator):        
    def format_actual_file(self, file_path):
        '''
        Reads the given result file and formats it to a proper format.
        '''
        if os.path.isfile(file_path):
            parser = XMLParser(encoding="UTF-8", recover=True)
            root = ElementTree.parse(file_path, parser=parser).getroot()
            
            lines = []
            # Extract the text from <text> elements.
            for text_element in root.iter('text'):
                if text_element is not None and text_element.text is not None:
                    lines.append(text_element.text)
                
            return "\n".join(lines)
        else:
            return "" 

if __name__ == "__main__":      
    PdfToHtmlEvaluator(Evaluator.get_argument_parser().parse_args()).process()