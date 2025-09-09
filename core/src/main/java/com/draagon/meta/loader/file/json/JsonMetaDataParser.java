package com.draagon.meta.loader.file.json;

import com.draagon.meta.MetaData;
import com.draagon.meta.MetaDataException;
import com.draagon.meta.attr.MetaAttribute;
import com.draagon.meta.loader.file.FileMetaDataParser;
import com.draagon.meta.loader.types.ChildConfig;
import com.draagon.meta.loader.types.TypeConfig;
import com.draagon.meta.loader.file.FileMetaDataLoader;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Json MetaData Loader
 */
public class JsonMetaDataParser extends FileMetaDataParser {

    private static final Logger log = LoggerFactory.getLogger(JsonMetaDataParser.class);

    public JsonMetaDataParser(FileMetaDataLoader loader, String filename ) {
        super( loader, filename );
    }

    @Override
    public void loadFromStream( InputStream is) {

        try {
            JsonObject root = new JsonParser().parse(new InputStreamReader( is )).getAsJsonObject();

            if ( root.has( ATTR_TYPESCONFIG )) {
                loadAllTypes( root.getAsJsonObject( ATTR_TYPESCONFIG ).getAsJsonArray( ATTR_TYPES ));
            }
            else if ( root.has( ATTR_METADATA )) {

                JsonObject metadata = root.getAsJsonObject( ATTR_METADATA );

                String defPkg = "";
                if ( metadata.has( ATTR_DEFPACKAGE )) defPkg = parsePackageValue( metadata.getAsJsonPrimitive( ATTR_DEFPACKAGE ).getAsString() );
                else if ( metadata.has( ATTR_PACKAGE )) defPkg = parsePackageValue( metadata.getAsJsonPrimitive( ATTR_PACKAGE ).getAsString() );
                setDefaultPackageName( defPkg );

                //if ( metadata.has( ATTR_TYPES )) {
                //    loadAllTypes( metadata.getAsJsonArray( ATTR_TYPES ));
                //}

                // Parse the metadata elements
                if (  metadata.has( ATTR_CHILDREN )) {
                    parseMetaData( getLoader(), metadata.getAsJsonArray( ATTR_CHILDREN ), true);
                }
            }
            else {
                throw new MetaDataException("The root 'metadata' or 'types' object was not found in file [" + getFilename() + "]");
            }
        }
        catch (NullPointerException ex) {
            throw new MetaDataException("NullPointer Exception loading MetaData from file ["+getFilename()+"]: " + ex.getMessage(), ex);
        }
        finally {
            try { is.close(); } catch (Exception e) {}
        }
    }

    /**
     * Loads the specified group types
     */
    protected void loadAllTypes( JsonArray types ) throws MetaDataException {

        // Get all elements that have <type> elements
        for (JsonElement el : types ) {

            JsonObject entry = el.getAsJsonObject();
            JsonObject type = entry.getAsJsonObject( ATTR_TYPE );
            if (type==null) {
                throw new MetaDataException("Types array has no 'type' object in file [" +getFilename()+ "]");
            }

            String name = getValueAsString(type, ATTR_NAME);
            String clazz = getValueAsString(type, ATTR_CLASS);

            if (name == null || name.isEmpty()) {
                throw new MetaDataException("Type has no 'name' attribute specified in file [" +getFilename()+ "]");
            }
            TypeConfig typeConfig = getOrCreateTypeConfig(name, clazz);

            if ( type.has( ATTR_DEFSUBTYPE)) typeConfig.setDefaultSubType(getValueAsString(type, ATTR_DEFSUBTYPE));
            if ( type.has( ATTR_DEFNAME)) typeConfig.setDefaultName(getValueAsString(type, ATTR_DEFNAME));
            if ( type.has( ATTR_DEFNAMEPREFIX)) typeConfig.setDefaultNamePrefix(getValueAsString(type, ATTR_DEFNAMEPREFIX));
            loadChildren( typeConfig, type ).forEach( c-> typeConfig.addTypeChildConfig(c));

            // If we have subtypes, load them
            if ( type.has( ATTR_SUBTYPES )) {
                // Load all the types for the specific element type
                loadSubTypes( type.getAsJsonArray( ATTR_SUBTYPES ), typeConfig );
            }
        }
    }

