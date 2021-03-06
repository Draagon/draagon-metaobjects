/*
 * Copyright 2003 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */

package com.draagon.meta.loader;

import com.draagon.meta.MetaDataException;

@SuppressWarnings("serial")
public class MetaDataLoaderNotFoundException extends MetaDataException
{
  public MetaDataLoaderNotFoundException( String msg )
  {
    super( msg );
  }

  public MetaDataLoaderNotFoundException( String msg, Throwable cause )
  {
    super( msg, cause );
  }

  public MetaDataLoaderNotFoundException()
  {
    super( "MetaClassLoader Not Found Exception" );
  }
}

