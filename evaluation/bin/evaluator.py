import argparse
import ast
import pickle

from collections import Counter
from diff import doc_diff
from multiprocessing import Pool, Value
from utils import file_utils
from utils import time_utils
from utils import output_formatter as formatter
from utils import config_utils


class Evaluator:
    """
    A class that manages the evaluation of given PDF extraction tools against
    our evaluation criteria.
    """

    def __init__(self, args):
        """
        Creates a new evaluator based on the given command line arguments.
        """
        self.args = args
        self.formatter = formatter.OutputFormatter(width=140, mute=args.mute)
        self.path_manager = file_utils.PathManager(
            output_dir=self.args.output_dir,
            groundtruth_dir=self.args.groundtruth_dir
        )

    def process(self):
        """
        Starts the evaluation.
        """
        self.handle_evaluation_start()

        # Collect all the ground truth files.
        self.handle_collect_gt_files_start()
        self.gt_files = self.path_manager.collect_gt_files(
            self.args.prefix_filter,
            self.args.suffix_filter,
            self.args.yy_filter,
            self.args.mm_filter
        )
        self.handle_collect_gt_files_end(self.gt_files)

        # Evaluate each single tool.
        results_per_tool = {}
        for tool in self.args.tool_names:
            config_path = self.path_manager.get_tool_config_file(tool)
            conf = config_utils.read_config_file(config_path)

            results_per_tool[tool] = ToolEvaluator(tool, conf, self).process()

        self.handle_evaluation_end(results_per_tool)

        return results_per_tool

    # =========================================================================
    # Handler methods.

    def handle_evaluation_start(self):
        """
        Handles the start of the evaluation.
        """
        args = self.args
        self.formatter.print_heading("Evaluation.")
        self.formatter.print_gap()
        self.formatter.set_col_widths(30, -1)
        self.formatter.print_cols("Tools:", ", ".join(self.args.tool_names))
        self.formatter.print_cols("Num Threads:", args.num_threads)
        self.formatter.print_cols("Mute:", args.mute)
        self.formatter.print_cols("Force:", args.force)
        self.formatter.print_cols("Output Dir:", args.output_dir)
        self.formatter.print_cols("Groundtruth Dir:", args.groundtruth_dir)
        self.formatter.print_cols("YY Filter:", "'%s'" % args.yy_filter)
        self.formatter.print_cols("MM Filter:", "'%s'" % args.mm_filter)
        self.formatter.print_cols("Prefix:", "'%s'" % args.prefix_filter)
        self.formatter.print_cols("Suffix:", "'%s'" % args.suffix_filter)
        self.formatter.print_cols("Rearrange:", args.rearrange)
        self.formatter.print_cols("Case Insensitive:", args.case_insensitive)
        self.formatter.print_cols("Junk:", args.junk)
        self.formatter.print_gap()

    def handle_collect_gt_files_start(self):
        """
        Handles the start of collecting the ground truth files.
        """
        self.formatter.print_text("Collecting ground truth files ...")
        self.formatter.print_gap()

    def handle_collect_gt_files_end(self, gt_files):
        """
        Handles the end of collecting the ground truth files.
        """
        self.formatter.print_cols("# Found ground truth files:", len(gt_files))
        self.formatter.print_gap()

    def handle_evaluation_end(self, results_per_tool):
        """
        Handles the end of the evaluation.
        """
        pass


