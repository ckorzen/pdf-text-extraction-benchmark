import sys

from models.ltb import OutlineLevel, LTB


class BaseSerializer:
    """
    The base class for any serializers.
    """

    def __init__(self, roles_filter=[]):
        """
        Creates a new serializer.
        """
        self.roles_filter = set(roles_filter)

    def serialize(self, document, target=None):
        """
        Serializes the given TeX document to given target. If target is None,
        the serialization is printed to stdout.
        """
        metadata = self.format_metadata(document)
        outline = self.format_outline(document)
        doc = self.format_doc(metadata, outline)
        self.write(self._serialize(doc), target)

    def format_metadata(self, doc):
        """
        Formats the metadata of given TeX document.
        """
        return None

    def format_outline(self, doc):
        """
        Formats the outline of given TeX document.
        """
        return self.format_outline_element(doc.outline.root)

    def format_outline_element(self, element):
        """
        Formats an element of the outline (OutlineLevel or Block).
        """
        if isinstance(element, OutlineLevel):
            return self.format_outline_level(element)
        elif isinstance(element, LTB) and self.matches_roles_filter(element):
            return self.format_block(element)

    def format_outline_level(self, outline_level):
        """
        Formats an outline level.
        """
        return None

    def format_block(self, outline_level):
        """
        Formats a block.
        """
        return None

    def format_doc(self, metadata, outline):
        """
        Joins the given metadata and outline.
        """
        return None

    def _serialize(self, data):
        """
        Serializes the given data (representing the TeX document).
        """
        return data

    # =========================================================================

    def write(self, string, target=None):
        """
        Writes the given string to given target path. If target is None, the
        string is written to stdout.
        """
        if string is None:
            return

        # Write to stdout if there is no target path given.
        out = open(target, 'w') if target is not None else sys.stdout
        out.write(string)
        if out is not sys.stdout:
            out.close()

    # =========================================================================

    def matches_roles_filter(self, block):
        """
        Returns true if the given block matches the roles filter.
        """
        if len(self.roles_filter) == 0:
            return True
        if block.semantic_role in self.roles_filter:
            return True
        return False


class Settings:
    encoding = "UTF-8"
    indent = 4


class TagNames:
    root = "result"  # Only needed in xml.
    metadata = "metadata"
    file_path = "path"
    outline = "outline"
    typ = "type"  # Only needed in json.
    elements = "elements"  # Only needed in json.
    outline_level = "outline-level"
    block = "block"
    level = "level"
    semantic_role = "role"
    text = "text"  # Only needed in json.
