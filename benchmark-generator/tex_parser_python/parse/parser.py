from models import tex_models
from utils import file_utils
from tex_tokenizer import TeXTokenParser
from parser_expand_macros import expand_macros
from parser_debug import create_debug_string

# =============================================================================
# Semantics.


class TeXSemantics(object):
    """
    A class that defines some semantics for the production rule in grammar.
    """

    def __init__(self):
        self.document = tex_models.TeXDocument()
        self.begin_environment_cmds = []

    def get_environments_stack(self):
        return [x.get_environment() for x in self.begin_environment_cmds[::-1]]

    def DOC(self, elements):
        # Distinguish between macro definitions and all other elements.
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
        return tex_models.TeXGroup(
            elements=ast[1],
            document=self.document,
            environments=self.get_environments_stack()
        )

    def TEX_MACRO_DEF_CMD(self, ast):
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
        return tex_models.TeXBreakCommand(
            cmd_name=ast[0],
            document=self.document,
            environments=self.get_environments_stack()
        )

    def CONTROL_CMD(self, ast):
        cmd_name = "".join([ast[0]] + ast[1])
        opts_args = ast[2]
        cmd = tex_models.TeXControlCommand.factory(
            cmd_name=cmd_name,
            opts_args=opts_args,
            document=self.document,
            environments=self.get_environments_stack()
        )

        if isinstance(cmd, tex_models.TeXDocumentClassCommand):
            self.document.document_class = cmd.get_document_class()
        if isinstance(cmd, tex_models.TeXBeginEnvironmentCommand):
            self.begin_environment_cmds.append(cmd)
        if isinstance(cmd, tex_models.TeXEndEnvironmentCommand):
            begin_environment_cmd = self.begin_environment_cmds.pop()
            begin_environment_cmd.end_command = cmd
            cmd.begin_command = begin_environment_cmd

        return cmd

    def SYMBOL_CMD(self, ast):
        cmd_name = "".join([ast[0]] + ast[1])
        opts_args = [ast[2]]
        return tex_models.TeXCommand(
            cmd_name=cmd_name,
            opts_args=opts_args,
            document=self.document,
            environments=self.get_environments_stack()
        )

    def ARG(self, ast):
        return tex_models.TeXCommandArgument(
            elements=ast[1],
            document=self.document,
            environments=self.get_environments_stack()
        )

    def OPT(self, ast):
        return tex_models.TeXCommandOption(
            elements=ast[1],
            document=self.document,
            environments=self.get_environments_stack()
        )

    def MARKER(self, ast):
        return tex_models.TeXMarker(
            int(ast[1]),
            document=self.document,
            environments=self.get_environments_stack()
        )

    def TEXT(self, ast):
        return tex_models.TeXText(
            "".join(ast),
            document=self.document,
            environments=self.get_environments_stack()
        )

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
