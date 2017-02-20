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



package org.elacin.pdfextract.physical.graphics;

import org.elacin.pdfextract.content.GraphicContent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA. User: elacin Date: 15.01.11 Time: 21.57 To change this template use
 * File | Settings | File Templates.
 */
public class CategorizedGraphics {

// ------------------------------ FIELDS ------------------------------

/* these graphics are considered content */
    @NotNull
    private final List<GraphicContent> contents = new ArrayList<GraphicContent>();

/* These will be used to split a page into page regions */
    @NotNull
    private final List<GraphicContent> containers = new ArrayList<GraphicContent>();

/* these are separators which might divide regions of a page */
    @NotNull
    private final List<GraphicContent> verticalSeparators   = new ArrayList<GraphicContent>();
    @NotNull
    private final List<GraphicContent> horizontalSeparators = new ArrayList<GraphicContent>();

/*     This contains all the segmented pictures (except those which has been dropped for being too
    big), and is only here for rendering purposes. */
    @NotNull
    private final List<GraphicContent> graphicsToRender = new ArrayList<GraphicContent>();

// --------------------- GETTER / SETTER METHODS ---------------------
    @NotNull
    public List<GraphicContent> getContainers() {
        return containers;
    }

    @NotNull
    public List<GraphicContent> getContents() {
        return contents;
    }

    @NotNull
    public List<GraphicContent> getGraphicsToRender() {
        return graphicsToRender;
    }

    @NotNull
    public List<GraphicContent> getHorizontalSeparators() {
        return horizontalSeparators;
    }

    @NotNull
    public List<GraphicContent> getVerticalSeparators() {
        return verticalSeparators;
    }
}
