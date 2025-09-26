/*
 * Copyright 2003 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta.web.view.html;

import com.draagon.meta.attr.StringAttribute;
import com.draagon.meta.field.MetaField;
import com.draagon.meta.util.MetaDataUtil;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.MetaDataNotFoundException;
import com.draagon.meta.*;
import com.draagon.meta.registry.MetaDataRegistry;
import com.draagon.meta.web.view.ViewHelper;
import com.draagon.meta.web.view.WebView;
import com.draagon.meta.web.view.WebViewException;
import static com.draagon.meta.view.MetaView.TYPE_VIEW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.jsp.JspWriter;
import jakarta.servlet.jsp.PageContext;
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

    private static final Logger log = LoggerFactory.getLogger(HotLinkView.class);
    public final static String ATTR_LINKCLASS = "linkClass";
    public final static String ATTR_URL = "url";

    /**
     * Register HotLinkView type with registry.
     * Called by WebMetaDataProvider during service discovery.
     */
    public static void registerTypes(MetaDataRegistry registry) {
        registry.registerType(HotLinkView.class, def -> def
            .type(TYPE_VIEW).subType("hotlink")
            .inheritsFrom("view", "base")
            .optionalAttribute(ATTR_LINKCLASS, StringAttribute.SUBTYPE_STRING)
            .optionalAttribute(ATTR_URL, StringAttribute.SUBTYPE_STRING)
            .description("HTML hotlink view")
        );
        // Registered HotLinkView type with unified registry
    }

    public HotLinkView(String name) {
        super(name);
    }

    public void doView(PageContext page, Object o, String label, int mode, Map<String, String> params)
            throws MetaDataException {
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
            def = (String) getMetaAttr("linkClass").getValue();
        } catch (MetaDataNotFoundException e) {
        }

        return ViewHelper.getStringParam(params, "linkClass", def);
    }

    /**
     * Renders a hotlink view
     */
    protected void doHotLinkView(PageContext page, Object o, String label, int mode, Map params)
            throws IOException, MetaDataException {
        String link = createLink(page, o, mode, params);
        String val = getMetaField(o).getString(o);

        if (link == null && val == null) {
            return;
        }

        if (!hasMetaAttr("viewRef")) {
            HtmlViewHelper.drawLink(page, link, val, getLinkClass(params), params);
        } else {
            String viewRef = (String) getMetaAttr("viewRef").getValue();

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
            throws MetaDataException {
        String url = (String) getMetaAttr("url").getValue();

        if (url == null) {
            throw new MetaDataException("No 'url' attribute found for view [" + getName() + "] in field [" + getMetaField(o) + "]");
        }

        // If the URL starts with a "/" then prepend the Context Path
        if (url.startsWith("/")) {
            String context = ((HttpServletRequest) page.getRequest()).getContextPath();
            url = context + url;
        }

        MetaObject mc = MetaDataUtil.findMetaObject(o, this);

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
        // TODO: Replace URLConstructor with proper URL construction
        StringBuilder urlBuilder = new StringBuilder(url);
        if (!map.isEmpty()) {
            urlBuilder.append(url.contains("?") ? "&" : "?");
            boolean first = true;
            for (Object key : map.keySet()) {
                if (!first) urlBuilder.append("&");
                urlBuilder.append(key).append("=").append(map.get(key));
                first = false;
            }
        }
        return urlBuilder.toString();
    }

    /**
     * Retrieve the value for the field and place it into the object
     */
    @Override
    public void getValue(HttpServletRequest request, Object o, String label)
            throws MetaDataException {
        throw new MetaDataException("This is a read only view");
    }
}