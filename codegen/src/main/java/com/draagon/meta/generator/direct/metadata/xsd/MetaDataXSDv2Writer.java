package com.draagon.meta.generator.direct.metadata.xsd;

import com.draagon.meta.generator.GeneratorIOException;
import com.draagon.meta.generator.direct.metadata.xml.XMLDirectWriter;
import com.draagon.meta.loader.MetaDataLoader;

import java.io.OutputStream;

/**
 * XSDv2 Schema writer for MetaData configuration.
 * v6.0.0: Temporarily disabled pending ValidationChain-based schema generation implementation.
 * 
 * TODO: Implement ValidationChain-based XSD schema generation in future version
 */
public class MetaDataXSDv2Writer extends XMLDirectWriter<MetaDataXSDv2Writer> {

    private String nameSpace;

    public MetaDataXSDv2Writer( MetaDataLoader loader, OutputStream out ) throws GeneratorIOException {
        super(loader,out);
    }

    /////////////////////////////////////////////////////////////////////////
    // Options

    public MetaDataXSDv2Writer withNamespace( String nameSpace ) {
        this.nameSpace = nameSpace;
        return this;
    }

    public String getNameSpace() {
        return nameSpace;
    }

    ///////////////////////////////////////////////////////////////////////////
    // MetaDataXSDv2 Methods

    public void writeXML() throws GeneratorIOException {
        // v6.0.0: Temporarily throw exception pending ValidationChain implementation
        throw new UnsupportedOperationException(
            "MetaDataXSDv2Writer is temporarily disabled in v6.0.0. " +
            "XSD schema generation will be reimplemented using ValidationChain in a future version. " +
            "Please use ValidationChain-based validation instead of XSD schema validation for now."
        );
    }

    ////////////////////////////////////////////////////////////////////
    // Misc Methods

    @Override
    protected String getToStringOptions() {
        return super.getToStringOptions()
                +",nameSpace="+nameSpace;
    }
}