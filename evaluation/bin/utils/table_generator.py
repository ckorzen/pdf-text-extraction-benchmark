import argparse
import sys
import os.path

CWD = os.path.dirname(os.path.realpath(__file__))
sys.path.insert(0, os.path.abspath(os.path.join(CWD, "..")))

import evaluator  # NOQA
from evaluator import EvaluationResult  # Needed to satifies pickle.  # NOQA

# Some tool data. TODO: Read it from config.ini
tool_data = {
    # (name, runtime in ms)
    "pdftotext": ("pdftotext", 330),
    "pdftohtml": ("pdftohtml", 2237),
    "pdftoxml": ("pdftoxml", 653),
    "pdf2xml": ("pdf2xml", 36694),
    "pdfbox": ("PdfBox", 8794),
    "parscit": ("ParsCit", 6752),
    "lapdftext": ("LA-PdfText", 23627),
    "pdfminer": ("PdfMiner", 16434),
    "pdfXtk": ("pdfXtk", 22281),
    "pdfx": ("pdfx", -1),
    "pdfextract": ("pdf-extract", 33969),
    "PDFExtract": ("PDFExtract", 46173),
    "grobid": ("Grobid", 41655),
    "icecite": ("Icecite", 41379)
}

criterions_order = [
    "num_para_merges",
    "num_para_splits",
    "num_para_deletes",
    "num_para_inserts",
    "num_para_rearranges",
    "num_word_deletes",
    "num_word_inserts",
    "num_word_replaces",
]


class TableGenerator:
    """
    A super class to generate specific tables (for paper) from evaluation
    results.
    """

    def __init__(self, args, evaluation_results):
        """
        The default constructor of table generator.
        """
        self.args = args
        self.evaluation_results = evaluation_results
        self.criterion_nums = None
        self.num_errors = None

    def generate(self):
        """
        Generates a table.
        """
        header = self.generate_table_header()
        body = self.generate_table_body()
        footer = self.generate_table_footer()
        return "\n".join([header, body, footer])

    def generate_table_header(self):
        """
        Generates the table header.
        """
        return ""

    def generate_table_body(self):
        """
        Generates the table body.
        """
        rows = []
        for tool in self.args.tool_names:
            rows.append(self.generate_table_row(tool))
        return "\n".join(rows)

    def generate_table_row(self, tool):
        """
        Generates a row in the table body.
        """
        if tool not in evaluation_results:
            return ""

        cells = []
        cells.append(self.format_prefix_cell(tool))

        # Add the cells for criterions.
        for criterion in criterions_order:
            num, num_rel, is_best = self.get_criterion_nums(tool, criterion)
            cells.append(self.format_criterion_cell(num, num_rel, is_best))

        # Add the cell for num errors.
        num, is_best = self.get_num_errors(tool)
        cells.append(self.format_num_errors_cell(num, is_best))

        # Add the cell for avg. runtime.
        num, is_best = self.get_avg_runtime(tool)
        cells.append(self.format_avg_runtime_cell(num, is_best))

        return self.get_cell_separator().join(cells)

    def generate_table_footer(self):
        """
        Generates the table footer.
        """
        return ""

    # =========================================================================

    def get_criterion_nums(self, tool, criter):
        """
        Fetches the nums for the given criterion from results of given tool.
        """
        if self.criterion_nums is None:
            self.criterion_nums = {}

        if criter not in self.criterion_nums:
            self.criterion_nums[criter] = self.compute_criterion_nums(criter)

        min_num = min([x[0] for x in self.criterion_nums[criter].values()])
        num, num_rel = self.criterion_nums[criter][tool]

        return self.format_num(num), self.format_num(num_rel), num == min_num

    def compute_criterion_nums(self, criterion):
        """
        Computes the nums for the given criterion.
        """
        nums = {}
        for tool in self.evaluation_results:
            result = self.evaluation_results[tool]
            num_ops = getattr(result, "num_ops", None)
            num_ops_rel = getattr(result, "num_ops_rel", None)

            num = num_ops[criterion] if num_ops is not None else 0
            num_rel = num_ops_rel[criterion] if num_ops_rel is not None else 0

            nums[tool] = (num, num_rel * 100)
        return nums

    # =========================================================================

    def get_num_errors(self, tool):
        """
        Fetches the number of errors caused by the given tool.
        """
        if self.num_errors is None:
            self.num_errors = self.compute_num_errors()

        min_num_errors = min(self.num_errors.values())
        num_errors = self.num_errors.get(tool, -1)

        return self.format_num(num_errors), num_errors == min_num_errors

    def compute_num_errors(self):
        """
        Computes the nums of errors.
        """
        num_errors_dict = {}
        # Compute the number of errors for each tool.
        for tool in self.evaluation_results:
            result = self.evaluation_results[tool]
            error_codes = getattr(result, "error_codes", None)

            # Get number of errors caused by the tool.
            num_errors = 0
            if error_codes is not None:
                for i in range(10, 20):  # Missing or empty tool files.
                    num_errors += error_codes[i]
                num_errors += error_codes[99]  # Timeout on extraction.
            num_errors_dict[tool] = num_errors
        return num_errors_dict

    # =========================================================================

    def get_avg_runtime(self, tool):
        """
        Fetches the average runtime needed per PDF by the given tool.
        """
        if tool not in tool_data:
            return -1
        _, avg_runtime = tool_data[tool]

        min_runtime = min([x[1] for x in tool_data.values() if x[1] > 0])

        return self.format_num(avg_runtime / 1000), avg_runtime == min_runtime

    # =========================================================================

    def format_prefix_cell(self, tool):
        """
        Formats the cell left to the criterion cells (the first cell).
        """
        return ""

    def format_criterion_cell(self, num, num_rel, is_best):
        """
        Formats a cell containing the nums of a criterion.
        """
        return "%s %s" % (num, num_rel)

    def format_num_errors_cell(self, num, is_best):
        """
        Formats a cell containing the number of errors.
        """
        return "%s" % num

    def format_avg_runtime_cell(self, num, is_best):
        """
        Formats a cell containing the average runtime.
        """
        return "%s" % num

    def get_cell_separator(self):
        """
        Returns the separator to be used on joining the cells to a row.
        """
        return " "

    # =========================================================================

    def format_num(self, num):
        """
        Formats the given number. The number is rounded to next integer if
        it is larger or equal to 10 or is cut to one decimal point otherwise.
        """
        if isinstance(num, int) or num >= 10:
            return "%d" % round(num)
        else:
            return "%.1f" % num


