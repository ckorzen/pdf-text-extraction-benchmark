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



package org.elacin.pdfextract.geom;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by IntelliJ IDEA. User: elacin Date: May 19, 2010 Time: 9:43:07 PM <p/> A non-mutable
 * rectangle, with union and intercepts bits stolen from javas Rectangle2D. The problem with just
 * using that class was that is isnt available in an integer version.
 */
public final class Rectangle extends HasPositionAbstract {

// ------------------------------ FIELDS ------------------------------
    public static final Rectangle EMPTY_RECTANGLE = new Rectangle(0.1f, 0.1f, 0.1f, 0.1f);
    private transient int         hash            = -1;

/* caching, we do a lot of comparing */
    private transient boolean hasCalculatedHash;
    public final float        x, y, width, height, endX, endY;

// --------------------------- CONSTRUCTORS ---------------------------
    public Rectangle(final float x, final float y, final float width, final float height) {

        this.height = height;
        this.width  = width;
        this.x      = x;
        this.y      = y;
        endX        = x + width;
        endY        = y + height;

        if (height <= 0.0f) {
            throw new IllegalArgumentException("height must be positive: " + this);
        }

        if (width <= 0.0f) {
            throw new IllegalArgumentException("width must be positive " + this);
        }

        setPos(this);
    }

// ------------------------ INTERFACE METHODS ------------------------
// --------------------- Interface HasPosition ---------------------
    public void calculatePos() {
        assert false;
    }

// ------------------------ CANONICAL METHODS ------------------------
    @SuppressWarnings({ "ALL" })

/* generated */
    @Override
    public boolean equals(@Nullable final Object o) {

        if (this == o) {
            return true;
        }

        if ((o == null) || (getClass() != o.getClass())) {
            return false;
        }

        final Rectangle rectangle = (Rectangle) o;

        if (Float.compare(rectangle.height, height) != 0) {
            return false;
        }

        if (Float.compare(rectangle.width, width) != 0) {
            return false;
        }

        if (Float.compare(rectangle.x, x) != 0) {
            return false;
        }

        if (Float.compare(rectangle.y, y) != 0) {
            return false;
        }

        return true;
    }

    @SuppressWarnings({ "ALL" })

/* generated */
    @Override
    public int hashCode() {

        if (!hasCalculatedHash) {
            int result = ((x != +0.0f)
                          ? Float.floatToIntBits(x)
                          : 0);

            result            = 31 * result + ((y != +0.0f)
                                               ? Float.floatToIntBits(y)
                                               : 0);
            result            = 31 * result + ((width != +0.0f)
                                               ? Float.floatToIntBits(width)
                                               : 0);
            result            = 31 * result + ((height != +0.0f)
                                               ? Float.floatToIntBits(height)
                                               : 0);
            hash              = result;
            hasCalculatedHash = true;
        }

        return hash;
    }

    @Override
    public String toString() {

        final StringBuilder sb = new StringBuilder();

        sb.append("pos{");
        sb.append(" x=").append(x);
        sb.append(", y=").append(y);
        sb.append(", w=").append(width);
        sb.append(", h=").append(height);
        sb.append(", endX=").append(x + width);
        sb.append(", endY=").append(y + height);
        sb.append('}');

        return sb.toString();
    }

// -------------------------- PUBLIC METHODS --------------------------

    /**
     * Compute the area of this rectangle.
     *
     * @return The area of this rectangle
     */
    public float area() {
        return width * height;
    }

    /**
     * Determines the centre point of the rectangle
     */
    @NotNull
    public FloatPoint centre() {
        return new FloatPoint((x + (width / 2.0F)), (y + (height / 2.0F)));
    }

    /**
     * Determine whether this rectangle is contained by the passed rectangle
     *
     * @param r The rectangle that might contain this rectangle
     * @return true if the passed rectangle contains this rectangle, false if it does not
     */
    public boolean containedBy(@NotNull Rectangle r) {
        return (r.endX >= endX) && (r.x <= x) && (r.endY >= endY) && (r.y <= y);
    }

