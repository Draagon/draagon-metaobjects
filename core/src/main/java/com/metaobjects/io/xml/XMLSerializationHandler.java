package com.metaobjects.io.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public interface XMLSerializationHandler {

    public String getXmlAttr(Object o);
    public void setXmlAttr(Object o, String s);

    public void writeXmlValue(Object o, String xmlName, Document doc, Element e);
    public void readXmlValue(Object o, String xmlName, Element e);
}