import sys
import os.path

# The current working directory.
CWD = os.path.dirname(os.path.realpath(__file__))
sys.path.insert(0, os.path.abspath(os.path.join(CWD, "../../bin")))

from extractor import ToolExtractor
from lxml import etree
from utils import file_utils

title_xpath = """./algorithm[@name="SVM HeaderParse"]/title"""
body_xpath = """./body"""


class CiteSeerExtractor(ToolExtractor):
    def create_plain_output(self, raw_output_path):
        """
        Formats the given file.
        """
        if file_utils.is_missing_or_empty_file(raw_output_path):
            return 11, None

        try:
            # Read in the xml.
            xml = etree.parse(raw_output_path, etree.XMLParser(recover=True))
        except:
            return 12, None

        paragraphs = []

        # Extract the title as separate paragraph.
        title_node = xml.find(title_xpath)
        if title_node is not None and title_node.text is not None:
            paragraphs.append(title_node.text)

        body_node = xml.find(body_xpath)
        if body_node is not None and body_node.text is not None:
            paragraphs.append(body_node.text)

        return 0, "\n\n".join(paragraphs)
