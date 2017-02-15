# Benchmarking and Evaluating Current PDF Extraction Tools #

This project is about benchmarking and evaluating existing PDF extraction tools on their semantic abilities to extract the *body texts* from PDF documents, especially from *scientific articles*. It provides (1) a benchmark generator, (2) a ready-to-use benchmark and (3) an extensive evaluation, with meaningful evaluation criteria.

## The Benchmark Generator
The main features of the benchmark generator are:

+ Construction of high-quality benchmarks from TeX source files.
+ (Recursive) expansion of TeX macros.
+ Identification of the following 16 *semantic units*: 
    <span style="color:#555">title, author, affiliation, date, abstract, heading, paragraph of the body text, formula, figure, table, caption, listing-item, footnote, acknowledgements, references, appendix</span>
+ Serialization of desired semantic units, either in *plain text*, *XML* or *JSON* format.
+ Code is open source, see [`benchmark-generator/`](https://github.com/ckorzen/arxiv-benchmark/tree/master/benchmark-generator).


## The Benchmark
The provided benchmark

+ consists of *12,099 ground truth files* and *12,099 PDF files* of scientific articles, randomly selected from [*arXiv.org*](https://arxiv.org/).
Each ground truth file contains the *title*, the *headings* and the *body text paragraphs* of a particular scientific article.
+ was generated using the benchmark generated above.
+ is available under [`benchmark/`](https://github.com/ckorzen/arxiv-benchmark/tree/master/benchmark).

## The Evaluation
The evaluation

+ consists of *12,099 ground truth files* and *12,099 PDF files* of scientific articles, randomly selected from [*arXiv.org*](https://arxiv.org/).
Each ground truth file contains the *title*, the *headings* and the *body text paragraphs* of a particular scientific article.
+ was generated using the benchmark generated above.
+ [`evaluation/`](https://github.com/ckorzen/arxiv-benchmark/tree/master/evaluation).