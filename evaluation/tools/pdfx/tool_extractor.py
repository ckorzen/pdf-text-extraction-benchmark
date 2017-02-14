import sys
import os.path

# The current working directory.
CWD = os.path.dirname(os.path.realpath(__file__))
sys.path.insert(0, os.path.abspath(os.path.join(CWD, "../../bin")))

from extractor import ToolExtractor
from lxml import etree
from utils import file_utils

titles_xpath = '(/pdfx/article/front/title-group/article-title)'
regions_xpath = """(/pdfx/article/body/region[@class="DoCO:TextChunk"]
                  | /pdfx/article/body/section/region[@class="DoCO:TextChunk"]
                  | /pdfx/article/body/h1
                  | /pdfx/article/body/h2
                  | /pdfx/article/body/h3
                  | /pdfx/article/body/h4
                  | /pdfx/article/body/section/h1
                  | /pdfx/article/body/section/h2
                  | /pdfx/article/body/section/h3
                  | /pdfx/article/body/section/h4)"""


class PdfxExtractor(ToolExtractor):
    def create_plain_output(self, raw_output_path):
        """
        Reads the given actual file. Override it if you have to do more
        advanced stuff, like removing semantic markups, etc.
        """

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

        paras = []
        titles = xml.xpath(titles_xpath)
        for title in titles:
            if title is not None and title.text is not None:
                paras.append(title.text)

        regions = xml.xpath(regions_xpath)
        for region in regions:
            paras.append(self.extract_text_from_region(region))
        return 0, "\n\n".join(paras)

    def extract_text_from_region(self, region):
        parts = []
        for sub_node in region.xpath("child::node()"):
            if isinstance(sub_node, etree._ElementUnicodeResult):
                text = sub_node.strip("\n")
                if len(text) > 0:
                    parts.append(text)
            elif isinstance(sub_node, etree._Element):
                if sub_node.tag == "xref":
                    if sub_node.text is not None:
                        parts.append(sub_node.text)
                elif sub_node.tag == "marker":
                    if sub_node.text is not None:
                        parts.append(sub_node.text)
        return "".join(parts)
