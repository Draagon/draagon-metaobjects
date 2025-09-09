package com.draagon.meta.generator.direct;

import com.draagon.meta.generator.GeneratorBase;
import com.draagon.meta.generator.GeneratorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public abstract class DirectGeneratorBase extends GeneratorBase {

    protected static final Logger log = LoggerFactory.getLogger(DirectGeneratorBase.class);

    @Override
    public DirectGeneratorBase setScripts(List<String> scripts) {
        throw new GeneratorException( "A Direct Generator does not support specifying scripts");
    }

    /** Override this to handle argument parsing and validation */
    protected void parseArgs() {
        if (!hasArg(ARG_OUTPUTDIR)) throw new GeneratorException(
                "You must set a valid output directory with arg '"+ARG_OUTPUTDIR+"'");
    }
}