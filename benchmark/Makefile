WGET = wget -N
LN = ln -s

GROUNDTRUTH_DIR_DOWNLOAD_URL = 
GROUNDTRUTH_DIR_LOCAL_PATH = /nfs/raid1/arxiv/benchmark-data/benchmark/groundtruth/

PDF_DIR_DOWNLOAD_URL = 
PDF_DIR_LOCAL_PATH = /nfs/raid1/arxiv/benchmark-data/benchmark/pdf/

SRC_DIR_DOWNLOAD_URL = 
SRC_DIR_LOCAL_PATH = /nfs/raid1/arxiv/benchmark-data/benchmark/src/

GROUNDTRUTH_DIR = ./groundtruth
PDF_DIR = ./pdf
SRC_DIR = ./src

download-data: download-data-groundtruth download-data-pdf download-data-src

download-data-groundtruth:
	$(WGET) $(GROUNDTRUTH_DIR_DOWNLOAD_URL) $(GROUNDTRUTH_DIR)

download-data-pdf:
	$(WGET) $(PDF_DIR_DOWNLOAD_URL) $(PDF_DIR)

download-data-src:
	$(WGET) $(SRC_DIR_DOWNLOAD_URL) $(SRC_DIR)


link-data: link-data-groundtruth link-data-pdf link-data-src

link-data-groundtruth:
	$(LN) $(GROUNDTRUTH_DIR_LOCAL_PATH) $(GROUNDTRUTH_DIR)

link-data-pdf:
	$(LN) $(PDF_DIR_LOCAL_PATH) $(PDF_DIR)

link-data-src:
	$(LN) $(SRC_DIR_LOCAL_PATH) $(SRC_DIR)

