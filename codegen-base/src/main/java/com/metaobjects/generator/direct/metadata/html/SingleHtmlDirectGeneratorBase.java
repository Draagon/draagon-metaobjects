package com.metaobjects.generator.direct.metadata.html;

import com.metaobjects.generator.GeneratorException;
import com.metaobjects.generator.MetaDataFilters;
import com.metaobjects.generator.GeneratorIOException;
import com.metaobjects.generator.direct.DirectGeneratorBase;
import com.metaobjects.loader.MetaDataLoader;

import java.io.*;

/**
 * Base class for single HTML file generators.
 * This is the HTML equivalent of SingleJsonDirectGeneratorBase.
 */
public abstract class SingleHtmlDirectGeneratorBase extends DirectGeneratorBase {

    @Override
    public void execute(MetaDataLoader loader) {

        File outf = null;
        OutputStream fos = null;
        MetaDataHtmlDocumentationWriter writer = null;

        parseArgs();

        try {
            // Create output file
            outf = new File(getOutputDir(), getOutputFilename());
            outf.createNewFile();

            // Create the HTML Writer
            fos = new FileOutputStream(outf);
            writer = getWriter(loader, fos);
            writer.withFilename(outf.toString())
                    .withFilters(MetaDataFilters.create(getFilters()));

            // Write the HTML File
            writeHtml(writer);
        }
        catch (IOException e) {
            throw new GeneratorException("Unable to write to HTML file [" + outf + "]: " + e, e);
        }
        finally {
            try {
                // NOTE: This is critical as the close flushes the HTML to the outputstream
                if (writer != null) writer.close();
                else if (fos != null) fos.close();
            } catch (IOException e) {
                log.error("Error closing HTML file [" + outf + "]: " + e, e);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////
    // Implementation Methods

    protected abstract MetaDataHtmlDocumentationWriter getWriter(MetaDataLoader loader, OutputStream os) throws GeneratorIOException;

    protected void writeHtml(MetaDataHtmlDocumentationWriter writer) throws GeneratorIOException {
        log.info("Writing HTML file: " + writer.getFilename());
        writer.writeHtml();
    }
}