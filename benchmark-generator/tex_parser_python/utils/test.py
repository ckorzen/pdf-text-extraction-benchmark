import file_utils
from configparser import ConfigParser, ExtendedInterpolation


def read_config_file(path):
    """
    Reads the config file (config.ini) of the given tool.
    """
    if file_utils.is_missing_or_empty_file(path):
        raise ValueError("Config file '%s' does not exist." % path)

    config_parser = ConfigParser(interpolation=ExtendedInterpolation())
    try:
        config_parser.read(path)
        print(config_parser.get("instructions", "\\section"))
    except:
        raise ValueError("Could not read config file '%s'." % path)
    return Config(config_parser)


class Config:
    """
    A wrapper class for ConfigParser that allows to define a default value on
    getting a key/value pair, in case of the config does not hold a value for
    the given key.
    """

    def __init__(self, config_parser):
        """
        Creates a new Config object.
        """
        self.config_parser = config_parser

    def get_string(self, section, name, default=""):
        """
        Gets a string values from config.
        """
        if self.config_parser.has_option(section, name):
            return self.config_parser.get(section, name)
        else:
            return default

    def get_bool(self, section, name, default=False):
        """
        Gets a boolean values from config.
        """
        if self.config_parser.has_option(section, name):
            return self.config_parser.getboolean(section, name)
        else:
            return default

    def get_int(self, section, name, default=0):
        """
        Gets an integer values from config.
        """
        if self.config_parser.has_option(section, name):
            return self.config_parser.getint(section, name)
        else:
            return default

if __name__ == "__main__":
    config = read_config_file("/home/korzen/arxiv-benchmark/benchmark-generator/tex_parser_python/rules/default_rules.csv")
