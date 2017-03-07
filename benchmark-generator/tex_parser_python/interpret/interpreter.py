from utils import iterators

from models import tex_models
from models.ltb import LTB, Outline
from models.rules import Rules


def identify_outline(doc, rules_path):
    """
    Iterates through the elements of the given TeX document and identifies the
    logical text blocks bases on the given rules.

    Args:
        doc (TeXDocument): The parsed TeX document.
        rules_file (str): The path to the file with rules to use.

    Returns:
        The (hierarchical) outline of identified LTBs.
    """

    # Read the rules from file.
    rules = Rules.read_from_file(rules_path)
    # Identify the logical text blocks.
    interpreter = TeXInterpreter(doc, rules)
    interpreter.identify_blocks()

    return interpreter.outline


class TeXInterpreter():
    """
    A class to identify the logical text blocks of TeX documents based on
    rules.
    """

    def __init__(self, doc, rules):
        """
        Creates a new interpreter.

        Args:
            doc (TeXDocument): The parsed TeX document.
            rules (Rules): The rules to use on identifying the LTBs.
        """
        self.tex_document = doc
        self.rules = rules
        # The current hierarchy level.
        self.level = 0
        # The stack of unfinished blocks.
        self.stack = [LTB(level=self.level)]
        # The outline of finished blocks.
        self.outline = Outline()

    def identify_blocks(self):
        """
        Iterates through the elements of TeX document to identify logical text
        blocks using the given rules.
        """
        # Interpret the elements in given document.
        self._identify_blocks(self.tex_document)

        # Add the remaining blocks in the stack to the outline.
        while len(self.stack) > 0:
            block = self.stack.pop()
            if block.has_text():
                self.outline.append(block)

    def _identify_blocks(self, group):
        """
        Iterates through the elements of the given group to identify logical
        text blocks using the given rules.
        """
        # Iterate the elements in a shallow way (deeper traversals may be
        # defined by individual rules).
        itr = iterators.ShallowIterator(group.elements)
        for elem in itr:
            if isinstance(elem, tex_models.TeXWord):
                # Append text to the active block.
                self.append_text(elem.text)

            if isinstance(elem, tex_models.TeXGroup):
                # Interpret all elements of the group.
                self._identify_blocks(elem.elements)

            if isinstance(elem, tex_models.TeXCommand):
                # Interpret the command correspondingly to referring rule.
                # Obtain the referring rule.
                rule = self.rules.get_rule(elem)

                if rule is None:
                    # There is no such rule. Ignore the command.
                    if isinstance(elem, tex_models.TeXBeginEnvironmentCommand):
                        # If the command begins an environment, skip the entire
                        # environment.
                        itr.skip_to_element(elem.end_command)
                    continue

                # Process each single instruction given by the rule.
                for instruction in rule.get_instructions():
                    instruction.apply(self, itr, elem)

    # =========================================================================

    def introduce_block(self):
        """
        Appends a new block of current hierarchy level to stack of unfinished
        blocks.
        """
        block = LTB(level=self.level)
        self.stack.append(block)

    def finish_block(self):
        """
        Pops the topmost block from stack of unfinished blocks and adds it, if
        its text is non-empty, to the outline of finished blocks.
        """
        if len(self.stack) == 0:
            return
        block = self.stack.pop()
        if not block.has_text():
            return
        self.outline.append(block)

    def append_text(self, text):
        """
        Appends the given text to the topmost block in the stack of unfinished
        blocks.

        Args:
            text (str): The text to append.
        """
        if len(self.stack) == 0:
            return
        self.stack[-1].append_text(text)

    def set_hierarchy_level(self, level):
        """
        Sets the current hierarchy level to the given level.

        Args:
            level (int): The level to set.
        """
        self.level = level

    def set_semantic_role(self, role):
        """
        Sets the semantic role of topmost block in the stack of unfinished
        blocks.

        Args:
            role (str): The role to set.
        """
        if len(self.stack) == 0:
            return
        self.stack[-1].semantic_role = role
