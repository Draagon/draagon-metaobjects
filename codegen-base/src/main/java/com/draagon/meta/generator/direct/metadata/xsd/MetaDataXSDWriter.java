package com.draagon.meta.generator.direct.metadata.xsd;

import com.draagon.meta.generator.GeneratorIOException;
import com.draagon.meta.generator.direct.metadata.xml.XMLDirectWriter;
import com.draagon.meta.loader.MetaDataLoader;

import java.io.OutputStream;

/**
 * Legacy XSD Schema writer for MetaData configuration.
 * v6.0.0: This class is deprecated - use MetaDataConstraintXSDWriter instead.
 * 
 * @deprecated Use MetaDataConstraintXSDWriter for constraint-based XSD generation
 */
@Deprecated
public class MetaDataXSDWriter extends XMLDirectWriter<MetaDataXSDWriter> {

    private String nameSpace;

    public MetaDataXSDWriter( MetaDataLoader loader, OutputStream out ) throws GeneratorIOException {
        super(loader,out);
    }

    /////////////////////////////////////////////////////////////////////////
    // Options

    public MetaDataXSDWriter withNamespace( String nameSpace ) {
        this.nameSpace = nameSpace;
        return this;
    }

    public String getNameSpace() {
        return nameSpace;
    }

    ///////////////////////////////////////////////////////////////////////////
    // MetaDataXSD Methods

    public void writeXML() throws GeneratorIOException {
        // v6.0.0: Deprecated - use constraint-based XSD writer instead
        throw new UnsupportedOperationException(
            "MetaDataXSDWriter is deprecated in v6.0.0. " +
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