/*
 * Copyright 2003 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */

package com.draagon.meta.attr;

import com.draagon.meta.MetaDataNotFoundException;

@SuppressWarnings("serial")
public class MetaAttributeNotFoundException extends MetaDataNotFoundException
{
  public MetaAttributeNotFoundException( String msg, String name )
  {
    super( msg, name );
  }

  //public MetaAttributeNotFoundException( String msg, Throwable cause )
  //{
  //  super( msg, cause );
  //}

  //public MetaAttributeNotFoundException()
  //{
  //  super( "MetaView Not Found Exception" );
  //}
}

