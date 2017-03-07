def from_string(instructions_str):
    """
    Parses the given string, representing a series of (serialized) instructions
    and returns a list of related Instruction objects.

    The string contains comma-separated substrings, where each substring
    represents a single instruction. A substring is of form <name> <args>*,
    where <name> is an unique name referring to the instruction and <args> is a
    list of arguments to be passed to the instruction, for example:
    set_level 1
    set_role heading
    append_text [formula]
    start_block
    etc.

    Args:
        instruction_str (str): The string to parse.
    Returns:
        The list of related Instruction objects.
    """
    # Create an index that maps the available names of instructions to the
    # related constructors:
    # { "append_text": AppendTextPhrase, "skip_to": SkipTo, ... }
    index = {x.get_name(): x for x in Instruction.__subclasses__()}
    instructions = []
    # Split the string into its substrings.
    instructions_str_list = instructions_str.split(",")
    for instruction_str in instructions_str_list:
        # Split the substring into <name> and <args>
        values = instruction_str.split(" ")
        name = values[0]
        args = values[1:]
        if name in index:
            # Create the related Instruction object and append it to list.
            instructions.append(index[name].from_string(args))
    return instructions


class Instruction:
    """
    The super class of an instruction. An instruction defines a specific action
    to execute on identifying logical text blocks.
    """

    @staticmethod
    def get_name():
        """
        Returns the name of this instruction to use in rules file in order to
        refer to this instruction.
        """
        pass

    @staticmethod
    def from_string(args):
        """
        Creates a new instruction from the given args (given as str).

        Args:
            args (str): The arguments for this instruction.
        """
        pass

    def apply(self, interpreter, itr, element):
        """
        Defines the action to execute for this instruction.

        Args:
            interpreter (TeXInterpreter): The interpreter instance that
                identifies the LTBs.
            itr (ShallowIterator): The iterator instance that is used to
                iterate through the TeX elements on identifying the LTBs.
            element (TeXElement): The current TeX element that caused this
                instruction.
        """
        pass


class SkipTo(Instruction):
    """
    The instruction to skip to a given target element.
    """

    def __init__(self, target):
        """
        Creates a new SkipTo instruction.

        Args:
            target (str): The target element where to skip to.
        """
        self.target = target

    @staticmethod
    def from_string(args):
        """
        Creates a new SkipTo instruction from the given args (given as str).

        Args:
            args (str): The skip target.
        """
        return SkipTo(args[0])

    @staticmethod
    def get_name():
        return "skip_to"

    def apply(self, interpreter, itr, element):
        itr.skip_to(self.target)


class SetHierarchyLevel(Instruction):
    """
    The instruction to set the current hierarchy level.
    """

    def __init__(self, level):
        """
        Creates a new SetHierarchyLevel instruction.

        Args:
            level (int): The hierarchy level to set.
        """
        self.level = level

    @staticmethod
    def from_string(args):
        """
        Creates a new SetHierarchyLevel instruction from the given args
        (given as str).

        Args:
            args (str): The hierarchy level.
        """
        return SetHierarchyLevel(int(args[0]))

    @staticmethod
    def get_name():
        return "set_level"

    def apply(self, interpreter, itr, element):
        interpreter.set_hierarchy_level(self.level)


class SetSemanticRole(Instruction):
    """
    The instruction to set the semantic role of the current block.
    """

    def __init__(self, role):
        """
        Creates a new SetSemanticRole instruction.

        Args:
            role (str): The semantic role to set.
        """
        self.role = role

    @staticmethod
    def from_string(args):
        """
        Creates a new SetSemanticRole instruction from the given args
        (given as str).

        Args:
            args (str): The semantic role.
        """
        return SetSemanticRole(args[0])

    @staticmethod
    def get_name():
        return "set_role"

    def apply(self, interpreter, itr, element):
        interpreter.set_semantic_role(self.role)


class AppendTextPhrase(Instruction):
    """
    The instruction to append a text phrase to the current block.
    """

    def __init__(self, text):
        """
        Creates a new AppendTextPhrase instruction.

        Args:
            text (str): The text phrase to append.
        """
        self.text = text

    @staticmethod
    def from_string(args):
        """
        Creates a new AppendTextPhrase instruction from the given args
        (given as str).

        Args:
            args (str): The text phrase to append.
        """
        return AppendTextPhrase(args[0])

    @staticmethod
    def get_name():
        return "append_text"

    def apply(self, interpreter, itr, element):
        interpreter.append_text(self.text)


class VisitArg(Instruction):
    """
    The instruction to process the elements of an argument of a command.
    """

    def __init__(self, index):
        """
        Creates a new VisitArg instruction.

        Args:
            index (int): The index of argument to visit.
        """
        self.index = index

    @staticmethod
    def from_string(args):
        """
        Creates a new VisitArg instruction from the given args (given as str).

        Args:
            args (str): The index of argument to visit.
        """
        return VisitArg(int(args[0]))

    @staticmethod
    def get_name():
        return "visit_arg"

    def apply(self, interpreter, itr, element):
        if len(element.args) > self.index:
            interpreter._identify_blocks(element.args[self.index])


class StartBlock(Instruction):
    """
    The instruction to introduce a new Logical Text Block.
    """

    @staticmethod
    def from_string(args):
        """
        Creates a new StartBlock instruction.

        Args:
            args (str): Ignored in this instruction.
        """
        return StartBlock()

    @staticmethod
    def get_name():
        return "start_block"

    def apply(self, interpreter, itr, element):
        interpreter.introduce_block()


class FinishBlock(Instruction):
    """
    The instruction to finish the current Logical Text Block.
    """

    @staticmethod
    def from_string(args):
        """
        Creates a new FinishBlock instruction.

        Args:
            args (str): Ignored in this instruction.
        """
        return FinishBlock()

    @staticmethod
    def get_name():
        return "finish_block"

    def apply(self, interpreter, itr, element):
        interpreter.finish_block()


class RegisterWhitespace(Instruction):
    """
    The instruction to register a whitespace to the current Logical Text Block.
    """

    @staticmethod
    def from_string(args):
        """
        Creates a new RegisterWhitespace instruction.

        Args:
            args (str): Ignored in this instruction.
        """
        return RegisterWhitespace()

    @staticmethod
    def get_name():
        return "register_whitespace"

    def apply(self, interpreter, itr, element):
        interpreter.stack[-1].register_whitespace()
