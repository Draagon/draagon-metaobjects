package com.draagon.meta.generator;

import com.draagon.meta.io.MetaDataIOException;

public class GeneratorIOException extends MetaDataIOException {

    public GeneratorIOException(GeneratorIOWriter writer, String msg ) {
        super( writer, msg);
    }

    public GeneratorIOException(GeneratorIOWriter writer, String msg, Exception e ) {
        super( writer, msg, e );
    }
}
