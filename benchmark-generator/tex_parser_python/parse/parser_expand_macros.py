from copy import deepcopy

from utils import iterators
from models import tex_models


def expand_macros(group, macro_dict):
    """
    Expands all macro calls in the given group based on the given macro
    dictionary.
    """
    # Iterate through the elements in DFS order.
    dfs_iter = iterators.DFSIterator(group.elements)

    for element in dfs_iter:
        # Only a command can be a macro call.
        if not isinstance(element, tex_models.TeXCommand):
            continue

        # Check if the command is a macro call.
        cmd_name = element.cmd_name
        if cmd_name in macro_dict:
            replacement = deepcopy(macro_dict[cmd_name].replacement)
            # Expand the macro call.
            expand_macro(element, replacement, macro_dict)


def expand_macro(macro_call, replacement, macro_dict):
    """
    Expands the given macro call by the given replacement recursively.
    """
    # Iterate through the elements in DFS order.
    dfs_iter = iterators.DFSIterator(replacement.elements)

    # Replace all markers by related arguments.
    for element in dfs_iter:
        if not isinstance(element, tex_models.TeXMarker):
            continue
        macro_expansion = macro_call.args[element.i - 1].elements
        element.register_elements_from_macro_expansion(macro_expansion)

    macro_call.register_elements_from_macro_expansion(replacement.elements)
    # Expand macro calls recursively.
    expand_macros(replacement, macro_dict)
