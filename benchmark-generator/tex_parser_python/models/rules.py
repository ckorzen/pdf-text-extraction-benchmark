import models.instructions


class Rules:
    """
    A collection of rules that define how to interpret TeX elements.
    """
    def __init__(self):
        """
        Constructs a new rules collection.
        """
        self.rules = {}

    def add_rule(self, rule):
        """
        Adds the given rule to this collection.
        """

        # Do not add non-rules.
        if not isinstance(rule, Rule):
            return

        # Use a composed key to get selective rules.
        key = "%s_%s_%s" % (
            rule.get_identifier(),
            rule.get_document_class_filter(),
            rule.get_environment_filter()
        )
        self.rules[key] = rule

    def get_rule(self, el):
        """
        Returns the rule refering the the given TeX element.
        """

        # Obtain the most specific matching rule.
        # TODO
        key = "%s_%s_%s" % (el.command_name, "", "")
        if key in self.rules:
            return self.rules[key]

#        key = "%s_%s_%s" % (el.command_name, None, None)
#        if key in self.rules:
#            return self.rules[key]

#        key = "%s_%s_%s" % (el.command_name, None, el.environment)
#        if key in self.rules:
#            return self.rules[key]

#        key = "%s_%s_%s" % (el.command_name, el.document_class, None)
#        if key in self.rules:
#            return self.rules[key]

#        key = "%s_%s_%s" % (el.command_name, None, None)
#        if key in self.rules:
#            return self.rules[key]

        return None

    @staticmethod
    def read_from_file(path):
        """
        Reads the collection of rules from given file path.
        """
        rules = Rules()
        with open(path) as f:
            for line in f.read().splitlines():
                if len(line.strip()) == 0:
                    # Skip emtpy lines.
                    continue
                if line.strip().startswith('#'):
                    # Skip comment lines.
                    continue
                rules.add_rule(Rule.from_string(line))
        return rules

    def __str__(self):
        return "\n".join(["%s: %s" % (x, self.rules[x]) for x in self.rules])


class Rule:
    """
    A single rule.
    """
    def __init__(self, identifier, doc_class_filter, env_filter, instructions):
        self.identifier = identifier
        self.doc_class_filter = doc_class_filter
        self.env_filter = env_filter
        self.instructions = instructions

    def get_identifier(self):
        return self.identifier

    def get_document_class_filter(self):
        return self.doc_class_filter

    def get_environment_filter(self):
        return self.env_filter

    def get_instructions(self):
        return self.instructions

    @staticmethod
    def from_string(string):
        cmd_description, instructions_str = string.split(":")
        identifier, doc_class_filter, env_filter = cmd_description.split(",")
        instructions = models.instructions.from_string(instructions_str)
        return Rule(identifier, doc_class_filter, env_filter, instructions)
