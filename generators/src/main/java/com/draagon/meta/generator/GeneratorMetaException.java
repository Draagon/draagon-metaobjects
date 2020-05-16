package com.draagon.meta.generator;

import com.draagon.meta.MetaDataException;

public class GeneratorMetaException extends MetaDataException {

    public GeneratorMetaException( String msg ) {
        super(msg);
    }

    public GeneratorMetaException( String msg, Exception e ) {
        super(msg, e);
    }
}
