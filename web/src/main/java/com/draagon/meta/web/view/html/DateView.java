/*
 * Copyright 2003 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta.web.view.html;

import com.draagon.meta.field.MetaField;
import com.draagon.meta.*;
import com.draagon.meta.attr.StringAttribute;
import com.draagon.meta.InvalidValueException;
import com.draagon.meta.registry.MetaDataRegistry;
import com.draagon.meta.registry.MetaDataType;
import com.draagon.meta.web.view.*;
import com.draagon.meta.web.util.Param;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Date;
import java.util.Calendar;

import javax.servlet.http.*;
import javax.servlet.jsp.*;

@MetaDataType(type = "view", subType = "date", description = "HTML date input view")
public class DateView extends MonthView {

    private static final Logger log = LoggerFactory.getLogger(DateView.class);
    private final static String ATTR_MINRANGE = "min.range";
    private final static String ATTR_MAXRANGE = "max.range";
    private final static String ATTR_EMPTYVALUE = "empty.value";
    //private final static String ATTR_USEZERO    = "usezero";

    // Unified registry self-registration
    static {
        try {
            MetaDataRegistry.getInstance().registerType(DateView.class, def -> def
                .type("view").subType("date")
                .inheritsFrom("view", "base")
                .optionalAttribute(ATTR_MINRANGE, "string")
                .optionalAttribute(ATTR_MAXRANGE, "string")
                .optionalAttribute(ATTR_EMPTYVALUE, "string")
                .description("HTML date input view")
            );

            log.debug("Registered DateView type with unified registry");
        } catch (Exception e) {
            log.error("Failed to register DateView type with unified registry", e);
        }
    }

    public DateView(String name) {
        super(name);
        addMetaAttr(StringAttribute.create(ATTR_MINRANGE, null));
        addMetaAttr(StringAttribute.create(ATTR_MAXRANGE, null));
        addMetaAttr(StringAttribute.create(ATTR_EMPTYVALUE, null));
    }

    public void getValue(HttpServletRequest request, Object o, String label)
            throws MetaDataException {
        int year = HtmlViewHelper.getIntValue(request, label + "-year");
        int mon = HtmlViewHelper.getIntValue(request, label + "-mon");
        int day = HtmlViewHelper.getIntValue(request, label + "-day");

        // If no options are selected, then set the date to null
        if (year == 0 && mon == 0 && day == 0) {
            // Validate the value before setting
            performValidation(o, null);

            // Set the date to null
            getMetaField(o).setDate(o, null);
            return;
        }

        // Validate the ranges
        if (year < 1000) {
            throw new InvalidValueException("error.selectYear");
        }

        if (mon < 1 || mon > 12) {
            throw new InvalidValueException("error.selectMonth");
        }

        if (day < 1 || day > 31) {
            throw new InvalidValueException("error.selectDate");
        }

        // Set the date
        Calendar c = Calendar.getInstance();
        try {
            // Set the month, day, year
            c.setTime(new Date(0));
            c.set(year, mon - 1, day, 0, 0, 0);

            // Verify the date was real
            if (c.get(Calendar.YEAR) != year
                    || c.get(Calendar.MONTH) != (mon - 1)
                    || c.get(Calendar.DAY_OF_MONTH) != day) {
                throw new InvalidValueException("Invalid date");
            }
        } catch (Exception e) {
            throw new InvalidValueException("Invalid date");
        }

        // Validate the value before setting
        performValidation(o, c.getTime());

        // Set the new date
        getMetaField(o).setDate(o, c.getTime());
    }

    /**
     * Draws a date view of the field
     */
    public void doView(PageContext page, Object o, String label, int mode, Map params)
            throws MetaDataException {
        MetaField mf = getMetaField(o);

        try {
            doDateView(page, label, mode, mf.getDate(o), params);
        } catch (IOException e) {
            log.error("Error displaying DateView for field [" + mf + "] with mode [" + modeToString(mode) + "]", e);
            throw new WebViewException("Cannot render DateView for field [" + mf + "] in mode [" + modeToString(mode) + "]: " + e.getMessage(), e);
        }
    }

    /**
     * Draws a date view of the field
     */
    protected void doDateView(PageContext page, String label, int mode, Date dateVal, Map params)
            throws IOException {
        Calendar c = Calendar.getInstance();

        String emptyString = ViewHelper.getStringParam(params, ATTR_EMPTYVALUE, null);
        if (emptyString == null && hasMetaAttr(ATTR_EMPTYVALUE)) {
            emptyString = (String) getMetaAttr(ATTR_EMPTYVALUE).getValue();
        }
        if (emptyString != null) {
            setEmptyString(emptyString);
        }

        boolean useZero = (emptyString != null); // ViewHelper.getBooleanParam( params, ATTR_USEZERO, false );

        int year = 0;
        int mon = 0;
        int day = 0;

        if (dateVal != null) {
            c.setTime(dateVal);

            year = c.get(Calendar.YEAR);
            mon = c.get(Calendar.MONTH) + 1;
            day = c.get(Calendar.DAY_OF_MONTH);
        } else if (!useZero) {
            year = c.get(Calendar.YEAR);
            mon = c.get(Calendar.MONTH) + 1;
            day = c.get(Calendar.DAY_OF_MONTH);
        }

        // Output the Date Views

        JspWriter out = page.getOut();
        if (mode == WebView.READ) {
            if (year == 0 && mon == 0 && day == 0) {
                out.print("[NO DATE DEFINED]");
            } else {
                doMonthView(page, mode, label + "-mon", mon, params);
                out.print("&nbsp;");

                doDayView(page, mode, label + "-day", day, params);

                if (year > 0) {
                    out.print("&nbsp;, ");
                    HtmlViewHelper.drawText(page, "" + year, params);
                }
            }
        } else if (mode == WebView.EDIT) {
            //hasEmptyValue = empty;
            out.print("<table border=0 cellspacing=0 cellpadding=0><tr><td>");
            doMonthView(page, mode, label + "-mon", mon, params);
            out.print("</td><td>");
            doDayView(page, mode, label + "-day", day, params);
            out.print("</td><td>");

            c = Calendar.getInstance();
            int defMin = c.get(Calendar.YEAR) - 10;
            int defMax = c.get(Calendar.YEAR) + 30;

            try {
                defMin = Integer.parseInt((String) getMetaAttr(ATTR_MINRANGE).getValue());
            } catch (Exception e) {
            }
            try {
                defMax = Integer.parseInt((String) getMetaAttr(ATTR_MAXRANGE).getValue());
            } catch (Exception e) {
            }

            int min = ViewHelper.getIntParam(params, ATTR_MINRANGE, defMin);
            int max = ViewHelper.getIntParam(params, ATTR_MAXRANGE, defMax);

            //ystem.out.println( "min: " + min + ", max: " + max );

            if (min > 0 || max > 0) {
                doYearView(page, mode, label + "-year", year, min, max, params);
            } else if (year > 0) {
                HtmlViewHelper.drawTextBox(page, label + "-year", "" + year, 4, 4, params);
            } else {
                HtmlViewHelper.drawTextBox(page, label + "-year", "", 4, 4, params);
            }

            out.print("</td></tr></table>");
        } else {
            HtmlViewHelper.drawHidden(page, params);
        }
    }

    public void doDayView(PageContext page, int mode, String label, int sel, Map params) throws IOException {
        if (mode == READ) {
            if (sel >= 0 && sel < 32) {
                HtmlViewHelper.drawText(page, "" + sel, params);
            }
        } else if (mode == EDIT) {
            Collection<Param> days = new ArrayList<Param>();

            if (getEmptyString() != null) {
                days.add(new Param(getEmptyString(), ""));
            }

            for (int i = 1; i < 32; i++) {
                days.add(new Param("" + i, "" + i));
            }

            String selStr = (sel <= 0 || sel > 31) ? "" : "" + sel;

            HtmlViewHelper.drawSelectBox(page, label, selStr, days, params);
        }
    }

    public void doYearView(PageContext page, int mode, String label, int year, int min, int max, Map params)
            throws IOException {
        if (mode == READ) {
            HtmlViewHelper.drawText(page, "" + year, params);
        } else if (mode == EDIT) {
            Collection<Param> years = new ArrayList<Param>();

            if (getEmptyString() != null) {
                years.add(new Param(getEmptyString(), ""));
            }

            for (int i = min; i <= max; i++) {
                years.add(new Param("" + i, "" + i));
            }

            String sel = (year < min) ? "" : "" + (year - min + 1);

            HtmlViewHelper.drawSelectBox(page, label, sel, years, params);
        } else if (mode == HIDE) {
            HtmlViewHelper.drawHidden(page, params);
        }
    }
}
