package com.draagon.meta.generator.direct;

import com.draagon.meta.generator.GeneratorMetaException;
import com.draagon.meta.generator.MetaDataFilters;
import com.draagon.meta.generator.MetaDataWriterException;
import com.draagon.meta.loader.MetaDataLoader;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

public abstract class SingleFileDirectGeneratorBase<T extends FileDirectWriter> extends DirectGeneratorBase {

    @Override
    public void execute( MetaDataLoader loader ) {

        File outf = null;
        PrintWriter pw = null;
        T writer = null;

        parseArgs();

        try {
            // Create output file
            outf = new File(getOutputDir(), getOutputFilename());
            outf.createNewFile();

            // Get the printwriter
            pw = new PrintWriter(outf);

            writer = getWriter(loader, pw);
            writer.withFilters(MetaDataFilters.create( getFilters() ))
                    .withFilename( outf.toString() );

            // Write the UML File
            writeFile(writer);
        }
        catch( MetaDataWriterException | IOException e ) {
            throw new GeneratorMetaException( "Unable to write to file [" + outf + "]: " + e, e );
        }
        finally {
            if ( writer != null ) {
                try {
                    writer.close();
                } catch (MetaDataWriterException e) {
                    throw new GeneratorMetaException( "Unable to close file [" + outf + "]: " + e, e );
                }
            }
            else if ( pw != null ) pw.close();
        }
    }

    protected abstract T getWriter( MetaDataLoader loader, PrintWriter pw ) throws MetaDataWriterException;

    protected abstract void writeFile( T writer ) throws MetaDataWriterException;
}