/*
 * Copyright 2003 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta;

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
