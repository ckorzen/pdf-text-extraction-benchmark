import sys
import os.path
from lxml import etree

# The current working directory.
CWD = os.path.dirname(os.path.realpath(__file__))
sys.path.insert(0, os.path.abspath(os.path.join(CWD, "../../bin")))

from extractor import ToolExtractor
from utils import file_utils

title_xpath = "title"
regions_xpath = "(/pdf/page/region)"
line_xpath = "(./line)"


class PdfExtractExtractor(ToolExtractor):

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

        regions = []

        # Extract the title.
        title_nodes = xml.xpath(title_xpath)
        regions.append(
            "".join([x.text.replace("\n", " ").strip() for x in title_nodes]))

        # Extract the lines.
        region_nodes = xml.xpath(regions_xpath)
        for node in region_nodes:
            line_nodes = node.xpath(line_xpath)
            regions.append("\n".join(
                [x.text.replace("\n", " ").strip() for x in line_nodes
                    if x is not None and x.text is not None]))
        return 0, "\n\n".join(regions)
