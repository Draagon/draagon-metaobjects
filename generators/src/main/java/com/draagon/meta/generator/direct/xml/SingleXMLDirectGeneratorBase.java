package com.draagon.meta.generator.direct.xml;

import com.draagon.meta.generator.GeneratorMetaException;
import com.draagon.meta.generator.direct.DirectGeneratorBase;
import com.draagon.meta.generator.direct.FileDirectWriter;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.util.xml.XMLFileWriter;
import org.w3c.dom.Document;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

public abstract class SingleXMLDirectGeneratorBase extends DirectGeneratorBase {

    @Override
    public void execute( MetaDataLoader loader ) {

        File outf = null;
        FileOutputStream fos = null;

        parseArgs();

        try {
            // Create output file
            outf = new File(getOutputDir(), getOutputFilename());
            outf.createNewFile();

            fos = new FileOutputStream( outf );

            Document doc = XMLFileWriter.getBuilder();

            // Create the XML Writer
            XMLDirectWriter writer = getWriter( loader );

            // Write the XML File
            writeXML(writer, doc, outf.toString());

            // Write the XML Document to the file
            XMLFileWriter.writeToStream( doc, fos, true );
        }
        catch( IOException e ) {
            throw new GeneratorMetaException( "Unable to write to XML file [" + outf + "]: " + e, e );
        }
        finally {
            try {
                if (fos != null) fos.close();
            } catch ( IOException e ) {
                log.error( "Error closing XML file ["+outf+"]: "+e, e );
            }
        }
    }

    protected abstract XMLDirectWriter getWriter( MetaDataLoader loader );

    protected void writeXML( XMLDirectWriter writer, Document doc, String filename ) {
        log.info("{"+writer+"} Writing XML file: " + filename );

        writer.write(doc, filename );
    }
}
