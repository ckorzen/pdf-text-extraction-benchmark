import argparse
import ast
import os
import signal
import subprocess
import sys
import importlib

from multiprocessing import Pool, Value
from threading import Timer
from utils import file_utils
from utils import time_utils
from utils import output_formatter as formatter
from utils import config_utils


class Extractor:
    """
    A class that manages extraction processes from PDF files by given PDF
    extraction tools.
    """

    def __init__(self, args):
        """
        Creates a new extractor based on the given command line arguments.
        """
        self.args = args
        self.formatter = formatter.OutputFormatter(width=140, mute=args.mute)
        self.path_manager = file_utils.PathManager(
            tools_dir=self.args.tools_dir,
            pdfs_dir=self.args.pdf_dir,
            output_dir=self.args.output_dir
        )

    def process(self):
        """
        Starts the extraction.
        """
        self.handle_extraction_start()

        # Collect the pdf files to extract from.
        self.handle_collect_pdf_files_start()
        self.pdf_files = self.path_manager.collect_pdf_files(
            self.args.prefix_filter,
            self.args.suffix_filter,
            self.args.yy_filter,
            self.args.mm_filter
        )
        self.handle_collect_pdf_files_end(self.pdf_files)

        # Process each single tool.
        results_per_tool = {}
        for tool in self.args.tool_names:
            tool_extractor = self.get_tool_extractor(tool)
            if tool_extractor is not None:
                results_per_tool[tool] = tool_extractor.process()
            else:
                print("Error: The tool '%s' is not supported." % tool)

        self.handle_extraction_end(results_per_tool)

        return results_per_tool

    def get_tool_extractor(self, tool):
        """
        Returns an instance of the individual extractor for given tool which is
        defined in config file of the tool.
        """
        # Read the config file of tool.
        config_path = self.path_manager.get_tool_config_file(tool)
        config = config_utils.read_config_file(config_path)

        # Read the tool extractor name from config file to create an instance
        # from it.
        tool_extractor = config.get_string("extractor", "name")
        if tool_extractor is None:
            raise ValueError("No tool extractor given.")

        # Add the tool directory to path to import the tool extractor.
        sys.path.append(self.path_manager.get_tool_dir(tool))

        # Load each submodule of the tool extractor.
        module_names = tool_extractor.split('.')
        module_name = module_names[0]
        # Check if a module with same name was already loaded (which is the
        # case on running multiple extraction tools in a row).
        # If so, reload it. If not, import it.
        if module_name in sys.modules:
            module = importlib.reload(sys.modules[module_name])
        else:
            module = importlib.__import__(module_name)

        for name in module_names[1:]:
            module = getattr(module, name)

        # Remove the tool directory from path.
        sys.path.pop()

        # Return a new instance of the tool extractor.
        return module(tool, config, self)

    # =========================================================================
    # Handler methods.

    def handle_extraction_start(self):
        """
        Handles the start of the extraction.
        """
        self.formatter.print_heading("Extraction.")
        self.formatter.print_gap()
        self.formatter.set_col_widths(30, -1)
        self.formatter.print_cols("Tools:", ", ".join(self.args.tool_names))
        self.formatter.print_cols("Num Threads:", self.args.num_threads)
        self.formatter.print_cols("Mute:", self.args.mute)
        self.formatter.print_cols("PDFs root:", self.args.pdf_dir)
        self.formatter.print_cols("YY Filter:", "'%s'" % self.args.yy_filter)
        self.formatter.print_cols("MM Filter:", "'%s'" % self.args.mm_filter)
        self.formatter.print_cols("Prefix:", "'%s'" % self.args.prefix_filter)
        self.formatter.print_cols("Suffix:", "'%s'" % self.args.suffix_filter)
        self.formatter.print_cols("Timeout (secs):", self.args.timeout)
        self.formatter.print_gap()

    def handle_collect_pdf_files_start(self):
        """
        Handles the start of collecting the pdf files.
        """
        self.formatter.print_text("Collecting PDF files ...")
        self.formatter.print_gap()

    def handle_collect_pdf_files_end(self, pdf_files):
        """
        Handles the end of collecting the pdf files.
        """
        self.formatter.print_cols("# Found PDF files: ", len(pdf_files))
        self.formatter.print_gap()

    def handle_extraction_end(self, results_per_tool):
        """
        Handles the end of the extraction.
        """
        pass


