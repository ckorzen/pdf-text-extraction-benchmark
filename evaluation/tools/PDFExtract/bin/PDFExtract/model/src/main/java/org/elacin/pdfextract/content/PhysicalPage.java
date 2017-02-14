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

import org.apache.log4j.Logger;
import org.elacin.pdfextract.geom.Rectangle;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PhysicalPage {

// ------------------------------ FIELDS ------------------------------
    private static final Logger log = Logger.getLogger(PhysicalPage.class);

    /**
     * Contains all the graphics on the page
     */
    @NotNull
    private final List<GraphicContent> allGraphics;

    /**
     * This initially contains everything on the page. after creating the regions, content will be
     * moved from here. ideally this should be quite empty after the analysis.
     */
    @NotNull
    private final PhysicalPageRegion mainRegion;
    private final Rectangle          pageDimensions;

    /**
     * The physical page number (ie the sequence encountered in the document)
     */
    private final int pageNumber;

// --------------------------- CONSTRUCTORS ---------------------------
    public PhysicalPage(@NotNull List<? extends PhysicalContent> contents,
                        @NotNull final List<GraphicContent> graphics, int pageNumber,
                        final Rectangle pageDimensions) {

        this.pageNumber     = pageNumber;
        allGraphics         = graphics;
        this.pageDimensions = pageDimensions;
        mainRegion          = new PhysicalPageRegion(contents, this);
    }

// --------------------- GETTER / SETTER METHODS ---------------------
    @NotNull
    public List<GraphicContent> getAllGraphics() {
        return allGraphics;
    }

    @NotNull
    public PhysicalPageRegion getMainRegion() {
        return mainRegion;
    }

    public Rectangle getPageDimensions() {
        return pageDimensions;
    }

    public int getPageNumber() {
        return pageNumber;
    }
}
