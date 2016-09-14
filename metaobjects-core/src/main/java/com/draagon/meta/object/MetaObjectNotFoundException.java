/*
 * Copyright (c) 2003-2012 Blue Gnosis, LLC
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.draagon.meta.object;

import com.draagon.meta.MetaDataNotFoundException;

@SuppressWarnings("serial")
public class MetaObjectNotFoundException extends MetaDataNotFoundException
{
  public MetaObjectNotFoundException( String msg, String name )
  {
    super( msg, name );
  }

  public MetaObjectNotFoundException( String msg, Object o )
  {
    super( msg, o.getClass().toString() );
  }
  
  //public MetaObjectNotFoundException( String msg, Throwable cause )
  //{
  //  super( msg, cause );
  //}

  //public MetaObjectNotFoundException()
  //{
  //  super( "MetaObject Not Found Exception" );
  //}
}