    /**
     * Determine whether this rectangle contains the passed rectangle
     *
     * @param r The rectangle that might be contained by this rectangle
     * @return true if this rectangle contains the passed rectangle, false if it does not
     */
    public boolean contains(@NotNull Rectangle r) {
        return (endX >= r.endX) && (x <= r.x) && (endY >= r.endY) && (y <= r.y);
    }

    /**
     * Return the distance between this rectangle and the passed point. If the rectangle contains
     * the point, the distance is zero.
     *
     * @param p Point to find the distance to
     * @return distance beween this rectangle and the passed point.
     */
    public float distance(@NotNull final FloatPoint p) {

        float temp = x - p.x;

        if (temp < 0.0F) {
            temp = p.x - endX;
        }

        float distanceSquared = Math.max(0.0F, temp * temp);
        float temp2           = (y - p.y);

        if (temp2 < 0.0F) {
            temp2 = p.y - endY;
        }

        if (temp2 > 0.0F) {
            distanceSquared += (temp2 * temp2);
        }

        return MathUtils.sqrt(distanceSquared);
    }

    /**
     * Distance to another rectangle
     *
     * @param that another rectangle
     * @return the distance
     */
    public float distance(@NotNull Rectangle that) {

        if (intersectsWith(that)) {
            return 0.0f;
        }

        float distance = 0.0f;

        if (x > that.endX) {
            distance += (x - that.endX) * (x - that.endX);
        } else if (that.x > endX) {
            distance += (that.x - endX) * (that.x - endX);
        }

        if (y > that.endY) {
            distance += (y - that.endY) * (y - that.endY);
        } else if (that.y > endY) {
            distance += (that.y - endY) * (that.y - endY);
        }

        return MathUtils.sqrt(distance);
    }

    @NotNull
    public Rectangle getAdjustedBy(float adjust) {

        return new Rectangle(Math.max(0.1f, x - adjust), Math.max(0.1f, y - adjust),
                             Math.max(0.1f, width + 2 * adjust), Math.max(0.1f, height + 2 * adjust));
    }

    public float getMiddleX() {
        return x + width / 2.0f;
    }

    public float getMiddleY() {
        return y + height / 2.0f;
    }

    public float getVerticalDistanceTo(@NotNull Rectangle that) {

        if (that.endY < y) {
            return y - that.endY;
        }

        if (that.y > endY) {
            return endY - that.y;
        }

        return 0.0f;
    }

    @NotNull
    public Rectangle intersection(@NotNull Rectangle that) {

        float maxX = Math.max(endX, that.endX);
        float maxY = Math.max(endY, that.endY);
        float minX = Math.min(x, that.x);
        float minY = Math.min(y, that.y);

        return new Rectangle(minX, minY, maxX - minX, maxY - minY);
    }

    public boolean intersectsAdmittingOverlap(@NotNull Rectangle that, final float overlap) {

        if (isEmpty()) {
            return false;
        }

        if (that.endX < x + overlap) {
            return false;
        }

        if (that.x > endX - overlap) {
            return false;
        }

        if (that.y > endY - overlap) {
            return false;
        }

        return that.endY > y + overlap;
    }

    public boolean intersectsWith(@NotNull Rectangle that) {

        if (isEmpty()) {
            return false;
        }

        if (that.endX < x) {
            return false;
        }

        if (that.x > endX) {
            return false;
        }

        if (that.y > endY) {
            return false;
        }

        return that.endY > y;
    }

    /**
     * Determines if this rectangle has an area of 0
     */
    public final boolean isEmpty() {
        return (width <= 0.0F) || (height <= 0.0F);
    }

    /**
     * I stole this code from java.awt.geom.Rectange2D, im sure the details make sense :)
     */
    @NotNull
    public Rectangle union(@NotNull Rectangle that) {

        float x1 = Math.min(x, that.x);
        float y1 = Math.min(y, that.y);
        float x2 = Math.max(endX, that.endX);
        float y2 = Math.max(endY, that.endY);

        if (x2 < x1) {
            float t = x1;

            x1 = x2;
            x2 = t;
        }

        if (y2 < y1) {
            float t = y1;

            y1 = y2;
            y2 = t;
        }

        return new Rectangle(x1, y1, x2 - x1, y2 - y1);
    }
}
