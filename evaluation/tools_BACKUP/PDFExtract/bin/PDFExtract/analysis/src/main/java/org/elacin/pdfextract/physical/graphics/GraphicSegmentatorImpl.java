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



package org.elacin.pdfextract.physical.graphics;

import org.apache.log4j.Logger;
import org.elacin.pdfextract.content.GraphicContent;
import org.elacin.pdfextract.content.PhysicalContent;
import org.elacin.pdfextract.content.PhysicalPageRegion;
import org.elacin.pdfextract.formula.Formulas;
import org.elacin.pdfextract.geom.Rectangle;
import org.elacin.pdfextract.geom.Sorting;
import org.elacin.pdfextract.style.Style;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Created by IntelliJ IDEA. User: elacin Date: 13.11.10 Time: 03.29 To change this template use
 * File | Settings | File Templates.
 */
public class GraphicSegmentatorImpl implements GraphicSegmentator {

// ------------------------------ FIELDS ------------------------------
    private static final Logger log = Logger.getLogger(GraphicSegmentatorImpl.class);

/*  these are what might be rendered with normal font, so they are in addition to what
     Formulas.containsMath would find*/
    private static String POSSIBLE_MATH_SYMBOLS = "()-+";
    private final float   h;

/*     we need the pages dimensions here, because the size of regions is calculated based on content.
    *   it should be possible for graphic to cover all the contents if it doesnt cover all the page*/
    private final float w;

// --------------------------- CONSTRUCTORS ---------------------------
    public GraphicSegmentatorImpl(final Rectangle dims) {

        w = dims.width;
        h = dims.height;
    }

// ------------------------ INTERFACE METHODS ------------------------
// --------------------- Interface GraphicSegmentator ---------------------
    @NotNull
    public CategorizedGraphics categorizeGraphics(@NotNull List<GraphicContent> graphics,
            @NotNull PhysicalPageRegion region) {

        CategorizedGraphics ret = new CategorizedGraphics();

        categorizeGraphics(ret, region, graphics);

        /*
         *  this is a hack to deal with situations where one creates a table or similar with
         *   horizontal lines only. These would not be separators
         */
        List<GraphicContent> combinedHSeps = combineHorizontalSeparators(ret);

        categorizeGraphics(ret, region, combinedHSeps);
        Collections.sort(ret.getHorizontalSeparators(), Sorting.sortByLowerY);
        Collections.sort(ret.getVerticalSeparators(), Sorting.sortByLowerX);

        if (log.isInfoEnabled()) {
            logGraphics(ret);
        }

        return ret;
    }

// -------------------------- PUBLIC STATIC METHODS --------------------------

    /**
     * consider the graphic a separator if the aspect ratio is high
     */
    public static boolean canBeConsideredHorizontalSeparator(@NotNull GraphicContent g) {

        if (g.getPos().height > 15.0f) {
            return false;
        }

        return g.getPos().width / g.getPos().height > 10.0f;
    }

    public static boolean canBeConsideredMathBarInRegion(@NotNull GraphicContent g,
            @NotNull final PhysicalPageRegion region) {

        if (g.getPos().height > 5.0f) {
            return false;
        }

        if (g.getPos().width / g.getPos().height < 6.0f) {
            return false;
        }

        final List<PhysicalContent> surrounding = region.findSurrounding(g, 10);
        boolean                     foundOver   = false,
                                    foundUnder  = false,
                                    foundMath   = false;

        for (PhysicalContent content : surrounding) {
            if (content.getPos().y < g.getPos().endY) {
                foundUnder = true;
            }

            if (content.getPos().endY > g.getPos().y) {
                foundOver = true;
            }

            if (content.isText()) {
                if (Formulas.textContainsMath(content.getPhysicalText())) {
                    foundMath = true;
                } else {
                    final String text = content.getPhysicalText().getText();

                    for (int i = 0; i < text.length(); i++) {
                        if (POSSIBLE_MATH_SYMBOLS.indexOf(text.charAt(i)) != -1) {
                            foundMath = true;

                            break;
                        }
                    }
                }
            }

            if (foundOver && foundUnder && foundMath) {
                return true;
            }
        }

        return false;
    }

