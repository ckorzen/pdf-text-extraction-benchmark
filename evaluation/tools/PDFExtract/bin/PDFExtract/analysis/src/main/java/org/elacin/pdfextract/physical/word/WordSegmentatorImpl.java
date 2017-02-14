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



package org.elacin.pdfextract.physical.word;

import org.apache.log4j.Logger;

import org.elacin.pdfextract.content.PhysicalText;
import org.elacin.pdfextract.geom.Rectangle;
import org.elacin.pdfextract.geom.Sorting;
import org.elacin.pdfextract.style.Style;
import org.elacin.pdfextract.style.StyleComparator;
import org.elacin.pdfextract.style.StyleDifference;

import org.jetbrains.annotations.NotNull;

import java.util.*;

import static org.elacin.pdfextract.Constants.USE_EXISTING_WHITESPACE;
import static org.elacin.pdfextract.geom.MathUtils.isWithinVariance;
import static org.elacin.pdfextract.style.StyleDifference.*;

public class WordSegmentatorImpl implements WordSegmentator {

// ------------------------------ FIELDS ------------------------------
    private static final Logger log       = Logger.getLogger(WordSegmentatorImpl.class);
    public static final float   fontDenom = 5.0f;

// ------------------------ INTERFACE METHODS ------------------------
// --------------------- Interface WordSegmentator ---------------------

    /**
     * This method will convert the text into PhysicalTexts. <p/> To do this, the text is split on
     * whitespaces, character and word distances are approximated, and words are created based on
     * those
     */
    @NotNull
    public List<PhysicalText> segmentWords(@NotNull final List<PhysicalText> texts) {

        final long         t0  = System.currentTimeMillis();
        List<PhysicalText> ret = new ArrayList<PhysicalText>(texts.size());

        /**
         * iterate through all incoming TextPositions, and process them
         * in a line by line fashion. We do this to be able to calculate
         * char and word distances for each line
         */
        List<PhysicalText> line = new ArrayList<PhysicalText>();

        Collections.sort(texts, Sorting.sortTextByBaseLine);

        float baseline     = 0.0f;
        float maxY         = Float.MIN_VALUE;
        float maxX         = 0.0f;
        Style currentStyle = null;

        for (final PhysicalText text : texts) {

            /* if this is the first text in a line */
            if (line.isEmpty()) {
                baseline     = text.getBaseLine();
                maxX         = text.getPos().endX;
                currentStyle = text.getStyle();
            }

            final boolean stopGrouping = isOnAnotherLine(baseline, text, maxY)
                                         || isTooFarAwayHorizontally(maxX, text)
                                         || fontDiffers(currentStyle, text);

            if (stopGrouping) {
                if (!line.isEmpty()) {
                    ret.addAll(createWordsInLine(line));
                    line.clear();
                }

                baseline     = text.getBaseLine();
                maxY         = text.getPos().endY;
                currentStyle = text.getStyle();
            }

            /* then add the current text to start next line */
            line.add(text);
            maxY = Math.max(maxY, text.getPos().endX);
            maxX = text.getPos().endX;
        }

        if (!line.isEmpty()) {
            ret.addAll(createWordsInLine(line));
            line.clear();
        }

        if (log.isDebugEnabled()) {
            log.debug("LOG00010:word segmentation took " + (System.currentTimeMillis() - t0) + " ms");
        }

        return ret;
    }

// -------------------------- PUBLIC STATIC METHODS --------------------------

    /**
     * The above methods are generally responsible for grouping text according to line and style;
     * this is the one which will actually do the segmentation. <p/> There are two cases to consider
     * for this process: - whitespace already existing: combine characters into words in the obvious
     * way - whitespace must be found: <p/> First approximate the applied intra-word character
     * spacing. <p/> Then, iterate through the characters in the line from left to right: calculate
     * the real distance between a pair of characters normalize that by subtracting the charspacing
     * if that normalized spacing is bigger than fontSize / 15, consider the space a word boundary
     *
     * @param line
     * @return
     */
    @NotNull
    public static Collection<PhysicalText> createWordsInLine(@NotNull final List<PhysicalText> line) {

        /* unfinished words are put back into this queue, and will this be picked as currentWord below */
        /*
        * Note, sorting this by lower X-coords breaks because of bad information for many documents
        * */
        final List<PhysicalText> queue = line;

        /* this list of words will be returned */
        final Collection<PhysicalText> segmentedWords = new ArrayList<PhysicalText>();

        /* if we already have whitespace information */
        final boolean containsSpaces = USE_EXISTING_WHITESPACE && containsWhiteSpace(line);

        /* an approximate average charspacing distance */
        final float charSpacing = approximateCharSpacing(line);

        /* all font sizes are the same. if it is missing just guess 10 */
        final float fontSize;

        if (line.get(0).getStyle().xSize == 0) {
            fontSize = 10.0f;
        } else {
            fontSize = (float) line.get(0).getStyle().xSize;
        }

        /*
         *       this is necessary to keep track of the width of the last character we
         *       combined into a a word, else it would disappear when combining
         */
        float currentWidth = queue.get(0).getPos().width;

        if (log.isDebugEnabled()) {
            printLine(line);
        }

        /**
         * iterate through all texts from left to right, and combine into words as we go
         */
        while (!queue.isEmpty()) {
            final PhysicalText currentWord = queue.remove(0);
            final PhysicalText nextChar    = queue.isEmpty() ? null : queue.get(0);

            /* we have no need for spaces after establishing word boundaries, so skip */
            if ("".equals(currentWord.getText().trim())) {
                continue;
            }

            /* if it is the last in line */
            if (nextChar == null) {
                segmentedWords.add(currentWord);

                break;
            }

            /**
             * determine if we found a word boundary or not
             */
            final boolean isWordBoundary;

            if (containsSpaces) {
                isWordBoundary = "".equals(nextChar.getText().trim());
            } else {
                final float distance = currentWord.getPos().distance(nextChar.getPos());
                final float limit    = 0.8f * fontSize / fontDenom;

                isWordBoundary = distance - charSpacing > limit;

                if (log.isDebugEnabled()) {
                    log.debug(currentWord.getText() + "[" + currentWidth + "] " + distance + " "
                              + nextChar.getText() + "[" + nextChar.getPos().width + "]: limit=" + limit
                              + ", effective distance:" + (distance - charSpacing) + ", fontSize:"
                              + (fontSize) + ", charSpacing:" + charSpacing);
                }
            }

            /**
             * combine characters if necessary
             */
            if (isWordBoundary) {

                /* save this word and continue with next */
                segmentedWords.add(currentWord);
            } else {

                /* combine the two fragments */
                PhysicalText combinedWord = currentWord.combineWith(nextChar);

                queue.remove(nextChar);
                queue.add(0, combinedWord);
            }

            currentWidth = nextChar.getPos().width;
        }

        for (PhysicalText text : segmentedWords) {
            if (log.isDebugEnabled()) {
                log.debug("LOG00540: created " + text);
            }
        }

        return segmentedWords;
    }

// -------------------------- STATIC METHODS --------------------------

