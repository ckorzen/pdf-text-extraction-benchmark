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

/**
 * Created by IntelliJ IDEA. User: elacin Date: Nov 2, 2010 Time: 1:33:50 AM To change this template
 * use File | Settings | File Templates.
 */
public class WhitespaceRectangle extends PhysicalContent {

// ------------------------------ FIELDS ------------------------------
    public int score;

// --------------------------- CONSTRUCTORS ---------------------------
    public WhitespaceRectangle(final Rectangle bound) {
        super(bound);
    }

// ------------------------ OVERRIDING METHODS ------------------------
    @Override
    public boolean isWhitespace() {
        return true;
    }

// --------------------- GETTER / SETTER METHODS ---------------------
    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
}
