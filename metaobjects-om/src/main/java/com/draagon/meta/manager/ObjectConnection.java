/*
 * Copyright 2003 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */

package com.draagon.meta.manager;

import com.draagon.meta.*;

public interface ObjectConnection
{
  public Object getDatastoreConnection();

  public void setReadOnly( boolean state )
    throws MetaException;
  public boolean isReadOnly()
    throws MetaException;

  public void setAutoCommit( boolean state )
    throws MetaException;
  public boolean getAutoCommit()
    throws MetaException;

  public void commit()
    throws MetaException;
  public void rollback()
    throws MetaException;

  public void close()
    throws MetaException;
  public boolean isClosed()
    throws MetaException;
}
