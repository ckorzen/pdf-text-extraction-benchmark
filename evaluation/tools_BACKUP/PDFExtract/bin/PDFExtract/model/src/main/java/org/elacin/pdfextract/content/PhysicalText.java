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

import org.elacin.pdfextract.geom.Rectangle;
import org.elacin.pdfextract.style.Style;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA. User: elacin Date: Sep 23, 2010 Time: 2:36:44 PM To change this
 * template use File | Settings | File Templates.
 */
public class PhysicalText extends AssignablePhysicalContent implements StyledText {

    private final float baseLine;

// ------------------------------ FIELDS ------------------------------
    public float        charSpacing;
    public final String text;

    public PhysicalText(final String text, final Style style, final Rectangle position, float baseLine) {

        super(position, style);
        this.text     = text;
        this.baseLine = baseLine;
    }

// --------------------------- CONSTRUCTORS ---------------------------
    public PhysicalText(final String text, final Style style, final float x, final float y,
                        final float width, final float height, float baseLine) {
        this(text, style, new Rectangle(x, y, width, height), baseLine);
    }

// ------------------------ INTERFACE METHODS ------------------------
// --------------------- Interface StyledText ---------------------
    public String getText() {
        return text;
    }

// ------------------------ CANONICAL METHODS ------------------------
    @Override
    public String toString() {

        final StringBuilder sb = new StringBuilder();

        sb.append("Text");
        sb.append("{'").append(text).append('\'');
        sb.append(", style=").append(style);
        sb.append(", pos=").append(getPos());
        sb.append(", charSpacing=").append(charSpacing);
        sb.append('}');

        return sb.toString();
    }

// ------------------------ OVERRIDING METHODS ------------------------
    @NotNull
    @Override
    public PhysicalText getPhysicalText() {
        return this;
    }

    @Override
    public boolean isText() {
        return true;
    }

// --------------------- GETTER / SETTER METHODS ---------------------
    public float getBaseLine() {
        return baseLine;
    }

// -------------------------- PUBLIC METHODS --------------------------
    @NotNull
    public PhysicalText combineWith(@NotNull final PhysicalText next) {
        return new PhysicalText(text + next.text, style, getPos().union(next.getPos()), baseLine);
    }

    public float getAverageCharacterWidth() {
        return getPos().width / (float) text.length();
    }

    public boolean isSameStyleAs(@NotNull final PhysicalText next) {
        return getStyle().equals(next.getStyle());
    }
}
