# The Evaluation

**TODO**: The content of `output/` is still missing.

The following 13 PDF extraction tools were evaluated on their semantic abilities to extract the body texts from PDF files of scientific articles:

[pdftotext](https://poppler.freedesktop.org/), 
[pdftohtml](https://poppler.freedesktop.org/), 
[pdf2xml (Xerox)](https://sourceforge.net/projects/pdf2xml/), 
[pdf2xml (Tiedemann)](https://bitbucket.org/tiedemann/pdf2xml/), 
[PdfBox](https://github.com/apache/pdfbox), 
[ParsCit](https://github.com/knmnyn/ParsCit), 
[LA-PdfText](https://github.com/BMKEG/lapdftext), 
[PdfMiner](https://github.com/euske/pdfminer/), 
[pdfXtk](https://github.com/tamirhassan/pdfxtk), 
[pdf-extract](https://github.com/CrossRef/pdfextract), 
[PDFExtract](https://github.com/elacin/PDFExtract), 
[Grobid](https://github.com/kermitt2/grobid), 
[Icecite](https://github.com/ckorzen/icecite).

Each tool was used to extract texts from the [PDF files of the benchmark](../benchmark/pdf). 
For each tool, reasonable input parameters were selected in order to get output files that reflect, as far as possible, the
structure of [ground truth files](../benchmark/groundtruth).

For tools with XML output, the output was translated to plain text by identifying the relevant text parts.
If semantic roles were provided, only those logical text blocks were selected, which are also present in the ground truth files. 
If texts were broken down into any kind of blocks (like paragraphs, columns, or sections), they were separated by blank lines (like in the ground truth files).

## Basic Structure

There are three folders:

+ [`bin`](bin) contains all files needed to manage the extraction processes and to measure the evaluation criteria (see below).
+ [`output`](output) contains the output files of the extraction tools for each PDF file of the benchmark. <br/>
 Files ending with `.raw.txt` contain the original outputs of tools (usually in XML or plain text format). <br/>
 Files ending with `.final.txt` are the plain text files, translated from the original output.<br/>
+ [`tools`](tools) contains tool-specific subfolders, one for each evaluated tool. Each contains
 * a folder `bin` that contains the binaries of a tool to be used on the extraction process.
 * a file `tool_extractor.py` that contains the code to translate a `.raw.txt` file into a `.final.txt` file.
 * a file `config.ini` defining some tool-specific metadata, in particular the *command* to use on the extraction process.
 * a file `notices.txt` that contains some hints about the performed steps and some issues occurred on installing the tool.
 
For illustration, consider the folder [`tools/pdftotext`](tools/pdftotext).
It contains the folder [`tools/pdftotext/bin`](tools/pdftotext/bin) with the executable `pdftotext` that is used on the extraction process.
The file [`tools/pdftotext/config.ini`](tools/pdftotext/config.ini) with content
  
```
[general]
name = pdftotext
url = https://poppler.freedesktop.org/
info = Converts PDF files to plain text files.

[run]
cmd = ./bin/pdftotext -nopgbrk $IN $OUT

[extractor]
name = tool_extractor.PdfToTextExtractor
```

defines the name, a project url, a short info, the command to use on extraction (where `$IN` is a placeholder for the path to a PDF file path and `$OUT` is placeholder for the path to the output file) and the name of the class in `tool_extractor.py`.

## Evaluation Criteria

On evaluating a tool, each of its `.final.txt` output files is compared with the equivalent ground truth file.
The following evaluation criteria are measured:

+ **NL+**: the number of spurious newlines in the output file.
+ **NL–**: the number of missing newlines in the output file.
+ **P+**: the number of spurious paragraphs in the output file.
+ **P-**: the number of missing paragraphs in the output file.
+ **P<>**: the number of rearranged paragraphs in the output file.
+ **W+**: the number of spurious words in the output file.
+ **W–**: the number of missing words in the output file.
+ **W~-**: the number of misspelled words in the output file.

## Evaluation Results

The following table summarizes the evaluation results of each evaluated tool, broken down by the introduced criteria.

Each criteria is given by 2 numbers.
(1) An absolute number, and (2) a percentage, that gives, in case of NL+ and NL–, the absolute number relative to the number of newlines in the ground truth files and, in all other cases, the number of affected words relative to the number of words in the ground truth files.

The column ERR gives the number of PDF files which could not be processed by a tool.
The column T∅ gives the average time needed to process a single PDF file, in seconds.
The best values in each column are printed in bold. 

| Tool                | NL+  | NL–  | P+   | P-   | P<>  | W+   | W-   | W~   | ERR  | T∅   |
| ------------------- |:----:|:----:|:----:|:----:|:----:|:----:|:----:|:----:|:----:|:----:|
| pdftotext | 14 <br> <sup>(16%)</sup> | 44 <br> <sup>(53%)</sup> | 60 <br> <sup>(29%)</sup> | 2.3 <br> <sup>(0.6%)</sup> | 1.4 <br> <sup>(1.9%)</sup> | 24 <br> <sup>(0.7%)</sup> | 2.4 <br> <sup>(0.1%)</sup> | 41 <br> <sup>(1.2%)</sup> | 2 | **0.3** |
| pdftohtml | 3.6 <br> <sup>(4.3%)</sup> | 70 <br> <sup>(84%)</sup> | 9.2 <br> <sup>(31%)</sup> | 4.2 <br> <sup>(3.2%)</sup> | 0.1 <br> <sup>(0.1%)</sup> | 16 <br> <sup>(0.5%)</sup> | 1.6 <br> <sup>(0.0%)</sup> | 95 <br> <sup>(2.9%)</sup> | **0** | 2.2 |
| pdftoxml | 33 <br> <sup>(40%)</sup> | 20 <br> <sup>(25%)</sup> | 80 <br> <sup>(31%)</sup> | 1.8 <br> <sup>(0.5%)</sup> | 0.1 <br> <sup>(0.1%)</sup> | 21 <br> <sup>(0.6%)</sup> | 1.5 <br> <sup>(0.0%)</sup> | 154 <br> <sup>(4.7%)</sup> | 1 | 0.7 |
| PdfBox | **3.0** <br> <sup>**(3.6%)**</sup> | 70 <br> <sup>(85%)</sup> | 7.6 <br> <sup>(27%)</sup> | **0.9** <br> <sup>**(0.2%)**</sup> | 0.0 <br> <sup>(0.1%)</sup> | 17 <br> <sup>(0.5%)</sup> | **1.5** <br> <sup>**(0.0%)**</sup> | 53 <br> <sup>(1.6%)</sup> | 2 | 8.8 |
| pdf2xml | 33 <br> <sup>(40%)</sup> | 39 <br> <sup>(48%)</sup> | 44 <br> <sup>(21%)</sup> | 40 <br> <sup>(30%)</sup> | 7.8 <br> <sup>(9.5%)</sup> | 8.6 <br> <sup>(0.3%)</sup> | 3.6 <br> <sup>(0.1%)</sup> | 34 <br> <sup>(0.9%)</sup> | 1444 | 37 |
| ParsCit | 15 <br> <sup>(18%)</sup> | 39 <br> <sup>(47%)</sup> | 10 <br> <sup>(10%)</sup> | 14 <br> <sup>(6.4%)</sup> | 1.3 <br> <sup>(1.8%)</sup> | 16 <br> <sup>(0.5%)</sup> | 2.3 <br> <sup>(0.1%)</sup> | 37 <br> <sup>(1.1%)</sup> | 1 | 6.8 |
| LA-PdfText | 5.5 <br> <sup>(6.4%)</sup> | 23 <br> <sup>(28%)</sup> | **4.8** <br> <sup>**(3.1%)**</sup> | 52 <br> <sup>(73%)</sup> | 2.9 <br> <sup>(5.9%)</sup> | **5.7** <br> <sup>**(0.1%)**</sup> | 6.1 <br> <sup>(0.1%)</sup> | 26 <br> <sup>(0.6%)</sup> | 324 | 24 |
| PdfMiner | 32 <br> <sup>(38%)</sup> | 18 <br> <sup>(21%)</sup> | 84 <br> <sup>(30%)</sup> | 3.6 <br> <sup>(1.0%)</sup> | 1.4 <br> <sup>(2.1%)</sup> | 34 <br> <sup>(1.0%)</sup> | 2.6 <br> <sup>(0.1%)</sup> | 110 <br> <sup>(3.3%)</sup> | 23 | 16 |
| pdfXtk | 7.9 <br> <sup>(9.7%)</sup> | 68 <br> <sup>(84%)</sup> | 12 <br> <sup>(29%)</sup> | 4.5 <br> <sup>(3.5%)</sup> | 0.1 <br> <sup>(0.1%)</sup> | 59 <br> <sup>(1.8%)</sup> | 6.1 <br> <sup>(0.2%)</sup> | 95 <br> <sup>(3.0%)</sup> | 739 | 22 |
| pdf-extract | 95 <br> <sup>(114%)</sup> | 53 <br> <sup>(64%)</sup> | 99 <br> <sup>(32%)</sup> | 8.4 <br> <sup>(3.1%)</sup> | 4.1 <br> <sup>(7.7%)</sup> | 74 <br> <sup>(2.1%)</sup> | 41 <br> <sup>(1.2%)</sup> | 149 <br> <sup>(4.2%)</sup> | 72 | 34 |
| PDFExtract | 9.5 <br> <sup>(11%)</sup> | 33 <br> <sup>(40%)</sup> | 28 <br> <sup>(21%)</sup> | 22 <br> <sup>(25%)</sup> | 0.8 <br> <sup>(0.9%)</sup> | 12 <br> <sup>(0.4%)</sup> | 2.8 <br> <sup>(0.1%)</sup> | 61 <br> <sup>(1.8%)</sup> | 176 | 46 |
| Grobid | 9.5 <br> <sup>(11%)</sup> | 30 <br> <sup>(36%)</sup> | 7.5 <br> <sup>(6.7%)</sup> | 11 <br> <sup>(15%)</sup> | **0.0** <br> <sup>**(0.0%)**</sup> | 14 <br> <sup>(0.4%)</sup> | 1.6 <br> <sup>(0.0%)</sup> | 63 <br> <sup>(1.9%)</sup> | 29 | 42 |
| Icecite | 3.4 <br> <sup>(4.0%)</sup> | **10** <br> <sup>**(13%)**</sup> | 6.2 <br> <sup>(4.2%)</sup> | 7.7 <br> <sup>(5.5%)</sup> | 0.1 <br> <sup>(0.1%)</sup> | 10 <br> <sup>(0.3%)</sup> | 1.7 <br> <sup>(0.1%)</sup> | **21** <br> <sup>**(0.6%)**</sup> | 34 | 41 |

## Usage

To automate the extraction processes, you can use the file [`bin/extractor.py`](bin/extractor.py). 
Type `bin/extractor.py --help` to get detailed usage infos.

Equivalently, to evaluate the output files against the ground truth, use the file [`bin/evaluator.py`](bin/evaluator.py). 
Type `bin/evaluator.py --help` to get detailed usage infos.

The [`Makefile`](Makefile) defines rules `extract` and `evaluate` that calls the executables with values adapted to our project.
Call it by typing `make extract` or `make evaluate`.
