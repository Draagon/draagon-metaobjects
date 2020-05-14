package com.draagon.meta.generator;

public class WriterException extends RuntimeException {

    protected final WriterContext context;

    public WriterException( String msg, WriterContext context ) {
        super( context+" "+msg );
        this.context = context;
    }

    public WriterException( String msg, WriterContext context, Exception e ) {
        super( context+" "+msg, e );
        this.context = context;
    }
}
