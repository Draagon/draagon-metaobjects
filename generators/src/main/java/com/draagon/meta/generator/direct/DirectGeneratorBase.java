package com.draagon.meta.generator.direct;

import com.draagon.meta.generator.GeneratorBase;
import com.draagon.meta.generator.GeneratorMetaException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;

public abstract class DirectGeneratorBase extends GeneratorBase {

    protected Log log = LogFactory.getLog( this.getClass() );

    @Override
    public DirectGeneratorBase setScripts(List<String> scripts) {
        throw new GeneratorMetaException( "A Direct Generator does not support specifying scripts");
    }

    protected void parseArgs() { }
}