
/*
 * ---------------
 *
 * This file is derivative work.
  *
 * Copyright 2010-2011 Øyvind Berg (elacin [at] gmail.com)
 *
 * ---------------- Original notice: ----------------
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 *
 */

package org.elacin.pdfextract.datasource.pdfbox;

import org.apache.fontbox.util.BoundingBox;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdfviewer.PageDrawer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDMatrix;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType3Font;
import org.apache.pdfbox.util.Matrix;
import org.apache.pdfbox.util.TextNormalize;
import org.apache.pdfbox.util.TextPosition;
import org.elacin.pdfextract.Constants;
import org.elacin.pdfextract.content.PhysicalText;
import org.elacin.pdfextract.datasource.DocumentContent;
import org.elacin.pdfextract.datasource.PageContent;
import org.elacin.pdfextract.datasource.graphics.DrawingSurface;
import org.elacin.pdfextract.datasource.graphics.DrawingSurfaceImpl;
import org.elacin.pdfextract.geom.MathUtils;
import org.elacin.pdfextract.geom.Rectangle;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.util.*;
import java.util.List;

import static org.elacin.pdfextract.Constants.USE_EXISTING_WHITESPACE;

public class PDFBoxIntegration extends PageDrawer {

// ------------------------------ FIELDS ------------------------------
    private static final Logger  log           = Logger.getLogger(PDFBoxIntegration.class);
    @NotNull
    private static final byte[]  SPACE_BYTES   = { (byte) 32 };
    private static final boolean NO_DESCENDERS = true;

    /**
     * page state
     */
    @NotNull
    protected final DrawingSurface graphicsDrawer = new DrawingSurfaceImpl();

/* i couldnt find this information anywhere, so calculate it and cache it here */
    @NotNull
    protected Map<PDFont, Boolean> areFontsMonospaced = new HashMap<PDFont, Boolean>();

/* The normalizer is used to remove text ligatures/presentation forms and to correct
the direction of right to left text, such as Arabic and Hebrew. */
    @NotNull
    private final TextNormalize       normalize                        = new TextNormalize("UTF-8");
    @NotNull
    private final List<ETextPosition> charactersForPage                = new ArrayList<ETextPosition>();
    @NotNull
    private final Map<String, List<TextPosition>> characterListMapping = new HashMap<String,
                                                                             List<TextPosition>>();
    private BasicStroke basicStroke;
    private int         currentPageNo;

    /**
     * pdfbox state
     */

/* used to filter out text which is written several times to create a bold effect */
    private final PDDocument doc;

    /**
     * document state
     */
    public DocumentContent docContent;
    private final int      endPage;
    public Fonts           fonts;
    public float           rotation;
    private final int      startPage;

// --------------------------- CONSTRUCTORS ---------------------------
    public PDFBoxIntegration(final PDDocument doc, final int startPage, final int endPage)
            throws IOException {

        this.doc       = doc;
        this.startPage = startPage;
        this.endPage   = endPage;
    }

// ------------------------ OVERRIDING METHODS ------------------------
    @Override
    public void drawImage(Image awtImage, AffineTransform at) {

        final Shape currentClippingPath = getGraphicsState().getCurrentClippingPath();

        graphicsDrawer.drawImage(awtImage, at, currentClippingPath);
    }

    @Override
    public void drawPage(final Graphics g, final PDPage p, final Dimension pageDimension)
            throws IOException {
        super.drawPage(g, p, pageDimension);
    }

    @Override
    public void fillPath(int windingRule) throws IOException {

        Color currentColor = getGraphicsState().getNonStrokingColor().getJavaColor();

        getLinePath().setWindingRule(windingRule);

        final Shape currentClippingPath = getGraphicsState().getCurrentClippingPath();

        graphicsDrawer.fill(getLinePath(), currentColor, currentClippingPath);
        getLinePath().reset();
    }

