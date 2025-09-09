/*
 * Copyright 2001 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */

package com.draagon.meta.web.tag;

import com.draagon.meta.field.MetaField;
import com.draagon.meta.view.MetaView;
import com.draagon.meta.view.MetaViewNotFoundException;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.*;
//import com.draagon.meta.object.*;
import com.draagon.meta.web.view.*;
//import com.draagon.meta.web.view.html.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.*;

public class MetaViewTag extends TagSupport
{
    private static final Logger log = LoggerFactory.getLogger(MetaViewTag.class);

    public final static String READ = "read";
    public final static String EDIT = "edit";
    public final static String HIDE = "hide";

    private String name = null;
    private String field = null;
    private String mode = READ;
    private String view = null;
    private String params = null;
    private String label = null;

    private String styleClass = null;
    private String onChange = null;
    private String isReadOnly = null;
    private String height = null;
    private String width = null;

    public String getName() { return name; }
    public void setName( String name ) { this.name = name; }
    public String getField() { return field; }
    public void setField( String field ) { this.field = field; }
    public String getMode() { return mode; }
    public void setMode( String mode ) { if ( mode != null ) this.mode = mode; }
    public String getView() { return view; }
    public void setView( String view ) { this.view = view; }
    public String getLabel() { return label; }
    public void setLabel( String label ) { this.label = label; }
    public String getParams() { return params; }
    public void setParams( String params ) { this.params = params; }

    public String getStyleClass() { return styleClass; }
    public void setStyleClass( String styleClass ) { this.styleClass = styleClass; }
    public String getOnChange() { return onChange; }
    public void setOnChange( String onChange ) { this.onChange = onChange; }
    public String getIsReadOnly() { return isReadOnly; }
    public void setIsReadOnly( String isReadOnly ) { this.isReadOnly = isReadOnly; }
    public String getHeight() { return height; }
    public void setHeight( String height ) { this.height = height; }
    public String getWidth() { return width; }
    public void setWidth( String width ) { this.width = width; }

    public int doStartTag()
        throws JspException
    {
        try
        {
            int mode = MetaView.READ;
            if ( getMode().equals( EDIT )) mode = MetaView.EDIT;
            else if ( getMode().equals( HIDE )) mode = MetaView.HIDE;

            Object o = pageContext.getRequest().getAttribute( getName() );
            if ( o == null ) o = pageContext.getAttribute( getName() );
            if ( o == null ) return Tag.SKIP_BODY;

            //log.debug( "(doStartTag) Meta Object found with id [" + mo.getId() + "]" );

            MetaObject mc = MetaDataLoader.findMetaObject( o );

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

            WebView v = getView( mf );
            if ( v == null )
            {
                log.error( "No View defined for Field [" + mf + "] of Object [" + o + "]" );
                return Tag.SKIP_BODY;
            }

            // Get the label to use for the field
            String label = getLabel();
            if ( label == null || label.length() == 0 ) label = getField();

            // Render the view
            v.doView( pageContext, o, label, mode, parseParams( v ));
        }
        catch( Exception e )
        {
            log.error( "Error processing MetaView Tag [name:" + getName() + ", field:" + getField() + ", mode:" + getMode() + ", view:"+ getView() + ", params:" + getParams() + "]", e );
        }

        return Tag.SKIP_BODY;
    }

    protected WebView getView( MetaField mf )
    {
        ///////////////////////////////////////////////////////
        // Get the View
        try
        {
            if ( getView() == null || getView().length() == 0 )
                return (WebView) mf.getDefaultView();
            else
                return (WebView) mf.getView( getView() );

        }
        catch( MetaViewNotFoundException e ) {
        }
        catch( ClassCastException e ) {
          log.error( "View is not of type WebView, which is required by the MetaViewTag", e );
        }

        return null;
    }

    protected Map<String,String> parseParams( MetaView view )
    {
        String tmp = getParams();
        Map<String,String> p = new HashMap<String,String>();

        //if ( tmp == null ) return p;
        while ( tmp != null )
        {
            String s = null;
            int i = tmp.indexOf( ',' );
            if ( i >= 0 )
            {
                s = tmp.substring( 0, i );
                tmp = tmp.substring( i + 1 );
            }
            else
            {
                s = tmp;
                tmp = null;
            }

            i = s.indexOf( '=' );
            if ( i > 0 )
            {
                String n = s.substring( 0, i );
                String v = s.substring( i + 1 );

                p.put( n, v );
            }
        }

        setDefaultParameter( p, "styleClass", styleClass, view );
        setDefaultParameter( p, "onChange", onChange, view );
        setDefaultParameter( p, "isReadOnly", isReadOnly, view );
        setDefaultParameter( p, "width", width, view );
        setDefaultParameter( p, "height", height, view );

        return p;
    }

    private void setDefaultParameter( Map<String,String> p, String name, String val, MetaView view )
    {
      if ( val != null && val.length() > 0 )
        p.put( name, val );
      else if ( view.hasAttribute( name )){
        p.put( name, "" + view.getAttribute( name ));
      }
    }
}
