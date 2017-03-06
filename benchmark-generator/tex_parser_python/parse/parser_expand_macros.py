from copy import deepcopy

from utils import iterators
from models import tex_models


def expand_macros(group, macro_dict):
    """
    Expands all macro calls in the given group based on the given macro
    dictionary.

    Args:
        group (TeXGroup): The group to process.
        macro_dict (dict of str:TeXGroup): The macro dictionary.
    """
    # Iterate through the elements in DFS order.
    dfs_iter = iterators.DFSIterator(group.elements)

    for element in dfs_iter:
        # Ignore all non-commands (only a command can be a macro call).
        if not isinstance(element, tex_models.TeXCommand):
            continue

        # Check if the command is a macro call.
        if element.cmd_name in macro_dict:
            # Make a copy of the macro replacement.
            replacement = deepcopy(macro_dict[element.cmd_name].replacement)
            # Expand the macro call.
            expand_macro(element, replacement, macro_dict)


def expand_macro(macro_call, replacement, macro_dict):
    """
    Expands the given macro call by the given replacement recursively.

    Args:
        macro_call (TeXCommand): The macro call to expand.
        replacement (TeXGroup): The replacement to insert on expanding the
            macro call.
        macro_dict (dict of str:TeXGroup): The macro dictionary.
    """
    # Iterate through the elements in replacmenet in DFS order.
    dfs_iter = iterators.DFSIterator(replacement.elements)

    for element in dfs_iter:
        # Replace all markers by related arguments.
        if not isinstance(element, tex_models.TeXMarker):
            continue
        macro_expansion = macro_call.args[element.i - 1].elements
        element.register_elements_from_macro_expansion(macro_expansion)

    # Register the expanded elements to the macro call.
    macro_call.register_elements_from_macro_expansion(replacement.elements)
    # Expand macro calls recursively.
    expand_macros(replacement, macro_dict)
