import os
import os.path

# The current working directory.
CWD = os.path.dirname(os.path.realpath(__file__))
# The root directory of the project.
ROOT_DIR = os.path.abspath(os.path.join(CWD, "../../.."))

# Define some default values.
TOOLS_DIR = os.path.abspath(os.path.join(ROOT_DIR, "evaluation/tools/data"))
TOOL_OUTPUT_DIR_NAME = "output"
TOOL_CONFIG_FILE_NAME = "config.ini"
TOOL_RAW_OUTPUT_FILE_EXT = ".raw.txt"
TOOL_PLAIN_OUTPUT_FILE_EXT = ".final.txt"
TOOL_SERIALIZATION_FILE_EXT = ".serial.txt"
TOOL_VISUALIZATION_FILE_EXT = ".visualization.txt"

OUTPUT_DIR = os.path.abspath(os.path.join(ROOT_DIR, "evaluation/output/data"))

PDFS_DIR = os.path.abspath(os.path.join(ROOT_DIR, "benchmark/pdf/data"))
PDF_FILE_EXT = ".pdf"

GTS_DIR = os.path.abspath(os.path.join(ROOT_DIR, "benchmark/groundtruth/data"))
GT_FILE_EXT = ".body.txt"
GT_PDF_POS_INDEX_EXT = ".positions.txt"

# =============================================================================


class PathManager:
    """
    A class that manages paths to directories and file needed on extraction and
    evaluation.
    """

    def __init__(
            self,
            tools_dir=None,
            groundtruth_dir=None,
            pdfs_dir=None,
            output_dir=None):
        """
        Creates a new PathManager based on given root directories.
        """
        # The root dir of tools, where the binaries and configs are located.
        self.tools_dir = tools_dir or TOOLS_DIR
        # The root directory where the ground truth files are located.
        self.gt_dir = groundtruth_dir or GTS_DIR
        # The root directory where the pdf files are located.
        self.pdfs_dir = pdfs_dir or PDFS_DIR
        # The root directory where the tool output files are located.
        self.output_dir = output_dir or OUTPUT_DIR

    def get_tool_dir(self, tool):
        """
        Returns the path to the root directory of the given tool.
        """
        return os.path.abspath(os.path.join(self.tools_dir, tool))

    def get_tool_output_dir(self, tool):
        """
        Returns the path to the root directory of tool output files.
        """
        return os.path.abspath(os.path.join(self.output_dir, tool))

    def get_tool_config_file(self, tool):
        """
        Returns the path to the config file of given tool.
        """
        tool_dir = self.get_tool_dir(tool)
        tool_config_file = os.path.join(tool_dir, TOOL_CONFIG_FILE_NAME)
        return os.path.abspath(tool_config_file)

    def get_tool_raw_output_file(self, tool, pdf_file):
        """
        Returns the path to file, where the extraction result of given tool for
        the given pdf file is/should be stored.
        """
        return self.get_tool_file(tool, pdf_file, TOOL_RAW_OUTPUT_FILE_EXT)

    def get_tool_plain_output_file(self, tool, pdf_file):
        """
        Returns the path to file, where the plain version of extraction result
        is/should be stored.
        """
        return self.get_tool_file(tool, pdf_file, TOOL_PLAIN_OUTPUT_FILE_EXT)

    def get_tool_serialization_file(self, tool, gt_file):
        """
        Returns the path to file, where the evaluation results of given tool
        are serialized.
        """
        return self.get_tool_file(tool, gt_file, TOOL_SERIALIZATION_FILE_EXT)

    def get_tool_visualization_file(self, tool, gt_file):
        """
        Returns the path to file, where the evaluation results of given tool
        are visualized.
        """
        return self.get_tool_file(tool, gt_file, TOOL_VISUALIZATION_FILE_EXT)

    def get_tool_file(self, tool, ref_file, extension):
        """
        Returns the path to an output file of given tool, that ends with given
        extension and relates to the given reference file. The reference
        file is either a pdf file or a ground truth file of the benchmark.
        Examples:

        get_tool_file("pdftotext", "<GT_DIR>/0001/foo.bar.body.txt", ".csv")
        returns
        "<OUTPUT_DIR>/pdftotext/0001/foo.bar.csv"

        get_tool_file("pdftotext", "<PDF_DIR>/0001/foo.bar.pdf", ".final.txt")
        returns
        "<OUTPUT_DIR>/pdftotext/0001/foo.bar.final.txt"
        """

        ref_root_dir, ref_extension = None, None
        # Check if the ref_file is a ground truth- or pdf file.
        if ref_file.endswith(GT_FILE_EXT):
            ref_root_dir = self.gt_dir
            ref_extension = GT_FILE_EXT
        else:
            ref_root_dir = self.pdfs_dir
            ref_extension = PDF_FILE_EXT

        # Get the path of ref_file, relative to the ref_root_dir.
        # arxiv-benchmark/benchmark/groundtruth/0001/cond-mat0001108.body.txt
        # ->
        # 0001/cond-mat0001108.body.txt
        ref_file_rel = os.path.relpath(ref_file, ref_root_dir)

        # Remove the suffix from the relative path.
        # 0001/cond-mat0001108.body.txt -> 0001/cond-mat0001108
        ref_file_rel = ref_file_rel.replace(ref_extension, "")

        # Append the given extension.
        # 0001/cond-mat0001108 -> 0001/cond-mat0001108.final.txt
        tool_file_rel = ref_file_rel + extension

        # Compute the absolute path of output file.
        tool_file = os.path.join(self.get_tool_output_dir(tool), tool_file_rel)
        return os.path.abspath(tool_file)

    def get_pdf_pos_index_path(self, gt_file):
        """
        Returns the path to the PDF positions index file, where word ids are
        mapped to the positions of the related words in the PDF.
        """
        # Get the path of gt_file, relative to the self.gt_dir.
        gt_file_rel = os.path.relpath(gt_file, self.gt_dir)
        # Remove the suffix from the relative path.
        gt_file_rel = gt_file_rel.replace(GT_FILE_EXT, "")
        # Append the extension for the PDF position index file.
        pdf_pos_index_file_rel = gt_file_rel + GT_PDF_POS_INDEX_EXT
        # Compute the absolute path of the PDF position index file.
        pdf_pos_index_file = os.path.join(self.gt_dir, pdf_pos_index_file_rel)
        return os.path.abspath(pdf_pos_index_file)

    # =========================================================================
    # Collect methods.

    def collect_gt_files(self, prefix="", suffix="", yy="", mm=""):
        """
        Collects all groundtruth files matching the given filters.
        """
        return collect_files(self.gt_dir, prefix, suffix, yy, mm)

    def collect_pdf_files(self, prefix="", suffix="", yy="", mm=""):
        """
        Collects all pdf files matching the given filters.
        """
        return collect_files(self.pdfs_dir, prefix, suffix, yy, mm)

