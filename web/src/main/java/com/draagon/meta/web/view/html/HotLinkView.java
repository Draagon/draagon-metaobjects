/*
 * Copyright 2003 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta.web.view.html;

import com.draagon.meta.attr.AttributeDef;
import com.draagon.meta.field.MetaField;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.attr.MetaAttributeNotFoundException;
import com.draagon.meta.*;
import com.draagon.meta.web.view.ViewHelper;
import com.draagon.meta.web.view.WebView;
import com.draagon.meta.web.view.WebViewException;
import com.draagon.util.web.URLConstructor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>Renders a HotLink view.</p>
 * <p/>
 * <p>The possible MetaView attributes are the following: <ul> <li>url - The URL
 * pattern to use for the link; it can contain ${<name>} references which refer
 * to the field's to insert the value from</li> <li>linkClass - (Optional) The
 * class to assign the <a> html tag</li> </ul></p>
 */
public class HotLinkView extends HtmlView {

    private static Log log = LogFactory.getLog(HotLinkView.class);
    public final static String ATTR_LINKCLASS = "linkClass";
    public final static String ATTR_URL = "url";

    public HotLinkView(String name) {
        super(name);
        addAttributeDef(new AttributeDef(ATTR_LINKCLASS, String.class, false, "HTML style class for the link"));
        addAttributeDef(new AttributeDef(ATTR_URL, String.class, true, "The url for the hotlink"));
    }

    public void doView(PageContext page, Object o, String label, int mode, Map<String, String> params)
            throws MetaException {
        MetaField mf = getMetaField(o);
        //MetaClass mc = mf.getMetaClass();

        try {
            if (mode == READ) {
                doHotLinkView(page, o, label, mode, params);
            }
        } catch (IOException e) {
            log.error("Error displaying HotLinkView for field [" + mf + "] with mode [" + modeToString(mode) + "]", e);
            throw new WebViewException("Cannot render HotLinkView for field [" + mf + "] in mode [" + modeToString(mode) + "]: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieves the stylesheet class to use for the anchor html tag
     */
    public String getLinkClass(Map params) {
        String def = null;
        try {
            def = (String) getAttribute("linkClass");
        } catch (MetaAttributeNotFoundException e) {
        }

        return ViewHelper.getStringParam(params, "linkClass", def);
    }

    /**
     * Renders a hotlink view
     */
    protected void doHotLinkView(PageContext page, Object o, String label, int mode, Map params)
            throws IOException, MetaException {
        String link = createLink(page, o, mode, params);
        String val = getMetaField(o).getString(o);

        if (link == null && val == null) {
            return;
        }

        if (!hasAttribute("viewRef")) {
            HtmlViewHelper.drawLink(page, link, val, getLinkClass(params), params);
        } else {
            String viewRef = (String) getAttribute("viewRef");

            WebView mv = (WebView) getMetaField(o).getView(viewRef);

            String linkClass = getLinkClass(params);

            JspWriter out = page.getOut();

            // if ( text == null ) text = "&nbsp;";

            if (link != null) {
                out.print("<a");
                if (linkClass != null) {
                    out.print(" class=\"" + linkClass + "\"");
                }
                out.print(" href=\"" + link + "\">");
            }

            mv.doView(page, o, mode, params);
            // out.print( text );

            if (link != null) {
                out.print("</a>");
            }
        }
    }

    /**
     * Creates the hotlink by reading the url attribute and inserting the field
     * values
     */
    protected String createLink(PageContext page, Object o, int mode, Map params)
            throws MetaException {
        String url = (String) getAttribute("url");

        if (url == null) {
            throw new MetaException("No 'url' attribute found for view [" + getName() + "] in field [" + getMetaField(o) + "]");
        }

        // If the URL starts with a "/" then prepend the Context Path
        if (url.startsWith("/")) {
            String context = ((HttpServletRequest) page.getRequest()).getContextPath();
            url = context + url;
        }

        MetaObject mc = MetaDataLoader.findMetaObject(o);

        HashMap map = new HashMap();

        // Extract the possible fields for insertion
        int i = 0;
        while (true) {
            int j = url.indexOf("${", i);
            if (j >= 0) {
                int k = url.indexOf("}", j);
                if (k >= 0) {
                    // Increment to the next search segment
                    i = k + 1;

                    // Get the MetaField
                    String name = url.substring(j + 2, k);
                    MetaField mf = mc.getMetaField(name);

                    //get value out of data object using
                    String value = mf.getString(o);
                    if (value == null) {
                        value = "";
                    }

                    // Add the the HashMap
                    map.put(name, value);

                    continue;
                }
            }

            break;
        }//while

        // Construct the HotLink URL and return it
        return URLConstructor.constructURL(url, map);
    }

    /**
     * Retrieve the value for the field and place it into the object
     */
    @Override
    public void getValue(HttpServletRequest request, Object o, String label)
            throws MetaException {
        throw new MetaException("This is a read only view");
    }
}