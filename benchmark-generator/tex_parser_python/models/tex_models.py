class TeXElement:
    """
    The base class for any TeX element.
    """
    def __init__(self):
        """
        Creates a new TeX element.
        """
        # The elements, that resulted from a macro expansion and replace this
        # element.
        self.elements_from_macro_expansion = []

    def register_elements_from_macro_expansion(self, elements):
        """
        Registers the given elements that resulted from a macro expansion and
        replace this element.

        Args:
            elements (list of TeXElement): The elements that resulted from
                macro expansion.
        """
        self.elements_from_macro_expansion.extend(elements)

    def has_elements_from_macro_expansion(self):
        """
        Returns True if this element contains elements resulted from macro
        expansion.
        """
        return len(self.elements_from_macro_expansion) > 0

    def get_elements_from_macro_expansion(self):
        """
        Returns the elements that resulted from macro expansion and replace
        this element.
        """
        if not self.has_elements_from_macro_expansion():
            return []

        def _get_elements_from_macro_expansion(element):
            """
            Obtains the elements that resulted from macro expansion for the
            given element recursively.

            Args:
                element (TeXElement): The element to analyze.
            Returns:
                The elements that replace the given element "after expanding
                macros".
            """
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
    A class representing a TeX group (elements enclosed in {...}).
    """
    def __init__(self, elements=[]):
        """
        Creates a new group containing the given elements.

        Args:
            elements (list of TeXElement, optional): The elements of the group.
        """
        super().__init__()
        self.elements = elements

    def get_first_element(self):
        """
        Returns the first element of this group.

        Returns:
            The first element of this group.
        """
        return self.get_element(1)

    def get_last_element(self):
        """
        Returns the last element of this group.

        Returns:
            The last element of this group.
        """
        return self.get_element(len(self.elements))

    def get_element(self, num):
        """
        Returns the <num>-th element of this group.

        Args:
            num (int): The 1-based index of element to get.
        Returns:
            The <num>-th element in this group.
        """
        if len(self.elements) <= num:
            return self.elements[num - 1]

    # Override
    def __str__(self):
        return "{%s}" % "".join([str(x) for x in self.elements])

# =============================================================================
# Commands.


class TeXCommand(TeXElement):
    """
    The base class for each TeX command.
    """
    def __init__(self, cmd_name=None, opts_args=[]):
        """
        Creates a new TeX command.

        Args:
            cmd_name (str): The name of the command.
            opts_args (list of TeXCommandArgument|TeXCommandOption):
                The list of options and argument of this command (in order as
                they appeared in the TeX file).
        """
        super().__init__()
        self.cmd_name = cmd_name
        self.opts_args = opts_args
        # Create a list with only the options.
        self.opts = [x for x in opts_args if isinstance(x, TeXCommandOption)]
        # Create a list with only the arguments.
        self.args = [x for x in opts_args if isinstance(x, TeXCommandArgument)]

    def get_arg(self, num):
        """
        Returns the <num>-th argument of this command.

        Args:
            num (int): The 1-based index of argument to get.
        Returns:
            The <num>-th argument of this command.
        """
        if len(self.args) <= num:
            return self.args[num - 1]

    def get_opt(self, num):
        """
        Returns the <num>-th option of this command.

        Args:
            num (int): The 1-based index of option to get.
        Returns:
            The <num>-th option in this command.
        """
        if len(self.opts) <= num:
            return self.opts[num - 1]

    def get_first_arg_text(self):
        """
        Returns the text of the first argument of this command.
        For example, for command "\\begin{table}", this method returns "table".

        Returns:
            The text of first argument of this command.
        """
        first_arg = self.get_arg(1)
        if first_arg is None:
            return
        first_element = first_arg.get_element(1)
        if not isinstance(first_element, TeXWord):
            return
        return first_element.text

    def get_identifier(self):
        """
        Returns a general, but unique identifier for this command that can be
        used in rules to refer to this commands.

        For example, for command \\footnote{...}, this method should return
        "\\footnote" (because it is unique enough to distinguish it from other
        command and adding the argument(s) would result in variable
        identifiers).
        For command \\begin{Introduction}, the method should return
        "\\begin{Introduction}" (because "\\begin" is not unique enough,
        because there are other \\begin{...} commands with different meanings."

        Returns:
            A unique identifier for this command.
        """
        return self.cmd_name

    # Override
    def __str__(self):
        opts_args_str = "".join([str(x) for x in self.opts_args])
        return "%s%s" % (self.cmd_name, opts_args_str)

# -----------------------------------------------------------------------------
# Control commands.


class TeXControlCommand(TeXCommand):
    """
    A class representing a TeX control command, that is a command of form e.g.
    \\foobar[opt]{arg}.
    """

    @staticmethod
    def factory(cmd_name=None, opts_args=[]):
        """
        A factory that chooses the correct control command constructor for the
        given input.

        Args:
            cmd_name (str): The name of the command.
            opts_args (list of TeXCommandArgument|TeXCommandOption):
                The list of options and argument of this command.
        Returns:
            An instance of a (specific) control command.
        """

        # Map some specific command names to their specific subclasses.
        mappings = {
            "\\begin": TeXBeginEnvironmentCommand,
            "\\end": TeXEndEnvironmentCommand
        }

        # Choose the correct subclass.
        constructor = mappings.get(cmd_name, TeXControlCommand)
        return constructor(
            cmd_name=cmd_name,
            opts_args=opts_args
        )


class TeXBeginEnvironmentCommand(TeXControlCommand):
    """
    A class representing a \\begin{...} command.
    """
    def __init__(self, cmd_name=None, opts_args=[]):
        """
        Creates a new TeXBeginEnvironmentCommand.

         Args:
            cmd_name (str): The name of the command.
            opts_args (list of TeXCommandArgument|TeXCommandOption):
                The list of options and argument of this command.
        """
        super().__init__(cmd_name, opts_args)
        self.end_command = None

    def get_environment(self):
        """
        Returns the name of the environment introduced by this command.

        Returns:
            The name of the environment introduced by this command.
        """
        return self.get_first_arg_text()

    # Override
    def get_identifier(self):
        return self.cmd_name + str(self.get_arg(1))


class TeXEndEnvironmentCommand(TeXControlCommand):
    """
    A class representing an \\end{...} command.
    """
    def __init__(self, cmd_name=None, opts_args=[]):
        """
        Creates a new TeXEndEnvironmentCommand.

        Args:
            cmd_name (str): The name of the command.
            opts_args (list of TeXCommandArgument|TeXCommandOption):
                The list of options and argument of this command.
        """
        super().__init__(cmd_name, opts_args)
        self.begin_command = None

    def get_environment(self):
        """
        Returns the name of the environment finished by this command.

        Returns:
            The name of the environment finished by this command.
        """
        return self.get_first_arg_text()

    # Override
    def get_identifier(self):
        return self.cmd_name + str(self.get_arg(1))


class TeXMacroDefinition(TeXControlCommand):
    """
    A class representing a macro definition, like \\def\\foobar... or
    \\newcommand{\\foobar}...
    """
    def __init__(self, cmd_name=None, macro_name=None, replacement=None):
        """
        Creates a new macro definition.

        Args:
            cmd_name (str): The name of the command.
            macro_name (str): The name of the macro.
            replacement (TeXGroup): The replacement of the macro.
        """
        super().__init__(cmd_name)
        self.macro_name = macro_name
        self.replacement = replacement

    # Override
    def __str__(self):
        return "%s%s" % (self.macro_name, self.replacement)

# -----------------------------------------------------------------------------
# Args and Opts.


class TeXCommandArgument(TeXGroup):
    """
    A class representing an argument of a command (elements enclosed in {...}).
    """
    # Override
    def __str__(self):
        return "{%s}" % "".join([str(x) for x in self.elements])


class TeXCommandOption(TeXGroup):
    """
    A class representing an option of a command (elements enclosed in [...]).
    """
    # Override
    def __str__(self):
        return "[%s]" % "".join([str(x) for x in self.elements])

# =============================================================================


class TeXMarker(TeXElement):
    """
    A class representing a marker, that is a placeholder for an argument in a
    macro definition.
    """
    def __init__(self, i):
        """
        Creates a new TeXMarker.

        Args:
            i (int): An index pointing to the i-th argument of the macro call.
        """
        super().__init__()
        self.i = i

    # Override
    def __str__(self):
        return "#%s" % self.i

# =============================================================================


class TeXNewParagraph(TeXCommand):
    """
    A class representing a paragraph break, that are two or more line breaks.
    """
    def __init__(self, text):
        """
        Creates a new TeXNewParagraph.

        Args:
            text (str): The text that caused this paragraph break.
        """
        super().__init__()
        self.text = text

    # Override
    def __str__(self):
        return self.text

    # Override
    def get_identifier(self):
        # As a paragraph break isn't a TeX command in the proper sense, return
        # a generic identifier.
        return "(paragraph_break)"


class TeXNewLine(TeXCommand):
    """
    A class representing a line break, that is exactly one line break.
    """
    def __init__(self, text):
        """
        Creates a new TeXNewLine.

        Args:
            text (str): The text that caused this line break.
        """
        super().__init__()
        self.text = text

    # Override
    def __str__(self):
        return self.text

    # Override
    def get_identifier(self):
        # As a line break isn't a TeX command in the proper sense, return
        # a generic identifier.
        return "(line_break)"


class TeXWhitespace(TeXCommand):
    """
    A class representing a whitespace, that are one or more consecutive spaces.
    """
    def __init__(self, text):
        """
        Creates a new TeXWhitespace.

        Args:
            text (str): The text that caused this whitespace.
        """
        super().__init__()
        self.text = text

    # Override
    def __str__(self):
        return self.text

    # Override
    def get_identifier(self):
        # As a whitespace isn't a TeX command in the proper sense, return
        # a generic identifier.
        return "(space)"


class TeXWord(TeXElement):
    """
    A class representing a text word in a TeX document.
    """
    def __init__(self, text):
        """
        Creates a new TeXWord.

        Args:
            text (str): The textual content of the word (without whitespaces).
        """
        super().__init__()
        self.text = text

    # Override
    def __str__(self):
        return self.text

# =============================================================================


class TeXDocument:
    """
    A class representing a TeX document.
    """
    def __init__(self, document_class=None, elements=[], macro_definitions={}):
        """
        Creates a new TeX document.

        Args:
            document_class (str): The document class of this TeX document.
            elements (list of TeXElement, optional): The elements of this
                document.
            macro_definitions (dict of str:TeXGroup, optional): The dictionary
                of macro definitions, mapping the name of macros to their
                replacements.
        """
        self.metadata = {}
        self.document_class = document_class
        self.elements = elements
        self.macro_definitions = macro_definitions
