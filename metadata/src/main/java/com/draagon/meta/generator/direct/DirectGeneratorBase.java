package com.draagon.meta.generator.direct;

import com.draagon.meta.generator.GeneratorBase;
import com.draagon.meta.generator.GeneratorException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;

public abstract class DirectGeneratorBase extends GeneratorBase {

    protected Log log = LogFactory.getLog( this.getClass() );

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