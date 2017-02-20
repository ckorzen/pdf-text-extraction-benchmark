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
import org.elacin.pdfextract.content.PhysicalPage;
import org.elacin.pdfextract.content.PhysicalPageRegion;
import org.elacin.pdfextract.geom.Rectangle;
import org.elacin.pdfextract.style.Style;
import org.elacin.pdfextract.style.StyleComparator;
import org.elacin.pdfextract.style.StyleDifference;
import org.elacin.pdfextract.style.TextUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by IntelliJ IDEA. User: elacin Date: 08.12.10 Time: 01.27 To change this template use
 * File | Settings | File Templates.
 */
public class PageRegionSplitBySpacing {

// ------------------------------ FIELDS ------------------------------
    private static final Logger log = Logger.getLogger(PageRegionSplitBySpacing.class);

// -------------------------- PUBLIC STATIC METHODS --------------------------
    @NotNull
    public static boolean splitOfTopTextOfPage(@NotNull PhysicalPage page, float fractionToConsider) {

        PhysicalPageRegion r                      = page.getMainRegion();
        final Rectangle    realDims               = r.getPage().getPageDimensions();
        final int          minimumDistanceToSplit = 10;

        return tryHorizontalSplit(r, realDims, fractionToConsider, minimumDistanceToSplit);
    }

    @NotNull
    public static boolean splitRegionHorizontally(@NotNull PhysicalPageRegion region) {

        final int minimumDistanceToSplit = 20;

        return tryHorizontalSplit(region, region.getPos(), 1.0f, minimumDistanceToSplit);
    }

// -------------------------- STATIC METHODS --------------------------
    private static boolean sameStyleOverAndUnderHorizontalLine(final PhysicalPageRegion r,
            final float y, final Set<PhysicalContent> over) {

        List<PhysicalContent> under  = new ArrayList<PhysicalContent>();
        float                 yIndex = y;

        while (under.isEmpty() && (yIndex < r.getPos().endY)) {
            under.addAll(r.findContentAtYIndex(yIndex));
            yIndex += 1.0f;
        }

        final Style styleOver  = TextUtils.findDominatingStyle(over);
        final Style styleUnder = TextUtils.findDominatingStyle(under);

        return StyleComparator.styleCompare(styleOver, styleUnder) == StyleDifference.SAME_STYLE;
    }

    private static boolean tryHorizontalSplit(final PhysicalPageRegion r, final Rectangle dims,
            final float fractionToConsider, final int minimumDistanceToSplit) {

        final float          startY       = dims.y;
        final float          endY         = Math.min(r.getPos().endY,
                                                startY + dims.height * fractionToConsider);
        float                lastBoundary = -1000.0f;
        float                minX         = Float.MAX_VALUE,
                             maxX         = Float.MIN_VALUE;
        PhysicalPageRegion   activeRegion = r;
        Set<PhysicalContent> workingSet   = new HashSet<PhysicalContent>();

        for (float y = startY; y <= endY; y++) {
            if (y < activeRegion.getPos().y) {
                continue;
            }

            final List<PhysicalContent> row = activeRegion.findContentAtYIndex(y);

            workingSet.addAll(row);

            for (PhysicalContent content : row) {
                minX = Math.min(content.getPos().x, minX);
                maxX = Math.max(content.getPos().endX, maxX);
            }

            if (row.isEmpty()) {
                if (!TextUtils.listContainsStyledText(workingSet)) {
                    continue;
                }

                if ((y - lastBoundary < minimumDistanceToSplit)) {
                    continue;
                }

                if (sameStyleOverAndUnderHorizontalLine(activeRegion, y, workingSet)) {
                    continue;
                }

                if (log.isInfoEnabled()) {
                    log.info(String.format("LOG00530:split/hor at y=%s for %s ", y, activeRegion));
                }

                boolean success = PageRegionSplitBySeparators.splitRegionAtY(activeRegion, y);

                if (!success) {
                    break;
                }

                PhysicalPageRegion lowerNewSubRegion = activeRegion.getSubregions().get(
                                                           activeRegion.getSubregions().size() - 1);

                activeRegion = lowerNewSubRegion;

                // tryHorizontalSplit(lowerNewSubRegion, dims, fractionToConsider, minimumDistanceToSplit);
                // return r.extractSubRegionFromContent(workingSet);
                workingSet.clear();
                lastBoundary = y;

                /* only extract once */

                // return true;
            } else {
                lastBoundary = y;
            }
        }

        return false;
    }
}
