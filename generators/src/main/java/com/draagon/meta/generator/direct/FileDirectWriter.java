package com.draagon.meta.generator.direct;

import com.draagon.meta.generator.MetaDataFilters;
import com.draagon.meta.generator.MetaDataWriter;
import com.draagon.meta.generator.MetaDataWriterException;
import com.draagon.meta.generator.util.FileIndentor;
import com.draagon.meta.loader.MetaDataLoader;

import java.io.IOException;
import java.io.PrintWriter;


public abstract class FileDirectWriter<T extends FileDirectWriter> extends MetaDataWriter<T> {

    private FileIndentor indentor;
    protected final PrintWriter pw;

    public FileDirectWriter(MetaDataLoader loader, PrintWriter pw ) {
        super(loader);
        this.pw = pw;
    }

    /////////////////////////////////////////////////////////////////////////
    // Options

    //public FileDirectWriter withFilename( String filename ) { return (FileDirectWriter) super.withFilename( filename ); }

    public T withIndentor( String indentor ) {
        this.indentor = new FileIndentor( indentor );
        return (T) this;
    }

    protected FileIndentor getIndentor() {
        if ( indentor == null ) {
            indentor = new FileIndentor( "  " );
        }
        return indentor;
    }

    /////////////////////////////////////////////////////////////////////////
    // FileWriter methods

    protected void inc() {
        indentor = getIndentor().inc();
    }

    protected void dec() {
        indentor = getIndentor().dec();
    }

    @Override
    public void close() throws MetaDataWriterException {
        pw.close();
        if ( indentor != null && indentor.isIndented() ) throw new MetaDataWriterException(this, "The indenting increment is not back to root level, invalid logic");
    }

    /////////////////////////////////////////////////////////////////////////
    // Print Methods

    protected void print( String s ) {
        print( false, s );
    }

    protected void println( String s ) {
        println( false, s );
    }

    protected void print( boolean indent, String s ) {
        pw.print( (indent?getIndentor().pre():"") + s );
    }

    protected void println( boolean indent, String s ) {
        pw.println((indent?getIndentor().pre():"") + s );
    }

    protected void println() {
        pw.println();
    }

    /////////////////////////////////////////////////////////////////////////
    // Misc Methods

    @Override
    protected String getToStringOptions() {
        return ","+getIndentor();
    }
}
