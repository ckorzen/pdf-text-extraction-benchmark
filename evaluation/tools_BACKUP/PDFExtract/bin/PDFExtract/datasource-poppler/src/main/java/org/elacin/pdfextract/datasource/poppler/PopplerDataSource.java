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



package org.elacin.pdfextract.datasource.poppler;

import com.sun.jna.*;
import com.sun.jna.ptr.PointerByReference;

import org.elacin.pdfextract.datasource.DocumentContent;
import org.elacin.pdfextract.datasource.PDFSource;
import org.elacin.pdfextract.datasource.RenderedPage;

import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * Created by IntelliJ IDEA. User: elacin Date: 06.06.11 Time: 16.46 To change this template use
 * File | Settings | File Templates.
 */
public class PopplerDataSource implements PDFSource {

// ------------------------------ FIELDS ------------------------------
    final File                  file;
    private static final String uri           = "file:///Users/elacin/projects/evaluation/docs/C02-1013.pdf";
    private static final String emptyPassword = "";
    private PopplerInterface    poppler       = PopplerInterface.INSTANCE;

// --------------------------- CONSTRUCTORS ---------------------------
    public PopplerDataSource(final File file) {
        this.file = file;
    }

// ------------------------ INTERFACE METHODS ------------------------
// --------------------- Interface PDFSource ---------------------
    public void closeSource() {}

    @NotNull
    public DocumentContent readPages() {

        /* initialize glib */
        poppler.g_type_init();

        /* open document */
        GError errorObj                       = new GError();
        final PopplerDocument popplerDocument = poppler.poppler_document_new_from_file(uri,
                                                    emptyPassword, errorObj);

        if (popplerDocument == null) {
            throw new RuntimeException(errorObj.toString());
        }

        long t0 = System.currentTimeMillis();
        final int numPages = poppler.poppler_document_get_n_pages(popplerDocument);

        if (numPages > 0) {
            final Pointer    firstPage = poppler.poppler_document_get_page(popplerDocument, 0);
            PopplerRectangle selection = new PopplerRectangle(0, 0, 1000, 1000);
            final String content       = poppler.poppler_page_get_text(firstPage,
                                             PopplerSelectionStyle.POPPLER_SELECTION_GLYPH, selection);

            System.out.println("content = " + content);
//            final Pointer contentList = poppler.poppler_page_get_text_page(firstPage);

//            System.out.println(contentList);
        }

        /* start reading content */
        final DocumentContent documentContent = new DocumentContent();

        System.out.println("t = " + (System.currentTimeMillis() - t0));
        return documentContent;
    }

    @NotNull
    public RenderedPage renderPage(final int page) {
        return null;
    }

// -------------------------- INNER CLASSES --------------------------
    public interface PopplerInterface extends Library {

        PopplerInterface INSTANCE = (PopplerInterface) Native.loadLibrary("poppler-glib",
                                        PopplerInterface.class);

        PopplerDocument poppler_document_new_from_file(String uri, String password, GError error);

        int poppler_document_get_n_pages(PopplerDocument doc);

        Pointer poppler_document_get_page(PopplerDocument doc, int index);

        String poppler_page_get_text(Pointer page, int style, PopplerRectangle rect);

        Pointer poppler_page_get_text_page(Pointer page);

        void poppler_page_finalize(Pointer page);

        void g_type_init();
    }


    public interface PopplerSelectionStyle {

        int POPPLER_SELECTION_GLYPH = 0,
            POPPLER_SELECTION_WORD  = 1,
            POPPLER_SELECTION_LINE  = 2;
    }


    public static class PopplerRectangle extends Structure implements Structure.ByReference {

        public PopplerRectangle(final double x1, final double y1, final double x2, final double y2) {

            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
        }

        public double x1;
        public double y1;
        public double x2;
        public double y2;
    }


    public static class PopplerDocument extends Structure implements Structure.ByReference {

        // GObject parent_instance;
        public Pointer parent_instance;

        // PDFDoc *doc;
        public Pointer doc;

        // GList *layers;
        public Pointer layers;

        // GList *layers_rbgroups;
        public Pointer layers_rbgroups;

        // CairoOutputDev *output_dev;
        public Pointer output_dev;
    }


//    public static class PopplerPage extends Structure implements Structure.ByReference {}
    public static class GError extends Structure implements Structure.ByReference {

        public String domain;
        public int    code;
        public String message;

        @Override
        public String toString() {

            return "GError{" + "domain='" + domain + '\'' + ", code=" + code + ", " + "message='"
                   + message + '\'' + '}';
        }
    }


// --------------------------- main() method ---------------------------
    public static void main(String[] args) {

        System.setProperty("jna.library.path", "/opt/local/lib");
        new PopplerDataSource(new File("asd")).readPages();
    }
}
