import env  # NOQA

from models import tex_models
from utils import file_utils
from tex_tokenizer import TeXTokenParser
from parser_expand_macros import expand_macros
from parser_debug import create_debug_string

DEFAULT_EXPAND_MACROS = True

# =============================================================================
# Parser.


def parse(path=None, string=None, expand_macro_calls=DEFAULT_EXPAND_MACROS):
    """
    Parses a TeX file, given either by file path or by string, syntactically in
    order to model the hierarchy of the TeX elements.

    Args:
        path (str, optional): The path to the TeX file to process.
        string (str, optional): The content of TeX file to process. Either path
            or string must be set.
        expand_macro_calls (bool, optional): Flag to toggle the expansion of
            macro calls on parsing the TeX file.
    """
    return TeXParser().parse(path, string, expand_macro_calls)


class TeXParser():
    """
    A TeX parser based on a EBNF grammar.
    """

    def parse(self, path=None, string=None, expand_macro_calls=True):
        """
        Parses a TeX file, given either by file path or by string,
        syntactically in order to model the hierarchy of the TeX elements.

        Args:
            path (str, optional): The path to the TeX file to process.
            string (str, optional): The content of TeX file to process. Either
                path or string must be set.
            expand_macro_calls (bool, optional): Flag to toggle the expansion
                of macro calls on parsing the TeX file.
        """
        if path is not None:
            # A file path is given. Read its content.
            if file_utils.is_missing_or_empty_file(path):
                raise ValueError("The input path does not exist / is empty.")
            else:
                string = file_utils.read_file(path)

        # Parse the given string.
        doc = self.parse_string(string)

        if expand_macro_calls:
            # Expand the macros.
            expand_macros(doc, doc.macro_definitions)

        # Create a debug string.
        doc.debug_string = create_debug_string(doc)

        return doc

    def parse_string(self, string):
        """
        Parses the given TeX file, given as string.

        Args:
            string (str): The string to parse.
        """
        if string is None:
            # No string to parse given. Abort.
            raise ValueError("There is no input to parse given.")

        # Parse the string.
        # Set parseinfo to False to discard parse infos from AST.
        # Set whitespace to '' such that whitespaces are not ignored.
        tex_tokenizer = TeXTokenParser(parseinfo=False, whitespace='')
        tex_semantics = TeXSemantics()
        tex_tokenizer.parse(string, semantics=tex_semantics)
        return tex_semantics.document

# =============================================================================
# Semantics.


