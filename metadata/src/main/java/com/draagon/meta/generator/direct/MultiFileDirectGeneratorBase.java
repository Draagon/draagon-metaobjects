package com.draagon.meta.generator.direct;

import com.draagon.meta.MetaData;
import com.draagon.meta.generator.GeneratorException;
import com.draagon.meta.generator.GeneratorIOException;
import com.draagon.meta.generator.GeneratorIOWriter;
import com.draagon.meta.generator.MetaDataFilters;
import com.draagon.meta.generator.util.GeneratorUtil;
import com.draagon.meta.loader.MetaDataLoader;

import java.io.*;
import java.util.Collection;

public abstract class MultiFileDirectGeneratorBase<M extends MetaData> extends DirectGeneratorBase {

    @Override
    public void execute( MetaDataLoader loader ) {

        String filename = null;
        File outDir = null;
        OutputStream out = null;
        GeneratorIOWriter<?> writer = null;

        parseArgs();

        try {
            // Create output file
            outDir = getOutputDir();

            Collection<M> metadata = GeneratorUtil.getFilteredMetaData(
                    loader, getFilterClass(), getMetaDataFilters() );

            // Write each File
            for( M md : metadata ) {

                filename = md.getName();

                String path = getSingleOutputFilePath( md );
                filename = path;

                File fp = new File(outDir, path );
                if ( !fp.exists()) fp.mkdirs();

                String fn = getSingleOutputFilename( md );
                File f = new File( fp, fn );
                filename = f.getPath();
                f.createNewFile();

                // Get the printwriter
                out = new FileOutputStream(f);
                PrintWriter pw = new PrintWriter(new OutputStreamWriter(out));

                writer = getSingleWriter(loader, md, pw);
                writer.withFilters(MetaDataFilters.create( getFilters() ))
                        .withFilename( f.toString() );

                writeSingleFile( md, writer);
                writer.close();

                writer = null;
                pw = null;
            }

            // Write any final files if specified
            if ( hasArg(ARG_OUTPUTFILENAME)) {
                filename = getOutputFilename();

                // Create output file
                File outf = new File(getOutputDir(), getOutputFilename());
                outf.createNewFile();

                // Get the printwriter
                out = new FileOutputStream(outf);

                writer = getFinalWriter(loader, out);
                if ( writer != null ) {

                    writer.withFilters(MetaDataFilters.create(getFilters()))
                            .withFilename(outf.toString());

                    writeFinalFile(metadata, writer);
                }
                else {
                    OutputStream out2 = out;
                    out = null;
                    out2.close();
                }
            }
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
            else if ( out != null ) {
                try {
                    out.close();
                } catch (IOException e) {
                    log.error("Error closing output stream for file [" + filename + "]: " + e, e );
                }
            }
        }
    }

    protected abstract Class<M> getFilterClass();

    protected abstract <T extends GeneratorIOWriter> T getSingleWriter( MetaDataLoader loader, M md, PrintWriter pw ) throws GeneratorIOException;

    protected abstract <T extends GeneratorIOWriter> T getFinalWriter(MetaDataLoader loader, OutputStream out ) throws GeneratorIOException;

    protected abstract void writeSingleFile( M md, GeneratorIOWriter<?> writer ) throws GeneratorIOException;

    protected abstract void writeFinalFile( Collection<M> metadata, GeneratorIOWriter<?>  writer ) throws GeneratorIOException;

    protected abstract String getSingleOutputFilePath( M md );

    protected abstract String getSingleOutputFilename( M md );
}
