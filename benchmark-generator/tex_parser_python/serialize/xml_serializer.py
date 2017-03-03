import xml.etree.ElementTree as xml
import xml.dom.minidom as minidom

from base_serializer import BaseSerializer
from base_serializer import TagNames
from base_serializer import Settings


class XmlSerializer(BaseSerializer):
    """
    A class that serializes TeX documents to XML format.
    """

    def format_metadata(self, doc):
        root_element = XmlElement(TagNames.metadata)
        root_element.sub(TagNames.file_path, text="foo/bar/baz.py")  # TODO
        return root_element

    def format_outline(self, doc):
        outline = super().format_outline(doc)
        if outline is not None:
            outline_element = XmlElement(TagNames.outline)
            outline_element.sub_element(outline)
            return outline_element

    def format_outline_level(self, level):
        root_element = XmlElement(TagNames.outline_level, {
            TagNames.level: str(level.level)
        })
        for element in level.elements:
            outline_element = self.format_outline_element(element)
            if outline_element is not None:
                root_element.sub_element(outline_element)
        return root_element if root_element.num_children() > 0 else None

    def format_block(self, block):
        return XmlElement(
            TagNames.block,
            text=block.text,
            attributes={TagNames.semantic_role: block.semantic_role}
        )

    def format_doc(self, metadata, outline):
        root_element = XmlElement(TagNames.root)

        if metadata is not None:
            root_element.sub_element(metadata)
        if outline is not None:
            root_element.sub_element(outline)

        return root_element

    def _serialize(self, data):
        parsed = minidom.parseString(str(data))
        return parsed.toprettyxml(indent=Settings.indent * " ")


class XmlElement:
    def __init__(self, tag_name, attributes={}, text=None):
        self.element = xml.Element(tag_name, attributes)
        self.element.text = text

    def sub(self, tag_name, attributes={}, text=None):
        sub = xml.SubElement(self.element, tag_name, attributes)
        sub.text = text
        return sub

    def sub_element(self, other):
        self.element.append(other.element)

    def num_children(self):
        return len(list(self.element))

    def __str__(self):
        return str(xml.tostring(self.element), Settings.encoding)
