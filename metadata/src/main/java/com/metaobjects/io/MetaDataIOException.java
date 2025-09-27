package com.metaobjects.io;

import java.io.IOException;

public class MetaDataIOException extends IOException {

    public MetaDataIOException(MetaDataIO io, String msg ) {
        this( io, msg, null );
    }

    public MetaDataIOException(MetaDataIO io, String msg, Exception e ) {
        super( getPrefix( io, e ) + " " + msg, e );
    }

    protected static String getPrefix( MetaDataIO io, Exception e ) {

        // Don't write again if the wrapper one already has
        if ( e instanceof MetaDataIOException ) return "";

        return "["+io.path().getPathAndClear()+"]{writer="+ io.getClass().getSimpleName() + "," +
            "loader="  + io.getLoader().getName() + "}";
    }
}
