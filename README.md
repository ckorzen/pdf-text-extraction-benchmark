# A Benchmark & Evaluation for Text Extraction from PDF #

This project is about benchmarking and evaluating existing PDF extraction tools on their semantic abilities to extract the *body texts* from PDF documents, especially from *scientific articles*.
It provides (1) a benchmark generator, (2) a ready-to-use benchmark and (3) an extensive evaluation, with meaningful evaluation criteria.

## The Benchmark Generator
+ constructs high-quality benchmarks from *TeX source files*.
+ identifies the following 16 logical text blocks: 
*title*, *author(s)*, *affiliation(s)*, *date*, *abstract*, *headings*, *paragraphs of the body text*, *formulas*, *figures*, *tables*, *captions*, *listing-items*, *footnotes*, *acknowledgements*, *references*, *appendices*.
+ serializes desired logical text blocks to *plain text*, *XML* or *JSON* format.

For more details and usage, see [`benchmark-generator/`](benchmark-generator).

## The Benchmark
+ consists of *12,099 ground truth files* and *12,099 PDF files* of scientific articles, randomly selected from [*arXiv.org*](https://arxiv.org/).
Each ground truth file contains the *title*, the *headings* and the *body text paragraphs* of a particular scientific article.
+ was generated using the benchmark generated above.

For more details, see [`benchmark/`](benchmark).

## The Evaluation
+ assesses the following 13 PDF extraction tools:
[pdftotext](https://poppler.freedesktop.org/), [pdftohtml](https://poppler.freedesktop.org/), [pdf2xml (Xerox)](https://sourceforge.net/projects/pdf2xml/), [pdf2xml (Tiedemann)](https://bitbucket.org/tiedemann/pdf2xml/), [PdfBox](https://github.com/apache/pdfbox), [ParsCit](https://github.com/knmnyn/ParsCit), [LA-PdfText](https://github.com/BMKEG/lapdftext), [PdfMiner](https://github.com/euske/pdfminer/), [pdfXtk](https://github.com/tamirhassan/pdfxtk), [pdf-extract](https://github.com/CrossRef/pdfextract), [PDFExtract](https://github.com/elacin/PDFExtract), [Grobid](https://github.com/kermitt2/grobid), [Icecite](https://github.com/ckorzen/icecite).
+ provides meaningful evaluation criteria in order to assess the semantic abilities of a tool on identifying (1) *words*, (2) the *reading order*, (3) *paragraph boundaries* and (4) the *semantic roles* of text elements in PDF.

For more details, see [`evaluation/`](evaluation).
