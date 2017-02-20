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



package org.elacin.pdfextract.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by IntelliJ IDEA. User: elacin Date: May 26, 2010 Time: 5:01:34 PM To change this
 * template use File | Settings | File Templates.
 */
public class FileWalker {

// -------------------------- PUBLIC STATIC METHODS --------------------------

    /**
     * Recursively walk a directory tree and return a List of all Files found; the List is sorted using
     * File.compareTo().
     *
     * @param aStartingDir is a valid directory, which can be read.
     * @param extension    asd
     */
    @NotNull
    public static List<File> getFileListing(@NotNull final File aStartingDir, final String extension)
            throws FileNotFoundException {

        validateDirectory(aStartingDir);

        List<File> result = getFileListingNoSort(aStartingDir, extension);

        Collections.sort(result);

        return result;
    }

// -------------------------- STATIC METHODS --------------------------
    @NotNull
    static List<File> getFileListingNoSort(@NotNull File aStartingDir, final String extension) {

        List<File> ret = new ArrayList<File>();

        for (File file : aStartingDir.listFiles()) {
            if (file.getName().endsWith(extension)) {
                ret.add(file);
            }

            if (file.isDirectory()) {
                ret.addAll(getFileListingNoSort(file, extension));
            }
        }

        return ret;
    }

    /**
     * Directory is valid if it exists, does not represent a file, and can be read.
     */
    static void validateDirectory(@Nullable final File aDirectory) throws FileNotFoundException {

        if (aDirectory == null) {
            throw new IllegalArgumentException("Directory should not be null.");
        }

        if (!aDirectory.exists()) {
            throw new FileNotFoundException("Directory does not exist: " + aDirectory);
        }

        if (!aDirectory.isDirectory()) {
            throw new IllegalArgumentException("Is not a directory: " + aDirectory);
        }

        if (!aDirectory.canRead()) {
            throw new IllegalArgumentException("Directory cannot be read: " + aDirectory);
        }
    }
}
