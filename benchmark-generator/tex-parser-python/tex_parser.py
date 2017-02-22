from tex_tokenizer import TeXTokenParser
from argparse import ArgumentParser


class TeXSemantics(object):
        def __init__(self):
            self.doc = 1

        def start(self, ast):
            print("XXX")
            return ast

        def DOC(self, ast):
            return ast

        def ELEMENT(self, ast):
            print("ELEMENT", ast)
            return ast

        def GROUP(self, ast):
            print("GROUP", type(ast[0]))
            return ast

        def CMD(self, ast):
            print(self.doc)
            return ast

        def BREAK_CMD(self, ast):
            print("BREAK_CMD", ast)
            return ast

        def CONTROL_CMD(self, ast):
            print("CONTROL_CMD", ast)
            return ast

        def SYMBOL_CMD(self, ast):
            print("SYMBOL_CMD", ast)
            return ast

        def ARG(self, ast):
            print("ARG", ast)
            return ast

        def OPT(self, ast):
            print("OPT", ast)
            return ast

        def MARKER(self, ast):
            print("MARKER", ast)
            return ast

        def COMMENT(self, ast):
            print("COMMENT", ast)
            return ast

        def TEXT(self, ast):
            return "".join(ast)

        def CHAR(self, ast):
            return ast

        def WHITESPACE(self, ast):
            return ast

        def LETTER(self, ast):
            return ast

        def DIGIT(self, ast):
            return ast

        def NON_LETTER(self, ast):
            return ast


class TeXParser():
    """
    A simple TeX parser.
    """

    def __init__(self, args):
        self.args = args

    def parse(self):
        # TODO:
        with open(self.args.input) as f:
            text = f.read()
        f.close()

        tex_tokenizer = TeXTokenParser(parseinfo=False)
        tex_tokenizer.parse(text, semantics=TeXSemantics())


def get_argument_parser():
    """
    Creates an command line arguments parser.
    """
    arg_parser = ArgumentParser()
    arg_parser.add_argument(
        "input",
        help="The input.",
    )
    return arg_parser

if __name__ == '__main__':
    args = get_argument_parser().parse_args()
    TeXParser(args).parse()
