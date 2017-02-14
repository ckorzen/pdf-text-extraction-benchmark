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



package org.elacin.pdfextract;

import org.apache.log4j.Logger;

import org.elacin.pdfextract.datasource.DocumentContent;
import org.elacin.pdfextract.datasource.PDFSource;
import org.elacin.pdfextract.datasource.pdfbox.PDFBoxSource;
import org.elacin.pdfextract.logical.LogicalAnalysis;
import org.elacin.pdfextract.physical.GeometricAnalysis;
import org.elacin.pdfextract.renderer.PageRenderer;
import org.elacin.pdfextract.tree.DocumentNode;
import org.elacin.pdfextract.xml.SimpleXMLOutput;
import org.elacin.pdfextract.xml.TEIOutput;

import org.jetbrains.annotations.NotNull;

import java.io.File;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.Date;

import static org.elacin.pdfextract.Constants.*;

/**
 * Created by IntelliJ IDEA. User: elacin Date: 15.01.11 Time: 19.55 To change this template use
 * File | Settings | File Templates.
 */
public class ProcessDocument {

// ------------------------------ FIELDS ------------------------------
    private static final Logger log = Logger.getLogger(ProcessDocument.class);
    @NotNull
    public final File           pdfFile;
    @NotNull
    private final File          dest;
    public String               password;
    public int                  startPage;
    public int                  endPage;
    final boolean               arc;

// --------------------------- CONSTRUCTORS ---------------------------
    public ProcessDocument(File pdfFile, File dest, String password, int startPage, int endPage,
                           final boolean arc) {

        this.dest      = dest;
        this.pdfFile   = pdfFile;
        this.password  = password;
        this.startPage = startPage;
        this.endPage   = endPage;
        this.arc       = arc;
    }

// -------------------------- STATIC METHODS --------------------------
    @NotNull
    private static File getOutputFile(@NotNull File destination, @NotNull File baseFile,
                                      String extension) {

        final File output;

        if (destination.isDirectory()) {
            output = new File(destination, baseFile.getName().replace(".pdf", extension));
        } else {
            output = new File(destination.getAbsolutePath().replace(".pdf", extension));
        }

        return output;
    }

    static void renderPDF(PDFSource source, @NotNull DocumentNode root, @NotNull File destination) {

        long               t0         = System.currentTimeMillis();
        final PageRenderer renderer   = new PageRenderer(source, root, RENDER_RESOLUTION);
        DateFormat         dateFormat = new SimpleDateFormat("MMddHHmm");
        Date               date       = new Date();

        for (int i = 0; i < root.getChildren().size(); i++) {

            /* one indexed pages */
            final int pageNum = root.getChildren().get(i).getPageNumber();

            /* then open and write to file */
            String path = destination.getAbsolutePath();

            path = path.replace("%p", String.valueOf(pageNum));
            path = path.replace("%d", dateFormat.format(date));

            final File outputFile = new File(path);

            renderer.renderToFile(pageNum, outputFile);
        }

        log.debug("Rendering of pdf took " + (System.currentTimeMillis() - t0) + " ms");
    }

// -------------------------- PUBLIC METHODS --------------------------
    public DocumentNode processFile() {

        PDFSource    source = null;
        DocumentNode documentNode;

        try {
            source = new PDFBoxSource(pdfFile, startPage, endPage, password);

            final DocumentContent content = source.readPages();

            documentNode = GeometricAnalysis.analyzeDocument(content);
            if (SIMPLE_OUTPUT_ENABLED) {
                File xmlOutFile = getOutputFile(dest, pdfFile, SIMPLE_OUTPUT_EXTENSION);

                new SimpleXMLOutput().writeTree(documentNode, xmlOutFile);
            }

            LogicalAnalysis.analyzeDocument(documentNode, arc);

            if (RENDER_ENABLED) {
                renderPDF(source, documentNode, getOutputFile(dest, pdfFile, ".%d.%p.png"));
            }

            if (TEI_OUTPUT_ENABLED) {
                File teiOutFile = getOutputFile(dest, pdfFile, TEI_OUTPUT_EXTENSION);
 	    	
                new TEIOutput().writeTree(documentNode, teiOutFile);
            }
        } finally {
            if (source != null) {
                source.closeSource();
            }
        }

        return documentNode;
    }
}
