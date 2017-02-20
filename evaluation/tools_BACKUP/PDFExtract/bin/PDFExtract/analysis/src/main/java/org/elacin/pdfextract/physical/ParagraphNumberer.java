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



package org.elacin.pdfextract.physical;

/**
 * Created by IntelliJ IDEA. User: elacin Date: 19.01.11 Time: 00.46 To change this template use
 * File | Settings | File Templates.
 */
public final class ParagraphNumberer {

// ------------------------------ FIELDS ------------------------------
    private int page      = 0,
                region    = 0,
                paragraph = 0;

// --------------------------- CONSTRUCTORS ---------------------------
    public ParagraphNumberer(final int pageNumber) {
        setPage(pageNumber);
    }

// --------------------- GETTER / SETTER METHODS ---------------------
    public int getPage() {
        return page;
    }

// -------------------------- PUBLIC METHODS --------------------------
    public int getParagraphId(boolean graphic) {

        return (region * 1000 + paragraph) + (graphic
                ? 1000000
                : 0);
    }

    public void newPage() {

        page++;
        region    = 0;
        paragraph = 0;
    }

    public void newParagraph() {
        paragraph++;
    }

    public void newRegion() {

        region++;
        paragraph = 0;
    }

// -------------------------- OTHER METHODS --------------------------
    private void setPage(final int page) {

        this.page = page;
        region    = 0;
        paragraph = 0;
    }
}
