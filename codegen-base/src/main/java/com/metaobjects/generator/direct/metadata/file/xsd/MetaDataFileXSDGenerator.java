package com.metaobjects.generator.direct.metadata.file.xsd;

import com.metaobjects.generator.GeneratorException;
import com.metaobjects.generator.GeneratorIOException;
import com.metaobjects.generator.direct.metadata.xml.SingleXMLDirectGeneratorBase;
import com.metaobjects.generator.direct.metadata.xml.XMLDirectWriter;
import com.metaobjects.loader.MetaDataLoader;

import java.io.OutputStream;

/**
 * Generator for creating XSD Schema that validates metadata files themselves.
 * v6.0.0: Creates XSD schemas for validating the structure of metadata XML files
 * (like <metadata><children>...</children></metadata>), not the data instances.
 * 
 * This reads constraint definitions to understand valid metadata structure and
 * generates XSD Schema that can validate metadata files during development.
 */
public class MetaDataFileXSDGenerator extends SingleXMLDirectGeneratorBase {

    public final static String ARG_NAMESPACE = "nameSpace";
    public final static String ARG_TARGET_NAMESPACE = "targetNamespace";
    public final static String ARG_ELEMENT_FORM_DEFAULT = "elementFormDefault";

    private String nameSpace = null;
    private String targetNamespace = null;
    private String elementFormDefault = "qualified";

    //////////////////////////////////////////////////////////////////////
    // SingleFileDirectorGenerator Execute Override methods

    @Override
    protected void parseArgs() {

        if (!hasArg(ARG_NAMESPACE)) {
            throw new GeneratorException(ARG_NAMESPACE + " argument is required");
        }

        nameSpace = getArg(ARG_NAMESPACE);
        
        if (hasArg(ARG_TARGET_NAMESPACE)) {
            targetNamespace = getArg(ARG_TARGET_NAMESPACE);
        } else {
            targetNamespace = nameSpace; // Default to same as namespace
        }
        
        if (hasArg(ARG_ELEMENT_FORM_DEFAULT)) {
            elementFormDefault = getArg(ARG_ELEMENT_FORM_DEFAULT);
        }

        if (log.isDebugEnabled()) log.debug(toString());
    }

    @Override
    protected XMLDirectWriter getWriter(MetaDataLoader loader, OutputStream os) throws GeneratorIOException {
        return new MetaDataFileXSDWriter(loader, os)
                .withNamespace(nameSpace)
                .withTargetNamespace(targetNamespace)
                .withElementFormDefault(elementFormDefault);
    }

    ///////////////////////////////////////////////////
    // Misc Methods

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(this.getClass().getSimpleName() + "{");
        sb.append("args=").append(getArgs());
        sb.append(", filters=").append(getFilters());
        sb.append(", nameSpace=").append(nameSpace);
        sb.append(", targetNamespace=").append(targetNamespace);
        sb.append(", elementFormDefault=").append(elementFormDefault);
        sb.append('}');
        return sb.toString();
    }

    ///////////////////////////////////////////////////
    // Service Provider Pattern Registration

    // XSD generation attribute constants
    public static final String XSD_NAMESPACE = "xsdNamespace";
    public static final String XSD_TARGET_NAMESPACE = "xsdTargetNamespace";
    public static final String XSD_ELEMENT_FORM_DEFAULT = "xsdElementFormDefault";
    public static final String XSD_ELEMENT_NAME = "xsdElementName";
    public static final String XSD_TYPE_NAME = "xsdTypeName";

    /**
     * Registers XSD generation attributes for use by the service provider pattern.
     * Called by CodeGenMetaDataProvider to extend existing MetaData types with XSD-specific attributes.
     */
    public static void registerXSDAttributes(com.metaobjects.registry.MetaDataRegistry registry) {
        // Object-level XSD attributes
        registry.findType("object", "base")
            .optionalAttribute(XSD_NAMESPACE, "string")
            .optionalAttribute(XSD_TARGET_NAMESPACE, "string")
            .optionalAttribute(XSD_ELEMENT_FORM_DEFAULT, "string");

        registry.findType("object", "pojo")
            .optionalAttribute(XSD_ELEMENT_NAME, "string")
            .optionalAttribute(XSD_TYPE_NAME, "string");

        // Field-level XSD attributes
        registry.findType("field", "base")
            .optionalAttribute(XSD_ELEMENT_NAME, "string")
            .optionalAttribute(XSD_TYPE_NAME, "string");

        registry.findType("field", "string")
            .optionalAttribute(XSD_ELEMENT_NAME, "string");
    }
}