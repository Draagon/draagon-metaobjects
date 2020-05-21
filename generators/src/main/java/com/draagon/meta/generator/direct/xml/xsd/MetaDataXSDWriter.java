package com.draagon.meta.generator.direct.xml.xsd;

import com.draagon.meta.attr.MetaAttribute;
import com.draagon.meta.generator.MetaDataWriterException;
import com.draagon.meta.generator.direct.xml.XMLDirectWriter;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.loader.types.ChildConfig;
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
import java.util.List;

public class MetaDataXSDWriter extends XMLDirectWriter<MetaDataXSDWriter> {

    private String nameSpace;

    public MetaDataXSDWriter( MetaDataLoader loader, OutputStream out ) throws MetaDataWriterException {
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

    protected Document createDocument() throws MetaDataWriterException {
        try {
            DocumentBuilder db = XMLUtil.getBuilder();
            DOMImplementation domImpl = db.getDOMImplementation();
            return domImpl.createDocument( "http://www.w3.org/2001/XMLSchema", "xs:schema", null );
        } catch( IOException e ) {
            throw new MetaDataWriterException( this, "Error creating XML Builder: "+e, e );
        }
    }

    public void writeXML() throws MetaDataWriterException {

        Element rootElement = doc().getDocumentElement();
        rootElement.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:xs", "http://www.w3.org/2001/XMLSchema");
        rootElement.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns", nameSpace);
        rootElement.setAttribute("targetNamespace", nameSpace);
        rootElement.setAttribute("elementFormDefault", "qualified");

        doc().setStrictErrorChecking(true);

        writeTypes( rootElement, getLoader().getTypesConfig() );
    }

    protected void writeTypes( Element el, TypesConfig tsc )  throws MetaDataWriterException {
        for (TypeConfig tc : tsc.getTypes()) {
            writeType(el, tc);
        }
    }

    protected void writeType( Element el, TypeConfig tc) throws MetaDataWriterException {

        // <xs:element name="attr">
        Element typeEl = doc().createElement( "xs:element");
        typeEl.setAttribute( "name", tc.getName() );
        el.appendChild( typeEl );

        Element ctEl = doc().createElement( "xs:complexType");

        writeTypeChildren( ctEl, tc, tc.getTypeChildConfigs() );
        writeTypeAttributes( ctEl, tc, tc.getTypeChildConfigs() );

        typeEl.appendChild( ctEl );
    }

    protected boolean writeTypeChildren(Element el, TypeConfig tc, List<ChildConfig> typeChildConfigs) throws MetaDataWriterException {

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


    protected void writeTypeChild(Element el, ChildConfig cc)  throws MetaDataWriterException {

        Element ccEl = doc().createElement( "xs:element");
        ccEl.setAttribute( "ref", cc.getType() );
        ccEl.setAttribute( "minOccurs", "0");
        ccEl.setAttribute( "maxOccurs", "unbounded");
        el.appendChild( ccEl );
    }

    protected void writeTypeAttributes(Element el, TypeConfig tc, List<ChildConfig> typeChildConfigs) throws MetaDataWriterException {

        writeAttribute( el, "package", "string");
        if ( !tc.getName().equals("metadata")) {
            writeAttribute( el, "type", "string");
            writeAttribute( el, "subtype", "string");
            writeAttribute( el, "super", "string");
        }

        if ( typeChildConfigs != null ) {
            for (ChildConfig cc : typeChildConfigs) {

                if (MetaAttribute.TYPE_ATTR.equals(cc.getType())) {
                    writeAttribute(el, cc.getName(), cc.getSubType());
                }
            }
        }
    }

    /*protected void writeTypeAttribute(Element el, ChildConfig childConfig) throws MetaDataWriterException {

        Element attrEl = doc().createElement( "xs:attribute" );
        attrEl.setAttribute("name", childConfig.getName() );
        el.appendChild( attrEl );
    }*/

    protected void writeAttribute(Element el, String name, String type) throws MetaDataWriterException {

        Element attrEl = doc().createElement( "xs:attribute");
        attrEl.setAttribute("name", name );

        if ( type.equals("string")
                || type.equals("stringArray")
                || type.equals("int")
                || type.equals("boolean")) {

            Element stEl = doc().createElement("xs:simpleType");
            attrEl.appendChild(stEl);
            Element rEl = doc().createElement("xs:restriction");

            if ( type.equals("string") || type.equals("stringArray")) {
                rEl.setAttribute("base", "xs:string");
            }
            else if ( type.equals("integer")) {
                rEl.setAttribute("base", "xs:integer");
            }
            else if ( type.equals("boolean")) {
                rEl.setAttribute("base", "xs:boolean");
            }

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
