package com.draagon.meta.generator;

import java.io.File;
import java.util.*;

public abstract class GeneratorBase implements Generator {

    public static String ARG_OUTPUTDIR = "outputDir";
    public static String ARG_OUTPUTFILENAME = "outputFilename";

    private Map<String,String> args = new HashMap<>();
    private MetaDataFilters filters = new MetaDataFilters();
    private List<String> scripts = new ArrayList<>();

    @Override
    public GeneratorBase setArgs(Map<String, String> argMap) {
         args.putAll( argMap );
        return this;
    }

    protected Map<String,String> getArgs() {
        return args;
    }

    protected String getArg( String name ) {
        return args.get( name );
    }

    protected String getArg( String name, boolean required ) {
        if ( !hasArg( name )) {
            throw new GeneratorException( "No argument '"+name+"' was specified in the args map" );
        }
        return getArg( name );
    }

    protected boolean hasArg( String name ) {
        return args.containsKey( name );
    }

    @Override
    public GeneratorBase setFilters(List<String> filters) {
        this.filters.addFilters( filters );
        return this;
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
        final StringBuffer sb = new StringBuffer(this.getClass().getSimpleName()+"{");
        sb.append("args=").append(getArgs());
        sb.append(", filters=").append(getFilters());
        sb.append(", scripts=").append(getScripts());
        sb.append('}');
        return sb.toString();
    }
}
