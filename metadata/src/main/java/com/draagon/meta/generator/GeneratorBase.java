package com.draagon.meta.generator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;

/**
 * Base implementation for all metadata generators.
 * 
 * <p>Provides common functionality for code/configuration generators including
 * argument handling, filtering, and output management. Subclasses implement
 * specific generation logic for different output formats and targets.</p>
 * 
 * <p>Core features:</p>
 * <ul>
 *   <li>Argument parsing and validation</li>
 *   <li>Metadata filtering and selection</li>
 *   <li>Output directory and file management</li>
 *   <li>Script execution support</li>
 * </ul>
 * 
 * @author Draagon Software  
 * @since 4.4.0
 */
public abstract class GeneratorBase implements Generator {

    private static final Logger log = LoggerFactory.getLogger(GeneratorBase.class);

    public static String ARG_OUTPUTDIR       = "outputDir";
    public static String ARG_OUTPUTFILENAME  = "outputFilename";

    private Map<String,String> args = new HashMap<>();
    private MetaDataFilters filters = new MetaDataFilters();
    private List<String> scripts = new ArrayList<>();

    @Override
    public GeneratorBase setArgs(Map<String, String> argMap) {
        if (argMap != null) {
            args.putAll( argMap );
        }
        return this;
    }

    protected Map<String,String> getArgs() {
        return args;
    }

    protected String getArg( String name ) {
        if (name == null) {
            return null;
        }
        return args.get( name );
    }
    protected String getArg( String name, String defValue ) {
        if ( !hasArg( name )) {
            return defValue;
        }
        return getArg( name );
    }

    protected String getArg( String name, boolean required ) {
        if ( !hasArg( name )) {
            throw new GeneratorException( "No argument '"+name+"' was specified in the args map" );
        }
        return getArg( name );
    }

    protected boolean hasArg( String name ) {
        return name != null && args.containsKey( name );
    }

    @Override
    public GeneratorBase setFilters(List<String> filters) {
        this.filters.addFilters( filters );
        return this;
    }

    protected MetaDataFilters getMetaDataFilters() {
        return filters;
    }

    protected List<String> getFilters() {
        return filters.getFilters();
    }

    @Override
    public GeneratorBase setScripts(List<String> scripts) {
        this.scripts = scripts;
        return this;
    }

    protected List<String> getScripts() {
        return scripts;
    }

    protected String getOutputFilename() {
        return getArg(ARG_OUTPUTFILENAME, true );
    }

    protected File getOutputDir() {
        return getAndCreateDir( ARG_OUTPUTDIR, getArg(ARG_OUTPUTDIR, true ));
    }

    protected File getAndCreateDir( String name, String dirPath ) {

        File f = new File( dirPath );
        if (!f.exists()) {
            if ( !f.mkdirs() ) {
                throw new GeneratorException( "Directory ["+dirPath+"] could not be created for argument [" +name+ "]" );
            }
        }

        return f;
    }

    //////////////////////////////////////////////////////////////////////
    // Misc methods

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(this.getClass().getSimpleName()+"{");
        sb.append("args=").append(getArgs());
        sb.append(", filters=").append(getFilters());
        sb.append(", scripts=").append(getScripts());
        sb.append('}');
        return sb.toString();
    }
}
