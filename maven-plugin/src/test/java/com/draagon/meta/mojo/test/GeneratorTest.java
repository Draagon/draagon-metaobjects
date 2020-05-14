package com.draagon.meta.mojo.test;

import com.draagon.meta.generator.Generator;
import com.draagon.meta.loader.MetaDataLoader;

import java.util.List;
import java.util.Map;

public class GeneratorTest implements Generator {

    private MetaDataLoader loader = null;
    private Map<String,String> args = null;
    private List<String> filters = null;
    private List<String> scripts = null;

    public GeneratorTest() {}

    @Override
    public GeneratorTest setArgs(Map<String, String> args) {
        this.args = args;
        return this;
    }

    @Override
    public GeneratorTest setFilters(List<String> filters) {
        this.filters = filters;
        return this;
    }

    @Override
    public GeneratorTest setScripts(List<String> scripts) {
        this.scripts = scripts;
        return this;
    }

    @Override
    public void execute(MetaDataLoader loader) {
        this.loader = loader;

        System.out.println( "LOADER:  "  + loader );
        System.out.println( "ARGS:    "  + args );
        System.out.println( "FILTERS:  " + filters );
        System.out.println( "SCRIPTS: "  + scripts );
    }
}
