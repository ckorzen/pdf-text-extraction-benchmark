class LTB:
    """
    A logical text block.
    """

    def __init__(self, level=0, semantic_role="body"):
        self.level = level
        self.semantic_role = semantic_role
        self.text = ""

    def __str__(self):
        return str(self.__dict__)

    def __repr__(self):
        return self.__str__()


class Outline:
    def __init__(self):
        self.root = OutlineLevel(0)
        self.stack = [self.root]

    def append(self, ltb):
        while self.stack[-1].level < ltb.level:
            outline_level = OutlineLevel(self.stack[-1].level + 1)
            self.stack[-1].elements.append(outline_level)
            self.stack.append(outline_level)
        while self.stack[-1].level > ltb.level:
            self.stack.pop()
        self.stack[-1].elements.append(ltb)


class OutlineLevel:
    """
    An outline of logical text blocks.
    """

    def __init__(self, level):
        self.level = level
        self.elements = []
