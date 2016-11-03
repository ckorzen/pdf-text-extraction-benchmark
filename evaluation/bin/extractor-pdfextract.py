from extractor import Extractor
from lxml import etree
from os.path import isfile
from os.path import getsize

import file_util

title_xpath = "title"
sections_xpath = "(/pdf/section)"
line_xpath = "(./line)"

class PdfExtractExtractor(Extractor):
    
    def create_plain_output(self, raw_output_path):
        """ 
        Formats the given file.
        """
     
        if not file_util.is_missing_or_empty_file(raw_output_path):
            xml = etree.parse(raw_output_path, etree.XMLParser(recover=True))
            
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
    arg_parser = Extractor.get_argument_parser()
    args       = arg_parser.parse_args()
     
    PdfExtractExtractor(args).process()
