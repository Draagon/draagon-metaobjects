package com.draagon.meta.generator.direct;

import com.draagon.meta.generator.GeneratorMetaException;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.object.MetaObject;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;

public abstract class SingleFileDirectGeneratorBase extends DirectGeneratorBase<SingleFileDirectGeneratorBase> {

    /** Passes at context data for the execution */
    protected static class Context {

        public final MetaDataLoader loader;
        public final PrintWriter pw;
        public final String indent;
        public final Collection<MetaObject> objects;

        public Context( MetaDataLoader loader, PrintWriter pw, String indent, Collection<MetaObject> objects ) {
            this.loader = loader;
            this.pw = pw;
            this.indent = indent;
            this.objects = objects;
        }
        public Context incIndent() {
            return new Context( loader, pw, indent + "  ", objects);
        }
    }

    @Override
    public void execute( MetaDataLoader loader ) {

        File outf = null;
        PrintWriter pw = null;

        parseArgs();

        try {
            // Create output file
            outf = new File(getOutputDir(), getOutputFilename());
            outf.createNewFile();

            // Get the printwriter
            pw = new PrintWriter(outf);

            // Get the filtered objects
            Collection<MetaObject> filteredObjects =
                    filterByConfig( getFilteredMetaObjects(loader));

            // Create the Context for passing through the writers
            Context c = new Context(
                    loader,
                    pw,
                    "",
                    filteredObjects );

            // Write the UML File
            writeFile(c, outf.toString());
        }
        catch( IOException e ) {
            throw new GeneratorMetaException( "Unable to write PlantUML to file [" + outf + "]: " + e, e );
        }
        finally {
            if ( pw != null ) pw.close();
        }
    }

    protected void parseArgs() { }

    protected Collection<MetaObject> filterByConfig( Collection<MetaObject> objects ) {
        return objects;
    }

    protected abstract void writeFile(Context c, String filename ) throws IOException;
}
