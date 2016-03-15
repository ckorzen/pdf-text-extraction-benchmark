SHELL := /bin/bash

ARXIV_BENCHMARK_SRC = ../arxiv-benchmark
ARXIV_BENCHMARK_JAR = ./bin/arxiv-benchmark.jar
PDF_PARSER_SRC = ../../pdf-engine
PDF_PARSER_JAR_SRC = ../../pdf-cli/target/*-with-dependencies.jar
PDF_PARSER_JAR_TARGET = ./bin/icecite-pdf-parser.jar

EXTRACTOR_SRC = ./bin/extractor.py
EVALUATOR_SRC = ./bin/evaluator-$(TOOL).py

INPUT = ./input
OUTPUT = ./output

TEX_SRC = $(INPUT)/src
GROUNDTRUTH_TARGET = $(OUTPUT)/groundtruth
EVALUATION_TARGET = /home/korzen/Downloads/result.txt

PDF_SRC = $(INPUT)/pdf
TOOL_TARGET = $(OUTPUT)/$(TOOL)

MAX_DISTANCE = 0
MIN_SIMILARITY = 0.5
REARRANGE = True 
IGNORE_CASES = True
JUNK = \[formula\] \[table\] \[figure\] \[\\\cite=(.+)\] \[\\\ref=(.+)\]

# The command of tools.
pdftotext_CMD = pdftotext %IN %OUT
pdftohtml_CMD = pdftohtml -xml -zoom 1 -nomerge -stdout -q -enc "UTF-8" %IN > %OUT 
icecite_CMD = java -jar $(PDF_PARSER_JAR_TARGET) --feature words --format txt %IN  %OUT

all: extract evaluate

# Compile the groundtruth maker.
compile-arxiv-benchmark:
	mvn -f $(ARXIV_BENCHMARK_SRC)/pom.xml -DskipTests install
	@cp $(ARXIV_BENCHMARK_SRC)/target/*-with-dependencies.jar $(ARXIV_BENCHMARK_JAR) 

# Make the groundtruth files (a file containing the whole content and a file 
# containing only the body text)
arxiv-benchmark: arxiv-benchmark-full arxiv-benchmark-body

# Make groundtruth files containing the whole content from tex files.
arxiv-benchmark-full:
	@echo "*************************"
	@echo "Making groundtruth files."
	@echo "*************************"
	@java -jar $(ARXIV_BENCHMARK_JAR) \
		-input $(TEX_SRC) \
		-output $(GROUNDTRUTH_TARGET) \
		-prefix $(PREFIX) \
		-suffix ".full.txt"

# Make groundtruth files containing only the body texts from tex files.
arxiv-benchmark-body:
	@echo "*************************"
	@echo "Making groundtruth files."
	@echo "*************************"

	@java -jar $(ARXIV_BENCHMARK_JAR) \
		-input $(TEX_SRC) \
		-output $(GROUNDTRUTH_TARGET) \
		-prefix $(PREFIX) \
		-suffix ".body.txt" \
		-feature "body"

# Show an excerpt of the groundtruth files.
groundtruth-excerpt:
	@for FILE in $(GROUNDTRUTH_TARGET)/*/$(PREFIX)* ; do \
		if [ -f "$$FILE" ]; then \
			cat $$FILE | head -c 500 ; \
		fi ; \
		echo "*************" ; \
	done

# Extract stuff from the pdf source using the extraction tool defined by $TOOL.
extract: validate   
	@echo $($(TOOL)_CMD)
	@echo "Extracting from \"$(PDF_SRC)\" using the tool \"$(TOOL)\"".
	@python3 $(EXTRACTOR_SRC) --prefix "$(PREFIX)" "$($(TOOL)_CMD)" "$(PDF_SRC)" "$(TOOL_TARGET)" 
  
