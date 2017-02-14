import sys
import os.path

# The current working directory.
CWD = os.path.dirname(os.path.realpath(__file__))
sys.path.insert(0, os.path.abspath(os.path.join(CWD, "../../bin")))

from extractor import ToolExtractor
from utils import file_utils
from lxml import etree

pages_xpath = "(/PDFResult/page)"
text_fragments_xpath = "(./text-fragment)"


class PdfXtkExtractor(ToolExtractor):

    def create_plain_output(self, raw_output_path):
        """
        Reads the given actual file. Override it if you have to do more
        advanced stuff, like removing semantic markups, etc.
        """

#       <?xml version="1.0" encoding="UTF-8"?>
#       <PDFResult>
#           <page page_number="1">
#               <text-block bold="false" font-name="" font-size="11.9552"
#                   h="107.81301" italic="false" type="paragraph" w="1613.1141"
#                   x="476.5" y="201.6866">
#                       OrbitalStructureandMagneticOrderinginLayeredManganites:UniversalCorrelationandItsMechanism  # NOQA
#               </text-block>
#               ...
#               <text-line bold="false" font-name="CMR10" font-size="9.96264"
#                   h="41.51103" italic="false" w="754.9349" x="906.5013" y="373.98785">  # NOQA
#                       S.Okamoto,S.Ishihara,andS.Maekawa
#               </text-line>
#               ...
#               <text-fragment bold="false" font-name="CMBX12"
#                   font-size="11.9552" h="49.813335" italic="false"
#                   w="176.12845" x="476.5" y="201.6866">
#                       Orbital
#               </text-fragment>
#               ...
#           </page>
#       </PDFResult>

        # The xml provides text blocks, text lines and text fragments (words?).
        # But in text blocks and text lines, words are not separated by white-
        # spaces. That's why we extract the words (even they are not broken
        # down per text blocks but per page).

        if file_utils.is_missing_or_empty_file(raw_output_path):
            return 11, None
        try:
            xml = etree.parse(raw_output_path, etree.XMLParser(recover=True))
        except:
            return 12, None

        pages = xml.xpath(pages_xpath)
        texts_per_page = []
        for page in pages:
            text_fragments = page.xpath(text_fragments_xpath)
            texts_per_page.append(" ".join(
                [x.text for x in text_fragments
                    if x is not None and x.text is not None]))

        return 0, "\n\n".join(texts_per_page)
