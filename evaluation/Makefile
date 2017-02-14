PYTHON = @python3
CHECKSTYLE = @flake8
TEST_BINARIES = $(wildcard *_test.py)

TOOLS = $(shell $(MAKE) list-tools)

GROUND_TRUTH_DIR = ../benchmark/groundtruth
PDF_DIR = ../benchmark/pdf
TOOLS_DIR = ./tools
OUTPUT_DIR = ./output

EVALUATOR_SRC = ./bin/evaluator.py
EXTRACTOR_SRC = ./bin/extractor.py

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

list-tools:
	@echo $(shell find ./tools -mindepth 1 -maxdepth 1 -type d -printf "%f ")

###############################################################################

checkstyle:
	$(CHECKSTYLE) $(shell find . -mindepth 0 -maxdepth 5 -not -path "./tools/*/bin/*" -type f -name '*.py')

clean:
	@rm -rf $(shell find . -mindepth 0 -maxdepth 5 -name '*.pyc')
	@rm -rf $(shell find . -mindepth 0 -maxdepth 5 -name '__pycache__')
	@rm -rf $(shell find . -mindepth 0 -maxdepth 5 -name '*.log')
	@rm -rf $(shell find . -mindepth 0 -maxdepth 5 -name '*.log.1')
