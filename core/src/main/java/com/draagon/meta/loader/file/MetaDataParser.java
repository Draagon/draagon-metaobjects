package com.draagon.meta.loader.file;

import com.draagon.meta.MetaData;
import com.draagon.meta.MetaDataException;
import com.draagon.meta.MetaDataNotFoundException;
import com.draagon.meta.attr.MetaAttribute;
import com.draagon.meta.attr.StringAttribute;
import com.draagon.meta.io.object.xml.XMLObjectWriter;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.loader.config.ChildConfig;
import com.draagon.meta.loader.config.TypesConfig;
import com.draagon.meta.loader.config.TypeConfig;
import com.draagon.meta.util.MetaDataUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static com.draagon.meta.util.MetaDataUtil.expandPackageForPath;

/**
 * Absract MetaDataParser for reading from source files
 */
public abstract class MetaDataParser {

    private static Log log = LogFactory.getLog(MetaDataParser.class);

    public final static String ATTR_METADATA        = "metadata";
    public final static String ATTR_TYPESCONFIG     = "typesConfig";
    public final static String ATTR_TYPES           = "types";
    public final static String ATTR_PACKAGE         = "package";
    public final static String ATTR_DEFPACKAGE      = "defaultPackage";
    public final static String ATTR_CHILDREN        = "children";
    public final static String ATTR_NAME            = "name";
    public final static String ATTR_DEFNAME         = "defaultName";
    public final static String ATTR_DEFNAMEPREFIX   = "defaultNamePrefix";
    public final static String ATTR_CLASS           = "class";
    public final static String ATTR_TYPE            = "type";
    public final static String ATTR_SUBTYPE         = "subType";
    public final static String ATTR_SUBTYPES        = "subTypes";
    public final static String ATTR_DEFSUBTYPE      = "defaultSubType";
    public final static String ATTR_SUPER           = "super";
    public final static String ATTR_VALUE           = "value";

    protected static List<String> reservedAttributes = new ArrayList<>();
    static {
        reservedAttributes.add( ATTR_PACKAGE );
        reservedAttributes.add( ATTR_NAME );
        reservedAttributes.add( ATTR_CLASS );
        //reservedAttributes.add( ATTR_TYPES );
        reservedAttributes.add( ATTR_CHILDREN );
        reservedAttributes.add( ATTR_TYPE );
        reservedAttributes.add( ATTR_SUBTYPE );
        reservedAttributes.add( ATTR_SUBTYPES );
        reservedAttributes.add( ATTR_SUPER );
        reservedAttributes.add( ATTR_VALUE );
    }

    private FileMetaDataLoader loader;
    private String filename;
    private String defaultPackageName = "";

    protected class ParserInfoMsg {

        public final Map<String,Integer> types = new TreeMap <>();
        public final Map<String,Integer> data = new TreeMap <>();

        public ParserInfoMsg() {}
        public int incType( String n ) { return incMap( types, n ); }
        public int incData( String n ) { return incMap( data, n ); }


        public int incMap( Map<String,Integer> map, String n ) {
            synchronized(map) {
                if ( map.get(n) == null ) {
                    map.put(n, 1);
                    return 1;
                } else {
                    Integer i = map.get(n);
                    map.put(n, ++i);
                    return i;
                }
            }
        }
    }

    protected ParserInfoMsg info = new ParserInfoMsg();

    /** Create the MetaDataParser */
    protected MetaDataParser(FileMetaDataLoader loader, String filename ) {
        this.loader = loader;
        this.filename = filename;
    }

    /** Return the FileMetaDataLoader */
    public FileMetaDataLoader getLoader() {
        return this.loader;
    }

    /** Return the filename being loaded */
    public String getFilename() {
        return filename;
    }

    /** Set default package name */
    protected void setDefaultPackageName(String defPkg ) {
        this.defaultPackageName = defPkg;
    }

    /** Set default package name */
    protected String getDefaultPackageName() {
        return defaultPackageName;
    }

    /** Load the metadata models from the inputstream */
    public abstract void loadFromStream( InputStream is );

    /** Get the MetaDataTypes from the loader's MetaDataConfig */
    public TypesConfig getTypesConfig() {
        return this.loader.getTypesConfig();
    }

