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



package org.elacin.pdfextract.datasource.pdfbox;

import org.apache.log4j.Logger;
import org.apache.log4j.MDC;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.elacin.pdfextract.Constants;
import org.elacin.pdfextract.datasource.DocumentContent;
import org.elacin.pdfextract.datasource.PDFSource;
import org.elacin.pdfextract.datasource.RenderedPage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA. User: elacin Date: 15.01.11 Time: 19.57 To change this template use
 * File | Settings | File Templates.
 */
public class PDFBoxSource implements PDFSource {

// ------------------------------ FIELDS ------------------------------
    private static final Logger log = Logger.getLogger(PDFBoxSource.class);
    private DocumentContent     contents;
    @NotNull
    private final PDDocument    doc;
    private final int           endPage;
    @NotNull
    public final File           pdfDocument;
    private final int           startPage;

// --------------------------- CONSTRUCTORS ---------------------------
    public PDFBoxSource(@NotNull File pdfDocument, int startPage, int endPage, String password) {

        this.pdfDocument = pdfDocument;
        this.startPage   = startPage;
        this.endPage     = endPage;
        doc              = openPdfDocument(pdfDocument, password);
    }

// ------------------------ INTERFACE METHODS ------------------------
// --------------------- Interface PDFSource ---------------------
    @NotNull
    public DocumentContent readPages() {

        if (contents != null) {
            return contents;
        }

        final long        t0 = System.currentTimeMillis();
        PDFBoxIntegration pdfbox;

        try {
            pdfbox = new PDFBoxIntegration(doc, startPage, endPage);
            pdfbox.processDocument();
        } catch (IOException e) {
            throw new RuntimeException("Error while reading document", e);
        }

        final long td = System.currentTimeMillis() - t0;

        log.info("LOG01190:Read document in " + td + " ms");
        contents = pdfbox.getContents();

        return contents;
    }

    @NotNull
    public RenderedPage renderPage(int pageNum) {

        final PDPage        page = (PDPage) doc.getDocumentCatalog().getAllPages().get(pageNum - 1);
        final BufferedImage image;

        try {
            image = page.convertToImage(8, Constants.RENDER_DPI);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        float xScale = (float) image.getWidth() / page.findMediaBox().getWidth();
        float yScale = (float) image.getHeight() / page.findMediaBox().getHeight();

        return new RenderedPage(image, xScale, yScale);
    }

    public void closeSource() {

        try {
            doc.close();
        } catch (IOException e) {
            log.warn("LOG01250:Error while closing PDF document", e);
        }
    }

// -------------------------- STATIC METHODS --------------------------
    @NotNull
    protected static PDDocument openPdfDocument(@NotNull final File pdfFile,
            @Nullable final String password) {

        long t0 = System.currentTimeMillis();

        MDC.put("doc", pdfFile.getName());
        log.info("LOG00120:Opening PDF file " + pdfFile + ".");

        try {
            final PDDocument document = PDDocument.load(pdfFile);

            if (document.isEncrypted()) {
                if (password != null) {
                    try {
                        document.decrypt(password);
                    } catch (Exception e) {
                        throw new RuntimeException("Error while reading encrypted PDF:", e);
                    }
                } else {
                    log.warn("File claims to be encrypted, a password should be provided");
                }
            }

            log.debug("load()took" + (System.currentTimeMillis() - t0) + "ms");

            return document;
        } catch (IOException e) {
            MDC.put("doc", "");

            throw new RuntimeException("Error while reading " + pdfFile + ".", e);
        }
    }
}
