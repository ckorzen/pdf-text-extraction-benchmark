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



package org.elacin.pdfextract.physical.column;

import org.apache.log4j.Logger;

import org.elacin.pdfextract.content.PhysicalContent;
import org.elacin.pdfextract.content.PhysicalPageRegion;
import org.elacin.pdfextract.content.WhitespaceRectangle;
import org.elacin.pdfextract.geom.MathUtils;
import org.elacin.pdfextract.geom.Rectangle;
import org.elacin.pdfextract.geom.Sorting;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.elacin.pdfextract.Constants.*;
import static org.elacin.pdfextract.geom.RectangleCollection.Direction.E;
import static org.elacin.pdfextract.geom.RectangleCollection.Direction.W;

/**
 * Created by IntelliJ IDEA. User: elacin Date: Sep 23, 2010 Time: 12:54:21 PM To change this
 * template use File | Settings | File Templates.
 */
public class ColumnFinder {

    private static final String chars = "()[]abcdef1234567890o.?* ";

// ------------------------------ FIELDS ------------------------------
    public static final float   DEFAULT_COLUMN_WIDTH = 2.0f;
    private static final Logger log                  = Logger.getLogger(ColumnFinder.class);

// -------------------------- PUBLIC STATIC METHODS --------------------------
    @NotNull
    public static List<WhitespaceRectangle> extractColumnBoundaries(@NotNull PhysicalPageRegion region,
            @NotNull List<WhitespaceRectangle> whitespaces) {

        final List<WhitespaceRectangle> columnBoundaries = selectCandidateColumnBoundaries(region,
                                                               whitespaces);

        /* adjust columns to real height */
        if (COLUMNS_ENABLE_COLUMN_HEIGHT_ADJUSTMENT) {
            adjustColumnHeights(region, columnBoundaries);
        }

        filter(region, columnBoundaries);
        combineColumnBoundaries(region, columnBoundaries);

        return columnBoundaries;
    }

// -------------------------- STATIC METHODS --------------------------
    @Nullable
    private static WhitespaceRectangle adjustColumn(final PhysicalPageRegion region,
            final WhitespaceRectangle boundary, final float boundaryStartX, final float boundaryEndX) {

        final Rectangle rpos = region.getPos();

        /* find surrounding content */
        final List<PhysicalContent> everythingRightOf = findAllContentsRightOf(region, boundaryStartX);

        Collections.sort(everythingRightOf, Sorting.sortByLowerYThenLowerX);

        final List<PhysicalContent> closeOnLeft = findAllContentsImmediatelyLeftOfX(region,
                                                      boundaryStartX);

        Collections.sort(closeOnLeft, Sorting.sortByLowerYThenLowerX);

        float   realBoundaryY    = rpos.y;
        float   realBoundaryEndY = rpos.endY;
        boolean startYFound      = false;
        boolean boundaryStarted  = false;

        for (int y = (int) rpos.y; y <= (int) (rpos.endY + 1.0F); y++) {
            boolean foundContentRightOfX;
            PhysicalContent closestOnRight = getClosestToTheRightAtY(everythingRightOf, y,
                                                 boundary.getPos().endX);

            if (closestOnRight == null) {
                continue;
            } else {
                foundContentRightOfX = true;
            }

            /** if we find something blocking this row, start looking further down */

            /* content will be blocking if it intersects, naturally */
            boolean blocked = false;

            if (closestOnRight.getPos().x <= boundaryStartX) {
                blocked = true;
            } else if (COLUMNS_ENABLE_TEXT_SPLIT_CHECK) {

                /* also check if this column boundary would separate two words which otherwise are very close */
                for (PhysicalContent left : closeOnLeft) {
                    if (left instanceof WhitespaceRectangle) {
                        continue;
                    }

                    if (y < (int) left.getPos().y) {
                        continue;
                    }

                    if (y > (int) left.getPos().endY) {
                        continue;
                    }

                    if (closestOnRight.getPos().x - left.getPos().endX < 6.0f) {
                        blocked = true;

                        break;
                    }
                }
            }

            if (blocked) {
                if (boundaryStarted) {
                    break;
                }

                startYFound = false;
            } else {
                if (!startYFound && foundContentRightOfX) {
                    startYFound   = true;
                    realBoundaryY = (float) (y - 1);
                }

                if (!boundaryStarted && (y > (int) boundary.getPos().y)) {
                    boundaryStarted = true;
                }

                realBoundaryEndY = (float) y;
            }
        }

        if (!startYFound) {
            return null;
        }

        final Rectangle adjusted = new Rectangle(boundaryStartX, realBoundaryY + 0.5f, 1.0f,
                                       Math.max(0.1f, realBoundaryEndY - realBoundaryY - 0.5F));
        final WhitespaceRectangle newBoundary = new WhitespaceRectangle(adjusted);

        newBoundary.setScore(1000);

        return newBoundary;
    }

