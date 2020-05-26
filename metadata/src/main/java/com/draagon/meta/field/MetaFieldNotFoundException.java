/*
 * Copyright 2003 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */

package com.draagon.meta.field;

import com.draagon.meta.MetaDataNotFoundException;

@SuppressWarnings("serial")
public class MetaFieldNotFoundException extends MetaDataNotFoundException
{
  public MetaFieldNotFoundException( String msg, String name )
  {
    super( msg, name );
  }
}

