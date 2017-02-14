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

import org.elacin.pdfextract.formula.Formulas;
import org.elacin.pdfextract.geom.MathUtils;
import org.elacin.pdfextract.style.Style;
import org.elacin.pdfextract.style.TextUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;

/**
 * Created by IntelliJ IDEA. User: elacin Date: Apr 8, 2010 Time: 8:29:43 AM To change this template
 * use File | Settings | File Templates.
 */
public class LineNode extends AbstractParentNode<WordNode, ParagraphNode> {

// --------------------------- CONSTRUCTORS ---------------------------
    public LineNode() {}

    public LineNode(@NotNull final WordNode child) {
        super(child);
    }

// ------------------------ OVERRIDING METHODS ------------------------
    @NotNull
    @Override
    public String getText() {

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < getChildren().size(); i++) {
            final WordNode word = getChildren().get(i);

            sb.append(word.getText());

            if (i != getChildren().size() - 1) {
                sb.append(" ");
            }
        }

        return sb.toString();
    }

// -------------------------- PUBLIC METHODS --------------------------
    @NotNull
    public Style findDominatingStyle() {

        if (Formulas.textSeemsToBeFormula(getChildren())) {
            return Style.FORMULA;
        }

        return TextUtils.findDominatingStyle(getChildren());
    }

    /**
     * Returns a Comparator which compares only X coordinates
     */
    @NotNull
    @Override
    public Comparator<WordNode> getChildComparator() {

        if (findDominatingStyle().equals(Style.FORMULA)) {
            return new Comparator<WordNode>() {

                public int compare(@NotNull final WordNode o1, @NotNull final WordNode o2) {

                    if (o1.getPos().x < o2.getPos().x) {
                        return -1;
                    } else if (o1.getPos().x > o2.getPos().x) {
                        return 1;
                    }

                    return 0;
                }
            };
        }

        return new Comparator<WordNode>() {

            public int compare(@NotNull final WordNode o1, @NotNull final WordNode o2) {

                if (o1.getPos().endY < o2.getPos().y) {
                    return -1;
                }

                if (o1.getPos().y > o2.getPos().endY) {
                    return 1;
                }

                if (o1.getPos().endX < o2.getPos().x) {
                    return -1;
                }

                if (o1.getPos().x > o2.getPos().endX) {
                    return 1;
                }

                if (!MathUtils.isWithinPercent(o1.getPos().y, o2.getPos().y, 4)) {
                    return Float.compare(o1.getPos().y, o2.getPos().y);
                }

                return Float.compare(o1.getPos().x, o2.getPos().x);
            }
        };
    }

// -------------------------- OTHER METHODS --------------------------
    public boolean isIndented() {

        if (getParent() == null) {
            return false;
        }

        final float paragraphX = getParent().getPos().x;

        return getPos().x > paragraphX + 5.0f;    // (float) findDominatingStyle().xSize * 2.0f;
    }
}
