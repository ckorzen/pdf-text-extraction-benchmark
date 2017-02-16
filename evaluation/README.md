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

**TODO**: Introduce the evaluation criteria.
On evaluation, each `.final.txt` is compared with the equivalent ground truth file.

## Evaluation Results

**TODO**: Continue the table.

| Tool                | NL+  | NL-  | P+   | P-   | P<>  | W+   | W-   | W~   | ERR  | T    |
| ------------------- |:----:|:----:|:----:|:----:|:----:|:----:|:----:|:----:|:----:|:----:|
| pdftotext           | 14 <br/> <sup>(16%)</sup> | 44 <br/> <sup>(53%)</sup> | 60 <br/> <sup>(29%)</sup> | 2.3 <br/> <sup>(0.6%)</sup> | 1.4 <br/> <sup>(1.9%)</sup> | 24 <br/> <sup>(0.7%)</sup> | 2.4 <br/> <sup>(0.1%)</sup> | 41 <br/> <sup>(1.2%)</sup> | 2 <br/> <sup> </sup>   | 0.3 <br/> <sup> </sup> |
| pdftohtml           |      |      |      |      |      |      |      |      |      |      |
| pdf2xml (Xerox)     |      |      |      |      |      |      |      |      |      |      |
| pdf2xml (Tiedemann) |      |      |      |      |      |      |      |      |      |      |
| PdfBox              |      |      |      |      |      |      |      |      |      |      |
| ParsCit             |      |      |      |      |      |      |      |      |      |      |
| LA-PdfText          |      |      |      |      |      |      |      |      |      |      |
| PdfMiner            |      |      |      |      |      |      |      |      |      |      |
| pdfXtk              |      |      |      |      |      |      |      |      |      |      |
| pdf-extract         |      |      |      |      |      |      |      |      |      |      |
| PDFExtract          |      |      |      |      |      |      |      |      |      |      |
| Grobid              |      |      |      |      |      |      |      |      |      |      |
| Icecite             |      |      |      |      |      |      |      |      |      |      |


## Usage

**TODO**: Introduce the usage.