    @NotNull
    @Override
    public Graphics2D getGraphics() {
        throw new RuntimeException("PDFBoxSource does not have Graphics2D");
    }

    @Override
    public BasicStroke getStroke() {
        return basicStroke;
    }

    /**
     * Old version
     */
    public void processEncodedText(@NotNull byte[] string) throws IOException {

        /*
         *  Note on variable names.  There are three different units being used
         *     in this code.  Character sizes are given in glyph units, text locations
         *     are initially given in text units, and we want to save the data in
         *     display units. The variable names should end with Text or Disp to
         *     represent if the values are in text or disp units (no glyph units are saved).
         */
        final float fontSizeText          = getGraphicsState().getTextState().getFontSize();
        final float horizontalScalingText = getGraphicsState().getTextState()
                                                .getHorizontalScalingPercent() / 100f;

        // float verticalScalingText = horizontalScaling;//not sure if this is right but what else to
        // do???
        final float riseText             = getGraphicsState().getTextState().getRise();
        final float wordSpacingText      = getGraphicsState().getTextState().getWordSpacing();
        final float characterSpacingText = getGraphicsState().getTextState().getCharacterSpacing();

        /*
         *  We won't know the actual number of characters until
         * we process the byte data(could be two bytes each) but
         * it won't ever be more than string.length*2(there are some cases
         * were a single byte will result in two output characters "fi"
         */
        final PDFont font = getGraphicsState().getTextState().getFont();

        /*
         *  This will typically be 1000 but in the case of a type3 font this might be a different
         * number
         */
        final float glyphSpaceToTextSpaceFactor;

        if (font instanceof PDType3Font) {
            PDMatrix fontMatrix         = font.getFontMatrix();
            float    fontMatrixXScaling = fontMatrix.getValue(0, 0);

            glyphSpaceToTextSpaceFactor = 1.0f / fontMatrixXScaling;
        } else {
            glyphSpaceToTextSpaceFactor = /* 1.0f / */ 1000f;
        }

        float spaceWidthText = 0.0F;

        try {
            spaceWidthText = (font.getFontWidth(SPACE_BYTES, 0, 1) / glyphSpaceToTextSpaceFactor);
        } catch (Throwable exception) {
            log.warn(exception, exception);
        }

        if (spaceWidthText == 0.0F) {
            spaceWidthText = (font.getAverageFontWidth() / glyphSpaceToTextSpaceFactor);
            spaceWidthText *= .80f;
        }

        /* Convert textMatrix to display units */
        final Matrix initialMatrix = new Matrix();

        initialMatrix.setValue(0, 0, 1.0F);
        initialMatrix.setValue(0, 1, 0.0F);
        initialMatrix.setValue(0, 2, 0.0F);
        initialMatrix.setValue(1, 0, 0.0F);
        initialMatrix.setValue(1, 1, 1.0F);
        initialMatrix.setValue(1, 2, 0.0F);
        initialMatrix.setValue(2, 0, 0.0F);
        initialMatrix.setValue(2, 1, riseText);
        initialMatrix.setValue(2, 2, 1.0F);

        final Matrix  ctm                         = getGraphicsState().getCurrentTransformationMatrix();
        final Matrix  dispMatrix                  = initialMatrix.multiply(ctm);
        Matrix        textMatrixStDisp            = getTextMatrix().multiply(dispMatrix);
        final float   xScaleDisp                  = textMatrixStDisp.getXScale();
        final float   yScaleDisp                  = textMatrixStDisp.getYScale();
        final float   spaceWidthDisp              = spaceWidthText * xScaleDisp * fontSizeText;
        final float   wordSpacingDisp             = wordSpacingText * xScaleDisp * fontSizeText;
        float         maxVerticalDisplacementText = 0.0F;
        StringBuilder characterBuffer             = new StringBuilder(string.length);
        int           codeLength                  = 1;

        for (int i = 0; i < string.length; i += codeLength) {

            // Decode the value to a Unicode character
            codeLength = 1;

            String c = font.encode(string, i, codeLength);

            if ((c == null) && (i + 1 < string.length)) {

                // maybe a multibyte encoding
                codeLength++;
                c = font.encode(string, i, codeLength);
            }

            c = inspectFontEncoding(c);

            // todo, handle horizontal displacement
            // get the width and height of this character in text units
            float fontWidth = font.getFontWidth(string, i, codeLength) *0.95f;

            if (fontWidth == 0.0f) {
                fontWidth = spaceWidthDisp;
            }

            float characterHorizontalDisplacementText = (fontWidth / glyphSpaceToTextSpaceFactor);

            maxVerticalDisplacementText = Math.max(maxVerticalDisplacementText,
                    font.getFontHeight(string, i, codeLength) / glyphSpaceToTextSpaceFactor);

            if (maxVerticalDisplacementText <= 0.0f) {
                maxVerticalDisplacementText = font.getFontBoundingBox().getHeight()
                                              / glyphSpaceToTextSpaceFactor;
            }

            /**
             * PDF Spec - 5.5.2 Word Spacing
             *
             * Word spacing works the same was as character spacing, but applies
             * only to the space character, code 32.
             *
             * Note: Word spacing is applied to every occurrence of the single-byte
             * character code 32 in a string.  This can occur when using a simple
             * font or a composite font that defines code 32 as a single-byte code.
             * It does not apply to occurrences of the byte value 32 in multiple-byte
             * codes.
             *
             * RDD - My interpretation of this is that only character code 32's that
             * encode to spaces should have word spacing applied.  Cases have been
             * observed where a font has a space character with a character code
             * other than 32, and where word spacing (Tw) was used.  In these cases,
             * applying word spacing to either the non-32 space or to the character
             * code 32 non-space resulted in errors consistent with this interpretation.
             */
            float spacingText = characterSpacingText;

            if ((string[i] == (byte) 0x20) && (codeLength == 1)) {
                spacingText += wordSpacingText;
            }

            /*
             *  The text matrix gets updated after each glyph is placed.  The updated
             *          version will have the X and Y coordinates for the next glyph.
             */
            Matrix glyphMatrixStDisp = getTextMatrix().multiply(dispMatrix);

            // The adjustment will always be zero.  The adjustment as shown in the
            // TJ operator will be handled separately.
            float adjustment = 0.0F;

            // TODO : tx should be set for horizontal text and ty for vertical text
            // which seems to be specified in the font (not the direction in the matrix).
            float tx = ((characterHorizontalDisplacementText - adjustment / glyphSpaceToTextSpaceFactor)
                        * fontSizeText) * horizontalScalingText;
            Matrix td = new Matrix();

            td.setValue(2, 0, tx);

            float ty = 0.0F;

            td.setValue(2, 1, ty);
            setTextMatrix(td.multiply(getTextMatrix()));

            Matrix glyphMatrixEndDisp = getTextMatrix().multiply(dispMatrix);
            float  sx                 = spacingText * horizontalScalingText;
            Matrix sd                 = new Matrix();

            sd.setValue(2, 0, sx);

            float sy = 0.0F;

            sd.setValue(2, 1, sy);
            setTextMatrix(sd.multiply(getTextMatrix()));

            float widthText = glyphMatrixEndDisp.getXPosition() - glyphMatrixStDisp.getXPosition();

            characterBuffer.append(c);

            Matrix textMatrixEndDisp            = glyphMatrixEndDisp;
            float totalVerticalDisplacementDisp = maxVerticalDisplacementText * fontSizeText
                                                  * yScaleDisp;

            try {
                final ETextPosition text = new ETextPosition(page, textMatrixStDisp, textMatrixEndDisp,
                                               totalVerticalDisplacementDisp, new float[] { widthText },
                                               spaceWidthDisp, characterBuffer.toString(), font,
                                               fontSizeText,
                                               (int) (fontSizeText * getTextMatrix().getXScale()),
                                               wordSpacingDisp);

                correctPosition(font, string, i, c, fontSizeText, glyphSpaceToTextSpaceFactor,
                                horizontalScalingText, codeLength, text);
                processTextPosition(text);
            } catch (Exception e) {
                log.warn("LOG00570:Error adding '" + characterBuffer + "': " + e.getMessage());
            }

            textMatrixStDisp = getTextMatrix().multiply(dispMatrix);
            characterBuffer.setLength(0);
        }
    }

