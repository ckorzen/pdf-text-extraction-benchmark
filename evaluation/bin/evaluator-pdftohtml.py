import os
from lxml.etree import XMLParser
from xml.etree import ElementTree
from evaluator import Evaluator


class PdfToHtmlEvaluator(Evaluator):        
    def format_tool_file(self, tool_path):
        """ 
        Reads the given tool file. Override it if you have to do more 
        advanced stuff, like removing semantic markups, etc.
        """
        
        tool_output = ""        
        if not self.is_missing_or_empty(tool_path):
            parser = XMLParser(encoding="UTF-8", recover=True)
            root = ElementTree.parse(tool_path, parser=parser).getroot()
            
            lines = []
            # Extract the text from <text> elements.
            for text_element in root.iter('text'):
                if text_element is not None and text_element.text is not None:
                    lines.append(text_element.text)
                
            return "\n".join(lines)
        else:
            return "" 

if __name__ == "__main__":      
    PdfToHtmlEvaluator(Evaluator.get_argument_parser().parse_args()).evaluate()
