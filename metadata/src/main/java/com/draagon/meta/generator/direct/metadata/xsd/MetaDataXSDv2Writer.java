package com.draagon.meta.generator.direct.metadata.xsd;

import com.draagon.meta.attr.MetaAttribute;
import com.draagon.meta.generator.GeneratorIOException;
import com.draagon.meta.generator.direct.metadata.xml.XMLDirectWriter;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.loader.model.MetaModel;
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
import java.util.List;

public class MetaDataXSDv2Writer extends XMLDirectWriter<MetaDataXSDv2Writer> {

    private String nameSpace;

    //public Map<String,String> nameMap = new HashMap<String,String>();

    public MetaDataXSDv2Writer(MetaDataLoader loader, OutputStream out ) throws GeneratorIOException {
        super(loader,out);
    }

    /////////////////////////////////////////////////////////////////////////
    // Options

    public MetaDataXSDv2Writer withNamespace(String nameSpace ) {
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

        Element rootElement = doc().getDocumentElement();
        rootElement.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:xs", "http://www.w3.org/2001/XMLSchema");
        rootElement.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns", nameSpace);
        rootElement.setAttribute("targetNamespace", nameSpace);
        rootElement.setAttribute("elementFormDefault", "qualified");

        doc().setStrictErrorChecking(true);

        writeTypes( rootElement, getLoader().getTypesConfig() );
    }

    protected void writeTypes( Element el, TypesConfig tsc )  throws GeneratorIOException {

        //populateNameMap( tsc );

        for (TypeConfig tc : tsc.getTypes()) {
            writeType(el, tc);
        }
    }


    protected String getNameKey( ChildConfig cc ) {
        return getNameKey(cc.getType(),cc.getSubType());
    }

    protected String getNameKey( TypeConfig tc, SubTypeConfig stc) {
        return getNameKey(tc.getName(),stc.getName());
    }

    protected String getNameKey( String type, String subtype) {
        return type+"-"+subtype;
    }

    /*protected String getNameVal( TypeConfig tc, SubTypeConfig stc ) {
        String key = tc.getName().substring(0,1).toUpperCase() + tc.getName().substring(1);
        key = stc.getName()+key;
        return key;
    }*/

    /*protected void populateNameMap(TypesConfig tsc) {
        if ( tsc.getTypes() != null) {
            for (TypeConfig tc : tsc.getTypes()) {
                if (isRoot(tc)) continue;
                boolean first = true;
                if ( tc.getSubTypes() != null) {
                    for (SubTypeConfig stc : tc.getSubTypes()) {
                        nameMap.put(getNameKey(tc, stc), getNameVal(tc, stc, first));
                        first = false;
                    }
                }
            }
        }
    }*/

    protected boolean isRoot( TypeConfig tc ) {
        return tc.getName().equals(MetaModel.OBJECT_NAME);
    }

    protected Element addEl( Element el, String name ) throws GeneratorIOException {
        Element newEl = doc().createElement(name);
        el.appendChild( newEl );
        return newEl;
    }

    protected Element attr( Element el, String name, String val ) {
        el.setAttribute( name, val );
        return el;
    }

    protected void writeType( Element el, TypeConfig tc) throws GeneratorIOException {

        el.appendChild( doc().createTextNode("\n"));
        el.appendChild( doc().createComment("Type: "+tc.getName() ));

        if ( isRoot( tc )) {

            // <xs:element name="attr">
            Element typeEl = addEl( el,"xs:element");
            attr( typeEl,"name", tc.getName() );

            Element ctEl = addEl( typeEl,"xs:complexType");

            writeTypeChildrenForRoot( ctEl, tc, tc.getTypeChildConfigs() );
            writeTypeAttributes( ctEl, tc.getTypeChildConfigs(), true );
        }
        else {
            // <xs:element name="attr">
            el.appendChild( doc().createComment("Primary Element" ));
            Element typeEl = addEl( el,"xs:element");
            attr( typeEl,"name", tc.getName() );
            attr( typeEl,"type", tc.getName()+"-def" );

            //Element choiceEl = doc().createElement( "xs:choice");

            //Element ctEl = doc().createElement( "xs:complexType");

            writeSubTypes( el, tc );
            //writeTypeChildren( ctEl, tc, tc.getTypeChildConfigs() );
            //writeTypeAttributes( ctEl, tc, tc.getTypeChildConfigs(), true );

            //choiceEl.appendChild( ctEl );

            //typeEl.appendChild( ctEl );
        }
    }

    protected void writeSubTypes( Element el, TypeConfig tc )  throws GeneratorIOException {
        boolean first = true;
        for (SubTypeConfig stc : tc.getSubTypes()) {
            writeSubType(el, tc, stc, first);
            first = false;
        }
    }