    /**
     * This will process a TextPosition object and add the text to the list of characters on a page.
     * <p/> This method also filter out unwanted textpositions .
     *
     * @param text The text to process.
     */
    protected void processTextPosition(@NotNull TextPosition text_) {

        ETextPosition text = (ETextPosition) text_;

        if (text.getFontSize() == 0.0f) {
            if (log.isDebugEnabled()) {
                log.debug("LOG01100:ignoring text " + text.getCharacter() + " because fontSize is 0");
            }

            return;
        }

        if (!USE_EXISTING_WHITESPACE && "".equals(text.getCharacter().trim())) {
            return;
        }

        if (text.getCharacter().length() == 0) {
            if (log.isDebugEnabled()) {
                log.debug("LOG01110:Tried to render no text. wtf?");
            }

            return;
        }

//        java.awt.Rectangle javapos = new java.awt.Rectangle((int) text.getPos().x,
//                                         (int) text.getPos().y, (int) text.getPos().width,
//                                         (int) text.getPos().height);
//
//        if (!getGraphicsState().getCurrentClippingPath().intersects(javapos)) {
//            if (log.isDebugEnabled()) {
//                log.debug("LOG01090:Dropping text \"" + text.getCharacter() + "\" because it "
//                          + "was outside clipping path");
//            }
//
//            return;
//        }

        if (!textAlreadyRenderedAtSamePlace(text)) {
            if (log.isDebugEnabled()) {
                log.debug("LOG00770: ignoring text " + text.getCharacter()
                          + " because it seems to be rendered two times");
            }

            return;
        }

        if (!MathUtils.isWithinPercent(text.getDir(), rotation, 1)) {
            if (log.isDebugEnabled()) {
                log.debug("LOG00560: ignoring textposition " + text.getCharacter() + "because it has "
                          + "wrong rotation. TODO :)");
            }

            return;
        }

        /**
         * In the wild, some PDF encoded documents put diacritics (accents on
         * top of characters) into a separate Tj element.  When displaying them
         * graphically, the two chunks get overlayed.  With text output though,
         * we need to do the overlay. This code recombines the diacritic with
         * its associated character if the two are consecutive.
         */
        if (charactersForPage.isEmpty()) {
            charactersForPage.add(text);
        } else {

            /**
             * test if we overlap the previous entry. Note that we are making an assumption that we
             * need to only look back one TextPosition to find what we are overlapping.
             * This may not always be true.
             */
            TextPosition previousTextPosition = charactersForPage.get(charactersForPage.size() - 1);

            if (text.isDiacritic() && previousTextPosition.contains(text)) {
                previousTextPosition.mergeDiacritic(text, normalize);
            }

            /**
             * If the previous TextPosition was the diacritic, merge it into this one and remove it
             * from the list.
             */
            else if (previousTextPosition.isDiacritic() && text.contains(previousTextPosition)) {
                text.mergeDiacritic(previousTextPosition, normalize);
                charactersForPage.remove(charactersForPage.size() - 1);
                charactersForPage.add(text);
            } else {
                charactersForPage.add(text);
            }
        }
    }

