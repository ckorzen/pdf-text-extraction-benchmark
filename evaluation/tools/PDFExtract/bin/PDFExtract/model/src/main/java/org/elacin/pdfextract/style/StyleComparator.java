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



package org.elacin.pdfextract.style;

import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA. User: elacin Date: 29.11.10 Time: 03.39 To change this template use
 * File | Settings | File Templates.
 */
public class StyleComparator {

// ------------------------------ FIELDS ------------------------------
    private static final int SUBTLE_SIZE_DIFFERENCE = 1;

// -------------------------- PUBLIC STATIC METHODS --------------------------
    @NotNull
    public static StyleDifference styleCompare(@NotNull final Style one, @NotNull final Style two) {

        // noinspection ObjectEquality
        if ((one == Style.FORMULA) != (two == Style.FORMULA)) {
            return StyleDifference.SPLIT;
        }

        if (one.mathFont != two.mathFont) {
            return StyleDifference.BIG_DIFFERENCE;
        }

        if (one.mathFont && two.mathFont) {
            return StyleDifference.SAME_STYLE;
        }

        if (one.bold != two.bold) {
            return StyleDifference.BIG_DIFFERENCE;
        }

        if (!one.fontName.equals(two.fontName)) {
            return StyleDifference.BIG_DIFFERENCE;
        }

        if (!one.subType.equals(two.subType)) {
            return StyleDifference.SUBTLE_DIFFERENCE;
        }

        final int xDiff = Math.abs(one.ySize - two.ySize);
        final int yDiff = Math.abs(one.ySize - two.ySize);

        if ((xDiff == SUBTLE_SIZE_DIFFERENCE) || (yDiff == SUBTLE_SIZE_DIFFERENCE)) {
            return StyleDifference.SUBTLE_DIFFERENCE;
        }

        if ((xDiff > SUBTLE_SIZE_DIFFERENCE) || (yDiff > SUBTLE_SIZE_DIFFERENCE)) {
            return StyleDifference.BIG_DIFFERENCE;
        }

        if (one.ySize > 13.0f) {
            return StyleDifference.SAME_STYLE_AND_BIG_TEXT;
        }

        return StyleDifference.SAME_STYLE;
    }
}
