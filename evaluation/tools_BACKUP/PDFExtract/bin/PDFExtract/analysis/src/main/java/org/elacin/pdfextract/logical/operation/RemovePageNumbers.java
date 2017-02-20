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
import org.elacin.pdfextract.style.Style;
import org.elacin.pdfextract.style.TextUtils;
import org.elacin.pdfextract.tree.DocumentNode;
import org.elacin.pdfextract.tree.PageNode;
import org.elacin.pdfextract.tree.ParagraphNode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.elacin.pdfextract.style.TextUtils.findDominatingStyle;

/**
 * Created by IntelliJ IDEA. User: elacin Date: 24.05.11 Time: 11.26 To change this template use
 * File | Settings | File Templates.
 */
public class RemovePageNumbers implements Operation {

// ------------------------------ FIELDS ------------------------------
    private static final Logger log = Logger.getLogger(RemovePageNumbers.class);

// ------------------------ INTERFACE METHODS ------------------------
// --------------------- Interface Operation ---------------------
    public void doOperation(final DocumentNode root, final DocumentMetadata metadata) {

        /* make sure we only find max one page number per page */
        final int                 size                  = 2 * root.getChildren().size();
        List<List<ParagraphNode>> potPageNumbersForPage = new ArrayList<List<ParagraphNode>>(size);
        List<ParagraphNode>       allPotPageNumbers     = new ArrayList<ParagraphNode>(size);

        for (PageNode page : root.getChildren()) {
            final List<ParagraphNode> currentPagePotPageNum = new ArrayList<ParagraphNode>();

            for (ParagraphNode prf : page.getChildren()) {
                if (prf.hasRole()) {
                    continue;
                }

                /* look for one word paragraphs */
                if (prf.getChildren().size() != 1) {
                    continue;
                }

                /* only look for page numbers in the lower 15% of the page */
                if (prf.getPos().y < page.getPos().endY * 0.85f) {
                    continue;
                }

                if (isAllDigit(prf.getText().trim())) {
                    currentPagePotPageNum.add(prf);
                }
            }

            if (currentPagePotPageNum.isEmpty()){
                continue;
            }
            potPageNumbersForPage.add(currentPagePotPageNum);
            allPotPageNumbers.addAll(currentPagePotPageNum);
        }

        if (log.isInfoEnabled()) {
            log.info("LOG01540:potential page numbers :" + potPageNumbersForPage);
        }

        if (potPageNumbersForPage.size() < Math.max(1, root.getChildren().size() / 2)) {
            if (log.isInfoEnabled()) {
                log.info("LOG01560:Could not find page numbers");
            }

            return;
        }

        Style mostProbablePageNumStyle = findDominatingStyle(allPotPageNumbers);

        if (log.isInfoEnabled()) {
            log.info("LOG01550:mostProbablePageNumStyle" + mostProbablePageNumStyle);
        }

        for (List<ParagraphNode> pageNumCandidatesForPage : potPageNumbersForPage) {

            /* remove the page number candidates which has the wrong style */
            for (Iterator<ParagraphNode> iterator = pageNumCandidatesForPage.iterator(); iterator.hasNext(); ) {
                final ParagraphNode pageNumCandidate = iterator.next();
                if (!pageNumCandidate.getStyle().equals(mostProbablePageNumStyle)) {
                    iterator.remove();
                }
            }

            if (pageNumCandidatesForPage.isEmpty()){
                log.warn("LOG01570:No page numbers left after checking style");
                continue;
            } else if (pageNumCandidatesForPage.size() > 1){
                log.warn("LOG01570:Found several possible page numbers for a page:" + pageNumCandidatesForPage);
                continue;
            }
            ParagraphNode pageNumToRemove = pageNumCandidatesForPage.get(0);
            log.warn("LOG01580:Removing page number " + pageNumToRemove);
            pageNumToRemove.getParent().removeChild(pageNumToRemove);

        }

    }

// -------------------------- STATIC METHODS --------------------------
    private static boolean isAllDigit(final String text) {

        for (int i = 0; i < text.length(); i++) {
            if (!Character.isDigit(text.charAt(i))) {
                return false;
            }
        }

        return true;
    }
}
