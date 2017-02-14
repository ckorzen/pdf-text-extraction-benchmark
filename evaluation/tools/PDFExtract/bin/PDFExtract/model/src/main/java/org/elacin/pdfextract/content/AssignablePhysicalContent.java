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
 * Created by IntelliJ IDEA. User: elacin Date: Nov 3, 2010 Time: 5:17:33 PM To change this template
 * use File | Settings | File Templates.
 */
public abstract class AssignablePhysicalContent extends PhysicalContent {

// ------------------------------ FIELDS ------------------------------
    public static final int BLOCK_NOT_ASSIGNED = -1;
    public int              blockNum           = BLOCK_NOT_ASSIGNED;
    @NotNull
    protected Style         style;

// --------------------------- CONSTRUCTORS ---------------------------
    public AssignablePhysicalContent(final Rectangle position, @NotNull Style style) {

        super(position);
        this.style = style;
    }

// ------------------------ OVERRIDING METHODS ------------------------
    @NotNull
    @Override
    public AssignablePhysicalContent getAssignable() {
        return this;
    }

    @Override
    public boolean isAssignable() {
        return true;
    }

// --------------------- GETTER / SETTER METHODS ---------------------
    public int getBlockNum() {
        return blockNum;
    }

    public void setBlockNum(final int blockNum) {
        this.blockNum = blockNum;
    }

    @NotNull
    public Style getStyle() {
        return style;
    }

    public void setStyle(@NotNull final Style style) {
        this.style = style;
    }

// -------------------------- PUBLIC METHODS --------------------------
    public boolean isAssignedBlock() {
        return blockNum != BLOCK_NOT_ASSIGNED;
    }
}
