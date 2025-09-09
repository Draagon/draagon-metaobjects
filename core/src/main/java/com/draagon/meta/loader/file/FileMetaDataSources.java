package com.draagon.meta.loader.file;

import com.draagon.meta.MetaDataException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

/**
 * Created by dmealing on 11/30/16.
 */
public class FileMetaDataSources {

    private static final Logger log = LoggerFactory.getLogger(FileMetaDataSources.class);

    /** Holds the SourceData */
    public static class SourceData {

        public final String filename;
        public final Class<? extends FileMetaDataSources> sourceClass;
        public final String sourceData;

        public SourceData(String filename, Class<? extends FileMetaDataSources> sourceClass, String sourceData ) {
            this.filename = filename;
            this.sourceClass = sourceClass;
            this.sourceData = sourceData;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SourceData that = (SourceData) o;
            return Objects.equals(filename, that.filename) &&
                    Objects.equals(sourceClass, that.sourceClass) &&
                    Objects.equals(sourceData, that.sourceData);
        }

        @Override
        public int hashCode() {
            return Objects.hash(filename, sourceClass, sourceData);
        }

        @Override
        public String toString() {
            return "SourceData{" +
                    "filename='" + filename + '\'' +
                    ", sourceClass=" + sourceClass.getName() +
                    ", sourceData.hash='" + sourceData.hashCode() + '\'' +
                    '}';
        }
    }

    // This is only used when we're not loading from a classpath
    private String sourceDir = null;

    // Stores the loaded metadata files (in memory)
    private List<SourceData> sourceData = new ArrayList<>();

    protected ClassLoader loaderClassLoader;

    protected FileMetaDataSources() {
        this.loaderClassLoader=getClass().getClassLoader();
    }

    protected FileMetaDataSources(ClassLoader loaderClassLoader) {
        this.loaderClassLoader=loaderClassLoader;
    }

    public void setLoaderClassLoader(ClassLoader loaderClassLoader) {
        this.loaderClassLoader=loaderClassLoader;
    }

    public ClassLoader getLoaderClassLoader() {
        return loaderClassLoader;
    }

    protected void setSourceDir( String sourceDir ) {
        this.sourceDir = sourceDir;
    }

    /** Add the MetaData from another source */
    protected FileMetaDataSources add(FileMetaDataSources sources ) {
        sourceData.addAll( sources.sourceData );
        return this;
    }

    /**
     * Read the specified file
     */
    protected void read(String filename) throws MetaDataException {

        if ( filename.endsWith( ".bundle" )){
            loadFromBundleFile( filename );
        } else {
            loadFromInputStream( filename, getInputStreamForFilename( filename ));
        }
    }

    /**
     * Loads all the classes specified in the Filename
     */
    protected void loadFromBundleFile(String filename) throws MetaDataException {

        if ( log.isDebugEnabled())
            log.debug( "Loading files from bundle: "+filename);

        try {
            LineNumberReader in = new LineNumberReader( new InputStreamReader( getInputStreamForFilename( filename )));

            // Read each line in the file, and attempt to load it (including bundles)
            String line;
            while( (line = in.readLine()) != null ) {
                if (!line.trim().isEmpty()
                        && !line.trim().startsWith("#")) {

                    if ( log.isDebugEnabled())
                        log.debug( "Attempting to read ["+line.trim()+"] from bundle: "+filename);

                    read( line.trim() );
                }
            }

            // Close the bundle
            in.close();
        }
        catch( IOException e ) {
            throw new MetaDataException( "Error reading MetaData bundle [" + filename + "]: " + e.getMessage(), e );
        }
    }

