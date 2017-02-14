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



package org.elacin.pdfextract.datasource.graphics;

import org.jetbrains.annotations.NotNull;

import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.List;

import static java.awt.geom.PathIterator.*;

/**
 * Created by IntelliJ IDEA. User: elacin Date: 30.11.10 Time: 15.05 To change this template use
 * File | Settings | File Templates.
 */
public class PathSplitter {

// -------------------------- PUBLIC STATIC METHODS --------------------------
    @NotNull
    public static List<GeneralPath> splitPath(@NotNull final GeneralPath path) {

        List<GeneralPath>  subPaths = new ArrayList<GeneralPath>();
        GeneralPath        subPath  = new GeneralPath();
        final PathIterator iterator = path.getPathIterator(null);
        float[]            coords   = new float[6];

        while (!iterator.isDone()) {
            switch (iterator.currentSegment(coords)) {
            case SEG_MOVETO :
                subPath.moveTo(coords[0], coords[1]);

                break;
            case SEG_LINETO :
                subPath.lineTo(coords[0], coords[1]);

                break;
            case SEG_CLOSE :
                subPath.closePath();
                subPaths.add(subPath);
                subPath = new GeneralPath();

                break;
            case SEG_CUBICTO :
                subPath.curveTo(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]);

                break;
            case SEG_QUADTO :
                subPath.quadTo(coords[0], coords[1], coords[2], coords[3]);

                break;
            default :

                /* complain */
                assert false;
            }

            iterator.next();
        }

        /* some times legitimate paths are not closed, so add them anyway. */
        if (0.0 < subPath.getBounds().getWidth() * subPath.getBounds().getHeight()) {
            subPaths.add(subPath);
        }

        return subPaths;
    }
}
