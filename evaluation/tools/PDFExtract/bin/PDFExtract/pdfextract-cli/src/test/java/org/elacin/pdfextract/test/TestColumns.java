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

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA. User: elacin Date: May 12, 2010 Time: 4:35:20 AM To change this
 * template use File | Settings | File Templates.
 */
public class TestColumns {

// ------------------------------ FIELDS ------------------------------
    private DocumentNode        doc;
    private ArrayList<LineNode> lines;

//@BeforeClass(groups = "TestColumns")
//public void setUp() throws IOException {
//  doc = PDFDocumentLoader.readPDF("renderX/columns.pdf", "columns_out.xml", 4);
//}
//
//@Test(enabled = false)
//public void testWronglyCombinedLines() {
//
//  final List<ParagraphNode> paragraphsOnFirstPage = doc.getChildren().get(0).getChildren();
//
//  for (ParagraphNode child : paragraphsOnFirstPage) {
//      if (child.getPos().getY() == 221.60901f) {
//          assertEquals(child.getChildren().get(0).getText(), "This is the first page of the document. Its first capital letter T has red color and is 3 picas");
//          assertEquals(child.getChildren().get(1).getText(), "high.");
//          assertEquals(child.getChildren().size(), 2);
//          return;
//      }
//  }
//  fail("Could not fine paragraph");
//
//}
}
