package com.draagon.meta.loader.parser.xml;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for XML DOM navigation and element processing.
 * 
 * Provides common XML parsing utilities that can be used by various XML parsers
 * throughout the MetaObjects framework. This consolidates XML-specific logic
 * to avoid duplication across multiple XML parser implementations.
 * 
 * @author Draagon Software
 * @since 6.0.0 (consolidation from XMLMetaDataParserBase)
 */
public class XMLParsingUtils {

    /** Get the first child element */
    public static Element getFirstChildElement(Element n) {
        return getElementsOfName(n, null, true).iterator().next();
    }

    /**
     * Returns a collection of child elements for the given element
     */
    public static List<Element> getElements(Element e) {
        return getElementsOfName(e, null, false);
    }

    /**
     * Returns a collection of child elements of the given name
     * or all elements if name is null
     */
    public static List<Element> getElementsOfName(Node n, String name) {
        if (name == null) throw new IllegalArgumentException("Name cannot be null");
        return getElementsOfName(n, name, false);
    }

    /**
     * Returns a collection of child elements of the given name
     * or all elements if name is null
     */
    public static List<Element> getElementsOfName(Node n, String name, boolean firstOnly) {

        if (n == null) throw new IllegalArgumentException("Node cannot be null");

        ArrayList<Element> elements = new ArrayList<>();
        if (n == null) return elements;

        NodeList list = n.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            Node node = list.item(i);
            if (node instanceof Element && (name == null
                    || node.getNodeName().equals(name))) {

                elements.add((Element) node);
                if (firstOnly) break;
            }
        }

        return elements;
    }
}