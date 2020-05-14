package com.draagon.meta.generator.direct.xml.xsd;

import com.draagon.meta.generator.GeneratorMetaException;
import com.draagon.meta.generator.direct.xml.SingleXMLDirectGeneratorBase;
import com.draagon.meta.generator.direct.xml.XMLDirectWriter;
import com.draagon.meta.loader.MetaDataLoader;

public class MetaDataXSDGenerator extends SingleXMLDirectGeneratorBase {

    public final static String ARG_NAMESPACE = "nameSpace";

    private String nameSpace = null;

    //////////////////////////////////////////////////////////////////////
    // SingleFileDirectorGenerator Execute Override methods

    @Override
    protected void parseArgs() {

        if ( !hasArg( ARG_NAMESPACE)) throw new GeneratorMetaException( ARG_NAMESPACE+" argument is required" );

        nameSpace = getArg( ARG_NAMESPACE);

        if ( log.isDebugEnabled() ) log.debug(toString());
    }

    @Override
    protected XMLDirectWriter getWriter(MetaDataLoader loader) {
        return new MetaDataXSDWriter(loader, nameSpace);
    }

    ///////////////////////////////////////////////////
    // Misc Methods

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer(this.getClass().getSimpleName()+"{");
        sb.append("args=").append(getArgs());
        sb.append(", filters=").append(getFilters());
        //sb.append(", scripts=").append(getScripts());
        sb.append(", nameSpace=").append(nameSpace);
        sb.append('}');
        return sb.toString();
    }
}