    protected void writeSubType( Element el, TypeConfig tc, SubTypeConfig stc, boolean first ) throws GeneratorIOException {

        String name = getNameKey(tc,stc);
        String type = name+"-def";
        //String typeDef = tc.getName()+"Def";
        String baseType = tc.getName()+"-def";

        el.appendChild( doc().createTextNode("\n"));
        el.appendChild( doc().createComment("Type: "+tc.getName()+", SubType: "+stc.getName() ));

        // <xs:element name="attr">
        Element typeEl = doc().createElement( "xs:element");
        typeEl.setAttribute( "name", name );
        if (!name.equals(tc.getName())) typeEl.setAttribute( "substitutionGroup", tc.getName() );
        typeEl.setAttribute( "type", type );
        el.appendChild( typeEl );

        if (first) {
            el.appendChild( doc().createComment("Base ComplexType ["+baseType+"]" ));

            Element ctEl = doc().createElement("xs:complexType");
            ctEl.setAttribute("name", baseType);
            el.appendChild(ctEl);

            Element chEl = doc().createElement("xs:choice");
            chEl.setAttribute("maxOccurs", "unbounded");
            ctEl.appendChild(chEl);

            writeTypeChildrenForRoot( chEl, tc, tc.getTypeChildConfigs() );
            writeTypeAttributes( ctEl, tc.getTypeChildConfigs(), false );
        }

        el.appendChild( doc().createComment("SubType ComplexType ["+type+"]" ));

        Element ctEl = doc().createElement( "xs:complexType");
        ctEl.setAttribute( "name", type );
        el.appendChild( ctEl );
        Element ccEl = doc().createElement( "xs:complexContent");
        ctEl.appendChild(ccEl);
        Element extEl = doc().createElement( "xs:extension");
        extEl.setAttribute( "base", baseType);
        ccEl.appendChild(extEl);

        writeTypeChildrenForRoot( extEl, tc, stc.getChildConfigs() );
        writeTypeAttributes( extEl, stc.getChildConfigs(), false );
        //writeTypes( el, tc );
    }

    protected void writeSubTypeChildren(Element el, SubTypeConfig stc, List<ChildConfig> typeChildConfigs) throws GeneratorIOException {

        //boolean found = false;
        List<String> types = new ArrayList<>();

        //if ( typeChildConfigs != null || !typeChildConfigs.isEmpty() ) {

            Element choiceEl = doc().createElement( "xs:choice" );
            choiceEl.setAttribute("maxOccurs", "unbounded");
            el.appendChild( choiceEl );

            if ( typeChildConfigs != null ) {
                for (ChildConfig cc : typeChildConfigs) {

                    if (!types.contains(cc.getType())) {

                        //writeTypeChild(choiceEl, cc);
                        types.add(cc.getType());
                    }
                }
            }
            if ( stc.getChildConfigs() != null ) {
                for (ChildConfig cc : stc.getChildConfigs() ) {

                    if (!types.contains(cc.getType())) {

                        //writeTypeChild(choiceEl, cc);
                        types.add(cc.getType());
                    }
                }
            }

            //writeTypeAttributes( el, typeChildConfigs, false );
            //writeTypeAttributes( el, stc.getChildConfigs(), false );

            //return true;
        //}

        //return found;
    }

    protected boolean writeTypeChildrenForRoot(Element el, TypeConfig tc, List<ChildConfig> typeChildConfigs) throws GeneratorIOException {

        List<String> keys = new ArrayList<>();

        if ( typeChildConfigs == null || !typeChildConfigs.isEmpty() ) {

            Element choiceEl = doc().createElement( "xs:choice" );
            choiceEl.setAttribute("maxOccurs", "unbounded");
            el.appendChild( choiceEl );

            if ( typeChildConfigs != null ) {
                for (ChildConfig cc : typeChildConfigs) {

                    // Always draw the main one if this type is allowed at all
                    if ( !keys.contains( cc.getType())) {
                        Element ccEl = addEl( choiceEl, "xs:element");
                        attr( ccEl, "ref", cc.getType() );
                        attr( ccEl, "minOccurs", "0");
                        attr( ccEl, "maxOccurs", "unbounded");
                    }

                    String key = getNameKey(cc);
                    //String name = get(key);
                    if ( !cc.getSubType().equals("*") && !keys.contains(key)) {

                        Element ccEl = addEl( choiceEl, "xs:element");
                        attr( ccEl, "ref", key);
                        attr( ccEl, "minOccurs", "0");
                        attr( ccEl, "maxOccurs", "unbounded");

                        //writeTypeChild(choiceEl, key, cc);
                        keys.add(key);
                    }
                }
            }

            return true;
        }

        return false;
    }

    protected void writeTypeChild(Element el, String name, ChildConfig cc)  throws GeneratorIOException {

        //Element typeEl = doc().createElement( "xs:element");
        //typeEl.setAttribute( "name", name );
        //if (!name.equals(tc.getName())) typeEl.setAttribute( "substitutionGroup", tc.getName() );
        //typeEl.setAttribute( "type", type );
        //el.appendChild( typeEl );

        Element ccEl = addEl( el, "xs:element");
        attr( ccEl, "ref", name);
        attr( ccEl, "minOccurs", "0");
        attr( ccEl, "maxOccurs", "unbounded");
    }

    protected void writeTypeAttributes(Element el, List<ChildConfig> typeChildConfigs, boolean isRoot ) throws GeneratorIOException {

        writeAttribute( el, "package", "string");
        if ( !isRoot ) {
            writeAttribute( el, "name", "string");
            writeAttribute( el, "type", "string");
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

    protected void writeAttribute(Element el, String name, String type) throws GeneratorIOException {

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