    @Override
    public void setStroke(final BasicStroke newStroke) {
        basicStroke = newStroke;
    }

    @Override
    public void strokePath() throws IOException {

        Color       currentColor        = getGraphicsState().getStrokingColor().getJavaColor();
        final Shape currentClippingPath = getGraphicsState().getCurrentClippingPath();

        graphicsDrawer.strokePath(getLinePath(), currentColor, currentClippingPath);
        getLinePath().reset();
    }

// -------------------------- PUBLIC METHODS --------------------------
    public DocumentContent getContents() {
        return docContent;
    }

    public void processDocument() throws IOException {

        resetEngine();

        try {
            if (doc.isEncrypted()) {
                doc.decrypt("");
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not decrypt document", e);
        }

        currentPageNo = 0;
        docContent    = new DocumentContent();
        fonts         = new Fonts();

        for (final PDPage nextPage : (List<PDPage>) doc.getDocumentCatalog().getAllPages()) {
            PDStream contentStream = nextPage.getContents();

            currentPageNo++;

            if (contentStream != null) {
                COSStream contents = contentStream.getStream();

                processPage(nextPage, contents);
            }
        }

        docContent.setStyles(fonts.styles.values());
    }

// -------------------------- OTHER METHODS --------------------------
    private void correctPosition(@NotNull final PDFont fontObj, final byte[] string, final int i,
                                 @NotNull final String c, final float fontSizeText,
                                 final float glyphSpaceToTextSpaceFactor, float horizontalScalingText,
                                 final int codeLength, @NotNull final ETextPosition text)
            throws IOException {

        /**
         * Provide precise positioning of glyphs.
         *
         * There are several problems right which needs to be worked around:
         *
         * 1. Sometimes the PDF will make room for a glyph which belongs to a font with
         *      one or more very tall glyphs by jumping up on the page before drawing.
         *   Since most glyphs are (much) shorter than the tallest one, we need to make
         *      up for that by adjusting the Y coordinate back down. The distance which
         *      is jumped up is embedded in the PDF files, so there is no other way to go
         *      about this.
         *
         *  'beforeRoomForGlyph' is the position we were at before the jump back.
         *   Then we need to add spaceOverChar which is my estimate of where the glyph
         *      should begin. the result is kept in 'startY'
         *
         * 2. The default height we get might also be too big, so recalculate that based
         *      on character bounding
         *
         */
        final BoundingBox character = fontObj.getCharacterBoundingBox(string, i, codeLength);
        PDRectangle       fontBB    = null;

        try {
            fontBB = fontObj.getFontBoundingBox();
        } catch (RuntimeException e) {

            // ignore, this is frequently not implemented
        }

        final Rectangle pos    = text.getPos();
        float           adjust = (fontSizeText * horizontalScalingText) / glyphSpaceToTextSpaceFactor;

        adjust *= getTextMatrix().getXScale();

        final Rectangle newPos;

        if ((character != null) && (fontBB != null) && (character.getHeight() > 0.0f)
                && (fontBB.getHeight() > 0.0f)) {

            /* remove the upper and lower bounds filtered away by character */
            final float spaceUnderChar     = Math.min(fontBB.getLowerLeftY(), character.getLowerLeftY());
            final float spaceOverChar      = fontBB.getUpperRightY() - character.getUpperRightY();
            final float fontHeight         = fontBB.getHeight();

            /* calculate the upper left corner of the rendered glyph */
            float yStart = pos.endY - adjust * fontHeight;
            yStart += adjust * spaceOverChar;
            yStart -= adjust * spaceUnderChar;
            yStart -= pos.height;

            /* determine start X coordinate. */
            final float x;

            if (isMonoSpacedFont(fontObj)) {
                x = pos.x;
            } else {
//                float leftOfText = text.getX() - (adjust * fontBB.getWidth());
//
//                x = leftOfText + adjust * character.getLowerLeftX();
                x = pos.x;
            }

            /*
             *  It was much easier to write the word segmentation code with full font width,
             *   so lets keep that. I havent seen this causing any problems
             */
            float w = pos.width;

            /*
             *  Line segmentation code was obviously much easier by not having any descenders which
             *   can even overlap into the following line. Math symbols need to stay full length
             */
            final float characterHeight;

            if (NO_DESCENDERS && (Character.getType(c.charAt(0)) != (int) Character.MATH_SYMBOL)) {
                characterHeight = character.getUpperRightY();
            } else {
                characterHeight = character.getHeight();
            }

            float h = adjust * (characterHeight);

            /* correct if the NO_DESCENDERS hack made this character have no height*/
            if (NO_DESCENDERS && h < 0.1f){
                h = pos.height;
            }

            newPos = new Rectangle(x, yStart, w, h);
        } else {

            /*
             *  here we have a lot less information, so keep most of what was calculated. Just offset
             *   the Y coordinate
             */
            float h      = pos.height;
            float w      = pos.width;
            float startY = pos.y - h;// * 0.8f;

            if (fontObj instanceof PDType3Font) {

                /*
                 *  type 3 fonts typically have almost no information
                 * try to mitigate the damage by keeping them small.
                 */
                h      *= 0.5f;
                startY += h;    /* this is a _very_ quick and dirty hack */
            }

            newPos = new Rectangle(pos.x, startY, w, h);
        }

        if (log.isTraceEnabled()) {
            log.trace("LOG00730:Text " + c + ", " + "pos from " + pos + " to " + newPos);
        }

        text.setBaseLine(pos.y);
        text.setPos(newPos);
    }

    private void filterOutBadFonts(@NotNull List<ETextPosition> text) {

        final Map<PDFont, Integer> badCharsForStyle = new HashMap<PDFont, Integer>();
        final Map<PDFont, Integer> numCharsForStyle = new HashMap<PDFont, Integer>();

        for (TextPosition tp : text) {
            if (!badCharsForStyle.containsKey(tp.getFont())) {
                badCharsForStyle.put(tp.getFont(), 0);
                numCharsForStyle.put(tp.getFont(), 0);
            }

            char c = tp.getCharacter().charAt(0);

            if (Character.isISOControl(c)) {
                badCharsForStyle.put(tp.getFont(), badCharsForStyle.get(tp.getFont()) + 1);
            }

            numCharsForStyle.put(tp.getFont(), numCharsForStyle.get(tp.getFont()) + 1);
        }

        final Collection<PDFont> ignoredFonts = new ArrayList<PDFont>();

        for (Map.Entry<PDFont, Integer> pdFontIntegerEntry : numCharsForStyle.entrySet()) {
            int badChars   = badCharsForStyle.get(pdFontIntegerEntry.getKey());
            int totalChars = pdFontIntegerEntry.getValue();

            if (badChars > totalChars * 0.10f) {
                ignoredFonts.add(pdFontIntegerEntry.getKey());
                log.warn("LOG01060:Ignoring all content using font "
                         + pdFontIntegerEntry.getKey().getBaseFont() + " as it "
                         + "seems to be missing UTF-8 conversion information");
            }
        }

        for (Iterator<ETextPosition> iterator = text.iterator(); iterator.hasNext(); ) {
            TextPosition tp = iterator.next();

            if (ignoredFonts.contains(tp.getFont())) {
                iterator.remove();
            }
        }
    }

    private void filterOutControlCodes(@NotNull List<ETextPosition> text) {

        for (Iterator<ETextPosition> iterator = text.iterator(); iterator.hasNext(); ) {
            TextPosition tp = iterator.next();

            if (Character.isISOControl(tp.getCharacter().charAt(0))) {
                if (log.isDebugEnabled()) {
                    log.debug("Removing character \"" + tp.getCharacter() + "\"");
                }

                iterator.remove();
            }
        }
    }

    private boolean textAlreadyRenderedAtSamePlace(@NotNull final TextPosition text) {

        String             c                  = text.getCharacter();
        List<TextPosition> sameTextCharacters = characterListMapping.get(c);

        if (sameTextCharacters == null) {
            sameTextCharacters = new ArrayList<TextPosition>();
            characterListMapping.put(c, sameTextCharacters);

            return true;
        }

        /**
         * RDD - Here we compute the value that represents the end of the rendered
         * text.  This value is used to determine whether subsequent text rendered
         * on the same line overwrites the current text.
         *
         * We subtract any positive padding to handle cases where extreme amounts
         * of padding are applied, then backed off (not sure why this is done, but there
         * are cases where the padding is on the order of 10x the character width, and
         * the TJ just backs up to compensate after each character).  Also, we subtract
         * an amount to allow for kerning (a percentage of the width of the last
         * character).
         */
        boolean     suppressCharacter = false;
        final float tolerance         = (text.getWidth() / (float) c.length()) / 3.0f;

        for (TextPosition other : sameTextCharacters) {
            String otherChar = other.getCharacter();
            float  charX     = other.getX();
            float  charY     = other.getY();

            if ((otherChar != null) && MathUtils.isWithinVariance(charX, text.getX(), tolerance)
                    && MathUtils.isWithinVariance(charY, text.getY(), tolerance)) {
                suppressCharacter = true;
            }
        }

        boolean alreadyThere = true;

        if (!suppressCharacter) {
            sameTextCharacters.add(text);
            alreadyThere = false;
        }

        return !alreadyThere;
    }

    private boolean isMonoSpacedFont(@NotNull PDFont fontObj) {

        if (areFontsMonospaced.containsKey(fontObj)) {
            return areFontsMonospaced.get(fontObj);
        }

        List<Float> widths     = fontObj.getWidths();
        boolean     monospaced = true;

        if (widths == null) {
            monospaced = false;
        } else {
            final float firstWidth = widths.get(0);

            for (int i = 1; i < widths.size(); i++) {
                final float width = widths.get(i);

                if (!MathUtils.isWithinPercent(width, firstWidth, 1.0f)) {
                    monospaced = false;

                    break;
                }
            }
        }

        if (monospaced) {
            log.debug("LOG01080:Font " + fontObj.getBaseFont() + " is monospaced");
        }

        areFontsMonospaced.put(fontObj, monospaced);

        return monospaced;
    }

    /**
     * This will process the contents of a page.
     *
     * @param page    The page to process.
     * @param content The contents of the page.
     * @throws IOException If there is an error processing the page.
     */
    protected void processPage(@NotNull PDPage page, COSStream content) throws IOException {

        if ((currentPageNo >= startPage) && (currentPageNo <= endPage)) {

            /* show which page we are working on in the log */
            MDC.put("page", currentPageNo);
            charactersForPage.clear();
            characterListMapping.clear();
            pageSize = page.findCropBox().createDimension();
            rotation = (float) page.findRotation();

            /* this is used to 'draw' images on during pdf parsing */
            graphicsDrawer.clearSurface();
            setGraphicsState(null);
            resetEngine();
            processStream(page, page.findResources(), content);
            filterOutBadFonts(charactersForPage);

            /* filter out remaining definite bad characters */
            filterOutControlCodes(charactersForPage);

            List<PhysicalText> texts = new ArrayList<PhysicalText>(charactersForPage.size());

            for (ETextPosition tp : charactersForPage) {
                texts.add(tp.convertText(fonts));
            }

            final PDRectangle mediaBox = page.findMediaBox();
            Rectangle dimensions       = new Rectangle(mediaBox.getLowerLeftX(),
                                             mediaBox.getLowerLeftY(), mediaBox.getWidth(),
                                             mediaBox.getHeight());
            PageContent thisPage = new PageContent(texts, graphicsDrawer.getGraphicContents(),
                                       currentPageNo, dimensions);

            docContent.addPage(thisPage);
            MDC.remove("page");
        }
    }

@Override public void SHFill(final COSName ShadingName) throws IOException {
    super.SHFill(ShadingName);
}
}
