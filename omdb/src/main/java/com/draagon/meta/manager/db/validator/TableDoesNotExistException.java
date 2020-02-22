package com.draagon.meta.manager.db.validator;

import com.draagon.meta.MetaException;

@SuppressWarnings("serial")
public class TableDoesNotExistException extends MetaException
{
  public TableDoesNotExistException( String msg, Exception e ) {
    super( msg, e );
  }

  public TableDoesNotExistException( String msg ) {
    super( msg );
  }
}
