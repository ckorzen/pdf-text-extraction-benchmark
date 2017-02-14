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
import org.elacin.pdfextract.Constants;
import org.elacin.pdfextract.content.PhysicalContent;
import org.elacin.pdfextract.content.PhysicalPageRegion;
import org.elacin.pdfextract.content.WhitespaceRectangle;
import org.elacin.pdfextract.geom.FloatPoint;
import org.elacin.pdfextract.geom.HasPosition;
import org.elacin.pdfextract.geom.Rectangle;
import org.elacin.pdfextract.geom.RectangleCollection;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

import static org.elacin.pdfextract.Constants.*;
import static org.elacin.pdfextract.geom.RectangleCollection.Direction.E;
import static org.elacin.pdfextract.geom.RectangleCollection.Direction.W;

/**
 * Created by IntelliJ IDEA. User: elacin Date: Jun 23, 2010 Time: 13:05:06
 */
public final class WhitespaceFinder {

// ------------------------------ FIELDS ------------------------------
    private static final Logger log = Logger.getLogger(WhitespaceFinder.class);

    /* min[Height|Width] are the thinnest rectangles we will accept */
    private final float minHeight, minWidth;

    /* all the obstacles in the algorithm are found here, and are initially all
        the words on the page */
    protected final RectangleCollection region;

    /**
     * State while working follows below
     */

    /* a queue which will give us the biggest/best rectangles first */
    private final PriorityQueue<QueueEntry> queue;

    /* this holds a list of all queue entries which are not yet accepted. Upon finding a new
     * whitespace rectangle, these are added back to the queue. */
    private final List<QueueEntry> holdList = new ArrayList<QueueEntry>();

    /* this holds all the whitespace rectangles we have found */
    private final WhitespaceRectangle[] foundWhitespace;
    private int                         foundWhitespaceCount = 0;

    /* the number of whitespace we want to find */
    private final int wantedWhitespaces;

// --------------------------- CONSTRUCTORS ---------------------------
    WhitespaceFinder(RectangleCollection region, final int numWantedWhitespaces, final float minWidth,
                     final float minHeight) {

        this.region       = region;
        wantedWhitespaces = numWantedWhitespaces;
        foundWhitespace   = new WhitespaceRectangle[numWantedWhitespaces];
        queue             = new PriorityQueue<QueueEntry>(WHITESPACE_MAX_QUEUE_SIZE);
        this.minWidth     = minWidth;
        this.minHeight    = minHeight;
    }

// -------------------------- PUBLIC STATIC METHODS --------------------------
    public static List<WhitespaceRectangle> findWhitespace(final PhysicalPageRegion region) {

        final long t0             = System.currentTimeMillis();
        final int  numWhitespaces = WHITESPACE_NUMBER_WANTED;
        WhitespaceFinder finder   = new WhitespaceFinder(region, numWhitespaces,
                                        region.getMinimumColumnSpacing(), region.getMinimumRowSpacing());
        final List<WhitespaceRectangle> ret  = finder.findWhitespace();
        final long                      time = System.currentTimeMillis() - t0;

        log.info(String.format("LOG00380:%d of %d whitespaces for %s in %d ms", ret.size(),
                               numWhitespaces, region, time));

        return ret;
    }

// -------------------------- STATIC METHODS --------------------------

    /**
     * Finds the obstacle which is closest to the centre of the rectangle bound
     */
    static HasPosition choosePivot(QueueEntry entry) {

        final FloatPoint centrePoint     = entry.bound.centre();
        float            minDistance     = Float.MAX_VALUE;
        HasPosition      closestToCentre = entry.obstacles[0];

        for (int i = 0; i < entry.numObstacles; i++) {
            HasPosition obstacle = entry.obstacles[i];
            final float distance = obstacle.getPos().distance(centrePoint) * 100.0f
                                   / obstacle.getPos().height;

            if (distance < minDistance) {
                minDistance     = distance;
                closestToCentre = obstacle;
            }
        }

        return closestToCentre;
    }

    /**
     * Checks whether the rectangle represented by whitespaceCandidate is empty enough to be
     *  considered a whitespace rectangle
     */
    static boolean isEmptyEnough(QueueEntry whitespaceCandidate) {

        if (Constants.WHITESPACE_FUZZY_EMPTY_CHECK && (whitespaceCandidate.numObstacles != 0)) {

            /* accept a small intersection */
            float       intersectSum   = 0.0f,
                        whitespaceArea = whitespaceCandidate.bound.area();
            final float intersectLimit = whitespaceArea * WHITESPACE_FUZZINESS;

            for (int i = 0; i < whitespaceCandidate.numObstacles; i++) {
                final Rectangle obstaclePos  = whitespaceCandidate.obstacles[i].getPos();
                final float intersectSize    = whitespaceCandidate.bound.intersection(
                                                   obstaclePos).area();
                final float     smallestArea = Math.min(obstaclePos.area(), whitespaceArea);

                if (intersectSize > smallestArea * WHITESPACE_FUZZINESS) {
                    return false;
                }

                intersectSum += intersectSize;
            }

            return intersectSum < intersectLimit;
        }

        return whitespaceCandidate.numObstacles == 0;
    }

