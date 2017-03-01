import ast

from argparse import ArgumentParser

from parser import tex_parser
from interpreter import tex_interpreter
from serializer import blocks_serializer

DEFAULT_OUTPUT_FORMAT = "txt"
DEFAULT_RULES_FILE = "rules/new_rules.csv"
DEFAULT_EXPAND_MACROS = True


def main(args):
    """
    The main method.
    """
    process_tex_file(
        tex_file=args.tex_file,
        output_file=args.output_file,
        output_format=args.output_format,
        rules_file=args.rules_file,
        expand_macro_calls=args.expand_macros
    )


def process_tex_file(
        tex_file=None,
        output_file=None,
        output_format=DEFAULT_OUTPUT_FORMAT,
        rules_file=DEFAULT_RULES_FILE,
        expand_macro_calls=DEFAULT_EXPAND_MACROS):
    """
    Identifies blocks in given TeX file and writes them to given output path.
    """

    # Parse the TeX file.
    doc = parse_tex_file(tex_file, expand_macro_calls)
    # Identify the blocks.
    blocks = identify_blocks(doc, rules_file)
    # Serialize the blocks to file.
    serialize_blocks(blocks, output_file, output_format)


def parse_tex_file(tex_file, expand_macro_calls=True):
    """
    Parses the given TeX file.
    """
    return tex_parser.parse(
        path=tex_file,
        expand_macro_calls=expand_macro_calls
    )


def identify_blocks(tex_document, rules_file):
    """
    Identifies the blocks in given TeX document.
    """
    return tex_interpreter.identify_blocks(tex_document, rules_file)


def serialize_blocks(blocks, output_file, output_format):
    """
    Serializes the given blocks to given output file in given format.
    """
    return blocks_serializer.serialize(blocks, output_file, output_format)

# =============================================================================

if __name__ == '__main__':
    arg_parser = ArgumentParser()
    arg_parser.add_argument(
        "tex_file",
        help="The TeX file to process.",
    )
    arg_parser.add_argument(
        "-o", "--output_file",
        help="The path to the output file.",
        metavar="<path>"
    )
    arg_parser.add_argument(
        "-f", "--output_format",
        help="The output format. Default: %(default)s.",
        choices=blocks_serializer.get_choices(),
        default=DEFAULT_OUTPUT_FORMAT,
        metavar="<path>"
    )
    arg_parser.add_argument(
        "-r", "--rules_file",
        help="The path to the rules file. Default: %(default)s",
        default=DEFAULT_RULES_FILE,
        metavar="<path>"
    )
    arg_parser.add_argument(
        "--expand_macros",
        help="Toggles the expansion of macros. Default: %(default)s",
        default=True,
        type=ast.literal_eval,
        metavar="<bool>"
    )
    main(arg_parser.parse_args())
