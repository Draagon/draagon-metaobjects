package com.metaobjects.mojo.test;

import com.metaobjects.generator.Generator;
import com.metaobjects.loader.MetaDataLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class GeneratorTest implements Generator {
    private static final Logger log = LoggerFactory.getLogger(GeneratorTest.class);

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

        // Debug output for Maven plugin testing - only at DEBUG level
        log.debug("Generator test execution - Loader: {}", loader);
        log.debug("Generator test execution - Args: {}", args);
        log.debug("Generator test execution - Filters: {}", filters);
        log.debug("Generator test execution - Scripts: {}", scripts);
    }
}
