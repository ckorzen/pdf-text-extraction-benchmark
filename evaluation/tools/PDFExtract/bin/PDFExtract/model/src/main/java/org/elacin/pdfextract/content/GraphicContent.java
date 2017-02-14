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
import org.elacin.pdfextract.geom.Rectangle;
import org.elacin.pdfextract.style.Style;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

/**
 * Created by IntelliJ IDEA. User: elacin Date: Nov 3, 2010 Time: 4:43:12 PM To change this template
 * use File | Settings | File Templates.
 */
public class GraphicContent extends AssignablePhysicalContent {

// ------------------------------ FIELDS ------------------------------
    private static final Logger log = Logger.getLogger(GraphicContent.class);
    private boolean             canBeAssigned;
    private final Color         color;
    private final boolean       picture;

// --------------------------- CONSTRUCTORS ---------------------------
    public GraphicContent(final Rectangle position, boolean picture, Color color) {

        super(position, Style.GRAPHIC_IMAGE);
        this.picture = picture;
        this.color   = color;

        if (log.isDebugEnabled()) {
            log.debug("LOG00280:GraphicContent at " + position + ", picture =" + picture);
        }
    }

// ------------------------ CANONICAL METHODS ------------------------
    @Override
    public String toString() {

        final StringBuilder sb = new StringBuilder();

        sb.append("GraphicContent");
        sb.append("{canBeAssigned=").append(canBeAssigned);
        sb.append(", picture=").append(picture);
        sb.append(", pos=").append(getPos());
        sb.append(", style=").append(getStyle());
        sb.append('}');

        return sb.toString();
    }

// ------------------------ OVERRIDING METHODS ------------------------
    @NotNull
    @Override
    public GraphicContent getGraphicContent() {
        return this;
    }

    @Override
    public boolean isAssignable() {
        return canBeAssigned;
    }

    @Override
    public boolean isFigure() {
        return !picture;
    }

    @Override
    public boolean isGraphic() {
        return true;
    }

    @Override
    public boolean isGraphicButNotSeparator() {
        return !(isVerticalSeparator() || isHorizontalSeparator());
    }

    @Override
    public boolean isPicture() {
        return picture;
    }

// --------------------- GETTER / SETTER METHODS ---------------------
    public Color getColor() {
        return color;
    }

    public void setCanBeAssigned(final boolean canBeAssigned) {
        this.canBeAssigned = canBeAssigned;
    }

// -------------------------- PUBLIC METHODS --------------------------
    public boolean canBeCombinedWith(@NotNull final GraphicContent other) {

        if (this == other) {
            return false;
        }

        if (picture &&!other.picture) {
            return false;
        }

        return getPos().distance(other.getPos()) < 5.0f;
    }

    @NotNull
    public GraphicContent combineWith(@NotNull final GraphicContent other) {

        Color combinedColor;

        if (isBackgroundColor()) {
            combinedColor = other.color;
        } else {
            combinedColor = color;
        }

        return new GraphicContent(getPos().union(other.getPos()), picture, combinedColor);
    }

    public boolean isBackgroundColor() {
        return color.equals(Color.white);
    }

    /**
     * consider the graphic a separator if the aspect ratio is high
     */
    public boolean isHorizontalSeparator() {
        return getStyle().equals(Style.GRAPHIC_HSEP);
    }

    public boolean isMathBar() {
        return getStyle().equals(Style.GRAPHIC_MATH_BAR);
    }

    public boolean isSeparator() {
        return isVerticalSeparator() || isHorizontalSeparator();
    }

    /**
     * consider the graphic a separator if the aspect ratio is high
     */
    public boolean isVerticalSeparator() {
        return getStyle().equals(Style.GRAPHIC_VSEP);
    }
}
