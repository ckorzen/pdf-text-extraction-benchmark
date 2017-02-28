class LTB:
    """
    A logical text block.
    """

    def __init__(self, level=0, semantic_role="body"):
        self.level = level
        self.semantic_role = semantic_role
        self.text = ""

    def __str__(self):
        return "LTB(%s, %s, %s)" % (self.level, self.semantic_role, self.text)

    def __repr__(self):
        return self.__str__()
