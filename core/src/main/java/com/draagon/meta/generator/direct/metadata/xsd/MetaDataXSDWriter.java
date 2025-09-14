package com.draagon.meta.generator.direct.metadata.xsd;

import com.draagon.meta.attr.*;
import com.draagon.meta.generator.GeneratorIOException;
import com.draagon.meta.generator.direct.metadata.xml.XMLDirectWriter;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.loader.types.ChildConfig;
import com.draagon.meta.loader.types.SubTypeConfig;
import com.draagon.meta.loader.types.TypeConfig;
import com.draagon.meta.loader.types.TypesConfig;
import com.draagon.meta.util.XMLUtil;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

public class MetaDataXSDWriter extends XMLDirectWriter<MetaDataXSDWriter> {

    private static final String METADATA_TYPE_ENUM = "typeEnum";

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

    protected Document createDocument() throws GeneratorIOException {
        try {
            DocumentBuilder db = XMLUtil.getBuilder();
            DOMImplementation domImpl = db.getDOMImplementation();
            return domImpl.createDocument( "http://www.w3.org/2001/XMLSchema", "xs:schema", null );
        } catch( IOException e ) {
            throw new GeneratorIOException( this, "Error creating XML Builder: "+e, e );
        }
    }

    public void writeXML() throws GeneratorIOException {

        if (nameSpace == null) throw new GeneratorIOException(this, "No nameSpace was set for generating XSD file ["+getFilename()+"]");

        Element rootElement = doc().getDocumentElement();
        rootElement.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:xs", "http://www.w3.org/2001/XMLSchema");
        rootElement.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns", nameSpace);
        rootElement.setAttribute("targetNamespace", nameSpace);
        rootElement.setAttribute("elementFormDefault", "qualified");

        doc().setStrictErrorChecking(true);

        writeTypes( rootElement, getLoader().getTypesConfig() );
    }

    protected void writeTypes( Element el, TypesConfig tsc )  throws GeneratorIOException {
        if ( tsc.getTypes() != null && !tsc.getTypes().isEmpty()) {
            for (TypeConfig tc : tsc.getTypes()) {
                writeType(el, tc);
            }
        }
        else {
            log.warn("There are no Type Configurations defined in MetaDataLoader: " + getLoader());
        }
    }

    protected void writeType( Element el, TypeConfig tc) throws GeneratorIOException {

        // <xs:element name="attr">
        Element typeEl = doc().createElement( "xs:element");
        typeEl.setAttribute( "name", tc.getName() );
        el.appendChild( typeEl );

        Element ctEl = doc().createElement( "xs:complexType");
        Element intoEl = ctEl;

        if ( tc.getName().equals("attr")) {
            //<xs:simpleContent>
            //<xs:extension base="xs:string">
            Element scEl = doc().createElement("xs:simpleContent");
            ctEl.appendChild(scEl);

            Element extEl = doc().createElement("xs:extension");
            extEl.setAttribute("base", "xs:string");
            scEl.appendChild(extEl);

            intoEl = extEl;
        }

        List<ChildConfig> kids = new ArrayList<>();
        if ( tc.getTypeChildConfigs() != null ) kids.addAll( tc.getTypeChildConfigs() );
        if ( tc.getSubTypes() != null ) {
            for (SubTypeConfig stc : tc.getSubTypes()) {
                if ( stc.getChildConfigs() != null ) {
                    kids.addAll( stc.getChildConfigs() );
                }
            }
        }
        writeTypeChildren( intoEl, tc, kids );
        writeTypeAttributes( intoEl, tc, kids );

        typeEl.appendChild( ctEl );
    }

    protected boolean writeTypeChildren(Element el, TypeConfig tc, List<ChildConfig> typeChildConfigs) throws GeneratorIOException {

        List<String> types = new ArrayList<>();

        if ( typeChildConfigs == null || !typeChildConfigs.isEmpty() ) {

            Element choiceEl = doc().createElement( "xs:choice" );
            choiceEl.setAttribute("maxOccurs", "unbounded");
            el.appendChild( choiceEl );

            if ( typeChildConfigs != null ) {
                for (ChildConfig cc : typeChildConfigs) {

                    if (!types.contains(cc.getType())) {

                        writeTypeChild(choiceEl, cc);
                        types.add(cc.getType());
                    }
                }
            }

            return true;
        }

        return false;
    }