class ToolExtractor:
    """
    A class that manages the extraction from PDF files using a given PDF
    extraction tool.
    """
    # The total number of pdf files.
    num_pdf_files = 0
    # The number of already processed pdf files.
    num_processed_pdf_files = 0

    def __init__(self, tool, tool_config, extractor):
        """
        Creates a new tool extractor for the given tool. 'extractor' is the
        instance of higher extractor.
        """
        self.tool = tool
        self.tool_config = tool_config
        self.pdf_files = extractor.pdf_files
        self.args = extractor.args
        self.formatter = extractor.formatter
        self.path_manager = extractor.path_manager

    def init_global_vars(self, num_files, num_processed_files):
        """
        Initializes some global variables needed in the extraction threads.
        """
        global num_pdf_files
        global num_processed_pdf_files

        num_pdf_files = num_files
        num_processed_pdf_files = num_processed_files

    # =========================================================================

    def process(self):
        """
        Starts the extraction process of a single PDF extraction tool.
        """
        tool_result = ExtractionResult(self.tool)

        self.handle_tool_extraction_start(tool_result)

        if self.tool_config.get_bool("run", "disabled"):
            # Abort if the tool is marked as disabled in config.
            return tool_result

        # Initialize the counter for already processed pdf files.
        num_processed_pdf_files = Value('i', 0)

        # Define a pool of threads to do the extraction in parallel.
        pool = Pool(
            processes=self.args.num_threads,
            initializer=self.init_global_vars,
            initargs=(len(self.pdf_files), num_processed_pdf_files)
        )

        # Do the extraction: Extract from each pdf file.
        file_results = pool.map(self.process_pdf_file, self.pdf_files)
        pool.close()
        pool.join()

        # Aggregate all file results to a 'global' result.
        self.aggregate_file_results(file_results, tool_result)
        self.handle_tool_extraction_end(tool_result)

        return tool_result

    def process_pdf_file(self, pdf):
        """
        Extracts from the given pdf file using the given tool.
        """
        global num_processed_pdf_files

        tool = self.tool
        file_result = ExtractionResult(tool)
        file_result.pdf_path = pdf

        raw_path = self.path_manager.get_tool_raw_output_file(tool, pdf)
        plain_path = self.path_manager.get_tool_plain_output_file(tool, pdf)

        file_result.raw_output_path = raw_path
        file_result.plain_output_path = plain_path

        # Extract.
        if self.args.force or file_utils.is_missing_or_empty_file(raw_path):
            # Extract from pdf using the given tool.
            self.extract(pdf, raw_path, file_result)

        # Create plain output file.
        if self.args.force or file_utils.is_missing_or_empty_file(plain_path):
            self.create_plain_output_file(raw_path, plain_path, file_result)

        # Lock the counter, because += operation is not atomic
        with num_processed_pdf_files.get_lock():
            num_processed_pdf_files.value += 1

        self.handle_file_result(file_result)

        return file_result

    def extract(self, pdf, target_path, file_result):
        """
        Executes the given extraction command.
        """

        def kill_process(process, timeout):
            """
            Kills the given extraction process.
            """
            timeout["value"] = True
            os.killpg(os.getpgid(process.pid), signal.SIGTERM)

        def start_process(cmd, timeout):
            """
            Starts a single extraction process."
            """
            process = subprocess.Popen(
                [cmd],
                stdout=subprocess.PIPE,
                stderr=subprocess.PIPE,
                shell=True,
                preexec_fn=os.setsid,
                cwd=self.path_manager.get_tool_dir(self.tool)
            )
            timeout_value = {"value": False}
            timer = Timer(timeout, kill_process, [process, timeout_value])
            timer.start()
            stdout, stderr = process.communicate()
            stdout = stdout.decode("UTF-8").strip()
            stderr = stderr.decode("UTF-8").strip()
            timer.cancel()
            return process.returncode, timeout_value["value"], stdout, stderr

        # Abort if directories could not be created.
        if not file_utils.ensure_dirs(target_path):
            raise IOError("The path '%s' could not be created." % raw_path)

        cmd = self.tool_config.get_string("run", "cmd")
        if cmd is None:
            raise ValueError("No extraction command given.")
        cmd = cmd.replace('$IN', pdf)
        cmd = cmd.replace('$OUT', target_path)
        timeout = self.args.timeout

        # Run the command
        start = time_utils.time_in_ms()
        status_code, is_timeout, stdout, stderr = start_process(cmd, timeout)
        end = time_utils.time_in_ms()

        status_code = 99 if is_timeout else status_code
        status = "Timeout" if is_timeout else stderr or stdout

        file_result.cmd = cmd
        file_result.runtime = end - start
        file_result.status_code = status_code
        file_result.status = status

    def create_plain_output_file(self, raw_path, plain_path, file_result):
        """
        Creates a plain txt output file from given "raw" source path, which
        could be an XML or HTML file.
        """
        status_messages = {
            0: "OK",
            11: "Missing or empty output file",
            12: "Corrupt output file",
            99: "Timeout"
        }

        if not file_utils.ensure_dirs(plain_path):
            raise IOError("The path '%s' could not be created." % plain)

        raw_status_code, plain_output = self.create_plain_output(raw_path)
        status_code = max(raw_status_code, file_result.status_code)
        status = status_messages.get(status_code, "")

        # Write the plain output to file.
        file_utils.write_tool_file(
            plain_path,
            plain_output,
            status_code=status_code,
            status=status
        )

    def create_plain_output(self, raw_path):
        """
        Creates a plain txt output from given raw output file. Override
        it if you have to do more advanced stuff.
        """

        if not file_utils.is_missing_or_empty_file(raw_path):
            with open(raw_path, "r", errors='ignore') as f:
                return 0, f.read()
        else:
            return 11, None

    def aggregate_file_results(self, file_results, tool_result):
        """
        Aggregates the given list of file results to get average values for
        the tool. Writes the values into given tool_result.
        """

        if len(file_results) == 0:
            return

        sum_runtimes = sum([getattr(x, "runtime", 0) for x in file_results])
        tool_result.total_runtime = sum_runtimes
        tool_result.avg_runtime = sum_runtimes / len(file_results)

    # =========================================================================
    # Handler methods.

    def handle_tool_extraction_start(self, tool_result):
        """
        Handles the start of the extraction using a single tool.
        """

        tool_dir = self.path_manager.get_tool_dir(self.tool)
        output_dir = self.path_manager.get_tool_output_dir(self.tool)
        start_time = tool_result.start_time = time_utils.time_in_ms()
        start_time_str = time_utils.format_time(start_time)

        cmd = self.tool_config.get_string("run", "cmd")
        is_disabled = self.tool_config.get_bool("run", "disabled")

        self.formatter.print_heading("Extraction using tool %s" % self.tool)
        self.formatter.print_gap()
        self.formatter.print_cols("Tool Dir:", tool_dir)
        self.formatter.print_cols("Output dir:", output_dir)
        self.formatter.print_cols("CMD:", cmd)
        self.formatter.print_cols("Disabled:", is_disabled)
        self.formatter.print_gap()
        self.formatter.print_text("Extracting ...")
        self.formatter.print_gap()
        self.formatter.print_cols("Start time:", start_time_str)
        self.formatter.set_col_widths(10, 30, 40, 10, 5, -1)
        self.formatter.print_col_headers(
            "#", "Input", "Target", "Runtime", "Code", "Status")

    def handle_file_result(self, result):
        """
        Handles a single file result, related to a single gt file.
        """
        global num_pdf_files
        global num_processed_pdf_files

        pdf_path = getattr(result, "pdf_path", "")
        output_path = getattr(result, "plain_output_path", "")
        runtime = getattr(result, "runtime", 0)
        status_code = getattr(result, "status_code", -1)
        status = getattr(result, "status", "")

        self.formatter.print_cols(
            "%d/%d" % (num_processed_pdf_files.value, num_pdf_files),
            os.path.split(pdf_path)[1],
            os.path.split(output_path)[1],
            "%dms" % runtime,
            status_code,
            status[0:40]
        )

    def handle_tool_extraction_end(self, result):
        """
        Handles then end of the extraction using a single tool.
        """
        total_runtime = getattr(result, "total_runtime", 0)
        total_runtime_str = time_utils.format_time_delta(total_runtime)

        avg_runtime = getattr(result, "avg_runtime", 0)
        avg_runtime_str = time_utils.format_time_delta(avg_runtime)

        self.formatter.print_col_footers()
        self.formatter.set_col_widths(30, -1)
        self.formatter.print_cols("Total time: %s" % total_runtime_str)
        self.formatter.print_cols("Avg. Time: %s" % avg_runtime_str)
        self.formatter.print_gap()


class ExtractionResult:
    """
    A simple class representing an extraction result.
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
        "--tools_dir",
        metavar='<dir>',
        help="The root directory of tools. Default: '%(default)s'.",
        default=file_utils.TOOLS_DIR
    )
    parser.add_argument(
        "--pdf_dir",
        metavar='<dir>',
        help="The root directory of pdf files. Default: '%(default)s'.",
        default=file_utils.PDFS_DIR
    )
    parser.add_argument(
        "--output_dir",
        metavar='<dir>',
        help="The root directory for output files. Default: '%(default)s'.",
        default=file_utils.OUTPUT_DIR
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
        help="Process only pdf files ending with the given suffix. "
             "Default: %(default)s.",
        default=file_utils.PDF_FILE_EXT
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
        "--timeout",
        help="The maximal time in secs to wait for a single extraction task. "
             "Default: %(default)s.",
        type=int,
        default=300  # 5 min
    )
    parser.add_argument(
        "--force",
        help="Forces the extraction from PDF files, even if there are PDF "
             "files for which there are some output files. "
             "Default: %(default)s.",
        type=ast.literal_eval,
        default=False
    )
    return parser


if __name__ == "__main__":
    args = get_argument_parser().parse_args()
    Extractor(args).process()
