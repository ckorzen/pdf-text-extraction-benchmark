import re

from configparser import ConfigParser, ExtendedInterpolation

from utils import file_utils
from utils import string_utils
from models.instructions import Instruction


class Rules(dict):
    """
    A class representing a dictionary of rules, where a rule defines
    instructions to execute on for a specific command.
    """

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
        self[key] = rule

    def get_rule(self, cmd):
        """
        Checks if this dictionary of rules contains a rule for the given
        command.

        On searching for a referring rule in this dictionary, the rule with the
        "most specific matching" filters is selected.

        For example, consider a dictionary of rules with keys

        \\footnote_revtex_table
        \\footnote_revtex_None
        \\footnote_None_None

        For an command with identifier "\\footnote" that lives within a
        document with document class "revtex" and within an environment
        "table", the rule with key \\footnote_revtex_table would be selected,
        because its filters are the most specific matching ones.
        Apart, for an command with identifier "\\footnote" and document class
        "sigalternate", the rule with key \\footnote_None_None would be
        selected.

        Args:
            cmd (TeXCommand): The command for which a rule has to be found.
        Returns:
            A rule, defining the instructions to execute on seeing the given
            element; or None if this dictionary does not contain such rule for
            the given element.
        """

        # Obtain the identifier of the command.
        identifier = cmd.get_identifier()

        # Compose the list of document class filters to check.
        doc_class_filters = []
        if cmd.document.document_class is not None:
            doc_class_filters.append(cmd.document.document_class)
        doc_class_filters.append(None)

        # Compose the list of environment filters to check.
        env_filters = list(reversed(cmd.environments))
        env_filters.append(None)

        # Check each doc_class / env combination and select the rule with most
        # specific matching key.
        for doc_class_filter in doc_class_filters:
            for env_filter in env_filters:
                key = "%s_%s_%s" % (identifier, doc_class_filter, env_filter)
                if key in self:
                    return self[key]
        return None

    @staticmethod
    def read_from_file(path):
        return RuleFileParser().parse_rules(path)


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

    def __str__(self):
        return "Rule(id: %s; filters: %s,%s; %s" \
            % (self.identifier, self.doc_class_filter, self.env_filter,
               self.instructions)


class RuleFileParser(ConfigParser):
    """
    A parser to read a rules file.
    """

    rules_section = "rules"

    def __init__(self):
        """
        Creates a new rule parser.
        """
        super().__init__(interpolation=ExtendedInterpolation())
        # Dirty hack to allow escaped delimiters in keys. For example, we
        # would like to be able to write "\=" in a key, which should not
        # considered as an delimiter (because it is preceded by an "\").
        # The hack is to manipulate the regex that is used by ConfigParser to
        # check for delimiters.
        template = self._OPT_TMPL.format(delim="(?<!\\\\)=")
        self._optcre = re.compile(template, re.VERBOSE)

    def parse_rules(self, path):
        """
        Parses a rules file given by path and returns a related Rules object.

        Args:
            path (str): The path to the rules file to parse.
        Returns:
            The created Rules object.
        """
        if file_utils.is_missing_or_empty_file(path):
            # Raise an error because the rule file does not exist.
            raise ValueError("Rule file '%s' does not exist." % path)

        # Read the file.
        self.read(path)

        rules = Rules()
        for key, value in self.rule_items():
            # Split the key into identifier, doc_class_filter and env_filter.
            identifier, doc_class, env = string_utils.split(key, ",", 3)

            # Split the value into instructions pattern and arguments.
            instructions_str, args_str = string_utils.split(value, ";", 2, "")

            # Split the arguments into list of individual arguments.
            args_str_list = args_str.split(",")

            # Pass the arguments to the instructions pattern.
            resolved_instructions_str = instructions_str.format(*args_str_list)

            # Split the instructions into list of individual instructions.
            instructions_str_list = resolved_instructions_str.split(",")

            # Compose the list of Instruction objects.
            instructions = []
            for instruct_str in instructions_str_list:
                instructions.append(Instruction.from_string(instruct_str))

            # Compose the Rule object and add it to Rules object.
            rule = Rule(identifier, doc_class, env, instructions)
            rules.add_rule(rule)
        return rules

    def options(self, section):
        """
        Overrides the options() method in order to support leading and
        trailing whitespaces in option names.
        """
        options = ConfigParser.options(self, section)
        return [self.unwrap_quotes(x) for x in options]

    def has_option(self, section, option):
        """
        Overrides the has_option() method in order to support leading and
        trailing whitespaces in option names.
        """
        value = ConfigParser.has_option(self, section, option)
        if value:
            return True
        option = self.wrap_in_quotes(option)
        return ConfigParser.has_option(self, section, option)

    def optionxform(self, optionstr):
        """
        Overrides the optionxform() method in order to avoid the lowercasing
        of option names.
        """
        return optionstr

    def rule_items(self):
        """
        Returns a list of tuples with (name, value) for each option in the
        rules section.

        Returns:
            A list of tuples with (name, value) for each option in the rules
            section.
        """
        rule_items = []
        if self.has_section(self.rules_section):
            for key in self.options(self.rules_section):
                value = self.get(self.rules_section, key)
                if value is not None:
                    rule_items.append((key, value))
        return rule_items

    def get(self, section, option, raw=False, vars=None, fallback=""):
        """
        Overrides the get() method in order to support leading and
        trailing whitespaces in option and section names.
        """
        value = ConfigParser.get(
            self, section, option, raw=raw, vars=vars, fallback=None)
        if value is not None:
            return self.unwrap_quotes(value)

        option = self.wrap_in_quotes(option)
        value = ConfigParser.get(
            self, section, option, raw=raw, vars=vars, fallback=None)
        if value is not None:
            return self.unwrap_quotes(value)

    def unwrap_quotes(self, string):
        """
        Removes trailing and leading quotes if the string starts and ends with
        quotes.
        """
        if string is None:
            return None

        for quote in ['"', "'"]:
            if string.startswith(quote) and string.endswith(quote):
                return string.strip(quote)
        return string

    def wrap_in_quotes(self, string):
        """
        Removes trailing and leading quotes if the string starts and ends with
        quotes.
        """
        if string is None:
            return None

        for quote in ['"', "'"]:
            if string.startswith(quote) and string.endswith(quote):
                return string
        return '"%s"' % string
