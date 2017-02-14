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

import org.jetbrains.annotations.NotNull;

import java.util.Comparator;

/**
 * Created by IntelliJ IDEA. User: elacin Date: Apr 8, 2010 Time: 8:56:45 AM To change this template
 * use File | Settings | File Templates.
 */
public class ParagraphNode extends AbstractParentNode<LineNode, PageNode> {

// ------------------------------ FIELDS ------------------------------
    private final int seqNo;

// --------------------------- CONSTRUCTORS ---------------------------
    public ParagraphNode(final int seqNo) {
        this.seqNo = seqNo;
    }

    public ParagraphNode(@NotNull final LineNode child, final int seqNo) {

        super(child);
        this.seqNo = seqNo;
    }

// --------------------- GETTER / SETTER METHODS ---------------------
    public int getSeqNo() {
        return seqNo;
    }

// -------------------------- PUBLIC METHODS --------------------------
    @NotNull
    @Override
    public Comparator<LineNode> getChildComparator() {

        return new Comparator<LineNode>() {

            public int compare(@NotNull final LineNode o1, @NotNull final LineNode o2) {

                if (o1.getPos().y < o2.getPos().y) {
                    return -1;
                } else if (o1.getPos().y > o2.getPos().y) {
                    return 1;
                }

                return 0;
            }
        };
    }
}
