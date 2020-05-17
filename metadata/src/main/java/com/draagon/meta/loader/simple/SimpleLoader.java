package com.draagon.meta.loader.simple;

import com.draagon.meta.loader.LoaderOptions;
import com.draagon.meta.loader.MetaDataLoader;

public class SimpleLoader extends MetaDataLoader {

    public final static String SUBTYPE_SIMPLE = "simple";

    public SimpleLoader(String name) {
        super(LoaderOptions.create( false, false), SUBTYPE_SIMPLE, name );
    }

    @Override
    public SimpleLoader init() {
        super.init();

        SimpleBuilder.build( this, "com/draagon/meta/loader/simple/simple.types.xml" );

        return (SimpleLoader) this;
    }
}
