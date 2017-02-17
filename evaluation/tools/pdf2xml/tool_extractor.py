import sys
import os.path

from lxml import etree

# The current working directory.
CWD = os.path.dirname(os.path.realpath(__file__))
sys.path.insert(0, os.path.abspath(os.path.join(CWD, "../../bin")))

from extractor import ToolExtractor  # NOQA
from utils import file_utils  # NOQA

p_xpath = """/html/body/p"""


class Pdf2XmlExtractor(ToolExtractor):

    def create_plain_output(self, raw_output_path):
        """
        Formats the given file.
        """

        if file_utils.is_missing_or_empty_file(raw_output_path):
            return 11, None
        try:
            xml = etree.parse(raw_output_path, etree.XMLParser(recover=True))
        except:
            return 12, None

        p_nodes = xml.xpath(p_xpath)
        return 0, "\n\n".join(
            [x.text for x in p_nodes if x is not None and x.text is not None])
