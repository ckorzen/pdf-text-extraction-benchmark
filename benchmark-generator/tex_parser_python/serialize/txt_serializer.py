from base_serializer import BaseSerializer


class TxtSerializer(BaseSerializer):
    """
    A TXT serializer.
    """

    # Override
    def format_doc(self, metadata, outline):
        return outline

    # Override
    def format_outline_level(self, level):
        parts = []
        for element in level.elements:
            outline_element = self.format_outline_element(element)
            if outline_element is not None:
                parts.extend(outline_element)
        return parts if len(parts) > 0 else None

    # Override
    def format_block(self, block):
        # Return list here such that we can simply use extend() in
        # format_outline_level() above.
        return [block.get_text()]

    # Override
    def _serialize(self, data):
        """
        Serializes the given data (representing the TeX document).
        """
        if data is None:
            return ""
        return "\n\n".join(data) + "\n"
