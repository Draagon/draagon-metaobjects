/*
 * Copyright 2003-2004 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta.web.view.html;

import com.draagon.meta.web.view.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class HtmlView extends WebView {

    private static final Logger log = LoggerFactory.getLogger(HtmlView.class);

    public HtmlView(String name) {
        super(name);
    }
}