extract-excerpt:
	@for FILE in $(TOOL_TARGET)/*/$(PREFIX)* ; do \
		if [ -f "$$FILE" ]; then \
			cat $$FILE | head -c 500 ; \
		fi ; \
		echo "*************" ; \
	done

# Evaluate the stuff extracted from the given extraction tool. 
evaluate: 
	@echo "Evaluating the para extraction of the tool \"$(TOOL)\"".
	@python3 $(EVALUATOR_SRC) \
		--max_dist "$(MAX_DISTANCE)" \
		--min_similarity "$(MIN_SIMILARITY)" \
		--prefix "$(PREFIX)" \
		--output "$(EVALUATION_TARGET)" \
		"$(TOOL_TARGET)" $(GROUNDTRUTH_TARGET)
	@echo "*************"
	@column -t -s"	" /home/korzen/Downloads/result.txt
	@echo "*************"
	
# Evaluate the accuracy of words extraction.
evaluate-words: validate
	@python3 $(EVALUATOR_SRC) \
		--feature word \
		--prefix "$(PREFIX)" \
		--rearrange "$(REARRANGE)" \
		--ignore_cases "$(IGNORE_CASES)" \
		--junk "$(JUNK)" \
		--max_dist "$(MAX_DISTANCE)" \
		--output "$(EVALUATION_TARGET)" \
		"$(TOOL_TARGET)" $(GROUNDTRUTH_TARGET)
#@echo "*************"
#@column -t -s"	" /home/korzen/Downloads/result.txt
#@echo "*************"

# Evaluate the accuracy of paragraph extraction.
evaluate-paragraphs: validate
	@echo "Evaluating the para extraction of the tool \"$(TOOL)\"".
	@python3 $(EVALUATOR_SRC) \
		--feature paragraph \
		--prefix "$(PREFIX)" \
		--rearrange "$(REARRANGE)" \
		--ignore_cases "$(IGNORE_CASES)" \
		--junk "$(JUNK)" \
		--min_sim "$(MIN_SIMILARITY)" \
		--output "$(EVALUATION_TARGET)" \
		"$(TOOL_TARGET)" $(GROUNDTRUTH_TARGET)
#@echo "*************"
#@column -t -s"	" /home/korzen/Downloads/result.txt
#@echo "*************"
    
# Evaluate the accuracy of body extraction.
evaluate-body: validate
	@echo "Evaluating the para extraction of the tool \"$(TOOL)\"".
	@python3 $(EVALUATOR_SRC) \
		--feature body \
		--prefix "$(PREFIX)" \
		--ignore_cases "$(IGNORE_CASES)" \
		--junk "$(JUNK)" \
		--max_dist "$(MAX_DISTANCE)" \
		--output "$(EVALUATION_TARGET)" \
		"$(TOOL_TARGET)" $(GROUNDTRUTH_TARGET)
#@echo "*************"
#@column -t -s"	" /home/korzen/Downloads/result.txt
#@echo "*************"

compile-icecite-parser:
	mvn -f $(PDF_PARSER_SRC)/pom.xml -DskipTests install
	cp $(PDF_PARSER_JAR_SRC) $(PDF_PARSER_JAR_TARGET) 

compare-words:
	wdiff $(GROUNDTRUTH_TARGET)/*/$(PREFIX)*.full.txt $(TOOL_TARGET)/*/$(PREFIX)*.txt -w $$'\033[30;41m' -x $$'\033[0m' -y $$'\033[30;42m' -z $$'\033[0m' -in | less -R
	#paste <(fold -w 80 -s $(GROUNDTRUTH_TARGET)/*/$(PREFIX)*.full.txt | sed -e 's/^$$/ /g') \
	#<(fold -w 80 -s $(TOOL_TARGET)/*/$(PREFIX)*.txt | sed -e 's/^$$/ /g') | column -t -s"	" | less
	
compare-body:
	paste <(fold -w 80 -s $(GROUNDTRUTH_TARGET)/*/$(PREFIX)*.body.txt | sed -e 's/^$$/ /g') \
	<(fold -w 80 -s $(TOOL_TARGET)/*/$(PREFIX)*.txt | sed -e 's/^$$/ /g') | column -t -s"	" | less

# Validate that the TOOL variable is set properly.
validate:
	$(if $($(TOOL)_CMD),,$(error The tool "$(TOOL)" is not supported))

# Print a list of supported extraction tools.
list-tools:
# From the variables, filter those that ends with "CMD" and strip of the "_CMD".
	@echo $(subst _CMD,,$(filter %_CMD, $(.VARIABLES)))

