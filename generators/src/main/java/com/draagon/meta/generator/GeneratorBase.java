package com.draagon.meta.generator;

import com.draagon.meta.MetaData;
import com.draagon.meta.MetaException;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.object.MetaObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class GeneratorBase<T extends GeneratorBase> implements Generator<T> {

    public static String ARG_OUTPUTDIR = "outputDir";
    public static String ARG_OUTPUTFILENAME = "outputFilename";

    private Map<String,String> args = new HashMap<>();
    private String filter = null;
    private List<String> scripts = new ArrayList<>();

    @Override
    public T setArgs(Map<String, String> argMap) {
         args.putAll( argMap );
        return (T) this;
    }

    protected Map<String,String> getArgs() {
        return args;
    }

    protected String getArg( String name ) {
        return args.get( name );
    }

    protected String getArg( String name, boolean required ) {
        if ( !hasArg( name )) {
            throw new GeneratorMetaException( "No argument '"+name+"' was specified in the args map" );
        }
        return getArg( name );
    }

    protected boolean hasArg( String name ) {
        return args.containsKey( name );
    }

    @Override
    public T setFilter(String filter) {
        this.filter = filter;
        return (T) this;
    }

    protected String getFilter() {
        return filter;
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
                throw new GeneratorMetaException( "Directory ["+dirPath+"] could not be created for argument [" +name+ "]" );
            }
        }

        return f;
    }

    protected List<MetaData> getFilteredMetaData( MetaDataLoader loader ) {

        // TODO:  Make this actually filter
        return loader.getChildren();
    }
}
