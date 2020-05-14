package com.draagon.meta.generator.direct;

import com.draagon.meta.MetaData;
import com.draagon.meta.MetaDataException;
import com.draagon.meta.generator.Generator;
import com.draagon.meta.generator.GeneratorBase;
import com.draagon.meta.generator.GeneratorMetaException;
import com.draagon.meta.generator.WriterBase;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.object.MetaObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public abstract class DirectGeneratorBase extends GeneratorBase {

    protected Log log = LogFactory.getLog( this.getClass() );

    @Override
    public DirectGeneratorBase setScripts(List<String> scripts) {
        throw new GeneratorMetaException( "A Direct Generator does not support specifying scripts");
    }

    protected void parseArgs() { }

    protected abstract WriterBase getWriter(MetaDataLoader loader);
}