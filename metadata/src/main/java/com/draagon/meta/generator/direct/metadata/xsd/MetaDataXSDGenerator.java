package com.draagon.meta.generator.direct.metadata.xsd;

import com.draagon.meta.generator.GeneratorException;
import com.draagon.meta.generator.GeneratorIOException;
import com.draagon.meta.generator.direct.metadata.xml.SingleXMLDirectGeneratorBase;
import com.draagon.meta.generator.direct.metadata.xml.XMLDirectWriter;
import com.draagon.meta.loader.MetaDataLoader;

import java.io.OutputStream;

public class MetaDataXSDGenerator extends SingleXMLDirectGeneratorBase {

    public final static String ARG_NAMESPACE = "nameSpace";

    private String nameSpace = null;

    //////////////////////////////////////////////////////////////////////
    // SingleFileDirectorGenerator Execute Override methods

    @Override
    protected void parseArgs() {

        if ( !hasArg( ARG_NAMESPACE)) throw new GeneratorException( ARG_NAMESPACE+" argument is required" );

        nameSpace = getArg( ARG_NAMESPACE);

        if ( log.isDebugEnabled() ) log.debug(toString());
    }

    @Override
    protected XMLDirectWriter getWriter(MetaDataLoader loader, OutputStream os ) throws GeneratorIOException {
        return new MetaDataXSDWriter(loader, os)
                .withNamespace(nameSpace);
    }

    ///////////////////////////////////////////////////
    // Misc Methods

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(this.getClass().getSimpleName()+"{");
        sb.append("args=").append(getArgs());
        sb.append(", filters=").append(getFilters());
        sb.append(", nameSpace=").append(nameSpace);
        sb.append('}');
        return sb.toString();
    }
}
