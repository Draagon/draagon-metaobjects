/*
 * Copyright (c) 2003-2012 Doug Mealing LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.draagon.meta;

/**
 * This should be thrown when a value is invalid for a field
 * of an object.
 */
@SuppressWarnings("serial")
public class InvalidValueException extends ValueException {

  public InvalidValueException( String msg, Throwable t )
  {
    super( msg, t );
  }
  public InvalidValueException( String msg )
  {
    super( msg );
  }
}
