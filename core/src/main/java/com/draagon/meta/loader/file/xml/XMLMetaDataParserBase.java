package com.draagon.meta.loader.file.xml;

import com.draagon.meta.loader.base.XMLParsingUtils;
import com.draagon.meta.loader.file.FileMetaDataLoader;
import com.draagon.meta.loader.file.FileMetaDataParser;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.List;

/**
 * Base class for XML MetaData parsers that provides XML DOM navigation utilities.
 * 
 * Uses the consolidated XMLParsingUtils from metadata module to avoid code duplication.
 * 
 * @author Draagon Software
 * @since 6.0.0 (updated to use consolidated XML utilities)
 */
public abstract class XMLMetaDataParserBase extends FileMetaDataParser {

    public XMLMetaDataParserBase(FileMetaDataLoader loader, String file ) {
        super( loader, file );
    }

    /** Get the first child element */
    protected Element getFirstChildElement( Element n ) {
        return XMLParsingUtils.getFirstChildElement(n);
    }

    /**
     * Returns a collection of child elements for the given element
     */
    protected List<Element> getElements(Element e) {
        return XMLParsingUtils.getElements(e);
    }
    /**
     * Returns a collection of child elements of the given name
     * or all elements if name is null
     */
    protected List<Element> getElementsOfName(Node n, String name) {
        return XMLParsingUtils.getElementsOfName(n, name);
    }

    /**
     * Returns a collection of child elements of the given name
     * or all elements if name is null
     */
    protected List<Element> getElementsOfName(Node n, String name, boolean firstOnly) {
        return XMLParsingUtils.getElementsOfName(n, name, firstOnly);
    }
}
