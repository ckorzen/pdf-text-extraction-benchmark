# The Benchmark Generator

## Usage

The executable can be found at `bin/benchmark-generator.jar`. To execute it, make sure that you use *Java 1.8* or later. The usage is as follows:

```
java -jar bin/benchmark-generator.jar
   -f,--format <arg>      The format of output files [txt, xml, json], Default: "txt".
   -h,--help              Prints the help.
   -i,--input <arg>       A single TeX file or a directory which is scanned recursively for TeX files.
   -o,--output <arg>      The output file (if the input is a single TeX file) or the output directory.
   -p,--prefix <arg>      Only consider TeX files which starts with the given prefix(es).
   -r,--role <arg>        Only extract the logical text blocks with given semantic role(s). 
                          [abstract, affiliation, authors, acknowledgements, body, caption, figure, 
                           footnote, formula, heading, listing-item, table, title, reference]
   -s,--suffix <arg>      The suffix to use on creating the ground truth files. Default: ".txt"
```

The parameters `--input` and `--output` are mandatory.
For parameters `--prefix` and `--role`, you can define multiple values by setting the parameters multiple times. For example, if you wish to extract *title* and *abstract*, type

```
java -jar bin/benchmark-generator.jar --input [...] --output [...] --role title --role abstract
```


There is also a Makefile, defining a rule `benchmark` that calls the executable with values adapted to our project:

```
BENCHMARK_GENERATOR_JAR = ./bin/benchmark-generator.jar
INPUT = ../benchmark/src/
OUTPUT = ../benchmark/groundtruth/
SUFFIX = ".body.txt"
ROLE = "body"
FORMAT = "txt"
benchmark: 
	@java -jar $(BENCHMARK_GENERATOR_JAR) \
		--input $(INPUT) \
		--output $(OUTPUT) \
		--suffix $(SUFFIX) \
		--role $(ROLE) \
		--format $(FORMAT) 
 ```
 
Call it by typing `make benchmark`.

## Compiling the sources

If you wish to compile the source codes, make sure your system fulfills the following prerequisites:

+ Linux-based system.
+ Git 2.5 or later.
+ Java JDK 1.8 or later.
+ Apache Maven 3.3 or later.

Further, you need the dependency [`commons`](https://github.com/ckorzen/commons). Download it by typing

```
git clone https://github.com/ckorzen/commons.git
```

in a folder of your choice and install it by typing

```
cd commons
mvn -DskipTests install 
```

Back to `benchmark-generator/`, type `make compile-benchmark-generator`. This will compile the source codes and recreate the executable `bin/benchmark-generator.jar`.
 
## The Basic Structure

Details about the basic file/folder structure.

## The Approach

Details about the basic approach of the benchmark generator.

### The Rules

Details about the rules used on identifying logical text blocks.

## Usage

Details on prerequisites and how the benchmark generator can be used.