# =============================================================================
# Static methods.


def collect_files(root_dir, prefix="", suffix="", yy="", mm=""):
    """
    Collects all tool output files in the given root directory of a tool.
    """

    result = []

    # Check if the given root_dir is indeed a directory.
    if not os.path.isdir(root_dir):
        raise ValueError("The directory '%s' does not exist." % root_dir)

    # Try to read the directory.
    try:
        files = os.listdir(root_dir)
    except:
        raise ValueError("The directory '%s' can't be read." % root_dir)

    # Iterate through the entries of root directory.
    for yymm in sorted(files):
        # Check if yy/mm filters match.
        if not yymm.startswith(yy):
            continue
        if not yymm.endswith(mm):
            continue

        yymm_path = os.path.abspath(os.path.join(root_dir, yymm))

        # Only proceed if the entry is a directory.
        if not os.path.isdir(yymm_path):
            continue

        # Collect the pdf files of current dir and put it to result.
        files_of_dir = collect_files_from_dir(yymm_path, prefix, suffix)
        files_of_dir.sort()
        result.extend(files_of_dir)
    return result


def collect_files_from_dir(directory, prefix="", suffix="", recursive=True):
    """
    Collects the files in the given directory that matches the given prefix
    and suffix.
    """
    files = []
    _collect_files_from_dir(directory, prefix, suffix, recursive, files)
    return files


def _collect_files_from_dir(directory, prefix, suffix, recursive, result):
    """
    Collects the files in the given directory that matches the given prefix
    and suffix.
    """

    # Iterate through the entries of the directory.
    for name in sorted(os.listdir(directory)):
        path = os.path.abspath(os.path.join(directory, name))
        if os.path.isdir(path) and recursive is True:
            _collect_files_from_dir(path, prefix, suffix, recursive, result)
        elif os.path.isfile(path):
            # Check, if prefix/suffix matches.
            if not name.startswith(prefix):
                continue
            if not name.endswith(suffix):
                continue
            result.append(path)

