package com.draagon.meta.loader.file.json;

import com.draagon.meta.MetaData;
import com.draagon.meta.MetaException;
import com.draagon.meta.attr.MetaAttribute;
import com.draagon.meta.attr.StringAttribute;
import com.draagon.meta.loader.config.MetaDataConfig;
import com.draagon.meta.loader.config.TypeConfig;
import com.draagon.meta.loader.file.FileMetaDataLoader;
import com.draagon.meta.loader.file.MetaDataParser;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Json MetaData Loader
 */
public class JsonMetaDataParser extends MetaDataParser {

    private static Log log = LogFactory.getLog(JsonMetaDataParser.class);

    public final static String ATTR_METADATA    = "metadata";
    public final static String ATTR_TYPESCONFIG = "typesConfig";
    public final static String ATTR_TYPES       = "types";
    public final static String ATTR_PACKAGE     = "package";
    public final static String ATTR_CHILDREN    = "children";
    public final static String ATTR_NAME        = "name";
    public final static String ATTR_CLASS       = "class";
    public final static String ATTR_TYPE        = "type";
    public final static String ATTR_SUBTYPE     = "subtype";
    public final static String ATTR_SUBTYPES    = "subtypes";
    public final static String ATTR_SUPER       = "super";
    public final static String ATTR_VALUE       = "value";

    protected static List<String> reservedAttributes = new ArrayList<>();
    static {
        reservedAttributes.add( ATTR_METADATA );
        reservedAttributes.add( ATTR_PACKAGE );
        reservedAttributes.add( ATTR_NAME );
        reservedAttributes.add( ATTR_CLASS );
        reservedAttributes.add( ATTR_TYPES );
        reservedAttributes.add( ATTR_CHILDREN );
        reservedAttributes.add( ATTR_TYPE );
        reservedAttributes.add( ATTR_SUBTYPE );
        reservedAttributes.add( ATTR_SUBTYPES );
        reservedAttributes.add( ATTR_SUPER );
        reservedAttributes.add( ATTR_VALUE );
    }

    public JsonMetaDataParser(FileMetaDataLoader loader, String filename ) {
        super( loader, filename );
    }

    @Override
    public MetaDataConfig loadFromStream( InputStream is) {

        try {
            JsonObject root = new JsonParser().parse(new InputStreamReader( is )).getAsJsonObject();

            if ( root.has( ATTR_TYPESCONFIG )) {
                loadAllTypes( root.getAsJsonObject( ATTR_TYPESCONFIG ).getAsJsonArray( ATTR_TYPES ));
            }
            else if ( root.has( ATTR_METADATA )) {

                JsonObject metadata = root.getAsJsonObject( ATTR_METADATA );

                if ( metadata.has( ATTR_PACKAGE )) {
                    String defPkg = parsePackageValue( metadata.getAsJsonPrimitive( ATTR_PACKAGE ).getAsString() );
                    setDefaultPackageName( defPkg );
                }

                //if ( metadata.has( ATTR_TYPES )) {
                //    loadAllTypes( metadata.getAsJsonArray( ATTR_TYPES ));
                //}

                // Parse the metadata elements
                if (  metadata.has( ATTR_CHILDREN )) {
                    parseMetaData( getLoader(), metadata.getAsJsonArray( ATTR_CHILDREN ), true);
                }
            }
            else {
                throw new MetaException("The root 'metadata' or 'types' object was not found in file [" + getFilename() + "]");
            }
        }
        catch (NullPointerException ex) {
            throw new MetaException("NullPointer Exception loading MetaData from file ["+getFilename()+"]: " + ex.getMessage(), ex);
        }
        finally {
            try { is.close(); } catch (Exception e) {}
        }

        return getConfig();
    }

    /**
     * Loads the specified group types
     */
    protected void loadAllTypes( JsonArray types ) throws MetaException {

        // Get all elements that have <type> elements
        for (JsonElement el : types ) {

            JsonObject entry = el.getAsJsonObject();
            JsonObject typeEl = entry.getAsJsonObject( ATTR_TYPE );
            if (typeEl==null) {
                throw new MetaException("Types array has no 'type' object in file [" +getFilename()+ "]");
            }

            String name = getValueAsString(typeEl, ATTR_NAME);
            String clazz = getValueAsString(typeEl, ATTR_CLASS);

            if (name == null || name.isEmpty()) {
                throw new MetaException("Type has no 'name' attribute specified in file [" +getFilename()+ "]");
            }
            TypeConfig typeConfig = getOrCreateTypeConfig(name, clazz);

            if ( typeEl.has( "defaultSubType")) typeConfig.setDefaultSubTypeName(getValueAsString(typeEl, "defaultSubType"));
            if ( typeEl.has( "defaultName")) typeConfig.setDefaultName(getValueAsString(typeEl, "defaultName"));
            if ( typeEl.has( "defaultNamePrefix")) typeConfig.setDefaultNamePrefix(getValueAsString(typeEl, "defaultNamePrefix"));

            // If we have subtypes, load them
            if ( typeEl.has( ATTR_SUBTYPES )) {
                // Load all the types for the specific element type
                loadSubTypes( typeEl.getAsJsonArray( ATTR_SUBTYPES ), typeConfig );
            }
        }
    }

