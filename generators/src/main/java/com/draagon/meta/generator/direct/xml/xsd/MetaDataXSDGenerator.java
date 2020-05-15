package com.draagon.meta.generator.direct.xml.xsd;

import com.draagon.meta.generator.GeneratorMetaException;
import com.draagon.meta.generator.MetaDataWriter;
import com.draagon.meta.generator.MetaDataWriterException;
import com.draagon.meta.generator.direct.xml.SingleXMLDirectGeneratorBase;
import com.draagon.meta.generator.direct.xml.XMLDirectWriter;
import com.draagon.meta.loader.MetaDataLoader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

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
    protected XMLDirectWriter getWriter(MetaDataLoader loader, OutputStream os ) throws MetaDataWriterException {
        return new MetaDataXSDWriter(loader, os)
                .withNamespace(nameSpace);
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
