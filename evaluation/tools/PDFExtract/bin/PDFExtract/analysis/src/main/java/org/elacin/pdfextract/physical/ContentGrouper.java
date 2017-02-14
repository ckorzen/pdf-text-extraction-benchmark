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
import org.elacin.pdfextract.content.PhysicalContent;
import org.elacin.pdfextract.content.PhysicalPageRegion;
import org.elacin.pdfextract.formula.Formulas;
import org.elacin.pdfextract.geom.Rectangle;
import org.elacin.pdfextract.geom.RectangleCollection;
import org.elacin.pdfextract.style.TextUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by IntelliJ IDEA. User: elacin Date: 18.01.11 Time: 22.21 To change this template use
 * File | Settings | File Templates.
 */
public class ContentGrouper {

// ------------------------------ FIELDS ------------------------------
    private static final Logger     log       = Logger.getLogger(ContentGrouper.class);
    @NotNull
    final List<RectangleCollection> allBlocks = new ArrayList<RectangleCollection>(30);
    @NotNull
    RectangleCollection currentBlock          = new RectangleCollection(
                                                    new ArrayList<PhysicalContent>(), null);
    @NotNull
    final PhysicalPageRegion        region;
    @NotNull
    public final Rectangle          rpos;

// --------------------------- CONSTRUCTORS ---------------------------
    public ContentGrouper(@NotNull PhysicalPageRegion region) {

        this.region = region;
        rpos        = region.getPos();
    }

// -------------------------- PUBLIC METHODS --------------------------
    public List<RectangleCollection> findBlocksOfContent() {

        /** if this is contained in a grapic, just output the lines */
        if (region.isGraphicalRegion()) {
            for (PhysicalContent content : region.getContents()) {
                if (content.isGraphic() || content.isText()) {
                    currentBlock.addContent(content);
                    content.getAssignable().setBlockNum(allBlocks.size());
                }
            }

            allBlocks.add(currentBlock);

            return allBlocks;
        }

        /** do a preliminary formula block combining */
        createBlocksForFormulas();

        /**
         *  If not, use the whitespace added to the region to determine blocks of text
         */

        /* follow the trails left between the whitespace and construct blocks of text from that */
        for (float y = rpos.y; y < rpos.endY; y++) {
            final List<PhysicalContent> row = region.findContentAtYIndex(y);

            /* iterate through the line to find possible start of blocks */
            for (PhysicalContent contentInRow : row) {
                if (contentInRow.isAssignable() &&!contentInRow.getAssignable().isAssignedBlock()) {

                    /* find all connected texts from this */
                    markEverythingConnectedFrom(contentInRow);
                    allBlocks.add(currentBlock);
                    currentBlock = new RectangleCollection(new ArrayList<PhysicalContent>(), null);
                }
            }
        }

        if (!currentBlock.getContents().isEmpty()) {
            allBlocks.add(currentBlock);
        }

        return allBlocks;
    }

// -------------------------- OTHER METHODS --------------------------
    @SuppressWarnings({ "NumericCastThatLosesPrecision" })
    private boolean markEverythingConnectedFrom(@NotNull final PhysicalContent content) {

        if (!content.isAssignable()) {
            return false;
        }

        if (content.getAssignable().isAssignedBlock()) {
            return false;
        }

        if (content.isGraphic() && content.getGraphicContent().isSeparator()) {

            // content.getAssignable().setBlockNum(allBlocks.size());
            return false;
        }

        content.getAssignable().setBlockNum(allBlocks.size());
        currentBlock.addContent(content);

        if (content.isGraphic()) {
            return false;
        }

        /* try searching for texts in all directions */
        int startY = (int) Math.max(rpos.y, content.getPos().y);
        int endY   = (int) Math.min(rpos.endY, content.getPos().endY);

        for (int y = startY + 1; y < endY; y++) {
            markBothWaysFromCurrent(content, region.findContentAtYIndex(y));
        }

        int startX = 1 + (int) Math.max(rpos.x, content.getPos().x);
        int endX   = -1 + (int) Math.min(rpos.endX, content.getPos().endX);

        for (int x = startX; x < endX - 1; x++) {
            markBothWaysFromCurrent(content, region.findContentAtXIndex(x));
        }

        return true;
    }

    private void createBlocksForFormulas() {

        Set<PhysicalContent> workingSet = new HashSet<PhysicalContent>();
        boolean              skip       = false,
                             hasSkipped = false;
        float                minX       = Float.MAX_VALUE;
        float                endY       = Float.MIN_VALUE;

        for (float y = rpos.y; y < rpos.endY; y++) {
            final List<PhysicalContent> row = region.findContentAtYIndex(y);

            if (!TextUtils.listContainsStyledText(row)) {
                workingSet.clear();
                skip = false;
                minX = Float.MAX_VALUE;

                continue;
            }

            if (skip) {
                continue;
            }

            for (PhysicalContent content : row) {
                if (content.isAssignable() &&!workingSet.contains(content)) {
                    minX = Math.min(content.getPos().x, minX);
                    endY = Math.max(content.getPos().endY, endY);
                    workingSet.add(content);
                }
            }

            /* only detect indented formulas */
            if (minX < region.getPos().x + 20) {
                skip       = true;
                hasSkipped = true;

                continue;
            }

            /*
             *  if we found a formula, do hungry block combining of all continous content until
             *   we find a line which is not
             */
            if (Formulas.textSeemsToBeFormula(workingSet)) {
                while (y <= endY + 1) {
                    for (PhysicalContent content : row) {
                        if (content.isAssignable() &&!workingSet.contains(content)) {
                            workingSet.add(content);
                            endY = Math.max(content.getPos().endY, endY);
                        }
                    }

                    y++;
                    row.clear();
                    row.addAll(region.findContentAtYIndex(y));
                }

                for (PhysicalContent content : workingSet) {
                    if (!content.getAssignable().isAssignedBlock()) {
                        content.getAssignable().setBlockNum(allBlocks.size());
                        currentBlock.addContent(content);
                    }
                }

                /* if there was no non-formula text inbetween, combine this with the last block */
                if (!hasSkipped &&!allBlocks.isEmpty()) {
                    allBlocks.get(allBlocks.size() - 1).addContents(currentBlock.getContents());
                } else {
                    allBlocks.add(currentBlock);
                }

                printLastBlock();
                currentBlock = new RectangleCollection(new ArrayList<PhysicalContent>(), null);
                hasSkipped   = false;
            } else {

                // skip = true;
                // hasSkipped = true;
            }
        }
    }

    private void markBothWaysFromCurrent(final PhysicalContent current,
            @NotNull final List<PhysicalContent> line) {

        final int currentIndex = line.indexOf(current);

        /* left/up */
        for (int index = currentIndex - 1; (index >= 0); index--) {
            if (!markEverythingConnectedFrom(line.get(index))){
                break;
            }
        }

        /* right / down */
        for (int index = currentIndex + 1; (index < line.size()); index++) {
            if (!markEverythingConnectedFrom(line.get(index))){
                break;
            }
        }
    }

    private void printLastBlock() {

        StringBuffer          sb   = new StringBuffer();
        List<PhysicalContent> list = allBlocks.get(allBlocks.size() - 1).getContents();

        for (PhysicalContent content : list) {
            if (content.isText()) {
                sb.append(content.getPhysicalText().getText());
            }
        }

        log.info("LOG01370:Created block" + sb);
    }
}
