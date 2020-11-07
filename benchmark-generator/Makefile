INPUT = /input
OUTPUT = /output
TMP_DIR = /tmp

SUFFIX = .body.txt
ROLE = body
FORMAT = txt

BENCHMARK_GENERATOR_SRC = /tex-paragraph-parser

help:
	@echo "Help"
	@echo "====="
	@echo "\"make groundtruth\": parse the TeX files in \"/input\" dir and write the produced ground truth file to \"/output\" dir."
	@echo "  Type \"make help-groundtruth\" for more information."
	@echo ""

groundtruth: 
	java -jar $(BENCHMARK_GENERATOR_SRC)/target/*-with-dependencies.jar \
			--input "$(INPUT)" \
			--output "$(OUTPUT)" \
			--tmpDir "$(TMP_DIR)" \
			--prefix "$(PREFIX)" \
			--suffix "$(SUFFIX)" \
			--role "$(ROLE)" \
			--format "$(FORMAT)"

help-groundtruth:
	@echo "make groundtruth"
	@echo "====="
	@echo
	@echo "Parses the input directory recursively for TeX files and writes the body text (in the format one paragraph per line, each separated by a blank line) of each TeX file to a separate ground truth file in the output directory."
	@echo ""
	@echo "Usage: make groundtruth [PREFIX=<str>] [SUFFIX=<str>] [ROLE=<role>]"
	@echo ""
	@echo "The meanings of the options are:"
	@echo " * PREFIX:  Only consider the TeX files in the input directory which starts with that prefix."
	@echo " * SUFFIX:  Use this suffix for the names of the ground truth files to be created. Default: \"$(SUFFIX)\""
	@echo " * ROLE:    Only extract the logical text blocks with the given semantic role. Choose from: [abstract, affiliation, authors, acknowledgements, body, caption, figure, footnote, formula, heading, listing-item, table, title, reference]"
	@echo ""