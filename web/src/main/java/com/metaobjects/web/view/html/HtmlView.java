/*
 * Copyright 2003-2004 Doug Mealing LLC dba Meta Objects. All Rights Reserved.
 *
 * This software is the proprietary information of Doug Mealing LLC dba Meta Objects.
 * Use is subject to license terms.
 */
package com.metaobjects.web.view.html;

import com.metaobjects.web.view.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class HtmlView extends WebView {

    private static final Logger log = LoggerFactory.getLogger(HtmlView.class);

    public HtmlView(String name) {
        super(name);
    }
}
