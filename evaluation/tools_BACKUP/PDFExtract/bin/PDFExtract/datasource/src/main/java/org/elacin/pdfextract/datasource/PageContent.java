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

import org.elacin.pdfextract.content.GraphicContent;
import org.elacin.pdfextract.content.PhysicalText;
import org.elacin.pdfextract.geom.Rectangle;

import java.io.Serializable;
import java.util.List;

/**
 * Created by IntelliJ IDEA. User: elacin Date: 15.01.11 Time: 19.47 To change this template use
 * File | Settings | File Templates.
 */
public class PageContent implements Serializable {

// ------------------------------ FIELDS ------------------------------
    final List<PhysicalText>   characters;
    final Rectangle            dimensions;
    final List<GraphicContent> graphics;
    final int                  pageNum;

// --------------------------- CONSTRUCTORS ---------------------------
    public PageContent(List<PhysicalText> characters, List<GraphicContent> graphics, int pageNum,
                       Rectangle dimensions) {

        this.characters = characters;
        this.graphics   = graphics;
        this.pageNum    = pageNum;
        this.dimensions = dimensions;
    }

// --------------------- GETTER / SETTER METHODS ---------------------
    public List<PhysicalText> getCharacters() {
        return characters;
    }

    public Rectangle getDimensions() {
        return dimensions;
    }

    public List<GraphicContent> getGraphics() {
        return graphics;
    }

    public int getPageNum() {
        return pageNum;
    }
}
