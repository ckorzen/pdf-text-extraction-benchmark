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
import org.elacin.pdfextract.tree.DocumentNode;
import org.elacin.pdfextract.tree.PageNode;
import org.elacin.pdfextract.tree.ParagraphNode;

import java.util.List;

/**
 * Created by IntelliJ IDEA. User: elacin Date: 31.05.11 Time: 06.25 To change this template use
 * File | Settings | File Templates.
 */
public class ExtractTitle implements Operation {

// ------------------------------ FIELDS ------------------------------
    private static final Logger log = Logger.getLogger(ExtractTitle.class);

// ------------------------ INTERFACE METHODS ------------------------
// --------------------- Interface Operation ---------------------
    public void doOperation(final DocumentNode root, final DocumentMetadata metadata) {

        final List<Style> headerCandidates = metadata.getCandidateHeaderStyles();

        /* extract title */
        PageNode            firstPage           = root.getChildren().get(0);
        List<ParagraphNode> firstPagePrfs = firstPage.getChildren();

        for (int i = 0; i < firstPagePrfs.size(); i++) {
            final ParagraphNode titleParagraph = firstPagePrfs.get(i);

            if (headerCandidates.contains(titleParagraph.getStyle())) {

                /* check if the next text logically belongs with this */
                if (i + 1 != firstPagePrfs.size() - 1) {
                    ParagraphNode peekNext = firstPagePrfs.get(i + 1);

                    if (peekNext.getStyle().equals(titleParagraph.getStyle())) {
                        firstPage.removeChild(peekNext);
                        titleParagraph.addChildren(peekNext.getChildren());
                    }
                }

                root.setTitle(titleParagraph);
                firstPage.removeChild(titleParagraph);
//                headerCandidates.remove(titleParagraph.getStyle()); //TODO:does this make sense?
                log.warn("LOG01430:Title is " + root.getTitle());

                break;
            }
        }
    }
}