    /**
     * Get or Create a Type Configuration
     * @param typeName Name of the metadata type
     * @param typeClass MetaData type class
     * @return The create TypeModel
     */
    protected TypeConfig getOrCreateTypeConfig(String typeName, String typeClass) {

        if ( typeName == null || typeName.isEmpty() ) {
            throw new MetaDataException( "MetaData Type was null or empty ["+typeName+"] in file [" +getFilename()+ "]");
        }

        // Get the TypeModel with the specified element name
        TypeConfig typeConfig = getTypesConfig().getType( typeName );

        // If it doesn't exist, then create it and check for the "class" attribute
        if ( typeConfig == null ) {

            if ( typeClass == null || typeClass.isEmpty() )
                throw new MetaDataException( "MetaData Type [" + typeName + "] has no 'class' attribute specified in file [" +getFilename()+ "]");

            try {
                // Add a new TypeModel and add to the mapping
                typeConfig = getTypesConfig().createType( typeName, (Class<? extends MetaData>) Class.forName( typeClass ));
            }
            catch( ClassNotFoundException ex ) {
                throw new MetaDataException( "MetaData Type ["+typeName+"] has an invalid class ["+typeClass+"] in file ["+getFilename()+"]: " + ex.getMessage(), ex );
            }
        }
        return typeConfig;
    }

    /** Get the default package from the element */
    protected String parsePackageValue( String value ) {

        String defaultPackageName = value;
        if (defaultPackageName == null || defaultPackageName.trim().length() == 0) {
            defaultPackageName = "";
        }
        return defaultPackageName;
    }

    protected String getNextNamePrefix( MetaData parent, String typeName, String prefix ) {
        int i = 1;
        for( MetaData md : (List<MetaData>) parent.getChildrenOfType( typeName, true )) {
            if ( md.getName().startsWith( prefix )) {
                try {
                    int n = Integer.parseInt(md.getName().substring(prefix.length()));
                    if ( n >= i ) i = n+1;
                }
                catch( NumberFormatException ignore ) {}
            }
        }
        return prefix + i;
    }

    /** Create or Overlay the MetaData */
    protected MetaData createOrOverlayMetaData( boolean isRoot, MetaData parent, String typeName, String subTypeName, String name, String packageName, String superName) {

        if ( subTypeName != null && subTypeName.equals("*")) subTypeName = null;

        // Get the TypeModel map for this element
        TypeConfig types = getTypesConfig().getType( typeName );
        if ( types == null ) {
            // TODO:  What is the best behavior here?
            throw new MetaDataException( "Unknown type [" +typeName+ "] found on parent [" +parent+ "] in file [" +getFilename()+ "]" );
        }

        if (name == null || name.equals("")) {
            name = types.getDefaultName();
            if ( name == null ) {
                String prefix = types.getDefaultNamePrefix();
                if ( prefix != null ) {
                    name = getNextNamePrefix(parent, typeName, prefix);
                }
                if ( name == null ) throw new MetaDataException("MetaData [" +typeName+ "] found on parent [" +parent
                        + "] had no name specfied and no defaultName existed in file ["+getFilename()+"]");
            }
        }

        // Load or get the MetaData
        MetaData md = null;

        if (packageName == null || packageName.trim().isEmpty()) {
            // If not found, then use the default
            packageName = getDefaultPackageName();
        } else {
            // Convert any relative paths to the full package path
            packageName = expandPackageForPath( getDefaultPackageName(), packageName );
        }

        // Check if the metadata described already existed, and if so create and overloaded version
        try {
            if ( isRoot && packageName.length() > 0 ) {
                md = parent.getChild( packageName + MetaDataLoader.PKG_SEPARATOR + name, types.getBaseClass() );
            } else {
                md = parent.getChild( name, types.getBaseClass() );

                // If it's not a child from the same parent, we need to wrap it
                if ( md.getParent() != parent ) {
                    md = md.overload();
                    parent.addChild(md);
                }
            }
        }
        catch (MetaDataNotFoundException e) {
            // Handle cases where it's bad that it wasn't found
        }

        // If this MetaData doesn't exist yet, then we need to create it
        if (md == null) {

            // Get the super metadata if it exists
            MetaData superData = getSuperMetaData(parent, typeName, name, packageName, superName, types);

            // Create the new MetaData
            md = createNewMetaData(isRoot, parent, typeName, subTypeName, name, packageName, types, superData);

            // Add to the parent metadata
            parent.addChild(md);

            // Set the super data class if one exists
            if (superData != null) {
                md.setSuperData(superData);
            }
        }

        return md;
    }

