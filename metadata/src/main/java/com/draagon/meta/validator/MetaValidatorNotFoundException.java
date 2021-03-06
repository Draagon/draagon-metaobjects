/*
 * Copyright 2003 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */

package com.draagon.meta.validator;

import com.draagon.meta.MetaDataNotFoundException;

@SuppressWarnings("serial")
public class MetaValidatorNotFoundException
  extends MetaDataNotFoundException
{
  public MetaValidatorNotFoundException( String msg, String name )
  {
    super( msg, name );
  }

 /*public MetaValidatorNotFoundException( String msg, Throwable cause )
  {
    super( msg, cause );
  }

  public MetaValidatorNotFoundException()
  {
    super( "MetaValidator Not Found Exception" );
  }*/
}

