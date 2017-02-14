/*
 * Copyright 2010-2011 Øyvind Berg (elacin@gmail.com)
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */


package org.elacin.pdfextract.test;

import org.elacin.pdfextract.tree.DocumentNode;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;

/**
 * Created by IntelliJ IDEA. User: elacin Date: May 9, 2010 Time: 7:25:21 PM To change this template
 * use File | Settings | File Templates.
 */
public abstract class TestLatexComparison {

    @NotNull
    protected final String ELCXMLFILENAME;
    protected String       latexDOMString;
    @NotNull
    protected final String LATEXFILENAME;
    protected DocumentNode pdfDOM;
    @NotNull
    protected final String PDFFILENAME;

// ------------------------------ FIELDS ------------------------------
    protected final String TESTNAME;
    @NotNull
    protected final String XMLFILENAME;

// --------------------------- CONSTRUCTORS ---------------------------
    public TestLatexComparison(String inputname) {

        TESTNAME       = inputname;
        PDFFILENAME    = TESTNAME + ".pdf";
        XMLFILENAME    = TESTNAME + ".xml";
        ELCXMLFILENAME = TESTNAME + ".elc.xml";
        LATEXFILENAME  = TESTNAME + ".tex";
    }

// -------------------------- OTHER METHODS --------------------------
    protected void printLatexDOMToFile() throws FileNotFoundException {

        PrintStream out = new PrintStream(new File(XMLFILENAME));

        out.print(latexDOMString);
        out.close();
    }

    protected void readFiles() throws IOException {

        pdfDOM         = PDFDocumentLoader.readPDF(PDFFILENAME, ELCXMLFILENAME, 4);
        latexDOMString = LatexDocumentLoader.readLatex(LATEXFILENAME);
        printLatexDOMToFile();
    }
}
