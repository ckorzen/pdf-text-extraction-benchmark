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

import org.elacin.pdfextract.style.Style;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Created by IntelliJ IDEA. User: elacin Date: Mar 24, 2010 Time: 12:17:02 AM To change this
 * template use File | Settings | File Templates.
 */
public class DocumentNode extends AbstractParentNode<PageNode, DocumentNode> {

// ------------------------------ FIELDS ------------------------------

    /**
     * this contains all the different styles used in the document
     */
    @NotNull
    protected final List<Style>       styles = new ArrayList<Style>();
    @NotNull
    private final List<WordNode>      words  = new ArrayList<WordNode>();
    private ParagraphNode             abstractParagraph;
    private ParagraphNode             title;
    final private List<ParagraphNode> references = new ArrayList<ParagraphNode>();

// --------------------------- CONSTRUCTORS ---------------------------
    public DocumentNode() {}

// --------------------- GETTER / SETTER METHODS ---------------------
    public ParagraphNode getAbstractParagraph() {
        return abstractParagraph;
    }

    public void setAbstractParagraph(final ParagraphNode abstractParagraph) {
        this.abstractParagraph = abstractParagraph;
    }

    public List<ParagraphNode> getReferences() {
        return references;
    }

    @NotNull
    public List<Style> getStyles() {
        return styles;
    }

    public ParagraphNode getTitle() {
        return title;
    }

    public void setTitle(final ParagraphNode title) {
        this.title = title;
    }

    @NotNull
    public List<WordNode> getWords() {

        if (words.isEmpty()) {
            for (PageNode page : getChildren()) {
                for (ParagraphNode paragraph : page.getChildren()) {
                    for (LineNode line : paragraph.getChildren()) {
                        words.addAll(line.getChildren());
                    }
                }

//            for (GraphicsNode graphics : page.getGraphics()) {
//                for (ParagraphNode paragraph : graphics.getChildren()) {
//                    for (LineNode line : paragraph.getChildren()) {
//                        words.addAll(line.getChildren());
//                    }
//                }
//            }
            }
        }

        return words;
    }

// -------------------------- PUBLIC METHODS --------------------------

    /**
     * Returns a Comparator which will compare pagenumbers of the pages
     */
    @NotNull
    @Override
    public Comparator<PageNode> getChildComparator() {

        return new Comparator<PageNode>() {

            public int compare(@NotNull final PageNode o1, @NotNull final PageNode o2) {

                if (o1.getPage().getPageNumber() < o2.getPage().getPageNumber()) {
                    return -1;
                } else if (o1.getPage().getPageNumber() > o2.getPage().getPageNumber()) {
                    return 1;
                }

                return 0;
            }
        };
    }

    @Nullable
    public PageNode getPageNumber(final int pageNumber) {

        for (PageNode pageNode : getChildren()) {
            if (pageNode.getPageNumber() == pageNumber) {
                return pageNode;
            }
        }

        return null;
    }
}
