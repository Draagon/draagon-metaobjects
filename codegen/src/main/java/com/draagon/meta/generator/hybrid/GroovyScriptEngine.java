package com.draagon.meta.generator.hybrid;

import com.draagon.meta.generator.GeneratorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Groovy implementation of the script engine
 * Note: This requires Groovy to be available on the classpath
 */
public class GroovyScriptEngine implements ScriptEngine {
    
    private static final Logger log = LoggerFactory.getLogger(GroovyScriptEngine.class);
    
    private groovy.util.GroovyScriptEngine groovyEngine;
    private groovy.lang.GroovyShell groovyShell;
    private final Map<String, GroovyCompiledScript> scriptCache = new ConcurrentHashMap<>();
    private boolean initialized = false;
    
    @Override
    public void initialize() throws GeneratorException {
        if (initialized) return;
        
        try {
            // Check if Groovy is available
            Class.forName("groovy.lang.GroovyShell");
            
            // Initialize Groovy components
            groovyShell = new groovy.lang.GroovyShell();
            
            initialized = true;
            log.info("GroovyScriptEngine initialized successfully");
            
        } catch (ClassNotFoundException e) {
            throw new GeneratorException("Groovy is not available on the classpath. " +
                    "Please add Groovy dependency to use hybrid script generators.", e);
        } catch (Exception e) {
            throw new GeneratorException("Failed to initialize Groovy script engine: " + e.getMessage(), e);
        }
    }
    
    @Override
    public CompiledScript compile(String scriptName, String scriptSource) throws GeneratorException {
        if (!initialized) {
            initialize();
        }
        
        try {
            log.debug("Compiling Groovy script: {}", scriptName);
            
            groovy.lang.Script compiledScript = groovyShell.parse(scriptSource, scriptName);
            GroovyCompiledScript wrapped = new GroovyCompiledScript(scriptName, scriptSource, compiledScript);
            
            // Cache the compiled script
            scriptCache.put(scriptName, wrapped);
            
            log.debug("Successfully compiled Groovy script: {}", scriptName);
            return wrapped;
            
        } catch (Exception e) {
            throw new GeneratorException("Failed to compile Groovy script '" + scriptName + "': " + e.getMessage(), e);
        }
    }
    
    @Override
    public Object execute(CompiledScript script, Map<String, Object> bindings) throws GeneratorException {
        if (!(script instanceof GroovyCompiledScript)) {
            throw new GeneratorException("Script must be compiled by GroovyScriptEngine");
        }
        
        return ((GroovyCompiledScript) script).execute(bindings);
    }
    
    @Override
    public Object execute(String scriptSource, Map<String, Object> bindings) throws GeneratorException {
        if (!initialized) {
            initialize();
        }
        
        try {
            // Create a temporary binding
            groovy.lang.Binding binding = new groovy.lang.Binding();
            if (bindings != null) {
                for (Map.Entry<String, Object> entry : bindings.entrySet()) {
                    binding.setVariable(entry.getKey(), entry.getValue());
                }
            }
            
            // Create a shell with the binding and execute
            groovy.lang.GroovyShell shell = new groovy.lang.GroovyShell(binding);
            return shell.evaluate(scriptSource);
            
        } catch (Exception e) {
            throw new GeneratorException("Failed to execute Groovy script: " + e.getMessage(), e);
        }
    }
    
    @Override
    public String getLanguage() {
        return "Groovy";
    }
    
    @Override
    public String getVersion() {
        try {
            return groovy.lang.GroovySystem.getVersion();
        } catch (Exception e) {
            return "unknown";
        }
    }
    
    @Override
    public boolean supportsFeature(String feature) {
        switch (feature.toLowerCase()) {
            case "compilation":
            case "caching":
            case "bindings":
            case "closures":
            case "dynamic_typing":
                return true;
            default:
                return false;
        }
    }
    
    @Override
    public void shutdown() {
        scriptCache.clear();
        groovyShell = null;
        groovyEngine = null;
        initialized = false;
        log.info("GroovyScriptEngine shutdown complete");
    }
    
    public Map<String, GroovyCompiledScript> getScriptCache() {
        return new ConcurrentHashMap<>(scriptCache);
    }
    
    public void clearCache() {
        scriptCache.clear();
        log.debug("Script cache cleared");
    }
    
    // Inner class for Groovy compiled scripts
    static class GroovyCompiledScript implements CompiledScript {
        private final String name;
        private final String source;
        private final groovy.lang.Script compiledScript;
        private final long compilationTime;
        
        public GroovyCompiledScript(String name, String source, groovy.lang.Script compiledScript) {
            this.name = name;
            this.source = source;
            this.compiledScript = compiledScript;
            this.compilationTime = System.currentTimeMillis();
        }
        
        @Override
        public Object execute(Map<String, Object> bindings) throws GeneratorException {
            try {
                // Set up binding for this execution
                groovy.lang.Binding binding = new groovy.lang.Binding();
                if (bindings != null) {
                    for (Map.Entry<String, Object> entry : bindings.entrySet()) {
                        binding.setVariable(entry.getKey(), entry.getValue());
                    }
                }
                
                // Create a copy of the script with the new binding
                compiledScript.setBinding(binding);
                
                // Execute the script
                return compiledScript.run();
                
            } catch (Exception e) {
                throw new GeneratorException("Failed to execute compiled Groovy script '" + name + "': " + e.getMessage(), e);
            }
        }
        
        @Override
        public String getName() {
            return name;
        }
        
        @Override
        public String getSource() {
            return source;
        }
        
        @Override
        public long getCompilationTime() {
            return compilationTime;
        }
        
        @Override
        public String toString() {
            return "GroovyCompiledScript{name='" + name + "', compilationTime=" + 
                   new java.util.Date(compilationTime) + "}";
        }
    }
}