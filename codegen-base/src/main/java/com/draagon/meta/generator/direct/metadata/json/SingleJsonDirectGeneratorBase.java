package com.draagon.meta.generator.direct.metadata.json;

import com.draagon.meta.generator.GeneratorException;
import com.draagon.meta.generator.MetaDataFilters;
import com.draagon.meta.generator.GeneratorIOException;
import com.draagon.meta.generator.direct.DirectGeneratorBase;
import com.draagon.meta.loader.MetaDataLoader;

import java.io.*;

/**
 * Base class for single JSON file generators.
 * This is the JSON equivalent of SingleXMLDirectGeneratorBase.
 */
public abstract class SingleJsonDirectGeneratorBase extends DirectGeneratorBase {

    @Override
    public void execute(MetaDataLoader loader) {

        File outf = null;
        OutputStream fos = null;
        JsonDirectWriter writer = null;

        parseArgs();

        try {
            // Create output file
            outf = new File(getOutputDir(), getOutputFilename());
            outf.createNewFile();

            // Create the JSON Writer
            fos = new FileOutputStream(outf);
            writer = getWriter(loader, fos);
            writer.withFilename(outf.toString())
                    .withFilters(MetaDataFilters.create(getFilters()));

            // Write the JSON File
            writeJson(writer);
        }
        catch (IOException e) {
            throw new GeneratorException("Unable to write to JSON file [" + outf + "]: " + e, e);
        }
        finally {
            try {
                // NOTE: This is critical as the close flushes the JSON to the outputstream
                if (writer != null) writer.close();
                else if (fos != null) fos.close();
            } catch (IOException e) {
                log.error("Error closing JSON file [" + outf + "]: " + e, e);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////
    // Implementation Methods

    protected abstract JsonDirectWriter getWriter(MetaDataLoader loader, OutputStream os) throws GeneratorIOException;

    protected void writeJson(JsonDirectWriter writer) throws GeneratorIOException {
        log.info("Writing JSON file: " + writer.getFilename());
        writer.writeJson();
    }
}