class ToolEvaluator:
    """
    A class that manages the evaluation of a given PDF extraction tool against
    our evaluation criteria.
    """
    # The total number of ground truth files.
    num_gt_files = 0
    # The number of already processed ground truth files.
    num_processed_gt_files = 0

    def __init__(self, tool, tool_config, evaluator):
        """
        Creates a new tool evaluator for the given tool. 'evaluator' is the
        instance of higher evaluation.
        """
        self.tool = tool
        self.tool_config = tool_config
        self.gt_files = evaluator.gt_files
        self.args = evaluator.args
        self.formatter = evaluator.formatter
        self.path_manager = evaluator.path_manager

    def init_global_vars(self, num_files, num_processed_files):
        """
        Initializes some global variables to be able to use them within the
        evaluation threads.
        """
        global num_gt_files
        global num_processed_gt_files

        num_gt_files = num_files
        num_processed_gt_files = num_processed_files

    # =========================================================================

    def process(self):
        """
        Starts the evaluation of a given PDF extraction tool.
        """
        tool_result = EvaluationResult(self.tool)

        self.handle_tool_evaluation_start(tool_result)

        if self.tool_config.get_bool("run", "disabled"):
            # Abort if the tool is marked as disabled in config.
            return tool_result

        # Initialize the counter for already processed gt files.
        num_processed_gt_files = Value('i', 0)

        # Define a pool of threads to do the evaluation in parallel.
        pool = Pool(
            processes=self.args.num_threads,
            initializer=self.init_global_vars,
            initargs=(len(self.gt_files), num_processed_gt_files)
        )

        # Do the evaluation: Evaluate each output file against the related
        # ground truth file.
        file_results = pool.map(self.process_gt_file, self.gt_files)
        pool.close()
        pool.join()

        # Aggregate all results related to a single file to a 'global' result.
        self.aggregate_file_results(file_results, tool_result)
        self.handle_tool_evaluation_end(tool_result)

        return tool_result

    def process_gt_file(self, gt_file):
        """
        Evaluates the output of given tool against given ground truth file.
        """
        global num_processed_gt_files

        # Check if there is a serialized result, computed previously.
        f = self.path_manager.get_tool_serialization_file(self.tool, gt_file)
        if self.args.force or file_utils.is_missing_or_empty_file(f):
            file_result = self.evaluate(gt_file)
            self.serialize_file_result(file_result, f)
        else:
            file_result = self.deserialize_file_result(f)

        # Lock the counter, because += operation is not atomic
        with num_processed_gt_files.get_lock():
            num_processed_gt_files.value += 1

        self.handle_file_result(file_result)

        return file_result

    def evaluate(self, gt_file):
        """
        Processes the given tool file.
        """
        # Define some evaluation status codes:
        # 0:    Ok
        # 11:   Tool file is missing / empty.
        # 12:   Tool file is corrupt / incomplete.
        # 21:   GT file is missing / empty.
        # 22:   GT file is corrupt / incomplete.
        # 31:   PDF file is missing.
        # 99:   Timeout on extraction.

        file_result = EvaluationResult(self.tool)
        file_result.gt_file = gt_file

        # Read the ground truth file.
        if file_utils.is_missing_or_empty_file(gt_file):
            file_result.status_code = 21
        else:
            groundtruth, _ = file_utils.read_groundtruth_file(gt_file)
            file_result.status_code = 0

        # Read the tool file.
        tool = self.tool
        tool_file = self.path_manager.get_tool_plain_output_file(tool, gt_file)
        file_result.tool_file = tool_file
        if file_utils.is_missing_or_empty_file(tool_file):
            file_result.status_code = 11
        else:
            tool_output, status_code = file_utils.read_tool_file(tool_file)
            file_result.status_code = status_code

        # Check the status code and abort, if there is an error.
        if getattr(file_result, "status_code", -1) != 0:
            return file_result

        # Evaluate the strings.
        evaluation_result = self.evaluate_strings(groundtruth, tool_output)

        # Write visualization to file.
        vis_path = self.path_manager.get_tool_visualization_file(tool, gt_file)
        self.visualize_evaluation_result(evaluation_result, vis_path)

        # Compose the file result.
        file_result.num_ops = evaluation_result.num_ops
        file_result.num_ops_abs = evaluation_result.num_ops_abs
        file_result.num_ops_rel = evaluation_result.num_ops_rel
        file_result.num_paras_target = evaluation_result.num_paras_target
        file_result.num_words_target = evaluation_result.num_words_target

        return file_result

    def evaluate_strings(self, groundtruth, tool_output):
        """
        Computes precision and recall of words extraction. For that, run diff
        on the set of words of groundtruth (gt) and the actual extraction
        stats (actual). The precision of actual follows from the percentage of
        the number of common words to the number of extracted words. The recall
        follows from the percentage of the number of common words to the number
        of all words in the groundtruth.
        We only want to evaluate the accuracy of words extraction, but not to
        evaluate the correct order of extracted words. Thus, we try tro
        rearrange the words in the actual stats such that the order of words
        corresponds to the order in the groundtruth. You can disable the
        rearrange step by setting the rearrange flag to False.
        Per default, the evaluation is done case-insensitively. To make it
        case-sensitive, set the ignore_cases flag to False.
        Per default, the evaluation is based on exact matches of words. To
        match words with a defined distance as well, adjust max_dist.
        """

        return doc_diff.doc_diff_from_strings(
            tool_output,
            groundtruth,
            rearrange_phrases=self.args.rearrange,
            to_lower=self.args.case_insensitive,
            min_rearrange_length=10,
            refuse_common_threshold=1,
            excludes=self.args.junk,
            junk=self.args.junk
        )

    def aggregate_file_results(self, file_results, tool_result):
        """
        Aggregates the given list of file results to get average values for
        the tool to evaluate. Writes the values into given tool_result.
        """

        num_ok = 0
        num_errors = 0
        errors_by_freqs = Counter()
        sum_num_ops = Counter()
        sum_num_ops_abs = Counter()
        sum_num_words_target = 0
        sum_num_paras_target = 0

        for result in file_results:
            if not result:
                continue

            num_ops = getattr(result, "num_ops", None)
            num_ops_abs = getattr(result, "num_ops_abs", None)

            if num_ops is None or num_ops_abs is None:
                # TODO: Is this the correct status code?
                result.status_code = getattr(result, "status_code", None) or 22

            if getattr(result, "status_code", 0) != 0:
                errors_by_freqs[result.status_code] += 1
                num_errors += 1
                continue

            sum_num_ops += num_ops
            sum_num_ops_abs += num_ops_abs
            sum_num_paras_target += getattr(result, "num_paras_target", 0)
            sum_num_words_target += getattr(result, "num_words_target", 0)
            num_ok += 1

        # Compute average num ops.
        avg_num_ops = Counter({k: v/num_ok for k, v in sum_num_ops.items()})

        # Compute average relative num ops.
        avg_num_ops_rel = Counter(sum_num_ops_abs)
        avg_num_ops_rel["num_para_splits"] /= sum_num_paras_target
        avg_num_ops_rel["num_para_merges"] /= sum_num_paras_target
        avg_num_ops_rel["num_para_inserts"] /= sum_num_words_target
        avg_num_ops_rel["num_para_deletes"] /= sum_num_words_target
        avg_num_ops_rel["num_para_rearranges"] /= sum_num_words_target
        avg_num_ops_rel["num_word_inserts"] /= sum_num_words_target
        avg_num_ops_rel["num_word_deletes"] /= sum_num_words_target
        avg_num_ops_rel["num_word_replaces"] /= sum_num_words_target

        tool_result.num_errors = num_errors
        tool_result.error_codes = errors_by_freqs
        tool_result.num_ok = num_ok
        tool_result.num_ops = avg_num_ops
        tool_result.num_ops_rel = avg_num_ops_rel

    def serialize_file_result(self, file_result, path):
        """
        Serializes the given file result to file.
        """
        if not file_utils.ensure_dirs(path):
            raise IOError("The path '%s' could not be created." % path)
        f = open(path, 'wb+')
        pickle.dump(file_result, f)
        f.close()

    def deserialize_file_result(self, path):
        """
        Deserializes the stats from given file.
        """
        f = open(path, 'rb')
        file_result = pickle.load(f)
        f.close()
        return file_result

    def visualize_evaluation_result(self, evaluation_result, path):
        """
        Visualizes the given evaluation result.
        """
        if not file_utils.ensure_dirs(path):
            raise IOError("The path '%s' could not be created." % path)
        f = open(path, 'w+')
        f.write(evaluation_result.vis)
        f.close()

    # =========================================================================
    # Handler methods.

    def handle_tool_evaluation_start(self, tool_result):
        """
        Handles the start of evaluation.
        """

        output_dir = self.path_manager.get_tool_output_dir(self.tool)
        start_time = tool_result.start_time = time_utils.time_in_ms()
        start_time_str = time_utils.format_time(start_time)
        is_disabled = self.tool_config.get_bool("run", "disabled")

        self.formatter.print_heading("Evaluation of tool %s" % self.tool)
        self.formatter.print_gap()
        self.formatter.print_cols("Tool output dir: ", output_dir)
        self.formatter.print_cols("Disabled: ", is_disabled)

        self.formatter.print_gap()
        self.formatter.print_text("Evaluating ...")
        self.formatter.print_gap()
        self.formatter.print_cols("Start time: ", start_time_str)
        self.formatter.set_col_widths(12, 15, 14, 14, 14, 14, 14, 14, 14, 14)
        self.formatter.print_col_headers(
            "#",
            "File",
            "        NL+",  # number of para merges.
            "        NL-",  # number of para splits.
            "        P+",   # number of para deletes.
            "        P-",   # number of para inserts.
            "        P<>",  # number of para rearranges.
            "        W+",   # number of word deletes.
            "        W-",   # number of word inserts.
            "        W~"    # number of word replaces.
        )

    def handle_file_result(self, result):
        """
        Handles a single file result, related to a single gt file.
        """
        global num_gt_files
        global num_processed_gt_files

        self.formatter.print_cols(
            "%d/%d" % (num_processed_gt_files.value, num_gt_files),
            file_utils.get_basename_from_gt_file(result.gt_file),
            self.format_cell(result, "num_para_merges"),
            self.format_cell(result, "num_para_splits"),
            self.format_cell(result, "num_para_deletes"),
            self.format_cell(result, "num_para_inserts"),
            self.format_cell(result, "num_para_rearranges"),
            self.format_cell(result, "num_word_deletes"),
            self.format_cell(result, "num_word_inserts"),
            self.format_cell(result, "num_word_replaces"),
        )

    def handle_tool_evaluation_end(self, result):
        """
        Handles then end of the evaluation of a tool.
        """

        self.formatter.print_col_footers(
            "Total:",
            "",
            self.format_cell(result, "num_para_merges"),
            self.format_cell(result, "num_para_splits"),
            self.format_cell(result, "num_para_deletes"),
            self.format_cell(result, "num_para_inserts"),
            self.format_cell(result, "num_para_rearranges"),
            self.format_cell(result, "num_word_deletes"),
            self.format_cell(result, "num_word_inserts"),
            self.format_cell(result, "num_word_replaces"),
        )

        num_ok = getattr(result, "num_ok", 0)
        num_errors = getattr(result, "num_errors", 0)
        error_codes = getattr(result, "error_codes", 0)
        start_time = getattr(result, "start_time", 0)
        end_time = result.end_time = time_utils.time_in_ms()
        end_time_str = time_utils.format_time(end_time)
        runtime = end_time - start_time
        runtime_str = time_utils.format_time_delta(runtime)
        avg_runtime = runtime / (num_ok + num_errors)
        avg_runtime_str = time_utils.format_time_delta(avg_runtime)

        self.formatter.set_col_widths(30, -1)
        self.formatter.print_cols("End time: ", end_time_str)
        self.formatter.print_cols("Total runtime: ", runtime_str)
        self.formatter.print_cols("Avg. runtime: ", avg_runtime_str)
        self.formatter.print_cols("# evaluated files: ", num_ok + num_errors)
        self.formatter.print_cols("# errors: ", num_errors)
        if len(error_codes) > 0:
            self.formatter.print_cols("# errors broken down by error code:")
            for error_code, freq in sorted(error_codes.items()):
                self.formatter.print_cols(error_code, freq)

    # =========================================================================
    # Util methods.

    def format_cell(self, result, criterion):
        """
        Formats a 'table cell' with given values.
        """
        parts = []

        num_ops = getattr(result, "num_ops", None)
        num_ops_rel = getattr(result, "num_ops_rel", None)

        if num_ops is None or num_ops_rel is None:
            return ""

        value = num_ops[criterion]
        value_rel = num_ops_rel[criterion]

        value_str = ""
        if isinstance(value, int):
            value_str = "%s" % value
        elif isinstance(value, float):
            value_str = "%.1f" % value

        rel_str = "%.1f%%" % (value_rel * 100)
        parts.append(("%s" % value_str).rjust(6))
        parts.append(("(%s)" % rel_str).rjust(8))
        return "".join(parts)


