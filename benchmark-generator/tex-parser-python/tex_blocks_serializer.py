import json

class TeXBlocksSerializer:
    def serialize(self, blocks, output_path):
        with open(output_path, "w") as f:
            f.write(self.format_blocks(blocks))

    def format_blocks(self, blocks):
        return ""


class TeXBlocksTxtSerializer(TeXBlocksSerializer):
    """
    A class that serializes logical text blocks to plain text format.
    """

    def format_blocks(self, blocks):
        return "\n\n".join([x.text for x in blocks]) + "\n"


class TeXBlocksXMLSerializer(TeXBlocksSerializer):
    """
    A class that serializes logical text blocks to XML format.
    """

    def format_blocks(self, blocks):
        return ""


class TeXBlocksJSONSerializer(TeXBlocksSerializer):
    """
    A class that serializes logical text blocks to JSON format.
    """

    def format_blocks(self, blocks):
        return json.dumps(
            blocks,
            default=lambda x: x.__dict__,
            sort_keys=True,
            indent=4)


serializers = {
    "txt": TeXBlocksTxtSerializer,
    "xml": TeXBlocksXMLSerializer,
    "json": TeXBlocksJSONSerializer,
}


def serialize(blocks, output_path, output_format="txt"):
    if output_format not in serializers:
        raise ValueError("The format '%s' is not supported" % output_format)

    serializers[output_format]().serialize(blocks, output_path)