    /** Get the Super MetaData if it exists */
    protected MetaData getSuperMetaData(MetaData parent, String typeName, String name, String packageName, String superName, TypeConfig types ) {

        MetaData superData = null;

        // If a super class was specified
        if (superName != null && !superName.isEmpty()) {

            // Try to find it with the name prepended if not fully qualified
            try {
                if (superName.indexOf(MetaDataLoader.PKG_SEPARATOR) < 0 && packageName.length() > 0) {

                    superData = getLoader().getChild(packageName + MetaDataLoader.PKG_SEPARATOR + superName, types.getBaseClass() );
                }
            } catch (MetaDataNotFoundException e) {
                // TODO:  Should this throw a real exception
                log.debug("Could not find MetaData [" + packageName + MetaDataLoader.PKG_SEPARATOR + superName + "], assuming fully qualified");
            }

            // Try to find it by the provided name in the 'super' attribute
            if (superData == null) {
                String fullyQualifiedSuperName = getFullyQualifiedSuperMetaDataName(parent, packageName, superName);
                try {
                    superData = getLoader().getChild(fullyQualifiedSuperName, types.getBaseClass());
                }
                catch (MetaDataNotFoundException e) {
                    //log.info( "packageName="+packageName+", parentPkg="+(parent==null?null:parent.getPackage())
                    //        +", pkg="+pkg+", superName="+superName+", sn="+sn);
                    //log.error("Invalid MetaData [" +typeName+ "][" +name+ "] on parent ["+parent+"], the SuperClass [" + superName + "] does not exist in file ["+getFilename()+"]");
                    throw new MetaDataException("Invalid MetaData [" +typeName+ "][" +name+ "] on parent ["+parent
                            +"], the SuperClass [" + superName + "] does not exist in file ["+getFilename()+"]");
                }
            }
        }
        // Check to make sure people arent' defining attributes when it shouldn't
        else {
            if (superName != null && !superName.isEmpty()) {
                logErrorOnce( parent, "getSuperMetaData("+typeName+","+name+")",
                        "Attribute 'super' defined on MetaData [" +typeName+ "][" +name+ "] under parent [" +parent
                                + "], but should not be as metadata with that name already existed: file ["+getFilename()+"]");
            }
        }

        return superData;
    }

    /** Get the fully qualified metadata name for the Super MetaData */
    protected String getFullyQualifiedSuperMetaDataName(MetaData parent, String packageName, String superName) {

        if (shouldUseParentPackage(parent, packageName)) {
            packageName = parent.getPackage();
        }
        return MetaDataUtil.expandPackageForMetaDataRef(packageName, superName);
    }

    /** Determine if the packageName should change based on the parent metadata */
    protected boolean shouldUseParentPackage( MetaData parent, String packageName ) {
        // TODO:  This may need to be refactored
        return parent != null
                && !(parent instanceof MetaDataLoader)
                && !parent.getPackage().isEmpty()
                && !parent.getPackage().equals( packageName );
    }

    /** Create new MetaData */
    protected MetaData createNewMetaData(boolean isRoot, MetaData parent, String typeName, String subTypeName, String name, String packageName, TypeConfig typeConfig, MetaData superData) {

        if (subTypeName != null && subTypeName.isEmpty()) subTypeName = null;

        Class<? extends MetaData> c = null;

        // Attempt to load the referenced class
        if (subTypeName == null) {

            // Use the Super class type if no type is defined and a super class exists
            if (superData != null) {
                c = superData.getClass();
                subTypeName = superData.getSubTypeName();
            }
            else {
                if ( isRoot ) {
                    subTypeName = getSubTypeFromChildConfigs(ATTR_METADATA, null, typeConfig, name);
                } else {
                    subTypeName = getSubTypeFromChildConfigs(parent.getTypeName(), parent.getSubTypeName(), typeConfig, name);
                }

                if ( subTypeName == null ) {
                    subTypeName = typeConfig.getDefaultSubTypeName();
                    c = typeConfig.getDefaultTypeClass();
                }
                else {
                    c = (Class<? extends MetaData>) typeConfig.getSubTypeClass(subTypeName);
                }
                if (c == null) {
                    throw new MetaDataException("MetaData [type=" + typeName + "][name=" + name
                            + "] has no subtype defined and type [" + typeName + "] had no default specified in file ["
                            + getFilename() + "]");
                }
            }
        } else {
            c = (Class<? extends MetaData>) typeConfig.getSubTypeClass(subTypeName);
        }

        if (c == null) {
            throw new MetaDataException("MetaData [" + typeName + "] had type [" + subTypeName
                    + "], but it was not recognized in file ["+getFilename()+"]");
        }

        // Figure out the full name for the element, needs package prefix if root
        // TODO: Clean this up and go to caller where it exists as well
        String fullname = isRoot ? packageName + MetaDataLoader.PKG_SEPARATOR + name : name;

        // Use the parent type child records to verify this metadata child is acceptable
        if ( isRoot ) {
            verifyAcceptableChild( ATTR_METADATA, null, typeName, subTypeName, name);
        } else {
            verifyAcceptableChild(parent.getTypeName(), parent.getSubTypeName(), typeName, subTypeName, name);
        }

        // Create the object
        MetaData md= getLoader().newInstanceFromClass(c, typeName, subTypeName, fullname);

        return md;
    }


    protected String getSubTypeFromChildConfigs( String parentType, String parentSubType, TypeConfig typeConfig, String name ) {

        TypeConfig tc = getTypesConfig ().getType( parentType );

        ChildConfig cc = findBestChildConfigMatch( tc, parentType, parentSubType,
                typeConfig.getTypeName(), null, name );

        if ( cc == null ) return null;

        return ( cc.getSubType().equals("*")) ? null : cc.getSubType();
    }


