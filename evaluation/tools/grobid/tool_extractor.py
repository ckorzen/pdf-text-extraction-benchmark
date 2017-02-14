import sys
import os.path

# The current working directory.
CWD = os.path.dirname(os.path.realpath(__file__))
sys.path.insert(0, os.path.abspath(os.path.join(CWD, "../../bin")))

from extractor import ToolExtractor
from lxml import etree

from utils import file_utils

ns = {"ns": "http://www.tei-c.org/ns/1.0"}

title_xpath = """./ns:teiHeader/ns:fileDesc/ns:sourceDesc//ns:title"""
chapters_xpath = """./ns:text/ns:body/ns:div"""


class GrobidExtractor(ToolExtractor):
    # <?xml version="1.0" encoding="UTF-8"?>
    # <TEI xmlns="http://www.tei-c.org/ns/1.0" ...>
    #   <teiHeader xml:lang="en">
    #       <encodingDesc>
    #     ...
    #     </encodingDesc>
    #     <fileDesc>
    #           ...
    #           <sourceDesc>
    #       <biblStruct>
    #       <analytic>
    #           <author role="corresp">
    #                           ...
    #                       </author>
    #         <title level="a" type="main">Reducing quasi-ergodicity in a double well potential by Tsallis Monte Carlo simulation</title>  # NOQA
    #       </analytic>
    #        ...
    #       </biblStruct>
    #      </sourceDesc>
    #    </fileDesc>
    #     <profileDesc>
    #      ...
    #      <abstract>
    #       <p>...</p>
    #     </abstract>
    #    </profileDesc>
    #   </teiHeader>
    #   <text xml:lang="en">
    #    <body>
    #           <div xmlns="http://www.tei-c.org/ns/1.0"><head>1 Introduction</head><p> The ergodic hypothesis is fundamental to statistical mechanics. This hypothesis states that the time average of an observable event equals the phase-space average. In practical application, however, problems can arise in various types of simulations if the system must overcome high energy barriers to reach other regions of phase space. In that case, the length of a simulation needed in order to obtain enough statistical samples of all regions of phase space may be extremely long. In the Monte Carlo simulation , the errors arise as a consequence of the finite length of the Monte Carlo walk. This error can be serious in canonical Monte Carlo sampling, especially at low temperatures. This problem, referred as " quasi-ergodicity " by Valleau and Whittington<ref type="bibr" coords="2,267.17,298.42,10.92,10.91" target="#b0"> [1]</ref>...</div>  # NOQA
    #           <div xmlns="http://www.tei-c.org/ns/1.0"><head> 2 A generalized Monte Carlo scheme</head><p> In the generalized statistical mechanics proposed by Tsallis<ref type="bibr" coords="2,406.56,546.82,10.83,10.91" target="#b5"> [6]</ref>, a crucial role is played by the generalized entropy S q defined as</p><formula> S q = k 1 − p q i q − 1 (1)</formula><p> where q is a real number which characterizes the statistical mechanics, and p i is the probability of states i. This entropy S q becomes the  # NOQA
    #           </div>
    #       </body>
    #   </text>
    # </TEI>

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
        title_node = xml.find(title_xpath, namespaces=ns)
        if title_node is not None and title_node.text is not None:
            paragraphs.append(title_node.text)

        # Extract paragraphs from the body.
        chapter_nodes = xml.findall(chapters_xpath, namespaces=ns)
        for chapter_node in chapter_nodes:
            paragraphs.extend(self.find_paragraphs(chapter_node))

        return 0, "\n\n".join(paragraphs)

    def find_paragraphs(self, chapter_node):
        """ Finds paragraphs in the given "chapter" node (body/div). """
        paragraphs = []
        paragraph = []

        def introduce_new_paragraph():
            """
            Appends the current paragraph to the list of paragraphs if it
            is non-empty and introduces a new paragraph.
            """
            nonlocal paragraph
            nonlocal paragraphs

            if len(paragraph) > 0:
                paragraphs.append("".join(paragraph))
                paragraph = []

        def append_to_paragraph(text):
            """ Appends the given text to the current paragraph."""
            nonlocal paragraph

            # Remove @BULLET annotations from text.
            text = text.replace("@BULLET ", "")

            paragraph.append(text)

        def iterate(node):
            """ Iterates through the child nodes of the given node recursively
            and decides where to split the text into paragraphs."""
            for sub_node in node.xpath("child::node()"):

                # sub_node could be either text or a node.
                if isinstance(sub_node, etree._ElementUnicodeResult):
                    text = sub_node.strip("\n")
                    if len(text) > 0:
                        append_to_paragraph(text)
                elif isinstance(sub_node, etree._Element):
                    tag = self.remove_namespace(sub_node.tag)

                    if tag == "head":
                        # Put headings into separated paragraph.
                        introduce_new_paragraph()
                        iterate(sub_node)
                        introduce_new_paragraph()
                    elif tag == "p":
                        iterate(sub_node)
                    elif tag == "ref":
                        iterate(sub_node)
                    elif tag == "formula":
                        # Put formulas into separated paragraphs.
                        introduce_new_paragraph()
                        iterate(sub_node)
                        introduce_new_paragraph()

        iterate(chapter_node)
        introduce_new_paragraph()  # Append the remaining paragraph.

        return paragraphs

    def remove_namespace(self, tag):
        if '}' in tag:
            return tag.split('}', 1)[1]
        return tag