    /**
     * This is the quality function by which we sort rectangles to choose the 'best' one first. The
     * current function bases itself on the area of the rectangle, and then prefers high ones
     */
    static float rectangleQuality(Rectangle r) {
        return r.area() * (1 + r.height * 0.25f);
    }

// -------------------------- OTHER METHODS --------------------------

    /**
     *  The main algorithm. Finds the next whitespace rectangle
     * @return A new identified whitespace rectangle
     */
    WhitespaceRectangle findNextWhitespace() {

        queue.addAll(holdList);
        holdList.clear();

        while (!queue.isEmpty()) {

            /** Place an upper bound. If we reach this queue size we should already have enough data */
            if (WHITESPACE_MAX_QUEUE_SIZE - 4 <= queue.size()) {
                log.warn("Queue too long");

                return null;
            }

            /** this will always choose the rectangle with the highest priority */
            final QueueEntry current = queue.remove();

            /**
             * If we have accepted a whitespace rectangle since this was added to the queue, we need
             *  to recalculate the obstacles it references to make sure it doesnt overlap
             */
            if (current.numberOfWhitespaceFound != foundWhitespaceCount) {
                updateObstacleListForQueueEntry(current);
            }

            /**
             * if this contains no obstacles (or just barely touches on some) we have found a
             *  new whitespace rectangle
             */
            if (isEmptyEnough(current)) {
                final WhitespaceRectangle newWhitespace = new WhitespaceRectangle(current.bound);

                /** check if we accept the whitespace rectangle or not */

                /* check whether the whitespace is connected to either an edge or an existing
                 * whitespace. if it is not, leave it in the holdList list for now */
                if (WHITESPACE_CHECK_CONNECTED_FROM_EDGE &&!isNextToWhitespaceOrEdge(newWhitespace)) {
                    holdList.add(current);

                    continue;
                }

                /* find all the surrounding content. make sure this rectangle is not too small.
                 * This is an expensive check, which is why it is done here. i think it is still
                 * correct. */
                if (WHITESPACE_CHECK_LOCAL_HEIGHT) {
                    if (isWhitespaceTooShortForSurroundingText(newWhitespace)) {
                        continue;
                    }
                }

                /* we do not want to accept whitespace rectangles which has only one or two words
                 * on each side (0 is fine), as these doesn't affect layout and tend to break up
                 * small paragraphs of text unnecessarily */
                if (WHITESPACE_CHECK_TEXT_BOTH_SIDES) {
                    if (isWhitespaceNeedlesslySeparatingText(newWhitespace)) {
                        continue;
                    }
                }

                return newWhitespace;
            }

            /** choose an obstacle near the middle of the current rectangle */
            final HasPosition pivot = choosePivot(current);

            /**
             * Create four subrectangles, one on each side of the pivot, and determine the obstacles
             *  located inside it. Then add each subrectangle to the queue (as long as it is not too
             *  thin)
             */
            final QueueEntry[] subrectangles = splitSearchAreaAround(current, pivot);

            for (QueueEntry sub : subrectangles) {
                if (sub == null) {
                    continue;
                }

                queue.add(sub);
            }
        }

        /* if we ran out of rectangles in the queue, return null to signal that. */
        return null;
    }

    /**
     * This method provides a personal touch to the algorithm described in the paper which is
     * referenced. Here we will just accept rectangles which are adjacent to either another one
     * which we have already identified, or which are adjacent to the edge of the page.
     * <p/>
     * By assuring that the we thus form continous chains of rectangles, the results seem to be much
     * better.
     */
    final boolean isNextToWhitespaceOrEdge(final WhitespaceRectangle newWhitespace) {

        /* accept this rectangle if it is adjacent to the edge of the page */
        final float     l    = WHITESPACE_OBSTACLE_OVERLAP;
        final Rectangle wPos = newWhitespace.getPos(),
                        rPos = region.getPos();

        if ((wPos.x <= rPos.x + l) || (wPos.y <= rPos.y + l) || (wPos.endX >= rPos.endX - l)
                || (wPos.endY >= rPos.endY - l)) {
            return true;
        }

        /* also accept if it borders one of the already identified whitespaces */
        for (int i = 0; i < foundWhitespaceCount; i++) {
            final WhitespaceRectangle existing = foundWhitespace[i];

            if (wPos.distance(existing.getPos()) <= WHITESPACE_OBSTACLE_OVERLAP) {
                return true;
            }
        }

        return false;
    }

