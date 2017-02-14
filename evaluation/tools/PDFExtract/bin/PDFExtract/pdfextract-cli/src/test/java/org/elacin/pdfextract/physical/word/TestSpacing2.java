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
import org.apache.log4j.MDC;
import org.elacin.pdfextract.content.PhysicalText;
import org.elacin.pdfextract.geom.Sorting;
import org.elacin.pdfextract.style.Style;
import org.elacin.pdfextract.util.FileWalker;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import static org.testng.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA. User: elacin Date: 01.12.10 Time: 07.05 To change this template use
 * File | Settings | File Templates.
 */
public class TestSpacing2 {

// ------------------------------ FIELDS ------------------------------
    private static final Logger log = Logger.getLogger(TestSpacing2.class);

// -------------------------- STATIC METHODS --------------------------
    private static float[] parseDistancesString(final String s, final List<Float> distances) {

        String distancesString = s.trim().substring(1, s.length() - 2);

        for (StringTokenizer tokenizer = new StringTokenizer(distancesString, ", ");
                tokenizer.hasMoreTokens(); ) {
            distances.add(Float.valueOf(tokenizer.nextToken()));
        }

        float[] distancesArray = new float[distances.size()];

        for (int i = 0, size = distances.size(); i < size; i++) {
            distancesArray[i] = distances.get(i);
        }

        return distancesArray;
    }

// -------------------------- PUBLIC METHODS --------------------------
    @Test
    public void testSpacings() throws IOException {

        final List<File> files = FileWalker.getFileListing(new File("target/test-classes/spacings"),
                                     ".spacing");
        int correct = 0,
            total   = 0;

        for (File file : files) {
            MDC.put("testInfo", file.getName());

            int            fileCorrect = 0,
                           fileTotal   = 0;
            BufferedReader reader      = new BufferedReader(new FileReader(file));
            String         s;
            String[]       input = new String[4];
            int            i     = 0;

            // noinspection NestedAssignment
            while ((s = reader.readLine()) != null) {
                input[i++] = s;

                if (i == 4) {
                    if (processInput(input)) {
                        fileCorrect++;
                    }

                    fileTotal++;
                    i = 0;
                }
            }

            log.info("LOG00820:File " + file + ": got " + fileCorrect + " of " + fileTotal);
            correct += fileCorrect;
            total   += fileTotal;
            MDC.remove("testInfo");
        }

        final int   errors        = total - correct;
        final float ERROR_PERCENT = 0.004f;
        final float errorLimit    = (float) total * ERROR_PERCENT;

        log.warn("###############################");
        log.warn("TOTAL: = " + total);
        log.warn("ERRORS: = " + errors);
        log.warn("ERRORLIMIT: = " + errorLimit + " (" + ERROR_PERCENT + "%)");
        log.warn("###############################");

        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();    // To change body of catch statement use File | Settings | File Templates.
        }

        assertTrue(errors < errorLimit);
    }

// -------------------------- OTHER METHODS --------------------------
//private String findResult(final String base,
//                          final float fontSize,
//                          final List<Float> distances,
//                          final float[] distancesArray) {
//    final float charspace = CharSpacingFinder.calculateCharspacingForDistances(distances,
//    fontSize);
//
//    StringBuilder sb = new StringBuilder();
//    sb.append(base.charAt(0));
//    int strIndex = 1;
//    for (int i = 0; i < distancesArray.length; i++) {
//        float distance = distancesArray[i];
//
//        if (distance > charspace) {
//            sb.append(" ");
//        }
//
//        final char c = base.charAt(strIndex++);
//        sb.append(c);
//    }
//
//    return sb.toString();
//}
    private boolean processInput(final String[] input) {

        String      answer         = input[1];
        String      base           = answer.replaceAll(" ", "");
        int         fontSize       = (int) Math.max(8.0f, (float) Float.valueOf(input[2]));
        List<Float> distances      = new ArrayList<Float>();
        float[]     distancesArray = parseDistancesString(input[3], distances);

        if (distancesArray.length != base.length() - 1) {
            log.info("bad input = " + Arrays.toString(input));

            return true;
        }

        final float        width    = fontSize;
        List<PhysicalText> line     = new ArrayList<PhysicalText>();
        float              currentX = 0.0f;
        Style style                 = new Style("font", "", fontSize, fontSize, "fontid", false, false,
                                          false);

        for (int i = 0; i < base.length(); i++) {
            final char c        = base.charAt(i);
            float      distance = (i == 0)
                                  ? 0.0f
                                  : distancesArray[i - 1];

            currentX += distance;
            line.add(new PhysicalText(new String(new char[] { c }), style, currentX, 0.0f, width, 1.0f,
                                      0.0f));
            currentX += width;
        }

        Collections.sort(line, Sorting.sortByLowerX);

        final Collection<PhysicalText> ret    = WordSegmentatorImpl.createWordsInLine(line);
        final boolean                  equals = ret.size() - 1 == answer.length() - base.length();
        StringBuilder                  sb     = new StringBuilder();

        for (PhysicalText physicalText : ret) {
            sb.append(physicalText.getText()).append(" ");
        }

        sb.setLength(sb.length() - 1);

        String result = sb.toString();

        // final String result = findResult(base, fontSize, distances, distancesArray);
        //
        // final boolean equals = answer.equals(result);
        if (!equals) {
            log.warn("wrong result: got '" + result + "', expected: '" + answer + "'");

            // findResult(base, fontSize, distances, distancesArray);
        }

        return equals;
    }
}
