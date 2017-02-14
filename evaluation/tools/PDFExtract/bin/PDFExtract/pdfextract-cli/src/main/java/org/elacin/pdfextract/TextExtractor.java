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

import org.apache.commons.cli.*;
import org.apache.log4j.Logger;

import org.elacin.pdfextract.util.FileWalker;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA. User: elacin Date: Apr 8, 2010 Time: 6:50:25 AM To change this template
 * use File | Settings | File Templates.
 */
public class TextExtractor {

// ------------------------------ FIELDS ------------------------------
    public static final Logger log = Logger.getLogger("pdfextract-interface");
    private final File         destination;
    private final int          endPage;
    private final String       password;
    private final List<File>   pdfFiles;
    private final int          startPage;
    private final boolean      arc;

// --------------------------- CONSTRUCTORS ---------------------------
    public TextExtractor(final List<File> pdfFiles, final File destination, final int startPage,
                         final int endPage, final String password, final boolean arc) {

        this.pdfFiles    = pdfFiles;
        this.destination = destination;
        this.startPage   = startPage;
        this.endPage     = endPage;
        this.password    = password;
        this.arc         = arc;
    }

// -------------------------- STATIC METHODS --------------------------
    @NotNull
    protected static List<File> findAllPdfFilesUnderDirectory(final String filename) {

        List<File> ret  = new ArrayList<File>();
        File       file = new File(filename);

        if (!file.exists()) {
            throw new RuntimeException("File " + file + " does not exist");
        } else if (file.isDirectory()) {
            try {
                ret.addAll(FileWalker.getFileListing(file, ".pdf"));
            } catch (FileNotFoundException e) {
                log.error("Could not find file " + filename);
            }
        } else if (file.isFile()) {
            ret.add(file);
        }

        return ret;
    }

    @NotNull
    private static Options getOptions() {

        Options options = new Options();

        options.addOption("p", "password", true, "Password for decryption of document");
        options.addOption("s", "startpage", true, "First page to parse");
        options.addOption("e", "endpage", true, "Last page to parse");
        options.addOption("a", "arc", false, "Activate ARC extensions");

        return options;
    }

    @NotNull
    private static CommandLine parseParameters(final String[] args) {

        Options           options = getOptions();
        CommandLineParser parser  = new PosixParser();
        CommandLine       cmd     = null;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            log.error("Could not parse command line options: " + e.getMessage());
            usage();
            System.exit(1);
        }

        return cmd;
    }

    private static void usage() {

        new HelpFormatter().printHelp(TextExtractor.class.getSimpleName()
                                      + "<PDF file/dir> <XML output file/dir>", getOptions());
    }

// -------------------------- PUBLIC METHODS --------------------------
    public final void processFiles() {

        for (File pdfFile : pdfFiles) {
            try {
                ProcessDocument processDocument = new ProcessDocument(pdfFile, destination, password,
                                                      startPage, endPage, arc);

                processDocument.processFile();
            } catch (Exception e) {
                log.error("Error while processing PDF:", e);
            }
        }
    }

// --------------------------- main() method ---------------------------
    public static void main(String[] args) {

        CommandLine cmd = parseParameters(args);

        if (cmd.getArgs().length != 2) {
            usage();

            return;
        }

        int startPage = -1;

        if (cmd.hasOption("startpage")) {
            startPage = Integer.valueOf(cmd.getOptionValue("startpage"));
            log.info("LOG00140:Reading from page " + startPage);
        }

        int endPage = Integer.MAX_VALUE;

        if (cmd.hasOption("endpage")) {
            endPage = Integer.valueOf(cmd.getOptionValue("endpage"));
            log.info("LOG00150:Reading until page " + endPage);
        }

        String password = null;

        if (cmd.hasOption("password")) {
            password = cmd.getOptionValue("password");
        }


        final boolean arc = cmd.hasOption("arc");

        List<File> pdfFiles    = findAllPdfFilesUnderDirectory(cmd.getArgs()[0]);
        final File destination = new File(cmd.getArgs()[1]);

        if (pdfFiles.size() > 1) {

            /* if we have more than one input file, demand that the output be a directory */
            if (destination.exists()) {
                if (!destination.isDirectory()) {
                    log.error("When specifying multiple input files, output needs to be a directory");

                    return;
                }
            } else {
                if (!destination.mkdirs()) {
                    log.error("Could not create output directory");

                    return;
                }
            }
        }

        final TextExtractor textExtractor = new TextExtractor(pdfFiles, destination, startPage, endPage,
                                                password, arc);

        textExtractor.processFiles();
    }
}
