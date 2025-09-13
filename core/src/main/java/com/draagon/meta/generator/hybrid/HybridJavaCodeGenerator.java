package com.draagon.meta.generator.hybrid;

import com.draagon.meta.generator.GeneratorException;
import com.draagon.meta.generator.GeneratorIOException;
import com.draagon.meta.generator.GeneratorIOWriter;
import com.draagon.meta.generator.direct.javacode.simple.EnhancedJavaCodeGenerator;
import com.draagon.meta.generator.direct.javacode.simple.EnhancedJavaCodeWriter;
import com.draagon.meta.generator.direct.GenerationContext;
import com.draagon.meta.generator.direct.GenerationPlugin;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.object.MetaObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

/**
 * Hybrid Java Code Generator that extends the enhanced direct generator 
 * with Groovy scripting capabilities
 */
public class HybridJavaCodeGenerator extends EnhancedJavaCodeGenerator {
    
    private static final Logger log = LoggerFactory.getLogger(HybridJavaCodeGenerator.class);
    
    // New arguments for hybrid functionality
    public final static String ARG_SCRIPT_ENGINE = "scriptEngine";  // groovy, javascript, etc.
    public final static String ARG_SCRIPTS_DIR = "scriptsDir";      // directory containing scripts
    public final static String ARG_SCRIPT_FILES = "scriptFiles";    // comma-separated script files
    public final static String ARG_INLINE_SCRIPTS = "inlineScripts"; // semicolon-separated inline scripts
    public final static String ARG_ENABLE_CACHING = "enableCaching"; // enable script compilation caching
    
    protected ScriptEngine scriptEngine;
    protected ScriptContext globalScriptContext;
    protected final Map<String, CompiledScript> compiledScripts = new HashMap<>();
    protected final List<String> scriptFiles = new ArrayList<>();
    protected final List<String> inlineScripts = new ArrayList<>();
    
    public HybridJavaCodeGenerator() {
        super();
        log.info("Initializing HybridJavaCodeGenerator");
    }
    
    /**
     * Add a script file to be loaded and compiled
     */
    public HybridJavaCodeGenerator addScriptFile(String scriptFile) {
        scriptFiles.add(scriptFile);
        return this;
    }
    
    /**
     * Add an inline script to be compiled and executed
     */
    public HybridJavaCodeGenerator addInlineScript(String script) {
        inlineScripts.add(script);
        return this;
    }
    
    /**
     * Set the script engine to use (defaults to Groovy)
     */
    public HybridJavaCodeGenerator withScriptEngine(ScriptEngine engine) {
        this.scriptEngine = engine;
        return this;
    }
    
    /**
     * Configure with a custom script context
     */
    public HybridJavaCodeGenerator withScriptContext(ScriptContext context) {
        this.globalScriptContext = context;
        return this;
    }
    
    @Override
    protected void parseArgs() {
        super.parseArgs();
        
        // Initialize script engine if not already set
        if (scriptEngine == null) {
            String engineType = getArg(ARG_SCRIPT_ENGINE, "groovy");
            initializeScriptEngine(engineType);
        }
        
        // Initialize script context if not already set
        if (globalScriptContext == null) {
            globalScriptContext = new ScriptContext(globalContext);
        }
        
        // Parse script-related arguments
        parseScriptArguments();
        
        // Load and compile scripts
        loadAndCompileScripts();
        
        log.info("Hybrid generator initialized with {} compiled scripts", compiledScripts.size());
    }
    
    private void initializeScriptEngine(String engineType) {
        try {
            switch (engineType.toLowerCase()) {
                case "groovy":
                    scriptEngine = new GroovyScriptEngine();
                    break;
                default:
                    throw new GeneratorException("Unsupported script engine: " + engineType + 
                                               ". Supported engines: groovy");
            }
            
            scriptEngine.initialize();
            log.info("Initialized {} script engine version {}", 
                    scriptEngine.getLanguage(), scriptEngine.getVersion());
            
        } catch (Exception e) {
            throw new GeneratorException("Failed to initialize script engine '" + engineType + "': " + e.getMessage(), e);
        }
    }
    
