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



package org.elacin.pdfextract.datasource.graphics;

import org.apache.log4j.Logger;
import org.elacin.pdfextract.content.GraphicContent;
import org.elacin.pdfextract.geom.Rectangle;
import org.elacin.pdfextract.geom.Sorting;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Created by IntelliJ IDEA. User: elacin Date: 15.01.11 Time: 21.38 To change this template use
 * File | Settings | File Templates.
 */
public class DrawingSurfaceImpl implements DrawingSurface {

// ------------------------------ FIELDS ------------------------------
    private static final Logger log = Logger.getLogger(DrawingSurfaceImpl.class);

    /**
     * These lists will hold the contents while we are drawing it. This is grouped based on physical
     * properties only
     */
    @NotNull
    final List<GeneralPath>    figurePaths = new ArrayList<GeneralPath>();
    @NotNull
    final List<GraphicContent> pictures    = new ArrayList<GraphicContent>();
    @NotNull
    List<GraphicContent>       combined    = new ArrayList<GraphicContent>();

// ------------------------ INTERFACE METHODS ------------------------
// --------------------- Interface DrawingSurface ---------------------
    public void clearSurface() {

        figurePaths.clear();
        pictures.clear();
        combined = new ArrayList<GraphicContent>();
    }

    @SuppressWarnings({ "NumericCastThatLosesPrecision" })
    public void drawImage(@NotNull final Image image, @NotNull final AffineTransform at,
                          @NotNull final Shape clippingPath) {

        /* transform the coordinates by using the affinetransform. */
        Point2D upperLeft  = at.transform(new Point2D.Float(0.0F, 0.0F), null);
        Point2D dim        = new Point2D.Float((float) image.getWidth(null),
                                 (float) image.getHeight(null));
        Point2D lowerRight = at.transform(dim, null);

        /* this is necessary because the image might be rotated */
        float x    = (float) Math.min(upperLeft.getX(), lowerRight.getX());
        float endX = (float) Math.max(upperLeft.getX(), lowerRight.getX());
        float y    = (float) Math.min(upperLeft.getY(), lowerRight.getY());
        float endY = (float) Math.max(upperLeft.getY(), lowerRight.getY());

        /* respect the bound if set */
        final Rectangle2D bounds = clippingPath.getBounds2D();

        x = (float) Math.max(bounds.getMinX(), x);
        y = (float) Math.max(bounds.getMinY(), y);

        if (bounds.getMaxX() > 0.0) {
            endX = (float) Math.min(bounds.getMaxX(), endX);
        }

        if (bounds.getMaxY() > 0.0) {
            endY = (float) Math.min(bounds.getMaxY(), endY);
        }

        /* build the finished position - this will also do some sanity checking */
        org.elacin.pdfextract.geom.Rectangle pos;

        try {
            pos = new org.elacin.pdfextract.geom.Rectangle(x, y, endX - x, endY - y);
        } catch (Exception e) {
            log.warn("LOG00590:Error while adding graphics: " + e.getMessage());

            return;
        }

        pictures.add(new GraphicContent(pos, true, Color.BLACK));
    }

    public void fill(@NotNull final GeneralPath originalPath, @NotNull final Color color,
                     Shape currentClippingPath) {
        addVectorPath(originalPath, color, currentClippingPath);
    }

    @NotNull
    public List<GraphicContent> getGraphicContents() {

        if (!combined.isEmpty()) {
            return combined;
        }

        for (GeneralPath figurePath : figurePaths) {
            try {
                final org.elacin.pdfextract.geom.Rectangle pos = convertRectangle(
                                                                     figurePath.getBounds());
                final GraphicContent newFigure = new GraphicContent(pos, false, Color.BLACK);

                /*
                 *  some times bounding boxes around text might be drawn twice, in white and in another colour
                 * .
                 *  take advantage of the fact that figures with equal positions are deemed equal for the set,
                 *  find an existing one with same position, and combine them. Prefer to keep that which
                 *  stands
                 *  out from the background, as that is more useful :)
                 */
                if (newFigure.getColor().equals(Color.WHITE)) {
                    continue;
                }

                combined.add(newFigure);
            } catch (Exception e) {
                log.warn("LOG00580:Error while filling path " + figurePath + ": ", e);
            }
        }

        combineGraphics(combined);

        if (pictures.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("LOG01210:no pictures to combine");
            }
        } else {
            combineGraphics(pictures);
        }

