import sys
import os.path

# The current working directory.
CWD = os.path.dirname(os.path.realpath(__file__))
sys.path.insert(0, os.path.abspath(os.path.join(CWD, "../../bin")))

from extractor import ToolExtractor
from lxml import etree
from utils import file_utils

ns = {'x': 'http://www.tei-c.org/ns/1.0'}
paras_xpath = """(./x:text/x:body/x:div/x:p
                | ./x:text/x:body/x:div1/x:p
                | ./x:text/x:body/x:div2/x:p)"""


class PDFExtractExtractor(ToolExtractor):

    def create_plain_output(self, raw_output_path):
        """
        Reads the given actual file. Override it if you have to do more
        advanced stuff, like removing semantic markups, etc.
        """

        if file_utils.is_missing_or_empty_file(raw_output_path):
            return 11, None
        try:
            xml = etree.parse(raw_output_path, etree.XMLParser(recover=True))
        except:
            return 12, None

        para_nodes = xml.xpath(paras_xpath, namespaces=ns)
        return 0, "\n\n".join(
            [para_node.text for para_node in para_nodes
                if para_node is not None and para_node.text is not None])
