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


package org.elacin.pdfextract.test;

import org.elacin.pdfextract.tree.DocumentNode;
import org.elacin.pdfextract.tree.LineNode;
import org.elacin.pdfextract.tree.PageNode;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA. User: elacin Date: May 9, 2010 Time: 12:31:16 AM To change this
 * template use File | Settings | File Templates.
 */
public class DocumentNavigator {

// -------------------------- PUBLIC STATIC METHODS --------------------------
    @NotNull
    public static ArrayList<LineNode> getLineNodes(@NotNull DocumentNode doc) {

        ArrayList<LineNode> ret = new ArrayList<LineNode>();

        for (PageNode pageNode : doc.getChildren()) {

//      for (LayoutRegionNode layoutRegionNode : pageNode.getChildren()) {
//          for (ParagraphNode paragraphNode : layoutRegionNode.getChildren()) {
//              ret.addAll(paragraphNode.getChildren());
//          }
//
//      }
        }

        return ret;
    }
}
