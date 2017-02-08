SHELL := /bin/bash

ARXIV_BENCHMARK_SRC = ../arxiv-benchmark
ARXIV_BENCHMARK_JAR = ./bin/arxiv-benchmark.jar
PDF_PARSER_SRC = ../../pdf-parent
PDF_PARSER_JAR_SRC = ../../pdf-cli/target/*-with-dependencies.jar
PDF_PARSER_JAR_TARGET = ./bin/icecite/icecite-pdf-parser.jar

EXTRACTOR_SRC = ./bin/extractor-$(TOOL).py
EVALUATOR_SRC = ./bin/evaluator.py
TEX_TABLE_CREATOR_SRC = ./bin/tex_table_creator.py

INPUT = ./input
INPUT_SAMPLE = ./input_sample

OUTPUT = ./output
OUTPUT_SAMPLE = ./output_sample

REARRANGE = True 
IGNORE_CASES = True
JUNK = \[formula\] \[table\] \[figure\] \[\\\cite=.+\] \[\\\citep=.+\] \[\\\citet=.+\] \[\\\citealp=.+\] \[\\\citealt=.+\] \[\\\citetext=.+\] \[\\\citeauthor=.+\] \[\\\citeyear=.+\] \[\\\citeyearpar=.+\] \[\\\Citep=.+\] \[\\\Citet=.+\] \[\\\Citealp=.+\] \[\\\Citealt=.+\] \[\\\Citetext=.+\] \[\\\Citeauthor=.+\] \[\\\Citeyear=.+\] \[\\\Citeyearpar=.+\] \[\\\citetalias=.+\] \[\\\citepalias=.+\] \[\\\\ref=.+\] \[\\\\eqref=.+\] 
RECOMPUTE_REARRANGES = False
RECOMPUTE_NUM_OPS = False

PROCESSING_TYPE = parallel
NUM_THREADS = -1
DIRS = 
CRITERION = "ALL"
TIMEOUT=300

TOOL=icecite
TOOLS=$(subst _CMD,,$(filter %_CMD, $(.VARIABLES)))

SIDEBAR_WIDTH=200
SCREEN_WIDTH=$$(xrandr --current | grep '*' -m 1 | awk '{print $$1}' | cut -d 'x' -f1) 
SCREEN_HEIGHT=$$(xrandr --current | grep '*' -m 1 | awk '{print $$1}' | cut -d 'x' -f2)
AVAILABLE_WIDTH = $$(( $(SCREEN_WIDTH) - $(SIDEBAR_WIDTH) ))
DEBUG_LEFT_X=$(SIDEBAR_WIDTH)
DEBUG_LEFT_Y=0
DEBUG_LEFT_WIDTH=$$(( $(AVAILABLE_WIDTH) / 2 ))
DEBUG_LEFT_HEIGHT=$(SCREEN_HEIGHT)
DEBUG_RIGHT_X=$$(( $(DEBUG_LEFT_X) + $(DEBUG_LEFT_WIDTH) )) 
DEBUG_RIGHT_Y=0
DEBUG_RIGHT_WIDTH=$$(( $(AVAILABLE_WIDTH) / 2 ))
DEBUG_RIGHT_HEIGHT=$(SCREEN_HEIGHT)

DEBUG_LEFT_X_EVAL = $(shell echo $(DEBUG_LEFT_X) )
DEBUG_LEFT_Y_EVAL = $(shell echo $(DEBUG_LEFT_Y) )
DEBUG_LEFT_WIDTH_EVAL = $(shell echo $(DEBUG_LEFT_WIDTH) )
DEBUG_LEFT_HEIGHT_EVAL = $(shell echo $(DEBUG_LEFT_HEIGHT) )
DEBUG_RIGHT_X_EVAL = $(shell echo $(DEBUG_RIGHT_X) )
DEBUG_RIGHT_Y_EVAL = $(shell echo $(DEBUG_RIGHT_Y) )
DEBUG_RIGHT_WIDTH_EVAL = $(shell echo $(DEBUG_RIGHT_WIDTH) )
DEBUG_RIGHT_HEIGHT_EVAL = $(shell echo $(DEBUG_RIGHT_HEIGHT) )


# The available tools with info and the command to execute.

pdftotext_INFO = https://poppler.freedesktop.org/ - Converts PDF files to plain text files. 
pdftotext_CMD = pdftotext -nopgbrk %IN %OUT

pdftohtml_INFO = https://poppler.freedesktop.org/ - Converts PDF files to HTML format retaining formatting.
# -i: ignore images
# -xml: xml output
# -zoom 1: set the zoom to "normal"
# -nomerge: do not merge paragraphs
# -stdout: use standard output
# -enc: output text encoding name
# -q: do not print any messages or errors
pdftohtml_CMD = pdftohtml -i -xml -zoom 1 -nomerge -stdout -q -enc "UTF-8" %IN > %OUT 

pdftoxml_INFO = https://sourceforge.net/projects/pdf2xml/ - Converts PDF files to XML files. 
pdftoxml_CMD = ./bin/pdftoxml/pdftoxml.linux64.exe.1.2_7 -noImage -blocks %IN %OUT 

pdf2xml_INFO = https://bitbucket.org/tiedemann/pdf2xml/ - Converts PDF to XML. By Jörg Tiedemann.
pdf2xml_CMD = ./bin/pdf2xml/pdf2xml %IN > %OUT 2> /dev/null

pdfbox_INFO = https://pdfbox.apache.org/ - Apache PDFBox® - A Java PDF Library
pdfbox_CMD = java -jar ./bin/pdfbox/pdfbox-app-2.0.3.jar ExtractText %IN %OUT > /dev/null 2> /dev/null

parscit_INFO = https://github.com/knmnyn/ParsCit - An open-source CRF Reference String Parsing Package
parscit_CMD = ./bin/ParsCit/bin/citeExtract.pl -m extract_all %IN %OUT > /dev/null 2> /dev/null

lapdftext_INFO = https://github.com/BMKEG/lapdftext - A system for extracting accurate text from PDF by Ramakrishnan et al.
lapdftext_CMD = java -cp bin/lapdftextProject/lapdftext/target/lapdftext-1.7.5-SNAPSHOT-jar-with-dependencies.jar edu.isi.bmkeg.lapdf.bin.ReadSectionText %IN  %OUT > /dev/null 2> /dev/null

pdfminer_INFO = http://www.unixuser.org/~euske/python/pdfminer/index.html - Python PDF parser and analyzer. Also known as pdf2txt.py.
pdfminer_CMD = ./bin/pdfminer/build/scripts-2.7/pdf2txt.py -o %OUT %IN > /dev/null 2> /dev/null

pdfXtk_INFO = https://github.com/tamirhassan/pdfxtk - PDF extraction toolkit by Tamir Hassan.
pdfXtk_CMD = java -cp bin/pdfxtk/trunk/pdfXtk/target/pdfXtk-0.9-SNAPSHOT-jar-with-dependencies.jar at.ac.tuwien.dbai.pdfwrap.ProcessFile -xmillum -spaces -encoding "UTF-8" %IN %OUT > /dev/null 2> /dev/null

#pdfx_INFO = http://pdfx.cs.man.ac.uk/ - Fully-automated PDF-to-XML conversion of scientific articles. By Alexandru Constantin et al.
#pdfx_CMD = curl -H 'Content-Type: application/pdf' -L 'http://pdfx.cs.man.ac.uk' --data-binary @'%IN' > '%OUT'

pdfextract_INFO = https://github.com/CrossRef/pdfextract - A tool and library that can extract various areas of text from a PDF by Karl Jonathan Ward.
pdfextract_CMD = ~/.rvm/gems/ruby-2.0.0-p648/wrappers/pdf-extract extract --regions %IN > %OUT

PDFExtract_INFO = https://github.com/elacin/PDFExtract - PDF text extraction utility by Øyvind Raddum Berg.
PDFExtract_CMD = java -jar bin/PDFExtract/PDFExtract/pdfextract-cli/target/pdfextract-cli-M3-SNAPSHOT-jar-with-dependencies.jar %IN %OUT > /dev/null

grobid_INFO = https://github.com/kermitt2/grobid - A machine learning software for extracting information from scholarly documents
grobid_CMD = java -Xmx1024m -jar ./bin/grobid/grobid-core/target/grobid-core-0.4.0.one-jar.jar -gH ./bin/grobid/grobid-home/ -gP ./bin/grobid/grobid-home/config/grobid.properties -ignoreAssets -dIn %IN -dOut %OUT -exe processFullText > /dev/null 2> /dev/null

icecite_INFO = https://github.com/ckorzen/icecite - PDF extraction toolkit by Claudius Korzen.
icecite_CMD = java -jar $(PDF_PARSER_JAR_TARGET) --feature paragraphs --role title --role body-text --role section-heading --role itemize-item --format txt %IN  %OUT > /dev/null 2> /dev/null

# citeseer_INFO = https://github.com/SeerLabs/CiteSeerExtractor
# citeseer_CMD = python ./bin/CiteSeerExtractor/src/command_line.py %IN %OUT > /dev/null

all: extract evaluate

# ------------------------------------------------------------------------------

# Compile the groundtruth maker.
compile-arxiv-benchmark:
	mvn -f $(ARXIV_BENCHMARK_SRC)/pom.xml -DskipTests install
	@cp $(ARXIV_BENCHMARK_SRC)/target/*-with-dependencies.jar $(ARXIV_BENCHMARK_JAR) 

# Make the groundtruth files (a file containing the whole content and a file 
# containing only the body text)
arxiv-benchmark: 
	java -jar $(ARXIV_BENCHMARK_JAR) \
		--input $(INPUT)/src/ \
		--output $(OUTPUT)/groundtruth/ \
		--suffix ".body.txt" \
		--role "body" \
		--format "txt"
#		--dirs $(DIRS)
#		--prefix $(PREFIX) \
#		--texmf "/home/korzen/arxiv-benchmark/tex-paragraph-parser/target/classes/texmf"

# Make groundtruth files containing only the body texts from tex files.
arxiv-benchmark-sample:
	java -jar $(ARXIV_BENCHMARK_JAR) \
		--input $(INPUT_SAMPLE)/src/ \
		--output $(OUTPUT_SAMPLE)/groundtruth/ \
		--suffix ".body.txt" \
		--role "body" \
		--format "txt" \
		--dirs $(DIRS)


# ------------------------------------------------------------------------------

# Extract stuff from the pdf source using the extraction tool defined by $TOOL.
extract:
	$(foreach t,$(TOOLS),$(MAKE) --no-print-directory extract_tool TOOL=$(t);)

extract_tool:
	@python3 $(EXTRACTOR_SRC) \
	    "$($(TOOL)_CMD)" "$(INPUT)/pdf" "$(OUTPUT)/$(TOOL)" \
	    --dir_filter "$(DIRS)" \
	    --prefix "$(PREFIX)" \
	    --num_threads "$(NUM_THREADS)" \
	    --criterion "$(CRITERION)" \
	    --timeout "$(TIMEOUT)"

extract_sample:
	$(foreach t,$(TOOLS),$(MAKE) --no-print-directory extract_tool_sample TOOL=$(t);)

extract_tool_sample: validate
	@echo "$($(TOOL)_CMD)"
	@python3 $(EXTRACTOR_SRC) \
	    "$($(TOOL)_CMD)" "$(INPUT_SAMPLE)/pdf" "$(OUTPUT_SAMPLE)/$(TOOL)" \
	    --dir_filter "$(DIRS)" \
	    --prefix "$(PREFIX)" \
	    --num_threads "$(NUM_THREADS)" \
	    --criterion "$(CRITERION)" \
	    --timeout "$(TIMEOUT)"

# Evaluate the stuff extracted from the given extraction tool. 
evaluate: 
	@python3 $(EVALUATOR_SRC) \
		"$(OUTPUT)" "$(TOOLS)" "$(OUTPUT)/groundtruth/" "$(INPUT)/pdf/" \
		--dir_filter "$(DIRS)" \
		--prefix "$(PREFIX)" \
		--suffix ".final.txt" \
		--rearrange "$(REARRANGE)" \
		--ignore_cases "$(IGNORE_CASES)" \
		--junk "$(JUNK)" \
		--num_threads "$(NUM_THREADS)" \
		--recompute_rearranged_snapshot "$(RECOMPUTE_REARRANGES)" \
		--recompute_num_ops_snapshot "$(RECOMPUTE_NUM_OPS)" 

evaluate_sample: 
	@python3 $(EVALUATOR_SRC) \
		"$(OUTPUT_SAMPLE)" "$(TOOLS)" "$(OUTPUT_SAMPLE)/groundtruth/" "$(INPUT_SAMPLE)/pdf/" \
		--dir_filter "$(DIRS)" \
		--prefix "$(PREFIX)" \
		--suffix ".final.txt" \
		--rearrange "$(REARRANGE)" \
		--ignore_cases "$(IGNORE_CASES)" \
		--junk "$(JUNK)" \
		--num_threads "$(NUM_THREADS)" \
		--recompute_rearranged_snapshot "$(RECOMPUTE_REARRANGES)" \
		--recompute_num_ops_snapshot "$(RECOMPUTE_NUM_OPS)" 

tex_table_sample:
	@python3 $(TEX_TABLE_CREATOR_SRC) \
		"$(OUTPUT_SAMPLE)" "$(TOOLS)" "$(OUTPUT_SAMPLE)/groundtruth/" "$(INPUT_SAMPLE)/pdf/"\
		--dir_filter "$(DIRS)" \
		--prefix "$(PREFIX)" \
		--suffix ".final.txt" \
		--rearrange "$(REARRANGE)" \
		--ignore_cases "$(IGNORE_CASES)" \
		--junk "$(JUNK)" \
		--num_threads "$(NUM_THREADS)" \
		--recompute_rearranged_snapshot "$(RECOMPUTE_REARRANGES)" \
		--recompute_num_ops_snapshot "$(RECOMPUTE_NUM_OPS)" 

compile-icecite-parser:
	mvn -f $(PDF_PARSER_SRC)/pom.xml -DskipTests install
	cp $(PDF_PARSER_JAR_SRC) $(PDF_PARSER_JAR_TARGET) 

# Validate that the TOOL variable is set properly.
validate:
	$(if $($(TOOL)_CMD),,$(error The tool "$(TOOL)" is not supported))

# Print a list of supported extraction tools.
list-tools:
# From the variables, filter those that ends with "CMD" and strip of the "_CMD".
	@echo $(subst _CMD,,$(filter %_CMD, $(.VARIABLES)))

info:
	@echo $($(TOOL)_INFO)

