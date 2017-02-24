import ast
import models
import iterators
import debugger
import copy

from tex_tokenizer import TeXTokenParser
from argparse import ArgumentParser

# TODO: Create the macro dictionary.
# TODO: Resolve macros.
# TODO: Extend and refine the grammar.


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
        return models.TeXCommand(ast[0])

    def CONTROL_CMD(self, ast):
        command_name = "".join([ast[0]] + ast[1])
        opts_and_args = ast[2]
        return models.TeXCommand(command_name, opts_and_args)

    def SYMBOL_CMD(self, ast):
        command_name = "".join([ast[0]] + ast[1])
        opts_and_args = [ast[2]]
        return models.TeXCommand(command_name, opts_and_args)

    def ARG(self, ast):
        print(ast)
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

    def __init__(self, args):
        self.args = args

    def parse(self):
        """
        Parses a given TeX document.
        """
        # Parse the given TeX file.
        tex_document = self.parse_tex_document()

        # Expand the macros.
        if self.args.expand_macros:
            self.expand_macros(tex_document, tex_document.macro_definitions)

        return tex_document

    def parse_tex_document(self):
        """
        Tokenizes a given TeX document.
        """
        # TODO:
        with open(self.args.input) as f:
            text = f.read()
        f.close()
        tex_tokenizer = TeXTokenParser(parseinfo=False, whitespace='')
        tex_semantics = TeXSemantics()
        tex_tokenizer.parse(text, semantics=tex_semantics)
        return tex_semantics.document

    def expand_macros(self, group, macro_definitions):
        dfs_iter = iterators.DFSIterator(group)

        for element in dfs_iter:
            if not isinstance(element, models.TeXCommand):
                continue

            if element.command_name in macro_definitions:
                macro = macro_definitions[element.command_name]
                replacement = copy.deepcopy(macro.replacement)
                self.expand_macro(element, replacement, macro_definitions)

    def expand_macro(self, macro_call, replacement, macro_definitions):
        dfs_iter = iterators.DFSIterator(replacement)

        for element in dfs_iter:
            if not isinstance(element, models.TeXMarker):
                continue
            element.is_expanded = True
            element.expanded = macro_call.args[element.i - 1].elements

        macro_call.is_expanded = True
        macro_call.expanded = replacement.elements
        self.expand_macros(replacement, macro_definitions)


def get_argument_parser():
    """
    Creates an command line arguments parser.
    """
    arg_parser = ArgumentParser()
    arg_parser.add_argument(
        "input",
        help="The input.",
    )
    arg_parser.add_argument(
        "--expand_macros",
        metavar='<bool>',
        help="Flag to toogle the expansion of macros.",
        type=ast.literal_eval,
        default=True
    )
    return arg_parser

if __name__ == '__main__':
    args = get_argument_parser().parse_args()
    tex_document = TeXParser(args).parse()
    print(debugger.create_debug_string(tex_document))
