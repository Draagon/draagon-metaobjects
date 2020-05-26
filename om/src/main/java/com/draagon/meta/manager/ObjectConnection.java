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
    throws MetaDataException;
  public boolean isReadOnly()
    throws MetaDataException;

  public void setAutoCommit( boolean state )
    throws MetaDataException;
  public boolean getAutoCommit()
    throws MetaDataException;

  public void commit()
    throws MetaDataException;
  public void rollback()
    throws MetaDataException;

  public void close()
    throws MetaDataException;
  public boolean isClosed()
    throws MetaDataException;
}