    /**
     * consider the graphic a separator if the aspect ratio is high
     */
    public static boolean canBeConsideredVerticalSeparator(@NotNull GraphicContent g) {

        if (g.getPos().width > 15.0f) {
            return false;
        }

        return g.getPos().height / g.getPos().width > 15.0f;
    }

// -------------------------- STATIC METHODS --------------------------
    private static boolean graphicContainsTextFromRegion(@NotNull final PhysicalPageRegion region,
            @NotNull final GraphicContent graphic) {

        final int limit = 5;
        int       found = 0;

        for (PhysicalContent content : region.getContents()) {
            if (graphic.getPos().contains(content.getPos())) {
                found++;
            }

            if (found == limit) {
                return true;
            }
        }

        return false;
    }

// -------------------------- OTHER METHODS --------------------------
    @NotNull
    private List<GraphicContent> combineHorizontalSeparators(@NotNull CategorizedGraphics ret) {

        Map<String, List<GraphicContent>> hsepsForXCoordinate = new HashMap<String,
                                                                    List<GraphicContent>>();

        for (int i = 0; i < ret.getHorizontalSeparators().size(); i++) {
            GraphicContent hsep          = ret.getHorizontalSeparators().get(i);
            int            x             = ((int) hsep.getPos().x) / 3;    // divide by three as rounding
            int            w             = ((int) hsep.getPos().width) / 3;
            String         combineString = String.valueOf(x) + hsep.getColor() + w;

            if (!hsepsForXCoordinate.containsKey(combineString)) {
                hsepsForXCoordinate.put(combineString, new ArrayList<GraphicContent>());
            }

            hsepsForXCoordinate.get(combineString).add(hsep);
        }

        List<GraphicContent> combinedGraphics = new ArrayList<GraphicContent>();

        for (List<GraphicContent> sepList : hsepsForXCoordinate.values()) {
            if (sepList.size() < 2) {
                continue;
            }

            Collections.sort(sepList, Sorting.sortByLowerY);

            if (log.isInfoEnabled()) {
                log.info("LOG00970:Combining " + sepList);
            }

            ret.getHorizontalSeparators().removeAll(sepList);

            GraphicContent newlyCombined = sepList.get(0);

            for (int i = 1; i < sepList.size(); i++) {
                GraphicContent graphicPart = sepList.get(i);

                if (newlyCombined.getPos().distance(graphicPart.getPos()) > 50.0f) {
                    combinedGraphics.add(newlyCombined);
                    newlyCombined = graphicPart;
                } else {
                    newlyCombined = newlyCombined.combineWith(graphicPart);
                }
            }

            combinedGraphics.add(newlyCombined);
        }

        return combinedGraphics;
    }

    private void categorizeGraphics(@NotNull CategorizedGraphics ret,
                                    @NotNull PhysicalPageRegion region,
                                    @NotNull List<GraphicContent> list) {

        for (GraphicContent graphic : list) {
            if (isTooBigGraphic(graphic)) {
                if (log.isInfoEnabled()) {
                    log.info("LOG00501:considered too big " + graphic);
                }

                continue;
            }

            if (graphicContainsTextFromRegion(region, graphic)) {
                graphic.setCanBeAssigned(false);
                graphic.setStyle(Style.GRAPHIC_CONTAINER);
                ret.getContainers().add(graphic);
            } else if (canBeConsideredMathBarInRegion(graphic, region)) {
                graphic.setCanBeAssigned(true);
                graphic.setStyle(Style.GRAPHIC_MATH_BAR);
                ret.getContents().add(graphic);
//            } else if (canBeConsideredHorizontalSeparator(graphic)) {
//                graphic.setCanBeAssigned(true);
//                graphic.setStyle(Style.GRAPHIC_HSEP);
//                ret.getHorizontalSeparators().add(graphic);
//            } else if (canBeConsideredVerticalSeparator(graphic)) {
//                graphic.setCanBeAssigned(true);
//                graphic.setStyle(Style.GRAPHIC_VSEP);
//                ret.getVerticalSeparators().add(graphic);

                // } else if (canBeConsideredCharacterInRegion(graphic, region)) {
                // graphic.setStyle(Style.GRAPHIC_CHARACTER);
                // graphic.setCanBeAssigned(true);
                // ret.getContents().add(graphic);
            } else {
                graphic.setCanBeAssigned(true);
                graphic.setStyle(Style.GRAPHIC_IMAGE);
                ret.getContents().add(graphic);
            }

            ret.getGraphicsToRender().add(graphic);
        }
    }

    private boolean isTooBigGraphic(@NotNull final PhysicalContent graphic) {
        return graphic.getPos().area() >= (w * h);
    }

    private void logGraphics(@NotNull CategorizedGraphics ret) {

        for (GraphicContent g : ret.getContainers()) {
            log.info("LOG00502:considered container: " + g);
        }

        for (GraphicContent g : ret.getHorizontalSeparators()) {
            log.info("LOG00505:considered hsep: " + g);
        }

        for (GraphicContent g : ret.getVerticalSeparators()) {
            log.info("LOG00506:considered vsep: " + g);
        }

        for (GraphicContent g : ret.getContents()) {
            log.info("LOG00980:considered content: " + g);
        }
    }
}
