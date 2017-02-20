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


package org.elacin.pdfextract.test;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA. User: elacin Date: May 9, 2010 Time: 6:00:04 PM To change this template
 * use File | Settings | File Templates.
 */
public class LatexDocumentLoader {

// -------------------------- PUBLIC STATIC METHODS --------------------------
    @NotNull
    public static String readLatex(String filename) throws IOException {

        // /* Create vanilla SnuggleEngine and new SnuggleSession */
        // SnuggleEngine engine = new SnuggleEngine();
        // SnuggleSession session = engine.createSession();
        //
        // /* Parse some LaTeX input */
        // SnuggleInput input = new SnuggleInput(new File(filename));
        // session.parseInput(input);
        //
        // /* Specify how we want the resulting XML */
        // XMLStringOutputOptions options = new XMLStringOutputOptions();
        // options.setSerializationMethod(SerializationMethod.XHTML);
        // options.setIndenting(true);
        // options.setEncoding("UTF-8");
        // options.setAddingMathSourceAnnotations(true);
        // if (engine.getStylesheetManager().supportsXSLT20()) {
        // /* Caller has an XSLT 2.0 processor, so let's output named entities for readability */
        // options.setUsingNamedEntities(true);
        // }
        // /**
        // * Caused by: uk.ac.ed.ph.snuggletex.SnuggleRuntimeException: SerializatonMethod.XHTML requires an XSLT 2.0 processor.
        // * Your TransformerFactoryChooser uk.ac.ed.ph.snuggletex.utilities.DefaultTransformerFactoryChooser@69f94884 could not
        // *  provide such a processor. Please check that an XSLT 2.0 processor (e.g. Saxon) is available and your
        // * TransformerFactoryChooser is configured to provide this
        //
        // */
        //
        // /* Convert the results to an XML String, which in this case will
        // * be a single MathML <math>...</math> element. */
        // return session.buildXMLString(options);
        return "enable Snuggle in dependencies and rebuild :)";
    }
}
