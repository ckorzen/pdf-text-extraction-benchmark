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
import org.elacin.pdfextract.tree.LineNode;
import org.jetbrains.annotations.Nullable;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA. User: elacin Date: May 26, 2010 Time: 1:08:50 PM To change this
 * template use File | Settings | File Templates.
 */
public class TestC021004 {

// ------------------------------ FIELDS ------------------------------
    private DocumentNode        doc;
    private ArrayList<LineNode> lines;
    private BufferedReader      reader;

// -------------------------- PUBLIC METHODS --------------------------
    @BeforeClass(groups = "TestC021004")
    public void setUp() throws IOException {

        doc   = PDFDocumentLoader.readPDF("src/test/resources/C02-1004.pdf", "C02-1004_out.xml", 1);
        lines = DocumentNavigator.getLineNodes(doc);

        final URL url = PDFDocumentLoader.class.getClassLoader().getResource(
                            "src/test/resources/C021004.txt");

        reader = new BufferedReader(new FileReader(url.getFile()));
    }

    @Test()
    public void testContents() throws IOException {

        int found = 0,
            total = 0;

        while (reader.ready()) {
            final String line = reader.readLine();

            if (line.trim().length() != 0) {
                total++;

                final LineNode line1 = findLine(line);

                if (line1 == null) {
                    System.out.println("could not find line = " + line);
                } else {
                    found++;
                }
            }
        }

        System.out.println("Found " + found + " of " + total);
    }

// -------------------------- OTHER METHODS --------------------------
    @Nullable
    private LineNode findLine(final String line) {

        for (LineNode lineNode : lines) {
            if (lineNode.getText().equals(line)) {
                return lineNode;
            }
        }

        return null;
    }
}
