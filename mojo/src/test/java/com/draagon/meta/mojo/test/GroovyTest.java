package com.draagon.meta.mojo.test;

import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.mojo.GeneratorImpl;

import java.util.List;
import java.util.Map;

public class GroovyTest implements GeneratorImpl {

    private MetaDataLoader loader = null;
    private Map<String,String> args = null;
    private String filter = null;
    private List<String> scripts = null;

    public GroovyTest() {}

    @Override
    public void setArgs(Map<String, String> args) {
        this.args = args;
    }

    @Override
    public void setFilter(String filter) {
        this.filter = filter;
    }

    @Override
    public void setScripts(List<String> scripts) {
        this.scripts = scripts;
    }

    @Override
    public void execute(MetaDataLoader loader) {
        this.loader = loader;

        System.out.println( "LOADER:  " +loader );
        System.out.println( "ARGS:    " +args );
        System.out.println( "FILTER:  " +filter );
        System.out.println( "SCRIPTS: " +scripts );
    }
}