    private void parseScriptArguments() {
        // Parse script files argument
        if (hasArg(ARG_SCRIPT_FILES)) {
            String files = getArg(ARG_SCRIPT_FILES);
            for (String file : files.split(",")) {
                scriptFiles.add(file.trim());
            }
        }
        
        // Parse inline scripts argument  
        if (hasArg(ARG_INLINE_SCRIPTS)) {
            String scripts = getArg(ARG_INLINE_SCRIPTS);
            for (String script : scripts.split(";")) {
                inlineScripts.add(script.trim());
            }
        }
        
        // Configure script context
        globalScriptContext.setProperty("hybrid.cacheEnabled", getBooleanProperty(ARG_ENABLE_CACHING, true));
        globalScriptContext.setProperty("hybrid.scriptsDir", getArg(ARG_SCRIPTS_DIR, "."));
    }
    
    private void loadAndCompileScripts() {
        try {
            // Compile script files
            for (int i = 0; i < scriptFiles.size(); i++) {
                String scriptFile = scriptFiles.get(i);
                log.debug("Loading script file: {}", scriptFile);
                
                // In a real implementation, you'd read from the file system
                // For now, we'll create a placeholder
                String scriptContent = loadScriptFromFile(scriptFile);
                String scriptName = "scriptFile_" + i + "_" + scriptFile;
                
                CompiledScript compiled = scriptEngine.compile(scriptName, scriptContent);
                compiledScripts.put(scriptName, compiled);
            }
            
            // Compile inline scripts
            for (int i = 0; i < inlineScripts.size(); i++) {
                String scriptContent = inlineScripts.get(i);
                String scriptName = "inlineScript_" + i;
                
                CompiledScript compiled = scriptEngine.compile(scriptName, scriptContent);
                compiledScripts.put(scriptName, compiled);
            }
            
            log.info("Compiled {} scripts successfully", compiledScripts.size());
            
        } catch (Exception e) {
            throw new GeneratorException("Failed to load and compile scripts: " + e.getMessage(), e);
        }
    }
    
    private String loadScriptFromFile(String scriptFile) {
        // Placeholder implementation - in reality would read from file system
        // This is a sample Groovy script for field generation
        return """
            // Sample Groovy script for hybrid generation
            def generateCustomField(field, context) {
                if (field.name.endsWith("Id")) {
                    return "// This is a generated ID field: " + field.name
                }
                return null
            }
            
            // Main script execution
            if (context.currentField != null) {
                def customCode = generateCustomField(context.currentField, context)
                if (customCode != null) {
                    writer.println(true, customCode)
                }
            }
        """;
    }
    
    @Override
    protected HybridJavaCodeWriter getSingleWriter(MetaDataLoader loader, MetaObject md, PrintWriter pw) throws GeneratorIOException {
        
        // Create enhanced writer first
        HybridJavaCodeWriter writer = new HybridJavaCodeWriter(loader, pw, globalScriptContext);
        
        // Configure the writer - these methods return EnhancedJavaCodeWriter, so we need to cast
        ((EnhancedJavaCodeWriter) writer).forType(getType())
                .withPkgPrefix(getPkgPrefix())
                .withPkgSuffix(getPkgSuffix())
                .withNamePrefix(getNamePrefix())
                .withNameSuffix(getNameSuffix())
                .addArrayMethods(addArrayMethods())
                .addKeyMethods(addKeyMethods())
                .withIndentor("    ");
        
        // Configure with script engine and compiled scripts
        writer.withScriptEngine(scriptEngine)
              .withCompiledScripts(compiledScripts);
        
        return writer;
    }
    
    @Override
    protected void writeSingleFile(MetaObject mo, GeneratorIOWriter<?> writer) throws GeneratorIOException {
        log.info("Writing Hybrid JavaCode [{}] with {} scripts to file: {}", 
                getType(), compiledScripts.size(), writer.getFilename());

        String className = ((HybridJavaCodeWriter)writer).writeObject(mo);
        objectNameMap.put(mo, className);
    }
    
    // Helper method to get boolean properties
    private boolean getBooleanProperty(String key, boolean defaultValue) {
        if (hasArg(key)) {
            return Boolean.parseBoolean(getArg(key));
        }
        return defaultValue;
    }
    
    @Override
    public String toString() {
        return super.toString() + 
               ",scriptEngine=" + (scriptEngine != null ? scriptEngine.getLanguage() : "none") +
               ",scripts=" + compiledScripts.size() +
               ",scriptFiles=" + scriptFiles.size() +
               ",inlineScripts=" + inlineScripts.size();
    }
    
    /**
     * Clean up resources when generator is done
     */
    public void shutdown() {
        if (scriptEngine != null) {
            scriptEngine.shutdown();
        }
        compiledScripts.clear();
        scriptFiles.clear();
        inlineScripts.clear();
    }
}