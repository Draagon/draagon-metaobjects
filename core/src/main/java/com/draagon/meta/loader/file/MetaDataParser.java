package com.draagon.meta.loader.file;

import com.draagon.meta.MetaData;
import com.draagon.meta.MetaDataException;
import com.draagon.meta.MetaDataNotFoundException;
import com.draagon.meta.MetaException;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.loader.config.MetaDataConfig;
import com.draagon.meta.loader.config.MetaDataTypes;
import com.draagon.meta.loader.config.TypeModel;
import com.draagon.meta.util.MetaDataUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;

import static com.draagon.meta.util.MetaDataUtil.expandPackageForPath;

/**
 * Absract MetaDataParser for reading from source files
 */
public abstract class MetaDataParser {

    private static Log log = LogFactory.getLog(MetaDataParser.class);

    private FileMetaDataLoader loader;
    private String filename;
    private String defaultPackageName = "";

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

    /** Return the MetaDataConfig */
    public MetaDataConfig getConfig() {
        return this.loader.getMetaDataConfig();
    }

    /** Load the metadata models from the inputstream */
    public abstract MetaDataConfig loadFromStream( InputStream is );

    /** Get the MetaDataTypes from the loader's MetaDataConfig */
    public MetaDataTypes getTypes() {
        return this.loader.getMetaDataConfig().getMetaDataTypes();
    }

    /**
     * Get or Create a Type Configuration
     * @param typeName Name of the metadata type
     * @param typeClass MetaData type class
     * @return The create TypeModel
     */
    protected TypeModel getOrCreateTypeConfig(String typeName, String typeClass) {

        if ( typeName == null || typeName.isEmpty() ) {
            throw new MetaException( "MetaData Type was null or empty ["+typeName+"] in file [" +getFilename()+ "]");
        }

        // Get the TypeModel with the specified element name
        TypeModel typeModel = getTypes().getType( typeName );

        // If it doesn't exist, then create it and check for the "class" attribute
        if ( typeModel == null ) {

            if ( typeClass == null || typeClass.isEmpty() )
                throw new MetaException( "MetaData Type [" + typeName + "] has no 'class' attribute specified in file [" +getFilename()+ "]");

            try {
                // Add a new TypeModel and add to the mapping
                typeModel = getTypes().createType( typeName, (Class<? extends MetaData>) Class.forName( typeClass ));
            }
            catch( ClassNotFoundException ex ) {
                throw new MetaException( "MetaData Type ["+typeName+"] has an invalid class ["+typeClass+"] in file ["+getFilename()+"]: " + ex.getMessage(), ex );
            }
        }
        return typeModel;
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

        // Get the TypeModel map for this element
        TypeModel types = getTypes().getType( typeName );
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
    protected MetaData getSuperMetaData(MetaData parent, String typeName, String name, String packageName, String superName, TypeModel types ) {

        MetaData superData = null;

        // If a super class was specified
        if (superName.length() > 0) {

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
                try {
                    String fullyQualifiedSuperName = getFullyQualifiedSuperMetaDataName(parent, packageName, superName);
                    superData = getLoader().getChild(fullyQualifiedSuperName, types.getBaseClass());
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
    protected MetaData createNewMetaData(boolean isRoot, String typeName, String subTypeName, String name, String packageName, TypeModel types, MetaData superData) {

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
