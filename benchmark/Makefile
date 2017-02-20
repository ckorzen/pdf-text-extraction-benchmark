WGET = wget --mirror -q --show-progress -np -nH --cut-dirs 3 --reject "index.html*"
LN = ln -s

GROUNDTRUTH_DIR_DOWNLOAD_URL = http://arxiv-benchmark.informatik.uni-freiburg.de/data/benchmark/groundtruth/
GROUNDTRUTH_DIR_LOCAL_PATH = /nfs/raid1/arxiv/benchmark-data/benchmark/groundtruth/

PDF_DIR_DOWNLOAD_URL = http://arxiv-benchmark.informatik.uni-freiburg.de/data/benchmark/pdf/
PDF_DIR_LOCAL_PATH = /nfs/raid1/arxiv/benchmark-data/benchmark/pdf/

SRC_DIR_DOWNLOAD_URL = http://arxiv-benchmark.informatik.uni-freiburg.de/data/benchmark/src/
SRC_DIR_LOCAL_PATH = /nfs/raid1/arxiv/benchmark-data/benchmark/src/

GROUNDTRUTH_DIR = ./groundtruth
PDF_DIR = ./pdf
SRC_DIR = ./src

download-data: download-data-groundtruth download-data-pdf download-data-src

download-data-groundtruth:
	$(WGET) $(GROUNDTRUTH_DIR_DOWNLOAD_URL) -P $(GROUNDTRUTH_DIR)

download-data-pdf:
	$(WGET) $(PDF_DIR_DOWNLOAD_URL) -P $(PDF_DIR)

download-data-src:
	$(WGET) $(SRC_DIR_DOWNLOAD_URL) -P $(SRC_DIR)


link-data: link-data-groundtruth link-data-pdf link-data-src

link-data-groundtruth:
	rm -rf $(GROUNDTRUTH_DIR)
	$(LN) $(GROUNDTRUTH_DIR_LOCAL_PATH) $(GROUNDTRUTH_DIR)

link-data-pdf:
	rm -rf $(PDF_DIR)
	$(LN) $(PDF_DIR_LOCAL_PATH) $(PDF_DIR)

link-data-src:
	rm -rf $(SRC_DIR)
	$(LN) $(SRC_DIR_LOCAL_PATH) $(SRC_DIR)

