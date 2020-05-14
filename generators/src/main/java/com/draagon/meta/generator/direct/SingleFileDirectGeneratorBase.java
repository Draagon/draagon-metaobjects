package com.draagon.meta.generator.direct;

import com.draagon.meta.generator.GeneratorMetaException;
import com.draagon.meta.loader.MetaDataLoader;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

public abstract class SingleFileDirectGeneratorBase extends DirectGeneratorBase {

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

            FileDirectWriter writer = getWriter( loader );

            // Write the UML File
            writeFile(writer, pw, outf.toString());
        }
        catch( IOException e ) {
            throw new GeneratorMetaException( "Unable to write to file [" + outf + "]: " + e, e );
        }
        finally {
            if ( pw != null ) pw.close();
        }
    }

    protected abstract FileDirectWriter getWriter( MetaDataLoader loader );

    protected void writeFile( FileDirectWriter writer, PrintWriter pw, String filename ) {
        log.info("{"+writer+"} Writing file: " + filename );

        writer.write(pw, filename );
    }
}
