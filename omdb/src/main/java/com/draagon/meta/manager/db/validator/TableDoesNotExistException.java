package com.draagon.meta.manager.db.validator;

import com.draagon.meta.MetaDataException;


@SuppressWarnings("serial")
public class TableDoesNotExistException extends MetaDataException
{
  public TableDoesNotExistException( String msg, Exception e ) {
    super( msg, e );
  }

  public TableDoesNotExistException( String msg ) {
    super( msg );
  }
}
