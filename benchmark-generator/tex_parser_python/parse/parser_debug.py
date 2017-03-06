from models import tex_models
from utils import iterators

# =============================================================================
# Colorize Methods.


def black(text):
    """
    Changes the font color of given text to black.

    Args:
        text (str): The text to process.
    Returns:
        The text enriched with ANSI escape codes that colors the text.
    """
    return text


def red(text):
    """
    Changes the font color of given text to red.

    Args:
        text (str): The text to process.
    Returns:
        The text enriched with ANSI escape codes that colors the text.
    """
    return colorize(text, "\033[38;5;1m")


def green(text):
    """
    Changes the font color of given text to green.

    Args:
        text (str): The text to process.
    Returns:
        The text enriched with ANSI escape codes that colors the text.
    """
    return colorize(text, "\033[38;5;22m")


def blue(text):
    """
    Changes the font color of given text to blue.

    Args:
        text (str): The text to process.
    Returns:
        The text enriched with ANSI escape codes that colors the text.
    """
    return colorize(text, "\033[38;5;20m")


def gray(text):
    """
    Changes the font color of given text to gray.

    Args:
        text (str): The text to process.
    Returns:
        The text enriched with ANSI escape codes that colors the text.
    """
    return colorize(text, "\033[90m")


def red_bg(text):
    """
    Changes the background of given text to red.
    """
    return colorize(text, "\033[48;5;167m")


def green_bg(text):
    """
    Changes the background of given text to green.

    Args:
        text (str): The text to process.
    Returns:
        The text enriched with ANSI escape codes that colors the text.
    """
    return colorize(text, "\033[48;5;70m")


def blue_bg(text):
    """
    Changes the background of given text to blue.

    Args:
        text (str): The text to process.
    Returns:
        The text enriched with ANSI escape codes that colors the text.
    """
    return colorize(text, "\033[48;5;110m")


def gray_bg(text):
    """
    Changes the background of given text to gray.

    Args:
        text (str): The text to process.
    Returns:
        The text enriched with ANSI escape codes that colors the text.
    """
    return colorize(text, "\033[47m")


def colorize(text, color_code):
    """
    Applies the given color code to the *trunk* of the given text, that
    is the stripped version of the text. All leading and trailing whitespaces
    won't be colorized.

    Args:
        text (str): The text to process.
        color_code (str): The ANSI ecapse code to use in order to change the
            color of text.
    Returns:
        The text enriched with given ANSI escape code.
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
    Creates enriched with ANSI color codes for the given group.

    Args:
        group (TeXGroup): The group to process.
        color (function): The function to use to color the given group.
    Returns:
        The string representation of group, enriched with ANSI color codes.
    """
    parts = []
    parts.append(color("{"))
    parts.append(stringify_elements(group.elements))
    parts.append(color("}"))
    return "".join(parts)


def stringify_command(command, color):
    """
    Creates enriched with ANSI color codes for the given command.

    Args:
        command (TeXCommand): The command to process.
        color (function): The function to use to color the given command.
    Returns:
        The string representation of command, enriched with ANSI color codes.
    """
    parts = []
    parts.append(color(command.cmd_name))
    parts.append(stringify_elements(command.opts_args))
    return "".join(parts)


def stringify_macro_definition(macro_def, color):
    """
    Creates enriched with ANSI color codes for the given macro definition.

    Args:
        macro_def (TeXMacroDefinition): The macro definition to process.
        color (function): The function to use to color the given macro def.
    Returns:
        The string representation of macro definition, enriched with ANSI
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
    Creates enriched with ANSI color codes for the given argument.

    Args:
        arg (TeXCommandArgument): The argument to process.
        color (function): The function to use to color the given command.
    Returns:
        The string representation of argument, enriched with ANSI color codes.
    """
    parts = []
    parts.append(color("{"))
    parts.append(stringify_elements(arg.elements))
    parts.append(color("}"))
    return "".join(parts)


