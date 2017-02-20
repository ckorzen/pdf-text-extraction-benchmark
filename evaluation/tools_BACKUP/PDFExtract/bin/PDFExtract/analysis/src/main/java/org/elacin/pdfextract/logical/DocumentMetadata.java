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



package org.elacin.pdfextract.logical;

import org.apache.log4j.Logger;

import org.elacin.pdfextract.style.Style;
import org.elacin.pdfextract.tree.*;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class represents knowledge collected while performing the logical analysis of the document
 *  so the information can be shared between the different operations
 */
public class DocumentMetadata {

// ------------------------------ FIELDS ------------------------------
    private static final Logger       log = Logger.getLogger(DocumentMetadata.class);
    private final DocumentNode        root;
    private final Style               bodyText;
    private final List<Style>         candidateHeaderStyles;
    private final Map<Style, Integer> styleCounts;

// --------------------------- CONSTRUCTORS ---------------------------
    public DocumentMetadata(final DocumentNode root) {

        this.root             = root;
        styleCounts           = findStyleCountsFromDocument(root);
        bodyText              = findBodyTextFromStyleCounts(styleCounts);
        candidateHeaderStyles = findHeaderCandidates(root, bodyText);

        if (log.isInfoEnabled()) {
            log.info("LOG01410:bodytext is " + bodyText);
            log.info("LOG01480:headerCandidates = " + candidateHeaderStyles);
        }

    }

// -------------------------- STATIC METHODS --------------------------
    private static boolean canBeHeaderStyle(@NotNull final Style bodyText,
            @NotNull final LineNode line) {

        boolean b = line.getStyle().xSize >= bodyText.xSize;

        if (b) {
            log.info("LOG01450:Line " + line + " can be header style");
        }

        return b;
    }

    private static boolean canBeLineId(@NotNull final LineNode line, @NotNull final Style bodyText) {

        boolean fontSame            = bodyText.fontName.equals(line.getStyle().fontName);
        boolean smallerThanBodyText = bodyText.xSize >= line.getStyle().xSize;

        if (fontSame || smallerThanBodyText) {
            return false;
        }

        final String firstWord = line.getText().trim().split("\\s")[0];

        if (firstWord.length() > 3) {
            return false;
        }

        if (Character.isDigit(firstWord.charAt(0)) || firstWord.contains(".")
                || ("abcdABCI".indexOf(firstWord.charAt(0)) != -1)) {
            log.warn("LOG01440:Line " + line + " can be line id");

            return true;
        }

        return false;
    }

    @NotNull
    private static Style findBodyTextFromStyleCounts(final Map<Style, Integer> styleCounts) {

        Style bodyText = Style.NO_STYLE;
        int   maxCount = Integer.MIN_VALUE;

        for (Map.Entry<Style, Integer> entry : styleCounts.entrySet()) {
            if (maxCount < entry.getValue()) {
                maxCount = entry.getValue();
                bodyText = entry.getKey();
            }
        }

        return bodyText;
    }

    /**
     *  create a list of possible styles for headings
     */
    @NotNull
    private static List<Style> findHeaderCandidates(@NotNull final DocumentNode root,
            @NotNull final Style bodyText) {

        List<Style> headerCandidates = new ArrayList<Style>(root.getStyles().size());

        for (PageNode page : root.getChildren()) {
            for (ParagraphNode paragraph : page.getChildren()) {
                for (LineNode line : paragraph.getChildren()) {
                    Style lineStyle = line.getStyle();

                    if (headerCandidates.contains(lineStyle) || bodyText.equals(lineStyle)) {
                        continue;
                    }

                    if (canBeHeaderStyle(bodyText, line) || (canBeLineId(line, bodyText))) {
                        headerCandidates.add(lineStyle);
                    }
                }
            }
        }

        return headerCandidates;
    }

    private static Map<Style, Integer> findStyleCountsFromDocument(final DocumentNode root) {

        Map<Style, Integer> styleCounts = new HashMap<Style, Integer>(root.getStyles().size());

        for (int i = 0; i < root.getStyles().size(); i++) {
            styleCounts.put(root.getStyles().get(i), 0);
        }

        for (WordNode word : root.getWords()) {
            if (!styleCounts.containsKey(word.getStyle())) {
                continue;
            }

            int old = styleCounts.get(word.getStyle());

            styleCounts.put(word.getStyle(), old + word.getText().length());
        }

        return styleCounts;
    }

// --------------------- GETTER / SETTER METHODS ---------------------
    public Style getBodyText() {
        return bodyText;
    }

    public List<Style> getCandidateHeaderStyles() {
        return candidateHeaderStyles;
    }

    public DocumentNode getRoot() {
        return root;
    }

    public Map<Style, Integer> getStyleCounts() {
        return styleCounts;
    }
}
