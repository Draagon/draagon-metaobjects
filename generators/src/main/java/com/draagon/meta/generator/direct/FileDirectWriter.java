package com.draagon.meta.generator.direct;

import com.draagon.meta.generator.WriterContext;
import com.draagon.meta.loader.MetaDataLoader;

import java.io.PrintWriter;


public abstract class FileDirectWriter<D> extends DirectWriter<PrintWriter,D> {

    /** Passes at context data for the execution */
    public static class Context extends WriterContext<Context,PrintWriter,String> {

        public final String indent;

        protected Context(PrintWriter pw, String name ) {
            this(null, pw, name, "");
        }

        protected Context(Context parent, PrintWriter pw, String name, String indent ) {
            super( parent, pw, name );
            this.indent = indent;
        }

        @Override
        public Context newInstance( Context parent, PrintWriter pw, String name ) {
            return new Context( parent, pw, name, indent+"  ");
        }

        @Override
        public String getNodePathName() {
            return node;
        }
    }

    public FileDirectWriter(MetaDataLoader loader, MetaDataFilters filters) {
        super(loader, filters);
    }

    @Override
    public void write( PrintWriter out, D data ) {
        Context c = new Context(out,"root");
        writeFile(c,data);
    }

    protected abstract void writeFile(Context c, D data);

    ////////////////////////////////////////////////////////
    // Draw Helper Utils

    protected void pr1(Context c, String s ) {
        c.out.print( s );
    }

    protected void pr(Context c, String s ) {
        c.out.print( s );
    }

    protected void pn(Context c, String s ) {
        c.out.println( s );
    }

    protected void pn(Context c) {
        c.out.println();
    }

    ////////////////////////////////////////////////////////
    // Misc Methods

    @Override
    public String toString() {
        return this.getClass().getClass().getSimpleName();
    }
}