        combined.addAll(pictures);

        return combined;
    }

    public void strokePath(@NotNull final GeneralPath originalPath, @NotNull final Color color,
                           Shape currentClippingPath) {
        addVectorPath(originalPath, color, currentClippingPath);
    }

// -------------------------- STATIC METHODS --------------------------
    private static void combineGraphics(@NotNull final List<GraphicContent> list) {

        /**
         * Segment images
         *
         * We segment figures and pictures separately.
         *
         * The segmentation is done by first finding a list of graphical content which contains
         *  a certain amount of text which is then excluded from segmentation (because we later on
         *  use these graphics to separate text, so that information is most probably useful).
         *
         * Then we try to identify clusters of graphics, and combine them
         *
         */
        final long t0           = System.currentTimeMillis();
        final int  originalSize = list.size();

        for (Iterator<GraphicContent> iterator = list.iterator(); iterator.hasNext(); ) {
            final GraphicContent content = iterator.next();

            if (content.isFigure() && content.isBackgroundColor()) {
                iterator.remove();
            }
        }

        Collections.sort(list, Sorting.sortByLowerYThenLowerX);

        for (int i = 0; i < list.size(); i++) {
            final GraphicContent current = list.get(i);

            /*
             *  for every current - check the rest of the graphics in the list
             *   to see if its possible to combine
             */
            for (int j = i + 1; j < list.size(); j++) {
                float     minX            = current.getPos().x;
                float     minY            = current.getPos().y;
                float     maxX            = current.getPos().endX;
                float     maxY            = current.getPos().endY;
                Color     c               = current.getColor();
                final int firstCombinable = j;

                /* since we sorted the elements there might be several in a row - combine them all */
                while ((j < list.size()) && current.canBeCombinedWith(list.get(j))) {
                    minX = Math.min(minX, list.get(j).getPos().x);
                    minY = Math.min(minY, list.get(j).getPos().y);
                    maxX = Math.max(maxX, list.get(j).getPos().endX);
                    maxY = Math.max(maxY, list.get(j).getPos().endY);

                    if (!Color.WHITE.equals(c)) {
                        c = list.get(j).getColor();
                    }

                    j++;
                }

                /**
                 * combine if  we found some
                 */

                /*
                 *  i = 0
                 *  firstCombinable = 2
                 *  j = 3
                 * --
                 *  combine 0 and 2 only, j is one too high.
                 */
                if (firstCombinable != j) {

                    /* first remove */
                    final int numToCombine = j - firstCombinable;

                    for (int u = 0; u < numToCombine; u++) {
                        list.remove(firstCombinable);    // removing elements from the first one
                    }

                    list.remove(i);

                    /* then add the new graphic */
                    list.add(new GraphicContent(new Rectangle(minX, minY, maxX - minX, maxY - minY),
                                                current.isPicture(), c));
                    i = -1;    // start over

                    break;
                }
            }
        }

        if (log.isInfoEnabled() && (originalSize != list.size())) {
            log.info("LOG01310:Combined " + originalSize + " graphical elements into " + list.size()
                     + " in " + (System.currentTimeMillis() - t0) + "ms");
        }
    }

    @NotNull
    private static Rectangle convertRectangle(@NotNull final java.awt.Rectangle bounds) {

        return new Rectangle((float) bounds.x, (float) bounds.y, (float) bounds.width,
                             (float) bounds.height);
    }

// -------------------------- OTHER METHODS --------------------------
    private void addVectorPath(@NotNull GeneralPath originalPath, @NotNull Color color,
                               Shape clippingPath) {

        if (color.equals(Color.WHITE)) {
            return;
        }

        // if (!clippingPath.contains(originalPath.getBounds())) {
        // return;
        // }
        List<GeneralPath> paths = PathSplitter.splitPath(originalPath);

        for (GeneralPath path : paths) {
            boolean addedPath = false;

            for (GeneralPath figurePath : figurePaths) {
                if (figurePath.intersects(path.getBounds())) {
                    figurePath.append(path, true);
                    addedPath = true;

                    break;
                }
            }

            if (!addedPath) {
                GeneralPath newPath = new GeneralPath(path);

                figurePaths.add(newPath);
            }
        }
    }
}
