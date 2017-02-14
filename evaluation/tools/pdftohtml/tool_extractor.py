import sys
import os.path

# The current working directory.
CWD = os.path.dirname(os.path.realpath(__file__))
sys.path.insert(0, os.path.abspath(os.path.join(CWD, "../../bin")))

from utils import file_utils
from utils import string_utils
from lxml import etree
from extractor import ToolExtractor


class PdfToHtmlExtractor(ToolExtractor):
    # The general structure of output from pdftohtml is:

    # <?xml version="1.0" encoding="UTF-8"?>
    # <!DOCTYPE pdf2xml SYSTEM "pdf2xml.dtd">
    # <pdf2xml producer="poppler" version="0.33.0">
    #   <page number="1" position="absolute" top="0" left="0" height="792" width="612">  # NOQA
    #     <text ...>This is a link: <a href="...">Hello World</a>.</text>
    #   </page>
    # </pdf2xml>

    # where each "<text>" node relates to a line in pdf. Note that a <text> node  # NOQA
    # may contain nested "<a>" and "<i>" nodes.

    def create_plain_output(self, raw_output_path):
        formatted_lines = []
        if file_utils.is_missing_or_empty_file(raw_output_path):
            return 11, None

        try:
            xml = etree.parse(
                raw_output_path,
                etree.XMLParser(encoding="utf-8", recover=True)
            )
        except:
            return 12, None

        # Find all <text> nodes.
        line_nodes = xml.xpath("/pdf2xml/page/text")

        for node in line_nodes:
            if node is None:
                continue

            # Extract the text, including text in <a> and <i> elements.
            text_nodes = node.xpath("text() | a/text() | i/text()")

            # Compose the text and remove special characters like ^M
            text = "".join(text_nodes)
            text = string_utils.remove_control_characters(text)

            formatted_lines.append(text)

        return 0, "\n".join(formatted_lines)
