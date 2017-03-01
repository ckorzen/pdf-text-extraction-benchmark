from utils import iterators

from models import tex_models
from models.ltb import LTB, Outline
from models.rules import Rules


def identify_outline(tex_document, rules_path):
    """
    Iterates through the elements in the given document to identify logical
    text blocks using the given rules.
    """
    rules = Rules.read_from_file(rules_path)
    interpreter = TeXInterpreter(tex_document, rules)
    interpreter.identify_blocks()

    return interpreter.outline


class TeXInterpreter():
    def __init__(self, tex_document, rules):
        self.tex_document = tex_document
        self.rules = rules
        self.level = 0
        self.stack = [LTB(level=self.level)]
        self.outline = Outline()

    def identify_blocks(self):
        """
        Iterates through the elements in the given TeX document to identify
        logical text blocks using the given rules.
        """
        # Interpret the elements in given document.
        self._identify_blocks(self.tex_document)

        # Add the remaining blocks in the stack to the result list.
        while len(self.stack) > 0:
            block = self.stack.pop()
            if len(block.text) > 0:
                self.outline.append(block)

    def _identify_blocks(self, group):
        """
        Iterates through the elements in the given group to identify
        logical text blocks using the given rules.
        """
        # Iterate the elements in DFS order.
        for element in iterators.ShallowIterator(group.elements):
            if isinstance(element, tex_models.TeXText):
                # Append text to the active block.
                self.append_text(element.text)

            if isinstance(element, tex_models.TeXGroup):
                # Interpret all elements of the group.
                self._identify_blocks(element.elements)

            if isinstance(element, tex_models.TeXCommand):
                # Interpret the command correspondingly to referring rule.
                rule = self.rules.get_rule(element)

                if rule is None:
                    # There is no such rule. Ignore the command.
                    # TODO: Skip to end command.
                    continue

                # Process each single instruction given by the rule.
                for instruction in rule.get_instructions():
                    instruction.apply(self, element)

    # =========================================================================

    def introduce_block(self):
        """
        Introduces a new block.
        """
        block = LTB(level=self.level)
        self.stack.append(block)

    def finish_block(self):
        """
        Finishes the active block.
        """
        if len(self.stack) == 0:
            return
        block = self.stack.pop()
        if len(block.text) == 0:
            return
        self.outline.append(block)

    def append_text(self, text):
        """
        Appends the given text to the active block.
        """
        if len(self.stack) == 0:
            return
        self.stack[-1].text += text

    def set_hierarchy_level(self, level):
        """
        Sets the hierarchy level for all subsequent blocks.
        """
        self.level = level

    def set_semantic_role(self, role):
        """
        Sets the semantic role for the active block.
        """
        if len(self.stack) == 0:
            return
        self.stack[-1].semantic_role = role
