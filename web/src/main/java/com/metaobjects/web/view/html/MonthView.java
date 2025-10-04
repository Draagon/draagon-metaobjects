/*
 * Copyright 2003 Doug Mealing LLC dba Meta Objects. All Rights Reserved.
 *
 * This software is the proprietary information of Doug Mealing LLC dba Meta Objects.
 * Use is subject to license terms.
 */
package com.metaobjects.web.view.html;

import com.metaobjects.field.MetaField;
import com.metaobjects.*;
import com.metaobjects.registry.MetaDataRegistry;
import com.metaobjects.web.util.Param;
import com.metaobjects.web.view.*;
import static com.metaobjects.view.MetaView.TYPE_VIEW;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.*;
import jakarta.servlet.jsp.*;

public class MonthView extends HtmlView
{
  private static final Logger log = LoggerFactory.getLogger(MonthView.class);

  protected static List<String> months = null;

  private String emptyString = null;

  /**
   * Register MonthView type with registry.
   * Called by WebMetaDataProvider during service discovery.
   */
  public static void registerTypes(MetaDataRegistry registry) {
    registry.registerType(MonthView.class, def -> def
      .type(TYPE_VIEW).subType("month")
      .inheritsFrom("view", "base")
      .description("HTML month selector view")
    );
    // Registered MonthView type with unified registry
  }

 

  static {
    if ( months == null )
    {
      months = new ArrayList<String>();
      months.add( "January" );
      months.add( "February" );
      months.add( "March" );
      months.add( "April" );
      months.add( "May" );
      months.add( "June" );
      months.add( "July" );
      months.add( "August" );
      months.add( "September" );
      months.add( "October" );
      months.add( "November" );
      months.add( "December" );
    }
  }

  public MonthView(String name) {
        super(name);
    }

    @Override
    public String getSubType() {
        return "month";
    }
  
  public void setEmptyString( String empty ) {
	  this.emptyString = empty;
  }

  public String getEmptyString() {
	  return emptyString;
  }
  
  public void doView( PageContext page, Object o, String label, int mode, Map params )
    throws MetaDataException
  {
    MetaField mf = getMetaField( o );
    //MetaClass mc = mf.getMetaClass();

    try
    {
      int sel = ( mf.getInt( o ) == null ) ? 0 : mf.getInt( o ).intValue();
      doMonthView( page, mode, label, sel, params );
    }
    catch( IOException e )
    {
      log.error( "Error displaying MonthView for field [" + mf + "] with mode [" + modeToString( mode ) + "]", e );
      throw new WebViewException( "Cannot render MonthView for field [" + mf + "] in mode [" + modeToString( mode ) + "]: " + e.getMessage(), e );
    }
  }

  public void doMonthView( PageContext page, int mode, String label, int sel, Map params )
    throws IOException
  {
    if ( mode == READ ) 
    {
      if ( sel >= 0 && sel < months.size() ) {
        HtmlViewHelper.drawText( page, months.get( sel - 1 ), params );
      }
    } 
    else if ( mode == EDIT ) 
    {
      Collection<Param> data = new ArrayList<Param>();
      
      // Check to see if we want a -- as default value in month select
      if ( getEmptyString() != null ) {
    	  data.add( new Param( getEmptyString(), "" ));
      }
      int i = 1;
      for ( String m : months ) {
    	  data.add( new Param( m, "" + i ));
    	  i++;
      }
      
      String selStr = null;
      if ( sel > 0 ) selStr = "" + sel;
      
 	  HtmlViewHelper.drawSelectBox( page, label, selStr, data, params );
    }
    else if ( mode == HIDE ) {
      HtmlViewHelper.drawHidden( page, params );
    }
  }

  /**
   * Retrieve the value for the field and place it into the object
   */
  @Override
  public void getValue( HttpServletRequest request, Object o, String label )
    throws MetaDataException
  {
    // Get the integer
    int i = HtmlViewHelper.getIntValue( request, label );

    // Set the integer
    getMetaField( o ).setInt( o, Integer.valueOf( i ));
  }
}
