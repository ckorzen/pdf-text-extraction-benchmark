#!/bin/bash

#enables execution from another directory
#p=/path/to/PDFAnalyser
p=.

java -cp $p/bin:$p/../pdfXtk-Extras/jar/pdfxtk-extras.jar:$p/../pdfXtk-GUI/jar/pdfxtk-gui.jar:$p/../pdfXtk/jar/pdfxtk-backend.jar:$p/../pdfXtk/lib/commons-collections-3.1.jar:$p/../pdfXtk/lib/commons-logging.jar:$p/../pdfXtk/lib/fontbox-1.1.0.jar:$p/../pdfXtk/lib/jai_codec.jar:$p/../pdfXtk/lib/jai_core.jar:$p/../pdfXtk/lib/jai_imageio.jar:$p/../pdfXtk/lib/log4j-1.2.14.jar:$p/../pdfXtk/lib/pdfbox-1.1.0.jar:$p/../TouchGraph-Modified/jar/touchgraph-modified.jar:$p/../TouchGraph-Modified/lib/TGWikiBrowser.jar:$p/../pdfXtk/lib/xercesImpl.jar:$p/../pdfXtk-Extras/lib/JavaOCR.jar at.ac.tuwien.dbai.pdfwrap.ProcessFile "$@"
