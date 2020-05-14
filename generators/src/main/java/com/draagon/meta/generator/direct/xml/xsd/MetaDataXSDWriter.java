package com.draagon.meta.generator.direct.xml.xsd;

import com.draagon.meta.generator.direct.MetaDataFilters;
import com.draagon.meta.generator.direct.xml.XMLDirectWriter;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.loader.config.ChildConfig;
import com.draagon.meta.loader.config.TypeConfig;
import com.draagon.meta.loader.config.TypesConfig;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.IOException;
import java.util.List;

public class MetaDataXSDWriter extends XMLDirectWriter<String> {

    protected final String nameSpace;

    public MetaDataXSDWriter(MetaDataLoader loader, String nameSpace ) {
        super( loader, null );
        this.nameSpace = nameSpace;
    }

    @Override
    public void writeXML(Context c, String filename ) {

        log.info("Writing MetaDataXSD file: " + filename );

        Element rootElement = c.out.createElementNS("example:ns:uri", "test-results-upload");
        rootElement.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns", "example:ns:uri");
        rootElement.setAttributeNS("example:ns:uri", "attr1", "XXX");
        rootElement.setAttributeNS("example:ns:uri", "attr2", "YYY");

        c.out.setDocumentURI("http://www.w3.org/2001/XMLSchema");
        //c.out.setPrefix("xs");
        c.out.setStrictErrorChecking(true);
        c.out.appendChild( rootElement );

        writeTypes( c.inc(rootElement), loader.getMetaDataConfig().getTypesConfig() );
    }

    protected void writeTypes(Context c, TypesConfig tsc ) {
        for (TypeConfig tc : tsc.getTypes()) {
            writeType(c, tc);
        }
    }

    protected void writeType( Context c, TypeConfig tc) {

        // <xs:element name="attr">
        Element typeEl = c.out.createElement( "element");
        typeEl.setAttribute( "name", tc.getTypeName() );

        c.node.appendChild( typeEl );

        writeTypeChildren( c.inc(typeEl), tc, tc.getTypeChildConfigs() );
        writeTypeAttributes( c.inc(typeEl), tc, tc.getTypeChildConfigs() );
    }

    protected void writeTypeChildren(Context c, TypeConfig tc, List<ChildConfig> typeChildConfigs) {

        //Set<String> types = getUniqueChildTypes( typeChildConfigs );

        //if ( !types.isEmpty() ) {
        //drawTypeChildStart(c, tc);

        //types.forEach(type -> writeTypeChild(c.incIndent(), tc, type));

        //drawTypeChildEnd(c, tc);
        //}
    }


    protected void writeTypeChild(Context c, TypeConfig tc, String type) {
        //pn(c,c.indent+"<xs:element ref=\""+type+"\" minOccurs=\"0\" maxOccurs=\"unbounded\"/>");
    }

    protected void writeTypeAttributes(Context c, TypeConfig tc, List<ChildConfig> typeChildConfigs) {
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
}
