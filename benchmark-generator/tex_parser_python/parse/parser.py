import os.path

from copy import deepcopy

from models import tex_elements
from models import doc_elements

from utils import iterators
from utils import file_utils

from tex_tokenizer import TeXTokenParser
from parser_debug import create_debug_string

# Define some default values.
DEFAULT_EXPAND_MACROS = True

# =============================================================================
# Parser.


def parse(path=None, string=None, expand_macros=DEFAULT_EXPAND_MACROS):
    """
    Parses a TeX file, given either by file path or by string, syntactically in
    order to model the hierarchy of its TeX elements.

    Args:
        path (str, optional): The path to the TeX file to process.
        string (str, optional): The content of TeX file to process. Either path
            or string must be set.
        expand_macro_calls (bool, optional): Flag to toggle the expansion of
            macros on parsing the TeX file.
    Returns:
        The parsed TeX document.
    """
    return TeXParser().parse(path, string, expand_macros)


class TeXParser():
    """
    A basic TeX parser based on a EBNF grammar.
    """

    def parse(self, path=None, string=None, expand_macros=True):
        """
        Parses a TeX file, given either by file path or by string,
        syntactically in order to model the hierarchy of the TeX elements.

        Args:
            path (str, optional): The path to the TeX file to process.
            string (str, optional): The content of TeX file to process. Either
                path or string must be set.
            expand_macro_calls (bool, optional): Flag to toggle the expansion
                of macro calls on parsing the TeX file.
        Returns:
            The parsed TeX document.
        """
        if path is not None:
            # A file path is given. Read its content.
            path = os.path.abspath(path)
            if file_utils.is_missing_or_empty_file(path):
                raise ValueError("The input path does not exist / is empty.")
            else:
                string = file_utils.read_file(path)

        # Parse the given string.
        doc = self.parse_string(string)

        if expand_macros:
            # Expand the macros.
            self.expand_macros(doc, doc.macro_definitions)

        # Create a debug string.
        doc.debug_string = create_debug_string(doc)

        # Add some metadata.
        doc.path = path

        return doc

    def parse_string(self, string):
        """
        Parses the content of a TeX file, given as string.

        Args:
            string (str): The string to parse.
        Returns:
            The parsed TeX document.
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

    def expand_macros(self, group, macro_dict):
        """
        Expands all macro calls in the given group based on the given macro
        dictionary.

        Args:
            group (TeXGroup): The group to process.
            macro_dict (dict of str:TeXGroup): The macro dictionary.
        """
        # Iterate through the elements in DFS order.
        dfs_iter = iterators.DFSIterator(group.elements)

        for element in dfs_iter:
            # Ignore all non-commands (only a command can be a macro call).
            if not isinstance(element, tex_elements.TeXCommand):
                continue

            # Check if the command is a macro call.
            if element.cmd_name in macro_dict:
                macro = macro_dict[element.cmd_name]
                # Make a copy of the macro replacement.
                replacement = deepcopy(macro.replacement)
                # Expand the macro call.
                self.expand_macro(element, replacement, macro_dict)

    def expand_macro(self, macro_call, replacement, macro_dict):
        """
        Expands the given macro call by the given replacement recursively.

        Args:
            macro_call (TeXCommand): The macro call to expand.
            replacement (TeXGroup): The replacement to insert on expanding the
                macro call.
            macro_dict (dict of str:TeXGroup): The macro dictionary.
        """
        # Iterate through the elements in replacmenet in DFS order.
        dfs_iter = iterators.DFSIterator(replacement.elements)

        for element in dfs_iter:
            # Replace all markers by related arguments.
            if not isinstance(element, tex_elements.TeXMarker):
                continue
            macro_expansion = macro_call.args[element.i - 1].elements
            element.register_elements_from_macro_expansion(macro_expansion)

        # Register the expanded elements to the macro call.
        macro_call.register_elements_from_macro_expansion(replacement.elements)
        # Expand macro calls recursively.
        self.expand_macros(replacement, macro_dict)

# =============================================================================
# Semantics.


class TeXSemantics(object):
    """
    A class that gives semantic meanings to the production rule of the EBNF
    grammar.
    """

    def __init__(self):
        """
        Creates a new TeXSemantics object.
        """
        # The document to populate.
        self.document = doc_elements.TeXDocument()

    def DOC(self, elements):
        """
        Handles a DOC.

        Args:
            elements (list of TeXElement): The elements of the document.
        """
        # Separate macro definitions from all other elements.
        macro_definitions = {}
        other_elements = []
        for element in elements:
            if isinstance(element, tex_elements.TeXMacroDefinition):
                macro_definitions[element.macro_name] = element
            else:
                other_elements.append(element)
        self.document.macro_definitions = macro_definitions
        self.document.elements = other_elements

    def GROUP(self, data):
        """
        Handles GROUP (elements within {...}).

        Args:
            data (list): The components of the group.
        Returns:
            An instance of TeXGroup.
        """
        return tex_elements.TeXGroup(data[1])

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
        return tex_elements.TeXMacroDefinition(
            cmd_name=cmd_name,
            macro_name=macro_name,
            replacement=replacement
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
        return tex_elements.TeXMacroDefinition(
            cmd_name=cmd_name,
            macro_name=macro_name,
            replacement=replacement
        )

    def CONTROL_CMD(self, data):
        """
        Handles a CONTROL_CMD (a "general" command, for example of form
        "\\name[option]{cmd}").

        Args:
            data (list): The components of the control command.
        Returns:
            An instance of TeXControlCommand.
        """
        cmd_name = "".join([data[0]] + data[1])
        opts_args = data[2]
        cmd = tex_elements.TeXControlCommand.factory(cmd_name, opts_args)
        return cmd

    def SYMBOL_CMD(self, data):
        """
        Handles SYMBOL_CMD (a command that encodes a symbol), for example \\"a.

        Args:
            data (list): The components of the symbol command.
        Returns:
            An instance of TeXCommand.
        """
        cmd_name = "".join([data[0]] + data[1])
        opts_args = [data[2]]
        return tex_elements.TeXCommand(cmd_name, opts_args)

    def ARG(self, data):
        """
        Handles ARG (an argument of a command).

        Args:
            data (list): The components of the argument.
        Returns:
            An instance of TeXCommandArgument.
        """
        return tex_elements.TeXCommandArgument(elements=data[1])

    def OPT(self, data):
        """
        Handles OPT (an option of a command).

        Args:
            data (list): The components of the option.
        Returns:
            An instance of TeXCommandOption.
        """
        return tex_elements.TeXCommandOption(elements=data[1])

    def MARKER(self, data):
        """
        Handles MARKER (a placeholder for argument in a macro definition).

        Args:
            data (list): The components of the marker.
        Returns:
            An instance of TeXMarker.
        """
        return tex_elements.TeXMarker(int(data[1]))

    def NEW_PARAGRAPH(self, data):
        """
        Handles NEW_PARAGRAPH (two or more consecutive line breaks).

        Args:
            data (list): The components of the paragraph break.
        Returns:
            An instance of TeXNewParagraph.
        """
        return tex_elements.TeXNewParagraph("".join([data[0]] + data[1]))

    def NEW_LINE(self, data):
        """
        Handles NEW_LINE (exactly one line break).

        Args:
            data (list): The components of the line break.
        Returns:
            An instance of TeXNewLine.
        """
        return tex_elements.TeXNewLine("".join(data))

    def SPACE(self, data):
        """
        Handles SPACE (one or more whitespaces).

        Args:
            data (list): The components of the whitespace.
        Returns:
            An instance of TeXWhitespace.
        """
        return tex_elements.TeXWhitespace("".join(data))

    def WORD(self, data):
        """
        Handles WORD (a "general" text word without whitespaces).

        Args:
            data (list): The components of the word.
        Returns:
            An instance of TeXWord.
        """
        return tex_elements.TeXWord("".join(data))