class TeXTableGenerator(TableGenerator):
    """
    A class to generate a TeX table (for our paper) from evaluation results.
    """

    # The nums for the features column per tool.
    tex_feature_column_nums = {
        "pdftotext": "\\summary{\\no}{\\od}{\\no}{\\li}{\\di}{\\hy}",
        "pdftohtml": "\\summary{\\no}{\\od}{\\no}{\\li}{\\no}{\\no}",
        "pdftoxml": "\\summary{\\no}{\\od}{\\no}{\\no}{\\no}{\\no}",
        "pdf2xml": "\\summary{\\pa}{\\od}{\\no}{\\li}{\\no}{\\hy}",
        "pdfbox": "\\summary{\\no}{\\od}{\\no}{\\li}{\\di}{\\no}",
        "parscit": "\\summary{\\no}{\\no}{\\ro}{\\no}{\\no}{\\no}",
        "lapdftext": "\\summary{\\no}{\\od}{\\ro}{\\li}{\\no}{\\no}",
        "pdfminer": "\\summary{\\pa}{\\od}{\\no}{\\no}{\\no}{\\no}",
        "pdfXtk": "\\summary{\\no}{\\od}{\\no}{\\li}{\\no}{\\no}",
        "pdfx": "\\summary{\\no}{\\od}{\\ro}{\\li}{\\di}{\\hy}",
        "pdfextract": "\\summary{\\no}{\\od}{\\no}{\\li}{\\no}{\\no}",
        "PDFExtract": "\\summary{\\pa}{\\od}{\\ro}{\\li}{\\di}{\\hy}",
        "grobid": "\\summary{\\no}{\\od}{\\ro}{\\li}{\\di}{\\hy}",
        "icecite": "\\summary{\\pa}{\\od}{\\ro}{\\li}{\\di}{\\hy}"
    }

    def format_prefix_cell(self, tool):
        """
        Formats the cell left to the criterion cells (the first cell).
        """
        cells = []
        cells.append(self.format_tool_name_cell(tool))
        cells.append(self.format_features_cell(tool))
        return self.get_cell_separator().join(cells)

    def format_tool_name_cell(self, tool):
        """
        Formats the cell containing the tool name.
        """
        if tool not in tool_data:
            return ""
        tool_name, _ = tool_data[tool]
        return "\\tool{%s}" % tool_name

    def format_features_cell(self, tool):
        """
        Formats the cell containing the features of given tool.
        """
        if tool not in self.tex_feature_column_nums:
            return ""
        features = self.tex_feature_column_nums[tool]
        return "\TD{%s}" % features

    def format_criterion_cell(self, num, num_rel, is_best):
        """
        Formats a cell containing the nums of a criterion.
        """
        if is_best:
            return "\TDDbest{%s}{%s}" % (num, num_rel)
        else:
            return "\TDD{%s}{%s}" % (num, num_rel)

    def format_num_errors_cell(self, num, is_best):
        """
        Formats a cell containing the number of errors.
        """
        if is_best:
            return "\TDbest{%s}" % num
        else:
            return "\TD{%s}" % num

    def format_avg_runtime_cell(self, num, is_best):
        """
        Formats a cell containing the average runtime.
        """
        if is_best:
            return "\TDbest{%s} \TDeol" % num
        else:
            return "\TD{%s} \TDeol" % num

    def get_cell_separator(self):
        """
        Returns the separator to be used on joining the cells to a row.
        """
        return " & "