    protected void writeTypeChild(Element el, ChildConfig cc)  throws GeneratorIOException {

        Element ccEl = doc().createElement( "xs:element");
        ccEl.setAttribute( "ref", cc.getType() );
        ccEl.setAttribute( "minOccurs", "0");
        ccEl.setAttribute( "maxOccurs", "unbounded");
        el.appendChild( ccEl );
    }

    protected void writeTypeAttributes(Element el, TypeConfig tc, List<ChildConfig> typeChildConfigs) throws GeneratorIOException {

        writeAttribute( el, "package", "string");
        if ( !tc.getName().equals("metadata")) {
            writeAttribute( el, "name", "string");
            writeAttribute( el, "type", METADATA_TYPE_ENUM, tc.getSubTypeNames());
            writeAttribute( el, "super", "string");
        }

        List<String> names = new ArrayList<>();

        if ( typeChildConfigs != null ) {
            for (ChildConfig cc : typeChildConfigs) {

                if (MetaAttribute.TYPE_ATTR.equals(cc.getType()) && !names.contains(cc.getName())) {
                    writeAttribute(el, cc.getName(), cc.getSubType());
                    names.add(cc.getName());
                }
            }
        }
    }

    /*protected void writeTypeAttribute(Element el, ChildConfig childConfig) throws MetaDataWriterException {

        Element attrEl = doc().createElement( "xs:attribute" );
        attrEl.setAttribute("name", childConfig.getName() );
        el.appendChild( attrEl );
    }*/

    protected void writeAttribute(Element el, String name, String type) throws GeneratorIOException {
        writeAttribute(el, name, type, null);
    }

    protected void writeAttribute(Element el, String name, String type, Collection<String> enumVals ) throws GeneratorIOException {

        Element attrEl = doc().createElement( "xs:attribute");
        attrEl.setAttribute("name", name );

        boolean found = true;
        Element rEl = doc().createElement("xs:restriction");

        // TODO:  Use Types Config to load the MetaData and then get the DataType

        if ( type.equals(METADATA_TYPE_ENUM)) {
            rEl.setAttribute("base", "xs:string");
            if ( enumVals != null ) {
                //<xs:enumeration value = "boolean" / >
                for (String val : enumVals ) {
                    Element enumEl = doc().createElement("xs:enumeration");
                    enumEl.setAttribute("value", val );
                    rEl.appendChild( enumEl );
                }
            }
        }
        else if ( type.equals(StringAttribute.SUBTYPE_STRING)
                || type.equals(StringArrayAttribute.SUBTYPE_STRING_ARRAY)) {
            rEl.setAttribute("base", "xs:string");
        }
        else if ( type.equals(LongAttribute.SUBTYPE_LONG)) {
            rEl.setAttribute("base", "xs:long");
        }
        else if ( type.equals(PropertiesAttribute.SUBTYPE_PROPERTIES)) {
            // TODO: Use regex pattern for properties
            rEl.setAttribute("base", "xs:string");
        }
        else if ( type.equals(ClassAttribute.SUBTYPE_CLASS)) {
            // TODO: Use regex pattern for class
            rEl.setAttribute("base", "xs:string");
        }
        else if ( type.equals(IntAttribute.SUBTYPE_INT)) {
            rEl.setAttribute("base", "xs:integer");
        }
        else if ( type.equals(BooleanAttribute.SUBTYPE_BOOLEAN)) {
            rEl.setAttribute("base", "xs:boolean");
        }
        else {
            found = false;
        }

        if ( found ) {
            Element stEl = doc().createElement("xs:simpleType");
            attrEl.appendChild(stEl);
            stEl.appendChild(rEl);
        }

        el.appendChild( attrEl );
    }

    ////////////////////////////////////////////////////////////////////
    // Misc Methods

    @Override
    protected String getToStringOptions() {
        return super.getToStringOptions()
                +",nameSpace="+nameSpace;
    }
}