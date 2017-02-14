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

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.testng.Assert.assertEquals;

/**
 * Created by IntelliJ IDEA. User: elacin Date: Apr 8, 2010 Time: 6:39:51 AM To change this template
 * use File | Settings | File Templates.
 */
public class TestDocument07050001 extends TestLatexComparison {

// --------------------------- CONSTRUCTORS ---------------------------
    public TestDocument07050001() {
        super("0705.0001");
    }

// -------------------------- PUBLIC METHODS --------------------------
    @Test(enabled = false)
    public void TestMainTitle() {

        // final Collection<AbstractNode> maintitles = pdfDOM.getNodesWithRole(Role.MAINTITLE);
        boolean found = false;

        // for (AbstractNode mainTitle : maintitles) {
        // if (mainTitle.getText().equals("XMM-Newton observations of the first unidentified TeV gamma-ray source TeV J2032+4130?")) {
        // found = true;
        // break;
        // }
        // }
        assertEquals(found, true, "Could not find the correct title in " + PDFFILENAME + "!");
    }

    @BeforeClass(groups = "TestDocument07050001")
    public void setUp() throws IOException {
        readFiles();
    }

// -------------------------- OTHER METHODS --------------------------
    @Test(enabled = false)
    void testTopText() {

        // final Collection<AbstractNode> sections = pdfDOM.getNodesWithRole(Role.SECTION);
        boolean found = false;

        // for (AbstractNode mainTitle : sections) {
        // if (mainTitle.getText().equals("XMM-Newton observations of the first unidentified TeV gamma-ray source TeV J2032+4130?")) {
        // found = true;
        // break;
        // }
        // }
        assertEquals(found, true, "Could not find the correct title in " + PDFFILENAME + "!");
    }
}
