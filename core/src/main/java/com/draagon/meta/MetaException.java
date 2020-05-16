package com.draagon.meta;

/**
 * @deprecated Use MetaDataException
 */
public class MetaException extends MetaDataException {

    public MetaException(String msg )
    {
        super( msg );
    }

    public MetaException(String msg, Throwable cause )
    {
        super( msg, cause );
    }
}
