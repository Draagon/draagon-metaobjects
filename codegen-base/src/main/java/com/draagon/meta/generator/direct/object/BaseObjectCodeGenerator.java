package com.draagon.meta.generator.direct.object;

import com.draagon.meta.generator.GeneratorException;
import com.draagon.meta.generator.GeneratorIOException;
import com.draagon.meta.generator.GeneratorIOWriter;
import com.draagon.meta.generator.direct.MultiFileDirectGeneratorBase;
import com.draagon.meta.generator.direct.GenerationContext;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.object.MetaObject;

import java.io.PrintWriter;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Base class for Object Code Generators providing language-agnostic functionality 
 * for generating code from MetaObjects.
 */
public abstract class BaseObjectCodeGenerator extends MultiFileDirectGeneratorBase<MetaObject> {

    // Common argument constants
    public final static String ARG_TYPE        = "type";
    public final static String ARG_PKGPREFIX   = "pkgPrefix";
    public final static String ARG_PKGSUFFIX   = "pkgSuffix";
    public final static String ARG_NAMEPREFIX  = "namePrefix";
    public final static String ARG_NAMESUFFIX  = "nameSuffix";
    public final static String ARG_OPTARRAYS   = "optArrays";
    public final static String ARG_OPTKEYS     = "optKeys";
    public final static String ARG_DEBUG       = "debug";
    
    // Error message constants
    protected static final String ERROR_FINALOUTPUTDIR_REQUIRED = " argument is required when a " + ARG_OUTPUTFILENAME + " is specified";
    protected static final String ERROR_TYPE_REQUIRED = " argument is required";
    protected static final String ERROR_TYPE_INVALID = " argument has invalid value";

    protected Map<MetaObject,String> objectNameMap = new LinkedHashMap<>();
    protected GenerationContext globalContext;
    protected MetaDataLoader currentLoader = null;

    //////////////////////////////////////////////////////////////////////
    // Abstract methods for language-specific implementation

    /**
     * Get the supported object types for this language (e.g., "interface", "class", "struct")
     */
    protected abstract String[] getSupportedTypes();
    
    /**
     * Get the default type for this language
     */
    protected abstract String getDefaultType();
    
    /**
     * Create a language-specific writer instance
     */
    protected abstract BaseObjectCodeWriter createWriter(MetaDataLoader loader, MetaObject md, PrintWriter pw, GenerationContext context);
    
    /**
     * Get the file extension for this language (e.g., ".java", ".cs", ".ts", ".py")
     */
    protected abstract String getFileExtension();

    //////////////////////////////////////////////////////////////////////
    // Configuration Methods

    public BaseObjectCodeGenerator() {
        // Initialize with a default context - will be replaced during parseArgs
        this.globalContext = new GenerationContext(null);
    }
    
    public BaseObjectCodeGenerator withGlobalContext(GenerationContext context) {
        this.globalContext = context;
        return this;
    }

    //////////////////////////////////////////////////////////////////////
    // Argument Methods

    @Override
    protected void parseArgs() {
        if ( hasArg(ARG_OUTPUTFILENAME) && !hasArg(ARG_FINALOUTPUTDIR)) {
            throw new GeneratorException(ARG_FINALOUTPUTDIR + ERROR_FINALOUTPUTDIR_REQUIRED);
        }
        if ( !hasArg( ARG_TYPE)) {
            throw new GeneratorException(ARG_TYPE + ERROR_TYPE_REQUIRED);
        }
        
        // Validate type is supported
        String type = getArg(ARG_TYPE);
        String[] supportedTypes = getSupportedTypes();
        boolean typeSupported = false;
        for (String supportedType : supportedTypes) {
            if (supportedType.equals(type)) {
                typeSupported = true;
                break;
            }
        }
        if (!typeSupported) {
            throw new GeneratorException(ARG_TYPE + ERROR_TYPE_INVALID + ". Supported: " + String.join(", ", supportedTypes));
        }

        super.parseArgs();
        
        // Note: globalContext will be initialized in getSingleWriter() when loader is available
        globalContext = null;

        if ( log.isDebugEnabled() ) log.debug("Enhanced ParseArgs: "+toString());
    }

    // Getters for arguments
    public String getType() {
        return getArg(ARG_TYPE, getDefaultType());
    }

    public String getPkgPrefix() {
        return getArg(ARG_PKGPREFIX, "");
    }

    public String getPkgSuffix() {
        return getArg(ARG_PKGSUFFIX, "");
    }

