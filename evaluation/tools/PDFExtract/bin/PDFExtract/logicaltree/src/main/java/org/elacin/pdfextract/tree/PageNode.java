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

import org.elacin.pdfextract.content.PhysicalPage;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Created by IntelliJ IDEA. User: elacin Date: Mar 23, 2010 Time: 9:33:52 PM To change this
 * template use File | Settings | File Templates.
 */
public class PageNode extends AbstractParentNode<ParagraphNode, DocumentNode> {

// ------------------------------ FIELDS ------------------------------
    private final List<GraphicsNode> graphics = new ArrayList<GraphicsNode>();
    private final int                pageNumber;
    private PhysicalPage             physicalPage;

// --------------------------- CONSTRUCTORS ---------------------------
    public PageNode(int pageNumber) {
        this.pageNumber = pageNumber;
    }

// --------------------- GETTER / SETTER METHODS ---------------------
    public List<GraphicsNode> getGraphics() {
        return graphics;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public PhysicalPage getPhysicalPage() {
        return physicalPage;
    }

    public void setPhysicalPage(final PhysicalPage physicalPage) {
        this.physicalPage = physicalPage;
    }

// -------------------------- PUBLIC METHODS --------------------------
    public void addGraphics(GraphicsNode graphicsNode) {
        graphics.add(graphicsNode);
    }

    /**
     * Returns a Comparator which compares coordinates within a page
     */
    @NotNull
    @Override
    public Comparator<ParagraphNode> getChildComparator() {

        return new Comparator<ParagraphNode>() {

            public int compare(final ParagraphNode o1, final ParagraphNode o2) {

                // final int thisRegion = o1.getSeqNo() / 1000;
                // final int thatRegion = o2.getSeqNo() / 1000;
                //
                // if (thisRegion < thatRegion) {
                // return -1;
                // } else if (thisRegion > thatRegion) {
                // return 1;
                // }
                //
                // return (o1.getPos().getY() < o2.getPos().getY() ? -1 :
                // (o1.getSeqNo() == o2.getSeqNo() ? 0 : 1));
                return ((o1.getSeqNo() < o2.getSeqNo())
                        ? -1
                        : ((o1.getSeqNo() == o2.getSeqNo())
                           ? 0
                           : 1));
            }
        };
    }
}
