/*
 * Created on Aug 23, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.draagon.meta;

/**
 * @author dmealing
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
@SuppressWarnings("serial")
public class MetaDataException extends RuntimeException {

  public MetaDataException( String msg )
  {
    super( msg );
  }

  public MetaDataException( String msg, Throwable cause )
  {
    super( msg, cause );
  }

  public MetaDataException( Throwable cause )
  {
    super( cause );
  }

  public MetaDataException()
  {
    super( "Meta Data Exception" );
  }
}