    public String getNamePrefix() {
        return getArg(ARG_NAMEPREFIX, "");
    }

    public String getNameSuffix() {
        return getArg(ARG_NAMESUFFIX, "");
    }

    public boolean addArrayMethods() {
        return Boolean.valueOf( getArg(ARG_OPTARRAYS,"false"));
    }

    public boolean addKeyMethods() {
        return Boolean.valueOf( getArg(ARG_OPTKEYS,"false"));
    }

    ///////////////////////////////////////////////////
    // Multi-File Methods

    @Override
    protected Class<MetaObject> getFilterClass() {
        return MetaObject.class;
    }

    @Override
    protected GeneratorIOWriter getSingleWriter(MetaDataLoader loader, MetaObject md, PrintWriter pw) throws GeneratorIOException {
        // Initialize global context with loader if not already done
        if (currentLoader == null) {
            currentLoader = loader;
            globalContext = new GenerationContext(loader);
            
            // Configure context with arguments now that we have the loader
            configureGlobalContext();
        }
        
        // Create a new context for each file generation, inheriting from global context
        GenerationContext fileContext = new GenerationContext(loader);
        
        // Copy global settings
        copyContextSettings(globalContext, fileContext);
        
        return createWriter(loader, md, pw, fileContext);
    }
    
    /**
     * Configure the global context with common settings
     */
    protected void configureGlobalContext() {
        globalContext.setProperty("generator.type", getType())
                    .setProperty("package.prefix", getPkgPrefix())
                    .setProperty("package.suffix", getPkgSuffix())
                    .setProperty("name.prefix", getNamePrefix())
                    .setProperty("name.suffix", getNameSuffix())
                    .setProperty("generate.arrayMethods", addArrayMethods())
                    .setProperty("generate.keyMethods", addKeyMethods())
                    .setProperty("debug", hasArg(ARG_DEBUG));
    }
    
    /**
     * Copy settings from global context to file context
     */
    protected void copyContextSettings(GenerationContext source, GenerationContext target) {
        // Copy properties
        for (String key : java.util.List.of("generator.type", "package.prefix", "package.suffix", 
                                  "name.prefix", "name.suffix", "generate.arrayMethods", 
                                  "generate.keyMethods", "debug")) {
            if (source.getProperty(key, null) != null) {
                target.setProperty(key, source.getProperty(key, null));
            }
        }
    }

    @Override
    protected void writeSingleFile(MetaObject mo, GeneratorIOWriter<?> writer) throws GeneratorIOException {
        log.info("Writing " + getLanguageName() + " Code [" + getType() + "] to file: " + writer.getFilename());

        String className = ((BaseObjectCodeWriter)writer).writeObject(mo);
        objectNameMap.put(mo, className);
    }
    
    /**
     * Get the name of the target language for logging purposes
     */
    protected abstract String getLanguageName();

    // File path and naming methods
    protected String getSingleOutputFilePath(MetaObject md) {
        String path = md.getPackage().replaceAll( "::", ".");
        if ( isNotBlank(getPkgPrefix())) {
            String pre = getPkgPrefix();
            if ( pre.endsWith(".")) path = pre+path;
            else path = pre+"."+path;
        }
        if ( isNotBlank(getPkgSuffix())) {
            String suf = getPkgSuffix();
            if ( suf.startsWith(".")) path = path+suf;
            else path = path+"."+suf;
        }
        path=path.replace('.', getPathSeparator());
        return path;
    }
    
    /**
     * Get the path separator for this target language/platform
     */
    protected char getPathSeparator() {
        return java.io.File.separatorChar;
    }

    protected String getSingleOutputFilename(MetaObject md) {
        String name = md.getShortName();
        if ( isNotBlank(getNamePrefix())) name = getNamePrefix()+"-"+name;
        if ( isNotBlank(getNameSuffix())) name = name+"-"+getNameSuffix();
        name = name.replaceAll("--","-");
        name = convertToLanguageNaming(name) + getFileExtension();
        return name;
    }
    
    /**
     * Convert name to language-specific naming conventions (e.g., PascalCase, camelCase, snake_case)
     */
    protected abstract String convertToLanguageNaming(String name);

    protected boolean isNotBlank(String str) {
        return str != null && !str.trim().isEmpty();
    }
    
    @Override
    public String toString() {
        return super.toString() + 
               ",context=" + (globalContext != null ? "initialized" : "null");
    }
}