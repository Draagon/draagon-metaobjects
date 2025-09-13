package com.draagon.meta.generator.direct.javacode.simple;

import com.draagon.meta.generator.GeneratorException;
import com.draagon.meta.generator.GeneratorIOException;
import com.draagon.meta.generator.GeneratorIOWriter;
import com.draagon.meta.generator.direct.MultiFileDirectGeneratorBase;
import com.draagon.meta.generator.direct.GenerationContext;
import com.draagon.meta.generator.direct.GenerationPlugin;
import com.draagon.meta.generator.direct.javacode.overlay.JavaCodeOverlayXMLWriter;
import com.draagon.meta.generator.util.GeneratorUtil;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.object.MetaObject;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * Enhanced Java Code Generator that supports plugins and configurable code fragments
 */
public class EnhancedJavaCodeGenerator extends MultiFileDirectGeneratorBase<MetaObject> {

    // Same constants as SimpleJavaCodeGenerator
    public final static String ARG_TYPE        = "type";
    public final static String TYPE_INTERFACE  = "interface";

    public final static String ARG_PKGPREFIX   = "pkgPrefix";
    public final static String ARG_PKGSUFFIX   = "pkgSuffix";
    public final static String ARG_NAMEPREFIX  = "namePrefix";
    public final static String ARG_NAMESUFFIX  = "nameSuffix";

    public final static String ARG_OPTARRAYS   = "optArrays";
    public final static String ARG_OPTKEYS     = "optKeys";
    
    // New arguments for enhanced functionality
    public final static String ARG_PLUGINS     = "plugins";
    public final static String ARG_DEBUG       = "debug";
    
    // Error message constants
    private static final String ERROR_FINALOUTPUTDIR_REQUIRED = " argument is required when a " + ARG_OUTPUTFILENAME + " is specified";
    private static final String ERROR_TYPE_REQUIRED = " argument is required, valid values=[" + TYPE_INTERFACE + "]";
    private static final String ERROR_TYPE_INVALID = " argument only supports the following values: [" + TYPE_INTERFACE + "]";

    protected Map<MetaObject,String> objectNameMap = new LinkedHashMap<>();
    protected GenerationContext globalContext;
    protected List<GenerationPlugin> plugins = new ArrayList<>();
    protected MetaDataLoader currentLoader = null;

    //////////////////////////////////////////////////////////////////////
    // Configuration Methods

    public EnhancedJavaCodeGenerator() {
        // Initialize with a default context - will be replaced during parseArgs
        this.globalContext = new GenerationContext(null);
    }
    
    public EnhancedJavaCodeGenerator addPlugin(GenerationPlugin plugin) {
        plugins.add(plugin);
        if (globalContext != null) {
            globalContext.addPlugin(plugin);
        }
        return this;
    }
    
    public EnhancedJavaCodeGenerator withGlobalContext(GenerationContext context) {
        this.globalContext = context;
        // Add any previously registered plugins
        for (GenerationPlugin plugin : plugins) {
            context.addPlugin(plugin);
        }
        return this;
    }

    //////////////////////////////////////////////////////////////////////
    // Argument Methods

    @Override
    protected void parseArgs() {
        if ( hasArg(ARG_OUTPUTFILENAME) && !hasArg(ARG_FINALOUTPUTDIR)) throw new GeneratorException(
                ARG_FINALOUTPUTDIR + ERROR_FINALOUTPUTDIR_REQUIRED );
        if ( !hasArg( ARG_TYPE)) throw new GeneratorException(
                ARG_TYPE + ERROR_TYPE_REQUIRED );
        if ( !getArg( ARG_TYPE).equals(TYPE_INTERFACE)) throw new GeneratorException(
                ARG_TYPE + ERROR_TYPE_INVALID );

        super.parseArgs();
        
        // Note: globalContext will be initialized in getSingleWriter() when loader is available
        globalContext = null;

        if ( log.isDebugEnabled() ) log.debug("Enhanced ParseArgs: "+toString());
    }

