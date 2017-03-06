import models.instructions


class Rules:
    """
    A class representing a dictionary of rules, defining instructions to
    execute on seeing a specific command.
    """
    def __init__(self):
        """
        Constructs a new dictionary of rules.
        """
        self.rules = {}

    def add_rule(self, rule):
        """
        Adds the given rule to this dictionary with a key of form
        <identifier>_<doc_class>_<environment> in order to enable selective
        rules.

        <identifier> is the identifier of the element, referred by the rule.
        <doc_class> is the document class filter of the rule that can be used
            to restrict the rule to elements that only occur in documents with
            given document class.
        <environment> is the environment filter of the rule that can be used
            to restrict the rule to elements that only occur in given
            environment.
        For example, a rule with identifier "\\footnote", document class filter
        "revtex" and environment filter "table" is indexed with key
        "\\footnote_revtex_table".

        In case of there is no document class filter and/or environment filter
        for the given rule, the related placeholder is replaces by None.
        For example, a rule with identifier "\\footnote" and not document class
        filter and no environment filter is indexed with key
        "\\footnote_None_None".

        Args:
            rule (Rule): The rule to add.
        """

        # Compose the key.
        key = "%s_%s_%s" % (
            rule.get_identifier(),
            rule.get_document_class_filter(),
            rule.get_environment_filter()
        )
        # Add the rule to dictionary.
        self.rules[key] = rule

    def get_rule(self, el):
        """
        Checks if this dictionary of rules contains a rule for the given
        element.

        On searching for a referring rule in this dictionary, the rule with the
        "most specific matching" filters is selected.

        For example, consider a dictionary of rules with keys

        \\footnote_revtex_table
        \\footnote_revtex_None
        \\footnote_None_None

        For an element with identifier "\\footnote" that lives within a
        document with document class "revtex" and within an environment
        "table", the rule with key \\footnote_revtex_table would be selected,
        because its filters are the most specific matching ones.
        Apart, for an element with identifier "\\footnote" and document class
        "sigalternate", the rule with key \\footnote_None_None would be
        selected.

        Args:
            el (TeXElement): The TeX element for which a rule has to be found.
        Returns:
            A rule, defining the instructions to execute on seeing the given
            element; or None if this dictionary does not contain such rule for
            the given element.
        """

        # Compose the list of document class filters to check.
        doc_class_filters = []
        if el.document.document_class is not None:
            doc_class_filters.append(el.document.document_class)
        doc_class_filters.append("")

        # Compose the list of environment filters to check.
        env_filters = list(reversed(el.environments))
        env_filters.append("")

        # Check each doc_class / env combination and select the rule with most
        # specific matching key.
        for doc_class_filter in doc_class_filters:
            for env_filter in env_filters:
                key = "%s_%s_%s" % (el.cmd_name, doc_class_filter, env_filter)
                if key in self.rules:
                    return self.rules[key]
        return None

    @staticmethod
    def read_from_file(path):
        """
        Reads the file given by path containing rules and constructs a related
        Rules object.

        Args:
            path (str): The string to the rules file.
        Returns:
            The created Rules object.
        """
        rules = Rules()
        with open(path) as f:
            for line in f.read().splitlines():
                if len(line.strip()) == 0:
                    # Skip empty lines.
                    continue
                if line.strip().startswith('#'):
                    # Skip comment lines.
                    continue
                # Construct a rule from line and append it to Rules object.
                rules.add_rule(Rule.from_string(line))
        return rules

    def __str__(self):
        return "\n".join(["%s: %s" % (x, self.rules[x]) for x in self.rules])


class Rule:
    """
    A class representing a single rule that defines a series of instructions
    to execute on seeing a specific TeX command.
    """
    def __init__(self, identifier, doc_class_filter=None, env_filter=None,
                 instructions=[]):
        """
        Creates a new rule.

        Args:
            identifier (str): The identifier of referred command.
            doc_class_filter (str, otional): The document class filter.
            env_filter (str, optional): The environment filter.
            instructions (list of Instruction, optional): The instructions to
                execute.
        """
        self.identifier = identifier
        self.doc_class_filter = doc_class_filter
        self.env_filter = env_filter
        self.instructions = instructions

    @staticmethod
    def from_string(string):
        """
        Creates a new rule from given string of form:
        <identifier>,<doc_class_filter>,<env_filter>:<instruction>*

        Args:
            string (str): The string representing a single rule.
        """
        cmd_description, instructions_str = string.split(":")
        identifier, doc_class_filter, env_filter = cmd_description.split(",")
        instructions = models.instructions.from_string(instructions_str)
        return Rule(identifier, doc_class_filter, env_filter, instructions)

    def get_identifier(self):
        """
        Returns the identifier of referred command.

        Returns:
            The identifier of referred command.
        """
        return self.identifier

    def get_document_class_filter(self):
        """
        Returns the document class filter.

        Returns:
            The document class filter.
        """
        return self.doc_class_filter

    def get_environment_filter(self):
        """
        Returns the environment filter.

        Returns:
            The environment filter.
        """
        return self.env_filter

    def get_instructions(self):
        """
        Returns the instructions to execute.

        Returns:
            The instructions.
        """
        return self.instructions
