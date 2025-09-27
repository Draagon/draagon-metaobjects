/*
 * Copyright 2003 Doug Mealing LLC dba Meta Objects. All Rights Reserved.
 *
 * This software is the proprietary information of Doug Mealing LLC dba Meta Objects.
 * Use is subject to license terms.
 */
package com.metaobjects;

/**
 * This exception should be thrown when an expected value
 * is not found.
 */
@SuppressWarnings("serial")
public class ValueNotFoundException extends ValueException  {

  public ValueNotFoundException( String msg, Throwable t )
  {
    super( msg, t );
  }
  public ValueNotFoundException( String msg )
  {
    super( msg );
  }
}
