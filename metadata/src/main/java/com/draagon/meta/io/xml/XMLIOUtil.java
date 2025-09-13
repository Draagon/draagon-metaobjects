package com.draagon.meta.io.xml;

import com.draagon.meta.DataTypes;
import com.draagon.meta.MetaData;
import com.draagon.meta.attr.MetaAttribute;
import com.draagon.meta.field.MetaField;
import com.draagon.meta.io.MetaDataIO;
import com.draagon.meta.io.MetaDataIOException;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.util.DataConverter;
import com.draagon.meta.util.MetaDataUtil;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

import static com.draagon.meta.io.xml.XMLIOConstants.*;

public class XMLIOUtil {

    public static String getXmlName( MetaData md ) {

        // Return null for the XML Name so it pulls all children and sets the type field
        if ( md instanceof MetaObject && isXmlTyped( (MetaObject) md ))
            return null;

        if ( md.hasMetaAttr( ATTR_XMLNAME )) {
            return md.getMetaAttr( ATTR_XMLNAME ).getValueAsString();
        }
        return md.getShortName();
    }

    public static boolean isXmlTyped( MetaObject mo ) {
        if (mo.hasMetaAttr(ATTR_XMLTYPED)) {
            MetaAttribute<?> a = mo.getMetaAttr(ATTR_XMLTYPED);
            if (a.getValue() == null) return false;
            return true;
        }
        return false;
    }

    public static String getXmlTypedField( MetaObject mo ) {
        if (mo.hasMetaAttr(ATTR_XMLTYPED)) {
            MetaAttribute<?> a = mo.getMetaAttr(ATTR_XMLTYPED);
            if (a.getValue() == null) return null;
            return a.getValueAsString();
        }
        return null;
    }

    public static boolean ifXmlIgnore( MetaField<?> mf ) {
        return xmlBoolean(mf, ATTR_XMLIGNORE);
    }

    public static boolean xmlBoolean(MetaField<?> mf, String attrName) {
        if (mf.hasMetaAttr(attrName)) {
            MetaAttribute<?> a = mf.getMetaAttr(attrName);
            if (a.getValue() == null) return false;
            return DataConverter.toBoolean(a.getValue());
        }
        return false;
    }

    public static boolean isXmlAttr( MetaField<?> mf ) {
        return xmlBoolean(mf, ATTR_ISXMLATTR);
    }

    public static boolean xmlWrap( MetaField<?> mf ) {

        boolean wrap = true;

        if ( mf.hasMetaAttr( ATTR_XMLWRAP )) {
            MetaAttribute<?> isXml = mf.getMetaAttr( ATTR_XMLWRAP );
            wrap = DataConverter.toBoolean( isXml.getValue() );
        }
        else if ( mf.getDataType() == DataTypes.OBJECT ) {
            wrap = false;
        }
        else if ( mf.getDataType() == DataTypes.OBJECT_ARRAY ) {
            /*try {
                MetaObject objRef = MetaDataUtil.getObjectRef(mf);
                if ( getXmlName( objRef ).equals( getXmlName( mf ))) {
                    wrap = false;
                }
            }
            catch( MetaObjectNotFoundException ignore ) {
                wrap = true;
            }*/
            wrap = true;
        }

        return wrap;
    }

    public static boolean hasObjectRef(MetaDataIO io, MetaField<?> mf ) throws MetaDataIOException {
        return MetaDataUtil.hasObjectRef(mf);
    }

    public static MetaObject getObjectRef(MetaDataIO io, MetaField<?> mf ) throws MetaDataIOException {
        return MetaDataUtil.getObjectRef(mf);
        /*ObjectReference oref = mf.getFirstObjectReference();
        if ( oref == null ) throw new MetaDataIOException( io, "No ObjectReference existed ["+mf+"]" );
        MetaObject refmo = oref.getReferencedObject();
        if ( refmo == null ) throw new MetaDataIOException( io, "No MetaObject reference not found ["+refmo+"]" );
        return refmo;*/
    }

    /*public static Element getFirstElementByName(MetaDataIO io, Element e, MetaField mf ) throws MetaDataIOException {

        String xmlName = getXmlName( mf);
        Node node = e.getElementsByTagName(xmlName).item(0);
        if (node == null) return null;
        if ( node instanceof Element ) return (Element) e;
        throw new MetaDataIOException( io, "Node found by name ["+xmlName+"] was not an Element ["+node+"]");
    }

    public static List<Element> getElementsByName(MetaDataIO io, Element e, MetaData md ) throws MetaDataIOException {

        List<Element> elements = new ArrayList<>();
        String xmlName = getXmlName( md);
        NodeList nodeList = e.getElementsByTagName(xmlName);
        for( int i=0; i < nodeList.getLength(); i++ ) {
            Node node = nodeList.item(i);
            if ( node instanceof Element ) {
                elements.add((Element) node);
            } else {
                throw new MetaDataIOException( io, "Node found by name ["+xmlName+"] was not an Element ["+node+"]");
            }
        }
        return elements;
    }*/

    /**
     * Returns a collection of child elements of the given name
     * or all elements if name is null
     */
    public static Element getFirstChildElementOfName(Node n, String name) {
        //if ( name == null ) throw new IllegalArgumentException("Name cannot be null");
        List<Element> els = getChildElementsOfName( n, name, true );
        if ( els.isEmpty() ) return null;
        return els.iterator().next();
    }

    /**
     * Returns a collection of child elements of the given name
     * or all elements if name is null
     */
    public static List<Element> getChildElementsOfName(Node n, String name) {
        //if ( name == null ) throw new IllegalArgumentException("Name cannot be null");
        return getChildElementsOfName( n, name, false );
    }

    /**
     * Returns a collection of child elements of the given name
     * or all elements if name is null
     */
    public static List<Element> getChildElementsOfName(Node n, String name, boolean firstOnly) {

        if ( n == null ) throw new IllegalArgumentException("Node cannot be null");

        ArrayList<Element> elements = new ArrayList<>();

        NodeList list = n.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            Node node = list.item(i);
            if (node instanceof Element && ( name == null
                    || node.getNodeName().equals(name))) {

                elements.add((Element) node);
                if ( firstOnly ) break;
            }
        }

        return elements;
    }
}
