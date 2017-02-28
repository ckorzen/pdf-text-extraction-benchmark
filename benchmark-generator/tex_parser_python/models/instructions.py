def from_string(instructions_str):
    index = {x.get_name(): x for x in Instruction.__subclasses__()}
    instructions = []
    instructions_str_list = instructions_str.split(",")
    for instruction_str in instructions_str_list:
        values = instruction_str.split(" ")
        name = values[0]
        args = values[1:]
        if name in index:
            instructions.append(index[name].from_string(args))
    return instructions


class Instruction:
    pass


class SkipTo(Instruction):
    def __init__(self, target):
        self.target = target

    @staticmethod
    def from_string(args):
        return SkipTo(args[0])

    @staticmethod
    def get_name():
        return "skip_to"

    def apply(self, interpreter, element):
        # TODO
        pass


class SetHierarchyLevel(Instruction):
    def __init__(self, level):
        self.level = level

    @staticmethod
    def from_string(args):
        return SetHierarchyLevel(int(args[0]))

    @staticmethod
    def get_name():
        return "set_level"

    def apply(self, interpreter, element):
        interpreter.set_hierarchy_level(self.level)


class SetSemanticRole(Instruction):
    def __init__(self, role):
        self.role = role

    @staticmethod
    def from_string(args):
        return SetSemanticRole(args[0])

    @staticmethod
    def get_name():
        return "set_role"

    def apply(self, interpreter, element):
        interpreter.set_semantic_role(self.role)


class AppendTextPhrase(Instruction):
    name = "append_text"

    def __init__(self, text):
        self.text = text

    @staticmethod
    def from_string(args):
        return AppendTextPhrase(args[0])

    @staticmethod
    def get_name():
        return "append_text"

    def apply(self, interpreter, element):
        interpreter.append_text(self.text)


class VisitArg(Instruction):
    def __init__(self, index):
        self.index = index

    @staticmethod
    def from_string(args):
        return VisitArg(int(args[0]))

    @staticmethod
    def get_name():
        return "visit_arg"

    def apply(self, interpreter, element):
        if len(element.args) > self.index:
            interpreter._identify_blocks(element.args[self.index])


class StartBlock(Instruction):
    @staticmethod
    def from_string(args):
        return StartBlock()

    @staticmethod
    def get_name():
        return "start_block"

    def apply(self, interpreter, element):
        interpreter.introduce_block()


class FinishBlock(Instruction):
    @staticmethod
    def from_string(args):
        return FinishBlock()

    @staticmethod
    def get_name():
        return "finish_block"

    def apply(self, interpreter, element):
        interpreter.finish_block()
