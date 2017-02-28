from utils import iterators
from models import tex_models
from models.ltb import LTB
from models.rules import Rules


class TeXInterpreter():

    def interpret(self, tex_document, rules_path):
        """
        Iterates through the elements in the given TeX document to identify
        logical text blocks using the given rules.
        """
        # Read the rules.
        rules = Rules.read_from_file(rules_path)
        # The stack of active blocks.
        stack = [LTB(level=0)]
        # The list of finished blocks.
        result = []

        # Interpret.
        self._interpret(tex_document, rules, 0, stack, result)

        # Empty the stack.
        while len(stack) > 0:
            result.append(stack.pop())

        return result

    def _interpret(self, group, rules, level, stack, result):
        # Iterate the document tree in DFS order.
        dfs_iter = iterators.DFSIterator(group)

        for elem in dfs_iter:
            if isinstance(elem, tex_models.TeXGroup):
                dfs_iter.extend(elem.elements)

            if isinstance(elem, tex_models.TeXText):
                stack[-1].text += elem.text

            if isinstance(elem, tex_models.TeXCommand):
                rule = rules.get_rule(elem)

                if rule is None:
                    continue

                if rule.get_hierarchy_level(default=-1) > -1:
                    # Set the hierarchy level for all following blocks.
                    level = rule.get_hierarchy_level()
                if rule.get_starts_ltb_type(default=-1) > 1:
                    # Finish the active block in stack.
                    result.append(stack.pop())
                if rule.get_starts_ltb_type(default=-1) > 0:
                    # Append a new block to stack.
                    stack.append(LTB(level=level))
                if rule.get_semantic_role(default=None) is not None:
                    # Set the semantic role of active block.
                    stack[-1].semantic_role = rule.get_semantic_role()
                if rule.get_text_phrase(default=None) is not None:
                    # Append a text phrase to active block.
                    stack[-1].text += rule.get_text_phrase()
                    # TODO
                    if rule.get_end_command(default=None) is not None:
                        dfs_iter.skip_to(rule.get_end_command())
                for i in rule.get_args_to_visit(default=[]):
                    # Process the elements in arg to visit.
                    self._interpret(elem.args[i], rules, level, stack, result)
                if rule.get_ends_ltb_type(default=-1) > 0:
                    # Finish the active block.
                    result.append(stack.pop())
                if rule.get_ends_ltb_type(default=-1) > 1:
                    # Append a new block.
                    stack.append(LTB(level=level))

        return result
