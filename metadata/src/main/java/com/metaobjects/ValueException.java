/*
 * Copyright 2003 Doug Mealing LLC dba Meta Objects. All Rights Reserved.
 *
 * This software is the proprietary information of Doug Mealing LLC dba Meta Objects.
 * Use is subject to license terms.
 */
package com.metaobjects;

/**
 * Value exceptions are when problems are encountered with
 * the value of an object.
 */
@SuppressWarnings("serial")
public abstract class ValueException extends RuntimeException
{
  public ValueException( String msg, Throwable t )
  {
    super( msg, t );
  }

  public ValueException( String msg )
  {
    super( msg );
  }
}
