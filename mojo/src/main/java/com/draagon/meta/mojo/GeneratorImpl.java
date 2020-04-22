package com.draagon.meta.mojo;

import com.draagon.meta.loader.MetaDataLoader;

import java.util.List;
import java.util.Map;

public interface GeneratorImpl {

    public void setArgs( Map<String,String> args );
    public void setFilter( String filter );
    public void setScripts( List<String> scripts );

    public void execute( MetaDataLoader loader );
}
