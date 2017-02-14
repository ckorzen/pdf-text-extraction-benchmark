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


package org.elacin.pdfextract.physical;

import org.apache.log4j.Logger;
import org.apache.log4j.MDC;
import org.elacin.pdfextract.Constants;
import org.elacin.pdfextract.content.PhysicalPage;
import org.elacin.pdfextract.content.PhysicalText;
import org.elacin.pdfextract.datasource.DocumentContent;
import org.elacin.pdfextract.datasource.PageContent;
import org.elacin.pdfextract.physical.word.WordSegmentator;
import org.elacin.pdfextract.physical.word.WordSegmentatorImpl;
import org.elacin.pdfextract.tree.DocumentNode;
import org.elacin.pdfextract.tree.PageNode;

import java.util.List;

/**
 * Created by IntelliJ IDEA. User: elacin Date: 29.01.11 Time: 20.09 To change this template use
 * File | Settings | File Templates.
 */
public class GeometricAnalysis {

    private static final Logger         log             = Logger.getLogger(GeometricAnalysis.class);
    public static final WordSegmentator wordSegmentator = new WordSegmentatorImpl();

    public static DocumentNode analyzeDocument(final DocumentContent content) {

        DocumentNode root = new DocumentNode();
        final long   t0   = System.currentTimeMillis();

        root.getStyles().addAll(content.getStyles());

        for (final PageContent inputPage : content.getPages()) {
            MDC.put("page", inputPage.getPageNum());

            if (inputPage.getCharacters().isEmpty()) {
                log.error("LOG01150:Page " + inputPage.getPageNum() + " is empty");

                continue;
            }

            final List<PhysicalText> words = wordSegmentator.segmentWords(inputPage.getCharacters());

            /* create a physical page instance */
            PhysicalPage pp = new PhysicalPage(words, inputPage.getGraphics(), inputPage.getPageNum(),
                                               inputPage.getDimensions());

            /* divide the page in smaller sections */
            final PageNode pageNode = PageSegmentator.analyzePage(pp);

            if (Constants.RENDER_ENABLED) {
                pageNode.setPhysicalPage(pp);
            }

            root.addChild(pageNode);
        }

        MDC.remove("page");

        final long td = System.currentTimeMillis() - t0;

        log.info("Analyzed " + content.getPages().size() + " pages in " + td + "ms");

        return root;
    }
}
