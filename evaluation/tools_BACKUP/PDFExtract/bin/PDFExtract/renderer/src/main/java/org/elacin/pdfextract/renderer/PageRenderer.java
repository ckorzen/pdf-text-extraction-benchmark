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



package org.elacin.pdfextract.renderer;

import org.apache.log4j.Logger;
import org.elacin.pdfextract.Constants;
import org.elacin.pdfextract.content.PhysicalPage;
import org.elacin.pdfextract.content.PhysicalPageRegion;
import org.elacin.pdfextract.content.WhitespaceRectangle;
import org.elacin.pdfextract.datasource.DocumentContent;
import org.elacin.pdfextract.datasource.PDFSource;
import org.elacin.pdfextract.datasource.PageContent;
import org.elacin.pdfextract.datasource.RenderedPage;
import org.elacin.pdfextract.geom.HasPosition;
import org.elacin.pdfextract.geom.Rectangle;
import org.elacin.pdfextract.tree.*;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static org.elacin.pdfextract.Constants.*;

/**
 * Created by IntelliJ IDEA. User: elacin Date: Jun 17, 2010 Time: 5:02:09 AM To change this
 * template use File | Settings | File Templates.
 */
public class PageRenderer {

// ------------------------------ FIELDS ------------------------------
    private static final Logger log               = Logger.getLogger(PageRenderer.class);
    @NotNull
    private static final Color  TRANSPARENT_WHITE = new Color(255, 255, 255, 0);
    @NotNull
    private static final Color  DONT_DRAW         = new Color(254, 254, 254, 0);
    private int                 paragraphCounter  = 1;
    private final DocumentNode  documentNode;
    private Graphics2D          graphics;
    private final int           resolution;
    private final PDFSource     source;
    private float               xScale;
    private float               yScale;

// --------------------------- CONSTRUCTORS ---------------------------
    public PageRenderer(final PDFSource source, final DocumentNode documentNode, final int resolution) {

        this.source       = source;
        this.documentNode = documentNode;
        this.resolution   = resolution;
    }

// -------------------------- STATIC METHODS --------------------------
    static Color getColorForObject(@NotNull Object o) {

        if (o.getClass().equals(WhitespaceRectangle.class)) {
            WhitespaceRectangle w = (WhitespaceRectangle) o;

            if (w.getScore() == 1000) {
                if (RENDER_COLUMNS) {
                    return Color.RED;
                }
            } else if ((w.getScore() == 500) && RENDER_COLUMN_CANDIDATES) {
                return Color.RED;
            } else if (Constants.RENDER_WHITESPACE) {
                return Color.BLACK;
            }
        }

        if (RENDER_PARAGRAPHS && o.getClass().equals(ParagraphNode.class)) {
            return Color.YELLOW;
        }

        if (RENDER_GRAPHIC_NODES && o.getClass().equals(GraphicsNode.class)) {
            return Color.MAGENTA;
        }

        if (RENDER_LINE_NODES && o.getClass().equals(LineNode.class)) {
            return Color.BLUE;
        }

        if (RENDER_WORD_NODES && o.getClass().equals(WordNode.class)) {
            return Color.ORANGE;
        }

        if (RENDER_PAGE_REGIONS && o.getClass().equals(PhysicalPageRegion.class)) {
            return Color.RED;
        }

        return DONT_DRAW;
    }

// -------------------------- PUBLIC METHODS --------------------------
    @NotNull
    public BufferedImage renderToFile(final int pageNum, File outputFile) {

        final PageNode pageNode = documentNode.getPageNumber(pageNum);

        if (pageNode == null) {
            throw new RuntimeException("Renderer: No contents found for page " + pageNum + ".");
        }

        long t0 = System.currentTimeMillis();

        /* first have PDFBox draw the pdf to a BufferedImage */
        final BufferedImage image;

        if (RENDER_REAL_PAGE) {
            final RenderedPage renderedPage = source.renderPage(pageNum);

            image  = renderedPage.getRendering();
            xScale = renderedPage.getXScale();
            yScale = renderedPage.getYScale();
        } else {
            Rectangle             dims       = null;
            final DocumentContent docContent = source.readPages();

            for (PageContent page : docContent.getPages()) {
                if (page.getPageNum() == pageNum) {
                    dims = page.getDimensions();
                }
            }

            assert dims != null;

            float         scaling  = resolution / (float) RENDER_DPI;
            int           widthPx  = Math.round(dims.width * scaling);
            int           heightPx = Math.round(dims.height * scaling);
            BufferedImage ret      = new BufferedImage(widthPx, heightPx, BufferedImage.TYPE_INT_ARGB);
            Graphics2D    g        = (Graphics2D) ret.getGraphics();

            g.setBackground(TRANSPARENT_WHITE);
            g.clearRect(0, 0, ret.getWidth(), ret.getHeight());
            g.scale(scaling, scaling);
            image = ret;

            // yScale = heightPx / pageNode.getPos().getHeight();
            yScale = scaling;
            xScale = scaling;

            // xScale = widthPx / pageNode.getPos().getWidth();
            // xScale = 1.0f;
            // yScale = 1.0f;
        }

        /* then draw our information on top */
        graphics = image.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        /* render graphics and whitespace, both are left in the physical page */
        final PhysicalPage physicalPage = pageNode.getPhysicalPage();

        if (physicalPage != null) {
            drawRegionAndWhitespace(physicalPage.getMainRegion(), 0);
        }

        drawTree(pageNode);

        /* write to file */
        try {
            ImageIO.write(image, "png", outputFile);
        } catch (IOException e) {
            log.warn("Error while writing rendered image to file", e);
        }

        log.info(String.format("LOG00180:Rendered page %d in %d ms", pageNum,
                               System.currentTimeMillis() - t0));

        return image;
    }

// -------------------------- OTHER METHODS --------------------------
    @SuppressWarnings({ "NumericCastThatLosesPrecision" })
    private void drawRectangle(@NotNull final HasPosition object) {

        final int       ALPHA = 60;
        final Rectangle pos   = object.getPos();
        final Color     color = getColorForObject(object);

        if (DONT_DRAW.equals(color)) {
            return;
        }

        graphics.setColor(color);

        final int x      = (int) ((float) pos.x * xScale);
        final int width  = (int) ((float) pos.width * xScale);
        int       y      = (int) ((float) pos.y * yScale);
        final int height = (int) ((float) pos.height * yScale);

        graphics.drawRect(x, y, width, height);

        if (true) {
            graphics.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), ALPHA));
            graphics.fillRect(x, y, width, height);
        }
    }

    private void drawParagraphNumber(@NotNull HasPosition p) {

        graphics.setFont(new Font("TimesRoman", Font.BOLD | Font.ITALIC, 20));

        final float x = p.getPos().x * xScale;
        final float y = p.getPos().y * yScale;

        graphics.setColor(Color.red.brighter().brighter());

        // graphics.setBackground(Color.red.brighter().brighter());
        graphics.fillRect((int) x, (int) y, 30, 25);

        // graphics.setBackground(Color.black);
        graphics.setColor(Color.black);
        graphics.drawString(String.valueOf(paragraphCounter), x, y + 20);
        paragraphCounter++;
    }

    private void drawRegionAndWhitespace(@NotNull PhysicalPageRegion region, final int nesting) {

        for (WhitespaceRectangle o : region.getWhitespace()) {
            drawRectangle(o);
        }

        if (Constants.RENDER_PAGE_REGIONS) {
            Rectangle   pos = region.getPos();
            final Color color;    // = Color.DARK_GRAY;

            switch (nesting) {
            case 0 :
                color = Color.BLACK;

                break;
            case 1 :
                color = Color.BLUE;

                break;
            case 2 :
                color = Color.RED;

                break;
            case 3 :
                color = Color.MAGENTA;

                break;
            default :
                color = Color.GREEN;
            }

            Stroke      oldStroke    = graphics.getStroke();
            final float dash1[]      = { (float) (1 + 3 * nesting) };
            final BasicStroke dashed = new BasicStroke(3.0f, BasicStroke.CAP_BUTT,
                                           BasicStroke.JOIN_MITER, 10.0f, dash1, 0.0f);

            graphics.setStroke(dashed);
            graphics.setColor(color);

            final int               x      = (int) ((float) pos.x * xScale);
            final int               width  = (int) ((float) pos.width * xScale);
            int                     y      = (int) ((float) pos.y * yScale);
            final int               height = (int) ((float) pos.height * yScale);
            RoundRectangle2D.Double r      = new RoundRectangle2D.Double(x, y, width, height, 20.0,
                                                 20.0);

            graphics.draw(r);
            graphics.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 40));
            graphics.fill(r);
            graphics.setStroke(oldStroke);
        }

        for (PhysicalPageRegion subRegion : region.getSubregions()) {
            drawRegionAndWhitespace(subRegion, nesting + 1);
        }
    }

    private void drawTree(@NotNull PageNode page) {

        paragraphCounter = 1;

        for (GraphicsNode graphicsNode : page.getGraphics()) {

            // for (ParagraphNode paragraph : graphicsNode.getChildren()) {
            // for (LineNode lineNode : paragraph.getChildren()) {
            // for (WordNode wordNode : lineNode.getChildren()) {
            // drawRectangle(wordNode);
            // }
            // drawRectangle(lineNode);
            // }
            // drawRectangle(paragraph);
            // }
            drawRectangle(graphicsNode);

            // if (RENDER_PARAGRAPH_NUMBERS) {
            // drawParagraphNumber(graphicsNode);
            // }
        }

        for (ParagraphNode paragraphNode : page.getChildren()) {
            for (LineNode lineNode : paragraphNode.getChildren()) {
                for (WordNode wordNode : lineNode.getChildren()) {
                    drawRectangle(wordNode);
                }

                drawRectangle(lineNode);
            }

            drawRectangle(paragraphNode);

            if (RENDER_PARAGRAPH_NUMBERS) {
                drawParagraphNumber(paragraphNode);
            }
        }
    }
}
