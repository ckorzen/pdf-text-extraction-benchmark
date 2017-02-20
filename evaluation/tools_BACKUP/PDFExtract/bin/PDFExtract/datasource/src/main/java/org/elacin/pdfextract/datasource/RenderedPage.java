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



package org.elacin.pdfextract.datasource;

import java.awt.image.BufferedImage;

/**
 * Created by IntelliJ IDEA. User: elacin Date: 16.01.11 Time: 20.47 To change this template use
 * File | Settings | File Templates.
 */
public class RenderedPage {

// ------------------------------ FIELDS ------------------------------
    final BufferedImage rendering;
    final float         xScale, yScale;

// --------------------------- CONSTRUCTORS ---------------------------
    public RenderedPage(BufferedImage rendering, float xScale, float yScale) {

        this.yScale    = yScale;
        this.rendering = rendering;
        this.xScale    = xScale;
    }

// --------------------- GETTER / SETTER METHODS ---------------------
    public BufferedImage getRendering() {
        return rendering;
    }

// -------------------------- PUBLIC METHODS --------------------------
    public float getXScale() {
        return xScale;
    }

    public float getYScale() {
        return yScale;
    }
}