class EvaluationResult:
    """
    A simple class representing an evaluation result.
    """
    def __init__(self, tool):
        self.tool = tool

# =============================================================================
# Methods for argument parser.


def check_num_threads():
    class CheckNumThreads(argparse.Action):
        def __call__(self, parser, args, values, option_string=None):
            if values is not None:
                val = int(values)
                if val < 1:
                    setattr(args, self.dest, None)
                else:
                    setattr(args, self.dest, val)
    return CheckNumThreads


def get_argument_parser():
    """
    Creates an parser to parse the command line arguments.
    """

    parser = argparse.ArgumentParser()
    parser.add_argument(
        "tool_names",
        help="The name of tools to evaluate.",
        nargs='+'
    )
    parser.add_argument(
        "--output_dir",
        metavar='<dir>',
        help="The root directory of tool outputs. Default: '%(default)s'.",
        default=file_utils.OUTPUT_DIR
    )
    parser.add_argument(
        "--groundtruth_dir",
        metavar='<dir>',
        help="The root directory of ground truth. Default: '%(default)s'.",
        default=file_utils.GTS_DIR
    )
    parser.add_argument(
        "-p", "--prefix_filter",
        metavar='<string>',
        help="Process only ground truth files starting with the given prefix. "
             "Default: %(default)s.",
        default=""
    )
    parser.add_argument(
        "-s", "--suffix_filter",
        metavar='<string>',
        help="Process only ground truth files ending with the given suffix. "
             "Default: %(default)s.",
        default=file_utils.GT_FILE_EXT
    )
    parser.add_argument(
        "-yy", "--yy_filter",
        metavar='<string>',
        help="Process only the ground truth files of given year.",
        default=""
    )
    parser.add_argument(
        "-mm", "--mm_filter",
        metavar='<int>',
        help="Process only the ground truth files of given month.",
        default=""
    )
    parser.add_argument(
        "-r", "--rearrange",
        metavar='<bool>',
        help="Toggles the rearranging of words.",
        type=ast.literal_eval,
        default=True
    )
    parser.add_argument(
        "-i", "--case_insensitive",
        metavar='<bool>',
        help="Toggle case-sensitivity.",
        type=ast.literal_eval,
        default=True
    )
    parser.add_argument(
        "-j", "--junk",
        metavar='<string>',
        help="The list of placeholders marking the phrases to ignore.",
        nargs="+",
        default=[]
    )
    parser.add_argument(
        "-n", "--num_threads",
        metavar='<int>',
        help="The number of threads to use for evaluation. "
             "Per default, as many as possible threads are used.",
        action=check_num_threads(),
        default=None,
    )
    parser.add_argument(
        "-m", "--mute",
        metavar='<bool>',
        help="Flag to switch off the output.",
        type=ast.literal_eval,
        default=False
    )
    parser.add_argument(
        "-f", "--force",
        metavar='<bool>',
        help="Forces the computation of values for evaluation criteria, "
             "even if there are some cached values. Default: %(default)s.",
        type=ast.literal_eval,
        default=False
    )
    return parser

if __name__ == "__main__":
    args = get_argument_parser().parse_args()
    Evaluator(args).process()
