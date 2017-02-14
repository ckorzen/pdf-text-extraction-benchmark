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

import org.apache.log4j.Logger;
import org.elacin.pdfextract.Constants;
import org.elacin.pdfextract.geom.Rectangle;
import org.elacin.pdfextract.style.Style;
import org.elacin.pdfextract.tree.*;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Collections;
import java.util.List;

import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;
import static org.elacin.pdfextract.geom.Sorting.sortStylesById;

/**
 * Created by IntelliJ IDEA. User: elacin Date: 16.01.11 Time: 17.14 To change this template use
 * File | Settings | File Templates.
 */
public class SimpleXMLOutput implements XMLWriter {

// ------------------------------ FIELDS ------------------------------
    private static final Logger log              = Logger.getLogger(SimpleXMLOutput.class);
    private int                 indent           = 0;
    private final int           indentationWidth = 4;

// ------------------------ INTERFACE METHODS ------------------------
// --------------------- Interface XMLWriter ---------------------
    public void writeTree(@NotNull final DocumentNode root, @NotNull final File output) {

        /* write to file */
        log.info("LOG00110:Opening " + output + " for output");

        final PrintStream out;

        try {
            out = new PrintStream(new BufferedOutputStream(new FileOutputStream(output, false),
                    8192 * 4), false, "UTF-8");
        } catch (Exception e) {
            throw new RuntimeException("Could not open output file", e);
        }

        StringBuffer sb = new StringBuffer();

        writeDocument(sb, root);

        // final String result = PrettyPrinter.prettyFormat(sb.toString());
        final String result = sb.toString();

        out.print(result);
        out.close();
    }

// -------------------------- OTHER METHODS --------------------------
    private void writeDocument(@NotNull final StringBuffer out, @NotNull DocumentNode root) {

        out.append("<document>\n");
        writeStyles(out, root.getStyles());

        for (PageNode node : root.getChildren()) {
            writePage(out, node);
        }

        out.append("</document>");
    }

    private void writeLine(@NotNull final StringBuffer out, @NotNull LineNode line) {

        indent += indentationWidth;
        indent(out);

        if (line.findDominatingStyle().equals(Style.FORMULA)) {
            out.append("<formula>");
            out.append(getTextForNode(line));
            out.append("</formula>\n");
        } else {
            out.append("<line");
            out.append(" styleRef=\"").append(String.valueOf(line.findDominatingStyle().id)).append(
                "\"");

            if (Constants.VERBOSE_OUTPUT) {
                writeRectangle(out, line.getPos());
                out.append(">\n");

                for (WordNode word : line.getChildren()) {
                    writeWord(out, word);
                }

                out.append("</line>\n");
            } else {
                out.append(">");
                out.append(getTextForNode(line));
                out.append("</line>\n");
            }
        }

        indent -= indentationWidth;
    }

    private void indent(final StringBuffer out) {

        for (int i = 0; i < indent; i++) {
            out.append(" ");
        }
    }

    private void writePage(@NotNull StringBuffer out, @NotNull PageNode page) {

        out.append("<page");
        out.append(" num=\"").append(Integer.toString(page.getPageNumber())).append("\"");

        if (Constants.VERBOSE_OUTPUT) {
            writeRectangle(out, page.getPos());
        }

        out.append(">\n");

        for (ParagraphNode paragraphNode : page.getChildren()) {
            writeParagraph(out, paragraphNode);
        }

        for (GraphicsNode graphicsNode : page.getGraphics()) {
            writeGraphic(out, graphicsNode);
        }

        out.append("</page>\n");
    }

    private void writeGraphic(final StringBuffer out, final GraphicsNode graphicsNode) {

        indent += indentationWidth;
        indent(out);
        out.append("<graphics");
        writeRectangle(out, graphicsNode.getPos());
        out.append(">\n");

        for (ParagraphNode paragraphNode : graphicsNode.getChildren()) {
            writeParagraph(out, paragraphNode);
        }

        indent(out);
        out.append("</graphic>\n");
        indent -= indentationWidth;
    }

    private void writeParagraph(@NotNull final StringBuffer out,
                                @NotNull final ParagraphNode paragraph) {

        indent += indentationWidth;
        indent(out);
        out.append("<paragraph");
        writeRectangle(out, paragraph.getPos());
        out.append(" seqno=\"").append(paragraph.getSeqNo()).append("\"");
        out.append(">\n");

        for (LineNode line : paragraph.getChildren()) {
            writeLine(out, line);
        }

        indent(out);
        out.append("</paragraph>\n");
        indent -= indentationWidth;
    }

    private void writeRectangle(@NotNull StringBuffer sb, @NotNull Rectangle pos) {

        sb.append(" x=\"").append(String.valueOf(pos.x)).append("\"");
        sb.append(" y=\"").append(String.valueOf(pos.y)).append("\"");
        sb.append(" w=\"").append(String.valueOf(pos.width)).append("\"");
        sb.append(" h=\"").append(String.valueOf(pos.height)).append("\"");
    }

    private void writeStyles(@NotNull final StringBuffer out, @NotNull List<Style> styles) {

        out.append("<styles>\n");

        /* output the styles sorted by id */
        Collections.sort(styles, sortStylesById);

        for (Style style : styles) {
            out.append("<style");
            out.append(" id=\"").append(String.valueOf(style.id)).append("\"");
            out.append(" font=\"").append(style.fontName).append("\"");
            out.append(" size=\"").append(String.valueOf(style.xSize)).append("\"");

            if (style.isItalic()) {
                out.append(" italic=\"true\"");
            }

            if (style.isMathFont()) {
                out.append(" math=\"true\"");
            }

            if (style.isBold()) {
                out.append(" bold=\"true\"");
            }

            out.append("/>\n");
        }

        out.append("</styles>\n");
    }

    private void writeWord(@NotNull final StringBuffer out, @NotNull WordNode word) {

        out.append("<word");
        out.append(" value=\"").append(getTextForNode(word)).append("\"");
        out.append(" styleRef=\"").append(String.valueOf(word.getStyle().id)).append("\" ");
        writeRectangle(out, word.getPos());
        out.append("/>\n");
    }

    private String getTextForNode(final AbstractNode text) {

        if (Constants.ESCAPE_HTML) {
            return escapeHtml(text.getText());
        }

        return text.getText();
    }
}
