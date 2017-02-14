def remove_control_characters(text):
    """ Removes all control characters from given text."""

    if text is None:
        return

    # Create a dict with all the control characters.
    ctrl_chars = dict.fromkeys(range(32))

    return text.translate(ctrl_chars)