    /**
     * Loads all the classes specified in the Filename
     */
    protected InputStream getInputStreamForFilename(String filename) throws MetaDataException {

        if ( log.isDebugEnabled())
            log.debug( "Getting InputStream for file: "+filename);

        // LOAD THE FILE
        if (filename == null) {
            throw new NullPointerException("The MetaData file was null");
        }

        // Try to load from the file system if a Source Directory was specified
        if ( sourceDir != null ) {

            // Attempt to fetch it as a resource first
            if (getResourceViaClassLoaderChain(filename) != null) {
                return getResourceInputStream(filename);
            }
            // Otherwise, get it as an actual File using the SourceDir
            else {
                return getFileInputStream(filename);
            }
        }
        // Otherise, try to load as a resource instead
        else {
            return getResourceInputStream(filename);
        }
    }

    protected URL getResourceViaClassLoaderChain(String filename) {

        // Attempt to fetch via the local class (this is useful for OSGI where the MetaDataSources is
        // actually the correct ClassLoader to use to find the appropriate files
        URL url = getClass().getClassLoader().getResource(filename);
        if ( url != null && log.isDebugEnabled())
            log.debug( "Retrieved from Source "+getClass().getName()+"'s ClassLoader: "+filename);

        // Fetch using the passed in ClassLoader, which is useful when the resources are specified in a specific
        // ClassLoader, usually from the MetaData Plugin (MOJO)
        if (url == null && getLoaderClassLoader() != null) {
            url = getLoaderClassLoader().getResource(filename);
            if ( url != null && log.isDebugEnabled())
                log.debug( "Retrieved Loader specified ClassLoader: "+filename);
        }

        // If it's still not found, use the SystemClassLoader, for a final attempt
        if (url == null) {
            url = ClassLoader.getSystemClassLoader().getResource(filename);
            if ( url != null && log.isDebugEnabled())
                log.debug( "Retrieved from System ClassLoader: "+filename);
        }

        if ( url == null && log.isDebugEnabled())
            log.debug( "Could not find resource on any ClassLoader: "+filename);

        return url;
    }

    private InputStream getResourceInputStream(String filename) {

        URL url = getResourceViaClassLoaderChain(filename);
        if (url == null) {
            throw new MetaDataException("MetaData file [" + filename + "] was not found on the ClassLoader for the System or FileMetaDataSources ["+this.getClass().getName()+"]");
        }

        // Construct URI and return the File
        try {
            return url.openStream();
        }
        catch (IOException e) {
            throw new MetaDataException("Could not open URL resource [" + url + "] for MetaData file [" +filename+ "]: " + e.getMessage(), e);
        }
    }

    private InputStream getFileInputStream(String filename) {

        // Append the source directory if needed
        String s = sourceDir;
        if (!s.isEmpty() && !s.endsWith("/")) s = s + "/";

        // See if the filename exists
        String fn = s + filename;
        File f = new File(fn);

        if (!f.exists()) {
            throw new MetaDataException("The MetaData file [" + f + "] was not found on the filesystem");
        }

        try {
            return new FileInputStream(f);
        }
        catch( IOException e ) {
            throw new MetaDataException( "Error creating FieInputStream for MetaData File ["+ fn + "]: " + e.getMessage(), e );
        }
    }

    protected void loadFromInputStream( String filename, InputStream is ) {

        try {
            String data = new Scanner( is ).useDelimiter("\\Z").next();
            if ( data.isEmpty() ) {
                throw new MetaDataException("MetaData File had no contents [" + filename + "]");
            }

            sourceData.add( new SourceData( filename, getClass(), data ));
        }
        catch (RuntimeException e ) {
            throw new MetaDataException( "Error reading from MetaData File ["+ filename + "]: " + e.getMessage(), e );
        }
        finally {
            try { is.close(); } catch( IOException ignore ) {}
        }
    }

    public List<SourceData> getSourceData() {
        return sourceData;
    }

    ///////////////////////////////////////////////////////////////////////
    // Misc Methods

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileMetaDataSources that = (FileMetaDataSources) o;
        return Objects.equals(sourceDir, that.sourceDir) &&
                Objects.equals(sourceData, that.sourceData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceDir, sourceData);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() +"{" +
                "sourceDir='" + sourceDir + '\'' +
                ", sourceData=" + sourceData +
                ", loaderClassLoader=" + loaderClassLoader +
                '}';
    }
}
