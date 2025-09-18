package com.draagon.meta.generator.direct.metadata.xsd;

import com.draagon.meta.generator.GeneratorIOException;
import com.draagon.meta.generator.direct.metadata.xml.XMLDirectWriter;
import com.draagon.meta.loader.MetaDataLoader;

import java.io.OutputStream;

/**
 * Legacy XSDv2 Schema writer for MetaData configuration.
 * v6.0.0: This class is deprecated - use MetaDataConstraintXSDWriter instead.
 * 
 * @deprecated Use MetaDataConstraintXSDWriter for constraint-based XSD generation
 */
@Deprecated
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
        // v6.0.0: Deprecated - use constraint-based XSD writer instead
        throw new UnsupportedOperationException(
            "MetaDataXSDv2Writer is deprecated in v6.0.0. " +
            "Use MetaDataConstraintXSDWriter for constraint-based XSD schema generation instead."
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