# =============================================================================


def get_basename_from_gt_file(gt_file):
    """
    Returns the basename (the filename without extension) of given ground
    truth file.
    """
    directory, file_name = os.path.split(gt_file)
    return file_name.replace(GT_FILE_EXT, "")


def get_basename_from_pdf_file(pdf_file):
    """
    Returns the basename (the filename without extension) of given ground
    truth file.
    """
    directory, file_name = os.path.split(pdf_file)
    return file_name.replace(PDF_FILE_EXT, "")

# =============================================================================


def read_groundtruth_file(path):
    """
    Reads the given ground truth file.
    """
    if is_missing_or_empty_file(path):
        return None, None

    lines = []
    source_tex_file = None
    with open(path) as f:
        for line in f:
            if line.startswith("##source"):
                _, source_tex_file = line.split("\t")
            else:
                lines.append(line)
    gt = "".join(lines)
    return gt, source_tex_file


def read_tool_file(path):
    """
    Reads the given plain output file.
    """
    if is_missing_or_empty_file(path):
        return None, None

    lines = []
    status_code = -1
    with open(path) as f:
        for line in f:
            if line.startswith("##status"):
                status_code = int(line.split("\t")[1])
            else:
                lines.append(line)
    tool_output = "".join(lines)
    return tool_output, status_code


def write_tool_file(path, content, status_code=-1, status=""):
    """
    Write a tool file.
    """
    # Store the output to file.
    with open(path, "w+") as f:
        # Write the status into the header of file.
        if status_code > -1 and len(status) > 0:
            f.write("##status\t%d\t%s\n" % (status_code, status))

        if content is not None:
            # Write the plain output to file.
            f.write(content)

# =============================================================================

def read_pdf_pos_index_file(path):
    """
    Reads the given PDf position index file.
    """
    if is_missing_or_empty_file(path):
        return None

    index = {}
    with open(path) as f:
        for line in f:
            line = line.strip()
            
            # Ignore comment lines.
            if line.startswith('#'):
                continue

            fields = line.split("\t")
            if len(fields) < 1:
                continue
            word_id = fields[0]
            positions = []
            # Parse the positions.
            for i in range(1, len(fields)):
                pos_fields = fields[i].split(",")
                if len(pos_fields) != 5:
                    continue
                page_number = int(pos_fields[0])
                min_x = float(pos_fields[1])
                min_y = float(pos_fields[2])
                max_x = float(pos_fields[3])
                max_y = float(pos_fields[4])
                pos = PdfPosition(page_number, min_x, min_y, max_x, max_y)
                positions.append(pos)
            index[word_id] = positions
    return index

class PdfPosition:
    def __init__(self, page_number, min_x, min_y, max_x, max_y):
        self.page_number = page_number
        self.min_x = min_x
        self.min_y = min_y
        self.max_x = max_x
        self.max_y = max_y

    def has_vertical_overlap(self, other):
        """
        Checks if this PDF position overlaps the given other PDF position 
        horizontally and/or vertically.
        """
        if other is None:
            return False
            
        if self.page_number != other.page_number:
            return False

        return max(self.min_y, other.min_y) <= min(self.max_y, other.max_y)

    def extend(self, other):
        """
        Extends this PDF position by the given other PDF position.
        """
        if other is None:
            return
            
        if self.page_number != other.page_number:
            return
            
        self.min_x = min(self.min_x, other.min_x)
        self.min_y = min(self.min_y, other.min_y)
        self.max_x = max(self.max_x, other.max_x)
        self.max_y = max(self.max_y, other.max_y)

        return False

    def __str__(self):
        return "PdfPosition(%s, %s, %s, %s, %s)" \
            % (self.page_number, self.min_x, self.min_y, self.max_x, self.max_y)
            
    def __repr__(self):
        return self.__str__()


# =============================================================================


def is_missing_or_empty_file(path):
    """
    Returns true, if the given file_path does not exist or if the content of
    the file is empty.
    """
    if path is None:
        return True
    return not os.path.isfile(path) or os.path.getsize(path) == 0


def ensure_dirs(file_path):
    """
    Tries to create all parent directories of given file_path. Returns True, if
    all directories could be created successfully (or already existed). Returns
    False otherwise.
    """
    directory, file_name = os.path.split(file_path)
    # Try to create the directory if it doesn't exist.
    try:
        os.makedirs(directory, exist_ok=True)
    except:
        # Return False if the directory couldn't be created.
        return False
    return True
