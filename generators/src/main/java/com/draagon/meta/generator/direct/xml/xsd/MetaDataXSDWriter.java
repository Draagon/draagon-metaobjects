package com.draagon.meta.generator.direct.xml.xsd;

import com.draagon.meta.generator.MetaDataWriterException;
import com.draagon.meta.generator.direct.xml.XMLDirectWriter;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.loader.config.ChildConfig;
import com.draagon.meta.loader.config.TypeConfig;
import com.draagon.meta.loader.config.TypesConfig;
import org.w3c.dom.Element;

import java.io.OutputStream;
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

    public void writeXML() throws MetaDataWriterException {

        Element rootElement = doc().createElementNS("example:ns:uri", "test-results-upload");
        rootElement.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns", "example:ns:uri");
        rootElement.setAttributeNS("example:ns:uri", "attr1", "XXX");
        rootElement.setAttributeNS("example:ns:uri", "attr2", "YYY");

        doc().setDocumentURI("http://www.w3.org/2001/XMLSchema");
        //doc().setPrefix("xs");
        doc().setStrictErrorChecking(true);
        doc().appendChild( rootElement );

        writeTypes( rootElement, getLoader().getMetaDataConfig().getTypesConfig() );
    }

    protected void writeTypes( Element el, TypesConfig tsc )  throws MetaDataWriterException {
        for (TypeConfig tc : tsc.getTypes()) {
            writeType(el, tc);
        }
    }

    protected void writeType( Element el, TypeConfig tc) throws MetaDataWriterException {

        // <xs:element name="attr">
        Element typeEl = doc().createElement( "element");
        typeEl.setAttribute( "name", tc.getTypeName() );

        el.appendChild( typeEl );

        writeTypeChildren( typeEl, tc, tc.getTypeChildConfigs() );
        writeTypeAttributes( typeEl, tc, tc.getTypeChildConfigs() );
    }

    protected void writeTypeChildren(Element el, TypeConfig tc, List<ChildConfig> typeChildConfigs) throws MetaDataWriterException {

        //Set<String> types = getUniqueChildTypes( typeChildConfigs );

        //if ( !types.isEmpty() ) {
        //drawTypeChildStart(c, tc);

        //types.forEach(type -> writeTypeChild(c.incIndent(), tc, type));

        //drawTypeChildEnd(c, tc);
        //}
    }


    protected void writeTypeChild(Element el, TypeConfig tc, String type)  throws MetaDataWriterException {
        //pn(c,c.indent+"<xs:element ref=\""+type+"\" minOccurs=\"0\" maxOccurs=\"unbounded\"/>");
    }

    protected void writeTypeAttributes(Element el, TypeConfig tc, List<ChildConfig> typeChildConfigs) throws MetaDataWriterException {
        //pn(c,c.indent+"<xs:attribute name=\"package\"/>");
        //pn(c,c.indent+"<xs:attribute name=\"type\"/>");
        //pn(c,c.indent+"<xs:attribute name=\"subtype\"/>");
        //pn(c,c.indent+"<xs:attribute name=\"_isAbstract\"/>");
    }

    //////////////////////////////////////////////////////////////////////
    // XSD Draw methods

    /*protected void drawFileStart( Context c ) {

        pn(c,"<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        pn(c,"<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"");
        pn(c,"    xmlns=\""+nameSpace+"\"");
        pn(c,"    targetNamespace=\""+nameSpace+"\"");
        pn(c,"    elementFormDefault=\"qualified\">");
    }

    protected void drawFileEnd( Context c ) {
        c.pw.println("</xs:schema>");
    }

    protected void drawTypeStart(Context c, TypeConfig tc) {
        pn(c, c.indent+"<xs:element name=\"" + tc.getTypeName() + "\">");
        pn(c, c.indent+"  <xs:complexType>");
    }

    protected void drawTypeEnd(Context c, TypeConfig tc) {
        pn(c, c.indent+"  </xs:complexType>");
        pn(c, c.indent+"</xs:element>");
    }

    protected void drawTypeChildStart(Context c, TypeConfig tc) {
        pn(c, c.indent+"<xs:choice maxOccurs=\"unbounded\">");
    }

    protected void drawTypeChildEnd(Context c, TypeConfig tc) {
        pn(c, c.indent+"</xs:choice>");
    }

    protected void drawNewLine( Context c ) {
        pn(c,"");
    }*/


    /////////////////////////////////////////////////////////////////////////
    // Misc Methods

    @Override
    protected String getToStringOptions() {
        return super.getToStringOptions()
                +",nameSpace="+nameSpace;
    }
}
