/*
 * Copyright 2003-2004 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta.web.view.html;

import com.draagon.meta.web.view.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class HtmlView extends WebView {

    private static Log log = LogFactory.getLog(HtmlView.class);

    public HtmlView(String name) {
        super(name);
    }
}