    private static void adjustColumnHeights(@NotNull PhysicalPageRegion region,
            @NotNull List<WhitespaceRectangle> columnBoundaries) {

        final Collection<WhitespaceRectangle> newBoundaries = new ArrayList<WhitespaceRectangle>();

        for (final WhitespaceRectangle boundary : columnBoundaries) {
            final Rectangle bpos = boundary.getPos();

            /*
             *  calculate three possible columns, on the left and right side of the rectangle,
             *   and along the middle
             */
            final float ADJUST    = 1.0f;
            final float leftX     = Math.min(bpos.x + ADJUST, bpos.endX);
            final float leftEndX  = Math.min(leftX + COLUMNS_MIN_COLUMN_WIDTH, bpos.endX - ADJUST);
            final float midX      = bpos.getMiddleX();
            final float midEndX   = Math.min(midX + COLUMNS_MIN_COLUMN_WIDTH, bpos.endX);
            final float rightEndX = Math.max(bpos.endX - ADJUST, bpos.x);
            final float rightX    = Math.max(rightEndX - COLUMNS_MIN_COLUMN_WIDTH, bpos.x);

            //
            final WhitespaceRectangle middle = adjustColumn(region, boundary, midX, midEndX);
            final WhitespaceRectangle left   = adjustColumn(region, boundary, leftX, leftEndX);
            final WhitespaceRectangle right  = adjustColumn(region, boundary, rightX, rightEndX);

            /* then choose the tallest */
            final float lHeight = ((left == null) ? -1.0f : left.getPos().height);
            final float mHeight = ((middle == null) ? -1.0f : middle.getPos().height);
            final float rHeight = ((right == null) ? -1.0f : right.getPos().height);

            //
            @Nullable final WhitespaceRectangle adjusted;

            if ((lHeight > mHeight) && (lHeight > rHeight)) {
                adjusted = left;
            } else if ((rHeight > mHeight) && (rHeight > lHeight)) {
                adjusted = right;
            } else {
                if (middle != null) {
                    adjusted = middle;
                } else if (right != null) {
                    adjusted = right;
                } else if (left != null) {
                    adjusted = left;
                } else {
                    adjusted = null;
                }
            }

            if ((adjusted != null) &&!newBoundaries.contains(adjusted)) {
                newBoundaries.add(adjusted);
            }
        }

        columnBoundaries.clear();
        columnBoundaries.addAll(newBoundaries);
    }

