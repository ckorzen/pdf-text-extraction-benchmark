from models import tex_models
from utils import iterators

# TODO: There are a couple of new commands: TeXBeginEnvironmentCommand, etc.

# =============================================================================
# Colorize Methods.


def black(text):
    """
    Sets the color of given text to black.
    """
    return text


def red(text):
    """
    Sets the color of given text to red.
    """
    return colorize(text, "\033[38;5;1m")


def green(text):
    """
    Sets the color of given text to green.
    """
    return colorize(text, "\033[38;5;22m")


def blue(text):
    """
    Sets the color of given text to blue.
    """
    return colorize(text, "\033[38;5;20m")


def gray(text):
    """
    Sets the color of given text to gray.
    """
    return colorize(text, "\033[90m")


def red_bg(text):
    """
    Adds a red background to the given text.
    """
    return colorize(text, "\033[48;5;167m")


def green_bg(text):
    """
    Adds a green background to the given text.
    """
    return colorize(text, "\033[48;5;70m")


def blue_bg(text):
    """
    Adds a blue background to the given text.
    """
    return colorize(text, "\033[48;5;110m")


def gray_bg(text):
    """
    Adds a gray background to the given text.
    """
    return colorize(text, "\033[47m")


def colorize(text, color_code):
    """
    Applies the given color code to the *trunk* of the given text, that
    is the stripped version of the text. All leading and trailing whitespaces
    won't be colorized.
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
    parts = []
    parts.append(color("{"))
    parts.append(stringify_elements(group.elements))
    parts.append(color("}"))
    return "".join(parts)


def stringify_command(command, color):
    parts = []
    parts.append(color(command.cmd_name))
    parts.append(stringify_elements(command.opts_args))
    return "".join(parts)


def stringify_macro_definition(macro_def, color):
    parts = []
    parts.append(macro_def.macro_name)
    parts.append(" -> ")
    if macro_def.replacement is not None:
        parts.append(stringify_element(macro_def.replacement))
    return color("".join(parts))


def stringify_arg(arg, color):
    parts = []
    parts.append(color("{"))
    parts.append(stringify_elements(arg.elements))
    parts.append(color("}"))
    return "".join(parts)


def stringify_opt(opt, color):
    parts = []
    parts.append(color("["))
    parts.append(stringify_elements(opt.elements))
    parts.append(color("]"))
    return "".join(parts)


def stringify_marker(marker, color):
    return color("#%s" % marker.i)


def stringify_text(text, color):
    return color(text.text)

# =============================================================================

model_spec = {
    tex_models.TeXGroup: (
        "Group", stringify_group, red
    ),
    tex_models.TeXCommand: (
        "Command", stringify_command, blue
    ),
    tex_models.TeXBreakCommand: (
        "Break Command", stringify_command, blue
    ),
    tex_models.TeXMacroDefinition: (
        "Macro Definition", stringify_macro_definition, gray_bg
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
    color_legend = create_color_legend()
    macro_defs_str = stringify_elements(doc.macro_definitions.values(), "\n")
    elements_str = stringify_elements(doc.elements)

    return "\n\n".join([
        "Legend: %s" % color_legend.strip(),
        "Macro Definitions:\n\n%s" % macro_defs_str.strip(),
        "Elements:\n\n%s" % elements_str.strip()
    ])


def create_color_legend():
    return ", ".join([create_color_legend_entry(x) for x in model_spec.keys()])


def create_color_legend_entry(model):
    type_name = model_spec[model][0]
    color = model_spec[model][2]
    return color(type_name)


def stringify_elements(elements, delim=""):
    parts = []
    itr = iterators.ShallowIterator(elements)
    for element in itr:
        parts.append(stringify_element(element))
    return delim.join(parts)


def stringify_element(element):
    model = type(element)
    if model not in model_spec:
        return ""
    stringify_method = model_spec[model][1]
    color = model_spec[model][2]
    return stringify_method(element, color)
