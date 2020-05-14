package com.draagon.meta.generator.direct.json;

import com.draagon.meta.generator.GeneratorMetaException;
import com.draagon.meta.generator.direct.DirectGeneratorBase;
import com.draagon.meta.generator.direct.xml.XMLDirectWriter;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.util.xml.XMLFileWriter;
import com.google.gson.stream.JsonWriter;
import org.w3c.dom.Document;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

public abstract class SingleJsonDirectGeneratorBase extends DirectGeneratorBase {

    @Override
    public void execute( MetaDataLoader loader ) {

        File outf = null;
        JsonWriter json = null;

        try {
            File f = getOutputDir();

            outf = new File(f, getOutputFilename());
            outf.createNewFile();

            json = new JsonWriter(new FileWriter(outf));
            json.setIndent("  ");

            // Create the XML Writer
            JsonDirectWriter writer = getWriter( loader );

            // Write the XML File
            writeJson(writer, json, outf.toString());

        }
        catch( IOException e ) {
            throw new GeneratorMetaException( "Unable to write to XML file [" + outf + "]: " + e, e );
        }
        finally {
            try {
                if (json != null) json.close();
            } catch ( IOException e ) {
                log.error( "Error closing Json file ["+outf+"]: "+e, e );
            }
        }
    }

    protected abstract JsonDirectWriter getWriter( MetaDataLoader loader );

    protected void writeJson( JsonDirectWriter writer, JsonWriter json, String filename ) {
        log.info("Writing Json file: " + filename );

        writer.write(json, filename );
    }
}
