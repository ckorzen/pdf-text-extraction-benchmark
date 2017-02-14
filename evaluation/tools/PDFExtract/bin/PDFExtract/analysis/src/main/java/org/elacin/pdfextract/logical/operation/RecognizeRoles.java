/*
 * Copyright 2010-2011 �yvind Berg (elacin@gmail.com)
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



package org.elacin.pdfextract.logical.operation;

import org.apache.log4j.Logger;

import org.elacin.pdfextract.logical.DocumentMetadata;
import org.elacin.pdfextract.logical.Operation;
import org.elacin.pdfextract.style.Style;
import org.elacin.pdfextract.tree.DocumentNode;
import org.elacin.pdfextract.tree.Role;
import org.elacin.pdfextract.tree.WordNode;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA. User: elacin Date: Mar 23, 2010 Time: 3:11:50 AM To change this
 * template use File | Settings | File Templates.
 */
public class RecognizeRoles implements Operation {

// ------------------------------ FIELDS ------------------------------
    private static final Logger log = Logger.getLogger(RecognizeRoles.class);

/* these are used to recognize identifiers */
    static final Pattern id                = Pattern.compile("(?:X\\d{1,2}|\\w{1,2})");
    static final Pattern refWithDotPattern = Pattern.compile("\\s*(" + id + "\\s*\\.\\s*\\d?).*",
                                                 Pattern.DOTALL | Pattern.MULTILINE);
    static final Pattern numInParenthesisPattern = Pattern.compile("(\\(\\s*" + id + "\\s*\\)).*",
                                                       Pattern.DOTALL | Pattern.MULTILINE);
    @Nullable
    final Style breadtext = null;

// ------------------------ INTERFACE METHODS ------------------------
// --------------------- Interface Operation ---------------------
    public void doOperation(@NotNull final DocumentNode root, final DocumentMetadata metadata) {

        if (breadtext == null) {
            log.error("provide breadtext here");

            return;
        }

        for (WordNode word : root.getWords()) {
            checkForIdentifier(word);
            checkForTopNote(word);
            checkForPageNumber(word);
        }
    }

// -------------------------- OTHER METHODS --------------------------
    void checkForIdentifier(@NotNull final WordNode word) {

        String       mark        = null;
        final String trimmedText = word.text.trim();

        if ("".equals(trimmedText)) {
            return;
        }

        // TODO:!
        if (word.getStyle().equals(breadtext)) {
            return;
        }

        final Matcher matcher = numInParenthesisPattern.matcher(word.text);

        if (matcher.matches()) {
            mark = matcher.group(1);
        } else {
            final Matcher matcher2 = refWithDotPattern.matcher(word.text);

            if (matcher2.matches()) {
                mark = matcher2.group(1);
            }
        }

        /* if the first character is '*' or '-' set that as mark */

        // final String firstChar = trimmedText.substring(0, 1);
        // if ("*-".contains(firstChar)) {
        // mark = firstChar;
        // }
        if (mark != null) {
            word.addRole(Role.IDENTIFIER);
        }
    }

    private void checkForPageNumber(@NotNull final WordNode word) {

        boolean isNumber = true;

        if (((word.text.length() < 5) && word.hasRole(Role.FOOTNOTE)) || word.hasRole(Role.HEADNOTE)) {
            for (int i = 0; i < word.text.length(); i++) {
                if (!Character.isDigit(word.text.charAt(i))) {
                    isNumber = false;

                    break;
                }
            }

            if (isNumber) {
                word.addRole(Role.PAGENUMBER);
            }
        }
    }

    private void checkForTopNote(@NotNull final WordNode word) {

        if (word.getPos().y < (word.getPage().getPos().height * 5.0f / 100)) {

            /* then check the font. we either want smaller than breadtext, or same size but different type */
            if ((word.getStyle().ySize < breadtext.ySize)
                    || ((word.getStyle().ySize == breadtext.ySize)
                        &&!word.getStyle().fontName.equals(breadtext.fontName))) {
                word.addRole(Role.HEADNOTE);
            }
        }
    }
}
