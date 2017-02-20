/*
 * Copyright 2010-2011 �yvind Berg (elacin@gmail.com)
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



package org.elacin.pdfextract.logical.operation;

import org.elacin.pdfextract.logical.DocumentMetadata;
import org.elacin.pdfextract.logical.Operation;
import org.elacin.pdfextract.tree.*;

/**
 * Created by IntelliJ IDEA. User: elacin Date: 30.03.11 Time: 05.13 To change this template use
 * File | Settings | File Templates.
 */
public class ExtractFootnotes implements Operation {

// ------------------------ INTERFACE METHODS ------------------------
// --------------------- Interface Operation ---------------------
    public void doOperation(final DocumentNode root, final DocumentMetadata metadata) {

        for (PageNode pageNode : root.getChildren()) {
            for (ParagraphNode prf : pageNode.getChildren()) {
                if (prf.getStyle().xSize < metadata.getBodyText().xSize) {
                    WordNode firstWord = prf.getChildren().get(0).getChildren().get(0);
                    char     ch        = firstWord.getText().charAt(0);

                    if (Character.isDigit(ch) || (ch == '*')) {
                        prf.addRole(Role.FOOTNOTE);
                    }
                }
            }
        }
    }
}
