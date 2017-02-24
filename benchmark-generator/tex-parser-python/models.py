class TeXElement:
    """
    The base class for any TeX element.
    """
    def __init__(self):
        pass

    def __str__(self):
        return ""

    def __repr__(self):
        return self.__str__()

    def debug_str(self):
        return self.__str__()


class TeXGroup(TeXElement):
    """
    A class representing a group (elements enclosed in {...}).
    """
    def __init__(self, elements):
        super(TeXGroup, self).__init__()
        self.elements = elements

    def __str__(self):
        return "{%s}" % "".join([str(x) for x in self.elements])


class TeXCommand(TeXElement):
    """
    A class representing a command.
    """
    def __init__(self, command_name, opts_args=[]):
        super(TeXCommand, self).__init__()
        self.command_name = command_name
        self.opts_and_args = opts_args
        self.opts = [x for x in opts_args if isinstance(x, TeXCommandOption)]
        self.args = [x for x in opts_args if isinstance(x, TeXCommandArgument)]

    def __str__(self):
        opts_and_args_str = "".join([str(x) for x in self.opts_and_args])
        return "%s%s" % (self.command_name, opts_and_args_str)


class TeXMacroDefinition(TeXCommand):
    """
    A class representing a macro definition.
    """
    def __init__(self, command_name, replacement=None):
        super(TeXMacroDefinition, self).__init__(command_name)
        self.replacement = replacement

    def __str__(self):
        return "%s%s" % (self.command_name, self.replacement)


class TeXCommandArgument(TeXGroup):
    """
    A class representing an argument of a group (elements enclosed in {...}).
    """
    def __str__(self):
        return "{%s}" % "".join([str(x) for x in self.elements])


class TeXCommandOption(TeXGroup):
    """
    A class representing an option of a group (elements enclosed in [...]).
    """

    def __str__(self):
        return "[%s]" % "".join([str(x) for x in self.elements])


class TeXMarker(TeXElement):
    """
    A class representing a marker in a macro definition.
    """
    def __init__(self, i):
        super(TeXMarker, self).__init__()
        self.i = i

    def __str__(self):
        return "#%s" % self.i


class TeXText(TeXElement):
    """
    A class representing some textual content.
    """
    def __init__(self, text):
        super(TeXText, self).__init__()
        self.text = text

    def __str__(self):
        return self.text


class TeXDocument:
    """
    A class representing a whole TeX document.
    """
    def __init__(self, elements=[], macro_definitions={}):
        self.elements = elements
        self.macro_definitions = macro_definitions
