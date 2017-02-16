# The Benchmark

The benchmarks provides TeX source files, PDF files and ground truth files for in total *12,099* scientific articles of [*arXiv.org*](https://arxiv.org/). The files are given in folders [`src`](src), [`pdf`](pdf) and [`groundtruth`](groundtruth).

## Basic structure

In the dataset of *arXiv*, scientific articles are organized in folders `YYMM`, where `YY` is a two-place digit representing the year and `MM` is a two-place digit representing the month of publication. For example, folder `1507` contains all articles from July 2015.
The folders [`src`](src), [`pdf`](pdf) and [`groundtruth`](groundtruth) reflect this structure.

For illustration, consider the article *astro-ph0001196* from January 2001. 
The associated files/folders are:

+ [`pdf/0001/astro-ph0001196.pdf`](pdf/0001/astro-ph0001196.pdf), the PDF file. 
+ [`src/0001/astro-ph0001196`](src/0001/astro-ph0001196), the folder with TeX source file and supplementary files like images, etc.
+ [`groundtruth/0001/astro-ph0001196.body.txt`](groundtruth/0001/astro-ph0001196.body.txt), the ground truth file.

All of the *12,099* scientific articles were selected randomly, 1% from each `YYMM` folder, in order to represent the variety of topics and creation times (and thus article formats), which can be deduced from the [arXiv submission rate statistics](https://arxiv.org/help/stats/2016_by_area/index/).

**TODO**: How to provide the TeX source files and PDF files? arXiv does not grant the right to distribute arXiv articles.

### Ground Truth Files

All ground truth files were generated using the [`benchmark-generator`](../benchmark-generator).
Each is a *plain text file* and contains the *title*, the *section headings* and the *body text paragraphs* of the associated scientific article, separated by blank lines and in order as they appear in the TeX file.
Each ground truth files has file extension `.body.txt`.

### PDF files

Please note that the provided PDF files are not those provided by *arXiv*, due to occasional (contentual) mismatches with the corresponding TeX files, but are regenerated from the provided TeX files.