    protected  List<ChildConfig> loadChildren(TypeConfig tc, JsonObject el) {
        List<ChildConfig> children = new ArrayList<>();
        if ( el.has(ATTR_CHILDREN)) {
            JsonArray chArray = el.get(ATTR_CHILDREN).getAsJsonArray();
            for( JsonElement e : chArray ) {
                JsonObject o = e.getAsJsonObject();
                if ( o.has( "child")) {
                    JsonObject ec = o.get("child").getAsJsonObject();
                    ChildConfig cc = tc.createChildConfig(ec.get(ATTR_TYPE).getAsString(), ec.get(ATTR_SUBTYPE).getAsString(), ec.get(ATTR_NAME).getAsString());
                    if (ec.has("nameAliases")) cc.setNameAliases(Arrays.asList(ec.get("nameAliases").getAsString().split(",")));
                    //if (ec.has("required")) cc.setRequired(ec.get("required").getAsBoolean());
                    //if (ec.has("autoCreate")) cc.setAutoCreate(ec.get("autoCreate").getAsBoolean());
                    //if (ec.has("defaultValue")) cc.setDefaultValue(ec.get("defaultValue").getAsString());
                    //if (ec.has("minValue")) cc.setMinValue(ec.get("minValue").getAsInt());
                    //if (ec.has("maxValue")) cc.setMaxValue(ec.get("maxValue").getAsInt());
                    //if (ec.has("inlineAttr")) cc.setInlineAttr(ec.get("inlineAttr").getAsString());
                    //if (ec.has("inlineAttrName")) cc.setInlineAttrName(ec.get("inlineAttrName").getAsString());
                    //if (ec.has("inlineAttrValueMap")) cc.setInlineAttrValueMap(ec.get("inlineAttrValueMap").getAsString());
                    children.add(cc);
                }
            }
        }
        return children;
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
                throw new MetaDataException("Types array has no 'type' object in file [" +getFilename()+ "]");
            }

            String name = getValueAsString( subTypeEl, ATTR_NAME);
            String tclass = getValueAsString( subTypeEl, ATTR_CLASS);
            //Boolean def = getValueAsBoolean( subTypeEl, "default");
            //if ( Boolean.TRUE.equals( def )) typeConfig.setDefaultSubTypeName( name );

            if (name == null && name.isEmpty()) {
                throw new MetaDataException("SubType of Type [" + typeConfig.getName() + "] has no 'name' attribute specified");
            }

            // Add the type class with the specified name
            typeConfig.addSubTypeConfig(name, tclass);

            // Load subtypes
            loadChildren( typeConfig, subTypeEl ).forEach( c-> typeConfig.addSubTypeChild( name, c));

            // Update info msg if verbose
            if ( getLoader().getLoaderOptions().isVerbose() ) {
                // Increment the # of subtypes
                info.incType(typeConfig.getName());
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
            Boolean isAbstract  = getValueAsBoolean(el, ATTR_ISABSTRACT);
            Boolean isInterface = getValueAsBoolean(el, ATTR_ISINTERFACE);
            String implementsArray = getValueAsString(el, ATTR_IMPLEMENTS);

            // See if the specified type exists or not
            if ( getTypesConfig().getTypeByName( typeName ) == null ) {

                // If we are strict, throw an exception, otheriwse log an error
                if ( getLoader().getLoaderOptions().isStrict() ) {
                    throw new MetaDataException("Unknown type [" + typeName + "] found on parent metadata [" + parent + "] in file [" + getFilename() + "]");
                } else {
                    log.warn("Unknown type [" + typeName + "] found on parent metadata [" + parent + "] in file [" + getFilename() + "]");
                    continue;
                }
            }

            // Create MetaData
            MetaData md = createOrOverlayMetaData( isRoot,
                    parent, typeName, subTypeName,
                    name, packageName, superName,
                    isAbstract, isInterface, implementsArray );

            // Different behavior if it's a MetaAttribute
            if ( md instanceof MetaAttribute) {
                parseMetaAttributeValue( (MetaAttribute) md, el );
            }
            // otherwise, parse as normal recursively
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
                createAttributeOnParent(md, attrName, value);
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