def stringify_opt(opt, color):
    """
    Creates enriched with ANSI color codes for the given option.

    Args:
        opt (TeXCommandOption): The option to process.
        color (function): The function to use to color the given option.
    Returns:
        The string representation of option, enriched with ANSI color codes.
    """
    parts = []
    parts.append(color("["))
    parts.append(stringify_elements(opt.elements))
    parts.append(color("]"))
    return "".join(parts)


def stringify_marker(marker, color):
    """
    Creates a string enriched with ANSI color codes for the given marker.

    Args:
        marker (TeXMarker): The marker to process.
        color (function): The function to use to color the given marker.
    Returns:
        The string representation of marker, enriched with ANSI color codes.
    """
    return color("#%s" % marker.i)


def stringify_text(text, color):
    """
    Creates a enriched with ANSI color codes for the given text.

    Args:
        text (TeXText): The text to process.
        color (function): The function to use to color the given text.
    Returns:
        The string representation of text, enriched with ANSI color codes.
    """
    return color(text.text)

# =============================================================================

# Define how to represent each single TeX element.
model_spec = {
    tex_models.TeXGroup: (
        "Group", stringify_group, red
    ),
    tex_models.TeXCommand: (
        "Command", stringify_command, blue
    ),
    tex_models.TeXControlCommand: (
        "ControlCommand", stringify_command, blue
    ),
    tex_models.TeXDocumentClassCommand: (
        "DocumentClassCommand", stringify_command, blue
    ),
    tex_models.TeXUsePackageCommand: (
        "UsePackageCommand", stringify_command, blue
    ),
    tex_models.TeXBeginEnvironmentCommand: (
        "BeginEnvironmentCommand", stringify_command, blue
    ),
    tex_models.TeXEndEnvironmentCommand: (
        "EndEnvironmentCommand", stringify_command, blue
    ),
    tex_models.TeXBreakCommand: (
        "BreakCommand", stringify_command, blue
    ),
    tex_models.TeXMacroDefinition: (
        "MacroDefinition", stringify_macro_definition, gray_bg
    ),
    tex_models.TeXCommandArgument: (
        "Arg", stringify_arg, red
    ),
    tex_models.TeXCommandOption: (
        "Opt", stringify_opt, green
    ),
    tex_models.TeXMarker: (
        "Marker", stringify_marker, green_bg
    ),
    tex_models.TeXText: (
        "Text", stringify_text, black
    )
}


def create_debug_string(doc):
    """
    Creates a (colored) debug string for the given document, to be able to
    identify the detected TeX elements.

    Args:
        doc (TeXDocument): The TeX document to process.
    Returns:
        A debug string illustrating the elements of the given document.
    """

    # Create a color legend (which TeX element is represented by which color?)
    color_legend = create_color_legend()
    # Create a debug string for the macro definitions.
    macro_defs_str = stringify_elements(doc.macro_definitions.values(), "\n")
    # Create a debug string for all other elements.
    elements_str = stringify_elements(doc.elements)

    return "\n\n".join([
        "Legend: %s" % color_legend.strip(),
        "Macro Definitions:\n\n%s" % macro_defs_str.strip(),
        "Elements:\n\n%s" % elements_str.strip()
    ])


def create_color_legend():
    """
    Creates a color legend to be able to identify which TeX element is
    represented by which color.

    Returns:
        A string containing the color legend.
    """
    return ", ".join([create_color_legend_entry(x) for x in model_spec.keys()])


def create_color_legend_entry(model):
    """
    Creates an entry in color legend for the given TeX model.

    Args:
        model (TeXElement): The model to process.
    Returns:
        The string representing the color legend entry for the given model.
    """
    type_name = model_spec[model][0]
    color = model_spec[model][2]
    return color(type_name)


def stringify_elements(elements, delim=""):
    """
    Creates a debug string for each given given TeX element and joins them
    separated by the given delimiter.

    Args:
        elements (list of TeXElement): The elements to stringify.
        delim (str, optional): The delimiter to use on joining the strings.
    Returns:
        The string representation of given elements.
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
        The string representation of given element.
    """
    model = type(element)
    if model not in model_spec:
        return ""
    stringify_method = model_spec[model][1]
    color = model_spec[model][2]
    return stringify_method(element, color)
