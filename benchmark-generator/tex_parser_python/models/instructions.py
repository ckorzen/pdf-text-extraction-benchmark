import re

ARG_REGEX = re.compile("{ARG(?P<num>\\d)}")
OPT_REGEX = re.compile("{OPT(?P<num>\\d)}")


class Instruction:
    """
    The base class of an instruction, where an instruction defines one or more
    actions that affects the identification of logical text blocks.
    """

    @staticmethod
    def from_string(string):
        """
        Parses the given string, representing a (serialized) instruction and
        creates the related Instruction object, initialized with related
        arguments.

        The string is of form <name> <args>*, where <name> is an unique name
        referring to the instruction and <args> is a list of arguments to be
        passed to the instruction, for example:

        "set_level 1" represents the instruction SetHierarchyLevel with
            argument "1".
        "set_role heading" represents the instruction SetSemanticRole with
            argument "heading".
        etc.

        Args:
            instruction_str (str): The string to parse.
        Returns:
            The created Instruction object.
        """
        # Split the substring into <name> and <args>
        values = string.split(" ")
        name = values[0]
        args = values[1:]
        if name not in instructions_index:
            raise ValueError("'%s' is not a valid instruction." % string)
        return instructions_index[name](*args)

    @staticmethod
    def get_name():
        """
        Returns the name of this instruction to use in rules file in order to
        refer to this instruction.
        """
        pass

    def apply(self, interpreter, itr, element, context):
        """
        Defines the action(s) to execute on calling this instruction.

        Args:
            interpreter (LTBIdentifier): The used interpreter.
            itr (ShallowIterator): The iterator instance that is used to
                iterate through the TeX elements on identifying the LTBs.
            element (TeXElement): The current TeX element that caused the call
                this of this instruction.
            context (Context): The current context.
        """
        pass

    def interpolate_arg(self, arg, cmd, interpreter):
        """
        Interpolates related values for {ARG<i>} and {OPT<i>} placeholders,
        where <i> is a number 0-9.

        Args:
            arg (str): The argument to process.
            cmd (TeXCommand): The related command.
            interpreter (TeXInterpreter): The interpreter.
        """
        # Interpolate ARG placeholders.
        arg_matches = ARG_REGEX.finditer(arg)
        for m in arg_matches:
            num = int(m.group("num"))
            text = str(interpreter.identify_blocks(cmd.get_arg(num).elements))
            arg = arg[:m.start()] + text + arg[m.end():]
        # Interpolate OPT placeholders.
        opt_matches = OPT_REGEX.finditer(arg)
        for m in opt_matches:
            num = int(m.group("num"))
            text = str(interpreter.identify_blocks(cmd.get_opt(num).elements))
            arg = arg[:m.start()] + text + arg[m.end():]
        return arg


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
    def get_name():
        return "skip_to"

    # Override
    def apply(self, interpreter, itr, element, context):
        itr.skip_to(self.target)


class SetHierarchyLevel(Instruction):
    """
    The instruction to set the current hierarchy level.
    """
    def __init__(self, level):
        """
        Creates a new SetHierarchyLevel instruction.

        Args:
            level (str): The hierarchy level to set.
        """
        self.level = int(level)

    @staticmethod
    def get_name():
        return "set_level"

    # Override
    def apply(self, interpreter, itr, element, context):
        context.set_hierarchy_level(self.level)


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
    def get_name():
        return "set_role"

    # Override
    def apply(self, interpreter, itr, element, context):
        context.set_semantic_role(self.role)


class AppendTextPhrase(Instruction):
    """
    The instruction to append a text phrase to the current block.
    """
    def __init__(self, *word):
        """
        Creates a new AppendTextPhrase instruction.

        Args:
            *word (str): A word to append.
        """
        self.text = " ".join(word)

    @staticmethod
    def get_name():
        return "append_text"

    # Override
    def apply(self, interpreter, itr, element, context):
        context.append_text(self.text)


class VisitArg(Instruction):
    """
    The instruction to process the elements of an argument of a command.
    """
    def __init__(self, index):
        """
        Creates a new VisitArg instruction.

        Args:
            index (str): The index of argument to visit.
        """
        self.index = int(index)

    @staticmethod
    def get_name():
        return "visit_arg"

    # Override
    def apply(self, interpreter, itr, element, context):
        if len(element.args) > self.index:
            interpreter._identify_blocks(element.args[self.index].elements, context)


class StartBlock(Instruction):
    """
    The instruction to introduce a new Logical Text Block.
    """
    @staticmethod
    def get_name():
        return "start_block"

    # Override
    def apply(self, interpreter, itr, element, context):
        context.introduce_block()


class FinishBlock(Instruction):
    """
    The instruction to finish the current Logical Text Block.
    """
    @staticmethod
    def get_name():
        return "finish_block"

    # Override
    def apply(self, interpreter, itr, element, context):
        context.finish_block()


class RegisterWhitespace(Instruction):
    """
    The instruction to register a whitespace to the current Logical Text Block.
    """
    @staticmethod
    def get_name():
        return "register_whitespace"

    # Override
    def apply(self, interpreter, itr, element, context):
        context.register_whitespace()


class SetDocumentClass(Instruction):
    """
    The instruction to set the documentclass of the TeX document.
    """
    def __init__(self, doc_class):
        """
        Creates a new SetDocumentClass instruction.

        Args:
            doc_class: The document class to set.
        """
        self.doc_class = doc_class

    @staticmethod
    def get_name():
        return "set_document_class"

    # Override
    def apply(self, interpreter, itr, element, context):
        doc_class = self.interpolate_arg(self.doc_class, element, interpreter)
        interpreter.doc.document_class = doc_class


class BeginEnvironment(Instruction):
    """
    The instruction to begin an environment.
    """
    def __init__(self, environment):
        """
        Creates a new BeginEnvironment instruction.

        Args:
            environment (str): The name of environment.
        """
        self.environment = environment

    @staticmethod
    def get_name():
        return "begin_environment"

    # Override
    def apply(self, interpreter, itr, element, context):
        environment = self.interpolate_arg(self.environment, element, interpreter)
        context.begin_environment(environment)


class EndEnvironment(Instruction):
    """
    The instruction to end an environment.
    """
    def __init__(self, environment):
        """
        Creates a new EndEnvironment instruction.

        Args:
            environment (str): The name of environment.
        """
        self.environment = environment

    @staticmethod
    def get_name():
        return "end_environment"

    # Override
    def apply(self, interpreter, itr, element, context):
        environment = self.interpolate_arg(self.environment, element, interpreter)
        context.end_environment(environment)


class SetMetadata(Instruction):
    """
    The instruction to set a metadata to the TeX document.
    """
    def __init__(self, key, value):
        """
        Creates a new SetMetadata instruction.

        Args:
            key: The key of the metadata field to set.
            value: The value of the metadata field to set.
        """
        self.key = key
        self.value = value

    @staticmethod
    def get_name():
        return "set_metadata"

    # Override
    def apply(self, interpreter, itr, element, context):
        return


# Create an index that maps the available names of instructions to the related
# constructors: { "append_text": AppendTextPhrase, "skip_to": SkipTo, ... }
instructions_index = {x.get_name(): x for x in Instruction.__subclasses__()}
