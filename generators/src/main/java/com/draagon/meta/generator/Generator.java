package com.draagon.meta.generator;

import com.draagon.meta.loader.MetaDataLoader;

import java.util.List;
import java.util.Map;

public interface Generator<T extends Generator> {

    public Generator setArgs( Map<String,String> args );
    public Generator setFilter( String filter );
    public Generator setScripts( List<String> scripts );

    public void execute( MetaDataLoader loader );
}
