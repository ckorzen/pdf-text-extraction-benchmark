import sys
import ast

from argparse import ArgumentParser

from parse import parser
from interpret import interpreter
from serialize import serializer

# Define some default values.
DEFAULT_OUTPUT_FILE = None
DEFAULT_OUTPUT_FORMAT = "txt"
DEFAULT_RULES_FILE = "rules/default_rules.csv"
DEFAULT_EXPAND_MACROS = True
DEFAULT_ROLES_FILTER = []


def main(args):
    """
    The main method.

    Args:
        args (Namespace): The command line arguments.
    """
    process_tex_file(**vars(args))


def process_tex_file(tex_file, **kwargs):
    """
    Processes the given TeX file:
    (1) Parses the TeX file syntactically in order to model the hierarchy of
        the TeX elements.
    (2) Identifies the logical text blocks (LTBs) using rules.
    (3) Serializes the LTBs to given target in given output format.

    Args:
        tex_file (str): The path to the TeX file to process.
        **output_file (str): The path to the output file for serialization. If
            not set, the serialized LTBs will be printed to stdout.
        **output_format (str): The serialization format [txt, xml, json].
        **rules_file (str): The path to the file with rules to use on
            identifying the LTBs.
        **expand_macros (bool): Flag to toggle the expansion of macro calls on
            parsing the TeX file.
        **roles_filter (list of str): A list of semantic roles. If not empty,
            only LTBs with included roles will be serialized. If empty or not
            set, all LTBs will be serialized.

    Returns:
        The parsed TeX document.
    """

    # Obtain the options.
    output_file = kwargs.get("output_file", DEFAULT_OUTPUT_FILE)
    output_format = kwargs.get("output_format", DEFAULT_OUTPUT_FORMAT)
    rules_file = kwargs.get("rules_file", DEFAULT_RULES_FILE)
    expand_macros = kwargs.get("expand_macros", DEFAULT_EXPAND_MACROS)
    roles = kwargs.get("roles_filter", DEFAULT_ROLES_FILTER)

    # Parse the TeX file.
    doc = parse_tex_file(tex_file, expand_macros)
    # Identify the hierarchical outline of LTBs.
    doc.ltb_outline = identify_ltb_outline(doc, rules_file)
    # Serialize the LTBs.
    doc.serialization = serialize(doc, output_file, output_format, roles)

    return doc


def parse_tex_file(tex_file, expand_macros=True):
    """
    Parses the given TeX file syntactically in order to model the hierarchy of
    the TeX elements.

    Args:
        tex_file (str): The path to the TeX file to process.
        expand_macros (bool, optional): Flag to toggle the expansion of macros.
    Returns:
        The parsed TeX document.
    """
    return parser.parse(
        path=tex_file,
        expand_macros=expand_macros
    )


def identify_ltb_outline(doc, rules_file=DEFAULT_RULES_FILE):
    """
    Identifies the hierarchical outline of logical text blocks (LTBs) using
    rules.

    Args:
        doc (TeXDocument): The parsed TeX document.
        rules_file (str, optional): The path to the file with rules to use.

    Returns:
        The (hierarchical) outline of identified LTBs.
    """
    return interpreter.identify_ltb_outline(doc, rules_file)


def serialize(
        doc,
        output_file=DEFAULT_OUTPUT_FILE,
        output_format=DEFAULT_OUTPUT_FORMAT,
        roles_filter=[]):
    """
    Serializes the LTBs to given file in given output format.

    Args:
        doc (TeXDocument): The parsed TeX document.
        output_file (str, optional): The path to the output file. If
            not set, the serialized LTBs will be printed to stdout.
        output_format (str, optional): The serialization format
            [txt, xml, json]. Default: txt.
        roles_filter (list of str, otional): A list of semantic roles. If not
            empty, only LTBs with included roles will be serialized. If empty
            or not set, all LTBs will be serialized.
    Returns:
        The serialization string.
    """
    serialization = serializer.serialize(doc, output_format, roles_filter)

    # Write the serialization to file (or stdout).
    if serialization is None:
        return

    # Write to stdout if there is no output_file given.
    out = open(output_file, 'w') if output_file is not None else sys.stdout
    out.write(serialization)
    if out is not sys.stdout:
        out.close()

    return serialization

# =============================================================================


def create_argument_parser():
    """
    Creates a related command line argument parser.

    Returns:
        The created argument parser.
    """
    arg_parser = ArgumentParser(
        description="Identifies and serializes logical text blocks (LTBs) in "
                    "TeX files."
    )
    arg_parser.add_argument(
        "tex_file",
        help="The TeX file to process.",
    )
    arg_parser.add_argument(
        "-o", "--output_file",
        help="The path to the output file for serialization.",
        default=DEFAULT_OUTPUT_FILE,
        metavar="<path>",
    )
    arg_parser.add_argument(
        "-f", "--output_format",
        help="The serialization format. Default: %(default)s.",
        choices=serializer.get_serialization_choices(),
        default=DEFAULT_OUTPUT_FORMAT,
        metavar="<path>"
    )
    arg_parser.add_argument(
        "-r", "--roles_filter",
        help="A list of semantic roles. If not empty, only LTBs with included "
             "roles will be serialized. If empty or not set, all LTBs will be "
             "serialized.",
        nargs="*",
        default=DEFAULT_ROLES_FILTER,
        metavar="<role>"
    )
    arg_parser.add_argument(
        "--rules_file",
        help="The path to the file with rules to use on identifying the LTBs. "
             "Default: %(default)s.",
        default=DEFAULT_RULES_FILE,
        metavar="<path>"
    )
    arg_parser.add_argument(
        "--expand_macros",
        help="Flag to toggle the expansion of macro calls on parsing the TeX "
             "file. Default: %(default)s",
        type=ast.literal_eval,
        default=DEFAULT_EXPAND_MACROS,
        metavar="<bool>"
    )
    return arg_parser

# =============================================================================

if __name__ == '__main__':
    main(create_argument_parser().parse_args())
