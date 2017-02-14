def visualize_split():
    """ Visualizes a split operation. """
    # There was a discussion whether to use a "‖" (which is naturally
    # associated with a split or a "==" (which is naturally associated with a
    # merge. We end up with "==", because we want to symbolize the errors, a
    # tool has made. And on a split the error is: The tool has faulty merged
    # two paragraphs.
    return red("=")


def visualize_merge():
    """ Visualizes a merge operation. """
    return red("|")


def red(text):
    """ Sets the color of given text to red. """
    return colorize(text, "\033[38;5;1m")


def green(text):
    """ Sets the color of given text to green. """
    return colorize(text, "\033[38;5;22m")


def blue(text):
    """ Sets the color of given text to blue. """
    return colorize(text, "\033[38;5;20m")


def gray(text):
    """ Sets the color of given text to gray. """
    return colorize(text, "\033[90m")


def red_bg(text):
    """ Adds a red background to the given text. """
    return colorize(text, "\033[48;5;167m")


def green_bg(text):
    """ Adds a green background to the given text. """
    return colorize(text, "\033[48;5;70m")


def blue_bg(text):
    """ Adds a blue background to the given text. """
    return colorize(text, "\033[48;5;110m")


def gray_bg(text):
    """ Adds a gray background to the given text. """

    return colorize(text, "\033[100m")


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

# ==============================================================================


def get_unnormalized_text(words):
    """ Returns the (unnormalized) text composed from the given words."""
    return "".join([x.unnormalized_with_whitespaces for x in words])
