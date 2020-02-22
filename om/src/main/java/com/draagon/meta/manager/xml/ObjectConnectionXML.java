/*
 * Copyright 2003 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */

package com.draagon.meta.manager.xml;

import com.draagon.meta.object.MetaObject;
import com.draagon.meta.*;
import com.draagon.meta.manager.*;

import java.util.*;

public class ObjectConnectionXML implements ObjectConnection
{
  //private static Log log = LogFactory.getLog( ObjectConnectionXML.class );

  private Map<MetaObject,List<Object>> tables;
  private boolean readonly = false;
  private boolean auto = true;
  private boolean closed = false;

  public ObjectConnectionXML( Map<MetaObject,List<Object>> tables )
  {
    this.tables = tables;
  }

  public Object getDatastoreConnection()
  {
    return tables;
  }

  public void setReadOnly( boolean state )
    throws MetaException
  {
    readonly = state;
  }

  public boolean isReadOnly()
    throws MetaException
  {
    return readonly;
  }

  public void setAutoCommit( boolean state )
    throws MetaException
  {
    auto = state;
  }

  public boolean getAutoCommit()
    throws MetaException
  {
    return auto;
  }

  public void commit()
    throws MetaException
  {
    // Do nothing for now
  }

  public void rollback()
    throws MetaException
  {
    // Do nothing for now
  }

  public void close()
    throws MetaException
  {
    closed = true;
  }

  public boolean isClosed()
    throws MetaException
  {
    return closed;
  }
}
