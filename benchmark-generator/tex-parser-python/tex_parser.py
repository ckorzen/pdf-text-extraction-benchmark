from tex_tokenizer import TeXTokenParser
from argparse import ArgumentParser

# =============================================================================
# Models.


class TeXDocument:
    """
    A class representing a whole TeX document.
    """
    def __init__(self, elements=[]):
        self.elements = elements


class TeXElement:
    """
    The base class for any TeX element.
    """
    def __init__(self):
        pass

    def __str__(self):
        return ""

    def __repr__(self):
        return self.__str__()


class TeXGroup(TeXElement):
    """
    A class representing a group (elements enclosed in {...}).
    """
    def __init__(self, elements):
        self.elements = elements

    def __str__(self):
        return "{%s}" % "".join([str(x) for x in self.elements])


class TeXCommand(TeXElement):
    """
    A class representing a command.
    """
    def __init__(self, command_name, opts_and_args=[]):
        self.command_name = command_name
        self.opts_and_args = opts_and_args

    def __str__(self):
        opts_and_args_str = "".join([str(x) for x in self.opts_and_args])
        return "%s%s" % (self.command_name, opts_and_args_str)


class TeXCommandArgument(TeXGroup):
    """
    A class representing an argument of a group (elements enclosed in {...}).
    """
    pass


class TeXCommandOption(TeXGroup):
    """
    A class representing an option of a group (elements enclosed in [...]).
    """
    def __str__(self):
        return "[%s]" % "".join([str(x) for x in self.elements])


class TeXText(TeXElement):
    """
    A class representing some textual content.
    """
    def __init__(self, text):
        self.text = text

    def __str__(self):
        return self.text

    def __repr__(self):
        return self.__str__()

# =============================================================================
# Semantics.


class TeXSemantics(object):
    """
    A class that defines some semantics for the production rule in grammar.
    """

    def start(self, ast):
        self.document = ast

    def DOC(self, ast):
        return TeXDocument(ast)

    def GROUP(self, ast):
        return TeXGroup(ast[1])

    def CONTROL_CMD(self, ast):
        command_name = "".join([ast[0]] + ast[1])
        opts_and_args = ast[2]
        return TeXCommand(command_name, opts_and_args)

    def ARG(self, ast):
        return TeXCommandArgument(ast[1])

    def OPT(self, ast):
        return TeXCommandOption(ast[1])

    def TEXT(self, ast):
        return TeXText("".join(ast))

# =============================================================================
# Parser.

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
        tex_semantics = TeXSemantics()
        tex_tokenizer.parse(text, semantics=tex_semantics)

        print(tex_semantics.document.elements)


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
