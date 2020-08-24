package com.draagon.meta.manager;

public interface ObjectConnection
{
  public Object getDatastoreConnection();

  public void setReadOnly( boolean state ) throws PersistenceException;
  public boolean isReadOnly() throws PersistenceException;

  public void setAutoCommit( boolean state ) throws PersistenceException;
  public boolean getAutoCommit() throws PersistenceException;

  public void commit() throws PersistenceException;
  public void rollback() throws PersistenceException;

  public void close() throws PersistenceException;
  public boolean isClosed() throws PersistenceException;
}