    protected void verifyAcceptableChild( String parentType, String parentSubType, String type, String subType, String name ) {

        TypeConfig tc = getTypesConfig().getType( parentType );

        ChildConfig cc = findBestChildConfigMatch( tc, parentType, parentSubType, type, subType, name );

        if (cc == null) {
            if ( getLoader().getLoaderOptions().isStrict() ) {
                throw new MetaDataException( "Child record ["+type+":"+subType+"] with name ["+name+"] is not allowed"
                        +" on parent records ["+parentType+":"+parentSubType+"] in file ["+getFilename()+"]" );
            }
            else if ( type.equals( MetaAttribute.TYPE_ATTR ) && getLoader().getLoaderOptions().allowsAutoAttrs() ) {
                // Auto add the new child configuration
                cc = tc.createChildConfig( type, subType, name );
                cc.setAutoCreatedFromFile( getFilename() );
                tc.addTypeChildConfig( cc );
            }
            else {
                //log.info( "LOADER: " + getLoader() );
                //log.info( "LOADER CONFIG: " + getLoader().getLoaderConfig() );
                String errMsg = "Child record ["+type+":"+subType+"] with name ["+name+"] was not configured"
                        +" for parent records ["+parentType+":"+parentSubType+"]: file ["+getFilename()+"]";
                logWarnOnce( getLoader(), "verifyAcceptableChild("+parentType+","+parentSubType+","+
                        type+","+subType+","+name+")", errMsg );
            }
        }
    }

    protected void logErrorOnce( MetaData parent, String KEY, String errMsg ) {

        if ( log.isErrorEnabled() ) {
            Object v = parent.getCacheValue( KEY );
            if ( v == null ) {
                log.error( errMsg );
                parent.setCacheValue( KEY, Boolean.TRUE );
            }
        }
    }


    protected void logWarnOnce( MetaData parent, String KEY, String errMsg ) {

        if ( log.isWarnEnabled() ) {
            Object v = parent.getCacheValue( KEY );
            if ( v == null ) {
                log.warn( errMsg );
                parent.setCacheValue( KEY, Boolean.TRUE );
            }
        }
    }

    protected ChildConfig findBestChildConfigMatch(TypeConfig parentTypeConfig, String parentType, String parentSubType, String type, String subType, String name ) {

        ChildConfig cc = null;
        TypeConfig tc = parentTypeConfig;

        List<ChildConfig> ccList;// Check for best match on subtype
        if ( parentSubType != null ) {
            ccList = tc.getSubTypeChildConfigs(parentSubType);
            if ( ccList != null ) {
                cc = tc.getBestMatchChildConfig(ccList, type, subType, name);
            }
        }

        if ( cc == null ) {

            // Check for best match type level
            ccList = tc.getTypeChildConfigs();
            cc = tc.getBestMatchChildConfig(ccList, type, subType, name);
        }

        return cc;
    }

    protected void createAttributeOnParent(MetaData parentMetaData, String attrName, String value) {

        String parentType = parentMetaData.getTypeName();
        String parentSubType = parentMetaData.getSubTypeName();

        TypeConfig parentTypeConfig = getTypesConfig().getType( parentMetaData.getTypeName() );
        ChildConfig cc = findBestChildConfigMatch( parentTypeConfig, parentType, parentSubType,
                MetaAttribute.TYPE_ATTR, null, attrName );

        MetaAttribute attr = null;

        if ( cc == null ) {
            String errMsg = "MetaAttribute with name ["+attrName+"] is not allowed on parent record ["
                    +parentType+":"+parentSubType+":"+parentMetaData.getName()+"] in file ["+getFilename()+"]";

            if ( getLoader().getLoaderOptions().allowsAutoAttrs() ) {
                cc = parentTypeConfig.createChildConfig( StringAttribute.TYPE_ATTR, StringAttribute.SUBTYPE_STRING, attrName );
                cc.setAutoCreatedFromFile( getFilename() );
            }
            else if ( getLoader().getLoaderOptions().isStrict() ) {
                throw new MetaDataException( errMsg );
            }
            else {
                logWarnOnce( parentMetaData, "createAttributeOnParent("+attrName+")", errMsg );

                attr = new StringAttribute( attrName );
                parentMetaData.addChild( attr );
            }
        }

        if ( attr == null && cc != null ) {
            attr = (MetaAttribute) createOrOverlayMetaData(parentType.equals(MetaDataLoader.TYPE_LOADER), parentMetaData,
                    cc.getType(), cc.getSubType(), attrName /*cc.getName()*/, null, null);
        }

        if ( attr != null ) {
            attr.setValueAsString(value);
        }
    }
}
