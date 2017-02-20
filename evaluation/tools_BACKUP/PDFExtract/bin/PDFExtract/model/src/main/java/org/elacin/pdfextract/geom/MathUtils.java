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

import org.elacin.pdfextract.content.WhitespaceRectangle;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * Created by IntelliJ IDEA. User: elacin Date: May 7, 2010 Time: 5:54:30 AM To change this template
 * use File | Settings | File Templates.
 */
public final class MathUtils {

// --------------------------- CONSTRUCTORS ---------------------------
    private MathUtils() {}

// -------------------------- PUBLIC STATIC METHODS --------------------------
    @NotNull
    public static Rectangle findBounds(@NotNull final Collection<? extends HasPosition> contents) {
        return findBounds_(contents, true);
    }

    @NotNull
    public static Rectangle findBoundsExcludingWhitespace(
            @NotNull final Collection<? extends HasPosition> contents) {
        return findBounds_(contents, false);
    }

    private static Rectangle findBounds_(final Collection<? extends HasPosition> contents,
            final boolean countWhitespace) {

        /* calculate bounds for this region */
        float minX    = Float.MAX_VALUE,
              minY    = Float.MAX_VALUE;
        float maxX    = Float.MIN_VALUE,
              maxY    = Float.MIN_VALUE;
        int   counted = 0;

        for (HasPosition content : contents) {

            // TODO: this really doesnt belong here
            if (!countWhitespace && (content instanceof WhitespaceRectangle)) {
                continue;
            }

            minX = Math.min(minX, content.getPos().x);
            minY = Math.min(minY, content.getPos().y);
            maxX = Math.max(maxX, content.getPos().endX);
            maxY = Math.max(maxY, content.getPos().endY);
            counted++;
        }

        final Rectangle newPos;

        if (counted == 0) {
            newPos = Rectangle.EMPTY_RECTANGLE;
        } else {
            newPos = new Rectangle(minX, minY, maxX - minX, maxY - minY);
        }

        return newPos;
    }

    /**
     * Returns true if num2 is within percentage percent of num1
     */
    public static boolean isWithinPercent(final float num1, final float num2, final float percentage) {

        // noinspection FloatingPointEquality
        if (num1 == num2) {
            return true;
        }

        return (num1 + num1 / 100.0F * percentage) >= num2
               && (num1 - num1 / 100.0F * percentage) <= num2;
    }

    /**
     * Returns true if num2 is within num ? i
     */
    public static boolean isWithinVariance(final float num1, final float num2, final float variance) {

        // noinspection FloatingPointEquality
        if (num1 == num2) {
            return true;
        }

        return (num1 - variance) <= num2 && (num1 + variance) >= num2;
    }

    @SuppressWarnings({ "NumericCastThatLosesPrecision" })
    public static float log(float a) {
        return (float) StrictMath.log((double) a);
    }

    @SuppressWarnings({ "NumericCastThatLosesPrecision" })
    public static float sqrt(float a) {
        return (float) StrictMath.sqrt((double) a);
    }
}
