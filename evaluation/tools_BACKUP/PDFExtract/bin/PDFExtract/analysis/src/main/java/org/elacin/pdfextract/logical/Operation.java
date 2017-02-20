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


package org.elacin.pdfextract.logical;

import org.elacin.pdfextract.tree.DocumentNode;

/**
 * Created by IntelliJ IDEA. User: elacin Date: Mar 23, 2010 Time: 3:09:37 AM To change this
 * template use File | Settings | File Templates.
 */
public interface Operation {

// -------------------------- PUBLIC METHODS --------------------------
    void doOperation(DocumentNode root, final DocumentMetadata metadata);
}
