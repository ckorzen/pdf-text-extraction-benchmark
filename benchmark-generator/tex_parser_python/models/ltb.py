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
        self.text = ""

    def __str__(self):
        return str(self.__dict__)

    def __repr__(self):
        return self.__str__()


class Outline:
    """
    A class representing the (hierarchical) outline of LTBs.
    """
    def __init__(self):
        """
        Creates a new outline.
        """
        # The root level.
        self.root = OutlineLevel(0)
        # The stack of hierarchy levels.
        self.stack = [self.root]

    def append(self, ltb):
        """
        Appends the given LTB to this outline.

        Args:
            ltb (LTB): The logical text block to append.
        """
        while self.stack[-1].level < ltb.level:
            # Append as many hierarchy levels as needed until the hierarchy
            # level of the outline matches the hierarchy of the LTB.
            outline_level = OutlineLevel(self.stack[-1].level + 1)
            self.stack[-1].elements.append(outline_level)
            self.stack.append(outline_level)
        while self.stack[-1].level > ltb.level:
            # Pop as many hierarchy levels as needed until the hierarchy
            # level of the outline matches the hierarchy of the LTB.
            self.stack.pop()
        # Append the LTB to the elements of current hierarchy level.
        self.stack[-1].elements.append(ltb)


class OutlineLevel:
    """
    A class representing a hierarchy level in the outline of LTBs.
    """
    def __init__(self, level):
        """
        Creates a new hierarchy level.

        Args:
            level (int): The level.
        """
        # The level.
        self.level = level
        # The elements of this level, either of type LTB or OutlineLevel.
        self.elements = []
