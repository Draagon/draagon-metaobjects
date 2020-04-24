package com.draagon.meta.generator;

import com.draagon.meta.MetaException;

public class GeneratorMetaException extends MetaException {

    public GeneratorMetaException( String msg ) {
        super(msg);
    }

    public GeneratorMetaException( String msg, Exception e ) {
        super(msg, e);
    }
}
