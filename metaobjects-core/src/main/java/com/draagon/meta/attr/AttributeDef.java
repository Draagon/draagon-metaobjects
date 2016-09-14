/*
 * Copyright (c) 2012 Doug Mealing LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 */
package com.draagon.meta.attr;

/**
 * Attribute Definitions are used to specify expected attributes on MetaData.
 *
 * @author dmealing
 */
public class AttributeDef {

    final private String name;
    final private Class<?> clazz;
    final private boolean required;
    final private String description;

    /**
     * Attribute Options are used to specify expected attributes on MetaData.
     *
     * @param name The expected name of the attribute
     * @param type The expected class of the return type of the attribute (use
     * null for any)
     * @param required Whether the attribute is required
     * @param desc A description of the attribute
     */
    public AttributeDef(String name, Class<?> type, boolean required, String desc) {
        this.name = name;
        this.clazz = type;
        this.required = required;
        this.description = desc;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Class<?> getType() {
        return clazz;
    }

    public boolean isRequired() {
        return required;
    }
}
