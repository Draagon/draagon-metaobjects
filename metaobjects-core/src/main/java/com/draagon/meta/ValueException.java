/*
 * Copyright 2003 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta;

/**
 * Value exceptions are when problems are encountered with
 * the value of an object.
 */
@SuppressWarnings("serial")
public class ValueException extends RuntimeException
{
  public ValueException( String msg, Throwable t )
  {
    super( msg, t );
  }

  public ValueException( Throwable t )
  {
    super( t );
  }

  public ValueException( String msg )
  {
    super( msg );
  }

  public ValueException()
  {
    super( "Value Exception" );
  }

}
