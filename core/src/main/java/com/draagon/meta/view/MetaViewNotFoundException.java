/*
 * Copyright 2003 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */

package com.draagon.meta.view;

import com.draagon.meta.MetaDataNotFoundException;

public class MetaViewNotFoundException extends MetaDataNotFoundException
{
  public MetaViewNotFoundException( String msg, String name )
  {
    super( msg, name );
  }
}

