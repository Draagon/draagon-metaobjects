package com.draagon.meta.loader.file;

import com.draagon.meta.MetaData;
import com.draagon.meta.MetaDataException;
import com.draagon.meta.MetaDataNotFoundException;
import com.draagon.meta.MetaException;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.loader.config.MetaDataConfig;
import com.draagon.meta.loader.config.TypeConfig;
import com.draagon.meta.object.MetaObjectNotFoundException;
import com.draagon.meta.util.MetaDataUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static com.draagon.meta.util.MetaDataUtil.expandPackageForPath;

/**
 * Absract MetaDataReader for reading from source files
 */
public abstract class MetaDataReader {

    private static Log log = LogFactory.getLog(MetaDataReader.class);

    private FileMetaDataLoader loader;
    private String filename;
    private String defaultPackageName = "";

    /** Create the MetaDataReader */
    protected MetaDataReader( FileMetaDataLoader loader, String filename ) {
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

    /** Return the MetaDataConfig */
    public MetaDataConfig getConfig() {
        return this.loader.getConfig();
    }

    /** Load the types configuration from the inputstream */
    public abstract MetaDataConfig loadTypesFromStream( InputStream is );

    /** Load the metadata models from the inputstream */
    public abstract MetaDataConfig loadFromStream( InputStream is );

    /**
     * Get or Create a Type Configuration
     * @param typeName Name of the metadata type
     * @param typeClass MetaData type class
     * @return The create TypeConfig
     */
    protected TypeConfig getOrCreateTypeConfig( String typeName, String typeClass) {

        if ( typeName == null || typeName.isEmpty() ) {
            throw new MetaException( "MetaData Type was null or empty ["+typeName+"] in file [" +getFilename()+ "]");
        }

        // Get the TypeConfig with the specified element name
        TypeConfig typeConfig = getLoader().getConfig().getTypeConfig( typeName );

        // If it doesn't exist, then create it and check for the "class" attribute
        if ( typeConfig == null ) {

            if ( typeClass == null || typeClass.isEmpty() )
                throw new MetaException( "MetaData Type [" + typeName + "] has no 'class' attribute specified in file [" +getFilename()+ "]");

            try {
                // Add a new TypeConfig and add to the mapping
                typeConfig = getConfig().createTypeConfig( typeName, (Class<? extends MetaData>) Class.forName( typeClass ));
            }
            catch( ClassNotFoundException ex ) {
                throw new MetaException( "MetaData Type ["+typeName+"] has an invalid class ["+typeClass+"] in file ["+getFilename()+"]: " + ex.getMessage(), ex );
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

    /** Create or Overlay the MetaData */
    protected MetaData createOrOverlayMetaData( boolean isRoot, MetaData parent, String typeName, String subTypeName, String name, String packageName, String superName) {

        if (name == null || name.equals("")) {
            throw new MetaException("MetaData [" +typeName+ "] found on parent [" +parent+ "] had no name specfied in file ["+getFilename()+"]");
        }

        // Get the TypeConfig map for this element
        TypeConfig types = getConfig().getTypeConfig( typeName );
        if ( types == null ) {
            // TODO:  What is the best behavior here?
            throw new MetaException( "Unknown type [" +typeName+ "] found on parent [" +parent+ "] in file [" +getFilename()+ "]" );
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
            md = createNewMetaData(isRoot, typeName, subTypeName, name, packageName, types, superData);

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
        if (superName.length() > 0) {

            // Try to find it with the name prepended if not fully qualified
            try {
                if (superName.indexOf(MetaDataLoader.PKG_SEPARATOR) < 0 && packageName.length() > 0) {

                    superData = getLoader().getMetaDataByName(types.getBaseClass(), packageName + MetaDataLoader.PKG_SEPARATOR + superName);
                }
            } catch (MetaDataNotFoundException e) {
                log.debug("Could not find MetaData [" + packageName + MetaDataLoader.PKG_SEPARATOR + superName + "], assuming fully qualified");
            }

            // Try to find it by the provided name in the 'super' attribute
            if (superData == null) {
                String pkg = null;
                String sn = null;
                try {
                    // TODO:  This shouldn't have needed to be this way
                    pkg = packageName;
                    if ( parent != null
                            && !(parent instanceof MetaDataLoader)
                            && !parent.getPackage().isEmpty()
                            && !parent.getPackage().equals( pkg )) {
                        pkg = parent.getPackage();
                    }
                    sn = MetaDataUtil.expandPackageForMetaDataRef(pkg, superName);
                    superData = getLoader().getMetaDataByName(types.getBaseClass(), sn);
                }
                catch (MetaDataNotFoundException e) {
                    //log.info( "packageName="+packageName+", parentPkg="+(parent==null?null:parent.getPackage())
                    //        +", pkg="+pkg+", superName="+superName+", sn="+sn);
                    log.error("Invalid MetaData [" +typeName+ "][" +name+ "] on parent ["+parent+"], the SuperClass [" + superName + "] does not exist in file ["+getFilename()+"]");
                    throw new MetaException("Invalid MetaData [" +typeName+ "][" +name+ "] on parent ["+parent+"], the SuperClass [" + superName + "] does not exist in file ["+getFilename()+"]");
                }
            }
        }
        // Check to make sure people arent' defining attributes when it shouldn't
        else {
            if (superName != null && !superName.isEmpty()) {
                log.warn("Attribute 'super' defined on MetaData [" +typeName+ "][" +name+ "] under parent [" + parent + "], but should not be as metadata with that name already existed");
            }
        }

        return superData;
    }

    /** Create new MetaData */
    protected MetaData createNewMetaData( boolean isRoot, String typeName, String subTypeName, String name, String packageName, TypeConfig types, MetaData superData) {

        if (subTypeName.isEmpty()) subTypeName = null;

        try {
            Class<? extends MetaData> c = null;

            // Attempt to load the referenced class
            if (subTypeName == null) {

                // Use the Super class type if no type is defined and a super class exists
                if (superData != null) {
                    c = superData.getClass();
                    subTypeName = superData.getSubTypeName();
                } else {
                    subTypeName = types.getDefaultSubTypeName();
                    c = types.getDefaultTypeClass();
                    if (c == null) {
                        throw new MetaException("MetaData [" + typeName + "][" + name + "] has no subtype defined and type [" + typeName + "] had no default specified");
                    }
                }
            } else {
                c = (Class<? extends MetaData>) types.getSubTypeClass(subTypeName);
                if (c == null) {
                    throw new MetaException("MetaData [" + typeName + "] had type [" + subTypeName + "], but it was not recognized");
                }
            }

            // Figure out the full name for the element, needs package prefix if root
            String fullname = name;
            if (isRoot) fullname = packageName + MetaDataLoader.PKG_SEPARATOR + fullname;

            // Create the object
            MetaData md = getLoader().newInstanceFromClass(c, typeName, subTypeName, fullname);

            if (!md.getTypeName().equals(typeName))
                throw new MetaDataException("Expected MetaData type [" + typeName + "], but MetaData instantiated was of type [" + md.getTypeName() + "]: " + md);

            if (!md.getSubTypeName().equals(subTypeName))
                throw new MetaDataException("Expected MetaData subType [" + subTypeName + "], but MetaData instantiated was of subType [" + md.getSubTypeName() + "]: " + md);

            if (!md.getName().equals(fullname))
                throw new MetaDataException("Expected MetaData name [" + fullname + "], but MetaData instantiated was of name [" + md.getName() + "]: " + md);

            return md;
        }
        catch (MetaException e) {
            throw e;
        }
        catch (Exception e) {
            log.error("Invalid MetaData [" + typeName + "]: " + e.getMessage());
            throw new MetaException("Invalid MetaData [" + typeName + "]", e);
        }
    }
}
