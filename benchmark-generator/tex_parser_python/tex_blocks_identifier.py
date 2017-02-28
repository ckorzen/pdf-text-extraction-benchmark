from argparse import ArgumentParser

from parser import tex_parser
from interpreter import tex_interpreter
from serializer import ltb_serializer

def identify_blocks(
        input_file,
        output_file,
        output_format="txt",
        rules_file="rules/default_rules.csv"):
    """
    Identifies blocks in given TeX file and writes them to given output path.
    """

    # Parse the TeX file.
    parser = tex_parser.TeXParser()
    tex_document = parser.parse(path=input_file)

    # Interpret the TeX file.
    interpreter = tex_interpreter.TeXInterpreter()
    blocks = interpreter.interpret(tex_document, rules_file)

    # Serialize the blocks to file.
    ltb_serializer.serialize(blocks, output_file, output_format)


def get_argument_parser():
    """
    Creates an command line arguments parser.
    """
    arg_parser = ArgumentParser()
    arg_parser.add_argument(
        "input_path",
        help="The input path.",
    )
    arg_parser.add_argument(
        "output_path",
        help="The output path.",
    )
    arg_parser.add_argument(
        "-f", "--format",
        help="The output format [txt, xml, json].",
        default="txt"
    )
    return arg_parser


if __name__ == '__main__':
    args = get_argument_parser().parse_args()
    identify_blocks(
        args.input_path,
        args.output_path,
        output_format=args.format
    )
