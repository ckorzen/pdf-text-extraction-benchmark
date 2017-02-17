#!/bin/bash

#enables execution from another directory
#p=/path/to/PDFAnalyser-GUI
p=.

java -cp $p/bin:$p/lib/touchgraph-modified-20110218.jar:$p/lib/pdfanalyser-backend-20110222.jar:$p/lib/fontbox-1.1.0.jar:$p/lib/pdfbox-1.1.0.jar:$p/lib/commons-logging.jar:$p/lib/log4j-1.2.14.jar:$p/lib/TGWikiBrowser.jar:$p/lib/xmillum.jar:$p/lib/xercesImpl.jar:$p/lib/jai_codec.jar:$p/lib/jai_core.jar at.ac.tuwien.dbai.pdfwrap.gui.GUI "$@"
