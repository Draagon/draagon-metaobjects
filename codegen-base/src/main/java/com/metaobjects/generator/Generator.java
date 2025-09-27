package com.metaobjects.generator;

import com.metaobjects.loader.MetaDataLoader;

import java.util.List;
import java.util.Map;

public interface Generator {

    public Generator setArgs( Map<String,String> args );
    public Generator setFilters( List<String> filters );
    public Generator setScripts( List<String> scripts );

    public void execute( MetaDataLoader loader );
}
