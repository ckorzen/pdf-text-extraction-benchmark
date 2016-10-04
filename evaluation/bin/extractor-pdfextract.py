from extractor import Extractor
from lxml import etree
from os.path import isfile
from os.path import getsize

title_xpath = "title"
sections_xpath = "(/pdf/section)"
line_xpath = "(./line)"

class PdfExtractExtractor(Extractor):
    
    def format_output(self, output_path):
        """ 
        Formats the given file.
        """
     
        if not self.is_missing_or_empty(output_path):
            xml = etree.parse(output_path, etree.XMLParser(recover=True))
            
            sections = []

            # Extract the title.
            title_nodes = xml.xpath(title_xpath)             
            sections.append("".join([x.text.replace("\n", " ").strip() for x in title_nodes]))                

            # Extract the lines.
            section_nodes = xml.xpath(sections_xpath) 
            for node in section_nodes:
                line_nodes = node.xpath(line_xpath)                                             
                sections.append("\n".join([x.text.replace("\n", " ").strip() for x in line_nodes if x is not None and x.text is not None]))
            return "\n\n".join(sections)
        return ""

if __name__ == "__main__":
    PdfExtractExtractor(Extractor.get_argument_parser().parse_args()).process() 