    /**
     * Tries to find an estimate of the character spacing applied to the given line of characters.
     * <p/> The idea is that font kerning and other local adjustments will contribute relatively
     * little to the observed distance between characters, whereas the more general applied
     * character spacing will make up by far the biggest amount of the space. <p/> These local
     * adjustments will contribute in both directions, so in many cases we will be able to get a
     * somewhat good approximation if we average out a semi-random number of the smallest distances.
     * <p/> To put some numbers to this, say we have character distances varying from 3.5 to 9pt. If
     * we iterate through the first n distances, with distances ranging from 3.5 to 4.5pt (the rest
     * being skipped for being too big), the approximation of the character spacing would thus end
     * up around 4.
     *
     * @param line list of characters in the line. this must be sorted
     * @return an approximate character spacing
     */
    static float approximateCharSpacing(@NotNull List<PhysicalText> line) {

        /**
         * the real lower bound where this algorithm applies might be higher, but
         *  at least for 0 or 1 distances it would be non-functional
         */
        if (line.size() <= 1) {
            return 0.0f;
        }

        final float[] distances = calculateDistancesBetweenCharacters(line);

        Arrays.sort(distances);

        /**
         * This value deserves a special notice. When it was written semi-random above,
         *  this is essentially what was meant. We start out with the smallest distance,
         *  and will keep iterating until the numbers start to be bigger. The underlying
         *  assumption here is that word spacing will never be only 1.5 times the smallest
         *  space occurring between two characters.
         *
         * The fontDenom is a quite random number, it's purpose is to avoid breaking the
         *  algorithm for negative character distances (which are common and useful),
         *  and its only properties are that it is too small to ever separate a word, and
         *  that it is a positive number :)
         */
        final float maxBoundary = Math.max(fontDenom, distances[0] * 2f);
        int         counted     = 0;
        float       sum         = 0.0f;

        for (float sortedDistance : distances) {
            if (sortedDistance > maxBoundary) {
                break;
            }

            sum += sortedDistance;
            counted++;
        }

        return sum / (float) counted;
    }

    /**
     * Calculates a list of distances between the given list of characters in the obvious way.
     *
     * @param line list of characters. this should be sorted!
     * @return
     */
    @NotNull
    private static float[] calculateDistancesBetweenCharacters(@NotNull List<PhysicalText> line) {

        if (line.size() <= 1) {
            return new float[0];
        }

        final float[] distances = new float[line.size() - 1];

        for (int i = 0; i < line.size() - 1; i++) {
            final Rectangle leftChar  = line.get(i).getPos();
            final Rectangle rightChar = line.get(i + 1).getPos();

            distances[i] = leftChar.distance(rightChar);
        }

        return distances;
    }

    private static boolean containsWhiteSpace(@NotNull List<PhysicalText> line) {

        for (PhysicalText physicalText : line) {
            if (" ".equals(physicalText.getText())) {
                return true;
            }
        }

        return false;
    }

    private static boolean fontDiffers(@NotNull final Style style, @NotNull final PhysicalText text) {

        StyleDifference diff = StyleComparator.styleCompare(text.getStyle(), style);

        return diff.equals(BIG_DIFFERENCE) || diff.equals(SPLIT);
    }

    private static boolean isOnAnotherLine(final float baseline, @NotNull final PhysicalText text,
            final float maxY) {
        return ((baseline != text.getBaseLine()) && (text.getBaseLine() > maxY));
    }

    private static boolean isTooFarAwayHorizontally(final float endX, @NotNull final PhysicalText text) {

        final float variation = text.getPos().width;

        return !isWithinVariance(endX, text.getPos().x, variation);
    }

    private static void printLine(@NotNull List<PhysicalText> physicalTexts) {

        StringBuffer sb = new StringBuffer();

        for (PhysicalText physicalText : physicalTexts) {
            sb.append(physicalText.getText());
        }

        log.debug("line:" + sb);
    }
}
