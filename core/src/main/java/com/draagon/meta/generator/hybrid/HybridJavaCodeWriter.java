package com.draagon.meta.generator.hybrid;

import com.draagon.meta.field.MetaField;
import com.draagon.meta.generator.GeneratorIOException;
import com.draagon.meta.generator.direct.object.javacode.JavaCodeWriter;
import com.draagon.meta.generator.direct.GenerationContext;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.object.MetaObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.util.Map;
import java.util.HashMap;

/**
 * Hybrid Java Code Writer that extends JavaCodeWriter with script execution capabilities
 */
public class HybridJavaCodeWriter extends JavaCodeWriter {
    
    private static final Logger log = LoggerFactory.getLogger(HybridJavaCodeWriter.class);
    
    protected ScriptEngine scriptEngine;
    protected ScriptContext scriptContext;
    protected Map<String, CompiledScript> compiledScripts = new HashMap<>();
    
    public HybridJavaCodeWriter(MetaDataLoader loader, PrintWriter pw) {
        super(loader, pw);
        this.scriptContext = new ScriptContext(loader);
    }
    
    public HybridJavaCodeWriter(MetaDataLoader loader, PrintWriter pw, ScriptContext context) {
        super(loader, pw, createCompatibilityContext(loader, context));
        this.scriptContext = context;
    }
    
    private static GenerationContext createCompatibilityContext(MetaDataLoader loader, ScriptContext context) {
        GenerationContext genContext = new GenerationContext(loader);
        genContext.setCurrentObject(context.getCurrentObject());
        genContext.setCurrentField(context.getCurrentField());
        genContext.setCurrentPackage(context.getCurrentPackage());
        genContext.setCurrentClassName(context.getCurrentClassName());
        return genContext;
    }
    
    // Configuration methods
    public HybridJavaCodeWriter withScriptEngine(ScriptEngine engine) {
        this.scriptEngine = engine;
        return this;
    }
    
    public HybridJavaCodeWriter withCompiledScripts(Map<String, CompiledScript> scripts) {
        this.compiledScripts = new HashMap<>(scripts);
        return this;
    }
    
    public HybridJavaCodeWriter addCompiledScript(String name, CompiledScript script) {
        this.compiledScripts.put(name, script);
        return this;
    }
    
    public HybridJavaCodeWriter withScriptContext(ScriptContext context) {
        this.scriptContext = context;
        // Create a GenerationContext wrapper for compatibility
        GenerationContext genContext = new GenerationContext(context.getLoader());
        genContext.setCurrentObject(context.getCurrentObject());
        genContext.setCurrentField(context.getCurrentField());
        genContext.setCurrentPackage(context.getCurrentPackage());
        genContext.setCurrentClassName(context.getCurrentClassName());
        super.withContext(genContext);
        return this;
    }
    
    @Override
    public String writeObject(MetaObject mo) throws GeneratorIOException {
        // Set up script context for this object
        scriptContext.setCurrentObject(mo);
        scriptContext.setBinding("object", mo);
        scriptContext.setBinding("writer", this);
        scriptContext.setBinding("logger", log);
        
        // Execute pre-object generation scripts
        executeScripts("beforeObject", mo, null);
        
        try {
            // Call the parent implementation
            String result = super.writeObject(mo);
            
            // Execute post-object generation scripts
            executeScripts("afterObject", mo, null);
            
            return result;
            
        } catch (Exception e) {
            throw new GeneratorIOException(this, "Error in hybrid object generation: " + e.getMessage(), e);
        }
    }
    
