from configparser import ConfigParser, ExtendedInterpolation

import models.instructions

from utils import file_utils
from utils.string_utils import split

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
        doc_class_filters.append("")

        # Compose the list of environment filters to check.
        env_filters = list(reversed(cmd.environments))
        env_filters.append("")

        # Check each doc_class / env combination and select the rule with most
        # specific matching key.
        for doc_class_filter in doc_class_filters:
            for env_filter in env_filters:
                key = "%s_%s_%s" % (identifier, doc_class_filter, env_filter)
                if key in self.rules:
                    return self.rules[key]
        return None

    @staticmethod
    def read_from_file(path):
        """
        Reads a config file with sections of form

        [instructions:set_level {0},set_role {1},finish_block,start_block]
        \section,doc_class,env: 1,${roles:heading}
        \subsection: 2,${roles:heading}

        where each section header (strings in [...]) starting with 
        "instructions:" define a instructions profile (a series of instructions
        which are valid for all subsequent options.
        An option is given by 
        (1) a key <identifier>[,<doc_class_filter>,[<env_filter>]] giving the
        identifier, the document class filter, and the environment of the 
        referred command.
        (2) a value consting of arguments to pass to the instruction profile in
        the section header, which may contain placeholders {0}, {1}, etc.

        Args:
            path (str): The string to the rules file.
        Returns:
            The created Rules object.
        """
        """
        Reads a config file with sections of form

        [instructions:set_level {0},set_role {1},finish_block,start_block]
        \section,doc_class,env: 1,${roles:heading}
        \subsection: 2,${roles:heading}

        where each section header (strings in [...]) starting with 
        "instructions:" define a instructions profile (a series of instructions
        which are valid for all subsequent options.
        An option is given by 
        (1) a key <identifier>[,<doc_class_filter>,[<env_filter>]] giving the
        identifier, the document class filter, and the environment of the 
        referred command.
        (2) a value consting of arguments to pass to the instruction profile in
        the section header, which may contain placeholders {0}, {1}, etc.

        Args:
            path (str): The string to the rules file.
        Returns:
            The created Rules object.
        """
        if file_utils.is_missing_or_empty_file(path):
            # Abort, because the rule file does not exist.
            raise ValueError("Config file '%s' does not exist." % path)

        rule_parser = ConfigParser(interpolation=ExtendedInterpolation())
        try:
            rule_parser.read(path)
        except:
            raise ValueError("Could not read config file '%s'." % path)

        rules = Rules()

        # Obtain some configs from rule files.
        rule_profiles_prefix = \
            rule_parser.get("configs", "rule_profiles_prefix")
        rule_profiles_header_delim = \
            rule_parser.get("configs", "rule_profiles_header_delim")
        delim = rule_parser.get("configs", "rule_profiles_field_delim")

        # Identify all rule profiles.
        for section in rule_parser.sections():
            # Ignore sections which do not represent a rule profile.
            if not section.startswith(rule_profiles_prefix):
                continue
            # Split the profile header into prefix and instructions pattern.
            _, instructions_pattern = section.split(rule_profiles_header_delim)

            # Iterate through the rules of the profile.
            for rule in rule_parser.options(section):
                # Obtain the identifier, doc_class and environment.
                identifier, doc_class, env = split(rule, delim, 3, "")
                # Obtain the arguments need to be passed to the pattern.
                args = rule_parser.get(section, rule).split(delim)
                # Pass the arguments to the pattern.
                instructions_str = instructions_pattern.format(*args)
                # Build a model for the instructions.
                instructs = models.instructions.from_string(instructions_str)
                rules.add_rule(Rule(identifier, doc_class, env, instructs))
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
        return "Rule(id: %s; filters: %s,%s; %s" % (self.identifier,
            self.doc_class_filter, self.env_filter, self.instructions)
