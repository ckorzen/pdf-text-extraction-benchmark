#!/bin/bash

#enables execution from another directory
#p=/path/to/PDFAnalyser
p=.

java -cp $p/bin:$p/lib/touchgraph-modified-20110218.jar:$p/lib/commons-logging.jar:$p/lib/jai_codec.jar:$p/lib/jai_core.jar:$p/lib/log4j-1.2.14.jar:$p/lib/pdfbox-1.1.0.jar:$p/lib/TGWikiBrowser.jar:$p/lib/xercesImpl.jar:$p/lib/commons-collections-3.1.jar:$p/lib/fontbox-1.1.0.jar at.ac.tuwien.dbai.pdfwrap.GraphMatcher "$@"