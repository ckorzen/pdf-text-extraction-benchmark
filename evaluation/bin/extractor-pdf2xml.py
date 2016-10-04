from extractor import Extractor
from lxml import etree
from os.path import isfile
from os.path import getsize

p_xpath = """/html/body/p"""

class Pdf2XmlExtractor(Extractor):
    
    def format_output(self, output_path):
        """ 
        Formats the given file.
        """
     
        if not self.is_missing_or_empty(output_path):
            xml = etree.parse(output_path, etree.XMLParser(recover=True))
            p_nodes = xml.xpath(p_xpath)
            return "\n\n".join([x.text for x in p_nodes if x is not None and x.text is not None])
        return ""

if __name__ == "__main__":
    Pdf2XmlExtractor(Extractor.get_argument_parser().parse_args()).process() 
