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



package org.elacin.pdfextract.xml;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * Created by IntelliJ IDEA. User: elacin Date: 16.01.11 Time: 23.55 To change this template use
 * File | Settings | File Templates.
 */
public class PrettyPrinter {

// -------------------------- PUBLIC STATIC METHODS --------------------------
    public static String prettyFormat(String input) {
        return prettyFormat(input, 4);
    }

// -------------------------- STATIC METHODS --------------------------
    private static String prettyFormat(String input, int indent) {

        try {
            Source       xmlInput     = new StreamSource(new StringReader(input));
            StringWriter stringWriter = new StringWriter();
            StreamResult xmlOutput    = new StreamResult(stringWriter);
            Transformer  transformer  = TransformerFactory.newInstance().newTransformer();

            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount",
                                          String.valueOf(indent));
            transformer.transform(xmlInput, xmlOutput);

            return stringWriter.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
