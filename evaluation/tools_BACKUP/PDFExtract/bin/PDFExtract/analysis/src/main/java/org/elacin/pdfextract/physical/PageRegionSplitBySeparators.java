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



package org.elacin.pdfextract.physical;

import org.apache.log4j.Logger;
import org.elacin.pdfextract.content.GraphicContent;
import org.elacin.pdfextract.content.PhysicalContent;
import org.elacin.pdfextract.content.PhysicalPageRegion;
import org.elacin.pdfextract.geom.Rectangle;
import org.elacin.pdfextract.physical.graphics.CategorizedGraphics;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by IntelliJ IDEA. User: elacin Date: 09.12.10 Time: 23.36 To change this template use
 * File | Settings | File Templates.
 */
public class PageRegionSplitBySeparators {

// ------------------------------ FIELDS ------------------------------
    private static final Logger log = Logger.getLogger(PageRegionSplitBySeparators.class);

// -------------------------- STATIC METHODS --------------------------
    static boolean splitRegionAtY(final PhysicalPageRegion r, final float splitAt) {

        Rectangle rpos  = r.getPos();
        boolean   ret   = true;
        Rectangle above = new Rectangle(rpos.x, 0.0f, rpos.width + 1, splitAt);

        ret &= r.extractSubRegionFromBound(above, false);

        Rectangle under = new Rectangle(rpos.x, splitAt, rpos.width + 1, rpos.endY - splitAt);

        ret &= r.extractSubRegionFromBound(under, false);

        return ret;
    }

    /**
     * Divide the region r by horizontal and vertical separators
     *
     * @param r
     * @param graphics
     */
    static void splitRegionBySeparators(@NotNull PhysicalPageRegion r,
            @NotNull CategorizedGraphics graphics) {

        List<GraphicContent> toRemove = new ArrayList<GraphicContent>();

        for (GraphicContent hsep : graphics.getHorizontalSeparators()) {
            float splitAt = hsep.getPos().y;

            if ((hsep.getPos().width < r.getPos().width * 0.6f) || (splitAt <= 0)
                    || (splitAt >= r.getPos().endY)) {
                continue;
            }

            /* search to see if this separator does not intersect with anything */
            Rectangle search                 = new Rectangle(0, splitAt, r.getPos().width,
                                                   hsep.getPos().height);
            final List<PhysicalContent> list = r.findContentsIntersectingWith(search);

            if (list.contains(hsep)) {
                list.remove(hsep);
            }

            for (Iterator<PhysicalContent> iterator = list.iterator(); iterator.hasNext(); ) {
                final PhysicalContent content = iterator.next();

                if (hsep.getPos().contains(content.getPos())) {
                    iterator.remove();
                }
            }

            if (list.isEmpty()) {
                if (log.isInfoEnabled()) {
                    log.info("LOG00880:split/hsep: splitting " + hsep);
                }

                splitRegionAtY(r, splitAt);

                // r.addContent(hsep);
                toRemove.add(hsep);
            } else {

                /* just add this for now */
                r.addContent(hsep);
            }
        }

        graphics.getHorizontalSeparators().removeAll(toRemove);

        // TODO: do something with vsep
        for (GraphicContent vsep : graphics.getVerticalSeparators()) {
            r.addContent(vsep);
        }
    }
}
