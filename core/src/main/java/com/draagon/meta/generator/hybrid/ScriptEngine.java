package com.draagon.meta.generator.hybrid;

import com.draagon.meta.generator.GeneratorException;
import java.util.Map;

/**
 * Interface for script engines that can execute generation scripts
 */
public interface ScriptEngine {
    
    /**
     * Initialize the script engine
     */
    void initialize() throws GeneratorException;
    
    /**
     * Compile a script from source code
     * @param scriptName name/id for the script
     * @param scriptSource the script source code
     * @return compiled script that can be executed
     */
    CompiledScript compile(String scriptName, String scriptSource) throws GeneratorException;
    
    /**
     * Execute a compiled script with the given bindings
     * @param script the compiled script to execute
     * @param bindings variables to make available to the script
     * @return the result of script execution
     */
    Object execute(CompiledScript script, Map<String, Object> bindings) throws GeneratorException;
    
    /**
     * Execute script source directly (less efficient than compile + execute)
     * @param scriptSource the script source code
     * @param bindings variables to make available to the script
     * @return the result of script execution
     */
    Object execute(String scriptSource, Map<String, Object> bindings) throws GeneratorException;
    
    /**
     * Get the script language/engine name
     */
    String getLanguage();
    
    /**
     * Get the script engine version
     */
    String getVersion();
    
    /**
     * Check if the engine supports a particular feature
     */
    boolean supportsFeature(String feature);
    
    /**
     * Clean up resources
     */
    void shutdown();
}

/**
 * Represents a compiled script that can be executed multiple times
 */
interface CompiledScript {
    
    /**
     * Execute the script with the given bindings
     */
    Object execute(Map<String, Object> bindings) throws GeneratorException;
    
    /**
     * Get the script name/identifier
     */
    String getName();
    
    /**
     * Get the original source code
     */
    String getSource();
    
    /**
     * Get compilation timestamp
     */
    long getCompilationTime();
}