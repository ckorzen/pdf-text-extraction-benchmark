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
    A class that gives semantic meanings to the production rule in EBNF
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
        """
        # Obtain the environment stack from stack of \\begin{...} commands.
        return [x.get_environment() for x in self.begin_environment_cmds[::-1]]

    def DOC(self, elements):
        """
        Handles a DOC.
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

    def GROUP(self, ast):
        """
        Handles a GROUP.
        """
        return tex_models.TeXGroup(
            elements=ast[1],
            document=self.document,
            environments=self.get_environments_stack()
        )

    def TEX_MACRO_DEF_CMD(self, ast):
        """
        Handles a TEX_MACRO_DEF_CMD (a \\def\\foobar... command).
        """
        cmd_name = ast[0]
        macro_name = "".join([ast[1]] + ast[2])
        replacement = ast[4]
        return tex_models.TeXMacroDefinition(
            cmd_name=cmd_name,
            macro_name=macro_name,
            replacement=replacement,
            document=self.document,
            environments=self.get_environments_stack()
        )

    def LATEX_MACRO_DEF_CMD(self, ast):
        """
        Handles a LATEX_MACRO_DEF_CMD (a \\newcommand{\\foobar}... command).
        """
        cmd_name = ast[0]
        macro_name = ast[1].elements[0].cmd_name
        replacement = ast[-1]
        return tex_models.TeXMacroDefinition(
            cmd_name=cmd_name,
            macro_name=macro_name,
            replacement=replacement,
            document=self.document,
            environments=self.get_environments_stack()
        )

    def BREAK_CMD(self, ast):
        """
        Handles a BREAK_CMD (a command that represents one or more linebreaks).
        """
        return tex_models.TeXBreakCommand(
            cmd_name=ast[0],
            document=self.document,
            environments=self.get_environments_stack()
        )

    def CONTROL_CMD(self, ast):
        """
        Handles a CONTROL_CMD (a "general" command).
        """
        cmd_name = "".join([ast[0]] + ast[1])
        opts_args = ast[2]
        cmd = tex_models.TeXControlCommand.factory(
            cmd_name=cmd_name,
            opts_args=opts_args,
            document=self.document,
            environments=self.get_environments_stack()
        )

        if isinstance(cmd, tex_models.TeXDocumentClassCommand):
            # Obtain the document clas..
            self.document.document_class = cmd.get_document_class()
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

    def SYMBOL_CMD(self, ast):
        """
        Handles a SYMBOL_CMD (a command that represents a symbol).
        """
        cmd_name = "".join([ast[0]] + ast[1])
        opts_args = [ast[2]]
        return tex_models.TeXCommand(
            cmd_name=cmd_name,
            opts_args=opts_args,
            document=self.document,
            environments=self.get_environments_stack()
        )

    def ARG(self, ast):
        """
        Handles an ARG (an argument of a command).
        """
        return tex_models.TeXCommandArgument(
            elements=ast[1],
            document=self.document,
            environments=self.get_environments_stack()
        )

    def OPT(self, ast):
        """
        Handles an OPT (an option of a command).
        """
        return tex_models.TeXCommandOption(
            elements=ast[1],
            document=self.document,
            environments=self.get_environments_stack()
        )

    def MARKER(self, ast):
        """
        Handles an MARKER (a placeholder for argument in a macro definition).
        """
        return tex_models.TeXMarker(
            int(ast[1]),
            document=self.document,
            environments=self.get_environments_stack()
        )

    def TEXT(self, ast):
        """
        Handles a TEXT.
        """
        return tex_models.TeXText(
            "".join(ast),
            document=self.document,
            environments=self.get_environments_stack()
        )