    /**
     * Finds up to the requested amount of whitespace rectangles based on the contents on the page
     * which has been provided.
     *
     * @return whitespace rectangles
     */
    List<WhitespaceRectangle> findWhitespace() {

        if (foundWhitespaceCount == 0) {

            /* first add the whole page (all its contents as obstacle)s to the priority queue */
            int           obstacleCount = region.getContents().size();
            HasPosition[] obstacles     = region.getContents().toArray(new HasPosition[obstacleCount]);

            queue.add(new QueueEntry(region.getPos(), obstacles, obstacleCount, 0));

            /* continue looking for whitespace until we have the wanted number or we run out */
            while (foundWhitespaceCount < wantedWhitespaces) {
                final WhitespaceRectangle newRectangle = findNextWhitespace();

                /* if no further rectangles exist, stop looking */
                if (newRectangle == null) {
                    break;
                }

                foundWhitespace[foundWhitespaceCount++] = newRectangle;
            }
        }

        ArrayList<WhitespaceRectangle> ret = new ArrayList<WhitespaceRectangle>(foundWhitespaceCount);

        for (int i = 0; i < foundWhitespaceCount; i++) {
            ret.add(foundWhitespace[i]);
        }

        return ret;
    }

    /**
     * Check if the whitespace rectangle is made useless by the way it separates text. see thesis
     *  text for details.
     */
    boolean isWhitespaceNeedlesslySeparatingText(final WhitespaceRectangle newWhitespace) {

        if (newWhitespace.getPos().width > 30) {
            return false;
        }

        /* decrease the size a tiny bit, so we don't include what blocked the rectangle, especially
         *   above and below */
        Rectangle                   search     = newWhitespace.getPos().getAdjustedBy(-1.0f);
        final float                 range      = 8.0f;
        final List<PhysicalContent> right      = region.searchInDirectionFromOrigin(E, search, range);
        int                         rightCount = 0;

        for (PhysicalContent content : right) {
            if (content.isText()) {
                rightCount++;
            }
        }

        if ((rightCount == 1) || (rightCount == 2)) {
            final List<PhysicalContent> left      = region.searchInDirectionFromOrigin(W, search, range);
            int                         leftCount = 0;

            for (PhysicalContent content : left) {
                if (content.isText()) {
                    leftCount++;
                }
            }

            if ((leftCount == 1) || (leftCount == 2)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Check if newWhitespace is too small considering the surrounding content
     */
    boolean isWhitespaceTooShortForSurroundingText(final WhitespaceRectangle newWhitespace) {

        final List<PhysicalContent> surroundings = region.findSurrounding(newWhitespace, 8);

        if (!surroundings.isEmpty()) {
            float averageHeight = 0.0f;
            int   counted       = 0;

            for (PhysicalContent surrounding : surroundings) {
                if (surrounding.isText()) {
                    averageHeight += surrounding.getPos().height;
                    counted++;
                }
            }

            if (counted != 0) {
                averageHeight /= (float) counted;

                float u = Math.max(((PhysicalPageRegion) region).getMinimumRowSpacing(), averageHeight);

                if (u > newWhitespace.getPos().height) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Creates four rectangles with the remaining space left after splitting the current rectangle
     * around the pivot. Also divides the obstacles among the newly created rectangles
     */
    QueueEntry[] splitSearchAreaAround(final QueueEntry current, final HasPosition pivot) {

        /* Everything inside here was the definitely most expensive parts of the implementation,
         *   so this is quite optimized to avoid too many float point comparisons and needless
         *   object creations. This cut execution time by some 90ish % :) */
        final int       missingRectangles = wantedWhitespaces - foundWhitespaceCount;
        final float     splitX            = pivot.getPos().x,
                        splitEndX         = pivot.getPos().endX,
                        splitY            = pivot.getPos().y,
                        splitEndY         = pivot.getPos().endY;
        final Rectangle bound             = current.bound;

        /* check which of the four possible subrectangles we want to create, and their dimensions */
        Rectangle     left      = null;
        HasPosition[] leftObs   = null;
        final float   leftWidth = splitX - bound.x;

        if ((splitX > bound.x) && (leftWidth > minWidth)) {
            left    = new Rectangle(bound.x, bound.y, leftWidth, bound.height);
            leftObs = new HasPosition[current.numObstacles + missingRectangles];
        }

        Rectangle     above       = null;
        HasPosition[] aboveObs    = null;
        final float   aboveHeight = splitY - bound.y;

        if ((splitY > bound.y) && (aboveHeight > minHeight)) {
            above    = new Rectangle(bound.x, bound.y, bound.width, aboveHeight);
            aboveObs = new HasPosition[current.numObstacles + missingRectangles];
        }

        Rectangle     right      = null;
        HasPosition[] rightObs   = null;
        final float   rightWidth = bound.endX - splitEndX;

        if ((splitEndX < bound.endX) && (rightWidth > minWidth)) {
            right    = new Rectangle(splitEndX, bound.y, rightWidth, bound.height);
            rightObs = new HasPosition[current.numObstacles + missingRectangles];
        }

        Rectangle     below       = null;
        HasPosition[] belowObs    = null;
        final float   belowHeight = bound.endY - splitEndY;

        if ((splitEndY < bound.endY) && (belowHeight > minHeight)) {
            below    = new Rectangle(bound.x, splitEndY, bound.width, belowHeight);
            belowObs = new HasPosition[current.numObstacles + missingRectangles];
        }

        /**
         * All the obstacles in current already fit within current.bound, so we can do just a quick
         *  check to see where they belong here. this way of doing it is primarily an optimization
         */
        int         leftIndex         = 0,
                    aboveIndex        = 0,
                    rightIndex        = 0,
                    belowIndex        = 0;
        final float adjustedSplitX    = splitX - WHITESPACE_OBSTACLE_OVERLAP,
                    adjustedSplitY    = splitY - WHITESPACE_OBSTACLE_OVERLAP,
                    adjustedSplitEndX = splitEndX + WHITESPACE_OBSTACLE_OVERLAP,
                    adjustedSplitEndY = splitEndY + WHITESPACE_OBSTACLE_OVERLAP;

        for (int i = 0; i < current.numObstacles; i++) {
            HasPosition     obstacle    = current.obstacles[i];
            final Rectangle obstaclePos = obstacle.getPos();

            /* including the pivot will break the algorithm */
            if (obstacle == pivot) {
                continue;
            }

            if ((left != null) && (obstaclePos.x < adjustedSplitX)) {
                leftObs[leftIndex++] = obstacle;
            }

            if ((right != null) && (obstaclePos.endX > adjustedSplitEndX)) {
                rightObs[rightIndex++] = obstacle;
            }

            if ((above != null) && (obstaclePos.y < adjustedSplitY)) {
                aboveObs[aboveIndex++] = obstacle;
            }

            if ((below != null) && (obstaclePos.endY > adjustedSplitEndY)) {
                belowObs[belowIndex++] = obstacle;
            }
        }

        final int n = foundWhitespaceCount;

        return new QueueEntry[] { (left == null) ? null : new QueueEntry(left, leftObs, leftIndex, n),
                                  (right == null)
                                  ? null : new QueueEntry(right, rightObs, rightIndex, n),
                                  (above == null)
                                  ? null : new QueueEntry(above, aboveObs, aboveIndex, n),
                                  (below == null)
                                  ? null : new QueueEntry(below, belowObs, belowIndex, n) };
    }

    /**
     * Checks if some of the newly added whitespace rectangles, that is those discovered after this
     * queue entry was added to the queue, overlaps with the area of this queue entry, and if so
     * adds them to this list of obstacles .
     */
    void updateObstacleListForQueueEntry(final QueueEntry entry) {

        int numNewestObstaclesToCheck = foundWhitespaceCount - entry.numberOfWhitespaceFound;

        for (int i = 0; i < numNewestObstaclesToCheck; i++) {
            final HasPosition obstacle = foundWhitespace[foundWhitespaceCount - 1 - i];

            if (entry.bound.intersectsAdmittingOverlap(obstacle.getPos(), WHITESPACE_OBSTACLE_OVERLAP)) {
                entry.addObstacle(obstacle);
            }

            entry.numberOfWhitespaceFound = foundWhitespaceCount;
        }
    }

// -------------------------- INNER CLASSES --------------------------
    static class QueueEntry implements Comparable<QueueEntry> {

        final Rectangle     bound;
        int                 numberOfWhitespaceFound, numObstacles;
        final HasPosition[] obstacles;
        final float         quality;

        private QueueEntry(final Rectangle bound, final HasPosition[] obstacles, int numObstacles,
                           int numFound) {

            this.bound              = bound;
            this.obstacles          = obstacles;
            this.numObstacles       = numObstacles;
            numberOfWhitespaceFound = numFound;
            quality                 = rectangleQuality(bound);
        }

        public final int compareTo(final QueueEntry other) {
            return Float.compare(other.quality, quality);
        }

        public void addObstacle(HasPosition obstacle) {
            obstacles[numObstacles++] = obstacle;
        }
    }
}