    @Override
    protected void writeObjectMethods(MetaObject mo) {
        inc();

        for (MetaField mf : mo.getMetaFields(false)) {
            context.setCurrentField(mf);
            scriptContext.setCurrentField(mf);
            scriptContext.setBinding("field", mf);
            
            // Execute pre-field generation scripts
            executeScripts("beforeField", mo, mf);
            
            // Notify plugins before field generation (from parent)
            for (com.draagon.meta.generator.direct.BaseGenerationPlugin<com.draagon.meta.object.MetaObject> plugin : context.getPlugins()) {
                if (plugin instanceof com.draagon.meta.generator.direct.GenerationPlugin) {
                    // Cast context to GenerationContext for plugin compatibility
                    com.draagon.meta.generator.direct.GenerationContext genContext = new com.draagon.meta.generator.direct.GenerationContext(context.getLoader());
                    genContext.setCurrentObject(context.getCurrentObject());
                    genContext.setCurrentField(context.getCurrentField());
                    ((com.draagon.meta.generator.direct.GenerationPlugin) plugin).beforeFieldGeneration(mf, genContext, this);
                }
            }

            // Check if scripts want to completely override field generation
            boolean skipStandardGeneration = scriptContext.getBooleanProperty("skipStandardGeneration", false);
            
            if (!skipStandardGeneration) {
                // Check if we should generate getters/setters (plugins might disable this)
                boolean generateGetters = context.getBooleanProperty("generate.getters", true);
                boolean generateSetters = context.getBooleanProperty("generate.setters", true);

                if (generateGetters || generateSetters) {
                    String getterName = getGetterMethodName(mf);
                    String setterName = getSetterMethodName(mf);
                    String valueName = getParameterName(mf);
                    String valueClass = getLanguageType(mf);

                    // Allow plugins to customize method names and types
                    for (com.draagon.meta.generator.direct.BaseGenerationPlugin<com.draagon.meta.object.MetaObject> plugin : context.getPlugins()) {
                        if (plugin instanceof com.draagon.meta.generator.direct.GenerationPlugin) {
                            com.draagon.meta.generator.direct.GenerationPlugin gp = (com.draagon.meta.generator.direct.GenerationPlugin) plugin;
                            // Create compatible GenerationContext for plugin calls
                            com.draagon.meta.generator.direct.GenerationContext genContext = new com.draagon.meta.generator.direct.GenerationContext(context.getLoader());
                            genContext.setCurrentObject(context.getCurrentObject());
                            genContext.setCurrentField(context.getCurrentField());
                            getterName = gp.customizeMethodName(mf, "getter", getterName, genContext);
                            setterName = gp.customizeMethodName(mf, "setter", setterName, genContext);
                            valueClass = gp.customizeFieldType(mf, valueClass, genContext);
                        }
                    }

                    // Allow scripts to customize as well
                    getterName = executeScriptFunction("customizeGetterName", getterName, mf, getterName);
                    setterName = executeScriptFunction("customizeSetterName", setterName, mf, setterName);
                    valueClass = executeScriptFunction("customizeFieldType", valueClass, mf, valueClass);

                    writeNewLine();
                    writeComment("////////////////////////////////////////////////////////////////////////////////////" );
                    writeComment("Methods for MetaField: " + mf.getName());
                    
                    // Execute field-specific scripts
                    executeScripts("fieldComment", mo, mf);
                    
                    writeNewLine();
                    
                    if (generateGetters) {
                        // Execute pre-getter scripts
                        executeScripts("beforeGetter", mo, mf);
                        writeGetter(getterName, valueClass, mf);
                        executeScripts("afterGetter", mo, mf);
                    }
                    
                    if (generateSetters) {
                        // Execute pre-setter scripts
                        executeScripts("beforeSetter", mo, mf);
                        writeSetter(setterName, valueName, valueClass, mf);
                        executeScripts("afterSetter", mo, mf);
                    }
                }
            }
            
            // Execute post-field generation scripts
            executeScripts("afterField", mo, mf);
            
            // Notify plugins after field generation (from parent)
            for (com.draagon.meta.generator.direct.BaseGenerationPlugin<com.draagon.meta.object.MetaObject> plugin : context.getPlugins()) {
                if (plugin instanceof com.draagon.meta.generator.direct.GenerationPlugin) {
                    // Create compatible GenerationContext for plugin calls
                    com.draagon.meta.generator.direct.GenerationContext genContext = new com.draagon.meta.generator.direct.GenerationContext(context.getLoader());
                    genContext.setCurrentObject(context.getCurrentObject());
                    genContext.setCurrentField(context.getCurrentField());
                    ((com.draagon.meta.generator.direct.GenerationPlugin) plugin).afterFieldGeneration(mf, genContext, this);
                }
            }
            
            // Reset skip flag for next field
            scriptContext.setProperty("skipStandardGeneration", false);
        }

        dec();
    }
    
