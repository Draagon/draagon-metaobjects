/*
 * Copyright 2003 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */

package com.draagon.meta.attr;

import com.draagon.meta.MetaDataException;

@SuppressWarnings("serial")
public class InvalidAttributeValueException extends MetaDataException
{
  public InvalidAttributeValueException( String msg )
  {
    super( msg );
  }

  public InvalidAttributeValueException( String msg, Throwable cause )
  {
    super( msg, cause );
  }

  public InvalidAttributeValueException()
  {
    super( "Invalid Attribute Value Exception" );
  }
}

