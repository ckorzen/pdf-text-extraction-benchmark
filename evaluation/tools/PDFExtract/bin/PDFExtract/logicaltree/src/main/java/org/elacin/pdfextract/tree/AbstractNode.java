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



package org.elacin.pdfextract.tree;

import org.apache.log4j.Logger;

import org.elacin.pdfextract.content.StyledText;
import org.elacin.pdfextract.geom.HasPositionAbstract;
import org.elacin.pdfextract.style.Style;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.EnumSet;

/**
 * Created by IntelliJ IDEA. User: elacin Date: Mar 23, 2010 Time: 7:44:33 AM To change this
 * template use File | Settings | File Templates.
 */
public abstract class AbstractNode<ParentType extends AbstractParentNode> extends HasPositionAbstract
        implements StyledText {

// ------------------------------ FIELDS ------------------------------
    protected static final Logger log   = Logger.getLogger(AbstractNode.class);
    @NotNull
    protected final EnumSet<Role> roles = EnumSet.noneOf(Role.class);
    @Nullable
    protected ParentType          parent;
    @Nullable
    protected DocumentNode        root;


// --------------------- GETTER / SETTER METHODS ---------------------
    @Nullable
    public ParentType getParent() {
        return parent;
    }

    @NotNull
    public Collection<Role> getRoles() {
        return roles;
    }


// -------------------------- PUBLIC METHODS --------------------------
    public void addRole(Role r) {
        log.info(r + ": " + this);
        roles.add(r);
    }

    @Nullable
    public PageNode getPage() {

        AbstractNode current = this;

        while (current != null) {
            if (current instanceof PageNode) {
                return (PageNode) current;
            }

            current = current.parent;
        }

        return null;
    }

    public abstract Style getStyle();

    public abstract String getText();

    public boolean hasRole(Role r) {
        return roles.contains(r);
    }

    public boolean hasRole() {
        return !roles.isEmpty();
    }

// -------------------------- OTHER METHODS --------------------------
    protected abstract void invalidateThisAndParents();
}
