class LTB:
    """
    A class representing a logical text block (LTB).
    """

    def __init__(self, level=0, semantic_role="body"):
        """
        Creates a new logical text block, given by the hierarchy level, the
        semantic role and the textual content.

        Args:
            level (int, optional): The hierarchy level of this block.
            semantic_role (str, optional): The semantic role of this block
        """
        self.level = level
        self.semantic_role = semantic_role
        self.is_whitespace_registered = False
        self.text_fragments = []

    def register_whitespace(self):
        """
        Registers a whitespace to this LTB, which means: keep in mind that a
        whitespace should be appended to this LTB before further text is
        appended to this LTB.
        This method was introduced to avoid series of more than one whitespace
        between words in case of multiple spaces, line breaks or paragraph
        breaks in the TeX file.
        Calling this method more than one time does not have any effect, as it
        will be introduced exactly one whitespace before the next text.
        """
        self.is_whitespace_registered = True

    def append_text(self, text):
        """
        Appends the given text to this LTB. Use this method whenever you want
        to append text to this LTB, as it respects the registered whitespaces.
        Appends a single whitespace before appending the text if one (or more)
        whitespaces are registered to this LTB and there is already some text
        in this LTB (the whitespace is not added if it would be the first
        character in LTB).
        Unregisters the whitespaces from this LTB after the text was appended.

        Args:
            text (str): The text to append.
        """
        if self.is_whitespace_registered and self.has_text():
            self.text_fragments.append(" ")
        self.text_fragments.append(text)
        self.is_whitespace_registered = False

    def get_text(self):
        """
        Returns the text of this LTB.

        Returns:
            The text of this LTB.
        """
        return "".join(self.text_fragments)

    def has_text(self):
        """
        Checks if this LTB contains text.

        Returns:
            True, if this LTB contains text; False otherwise.
        """
        return len(self.text_fragments) > 0

    # Override
    def __str__(self):
        return str(self.get_text())

    # Override
    def __repr__(self):
        return str(self.__dict__)


class LTBOutline:
    """
    A class representing the (hierarchical) outline of LTBs.
    """
    def __init__(self):
        """
        Creates a new outline.
        """
        # The root level (with level 0).
        self.root = LTBOutlineLevel(0)
        # The stack of hierarchy levels.
        self.stack = [self.root]

    def append(self, ltb):
        """
        Appends the given LTB to this outline.

        Args:
            ltb (LTB): The logical text block to append.
        """
        # Append as many hierarchy levels as needed until the hierarchy
        # level of the outline matches the hierarchy of the LTB.
        while self.stack[-1].level < ltb.level:
            outline_level = LTBOutlineLevel(self.stack[-1].level + 1)
            self.stack[-1].elements.append(outline_level)
            self.stack.append(outline_level)
        # Pop as many hierarchy levels as needed until the hierarchy
        # level of the outline matches the hierarchy of the LTB.
        while self.stack[-1].level > ltb.level:
            self.stack.pop()
        # Append the LTB to the elements of current hierarchy level.
        self.stack[-1].elements.append(ltb)

    # Override
    def __str__(self):
        return str(self.root)


class LTBOutlineLevel:
    """
    A class representing a hierarchy level in the outline of LTBs.
    """
    def __init__(self, level):
        """
        Creates a new outline level.

        Args:
            level (int): The level of the hierarchy level.
        """
        # The level.
        self.level = level
        # The elements of this level, either of type LTB or LTBOutlineLevel.
        self.elements = []

    # Override
    def __str__(self):
        parts = []
        for element in self.elements:
            parts.append(str(element))
        return " ".join(parts)
