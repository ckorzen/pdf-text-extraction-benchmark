import json

from collections import OrderedDict

from base_serializer import BaseSerializer
from base_serializer import TagNames
from base_serializer import Settings


class JsonSerializer(BaseSerializer):
    """
    A class that serializes TeX documents to JSON format.
    """

    def format_metadata(self, doc):
        return OrderedDict([
            (TagNames.metadata, OrderedDict([
                (TagNames.file_path, "/foo/bar/baz.tex")
            ]))
        ])

    def format_outline(self, doc):
        outline = super().format_outline(doc)
        if outline is not None:
            return OrderedDict([
                (TagNames.outline, outline)
            ])

    def format_outline_level(self, level):
        outline_elements = []
        for element in level.elements:
            outline_element = self.format_outline_element(element)
            if outline_element is not None:
                outline_elements.append(outline_element)

        if len(outline_elements) > 0:
            return OrderedDict([
                (TagNames.typ, TagNames.outline_level),
                (TagNames.level, level.level),
                (TagNames.elements, outline_elements)
            ])

    def format_block(self, block):
        return OrderedDict([
            (TagNames.typ, TagNames.block),
            (TagNames.semantic_role, block.semantic_role),
            (TagNames.text, block.text),
        ])

    def format_doc(self, metadata, outline):
        data = OrderedDict()
        if metadata is not None:
            data.update(metadata)
        if outline is not None:
            data.update(outline)
        return data

    def _serialize(self, data):
        return json.dumps(data, indent=Settings.indent) + "\n"
