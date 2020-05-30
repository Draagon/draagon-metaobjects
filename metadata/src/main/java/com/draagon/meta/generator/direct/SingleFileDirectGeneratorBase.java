package com.draagon.meta.generator.direct;

import com.draagon.meta.generator.GeneratorException;
import com.draagon.meta.generator.MetaDataFilters;
import com.draagon.meta.generator.GeneratorIOException;
import com.draagon.meta.loader.MetaDataLoader;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

public abstract class SingleFileDirectGeneratorBase<T extends FileDirectWriter> extends DirectGeneratorBase {

    @Override
    public void execute( MetaDataLoader loader ) {

        String filename = null;
        PrintWriter pw = null;
        T writer = null;

        parseArgs();

        try {
            // Create output file
            filename = getOutputFilename();
            File outf = new File(getOutputDir(), filename );
            outf.createNewFile();

            // Get the printwriter
            pw = new PrintWriter(outf);

            writer = getWriter(loader, pw);
            writer.withFilters(MetaDataFilters.create( getFilters() ))
                    .withFilename( outf.toString() );

            // Write the UML File
            writeFile(writer);
        }
        catch( IOException e ) {
            throw new GeneratorException( "Unable to write to file [" + filename + "]: " + e, e );
        }
        finally {
            if ( writer != null ) {
                try {
                    writer.close();
                } catch (IOException e) {
                    throw new GeneratorException( "Unable to close file [" + filename + "]: " + e, e );
                }
            }
            else if ( pw != null ) pw.close();
        }
    }

    protected abstract T getWriter( MetaDataLoader loader, PrintWriter pw ) throws GeneratorIOException;

    protected abstract void writeFile( T writer ) throws GeneratorIOException;
}