    private static void combineColumnBoundaries(@NotNull PhysicalPageRegion region,
            @NotNull List<WhitespaceRectangle> columnBoundaries) {

        for (int i = 0; i < columnBoundaries.size() - 1; i++) {
            WhitespaceRectangle left  = columnBoundaries.get(i);
            WhitespaceRectangle right = columnBoundaries.get(i + 1);
            final Rectangle     rpos  = right.getPos();
            final Rectangle     lpos  = left.getPos();

            if (Math.abs(rpos.x - lpos.x) < 50.0F) {

                /* combine the two. try first to pick a column index at the right hand side */
                final float                 startY        = Math.min(rpos.y, lpos.y);
                final float                 endY          = Math.max(rpos.endY, lpos.endY);
                float                       endX          = Math.max(rpos.endX, lpos.endX);
                float                       startX        = endX - DEFAULT_COLUMN_WIDTH;
                Rectangle newPos                          = new Rectangle(startX, startY,
                                                                DEFAULT_COLUMN_WIDTH, endY - startY);
                final List<PhysicalContent> intersectingR = region.findContentsIntersectingWith(newPos);

                /* if the first try intersected with something - try left */
                if (!intersectingR.isEmpty()) {
                    startX = Math.max(rpos.x, lpos.x);
                    newPos = new Rectangle(startX, startY, DEFAULT_COLUMN_WIDTH, endY - startY);
                }

                final List<PhysicalContent> intersectingL = region.findContentsIntersectingWith(newPos);

                if (!intersectingL.isEmpty()) {
                    continue;
                }

                log.warn("LOG01300:Combining column boundaries " + rpos + " and " + lpos);

                WhitespaceRectangle newBoundary = new WhitespaceRectangle(newPos);

                newBoundary.setScore(1000);
                columnBoundaries.set(i, newBoundary);
                columnBoundaries.remove(i + 1);
                i--;
                Collections.sort(columnBoundaries, Sorting.sortByLowerX);
            }
        }
    }

