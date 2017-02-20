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



package org.elacin.pdfextract.logical.operation;

import org.apache.log4j.Logger;

import org.elacin.pdfextract.logical.DocumentMetadata;
import org.elacin.pdfextract.logical.Operation;
import org.elacin.pdfextract.style.Style;
import org.elacin.pdfextract.tree.*;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Created by IntelliJ IDEA. User: elacin Date: 31.01.11 Time: 10.46 To change this template use
 * File | Settings | File Templates.
 */
public class RecognizeDivs implements Operation {

// ------------------------------ FIELDS ------------------------------
    private static final Logger log = Logger.getLogger(RecognizeDivs.class);

// ------------------------ INTERFACE METHODS ------------------------
// --------------------- Interface Operation ---------------------
    public void doOperation(@NotNull final DocumentNode root, @NotNull final DocumentMetadata metadata) {

        List<Style> headerCandidates = metadata.getCandidateHeaderStyles();
        Style       div1             = null,
                    div2             = null,
                    div3             = null;
        int         divFound         = 0;

        /* identify styles for three levels of divs */
        for (PageNode p : root.getChildren()) {
            for (ParagraphNode prf : p.getChildren()) {
                Style currentStyle = prf.getStyle();

                if (div3 != null) {
                    continue;
                }

                if (prf.hasRole()) {
                    continue;
                }

                if (!Character.isDigit(prf.getText().charAt(0))) {
                    continue;
                }

                if (headerCandidates.contains(currentStyle)) {
                    switch (divFound) {
                    case 0 :
                        div1 = currentStyle;

                        break;
                    case 1 :
                        div2 = currentStyle;

                        break;
                    case 2 :
                        div3 = currentStyle;

                        break;
                    default :
                        assert false;
                    }

                    headerCandidates.remove(currentStyle);
                    divFound++;
                }
            }
        }

        /* tag matching headline paragraphs with the corresponding role */
        for (PageNode p : root.getChildren()) {
            for (ParagraphNode prf : p.getChildren()) {
                Style currentStyle = prf.getStyle();
                Role  r            = null;

                if (!Character.isDigit(prf.getText().charAt(0))) {
                    continue;
                }

                if (currentStyle.equals(div1)) {
                    r = Role.DIV1;
                } else if (currentStyle.equals(div2)) {
                    r = Role.DIV2;
                } else if (currentStyle.equals(div3)) {
                    r = Role.DIV3;
                }

                if (r != null) {
                    prf.addRole(r);
                }
            }
        }
    }
}
