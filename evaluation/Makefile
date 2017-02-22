PYTHON = @python3
WGET = wget --mirror -q --show-progress -np -nH --cut-dirs 3 --reject "index.html*"
LN = ln -s

CHECKSTYLE = @flake8
TEST_BINARIES = $(wildcard *_test.py)

TOOLS = pdftotext pdftohtml pdftoxml pdfbox pdf2xml parscit lapdftext pdfminer pdfXtk pdfextract PDFExtract grobid icecite

GROUND_TRUTH_DIR = ../benchmark/groundtruth/data
PDF_DIR = ../benchmark/pdf/data
TOOLS_DIR = ./tools/data
OUTPUT_DIR = ./output/data

EVALUATOR_SRC = ./bin/evaluator.py
EXTRACTOR_SRC = ./bin/extractor.py
TABLE_GENERATOR_SRC = ./bin/utils/table_generator.py

PREFIX = 
GROUNDTRUTH_EXT = .body.txt
PDF_EXT = .pdf
YY = 
MM = 
REARRANGE = True
CASE_INSENSITIVE = True
JUNK = \\[formula\\] \\[table\\] \\[figure\\] \\[\\\\cite=.+\\] \\[\\\\citep=.+\\] \\[\\\\citet=.+\\] \\[\\\\citealp=.+\\] \\[\\\\citealt=.+\\] \\[\\\\citetext=.+\\] \\[\\\\citeauthor=.+\\] \\[\\\\citeyear=.+\\] \\[\\\\citeyearpar=.+\\] \\[\\\\Citep=.+\\] \\[\\\\Citet=.+\\] \\[\\\\Citealp=.+\\] \\[\\\\Citealt=.+\\] \\[\\\\Citetext=.+\\] \\[\\\\Citeauthor=.+\\] \\[\\\\Citeyear=.+\\] \\[\\\\Citeyearpar=.+\\] \\[\\\\citetalias=.+\\] \\[\\\\citepalias=.+\\] \\[\\\\\ref=.+\\] \\[\\\\\eqref=.+\\] 
NUM_THREADS = -1
MUTE = False
FORCE = False
TIMEOUT = 300
TABLE_TYPES = 

TOOLS_DIR_DOWNLOAD_URL = http://arxiv-benchmark.informatik.uni-freiburg.de/data/evaluation/tools/
TOOLS_DIR_LOCAL_PATH = /nfs/raid1/arxiv/benchmark-data/evaluation/tools/

OUTPUT_DIR_DOWNLOAD_URL = http://arxiv-benchmark.informatik.uni-freiburg.de/data/evaluation/output/
OUTPUT_DIR_LOCAL_PATH = /nfs/raid1/arxiv/benchmark-data/evaluation/output/

extract: 
	$(PYTHON) $(EXTRACTOR_SRC) \
		$(TOOLS) \
		--tools_dir "$(TOOLS_DIR)" \
		--pdf_dir "$(PDF_DIR)" \
		--output_dir "$(OUTPUT_DIR)" \
		--prefix_filter "$(PREFIX)" \
		--suffix_filter "$(PDF_EXT)" \
		--yy_filter "$(YY)" \
		--mm_filter "$(MM)" \
		--num_threads "$(NUM_THREADS)" \
		--timeout "$(TIMEOUT)" \
		--force "$(FORCE)" \
		--mute "$(MUTE)"

evaluate: 
	$(PYTHON) $(EVALUATOR_SRC) \
		$(TOOLS) \
		--output_dir "$(OUTPUT_DIR)" \
		--groundtruth_dir "$(GROUND_TRUTH_DIR)" \
		--prefix_filter "$(PREFIX)" \
		--suffix_filter "$(GROUNDTRUTH_EXT)" \
		--yy_filter "$(YY)" \
		--mm_filter "$(MM)" \
		--rearrange "$(REARRANGE)" \
		--case_insensitive "$(CASE_INSENSITIVE)" \
		--junk $(JUNK) \
		--num_threads "$(NUM_THREADS)" \
		--force "$(FORCE)" \
		--mute "$(MUTE)"

table:
	$(PYTHON) $(TABLE_GENERATOR_SRC) \
		$(TOOLS) \
		--table_type $(TABLE_TYPES) \
		--output_dir "$(OUTPUT_DIR)" \
		--groundtruth_dir "$(GROUND_TRUTH_DIR)" \
		--prefix_filter "$(PREFIX)" \
		--suffix_filter "$(GROUNDTRUTH_EXT)" \
		--yy_filter "$(YY)" \
		--mm_filter "$(MM)" \
		--rearrange "$(REARRANGE)" \
		--case_insensitive "$(CASE_INSENSITIVE)" \
		--junk $(JUNK) \
		--num_threads "$(NUM_THREADS)" \
		--force "$(FORCE)" \
		--mute "True"

list-tools:
	@echo $(shell find ./tools -mindepth 1 -maxdepth 1 -type d -printf "%f ")

# =============================================================================
# Data.

download-data: download-data-tools download-data-output

download-data-tools:
	$(WGET) $(TOOLS_DIR_DOWNLOAD_URL) -P $(TOOLS_DIR)

download-data-output:
	$(WGET) $(OUTPUT_DIR_DOWNLOAD_URL) -P $(OUTPUT_DIR)


link-data: link-data-tools link-data-output

link-data-tools:
	rm -rf $(TOOLS_DIR)
	$(LN) $(TOOLS_DIR_LOCAL_PATH) $(TOOLS_DIR)

link-data-output:
	rm -rf $(OUTPUT_DIR)
	$(LN) $(OUTPUT_DIR_LOCAL_PATH) $(OUTPUT_DIR)

# =============================================================================

checkstyle:
	$(CHECKSTYLE) $(shell find . -mindepth 0 -maxdepth 5 -not -path "./tools/*/bin/*" -type f -name '*.py')

clean:
	@rm -rf $(shell find . -mindepth 0 -maxdepth 5 -name '*.pyc')
	@rm -rf $(shell find . -mindepth 0 -maxdepth 5 -name '__pycache__')
	@rm -rf $(shell find . -mindepth 0 -maxdepth 5 -name '*.log')
	@rm -rf $(shell find . -mindepth 0 -maxdepth 5 -name '*.log.1')
