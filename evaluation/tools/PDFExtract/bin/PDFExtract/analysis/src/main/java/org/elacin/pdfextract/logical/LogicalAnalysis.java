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

import org.elacin.pdfextract.logical.operation.*;
import org.elacin.pdfextract.tree.DocumentNode;

/**
 * Created by IntelliJ IDEA. User: elacin Date: 31.05.11 Time: 06.38 To change this template use
 * File | Settings | File Templates.
 */
public class LogicalAnalysis {

// ------------------------------ FIELDS ------------------------------
    private static final Logger log = Logger.getLogger(LogicalAnalysis.class);

// -------------------------- PUBLIC STATIC METHODS --------------------------
    public static void analyzeDocument(final DocumentNode root, final boolean arc) {

        if (root.getWords().isEmpty() || root.getChildren().isEmpty()) {
            log.warn("LOG01590:tried to analyze empty document");
            return;
        }

        final DocumentMetadata metadata = new DocumentMetadata(root);

        new ExtractTitle().doOperation(root, metadata);
        new RemovePageNumbers().doOperation(root, metadata);

        if (arc) {
            new ExtractFootnotes().doOperation(root, metadata);
            new ExtractAbstractAndRemovePreceedingText().doOperation(root, metadata);
        }

        new RecognizeDivs().doOperation(root, metadata);
    }
}
