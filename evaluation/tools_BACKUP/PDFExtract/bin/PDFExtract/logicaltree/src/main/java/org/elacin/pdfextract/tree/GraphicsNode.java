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

import org.elacin.pdfextract.geom.Rectangle;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;

/**
 * Created by IntelliJ IDEA. User: elacin Date: 22.01.11 Time: 00.46 To change this template use
 * File | Settings | File Templates.
 */
public class GraphicsNode extends AbstractParentNode<ParagraphNode, PageNode> {

// ------------------------------ FIELDS ------------------------------
    @NotNull
    private Rectangle graphicsPos;

// --------------------------- CONSTRUCTORS ---------------------------
    public GraphicsNode(final Rectangle graphicsPos) {

        setPos(graphicsPos);
        this.graphicsPos = graphicsPos;
    }

// ------------------------ INTERFACE METHODS ------------------------
// --------------------- Interface HasPosition ---------------------
    @Override
    public void calculatePos() {

        super.calculatePos();
        setPos(getPos().union(graphicsPos));
    }

// --------------------- GETTER / SETTER METHODS ---------------------
    public Rectangle getGraphicsPos() {
        return graphicsPos;
    }

    public void setGraphicsPos(final Rectangle graphicsPos) {
        this.graphicsPos = graphicsPos;
    }

// -------------------------- PUBLIC METHODS --------------------------
    @NotNull
    @Override
    public Comparator<ParagraphNode> getChildComparator() {

        return new Comparator<ParagraphNode>() {

            public int compare(final ParagraphNode o1, final ParagraphNode o2) {

                final int thisRegion = o1.getSeqNo() / 1000;
                final int thatRegion = o2.getSeqNo() / 1000;

                if (thisRegion < thatRegion) {
                    return -1;
                } else if (thisRegion > thatRegion) {
                    return 1;
                }

                if (o1.getSeqNo() == o2.getSeqNo()) {
                    if (o1.getPos().y < o2.getPos().y) {
                        return -1;
                    } else {
                        return 0;
                    }
                } else {
                    if (o1.getPos().y < o2.getPos().y) {
                        return -1;
                    } else {
                        return 1;
                    }
                }
            }
        };
    }
}