    private String getValueAsString(JsonObject e, String name) {
        return e.has(name) ? e.getAsJsonPrimitive(name).getAsString() : null;
    }

    private Boolean getValueAsBoolean(JsonObject e, String name) {
        return e.has(name) ? e.getAsJsonPrimitive(name).getAsBoolean() : null;
    }
    /**
     * Loads the specified group types
     */
    protected void loadSubTypes(JsonArray subtypes, TypeConfig typeConfig) {

        // Iterate through each type
        for (JsonElement subtype : subtypes) {

            JsonObject entry = subtype.getAsJsonObject();
            JsonObject subTypeEl = entry.getAsJsonObject( ATTR_SUBTYPE );
            if (subTypeEl==null) {
                throw new MetaException("Types array has no 'type' object in file [" +getFilename()+ "]");
            }

            String name = getValueAsString( subTypeEl, ATTR_NAME);
            String tclass = getValueAsString( subTypeEl, ATTR_CLASS);
            //Boolean def = getValueAsBoolean( subTypeEl, "default");
            //if ( Boolean.TRUE.equals( def )) typeConfig.setDefaultSubTypeName( name );

            if (name == null && name.isEmpty()) {
                throw new MetaException("SubType of Type [" + typeConfig.getTypeName() + "] has no 'name' attribute specified");
            }

            try {
                Class<MetaData> tcl = (Class<MetaData>) Class.forName(tclass);

                // Add the type class with the specified name
                typeConfig.addSubType(name, tcl);
            }
            catch (ClassNotFoundException e) {
                throw new MetaException("MetaData file ["+getFilename()+"] has Type:SubType [" +typeConfig.getTypeName()+":"+name+ "] with invalid class: " + e.getMessage());
            }
        }
    }


    /** Parse the metadata */
    protected void parseMetaData(MetaData parent, JsonArray children, boolean isRoot ) {

        // Iterate through all elements
        for ( JsonElement child : children ) {

            JsonObject elo = child.getAsJsonObject();
            String typeName = elo.keySet().iterator().next();

            JsonObject el = elo.getAsJsonObject( typeName );
            String subTypeName  = getValueAsString(el, ATTR_TYPE);
            String name         = getValueAsString(el, ATTR_NAME);
            String packageName  = getValueAsString(el, ATTR_PACKAGE);
            String superName    = getValueAsString(el, ATTR_SUPER);

            // See if the specified type exists or not
            if ( getConfig().getTypesConfig().getType( typeName ) == null ) {

                // If we are strict, throw an exception, otheriwse log an error
                if ( getLoader().getLoaderConfig().isStrict() ) {
                    throw new MetaException("Unknown type [" + typeName + "] found on parent metadata [" + parent + "] in file [" + getFilename() + "]");
                } else {
                    log.warn("Unknown type [" + typeName + "] found on parent metadata [" + parent + "] in file [" + getFilename() + "]");
                    continue;
                }
            }

            // Create MetaData
            MetaData md = createOrOverlayMetaData( isRoot, parent, typeName, subTypeName, name, packageName, superName);

            // Different behavior if it's a MetaAttribute
            if ( md instanceof MetaAttribute) {
                parseMetaAttributeValue( (MetaAttribute) md, el );
            }
            // otherwide, parse as normal recursively
            else {
                // Parse any extra attributes
                parseAttributes( md, el );

                // Parse the sub elements
                if ( el.has( ATTR_CHILDREN )) {
                    parseMetaData(md, el.getAsJsonArray( ATTR_CHILDREN ), false);
                }
            }
        }
    }

    /**
     * Parses actual element attributes and adds them as StringAttributes
     */
    protected void parseAttributes( MetaData md, JsonObject el ) {

        el.entrySet().forEach( n -> {
            String attrName = n.getKey();
            if ( !reservedAttributes.contains( attrName )) {

                String value = n.getValue().getAsString();

                // TODO:  This should be replaced by the ruleset for handling attributes in the future
                StringAttribute sa = new StringAttribute( attrName );
                sa.setValue( value );
                md.addMetaAttr(sa);
            }
        });
    }

    /**
     * Parse the MetaAttribute Value
     */
    protected void parseMetaAttributeValue( MetaAttribute attr, JsonObject el ) {

        String value = getValueAsString( el, ATTR_VALUE );
        if ( value != null ) {
            attr.setValueAsString(value);
        }
        //else {
        //    attr.setValueAsString( el.get);
        //}

        // Get the first node
        /*Node nv = el.getFirstChild();

        // Loop through and ignore the comments
        while (nv != null && nv.getNodeType() == Node.COMMENT_NODE) {
            nv.getNextSibling();
        }

        // If a valid node exists, then get the data
        if (nv != null) {
            switch (nv.getNodeType()) {

                // If CDATA just set the whole thing
                case Node.CDATA_SECTION_NODE:
                    attr.setValueAsString(((CDATASection) nv).getData());
                    break;

                // If an Element just pass it in for parsing (for when a field can process XML elements)
                case Node.ELEMENT_NODE:
                    attr.setValue(nv);
                    break;

                // If just text, then pass it in
                case Node.TEXT_NODE:
                    attr.setValueAsString(nv.getNodeValue());
                    break;

                default:
                    log.warn("Unsupported Node Type for node [" + nv + "] in file ["+getFilename()+"]");
            }
        }*/
    }
}
