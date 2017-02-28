import sys
import models
import tex_parser_expand_macros
import file_utils

from tex_tokenizer import TeXTokenParser

# =============================================================================
# Semantics.


class TeXSemantics(object):
    """
    A class that defines some semantics for the production rule in grammar.
    """
    def start(self, ast):
        self.document = ast

    def DOC(self, elements):
        # Filter out macro definitions.
        tex_elements = []
        macro_definitions = {}
        for element in elements:
            if isinstance(element, models.TeXMacroDefinition):
                macro_definitions[element.command_name] = element
            else:
                tex_elements.append(element)
        return models.TeXDocument(tex_elements, macro_definitions)

    def GROUP(self, ast):
        return models.TeXGroup(ast[1])

    def TEX_MACRO_DEF_CMD(self, ast):
        macro_name = "".join([ast[1]] + ast[2])
        replacement = ast[4]
        return models.TeXMacroDefinition(macro_name, replacement)

    def LATEX_MACRO_DEF_CMD(self, ast):
        macro_name = ast[1].elements[0].command_name
        replacement = ast[-1]
        return models.TeXMacroDefinition(macro_name, replacement)

    def BREAK_CMD(self, ast):
        return models.TeXBreakCommand(ast[0])

    def CONTROL_CMD(self, ast):
        command_name = "".join([ast[0]] + ast[1])
        opts_and_args = ast[2]
        return models.TeXCommand(command_name, opts_and_args)

    def SYMBOL_CMD(self, ast):
        command_name = "".join([ast[0]] + ast[1])
        opts_and_args = [ast[2]]
        return models.TeXCommand(command_name, opts_and_args)

    def ARG(self, ast):
        return models.TeXCommandArgument(ast[1])

    def OPT(self, ast):
        return models.TeXCommandOption(ast[1])

    def MARKER(self, ast):
        return models.TeXMarker(int(ast[1]))

    def TEXT(self, ast):
        return models.TeXText("".join(ast))

# =============================================================================
# Parser.


class TeXParser():
    """
    A simple TeX parser.
    """

    def parse(self, path=None, string=None, expand_macros=True):
        """
        Parses a given TeX document.
        """
        if path is not None:
            if file_utils.is_missing_or_empty_file(path):
                print("Error: The given input path does not exist / is empty.")
                sys.exit(1)
            else:
                # Read the input path.
                string = file_utils.read_file(path)

        # Parse the given TeX file.
        doc = self.parse_tex_document(string)

        # Expand the macros.
        if expand_macros:
            tex_parser_expand_macros.expand_macros(doc, doc.macro_definitions)
        return doc

    def parse_tex_document(self, text):
        """
        Tokenizes a given TeX document.
        """
        if text is None:
            raise ValueError("Nothing to parse.")

        tex_tokenizer = TeXTokenParser(parseinfo=False, whitespace='')
        tex_semantics = TeXSemantics()
        tex_tokenizer.parse(text, semantics=tex_semantics)
        return tex_semantics.document