    // Getters for arguments (same as SimpleJavaCodeGenerator)
    public String getType() {
        return getArg(ARG_TYPE,TYPE_INTERFACE);
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
    protected EnhancedJavaCodeWriter getSingleWriter(MetaDataLoader loader, MetaObject md, PrintWriter pw) throws GeneratorIOException {
        // Initialize global context with loader if not already done
        if (currentLoader == null) {
            currentLoader = loader;
            globalContext = new GenerationContext(loader);
            
            // Re-configure context with arguments now that we have the loader
            globalContext.setProperty("generator.type", getType())
                        .setProperty("package.prefix", getPkgPrefix())
                        .setProperty("package.suffix", getPkgSuffix())
                        .setProperty("name.prefix", getNamePrefix())
                        .setProperty("name.suffix", getNameSuffix())
                        .setProperty("generate.arrayMethods", addArrayMethods())
                        .setProperty("generate.keyMethods", addKeyMethods())
                        .setProperty("debug", hasArg(ARG_DEBUG));
            
            // Add plugins to context
            for (GenerationPlugin plugin : plugins) {
                globalContext.addPlugin(plugin);
            }
        }
        
        // Create a new context for each file generation, inheriting from global context
        GenerationContext fileContext = new GenerationContext(loader);
        
        // Copy global settings
        globalContext.getPlugins().forEach(fileContext::addPlugin);
        
        // Copy properties
        for (String key : List.of("generator.type", "package.prefix", "package.suffix", 
                                  "name.prefix", "name.suffix", "generate.arrayMethods", 
                                  "generate.keyMethods", "debug")) {
            if (globalContext.getProperty(key, null) != null) {
                fileContext.setProperty(key, globalContext.getProperty(key, null));
            }
        }
        
        return new EnhancedJavaCodeWriter(loader, pw, fileContext)
                .forType(getType())
                .withPkgPrefix(getPkgPrefix())
                .withPkgSuffix(getPkgSuffix())
                .withNamePrefix(getNamePrefix())
                .withNameSuffix(getNameSuffix())
                .addArrayMethods(addArrayMethods())
                .addKeyMethods(addKeyMethods())
                .withIndentor("    ");
    }

    @Override
    protected void writeSingleFile(MetaObject mo, GeneratorIOWriter<?> writer) throws GeneratorIOException {
        log.info("Writing Enhanced JavaCode ["+getType()+"] to file: " + writer.getFilename() );

        String className = ((EnhancedJavaCodeWriter)writer).writeObject(mo);
        objectNameMap.put(mo, className);
    }

    @Override
    protected GeneratorIOWriter getFinalWriter(MetaDataLoader loader, OutputStream out) throws GeneratorIOException {
        return new JavaCodeOverlayXMLWriter( loader, out )
                .forObjects(objectNameMap);
    }

    @Override
    protected void writeFinalFile(Collection<MetaObject> metadata, GeneratorIOWriter<?> writer) throws GeneratorIOException {
        log.info("Writing Enhanced JavaCode Overlay XML to file: " + writer.getFilename() );
        ((JavaCodeOverlayXMLWriter)writer).writeXML();
    }

    // File path and naming methods (same as SimpleJavaCodeGenerator)
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
        path=path.replace('.', File.separatorChar);
        return path;
    }

    protected String getSingleOutputFilename(MetaObject md) {
        String name = md.getShortName();
        if ( isNotBlank(getNamePrefix())) name = getNamePrefix()+"-"+name;
        if ( isNotBlank(getNameSuffix())) name = name+"-"+getNameSuffix();
        name = name.replaceAll("--","-");
        name = GeneratorUtil.toCamelCase( name, true )+".java";
        return name;
    }

    private boolean isNotBlank(String str) {
        return str != null && !str.trim().isEmpty();
    }
    
    @Override
    public String toString() {
        return super.toString() + 
               ",plugins=" + plugins.size() + 
               ",context=" + (globalContext != null ? "initialized" : "null");
    }
}