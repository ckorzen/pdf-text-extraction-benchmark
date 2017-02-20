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



package org.elacin.pdfextract.content;

import org.apache.log4j.Logger;
import org.elacin.pdfextract.Constants;
import org.elacin.pdfextract.geom.HasPosition;
import org.elacin.pdfextract.geom.Rectangle;
import org.elacin.pdfextract.geom.RectangleCollection;
import org.elacin.pdfextract.style.Style;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by IntelliJ IDEA. User: elacin Date: Nov 8, 2010 Time: 7:44:41 PM To change this template
 * use File | Settings | File Templates.
 */
public class PhysicalPageRegion extends RectangleCollection {

// ------------------------------ FIELDS ------------------------------
    private static final Logger log = Logger.getLogger(PhysicalPageRegion.class);

    /* content */
    @NotNull
    private final List<PhysicalPageRegion>  subregions = new ArrayList<PhysicalPageRegion>();
    @NotNull
    private final List<WhitespaceRectangle> whitespace = new ArrayList<WhitespaceRectangle>();

    /* the physical page containing this region */
    @NotNull
    private final PhysicalPage page;

    /* the graphics containing this region */
    @Nullable
    private GraphicContent containingGraphic;

    /* average font sizes for this page region */
    private transient float   _avgFontSizeX;
    private transient float   _avgFontSizeY;
    private transient int     _medianOfVerticalDistances;
    private transient boolean fontInfoFound;
    private transient boolean medianFound;

// --------------------------- CONSTRUCTORS ---------------------------
    public PhysicalPageRegion(@NotNull final List<? extends PhysicalContent> contents,
                              @NotNull PhysicalPage page) {
        this(contents, null, page);
    }

    public PhysicalPageRegion(@NotNull final Collection<? extends PhysicalContent> contents,
                              @Nullable final PhysicalPageRegion parent, @NotNull PhysicalPage page) {

        super(contents, parent);
        this.page = page;
    }

// ------------------------ INTERFACE METHODS ------------------------
// --------------------- Interface HasPosition ---------------------
    @Override
    public void calculatePos() {

        if (Constants.WHITESPACE_USE_WHOLE_PAGE && (page.getMainRegion() == this)) {
            setPos(page.getPageDimensions());
        }

        super.calculatePos();

        if (isGraphicalRegion()) {
            setPos(getPos().union(containingGraphic.getPos()));
        }
    }

// ------------------------ OVERRIDING METHODS ------------------------
    @Override
    public void clearCache() {

        super.clearCache();
        fontInfoFound = false;
        medianFound   = false;
    }

// --------------------- GETTER / SETTER METHODS ---------------------
    @Nullable
    public GraphicContent getContainingGraphic() {
        return containingGraphic;
    }

    @NotNull
    public PhysicalPage getPage() {
        return page;
    }

    @NotNull
    public List<PhysicalPageRegion> getSubregions() {
        return subregions;
    }

    @NotNull
    public List<WhitespaceRectangle> getWhitespace() {
        return whitespace;
    }

// -------------------------- PUBLIC METHODS --------------------------
    public void addSubRegion(final PhysicalPageRegion newSub) {

        addContent(newSub);
        subregions.add(newSub);
    }

    public void addWhitespace(final Collection<WhitespaceRectangle> whitespace) {

        this.whitespace.addAll(whitespace);
        addContents(whitespace);
    }

    public void ensureAllContentInLeafNodes() {

        if (!subregions.isEmpty()) {
            Collection<PhysicalContent> contents = new ArrayList<PhysicalContent>();

            for (PhysicalContent content : getContents()) {
                if (!(content instanceof PhysicalPageRegion)) {
                    contents.add(content);
                }
            }

            doExtractSubRegion(contents, null, null, true);
        }

        for (int i = 0, subregionsSize = subregions.size(); i < subregionsSize; i++) {
            subregions.get(i).ensureAllContentInLeafNodes();
        }
    }

    public boolean extractSubRegionFromBound(@NotNull Rectangle bound, boolean addToParent) {

        final List<PhysicalContent> subContents = findContentsIntersectingWith(bound.getPos());

        return doExtractSubRegion(subContents, bound, null, addToParent);
    }

    /**
     * Returns a subregion with all the contents which is contained by bound. If more than two
     * pieces of content crosses the boundary of bound, it is deemed inappropriate for dividing the
     * page, and an exception is thrown
     *
     * @return the new region
     */
    public void extractSubRegionFromGraphic(@NotNull final GraphicContent graphic, boolean addToParent) {

        /* we can allow us to search a bit outside the graphic */
        final Rectangle pos       = graphic.getPos();
        final float     extra     = 2.0f;
        final Rectangle searchPos = new Rectangle(pos.x - extra, pos.y - extra, pos.width + 2 * extra,
                                        pos.height + 2 * extra);
        final List<PhysicalContent> subContents = findContentsIntersectingWith(searchPos);

        doExtractSubRegion(subContents, graphic, graphic, addToParent);
    }

    public float getAvgFontSizeX() {

        if (!fontInfoFound) {
            findAndSetFontInformation();
        }

        return _avgFontSizeX;
    }

    public float getAvgFontSizeY() {

        if (!fontInfoFound) {
            findAndSetFontInformation();
        }

        return _avgFontSizeY;
    }

    public int getMedianOfVerticalDistances() {

        if (!medianFound) {
            findAndSetMedianOfVerticalDistancesForRegion();
        }

        return _medianOfVerticalDistances;
    }

    public float getMinimumColumnSpacing() {
        return getAvgFontSizeX() * 0.8f;
    }

    public float getMinimumRowSpacing() {

        if (!fontInfoFound) {
            findAndSetFontInformation();
        }

        return (float) getMedianOfVerticalDistances() * 1.1f;
    }

