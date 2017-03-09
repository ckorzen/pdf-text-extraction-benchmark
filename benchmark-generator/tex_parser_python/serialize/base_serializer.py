import sys

from models.ltb import OutlineLevel, LTB


class BaseSerializer:
    """
    The super class for any serializers.
    """

    def __init__(self, roles_filter=[]):
        """
        Creates a new serializer.

        Args:
            roles_filter (list of string): The roles of LTBs to serialize.
        """
        self.roles_filter = set(roles_filter)

    def serialize(self, document, target=None):
        """
        Serializes the given TeX document to given target. If target is None,
        the serialization is printed to stdout.

        Args:
            document (TeXDocument): The document to serialize.
            target (str, optional): The path to target file of serialization.
                If None, the serialization will be printed to stdout.
        Returns:
            The serilaization string.
        """
        metadata = self.format_metadata(document)
        outline = self.format_outline(document)
        doc = self.format_doc(metadata, outline)
        string = self._serialize(doc)
        self.write(string, target)

    def format_metadata(self, doc):
        """
        Formats the metadata of given TeX document.

        Args:
            doc (TeXDocument): The TeX document to process.
        Returns:
            The metadata in a serialized form.
        """
        return None

    def format_outline(self, doc):
        """
        Formats the outline of given TeX document.

        Args:
            doc (TeXDocument): The TeX document to process.
        Returns:
            The outline in a serialized form.
        """
        return self.format_outline_element(doc.outline.root)

    def format_outline_element(self, element):
        """
        Formats an element of the outline (of type OutlineLevel or LTB).

        Args:
            element (OutlineLevel|LTB): The element to format.
        Returns:
            The outline element in a serialized form.
        """
        if isinstance(element, OutlineLevel):
            return self.format_outline_level(element)
        elif isinstance(element, LTB) and self.matches_roles_filter(element):
            return self.format_block(element)

    def format_outline_level(self, outline_level):
        """
        Formats an outline level.

        Args:
            outline_level (OutlineLevel): The level to format.
        Returns:
            The outline level in a serialized form.
        """
        return None

    def format_block(self, block):
        """
        Formats a logical text block.

        Args:
            block (LTB): The logical text block to format.
        Returns:
            The logical text block in a serialized form.
        """
        return None

    def format_doc(self, metadata, outline):
        """
        Joins the given metadata and outline.

        Args:
            metadata: The metadata in a serialized form.
            outline: The outline in a serialized form.
        Returns:
            The metadata and the outline in a joined form.
        """
        return None

    def _serialize(self, data):
        """
        Serializes the given data (representing the TeX document).

        Args:
            data: The data to serialize.
        Returns:
            The serialized TeX document as string.
        """
        return data

    # =========================================================================

    def write(self, string, target=None):
        """
        Writes the given string to given target path. If target is None, the
        string is written to stdout.

        Args:
            string (str): The string to write.
            target (str, optional): The path to the target path. If None, the
                serilaization will be printed to stdout.
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
        Returns True if the given block matches the roles filter.

        Args:
            block (LTB): The logical text block to process.
        Returns:
            True if the given block matches the roles filter; False otherwise.
        """
        if len(self.roles_filter) == 0:
            return True
        if block.semantic_role in self.roles_filter:
            return True
        return False


class Settings:
    """
    A class defining some settings of the serialization.
    """
    # The encoding to use.
    encoding = "UTF-8"
    # The indent to use in XML and JSON.
    indent = 4


class TagNames:
    """
    A class defining some tag names to use in XML and JSON.
    """
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
