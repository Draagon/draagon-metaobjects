/*
 * Copyright 2003 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */

package com.draagon.meta;


@SuppressWarnings("serial")
public class MetaException extends RuntimeException
{
  public MetaException( String msg )
  {
    super( msg );
  }

  public MetaException( String msg, Throwable cause )
  {
    super( msg, cause );
  }

  public MetaException( Throwable cause )
  {
    super( cause );
  }

  public MetaException()
  {
    super( "Meta Exception" );
  }
}
