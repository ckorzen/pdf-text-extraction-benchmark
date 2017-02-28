from copy import deepcopy

from utils import iterators
from models import tex_models


def expand_macros(group, macro_dict):
    dfs_iter = iterators.DFSIterator(group)

    for element in dfs_iter:
        if not isinstance(element, tex_models.TeXCommand):
            continue

        cmd_name = element.command_name
        if cmd_name in macro_dict:
            replacement = deepcopy(macro_dict[cmd_name].replacement)
            expand_macro(element, replacement, macro_dict)


def expand_macro(macro_call, replacement, macro_dict):
    dfs_iter = iterators.DFSIterator(replacement)

    for element in dfs_iter:
        if not isinstance(element, tex_models.TeXMarker):
            continue
        element.is_expanded = True
        element.expanded = macro_call.args[element.i - 1].elements

    macro_call.is_expanded = True
    macro_call.expanded = replacement.elements
    expand_macros(replacement, macro_dict)
