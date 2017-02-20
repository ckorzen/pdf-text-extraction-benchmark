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



package org.elacin.pdfextract.physical.paragraph;

import org.apache.log4j.Logger;
import org.elacin.pdfextract.physical.ParagraphNumberer;
import org.elacin.pdfextract.style.Style;
import org.elacin.pdfextract.style.StyleComparator;
import org.elacin.pdfextract.tree.LineNode;
import org.elacin.pdfextract.tree.ParagraphNode;
import org.elacin.pdfextract.tree.WordNode;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static org.elacin.pdfextract.Constants.SPLIT_PARAGRAPHS_BY_STYLES;

/**
 * Created by IntelliJ IDEA. User: elacin Date: 17.11.10 Time: 04.45 To change this template use
 * File | Settings | File Templates.
 */
public class ParagraphSegmentator {

// ------------------------------ FIELDS ------------------------------
    private static final Logger log                   = Logger.getLogger(ParagraphSegmentator.class);
    private float               medianVerticalSpacing = -1.0f;

// --------------------- GETTER / SETTER METHODS ---------------------
    public void setMedianVerticalSpacing(final int medianVerticalSpacing) {
        this.medianVerticalSpacing = medianVerticalSpacing;
    }

// -------------------------- PUBLIC METHODS --------------------------
    @NotNull
    public List<ParagraphNode> segmentParagraphsByStyleAndDistance(@NotNull final List<LineNode> lines,
            final ParagraphNumberer numberer) {

        if (medianVerticalSpacing == -1.0f) {
            throw new RuntimeException("set medianVerticalSpacing!");
        }

        List<ParagraphNode> ret = new ArrayList<ParagraphNode>();

        /* separate the lines by their dominant style into paragraphs */
        if (!lines.isEmpty()) {
            numberer.newParagraph();

            ParagraphNode currentParagraph = new ParagraphNode(numberer.getParagraphId(false));

            if (SPLIT_PARAGRAPHS_BY_STYLES) {
                Style    currentStyle = null;
                LineNode lastLine     = null;

                for (LineNode line : lines) {
                    final Style lineStyle = line.findDominatingStyle();

                    if (currentStyle == null) {
                        currentStyle = lineStyle;
                        lastLine     = line;
                    }

                    final float   distance = line.getPos().y - lastLine.getPos().endY;
                    final boolean split;

                    switch (StyleComparator.styleCompare(currentStyle, lineStyle)) {
                    case SPLIT :
                        split = true;

                        break;
                    case SAME_STYLE_AND_BIG_TEXT :

                        // split = distance > medianVerticalSpacing * 2.5f;
                        split = false;

                        break;
                    case SAME_STYLE :

                        /**
                         * if the styles are similar, only split if there seems to be much space
                         * between the two lines
                         */
                        split = distance > medianVerticalSpacing * 1.5f;

                        break;
                    case SUBTLE_DIFFERENCE :

                        /* if there is a word with the same style, treat as same */
                        boolean found = false;

                        for (WordNode word : line.getChildren()) {
                            if (word.getStyle().equals(currentStyle)) {
                                found = true;
                            }
                        }

                        if (found) {
                            split = distance > medianVerticalSpacing * 1.5f;
                        } else {

                            /**
                             * if the difference is subtle, do split if there seems to be some space
                             * between the two lines
                             */
                            split = distance > medianVerticalSpacing * 1.1f;
                        }

                        break;
                    case BIG_DIFFERENCE :
                        found = false;

                        for (WordNode word : line.getChildren()) {
                            if (word.getStyle().equals(currentStyle)) {
                                found = true;
                            }
                        }

                        if (found) {
                            split = false;
                        } else {
                            split = true;
                        }

                        break;
                    default :
                        throw new RuntimeException("made compiler happy :)");
                    }

                    if (split) {
                        if (!currentParagraph.getChildren().isEmpty()) {
                            if (log.isDebugEnabled()) {
                                log.debug(
                                    String.format(
                                        "LOG00660:Split/style: y:%s, "
                                        + "medianVerticalSpacing: %f, distance: %s, style: %s, %s, line: %s", line
                                            .getPos().y, medianVerticalSpacing, distance, currentStyle,
                                                         lineStyle, line));
                            }

                            ret.add(currentParagraph);
                        }

                        numberer.newParagraph();
                        currentParagraph = new ParagraphNode(numberer.getParagraphId(false));
                        currentStyle     = lineStyle;
                    }

                    currentParagraph.addChild(line);
                    lastLine = line;
                }
            } else {
                for (LineNode line : lines) {
                    currentParagraph.addChild(line);
                }
            }

            if (!currentParagraph.getChildren().isEmpty()) {
                ret.add(currentParagraph);
            }
        }

        return ret;
    }
}
