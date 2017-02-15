BENCHMARK_GENERATOR_SRC = ./tex-paragraph-parser
BENCHMARK_GENERATOR_JAR = ./bin/benchmark-generator.jar

INPUT = ../benchmark/src/
OUTPUT = ../benchmark/groundtruth/

SUFFIX = ".body.txt"
ROLE = "body"
FORMAT = "txt"

compile-benchmark-generator:
	mvn -f $(BENCHMARK_GENERATOR_SRC)/pom.xml -DskipTests install
	@cp $(BENCHMARK_GENERATOR_SRC)/target/*-with-dependencies.jar $(BENCHMARK_GENERATOR_JAR)

benchmark: 
	@java -jar $(BENCHMARK_GENERATOR_JAR) \
		--input $(INPUT) \
		--output $(OUTPUT) \
		--suffix $(SUFFIX) \
		--role $(ROLE) \
		--format $(FORMAT) 
