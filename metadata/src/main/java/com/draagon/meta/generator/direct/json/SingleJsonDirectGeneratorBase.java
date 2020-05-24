package com.draagon.meta.generator.direct.json;

import com.draagon.meta.generator.GeneratorException;
import com.draagon.meta.generator.MetaDataFilters;
import com.draagon.meta.generator.GeneratorIOException;
import com.draagon.meta.generator.direct.DirectGeneratorBase;
import com.draagon.meta.loader.MetaDataLoader;

import java.io.*;

public abstract class SingleJsonDirectGeneratorBase extends DirectGeneratorBase {

    @Override
    public void execute( MetaDataLoader loader ) throws GeneratorException {

        File outf = null;
        FileWriter fileWriter = null;
        JsonDirectWriter writer = null;

        try {
            File f = getOutputDir();

            outf = new File(f, getOutputFilename());
            outf.createNewFile();

            // Create the XML Writer
            fileWriter = new FileWriter(outf);
            writer = getWriter( loader, fileWriter );
            writer.withFilename( outf.toString() );
            writer.withFilters( MetaDataFilters.create( getFilters() ));
            writer.withIndent("  ");

            // Write the XML File
            writeJson(writer, outf.toString());

        }
        catch( GeneratorIOException e ) {
            throw new GeneratorException( e.toString(), e );
        }
        catch( IOException e ) {
            throw new GeneratorException( "Unable to write to Json file [" + outf + "]: " + e, e );
        }
        finally {
            try {
                if (writer != null) writer.close();
                else if ( fileWriter != null ) fileWriter.close();

            } catch( GeneratorIOException e ) {
                throw new GeneratorException( e.toString(), e );
            } catch( IOException e ) {
                log.error( "Error closing Json file ["+outf+"]: "+e, e );
            }
        }
    }

    protected abstract JsonDirectWriter getWriter( MetaDataLoader loader, Writer writer ) throws GeneratorIOException;

    protected void writeJson( JsonDirectWriter writer, String filename ) throws GeneratorIOException {
        log.info("Writing Json file: " + filename );

        writer.writeJson();
    }
}
