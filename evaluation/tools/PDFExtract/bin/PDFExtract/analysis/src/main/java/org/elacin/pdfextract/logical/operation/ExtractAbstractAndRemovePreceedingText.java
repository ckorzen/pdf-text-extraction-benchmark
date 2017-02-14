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

import org.apache.log4j.Logger;

import org.elacin.pdfextract.logical.DocumentMetadata;
import org.elacin.pdfextract.logical.Operation;
import org.elacin.pdfextract.style.StyleDifference;
import org.elacin.pdfextract.tree.DocumentNode;
import org.elacin.pdfextract.tree.PageNode;
import org.elacin.pdfextract.tree.ParagraphNode;

import java.util.List;

import static org.elacin.pdfextract.style.StyleComparator.styleCompare;

/**
 * Recognizes a heading with the name abstract, and the subsequent paragraphs of text until the next
 *  header-like element.
 *
 *  Also removes all content before this abstract, so it is essential that ExtractTitle be ran
 *  before this.
 *
 */
public class ExtractAbstractAndRemovePreceedingText implements Operation {

// ------------------------------ FIELDS ------------------------------
    private static final Logger log = Logger.getLogger(ExtractAbstractAndRemovePreceedingText.class);

// ------------------------ INTERFACE METHODS ------------------------
// --------------------- Interface Operation ---------------------
    public void doOperation(final DocumentNode root, final DocumentMetadata metadata) {

        if (root.getWords().isEmpty() || root.getChildren().isEmpty()) {
            throw new RuntimeException("tried to analyze empty document");
        }

        PageNode            firstPage = root.getChildren().get(0);
        List<ParagraphNode> prfs      = firstPage.getChildren();

        for (int i = 0; i < prfs.size(); i++) {
            final ParagraphNode absTitlePrf = prfs.get(i);

            if (absTitlePrf.getText().trim().toLowerCase().equals("abstract")
                    && (i + 1 != prfs.size())) {
                ParagraphNode abstractPrf = prfs.get(++i);

                i++;

                while (true) {
                    if (i == prfs.size()) {
                        break;
                    }

                    ParagraphNode   next = prfs.get(i);
                    StyleDifference diff = styleCompare(next.getStyle(), abstractPrf.getStyle());

                    if (diff != StyleDifference.SAME_STYLE) {
                        break;
                    }

                    abstractPrf.addChildren(next.getChildren());
                    prfs.remove(i);
                }

                /* set the newly created paragraph as the special abstract paragraph in the tree,
                 and remove it from the original position*  */
                root.setAbstractParagraph(abstractPrf);
                prfs.remove(abstractPrf);
                prfs.remove(absTitlePrf);

                /* then remove all preceeding content */
                for (int j = 0; j < i -2; j++){
                    prfs.remove(0);
                }


                if (log.isInfoEnabled()) {
                    String t    = abstractPrf.getText();
                    String text = t.substring(0, Math.min(30, t.length()));

                    log.info("LOG01460:Found abstract with text " + text);
                }
            }
        }
    }
}
