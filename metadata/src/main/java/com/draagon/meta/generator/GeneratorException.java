package com.draagon.meta.generator;

import com.draagon.meta.MetaDataException;

public class GeneratorException extends MetaDataException {

    public GeneratorException(String msg ) {
        super(msg);
    }

    public GeneratorException(String msg, Exception e ) {
        super(msg, e);
    }
}