class TeXSemantics(object):
    """
    A class that gives semantic meanings to the production rule of the EBNF
    grammar.
    """

    def __init__(self):
        """
        Creates a new semantics object.
        """
        # The document to populate.
        self.document = tex_models.TeXDocument()
        # The current stack of \\begin{...} commands, needed to obtain the
        # environment stack of each element and to identify the related
        # \\end{...} commands.
        self.begin_environment_cmds = []

    def get_environments_stack(self):
        """
        Returns the current environment stack.
        For example, if the current element lives within

        \\begin{document}
          \\begin{table}
            ...
          \\end{table}
        \\end{document}

        the environment stack for this element is [document, table].

        Returns:
            The current environment stack.
        """
        # Obtain the environment stack from stack of \\begin{...} commands.
        return [x.get_environment() for x in self.begin_environment_cmds[::-1]]

    def DOC(self, elements):
        """
        Handles a DOC.

        Args:
            elements (list of TeXElement): The elements of the document.
        """
        # Separate macro definitions from all other elements.
        tex_elements = []
        macro_definitions = {}
        for element in elements:
            if isinstance(element, tex_models.TeXMacroDefinition):
                macro_definitions[element.macro_name] = element
            else:
                tex_elements.append(element)
        self.document.elements = tex_elements
        self.document.macro_definitions = macro_definitions

    def GROUP(self, data):
        """
        Handles GROUP (elements within {...}).

        Args:
            data (list): The components of the group.
        Returns:
            An instance of TeXGroup.
        """
        return tex_models.TeXGroup(
            elements=data[1],
            document=self.document,
            environments=self.get_environments_stack()
        )

    def TEX_MACRO_DEF_CMD(self, data):
        """
        Handles TEX_MACRO_DEF_CMD (a \\def\\foobar... command).

        Args:
            data (list): The components of the macro definition.
        Returns:
            An instance of TeXMacroDefinition.
        """
        cmd_name = data[0]
        macro_name = "".join([data[1]] + data[2])
        replacement = data[4]
        return tex_models.TeXMacroDefinition(
            cmd_name=cmd_name,
            macro_name=macro_name,
            replacement=replacement,
            document=self.document,
            environments=self.get_environments_stack()
        )

    def LATEX_MACRO_DEF_CMD(self, data):
        """
        Handles LATEX_MACRO_DEF_CMD (a \\newcommand{\\foobar}... command).

        Args:
            data (list): The components of the macro definition.
        Returns:
            An instance of TeXMacroDefinition.
        """
        cmd_name = data[0]
        macro_name = data[1].elements[0].cmd_name
        replacement = data[-1]
        return tex_models.TeXMacroDefinition(
            cmd_name=cmd_name,
            macro_name=macro_name,
            replacement=replacement,
            document=self.document,
            environments=self.get_environments_stack()
        )

    def CONTROL_CMD(self, data):
        """
        Handles a CONTROL_CMD (a "general" command).

        Args:
            data (list): The components of the control command.
        Returns:
            An instance of TeXControlCommand.
        """
        cmd_name = "".join([data[0]] + data[1])
        opts_args = data[2]
        cmd = tex_models.TeXControlCommand.factory(
            cmd_name=cmd_name,
            opts_args=opts_args,
            document=self.document,
            environments=self.get_environments_stack()
        )

        if isinstance(cmd, tex_models.TeXBeginEnvironmentCommand):
            # Append the environment to stack.
            self.begin_environment_cmds.append(cmd)
        if isinstance(cmd, tex_models.TeXEndEnvironmentCommand):
            # Pop the environment from stack.
            begin_environment_cmd = self.begin_environment_cmds.pop()
            # Associate related \\begin{...} and \\end{...} commands.
            begin_environment_cmd.end_command = cmd
            cmd.begin_command = begin_environment_cmd
        return cmd

    def SYMBOL_CMD(self, data):
        """
        Handles SYMBOL_CMD (a command that encodes a symbol).

        Args:
            data (list): The components of the symbol command.
        Returns:
            An instance of TeXCommand.
        """
        cmd_name = "".join([data[0]] + data[1])
        opts_args = [data[2]]
        return tex_models.TeXCommand(
            cmd_name=cmd_name,
            opts_args=opts_args,
            document=self.document,
            environments=self.get_environments_stack()
        )

    def ARG(self, data):
        """
        Handles ARG (an argument of a command).

        Args:
            data (list): The components of the argument.
        Returns:
            An instance of TeXText.
        """
        return tex_models.TeXCommandArgument(
            elements=data[1],
            document=self.document,
            environments=self.get_environments_stack()
        )

    def OPT(self, data):
        """
        Handles OPT (an option of a command).

        Args:
            data (list): The components of the option.
        Returns:
            An instance of TeXCommandOption.
        """
        return tex_models.TeXCommandOption(
            elements=data[1],
            document=self.document,
            environments=self.get_environments_stack()
        )

    def MARKER(self, data):
        """
        Handles MARKER (a placeholder for argument in a macro definition).

        Args:
            data (list): The components of the marker.
        Returns:
            An instance of TeXMarker.
        """
        return tex_models.TeXMarker(
            int(data[1]),
            document=self.document,
            environments=self.get_environments_stack()
        )

    def NEW_PARAGRAPH(self, data):
        """
        Handles NEW_PARAGRAPH (two or more consecutive line breaks).

        Args:
            data (list): The components of the paragraph break.
        Returns:
            An instance of TeXNewParagraph.
        """
        return tex_models.TeXNewParagraph(
            text="".join([data[0]] + data[1]),
            document=self.document,
            environments=self.get_environments_stack()
        )

    def NEW_LINE(self, data):
        """
        Handles NEW_LINE (exactly one line break).

        Args:
            data (list): The components of the line break.
        Returns:
            An instance of TeXNewLine.
        """
        return tex_models.TeXNewLine(
            text="".join(data),
            document=self.document,
            environments=self.get_environments_stack()
        )

    def SPACE(self, data):
        """
        Handles SPACE (one or more whitespaces).

        Args:
            data (list): The components of the whitespace.
        Returns:
            An instance of TeXWhitespace.
        """
        return tex_models.TeXWhitespace(
            text="".join(data),
            document=self.document,
            environments=self.get_environments_stack()
        )

    def WORD(self, data):
        """
        Handles WORD (a "normal" word without whitespaces).

        Args:
            data (list): The components of the word.
        Returns:
            An instance of TeXWord.
        """
        return tex_models.TeXWord(
            "".join(data),
            document=self.document,
            environments=self.get_environments_stack()
        )
