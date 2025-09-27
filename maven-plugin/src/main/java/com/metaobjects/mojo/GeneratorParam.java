package com.metaobjects.mojo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Configuration parameter for MetaObjects code generators in Maven plugin.
 * Provides fluent configuration through builder pattern for classname, arguments,
 * filters, and scripts used during code generation.
 */
public class GeneratorParam {

    private String classname = null;
    private Map<String,String> args = null;
    private List<String> filters = null;
    private List<String> scripts = null;

    public GeneratorParam() {}

    /**
     * Get the fully qualified classname of the generator implementation
     * @return Generator classname, or null if not set
     */
    public String getClassname() {
        return classname;
    }

    /**
     * Set the fully qualified classname of the generator implementation
     * @param classname Generator classname (e.g., "com.metaobjects.generator.direct.plantuml.PlantUMLGenerator")
     */
    public void setClassname(String classname) {
        this.classname = classname;
    }

    /**
     * Get the generator arguments (key-value pairs)
     * @return Map of generator arguments, or null if not set
     */
    public Map<String, String> getArgs() {
        return args;
    }

    /**
     * Set the generator arguments (key-value pairs)
     * @param args Map of generator arguments
     */
    public void setArgs(Map<String, String> args) {
        this.args = args;
    }

    /**
     * Get the list of filters to apply during generation
     * @return List of filters, or null if not set
     */
    public List<String> getFilters() {
        return filters;
    }

    /**
     * Set the list of filters to apply during generation
     * @param filters List of filter patterns
     */
    public void setFilters(List<String> filters) {
        this.filters = filters;
    }

    /**
     * Get the list of scripts to execute during generation
     * @return List of scripts, or null if not set
     */
    public List<String> getScripts() {
        return scripts;
    }

    /**
     * Set the list of scripts to execute during generation
     * @param scripts List of script paths or content
     */
    public void setScripts(List<String> scripts) {
        this.scripts = scripts;
    }

    //////////////////////////////////////////////////////////////
    // BUILDER PATTERN SUPPORT

    /**
     * Create a new GeneratorParam builder
     * @return New GeneratorParam.Builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Create a new GeneratorParam builder with specified classname
     * @param classname Generator classname
     * @return New GeneratorParam.Builder instance
     */
    public static Builder builder(String classname) {
        return new Builder(classname);
    }

    /**
     * Builder pattern for GeneratorParam configuration
     */
    public static class Builder {
        private String classname;
        private final Map<String, String> args = new HashMap<>();
        private final List<String> filters = new ArrayList<>();
        private final List<String> scripts = new ArrayList<>();

        public Builder() {}

        public Builder(String classname) {
            this.classname = classname;
        }

        /**
         * Set the generator classname
         * @param classname Fully qualified generator classname
         * @return This builder for method chaining
         */
        public Builder withClassname(String classname) {
            this.classname = classname;
            return this;
        }

        /**
         * Add a generator argument
         * @param key Argument key
         * @param value Argument value
         * @return This builder for method chaining
         */
        public Builder withArg(String key, String value) {
            this.args.put(key, value);
            return this;
        }

        /**
         * Add multiple generator arguments
         * @param args Map of arguments to add
         * @return This builder for method chaining
         */
        public Builder withArgs(Map<String, String> args) {
            this.args.putAll(args);
            return this;
        }

        /**
         * Add a filter
         * @param filter Filter pattern to add
         * @return This builder for method chaining
         */
        public Builder withFilter(String filter) {
            this.filters.add(filter);
            return this;
        }

        /**
         * Add multiple filters
         * @param filters List of filter patterns to add
         * @return This builder for method chaining
         */
        public Builder withFilters(List<String> filters) {
            this.filters.addAll(filters);
            return this;
        }

        /**
         * Add a script
         * @param script Script path or content to add
         * @return This builder for method chaining
         */
        public Builder withScript(String script) {
            this.scripts.add(script);
            return this;
        }

        /**
         * Add multiple scripts
         * @param scripts List of script paths or content to add
         * @return This builder for method chaining
         */
        public Builder withScripts(List<String> scripts) {
            this.scripts.addAll(scripts);
            return this;
        }

        /**
         * Build the configured GeneratorParam
         * @return Configured GeneratorParam instance
         * @throws IllegalStateException if classname is not set
         */
        public GeneratorParam build() {
            if (classname == null || classname.trim().isEmpty()) {
                throw new IllegalStateException("Generator classname is required");
            }

            GeneratorParam param = new GeneratorParam();
            param.classname = this.classname;
            
            if (!args.isEmpty()) {
                param.args = new HashMap<>(this.args);
            }
            
            if (!filters.isEmpty()) {
                param.filters = new ArrayList<>(this.filters);
            }
            
            if (!scripts.isEmpty()) {
                param.scripts = new ArrayList<>(this.scripts);
            }
            
            return param;
        }
    }
}