    /**
     * Execute all compiled scripts that match a given trigger point
     */
    protected void executeScripts(String trigger, MetaObject object, MetaField field) {
        if (scriptEngine == null || compiledScripts.isEmpty()) {
            return;
        }
        
        try {
            // Set up bindings for script execution
            Map<String, Object> bindings = scriptContext.getAllBindings();
            bindings.put("trigger", trigger);
            bindings.put("object", object);
            bindings.put("field", field);
            bindings.put("context", scriptContext);
            bindings.put("writer", this);
            
            // Execute all compiled scripts
            for (Map.Entry<String, CompiledScript> entry : compiledScripts.entrySet()) {
                try {
                    log.debug("Executing script '{}' for trigger '{}'", entry.getKey(), trigger);
                    Object result = scriptEngine.execute(entry.getValue(), bindings);
                    
                    // Store script result in context for potential use by other scripts
                    scriptContext.setScriptVariable("lastScriptResult", result);
                    
                } catch (Exception e) {
                    log.warn("Error executing script '{}' for trigger '{}': {}", 
                            entry.getKey(), trigger, e.getMessage());
                    // Continue with other scripts even if one fails
                }
            }
            
        } catch (Exception e) {
            log.error("Error in script execution for trigger '{}': {}", trigger, e.getMessage());
        }
    }
    
    /**
     * Execute a specific script function and return the result, with fallback to default
     */
    protected <T> T executeScriptFunction(String functionName, T defaultValue, Object... args) {
        if (scriptEngine == null) {
            return defaultValue;
        }
        
        try {
            // Try to find and execute a script function
            for (CompiledScript script : compiledScripts.values()) {
                Map<String, Object> bindings = scriptContext.getAllBindings();
                bindings.put("functionName", functionName);
                bindings.put("args", args);
                bindings.put("defaultValue", defaultValue);
                
                Object result = scriptEngine.execute(script, bindings);
                
                // If script returned non-null result, use it
                if (result != null && !result.equals(defaultValue)) {
                    return (T) result;
                }
            }
            
        } catch (Exception e) {
            log.debug("Error executing script function '{}': {}", functionName, e.getMessage());
        }
        
        return defaultValue;
    }
    
    /**
     * Allow scripts to directly write output
     */
    public void writeScriptOutput(String output) {
        if (output != null && !output.isEmpty()) {
            for (String line : output.split("\n")) {
                println(true, line);
            }
        }
    }
    
    /**
     * Allow scripts to add custom imports
     */
    public void addScriptImport(String importStatement) {
        scriptContext.addImport(importStatement);
        importList.add(importStatement);
    }
    
    /**
     * Allow scripts to add custom comments
     */
    public void addScriptComment(String comment) {
        writeComment(comment);
    }
    
    /**
     * Get the script context for advanced script operations
     */
    public ScriptContext getScriptContext() {
        return scriptContext;
    }
    
    /**
     * Check if scripting is enabled
     */
    public boolean isScriptingEnabled() {
        return scriptEngine != null && !compiledScripts.isEmpty();
    }
    
    /**
     * Get count of compiled scripts
     */
    public int getCompiledScriptCount() {
        return compiledScripts.size();
    }
    
    @Override
    protected String getToStringOptions() {
        return super.getToStringOptions() + 
               ",scripting=" + (scriptEngine != null ? scriptEngine.getLanguage() : "disabled") +
               ",scripts=" + compiledScripts.size();
    }
}