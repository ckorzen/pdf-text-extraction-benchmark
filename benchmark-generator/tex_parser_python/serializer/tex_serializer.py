import sys

import json

import xml.etree.ElementTree as et
from xml.dom import minidom

from collections import OrderedDict
from models.ltb import LTB, OutlineLevel

DEFAULT_ENCODING = "UTF-8"
DEFAULT_INDENT = 4

NAME_ROOT = "result"  # Only needed in xml.
NAME_METADATA = "metadata"
NAME_METADATA_FILE_PATH = "path"
NAME_OUTLINE = "outline"
NAME_OUTLINE_TYPE = "type"  # Only needed in json.
NAME_OUTLINE_ELEMENTS = "elements"  # Only needed in json.
NAME_OUTLINE_LEVEL = "outline-level"
NAME_BLOCK = "block"
NAME_BLOCK_LEVEL = "level"
NAME_BLOCK_SEMANTIC_ROLE = "role"
NAME_BLOCK_TEXT = "text"  # Only needed in json.


class TeXDocumentSerializer:
    def __init__(self, roles_filter=[]):
        self.roles_filter = set(roles_filter)

    def serialize(self, doc, output_path):
        """
        Serializes the given TeX document to given output path or to stdout if
        there is no output_path given.
        """
        handle = open(output_path, 'w') if output_path else sys.stdout
        handle.write(self.format_doc(doc))
        if handle is not sys.stdout:
            handle.close()

    def format_doc(self, doc):
        return ""

    def matches_roles_filter(self, block):
        if len(self.roles_filter) == 0:
            return True
        if block.semantic_role in self.roles_filter:
            return True
        return False


class TeXDocumentTxtSerializer(TeXDocumentSerializer):
    """
    A class that serializes TeX documents to plain text format.
    """

    def format_doc(self, doc):
        return self.format_outline(doc)

    def format_outline(self, doc):
        parts = self.format_outline_level(doc.outline.root)
        return "\n\n".join(parts) + "\n"

    def format_outline_level(self, element):
        level_str = []
        if isinstance(element, OutlineLevel):
            for el in element.elements:
                level_str.extend(self.format_outline_level(el))
        elif isinstance(element, LTB) and self.matches_roles_filter(element):
            level_str.append(element.text)
        return level_str


class TeXDocumentXmlSerializer(TeXDocumentSerializer):
    """
    A class that serializes TeX documents to XML format.
    """
    def format_doc(self, doc):
        xml_tree = et.Element("result")

        metadata_xml = self.format_metadata(doc)
        if metadata_xml is not None:
            xml_tree.append(metadata_xml)
        outline_xml = self.format_outline(doc)
        if outline_xml is not None:
            xml_tree.append(outline_xml)

        return self.to_pretty_xml(xml_tree)

    def format_metadata(self, doc):
        metadata_xml = et.Element(NAME_METADATA)
        path_xml = et.SubElement(metadata_xml, NAME_METADATA_FILE_PATH)
        path_xml.text = "foo/bar/baz.py"
        return metadata_xml

    def format_outline(self, doc):
        contains_blocks, formatted = self.format_outline_level(doc.outline.root)
        if contains_blocks and formatted is not None:
            outline_xml = et.Element(NAME_OUTLINE)
            outline_xml.append(formatted)
            return outline_xml
        return None

    def format_outline_level(self, element):
        contains_blocks = False
        if isinstance(element, OutlineLevel):
            outline_level_xml = et.Element(NAME_OUTLINE_LEVEL)
            outline_level_xml.set(NAME_BLOCK_LEVEL, str(element.level))
            for el in element.elements:
                contains_block, formatted = self.format_outline_level(el)
                if contains_block and formatted is not None:
                    outline_level_xml.append(formatted)
                contains_blocks = contains_blocks or contains_block
            return (contains_blocks, outline_level_xml)
        elif isinstance(element, LTB) and self.matches_roles_filter(element):
            block_xml = et.Element(NAME_BLOCK)
            block_xml.set(NAME_BLOCK_SEMANTIC_ROLE, element.semantic_role)
            block_xml.text = element.text
            return (True, block_xml)
        return False, None

    def to_pretty_xml(self, xml):
        parsed = minidom.parseString(et.tostring(xml, DEFAULT_ENCODING))
        return parsed.toprettyxml(indent=DEFAULT_INDENT * " ")


class TeXDocumentJsonSerializer(TeXDocumentSerializer):
    """
    A class that serializes TeX documents to JSON format.
    """

    def format_doc(self, doc):
        data = OrderedDict()
        metadata = self.format_metadata(doc)
        if metadata is not None:
            data.update(metadata)
        outline = self.format_outline(doc)
        if outline is not None:
            data.update(outline)
        return json.dumps(data, indent=DEFAULT_INDENT) + "\n"

    def format_metadata(self, doc):
        return OrderedDict([
            (NAME_METADATA, OrderedDict([
                (NAME_METADATA_FILE_PATH, "/foo/bar/baz.tex")
            ]))
        ])

    def format_outline(self, doc):
        contains_blocks, outline_str = self.format_outline_level(doc.outline.root)
        if contains_blocks and outline_str is not None:
            return OrderedDict([
                (NAME_OUTLINE, outline_str)
            ])

    def format_outline_level(self, element):
        if isinstance(element, OutlineLevel):
            outline_elements = []
            contains_blocks = False
            for el in element.elements:
                contains_block, formatted = self.format_outline_level(el)
                contains_blocks = contains_blocks or contains_block
                if contains_block and formatted is not None:
                    outline_elements.append(formatted)

            return (contains_blocks, OrderedDict([
                (NAME_OUTLINE_TYPE, NAME_OUTLINE_LEVEL),
                (NAME_BLOCK_LEVEL, element.level),
                (NAME_OUTLINE_ELEMENTS, outline_elements)
            ]))
        elif isinstance(element, LTB) and self.matches_roles_filter(element):
            return (True, OrderedDict([
                (NAME_OUTLINE_TYPE, NAME_BLOCK),
                (NAME_BLOCK_SEMANTIC_ROLE, element.semantic_role),
                (NAME_BLOCK_TEXT, element.text),
            ]))
        return False, None

# =============================================================================

serializers = {
    "txt": TeXDocumentTxtSerializer,
    "xml": TeXDocumentXmlSerializer,
    "json": TeXDocumentJsonSerializer,
}


def get_choices():
    """
    Returns the supported serialization formats.
    """
    return sorted(list(serializers.keys()))


def serialize(doc, output_path, output_format="txt", roles_filter=[]):
    if output_format not in serializers:
        raise ValueError("The format '%s' is not supported" % output_format)
    serializers[output_format](roles_filter).serialize(doc, output_path)
