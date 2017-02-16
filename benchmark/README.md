# The Benchmark

The benchmarks provides TeX source files, PDF files and ground truth files for in total 12,099 scientific articles of [*arXiv.org*](https://arxiv.org/). The files are given in folders [`src`](src), [`pdf`](pdf) and [`groundtruth`](groundtruth).

## Basic structure

In the dataset of *arXiv*, scientific articles are organized in folders `YYMM`, where `YY` is a two-place digit representing the year and `MM` is a two-place digit representing the month of publication. For example, folder `1507` contains all articles from July 2015.

The folders [`src`](src), [`pdf`](pdf) and [`groundtruth`](groundtruth) reflect this structure. <br />
For illustration, consider the article *astro-ph0001196* from January 2001. 
The associated files/folders are:

+ [`pdf/0001/astro-ph0001196.pdf`](pdf/0001/astro-ph0001196.pdf), the PDF file. 
+ [`src/0001/astro-ph0001196`](src/0001/astro-ph0001196), the folder with TeX source file and supplementary files like images, etc.
+ [`groundtruth/0001/astro-ph0001196.body.txt`](groundtruth/0001/astro-ph0001196.body.txt), the ground truth file.

In total, the benchmark provides the TeX files, PDF files and ground truth files for 12,099 scientific articles of *arXiv*

TODO: How to provide the TeX source files and PDF files? arXiv does not grant the right to distribute arXiv articles.

## The selection of scientific articles

From each `YYMM` folder in the *arXiv* dataset, 1% of the articles 

