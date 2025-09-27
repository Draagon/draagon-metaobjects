package com.metaobjects.generator.direct.metadata.xml;

import com.metaobjects.generator.GeneratorException;
import com.metaobjects.generator.MetaDataFilters;
import com.metaobjects.generator.GeneratorIOException;
import com.metaobjects.generator.direct.DirectGeneratorBase;
import com.metaobjects.loader.MetaDataLoader;

import java.io.*;

public abstract class SingleXMLDirectGeneratorBase extends DirectGeneratorBase {

    @Override
    public void execute( MetaDataLoader loader ) {

        File outf = null;
        OutputStream fos = null;
        XMLDirectWriter writer = null;

        parseArgs();

        try {
            // Create output file
            outf = new File(getOutputDir(), getOutputFilename());
            outf.createNewFile();

            // Create the XML Writer
            fos = new FileOutputStream( outf );
            writer = getWriter( loader, fos );
            writer.withFilename( outf.toString())
                    .withFilters(MetaDataFilters.create( getFilters() ));

            // Write the XML File
            writeXML( writer );
        }
        catch( IOException e ) {
            throw new GeneratorException( "Unable to write to XML file [" + outf + "]: " + e, e );
        }
        finally {
            try {
                // NOTE: This is critical as the close flushes the XML to the outputstream
                if (writer != null) writer.close();
                else if ( fos != null ) fos.close();
            } catch ( IOException e ) {
                log.error( "Error closing XML file ["+outf+"]: "+e, e );
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////
    // Implementation Methods

    protected abstract XMLDirectWriter getWriter( MetaDataLoader loader, OutputStream os ) throws GeneratorIOException;

    protected void writeXML( XMLDirectWriter writer ) throws GeneratorIOException {
        log.info("Writing XML file: " + writer.getFilename() );
        writer.writeXML();
    }
}