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
        key = "%s_%s_%s" % (el.command_name, None, None)
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
    def read_from_file(path, delimiter=","):
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
                values = line.split(delimiter)
                rules.add_rule(Rule(values))
        return rules

    def __str__(self):
        return "\n".join(["%s: %s" % (x, self.rules[x]) for x in self.rules])


class Rule:
    """
    A single rule.
    """
    def __init__(self, values):
        self.values = values

    def get_identifier(self, default=None):
        return self.get_string(0, default)

    def get_end_command(self, default=None):
        return self.get_string(1, default)

    def get_document_class_filter(self, default=None):
        return self.get_string(2, default)

    def get_environment_filter(self, default=None):
        return self.get_string(3, default)

    def get_text_phrase(self, default=None):
        return self.get_string(4, default)

    def get_starts_ltb_type(self, default=None):
        return self.get_int(5, default)

    def get_ends_ltb_type(self, default=None):
        return self.get_int(6, default)

    def get_semantic_role(self, default=None):
        return self.get_string(7, default)

    def get_hierarchy_level(self, default=None):
        return self.get_int(8, default)

    def get_args_to_visit(self, default=None):
        return self.get_int_list(9, delim=" ", default=default)

    # =========================================================================

    def get_string(self, index, default=None):
        if index < 0 or index >= len(self.values):
            return default
        return self.values[index] if len(self.values[index]) > 0 else default

    def get_int(self, index, default=None):
        if index < 0 or index >= len(self.values):
            return default
        try:
            return int(self.values[index])
        except ValueError:
            return default

    def get_int_list(self, index, delim=" ", default=None):
        if index < 0 or index >= len(self.values):
            return default
        try:
            return [int(x) for x in self.values[index].split(delim)]
        except ValueError:
            return default

    # =========================================================================

    def __str__(self):
        return ", ".join(self.values)
