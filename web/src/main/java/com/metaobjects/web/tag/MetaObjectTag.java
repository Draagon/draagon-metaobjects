/*
 * Copyright 2001 Doug Mealing LLC dba Meta Objects. All Rights Reserved.
 *
 * This software is the proprietary information of Doug Mealing LLC dba Meta Objects.
 * Use is subject to license terms.
 */

package com.metaobjects.web.tag;

import com.metaobjects.field.MetaField;
import com.metaobjects.util.MetaDataUtil;
import com.metaobjects.object.MetaObject;
import com.metaobjects.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.jsp.*;
import jakarta.servlet.jsp.tagext.*;

public class MetaObjectTag extends TagSupport
{
    private static final Logger log = LoggerFactory.getLogger(MetaObjectTag.class);

    private String var = null;
    //private String type = "java.lang.String";
    private String name = null;
    private String field = null;

    public String getVar() { return var; }
    public void setVar( String var ) { this.var = var; }

    //public String getType() { return type; }
    //public void setType( String type ) { this.type = type; }

    public String getName() { return name; }
    public void setName( String name ) { this.name = name; }

    public String getField() { return field; }
    public void setField( String field ) { this.field = field; }

    public int doStartTag()
        throws JspException
    {
        try
        {
            Object o = pageContext.getRequest().getAttribute( getName() );
            if ( o == null ) return Tag.SKIP_BODY;

            //log.debug( "(doStartTag) Meta Object found with id [" + mo.getId() + "]" );

            MetaObject mc = MetaDataUtil.findMetaObject( o, this );

            if ( mc == null )
            {
                log.error( "Cannot find MetaClass for object [" + o + "]" );
                return Tag.SKIP_BODY;
            }

            MetaField mf = mc.getMetaField( getField() );
            if ( mf == null )
            {
                log.error( "Cannot find MetaField for MetaClass [" + mc + "] with name [" + getField() + "]" );
                return Tag.SKIP_BODY;
            }

            String val = mf.getString( o );

            if ( getVar() == null )
            {
                JspWriter out = pageContext.getOut();
                if ( val != null ) out.print( val );
            }
            else
            {
                pageContext.setAttribute( getVar(), val, PageContext.PAGE_SCOPE );
            }
        }
        catch( Exception e )
        {
            log.error( "Error processing Meta Object Tag [name:" + getName() + ", field:" + getField() + "]", e );
            throw new JspException( "Error processing Meta Object Tag [name:" + getName() + ", field:" + getField() + "]: " + e );
        }

        return Tag.SKIP_BODY;
    }
}
