from extractor import Extractor
import file_util
from lxml import etree
from os.path import isfile

text_blocks_xpath = "(/PDFResult/page/text-block)"

class pdfXtkExtractor(Extractor):
    
    def create_plain_output(self, raw_output_path):
        ''' Reads the given actual file. Override it if you have to do more 
        advanced stuff, like removing semantic markups, etc.'''

        if not file_util.is_missing_or_empty_file(raw_output_path):
            xml = etree.parse(raw_output_path, etree.XMLParser(recover=True))
            
            text_block_nodes = xml.xpath(text_blocks_xpath)     
            text_blocks = [x.text for x in text_block_nodes if x.text != "[empty:empty]" and x.text != "[empty:spaces]"]
            return "\n\n".join(text_blocks)
        return ""

if __name__ == "__main__":
    arg_parser = Extractor.get_argument_parser()
    args       = arg_parser.parse_args()
     
    pdfXtkExtractor(args).process()
