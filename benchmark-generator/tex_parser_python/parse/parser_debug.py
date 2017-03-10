from models import tex_elements
from utils import iterators

# =============================================================================
# Colorize Methods.


def black(text):
    """
    Adds ANSI color codes to the given text in order to change its font color
    to black.

    Args:
        text (str): The text to process.
    Returns:
        The text enriched with ANSI color code.
    """
    return text


def red(text):
    """
    Adds ANSI color codes to the given text in order to change its font color
    to red.

    Args:
        text (str): The text to process.
    Returns:
        The text enriched with ANSI color code.
    """
    return colorize(text, "\033[38;5;1m")


def green(text):
    """
    Adds ANSI color codes to the given text in order to change its font color
    to green.

    Args:
        text (str): The text to process.
    Returns:
        The text enriched with ANSI color code.
    """
    return colorize(text, "\033[38;5;22m")


def blue(text):
    """
    Adds ANSI color codes to the given text in order to change its font color
    to blue.

    Args:
        text (str): The text to process.
    Returns:
        The text enriched with ANSI color code.
    """
    return colorize(text, "\033[38;5;20m")


def gray(text):
    """
    Adds ANSI color codes to the given text in order to change its font color
    to gray.

    Args:
        text (str): The text to process.
    Returns:
        The text enriched with ANSI color code.
    """
    return colorize(text, "\033[90m")


def red_bg(text):
    """
    Adds ANSI color codes to the given text in order to change its background
    color to red.

    Args:
        text (str): The text to process.
    Returns:
        The text enriched with ANSI color code.
    """
    return colorize(text, "\033[48;5;167m")


def green_bg(text):
    """
    Adds ANSI color codes to the given text in order to change its background
    color to green.

    Args:
        text (str): The text to process.
    Returns:
        The text enriched with ANSI color code.
    """
    return colorize(text, "\033[48;5;70m")


def blue_bg(text):
    """
    Adds ANSI color codes to the given text in order to change its background
    color to blue.

    Args:
        text (str): The text to process.
    Returns:
        The text enriched with ANSI color code.
    """
    return colorize(text, "\033[48;5;110m")


def gray_bg(text):
    """
    Adds ANSI color codes to the given text in order to change its background
    color to gray.

    Args:
        text (str): The text to process.
    Returns:
        The text enriched with ANSI color code.
    """
    return colorize(text, "\033[47m")


def colorize(text, color_code):
    """
    Adds the given ANSI color code to the *trunk* of the given text, that
    is the stripped version of the text. All leading and trailing whitespaces
    won't be colorized.

    Args:
        text (str): The text to process.
        color_code (str): The ANSI color code to use in order to change the
            color of text.
    Returns:
         The text enriched with the ANSI color code.
    """

    if len(text) == 0:
        return ""

    # Allow the combinations of color codes. For that append the color code to
    # any reset markers ("\033[0m") in the text, because they reset *all* color
    # codes. Need to renew the color code to apply.
    text = text.replace("\033[0m", "\033[0m%s" % color_code)

    # The tool 'less' has issues on displaying color codes if colred text
    # contains newlines ("\n"): Only the first line is colored.
    # As a workaround, enrich each line with given color code.
    lines = text.split("\n")
    colored_lines = []
    for line in lines:
        # Color only "trunk" (the line without leading/trailing whitespaces.
        lstripped = line.lstrip()
        leading_ws = line[: - len(lstripped)]

        lrstripped = lstripped.rstrip()
        trailing_ws = lstripped[len(lrstripped):]

        # Wrap the trunk in color code.
        colored_line = "%s%s%s" % (color_code, lrstripped, "\033[0m")

        # Reinsert leading and trailing whitespaces.
        colored_line = "%s%s%s" % (leading_ws, colored_line, trailing_ws)

        colored_lines.append(colored_line if len(line) > 0 else "")

    return "\n".join(colored_lines)

# =============================================================================
# Stringify Methods.


def stringify_group(group, color):
    """
    Creates a string representation for the given group, enriched with ANSI
    color codes.

    Args:
        group (TeXGroup): The group to process.
        color (function): The function to use on creating the string.
    Returns:
        The string representation of the group, enriched with ANSI color codes.
    """
    parts = []
    parts.append(color("{"))
    parts.append(stringify_elements(group.elements))
    parts.append(color("}"))
    return "".join(parts)


def stringify_command(command, color):
    """
    Creates a string representation for the given command, enriched with ANSI
    color codes.

    Args:
        command (TeXCommand): The command to process.
        color (function): The function to use on creating the string.
    Returns:
        The string representation of the command, enriched with ANSI color
        codes.
    """
    parts = []
    parts.append(color(command.cmd_name))
    parts.append(stringify_elements(command.opts_args))
    return "".join(parts)


def stringify_macro_definition(macro_def, color):
    """
    Creates a string representation for the given macro definition, enriched
    with ANSI color codes.

    Args:
        macro_def (TeXMacroDefinition): The macro definition to process.
        color (function): The function to use on creating the string.
    Returns:
        The string representation of the macro definition, enriched with ANSI
        color codes.
    """
    parts = []
    parts.append(macro_def.macro_name)
    parts.append(" -> ")
    if macro_def.replacement is not None:
        parts.append(stringify_element(macro_def.replacement))
    return color("".join(parts))


