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
import org.jetbrains.annotations.NotNull;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

/**
 * Created by IntelliJ IDEA. User: elacin Date: May 11, 2010 Time: 7:23:37 PM To change this
 * template use File | Settings | File Templates.
 */
public class TestSpacing {

// ------------------------------ FIELDS ------------------------------
    private DocumentNode        doc;
    private ArrayList<LineNode> lines;

// -------------------------- PUBLIC METHODS --------------------------
    @BeforeClass(groups = "TestSpacing")
    public void setUp() throws IOException {

        doc   = PDFDocumentLoader.readPDF("src/test/resources/renderX/spacing.pdf", "spacing_out.xml",
                                          4);
        lines = DocumentNavigator.getLineNodes(doc);
    }

    @Test()
    public void testHeader() {

        check("Text Attributes - Character and Word Spacing");

        // check("Page 1");
        // check("Text Attributes - Character and Word Spacing");
        check("This test contains examples of character and word spacing.");
        check("Character spacing:");
        check("Word spacing:");

//  check("? RenderX 2000");
//  check("XSL Formatting Objects Test Suite");
//  check("Text Attributes - Character and Word Spacing");
//  check("Page 2");
    }

    @Test()
    public void testMinusOneCharInterval() {

        check("This text has inter-character intervals increased by 1pt This text has inter-character");
        check("intervals increased by 1pt This text has inter-character intervals increased by 1pt");
        check("This text has inter-character intervals increased by 1pt This text has inter-character");
        check("intervals increased by 1pt This text has inter-character intervals increased by 1pt");
    }

    @Test()
    public void testMinusTwoWordInterval() {

        check("In this text, spaces between words are reduced by -2pt In this text, spaces between words are reduced by");
        check("-2pt In this text, spaces between words are reduced by -2pt In this text, spaces between words are reduced");
        check("by -2pt In this text, spaces between words are reduced by -2pt");
    }

    @Test()
    public void testMinuxOneCharInterval() {

        check("This text has inter-character intervals reduced by -1pt This text has inter-character intervals reduced by -1pt This text has");
        check("inter-character intervals reduced by -1pt This text has inter-character intervals reduced by -1pt This text has inter-character");
        check("intervals reduced by -1pt This text has inter-character intervals reduced by -1pt");
    }

    @Test()
    public void testNormalCharInterval() {

        check("This text has inter-character intervals increased by 0pt (i.e. normally spaced). This text has");
        check("inter-character intervals increased by 0pt (i.e. normally spaced). This text has inter-character");
        check("intervals increased by 0pt (i.e. normally spaced). This text has inter-character intervals increased");
        check("by 0pt (i.e. normally spaced). This text has inter-character intervals increased by 0pt (i.e. normally");
        check("spaced). This text has inter-character intervals increased by 0pt (i.e. normally spaced).");
    }

    @Test()
    public void testNormalWordInterval() {

        check("In this text, spaces between words are increased by 0pt (i.e. normally spaced). In this text, spaces");
        check("between words are increased by 0pt (i.e. normally spaced). In this text, spaces between words are");
        check("increased by 0pt (i.e. normally spaced). In this text, spaces between words are increased by 0pt");
        check("(i.e. normally spaced). In this text, spaces between words are increased by 0pt (i.e. normally");
        check("spaced).");
    }

    @Test()
    public void testNormalWordSpace() {

        check("In this text, spaces between words are normal. In this text, spaces between words are normal. In");
        check("this text, spaces between words are normal. In this text, spaces between words are normal. In this");
        check("text, spaces between words are normal. In this text, spaces between words are normal. In this text,");
        check("spaces between words are normal. In this text, spaces between words are normal. In this text,");
        check("spaces between words are normal. In this text, spaces between words are normal.");
    }

    @Test()
    public void testNormallySpaced() {

        check("This text is normally spaced. This text is normally spaced. This text is normally spaced. This text");
        check("is normally spaced. This text is normally spaced. This text is normally spaced. This text is normally");
        check("spaced. This text is normally spaced. This text is normally spaced. This text is normally spaced.");
        check("This text is normally spaced. This text is normally spaced.");
    }

    @Test()
    public void testPlusFourCharInterval() {

        check("This text has inter-character intervals increased by");
        check("4pt This text has inter-character intervals increased");
        check("by 4pt This text has inter-character intervals increased");
        check("by 4pt This text has inter-character intervals increased");
        check("by 4pt This text has inter-character intervals increased");
        check("by 4pt This text has inter-character intervals increased");
        check("by 4pt");
    }

    @Test()
    public void testPlusSixWordInterval() {

        check("In this text, spaces between words are increased by 6pt In this text, spaces between");
        check("words are increased by 6pt In this text, spaces between words are increased by");
        check("6pt In this text, spaces between words are increased by 6pt In this text, spaces");
        check("between words are increased by 6pt");
    }

    @Test()
    public void testPlusTwoCharInterval() {

        check("This text has inter-character intervals increased by 2pt This text has");
        check("inter-character intervals increased by 2pt This text has inter-character");
        check("intervals increased by 2pt This text has inter-character intervals");
        check("increased by 2pt This text has inter-character intervals increased by");
        check("2pt This text has inter-character intervals increased by 2pt");
    }

    @Test()
    public void testPlusTwoWordInterval() {

        check("are increased by 2pt In this text, spaces between words are increased by 2pt In this text,");
        check("spaces between words are increased by 2pt In this text, spaces between words are increased");
        check("by 2pt");
    }

// -------------------------- OTHER METHODS --------------------------
    private void check(@NotNull final String s) {

        LineNode lineNode = null;

        for (LineNode line : lines) {
            if (line.getText().equals(s)) {
                lineNode = line;
            }
        }

        if (lineNode == null) {
            fail("Could not find line " + s);
        }

        assertEquals(lineNode.getText(), s, lineNode.toString());

//  assertEquals(lineNode.getChildren().size(), s.split(" ").length,
//               lineNode + " has wrong number of words." + lineNode.toString());
    }
}