    public boolean isGraphicalRegion() {
        return containingGraphic != null;
    }

    public void removeSubRegion(final PhysicalPageRegion newSub) {

        removeContent(newSub);
        subregions.remove(newSub);
    }

    public void setContainingGraphic(@Nullable GraphicContent containingGraphic) {

        if (this.containingGraphic != null) {
            removeContent(this.containingGraphic);
        }

        if (containingGraphic != null) {
            this.containingGraphic = containingGraphic;

            // addContent(containingGraphic);
        }
    }

// -------------------------- OTHER METHODS --------------------------
    private boolean doExtractSubRegion(@NotNull final Collection<PhysicalContent> subContents,
                                       @Nullable final HasPosition bound,
                                       @Nullable final GraphicContent graphic, boolean addToParent) {

        if (subContents.isEmpty()) {
            if (log.isInfoEnabled()) {
                log.info("LOG00960:bound " + bound + " contains no content in " + this + ". wont "
                         + "extract");
            }

            return false;
        }

        if (subContents.size() == getContents().size()) {
            if (log.isInfoEnabled()) {
                log.info("LOG00950:bound " + bound + " contains all content in " + this + ". wont "
                         + "extract");
            }

            return false;
        }

        /**
         * whitespace rectangles might be important for layout both in this region and in the one
         * which will be extracted, so leave them in both
         */
        boolean                   onlyWhitespace = true;
        List<WhitespaceRectangle> saveWhitespace = new ArrayList<WhitespaceRectangle>();

        for (PhysicalContent subContent : subContents) {
            if (subContent instanceof WhitespaceRectangle) {
                saveWhitespace.add((WhitespaceRectangle) subContent);
            } else {
                onlyWhitespace = false;
            }
        }

        /* dont bother to create a region for only whitespace */
        if (onlyWhitespace) {
            if (log.isInfoEnabled()) {
                log.info("LOG01330:Tried to extract only whitespace. ignoring");
            }

            return false;
        }

        final PhysicalPageRegion newRegion = new PhysicalPageRegion(subContents, this, page);

        if (graphic == null) {
            newRegion.setContainingGraphic(containingGraphic);
        } else {
            newRegion.setContainingGraphic(graphic);
        }

        /* move this regions subregions if they are contained by the new region */
        List<PhysicalPageRegion> toMove = new ArrayList<PhysicalPageRegion>();

        for (PhysicalContent subContent : subContents) {
            if (subContent instanceof PhysicalPageRegion) {
                toMove.add((PhysicalPageRegion) subContent);
            }
        }

        for (PhysicalPageRegion subRegionToMove : toMove) {
            subregions.remove(subRegionToMove);
            newRegion.subregions.add(subRegionToMove);
        }

        log.info("LOG00890:Extracted PPR:" + newRegion + " from " + this);
        removeContents(subContents);
        addContents(saveWhitespace);

        if (addToParent && (getParent() != null) && (getParent() instanceof PhysicalPageRegion)) {
            ((PhysicalPageRegion) getParent()).addSubRegion(newRegion);
        } else {
            addSubRegion(newRegion);
        }

        return true;
    }

/* find average font sizes, and most used style for the region */
    protected void findAndSetFontInformation() {

        float xFontSizeSum  = 0.0f,
              yFontSizeSum  = 0.0f;
        int   numCharsFound = 0;

        for (PhysicalContent content : getContents()) {
            if (content.isText()) {
                final Style style  = content.getPhysicalText().getStyle();
                final int   length = content.getPhysicalText().getText().length();

                xFontSizeSum  += (float) (style.xSize * length);
                yFontSizeSum  += (float) (style.ySize * length);
                numCharsFound += length;
            }
        }

        if (numCharsFound == 0) {
            _avgFontSizeX = Float.MIN_VALUE;
            _avgFontSizeY = Float.MIN_VALUE;
        } else {
            _avgFontSizeX = xFontSizeSum / (float) numCharsFound;
            _avgFontSizeY = yFontSizeSum / (float) numCharsFound;
        }

        fontInfoFound = true;
    }

    /**
     * Finds an approximation of the normal vertical line spacing for the region. <p/> This is done
     * by looking at three vertical rays, calculating distances between all the lines intersecting
     * those lines, and then returning the median of all those distances. <p/> minimum value is 2,
     * max is (avgFontSize * 3) -1. -1 if nothing is found;
     */
    protected int findAndSetMedianOfVerticalDistancesForRegion() {

        final int LIMIT         = (int) getAvgFontSizeY() * 3;
        int[]     distanceCount = new int[LIMIT];

        for (float x = getPos().x; x <= getPos().endX; x += getPos().width / 3) {
            final List<PhysicalContent> column = findContentAtXIndex(x);

            for (int i = 1; i < column.size(); i++) {
                final PhysicalContent current = column.get(i - 1);
                final PhysicalContent below   = column.get(i);

                /* increase count for this distance (rounded down to an int) */
                final int d = (int) (below.getPos().y - current.getPos().endY);

                if ((d > 0) && (d < LIMIT)) {
                    distanceCount[d]++;
                }
            }
        }

        int highestFrequency = -1;
        int index            = -1;

        for (int i = 2; i < distanceCount.length; i++) {
            if (distanceCount[i] >= highestFrequency) {
                index            = i;
                highestFrequency = distanceCount[i];
            }
        }

        float temp = (float) Math.max(index, (int) (getAvgFontSizeY() * 0.5f));

        _medianOfVerticalDistances = (int) (temp + Math.max(1.0f, temp * 0.1f));
        medianFound                = true;

        return _medianOfVerticalDistances;
    }
}
