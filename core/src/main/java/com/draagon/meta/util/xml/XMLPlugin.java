/*
 * Copyright 2003 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 *
 * Moved from draagon-framework-java to avoid the dependency with draagon-utilities.
 */
package com.draagon.meta.util.xml;

import java.io.IOException;
import java.io.InputStream;

import org.w3c.dom.Document;

/**
 * Meta Class loader for XML files
 */
public interface XMLPlugin
{
    public Document loadFromStream( InputStream is ) throws IOException;
}
