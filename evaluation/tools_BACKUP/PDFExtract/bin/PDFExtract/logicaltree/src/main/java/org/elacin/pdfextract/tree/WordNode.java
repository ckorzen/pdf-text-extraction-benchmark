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



package org.elacin.pdfextract.tree;

import org.elacin.pdfextract.content.StyledText;
import org.elacin.pdfextract.geom.Rectangle;
import org.elacin.pdfextract.style.Style;

import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: elacin
 * Date: Mar 18, 2010
 * Time: 2:31:58 PM
 */

/**
 * This class represents one word read from the PDF, and all the information about that word,
 * including style, position, distance to former word (if this is not the first word on a line, and
 * the textual representation. <p/> Note that all whitespace characters will have been stripped from
 * the text.
 */
public class WordNode extends AbstractNode<LineNode> {

    private final float   charSpacing;
    protected final Style style;

// ------------------------------ FIELDS ------------------------------
    public final String text;

// --------------------------- CONSTRUCTORS ---------------------------
    public WordNode(final Rectangle position, final Style style, final String text,
                    final float charSpacing) {

        setPos(position);
        this.style       = style;
        this.text        = text;
        this.charSpacing = charSpacing;
    }

// ------------------------ INTERFACE METHODS ------------------------
// --------------------- Interface HasPosition ---------------------
    public void calculatePos() {
        assert false;
    }

// --------------------- Interface StyledText ---------------------
    @Override
    @NotNull
    public String getText() {
        return text;
    }

    @Override
    @NotNull
    public Style getStyle() {
        return style;
    }

// ------------------------ CANONICAL METHODS ------------------------
    @NotNull
    @Override
    public String toString() {
        return "WordNode{text='" + text + '\'' + ", position=" + getPos() + ", style=" + style;
    }

// --------------------- GETTER / SETTER METHODS ---------------------
// -------------------------- PUBLIC METHODS --------------------------
    public boolean isPartOfSameWordAs(@NotNull final WordNode nextNode) {

        float distance = nextNode.getPos().x - getPos().endX;

        return distance <= charSpacing;    // * 1.01f;
    }

// -------------------------- OTHER METHODS --------------------------
    protected void invalidateThisAndParents() {

        if (getParent() != null) {
            getParent().invalidateThisAndParents();
        }
    }
}
