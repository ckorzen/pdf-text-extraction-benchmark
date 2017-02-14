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

/**
 * Created by IntelliJ IDEA. User: elacin Date: May 19, 2010 Time: 9:56:04 PM <p/> A mutable point
 */
public class FloatPoint {

// ------------------------------ FIELDS ------------------------------
    float x, y;

// --------------------------- CONSTRUCTORS ---------------------------
    public FloatPoint(final float x, final float y) {
        setPosition(x, y);
    }

// ------------------------ CANONICAL METHODS ------------------------
    @Override
    public String toString() {

        final StringBuilder sb = new StringBuilder();

        sb.append("Point");
        sb.append(", x=").append(x);
        sb.append(", y=").append(y);
        sb.append('}');

        return sb.toString();
    }

// --------------------- GETTER / SETTER METHODS ---------------------
    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

// -------------------------- PUBLIC METHODS --------------------------
    public final void setPosition(float x, float y) {

        this.x = x;
        this.y = y;
    }
}
