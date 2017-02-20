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



package org.elacin.pdfextract.physical.line;

import org.apache.log4j.Logger;
import org.elacin.pdfextract.content.PhysicalContent;
import org.elacin.pdfextract.content.PhysicalText;
import org.elacin.pdfextract.geom.Rectangle;
import org.elacin.pdfextract.geom.RectangleCollection;
import org.elacin.pdfextract.geom.Sorting;
import org.elacin.pdfextract.tree.LineNode;
import org.elacin.pdfextract.tree.WordNode;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.elacin.pdfextract.content.AssignablePhysicalContent.BLOCK_NOT_ASSIGNED;

public class LineSegmentator {

// ------------------------------ FIELDS ------------------------------
    public static final int     LIMIT     = 1;
    public static final int     LOOKAHEAD = 2;
    private static final Logger log       = Logger.getLogger(LineSegmentator.class);

// -------------------------- PUBLIC STATIC METHODS --------------------------
    @NotNull
    public static List<LineNode> createLinesFromBlocks(RectangleCollection block) {

        /* compile paragraphs of text based on the assigned block numbers */
        int minY = Integer.MAX_VALUE,
            maxY = Integer.MIN_VALUE;

        /* reuse the assignment numbers */
        for (PhysicalContent content : block.getContents()) {
            content.getAssignable().setBlockNum(BLOCK_NOT_ASSIGNED);
            minY = Math.min((int) content.getPos().y, minY);
            maxY = Math.max((int) content.getPos().endY, maxY);
        }

        maxY++;    // account for rounding

        final int            blockHeight = maxY - minY;
        final List<LineNode> lines       = new ArrayList<LineNode>();
        int[]                counts      = new int[blockHeight];

        for (PhysicalContent content : block.getContents()) {
            if (!content.isAssignable()) {
                continue;
            }

            int contentHeight = (int) content.getPos().height;
            int contentStart  = (int) content.getPos().y;
            int contentWidth  = (int) content.getPos().width;

            for (int contentY = 0; contentY < contentHeight; contentY++) {
                counts[contentStart + contentY - minY] += contentWidth;
            }
        }

        List<Integer> lineBoundaries = findLineBoundaries(counts);

        Collections.sort(block.getContents(), Sorting.sortByLowerY);

        LineNode currentLine = new LineNode();

        for (int i = 0; i < lineBoundaries.size() - 1; i++) {
            int start = minY + lineBoundaries.get(i) - 1;
            int stop  = minY + lineBoundaries.get(i + 1);

            for (PhysicalContent content : block.getContents()) {
                final Rectangle contentPos = content.getPos();

                if (content.getAssignable().isAssignedBlock()) {
                    continue;
                }

                if ((contentPos.y > start - 1) && (contentPos.endY < stop + 1)) {
                    content.getAssignable().setBlockNum(1);

                    if (content.isText()) {
                        currentLine.addChild(createWordNode(content.getPhysicalText()));
                    } else {
                        currentLine.addChild(createWordNodeFromGraphic(content));
                    }
                }
            }

            if (!currentLine.getChildren().isEmpty()) {
                lines.add(currentLine);
                currentLine = new LineNode();
            }
        }

        combineLines(lines);

        return lines;
    }

    @NotNull
    public static WordNode createWordNode(@NotNull final PhysicalText text) {
        return new WordNode(text.getPos(), text.getStyle(), text.text, text.charSpacing);
    }

    public static WordNode createWordNodeFromGraphic(final PhysicalContent content) {

        return new WordNode(content.getPos(), content.getGraphicContent().getStyle(),
                            content.getGraphicContent().getStyle().id, -1);
    }

// -------------------------- STATIC METHODS --------------------------

    /**
     * Some times very small punctuation like full stops and commas seems to be left on a line on
     * their own. work around that here
     *
     * @param lines
     */
    private static void combineLines(final List<LineNode> lines) {

        for (int i = 1; i < lines.size(); i++) {
            final LineNode lastLine    = lines.get(i - 1);
            final LineNode currentLine = lines.get(i);

            if (currentLine.getChildren().size() >= 4) {
                continue;
            }

            if (!currentLine.getStyle().equals(lastLine.getStyle())) {
                continue;
            }

            if (currentLine.getPos().height > 0.7f * lastLine.getPos().height) {
                continue;
            }

            if (lastLine.getPos().getVerticalDistanceTo(currentLine.getPos()) > 2) {
                continue;
            }

            if (log.isDebugEnabled()) {
                log.debug("LOG01360:Combining line " + currentLine);
            }

            lastLine.addChildren(currentLine.getChildren());
            lines.remove(currentLine);
            i--;
        }
    }

    @NotNull
    private static List<Integer> findLineBoundaries(@NotNull int[] counts) {

        List<Integer> lineBoundaries = new ArrayList<Integer>();

        lineBoundaries.add(0);

        boolean hasFoundText = false;

        for (int i = 0; i < counts.length; i++) {
            if (hasFoundText && (counts[i] < LOOKAHEAD)) {
                boolean isBoundary = true;

                for (int j = i + 1; (j < i + LOOKAHEAD) && (j < counts.length); j++) {
                    if (counts[j] <= LIMIT) {
                        isBoundary = false;

                        break;
                    }
                }

                if (isBoundary) {
                    lineBoundaries.add(i + 1);
                    hasFoundText = false;
                }
            } else if (counts[i] > LIMIT) {
                hasFoundText = true;
            }
        }

        /* add the end as well */
        lineBoundaries.add(counts.length);

        return lineBoundaries;
    }
}