    private static void filter(final PhysicalPageRegion r, final List<WhitespaceRectangle> boundaries) {

        List<WhitespaceRectangle> toRemove = new ArrayList<WhitespaceRectangle>();
        StringBuilder sb = new StringBuilder();

        Collections.sort(boundaries, Sorting.sortByLowerX);

        for (int i = boundaries.size() - 1; i >= 0; i--) {
            final WhitespaceRectangle boundary = boundaries.get(i);

            if (boundary.getPos().height < r.getPos().height * 0.15f) {
                toRemove.add(boundary);

                continue;
            }

            final float boundaryToTheLeft = 20;

//            if (i == 0) {
//                boundaryToTheLeft = r.getPos().x;
//            } else {
//                boundaryToTheLeft = boundaries.get(i - 1).getPos().endX;
//            }

            final Rectangle bpos        = boundary.getPos();
            final float     searchWidth = bpos.x - boundaryToTheLeft;

            if (searchWidth <= 0.0F) {
                toRemove.add(boundary);

                continue;
            }

            Rectangle search                             = new Rectangle(boundaryToTheLeft, bpos.y,
                                                               searchWidth, bpos.height);
            final List<PhysicalContent> contentToTheLeft = r.findContentsIntersectingWith(search);

            /* demand a certain amount of words on the left side to split */
            if (contentToTheLeft.size() < 4) {
                toRemove.add(boundary);

                continue;
            }

            sb.setLength(0);

            for (PhysicalContent content : contentToTheLeft) {
                if (content.isText()) {
                    sb.append(content.getPhysicalText().getText());
                }
            }

            int charsFound = 0;

            for (int j = 0; j < sb.length(); j++) {
                if (chars.indexOf(sb.charAt(j)) == -1) {
                    charsFound++;
                }
            }

            if (charsFound <= 4) {
                toRemove.add(boundary);

                continue;
            }

            if ((sb.length() < 20) && (charsFound < 10)) {
                toRemove.add(boundary);

                continue;
            }

            if (boundary.getPos().x < r.getPos().x + r.getPos().width * 0.05f) {
                toRemove.add(boundary);

                continue;
            }

            if (boundary.getPos().endX > r.getPos().endX - r.getPos().width * 0.05f) {
                toRemove.add(boundary);

                continue;
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("Removing columns" + toRemove);
        }

        boundaries.removeAll(toRemove);
    }

    @NotNull
    private static List<PhysicalContent> findAllContentsImmediatelyLeftOfX(
            @NotNull PhysicalPageRegion region, float x) {

        final float     lookLeft = 10.0f;
        final Rectangle rpos     = region.getPos();
        final Rectangle search   = new Rectangle(x - lookLeft, rpos.y, lookLeft, rpos.height);

        return region.findContentsIntersectingWith(search);
    }

    @NotNull
    private static List<PhysicalContent> findAllContentsRightOf(@NotNull PhysicalPageRegion region,
            float x) {

        final Rectangle search = new Rectangle(x, region.getPos().y, region.getPos().width,
                                     region.getPos().height);

        return region.findContentsIntersectingWith(search);
    }

    /**
     * @param everythingRightOf x sorted list
     * @param y                 row to look at
     * @param endX
     * @return
     */
    @Nullable
    private static PhysicalContent getClosestToTheRightAtY(
            final List<PhysicalContent> everythingRightOf, final int y, final float endX) {

        PhysicalContent closest     = null;
        float           minDistance = Float.MAX_VALUE;

        for (int j = 0; j < everythingRightOf.size(); j++) {
            PhysicalContent content = everythingRightOf.get(j);

            if ((content instanceof WhitespaceRectangle) || (content instanceof PhysicalPageRegion)) {
                continue;
            }

            final Rectangle blockerPos = content.getPos();

            if (blockerPos.endY < (float) y) {
                continue;
            }

            if (blockerPos.y > (float) y) {
                break;
            }

            float distance = blockerPos.x - endX;

            if (distance < minDistance) {
                minDistance = distance;
                closest     = content;
            }
        }

        return closest;
    }

    @NotNull
    private static List<WhitespaceRectangle> selectCandidateColumnBoundaries(
            @NotNull PhysicalPageRegion region, @NotNull List<WhitespaceRectangle> whitespaces) {

        final float                     LOOKAHEAD        = 10.0f;
        final float                     HALF_LOOKAHEAD   = LOOKAHEAD / 2.0F;
        final List<WhitespaceRectangle> columnBoundaries = new ArrayList<WhitespaceRectangle>();

        for (WhitespaceRectangle whitespace : whitespaces) {
            final Rectangle pos     = whitespace.getPos();
            final float     posX    = pos.x;
            final float     posEndX = pos.endX;

            if (pos.height / pos.width <= 1.5f) {
                continue;
            }

            final Rectangle smallerPos = pos.getAdjustedBy(-1.0f);

            /* count how much text is to the immediate left of the current whitespace */
            final List<PhysicalContent> left = region.searchInDirectionFromOrigin(W, smallerPos,
                                                   LOOKAHEAD);

            Collections.sort(left, Sorting.sortByHigherX);

            int leftCount = 0;

            for (PhysicalContent content : left) {
                if (content instanceof WhitespaceRectangle) {
                    continue;
                }

                if (MathUtils.isWithinVariance(content.getPos().endX, posX + HALF_LOOKAHEAD,
                                               LOOKAHEAD)) {
                    leftCount++;
                }
            }

            /* and how much is to the right */
            final List<PhysicalContent> right = region.searchInDirectionFromOrigin(E, smallerPos,
                                                    LOOKAHEAD);

            Collections.sort(right, Sorting.sortByLowerX);

            int rightCount = 0;

            for (PhysicalContent content : right) {
                if (content instanceof WhitespaceRectangle) {
                    continue;
                }

                if (MathUtils.isWithinVariance(content.getPos().x, posEndX + HALF_LOOKAHEAD,
                                               LOOKAHEAD)) {
                    rightCount++;
                }
            }

            if ((leftCount == 0) && (rightCount < 8)) {
                continue;
            }

            if ((rightCount == 0) && (leftCount < 8)) {
                continue;
            }

            if ((leftCount >= 3) || (rightCount >= 3)) {
                columnBoundaries.add(whitespace);
                whitespace.setScore(500);
            }
        }

        return columnBoundaries;
    }
}
