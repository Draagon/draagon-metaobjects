package com.draagon.meta.loader.file;

import com.draagon.meta.MetaException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Created by dmealing on 11/30/16.
 */
public class MetaDataSources {

    private static Log log = LogFactory.getLog(MetaDataSources.class);

    // This is only used when we're not loading from a classpath
    private String sourceDir = null;

    /** Holds the SourceData */
    public static class SourceData {
        public final String sourceName;
        public final String sourceData;
        public SourceData( String sourceName, String sourceData ) {
            this.sourceName = sourceName;
            this.sourceData = sourceData;
        }
    }

    // Stores the loaded metadata files (in memory)
    private List<SourceData> sourceData = new ArrayList<>();

    protected MetaDataSources() {
    }

    protected void setSourceDir( String sourceDir ) {
        this.sourceDir = sourceDir;
    }

    /** Add the MetaData from another source */
    protected MetaDataSources add(MetaDataSources sources ) {
        sourceData.addAll( sources.sourceData );
        return this;
    }

    /**
     * Read the specified file
     */
    protected void read(String file) throws MetaException {

        if ( file.endsWith( ".xml" )) {
            loadFromXMLFile( file );
        } else if ( file.endsWith( ".bundle" )){
            loadFromBundleFile( file );
        } else {
            log.error( "Unknown metadata file type [" + file + "], so ignoring..." );
        }
    }

    /**
     * Loads all the classes specified in the Filename
     */
    protected void loadFromBundleFile(String file) throws MetaException {

        try {
            LineNumberReader in = new LineNumberReader( new InputStreamReader(getInputStream(file)));

            // Read each line in the file, and attempt to load it (including bundles)
            String line;
            while( (line = in.readLine()) != null ) {
                if (!line.trim().isEmpty()
                        && !line.trim().startsWith("#")) {
                    read( line.trim() );
                }
            }

            // Close the bundle
            in.close();
        } catch( IOException e ) {
            throw new MetaException( "Error reading metadata bundle [" + file + "]: " + e.getMessage(), e );
        }
    }

    /**
     * Loads all the classes specified in the Filename
     */
    protected void loadFromXMLFile(String file) throws MetaException {

        // LOAD THE XML FILE
        if (file == null) {
            throw new MetaException("The Meta XML file was not specified");
        }

        try {
            InputStream is = getInputStream(file);
            loadFromStream( file, is);
        }
        catch (MetaException e) {
            throw new MetaException("The Meta XML file [" + file + "] could not be loaded: " + e.getMessage(), e);
        }
    }

    /** Add a metadata file */
    protected InputStream getInputStream( String file ) {

        InputStream is = null;

        if ( sourceDir != null ) {

            // Append the source directory if needed
            String s = sourceDir;
            if ( !s.isEmpty() && !s.endsWith( "/" )) s = s + "/";

            // See if the filename exists
            String fn = s + file;
            File f = new File(fn);

            if (f.exists()) {
                try {
                    is = new FileInputStream(f);
                } catch (Exception e) {
                    log.error("Can not read Metadata file [" + fn + "]: " + e.getMessage());
                    throw new MetaException("Can not read Metadata file [" + fn + "]: " + e.getMessage(), e);
                }
            }
        }

        // Try to load as a resource instead
        if ( is == null ) {
            is = getInputStreamAsResource( file );
        }

        return is;
    }

    protected InputStream getInputStreamAsResource( String file ) {
        InputStream is = getClass().getClassLoader().getResourceAsStream(file);
        if (is == null) {
            is = ClassLoader.getSystemClassLoader().getResourceAsStream(file);
            if ( is == null ) {
                log.error("Metadata file [" + file + "] was not found");
                throw new MetaException("The Metadata file [" + file + "] was not found");
            }
        }
        return is;
    }

    protected void loadFromStream( String file, InputStream is ) {

        try {
            String data = new Scanner( is ).useDelimiter("\\Z").next();
            if ( !data.isEmpty() ) sourceData.add( new SourceData( file, data ));
        }
        catch (Exception e ) {
            log.error( "Error reading from Meta XML File ["+ file + "]: " + e.getMessage(), e );
        }
    }

    List<SourceData> getSourceData() {
        return sourceData;
    }
}
