/*
 * Copyright 2003 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */

package com.draagon.meta.loader;

import com.draagon.meta.MetaDataException;

/**
 * @deprecated As of 5.2.0, use {@link com.draagon.meta.MetaDataConfigurationException#forLoaderConfiguration} instead.
 *             This class represents a configuration issue and should use the configuration exception hierarchy.
 * 
 * @since 1.0 (deprecated in 5.2.0)
 */
@Deprecated(since = "5.2.0", forRemoval = true)
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