class MarkdownTableGenerator(TableGenerator):
    """
    A class to generate a Markdown table (for GitHub) from evaluation results.
    """

    def format_prefix_cell(self, tool):
        """
        Formats the cell left to the criterion cells (the first cell).
        """
        if tool not in tool_data:
            return ""
        return "| %s" % tool_data[tool][0]

    def format_criterion_cell(self, num, num_rel, is_best):
        """
        Formats a cell containing the nums of a criterion.
        """
        if is_best:
            return "**%s** <br> <sup>**(%s%%)**</sup>" % (num, num_rel)
        else:
            return "%s <br> <sup>(%s%%)</sup>" % (num, num_rel)

    def format_num_errors_cell(self, num, is_best):
        """
        Formats a cell containing the number of errors.
        """
        if is_best:
            return "**%s**" % num
        else:
            return num

    def format_avg_runtime_cell(self, num, is_best):
        """
        Formats a cell containing the average runtime.
        """
        if is_best:
            return "**%s** |" % num
        else:
            return "%s |" % num

    def get_cell_separator(self):
        """
        Returns the separator to be used on joining the cells to a row.
        """
        return " | "

# =============================================================================


def check_table_types(default=[]):
    class CheckTableTypes(argparse.Action):
        def __call__(self, parser, args, nums, option_string=None):
            if nums is None or len(nums) == 0:
                setattr(args, self.dest, default)
            else:
                setattr(args, self.dest, nums)
    return CheckTableTypes


if __name__ == "__main__":
    table_generators = {
        "tex": TeXTableGenerator,
        "markdown": MarkdownTableGenerator
    }

    # Take the argument parser from evaluator.
    parser = evaluator.get_argument_parser()

    # Add an argument to define the table types.
    parser.add_argument(
        "--table_type",
        help="The type of table to generate.",
        choices=list(table_generators.keys()),
        action=check_table_types(list(table_generators.keys())),
        nargs="*",
    )

    # Parse the arguments.
    args = parser.parse_args()

    # Fetch the evaluation results.
    evaluation_results = evaluator.Evaluator(args).process()

    # Generate the tables.
    for table_type in args.table_type:
        if table_type in table_generators:
            generator = table_generators[table_type]
            table = generator(args, evaluation_results).generate()
            print(table)
        else:
            print("The table type '%s' is not supported." % table_type)
