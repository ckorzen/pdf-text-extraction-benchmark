from models import tex_models
from utils import file_utils
from tex_tokenizer import TeXTokenParser
from tex_parser_expand_macros import expand_macros
from tex_parser_debug import create_debug_string

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
            if isinstance(element, tex_models.TeXMacroDefinition):
                macro_definitions[element.command_name] = element
            else:
                tex_elements.append(element)
        return tex_models.TeXDocument(tex_elements, macro_definitions)

    def GROUP(self, ast):
        return tex_models.TeXGroup(ast[1])

    def TEX_MACRO_DEF_CMD(self, ast):
        macro_name = "".join([ast[1]] + ast[2])
        replacement = ast[4]
        return tex_models.TeXMacroDefinition(macro_name, replacement)

    def LATEX_MACRO_DEF_CMD(self, ast):
        macro_name = ast[1].elements[0].command_name
        replacement = ast[-1]
        return tex_models.TeXMacroDefinition(macro_name, replacement)

    def BREAK_CMD(self, ast):
        return tex_models.TeXBreakCommand(ast[0])

    def CONTROL_CMD(self, ast):
        command_name = "".join([ast[0]] + ast[1])
        opts_and_args = ast[2]
        return tex_models.TeXCommand(command_name, opts_and_args)

    def SYMBOL_CMD(self, ast):
        command_name = "".join([ast[0]] + ast[1])
        opts_and_args = [ast[2]]
        return tex_models.TeXCommand(command_name, opts_and_args)

    def ARG(self, ast):
        return tex_models.TeXCommandArgument(ast[1])

    def OPT(self, ast):
        return tex_models.TeXCommandOption(ast[1])

    def MARKER(self, ast):
        return tex_models.TeXMarker(int(ast[1]))

    def TEXT(self, ast):
        return tex_models.TeXText("".join(ast))

# =============================================================================
# Parser.


def parse(path=None, string=None, expand_macro_calls=True):
    """
    Parses the given TeX input.
    """
    return TeXParser().parse(path, string, expand_macro_calls)


class TeXParser():
    """
    A simple TeX parser.
    """
    def parse(self, path=None, string=None, expand_macro_calls=True):
        """
        Parses a given TeX document.
        """
        if path is not None:
            if file_utils.is_missing_or_empty_file(path):
                raise ValueError("The input path does not exist / is empty.")
            else:
                # Read the input path.
                string = file_utils.read_file(path)

        # Parse the given TeX file.
        doc = self.parse_tex_document(string)

        # Expand the macros.
        if expand_macro_calls:
            expand_macros(doc, doc.macro_definitions)

        # Create a debug string.
        doc.debug_string = create_debug_string(doc)

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