def stringify_arg(arg, color):
    """
    Creates a string representation for the given command argument, enriched
    with ANSI color codes.

    Args:
        arg (TeXCommandArgument): The argument to process.
        color (function): The function to use on creating the string.
    Returns:
        The string representation of the command argument, enriched with ANSI
        color codes.
    """
    parts = []
    parts.append(color("{"))
    parts.append(stringify_elements(arg.elements))
    parts.append(color("}"))
    return "".join(parts)


def stringify_opt(opt, color):
    """
    Creates a string representation for the given command option, enriched
    with ANSI color codes.

    Args:
        opt (TeXCommandOption): The option to process.
        color (function): The function to use on creating the string.
    Returns:
        The string representation of the command option, enriched with ANSI
        color codes.
    """
    parts = []
    parts.append(color("["))
    parts.append(stringify_elements(opt.elements))
    parts.append(color("]"))
    return "".join(parts)


def stringify_marker(marker, color):
    """
    Creates a string representation for the given marker, enriched with ANSI
    color codes.

    Args:
        marker (TeXMarker): The marker to process.
        color (function): The function to use on creating the string.
    Returns:
        The string representation of the marker, enriched with ANSI color
        codes.
    """
    return color("#%s" % marker.i)


def stringify_word(text, color):
    """
    Creates a string representation for the given word, enriched with ANSI
    color codes.

    Args:
        text (TeXWord): The word to process.
        color (function): The function to use on creating the string.
    Returns:
        The string representation of the text, enriched with ANSI color codes.
    """
    return color(text.text)

# =============================================================================

# Define specifications how to create a string representation for a given
# TeXElement, with (1) a name for the element to use in color legend, (2)
# a function that defines the basic structure of the string to create for the
# element and (3) the color function to use in order to colorize the element.
model_spec = {
    tex_elements.TeXGroup: (
        "Group", stringify_group, red
    ),
    tex_elements.TeXCommand: (
        "Command", stringify_command, blue
    ),
    tex_elements.TeXControlCommand: (
        "ControlCommand", stringify_command, blue
    ),
    tex_elements.TeXBeginEnvironmentCommand: (
        "BeginEnvironmentCommand", stringify_command, blue
    ),
    tex_elements.TeXEndEnvironmentCommand: (
        "EndEnvironmentCommand", stringify_command, blue
    ),
    tex_elements.TeXMacroDefinition: (
        "MacroDefinition", stringify_macro_definition, gray_bg
    ),
    tex_elements.TeXCommandArgument: (
        "Arg", stringify_arg, red
    ),
    tex_elements.TeXCommandOption: (
        "Opt", stringify_opt, green
    ),
    tex_elements.TeXMarker: (
        "Marker", stringify_marker, green_bg
    ),
    tex_elements.TeXWhitespace: (
        "", stringify_word, black
    ),
    tex_elements.TeXWord: (
        "Text", stringify_word, black
    )
}


def create_debug_string(doc):
    """
    Creates a (colored) debug string for the given document, to be able to
    identify the detected TeX elements. The colors are implemented by using
    ANSI color codes and can be displayed in terminal, e.g. by using less -R.

    Args:
        doc (TeXDocument): The TeX document to create a debug string for.
    Returns:
        A debug string highlighting the individual elements of the TeX document
        by colors.
    """

    # Create a color legend, that clarifies which TeX element is represented by
    # which color.
    color_legend = create_color_legend()
    # Create a debug string for the macro definitions.
    macro_defs_str = stringify_elements(doc.macro_definitions.values(), "\n")
    # Create a debug string for all other elements.
    elements_str = stringify_elements(doc.elements)

    # Join all three components.
    return "\n\n".join([
        "Legend: %s" % color_legend.strip(),
        "Macro Definitions:\n\n%s" % macro_defs_str.strip(),
        "Elements:\n\n%s" % elements_str.strip()
    ])


def create_color_legend():
    """
    Creates a color legend that clarifies which TeX element is represented by
    which color.

    Returns:
        A string containing the color legend.
    """
    return ", ".join([create_color_legend_entry(x) for x in model_spec.keys()])


def create_color_legend_entry(model):
    """
    Creates an entry in color legend for the given model of a TeXElement.

    Args:
        model (class): The model to process.
    Returns:
        The string representing the color legend entry for the given model.
    """
    type_name = model_spec[model][0]
    color = model_spec[model][2]
    return color(type_name)


def stringify_elements(elements, delim=""):
    """
    Creates a debug string for the given TeX elements and joins them using the
    given delimiter.

    Args:
        elements (list of TeXElement): The elements to stringify.
        delim (str, optional): The delimiter to use on joining the substrings.
    Returns:
        The debug string for the given elements.
    """
    parts = []
    itr = iterators.ShallowIterator(elements)
    for element in itr:
        parts.append(stringify_element(element))
    return delim.join(parts)


def stringify_element(element):
    """
    Creates a debug string for the given TeX element.

    Args:
        element (TeXElement): The element to stringify.
    Returns:
        The debug string for the given element.
    """
    model = type(element)
    if model not in model_spec:
        return ""
    stringify_method = model_spec[model][1]
    color = model_spec[model][2]
    return stringify_method(element, color)
