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

import org.elacin.pdfextract.tree.*;
import org.elacin.pdfextract.tree.Role;

import org.jetbrains.annotations.NotNull;

import org.tei_c.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import java.lang.String;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA. User: elacin Date: 14.01.11 Time: 17.02 To change this template use
 * File | Settings | File Templates.
 */
public class TEIOutput implements XMLWriter {

// ------------------------------ FIELDS ------------------------------
    private static final Logger log = Logger.getLogger(TEIOutput.class);

// ------------------------ INTERFACE METHODS ------------------------
// --------------------- Interface XMLWriter ---------------------
    public void writeTree(@NotNull DocumentNode root, File destination) {

        long      t0  = System.currentTimeMillis();
        final TEI tei = new TEI();

        addHeader(root, tei);

        final Text text = new Text();

        addFront(root, text);
        addBody(root, text);
        addBack(root, text);
        tei.setText(text);

        try {
            JAXBContext jaxbContext = JAXBContext.newInstance("org.tei_c");
            Marshaller  marshaller  = jaxbContext.createMarshaller();

            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(tei, new FileOutputStream(destination));
        } catch (JAXBException e) {
            log.warn("LOG01140:", e);

            return;
        } catch (FileNotFoundException e) {
            log.warn("LOG01120:", e);

            return;
        }

        long time = System.currentTimeMillis() - t0;

        if (log.isInfoEnabled()) {
            log.info("LOG01510:" + TEIOutput.class + " took " + time + "ms");
        }
    }

// -------------------------- OTHER METHODS --------------------------
    private void addAbstract(final DocumentNode root, @NotNull Front front) {

        if (root.getAbstractParagraph() == null) {
            return;
        }

        final P   p   = new P();
        final Div div = new Div().withType("abs");

        div.withMeetingsAndBylinesAndDatelines(new Head().withContent("Abstract"), p);

        for (LineNode lineNode : root.getAbstractParagraph().getChildren()) {
            addLineToContent(p.getContent(), lineNode);
        }

        front.withSetsAndProloguesAndEpilogues(div);
    }

    private void addBack(DocumentNode root, @NotNull Text text) {

        final Back back = new Back();

        /* references goes here */
        text.withIndicesAndSpenAndSpanGrps(back);
    }

    Div          currentDiv;
    Div1         currentDiv1;
    Div2         currentDiv2;
    int          divLevel;
    List<Object> currentContent;

    private void addBody(@NotNull DocumentNode root, @NotNull Text text) {

        final Body          body = new Body();
        List<ParagraphNode> prfs = new ArrayList<ParagraphNode>();

        for (PageNode pageNode : root.getChildren()) {
            prfs.addAll(pageNode.getChildren());
        }

        divLevel   = 0;
        currentDiv = new Div();
        body.withIndicesAndSpenAndSpanGrps(currentDiv);
        currentContent = currentDiv.getMeetingsAndBylinesAndDatelines();

        boolean createNewP = false;
        P       currentP   = new P();

        currentContent.add(currentP);

        for (ParagraphNode prf : prfs) {
            boolean isHead = false;

            if (prf.hasRole(Role.DIV1)) {
                divLevel    = 1;
                currentDiv1 = new Div1();
                body.withIndicesAndSpenAndSpanGrps(currentDiv1);
                currentContent = currentDiv1.getMeetingsAndBylinesAndDatelines();
                isHead         = true;
                createNewP     = true;
            } else if (prf.hasRole(Role.DIV2)) {
                divLevel    = 2;
                currentDiv2 = new Div2();
                currentDiv1.getMeetingsAndBylinesAndDatelines().add(currentDiv2);
                currentContent = currentDiv2.getMeetingsAndBylinesAndDatelines();
                isHead         = true;
                createNewP     = true;
            }

            if (prf.hasRole(Role.FOOTNOTE)) {
                LineNode firstLine = prf.getChildren().get(0);
                WordNode firstWord = firstLine.getChildren().get(0);
                Note     note      = new Note();

                firstLine.removeChild(firstWord);
                addParagraphToContent(note.getContent(), prf);
                note.withPlaces("below").withNS(firstWord.getText());

//                currentContent.add(note);
                body.withIndicesAndSpenAndSpanGrps(note);

                continue;
            }

            if (isHead) {
                LineNode firstLine = prf.getChildren().get(0);
                String   divName   = "sec" + firstLine.getChildren().get(0).getText();
                Head     head      = new Head().withId(divName);

                firstLine.removeChild(firstLine.getChildren().get(0));
                addParagraphToContent(head.getContent(), prf);
                currentContent.add(head);
            } else {
                if (createNewP) {
                    currentP = new P();
                    currentContent.add(currentP);
                    createNewP = false;
                }

                for (LineNode line : prf.getChildren()) {
                    boolean indented = line.isIndented();

                    if (!currentP.getContent().isEmpty() && indented) {
                        currentP = new P();
                        currentContent.add(currentP);
                    }

                    addLineToContent(currentP.getContent(), line);
                }
            }
        }

        for (PageNode pageNode : root.getChildren()) {
            for (GraphicsNode graphicsNode : pageNode.getGraphics()) {
                if (graphicsNode.getText().isEmpty()) {
                    continue;
                }

                Graphic g = new Graphic();
                P       p = new P();

                for (ParagraphNode paragraphNode : graphicsNode.getChildren()) {
                    addParagraphToContent(p.getContent(), paragraphNode);
                }

//                body.withIndicesAndSpenAndSpanGrps(new Figure().withHeadsAndPSAndAbs(p));
            }
        }

        text.withIndicesAndSpenAndSpanGrps(body);
    }

    private void addLineToContent(final List<Object> contentList, final LineNode line) {

        String content = line.getText();

        if (!contentList.isEmpty()) {
            String former = (String) contentList.get(contentList.size() - 1);

            if (former.endsWith("-")) {
                String combined = former.substring(0, former.length() - 1) + content;

                contentList.remove(contentList.size() - 1);
                contentList.add(combined);

                return;
            }
        }

        contentList.add(content);
    }

    private void addParagraphToContent(final List<Object> content, final ParagraphNode prf) {

        for (LineNode line : prf.getChildren()) {
            addLineToContent(content, line);
        }
    }

    private void addFront(DocumentNode root, @NotNull Text text) {

        final Front front = new Front();

        addAbstract(root, front);
        text.withIndicesAndSpenAndSpanGrps(front);
    }

    private void addHeader(DocumentNode root, @NotNull TEI tei) {

        ParagraphNode title1 = root.getTitle();

        if (title1 == null) {
            return;
        }

        final TeiHeader header   = new TeiHeader();
        final FileDesc  fileDesc = new FileDesc();

        /* title, author and editor */
        final TitleStmt titleStmt = new TitleStmt();
        final Title     title     = new Title();

        for (LineNode lineNode : title1.getChildren()) {
            title.withContent(lineNode.getText());
        }

        titleStmt.withTitles(title);
        fileDesc.setTitleStmt(titleStmt);
        header.setFileDesc(fileDesc);
        tei.setTeiHeader(header);
    }
}
