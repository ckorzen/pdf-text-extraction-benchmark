class TeXElement:
    """
    The base class for any TeX element.
    """
    def __init__(self, document=None, environments=[]):
        self.document = document
        self.environments = environments
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

# =============================================================================


class TeXGroup(TeXElement):
    """
    A class representing a group (elements enclosed in {...}).
    """
    def __init__(self, elements=[], document=None, environments=[]):
        super().__init__(
            document=document,
            environments=environments
        )
        self.elements = elements

    def get_first_element(self):
        return self.get_element(1)

    def get_last_element(self):
        return self.get_element(len(self.elements))

    def get_element(self, num):
        if len(self.elements) <= num:
            return self.elements[num - 1]

    def __str__(self):
        return "{%s}" % "".join([str(x) for x in self.elements])

# =============================================================================
# Commands.


class TeXCommand(TeXElement):
    """
    A class representing a command.
    """
    def __init__(self, cmd_name=None, opts_args=[], document=None,
                 environments=[]):
        super(TeXCommand, self).__init__(
            document=document,
            environments=environments
        )
        self.cmd_name = cmd_name
        self.opts_args = opts_args
        self.opts = [x for x in opts_args if isinstance(x, TeXCommandOption)]
        self.args = [x for x in opts_args if isinstance(x, TeXCommandArgument)]

    def get_arg(self, num):
        if len(self.args) <= num:
            return self.args[num - 1]

    def get_opt(self, num):
        if len(self.opts) <= num:
            return self.opts[num - 1]

    def get_first_arg_text(self):
        """
        Returns the text of first argument of this command.
        For example, returns table for "\\begin{table}".
        """
        first_arg = self.get_arg(1)
        if first_arg is None:
            return
        first_element = first_arg.get_element(1)
        if not isinstance(first_element, TeXText):
            return
        return first_element.text

    def __str__(self):
        opts_args_str = "".join([str(x) for x in self.opts_args])
        return "%s%s" % (self.cmd_name, opts_args_str)

# -----------------------------------------------------------------------------
# Control commands.


class TeXControlCommand(TeXCommand):
    @staticmethod
    def factory(cmd_name=None, opts_args=[], document=None, environments=[]):
        mappings = {
            "\\documentclass": TeXDocumentClassCommand,
            "\\documentstyle": TeXDocumentClassCommand,
            "\\usepackage": TeXUsePackageCommand,
            "\\begin": TeXBeginEnvironmentCommand,
            "\\end": TeXEndEnvironmentCommand
        }

        constructor = mappings.get(cmd_name, TeXControlCommand)
        return constructor(
            cmd_name=cmd_name,
            opts_args=opts_args,
            document=document,
            environments=environments
        )


class TeXDocumentClassCommand(TeXControlCommand):
    """
    A class representing a \\documentclass command.
    """
    def get_document_class(self):
        return self.get_first_arg_text()


class TeXUsePackageCommand(TeXControlCommand):
    """
    A class representing a \\usepackage command.
    """
    pass


class TeXMacroDefinition(TeXControlCommand):
    """
    A class representing a macro definition.
    """
    def __init__(self, cmd_name=None, macro_name=None, replacement=None,
                 document=None, environments=[]):
        super().__init__(
            cmd_name=cmd_name,
            document=document,
            environments=environments
        )
        self.macro_name = macro_name
        self.replacement = replacement

    def __str__(self):
        return "%s%s" % (self.macro_name, self.replacement)


class TeXBeginEnvironmentCommand(TeXControlCommand):
    """
    A class representing a \\begin command.
    """
    def __init__(self, cmd_name=None, opts_args=[], document=None,
                 environments=[]):
        super().__init__(
            cmd_name=cmd_name,
            opts_args=opts_args,
            document=document,
            environments=environments)
        self.end_command = None

    def get_environment(self):
        return self.get_first_arg_text()


class TeXEndEnvironmentCommand(TeXControlCommand):
    """
    A class representing an \\end command.
    """
    def __init__(self, cmd_name=None, opts_args=[], document=None,
                 environments=[]):
        super().__init__(
            cmd_name=cmd_name,
            opts_args=opts_args,
            document=document,
            environments=environments
        )
        self.begin_command = None

    def get_environment(self):
        return self.get_first_arg_text()

# -----------------------------------------------------------------------------
# Break commands.


class TeXBreakCommand(TeXCommand):
    """
    A class representing a command.
    """
    def __init__(self, cmd_name, document=None, environments=[]):
        super().__init__(
            cmd_name=cmd_name,
            document=document,
            environments=environments
        )

    def __str__(self):
        return "//"

# -----------------------------------------------------------------------------
# Args and Opts.


class TeXCommandArgument(TeXGroup):
    """
    A class representing an argument of a group (elements enclosed in {...}).
    """
    def __init__(self, elements=[], document=None, environments=[]):
        super().__init__(
            elements=elements,
            document=document,
            environments=environments
        )

    def __str__(self):
        return "{%s}" % "".join([str(x) for x in self.elements])


class TeXCommandOption(TeXGroup):
    """
    A class representing an option of a group (elements enclosed in [...]).
    """
    def __init__(self, elements=[], document=None, environments=[]):
        super().__init__(
            elements=elements,
            document=document,
            environments=environments
        )

    def __str__(self):
        return "[%s]" % "".join([str(x) for x in self.elements])

# =============================================================================


class TeXMarker(TeXElement):
    """
    A class representing a marker in a macro definition.
    """
    def __init__(self, i, document=None, environments=[]):
        super().__init__(
            document=document,
            environments=environments
        )
        self.i = i

    def __str__(self):
        return "#%s" % self.i

# =============================================================================


class TeXText(TeXElement):
    """
    A class representing some textual content.
    """
    def __init__(self, text, document=None, environments=[]):
        super().__init__(
            document=document,
            environments=environments
        )
        self.text = text

    def __str__(self):
        return self.text

# =============================================================================


class TeXDocument:
    """
    A class representing a whole TeX document.
    """
    def __init__(self, document_class=None, elements=[], macro_definitions={}):
        self.document_class = document_class
        self.elements = elements
        self.macro_definitions = macro_definitions
