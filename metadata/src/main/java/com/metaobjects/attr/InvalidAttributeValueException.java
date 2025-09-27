/*
 * Copyright 2003 Doug Mealing LLC dba Meta Objects. All Rights Reserved.
 *
 * This software is the proprietary information of Doug Mealing LLC dba Meta Objects.
 * Use is subject to license terms.
 */

package com.metaobjects.attr;

import com.metaobjects.MetaDataException;

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

