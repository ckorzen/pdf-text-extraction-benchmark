from models.ltb import OutlineLevel, LTB


class BaseSerializer:
    """
    The base class for any serializers.
    """

    def __init__(self, roles_filter=[]):
        """
        Creates a new serializer. If the given roles filter is non-empty, only
        the LTBs with matching semantic roles will be serialized.

        Args:
            roles_filter (list of string): The roles of LTBs to serialize.
        """
        self.roles_filter = set(roles_filter)

    def serialize(self, document):
        """
        Serializes the given TeX document. The concrete output format is
        defined by the implementing serializer.

        Args:
            document (TeXDocument): The document to serialize.
        Returns:
            The serialization string.
        """
        metadata = self.format_metadata(document)
        outline = self.format_outline(document)
        doc = self.format_doc(metadata, outline)
        return self._serialize(doc)

    def format_metadata(self, doc):
        """
        Formats the metadata of given TeX document to a serializer-specific
        format (e.g., an ElementTree in case of XML).

        Args:
            doc (TeXDocument): The TeX document to process.
        Returns:
            The metadata in a serializer-specific format.
        """
        return None

    def format_outline(self, doc):
        """
        Formats the outline of LTBs of given TeX document to a
        serializer-specific format (e.g., an ElementTree in case of XML).

        Args:
            doc (TeXDocument): The TeX document to process.
        Returns:
            The outline of LTBs in a serializer-specific format.
        """
        return self.format_outline_element(doc.ltb_outline.root)

    def format_outline_element(self, element):
        """
        Formats an element of the outline (of type OutlineLevel or LTB) to a
        serializer-specific format (e.g., an ElementTree in case of XML).

        Args:
            element (OutlineLevel|LTB): The element to format.
        Returns:
            The outline element in a serializer-specific format.
        """
        if isinstance(element, OutlineLevel):
            return self.format_outline_level(element)
        elif isinstance(element, LTB) and self.matches_roles_filter(element):
            return self.format_block(element)

    def format_outline_level(self, outline_level):
        """
        Formats an outline level to a serializer-specific format (e.g., an
        ElementTree in case of XML).

        Args:
            outline_level (OutlineLevel): The level to format.
        Returns:
            The outline level in a serializer-specific format.
        """
        return None

    def format_block(self, block):
        """
        Formats a LTB to a serializer-specific format (e.g., an ElementTree in
        case of XML).

        Args:
            block (LTB): The logical text block to format.
        Returns:
            The LTB in a serializer-specific format.
        """
        return None

    def format_doc(self, metadata, outline):
        """
        Combines the given metadata and the given outline of LTBs to a
        serializer-specific format (e.g., an ElementTree in case of XML).

        Args:
            metadata: The metadata in a serializer-specific format.
            outline: The outline in a serializer-specific format.
        Returns:
            The combined metadata and outline in a serializer-specific format.
        """
        return None

    def _serialize(self, data):
        """
        Serializes the given data (representing the TeX document in a
        serializer-specific format) to string.

        Args:
            data: The data to serialize.
        Returns:
            The serialized TeX document as string.
        """
        return data

    # =========================================================================
    # Utils.

    def matches_roles_filter(self, block):
        """
        Returns True if the given LTB matches the roles filter of the
        serializer.

        Args:
            block (LTB): The logical text block to process.
        Returns:
            True if the given block matches the roles filter or if the
            roles_filter is empty; False otherwise.
        """
        if len(self.roles_filter) == 0:
            return True
        if block.semantic_role in self.roles_filter:
            return True
        return False


class Settings:
    """
    A class defining some default settings for the serialization.
    """
    # The encoding to use.
    encoding = "UTF-8"
    # The indent to use in XML and JSON.
    indent = 4


class TagNames:
    """
    A class defining some default tag names to use in XML and JSON.
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
