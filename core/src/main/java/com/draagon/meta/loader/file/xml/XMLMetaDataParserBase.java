package com.draagon.meta.loader.file.xml;

import com.draagon.meta.loader.file.FileMetaDataLoader;
import com.draagon.meta.loader.file.MetaDataParser;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

public abstract class XMLMetaDataParserBase extends MetaDataParser {

    public XMLMetaDataParserBase(FileMetaDataLoader loader, String file ) {
        super( loader, file );
    }


    /** Get the first child element */
    protected Element getFirstChildElement( Element n ) {
        return getElementsOfName( n, null, true ).iterator().next();
    }

    /**
     * Returns a collection of child elements for the given element
     */
    protected List<Element> getElements(Element e) {
        return getElementsOfName( e, null, false );
    }
    /**
     * Returns a collection of child elements of the given name
     * or all elements if name is null
     */
    protected List<Element> getElementsOfName(Node n, String name) {
        if ( name == null ) throw new IllegalArgumentException("Name cannot be null");
        return getElementsOfName( n, name, false );
    }

    /**
     * Returns a collection of child elements of the given name
     * or all elements if name is null
     */
    protected List<Element> getElementsOfName(Node n, String name, boolean firstOnly) {

        if ( n == null ) throw new IllegalArgumentException("Node cannot be null");

        ArrayList<Element> elements = new ArrayList<>();
        if (n == null) return elements;

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
