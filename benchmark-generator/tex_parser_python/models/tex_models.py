class TeXElement:
    """
    The base class for any TeX element.
    """
    def __init__(self):
        self.elements_from_macro_expansion = []

    def register_elements_from_macro_expansion(self, elements):
        """
        Registers the given elements that resulted from macro expansion.
        """
        self.elements_from_macro_expansion.extend(elements)

    def has_elements_from_macro_expansion(self):
        """
        Returns True if this element has elements from macro expansion.
        """
        return len(self.elements_from_macro_expansion) > 0

    def get_elements_from_macro_expansion(self):
        """
        Returns the elements that results from expanding a macro call.
        """
        if not self.has_elements_from_macro_expansion():
            return []

        def _get_elements_from_macro_expansion(element):
            # Search for nested calls.
            result = []
            if not element.has_elements_from_macro_expansion():
                result.append(element)
            else:
                for el in element.elements_from_macro_expansion:
                    result.extend(_get_elements_from_macro_expansion(el))
            return result

        return _get_elements_from_macro_expansion(self)


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


class TeXBreakCommand(TeXCommand):
    """
    A class representing a command.
    """
    def __init__(self, command_name):
        super(TeXBreakCommand, self).__init__(command_name)

    def __str__(self):
        return "//"


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
    def __init__(self, elements):
        super(TeXCommandArgument, self).__init__(elements)

    def __str__(self):
        return "{%s}" % "".join([str(x) for x in self.elements])


class TeXCommandOption(TeXGroup):
    """
    A class representing an option of a group (elements enclosed in [...]).
    """
    def __init__(self, elements):
        super(TeXCommandOption, self).__init__(elements)

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
