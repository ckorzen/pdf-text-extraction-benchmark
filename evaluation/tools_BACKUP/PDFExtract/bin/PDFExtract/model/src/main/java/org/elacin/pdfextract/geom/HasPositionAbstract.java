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

import org.jetbrains.annotations.Nullable;

/**
 * Created by IntelliJ IDEA. User: elacin Date: Oct 20, 2010 Time: 3:51:40 PM To change this
 * template use File | Settings | File Templates.
 */
public abstract class HasPositionAbstract implements HasPosition {

// ------------------------------ FIELDS ------------------------------
    @Nullable
    private Rectangle pos;

// --------------------------- CONSTRUCTORS ---------------------------
    protected HasPositionAbstract() {}

    protected HasPositionAbstract(final Rectangle pos) {
        this.pos = pos;
    }

// ------------------------ INTERFACE METHODS ------------------------
// --------------------- Interface HasPosition ---------------------
    public final Rectangle getPos() {

        if (pos == null) {
            calculatePos();
        }

        return pos;
    }

    public final void invalidatePos() {
        pos = null;
    }

// --------------------- GETTER / SETTER METHODS ---------------------
    protected void setPos(@Nullable final Rectangle pos) {
        this.pos = pos;
    }
}
