package com.metaobjects.mojo;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration parameter for MetaObjects data loaders in Maven plugin.
 * Provides fluent configuration through builder pattern for loader name, classname,
 * source directories, sources, and filters used during metadata loading.
 */
public class LoaderParam {

    private String name = null;
    private String classname = null;
    private String sourceDir = null;
    private List<String> sources = null;
    private List<String> filters = null;

    public LoaderParam() {}

    /**
     * Set the loader name (identifier)
     * @param name Loader name/identifier
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the loader name (identifier)
     * @return Loader name, or null if not set
     */
    public String getName() {
        return name;
    }

    /**
     * Set the loader name (alias for setName)
     * @param name Loader name/identifier
     */
    public void setLoaderName(String name) {
        this.name = name;
    }

    /**
     * Get the fully qualified classname of the loader implementation
     * @return Loader classname, or null if not set
     */
    public String getClassname() {
        return classname;
    }

    /**
     * Set the fully qualified classname of the loader implementation
     * @param classname Loader classname (e.g., "com.metaobjects.loader.file.FileMetaDataLoader")
     */
    public void setClassname(String classname) {
        this.classname = classname;
    }

    /**
     * Get the source directory path for metadata files
     * @return Source directory path, or null if not set
     */
    public String getSourceDir() {
        return sourceDir;
    }

    /**
     * Set the source directory path for metadata files
     * @param sourceDir Path to directory containing metadata files
     */
    public void setSourceDir(String sourceDir) {
        this.sourceDir = sourceDir;
    }

    /**
     * Get the list of source files/patterns to load
     * @return List of source files/patterns, or null if not set
     */
    public List<String> getSources() {
        return sources;
    }

    /**
     * Set the list of source files/patterns to load
     * @param sources List of source file paths or patterns
     */
    public void setSources(List<String> sources) {
        this.sources = sources;
    }

    /**
     * Get the list of filters to apply during loading
     * @return List of filters, or null if not set
     */
    public List<String> getFilters() {
        return filters;
    }

    /**
     * Set the list of filters to apply during loading
     * @param filters List of filter patterns
     */
    public void setFilters(List<String> filters) {
        this.filters = filters;
    }

    //////////////////////////////////////////////////////////////
    // BUILDER PATTERN SUPPORT

    /**
     * Create a new LoaderParam builder
     * @return New LoaderParam.Builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Create a new LoaderParam builder with specified name
     * @param name Loader name
     * @return New LoaderParam.Builder instance
     */
    public static Builder builder(String name) {
        return new Builder(name);
    }

    /**
     * Builder pattern for LoaderParam configuration
     */
    public static class Builder {
        private String name;
        private String classname;
        private String sourceDir;
        private final List<String> sources = new ArrayList<>();
        private final List<String> filters = new ArrayList<>();

        public Builder() {}

        public Builder(String name) {
            this.name = name;
        }

        /**
         * Set the loader name
         * @param name Loader name/identifier
         * @return This builder for method chaining
         */
        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        /**
         * Set the loader classname
         * @param classname Fully qualified loader classname
         * @return This builder for method chaining
         */
        public Builder withClassname(String classname) {
            this.classname = classname;
            return this;
        }

        /**
         * Set the source directory
         * @param sourceDir Path to source directory
         * @return This builder for method chaining
         */
        public Builder withSourceDir(String sourceDir) {
            this.sourceDir = sourceDir;
            return this;
        }

        /**
         * Add a source file/pattern
         * @param source Source file path or pattern
         * @return This builder for method chaining
         */
        public Builder withSource(String source) {
            this.sources.add(source);
            return this;
        }

        /**
         * Add multiple source files/patterns
         * @param sources List of source file paths or patterns
         * @return This builder for method chaining
         */
        public Builder withSources(List<String> sources) {
            this.sources.addAll(sources);
            return this;
        }

        /**
         * Add a filter
         * @param filter Filter pattern
         * @return This builder for method chaining
         */
        public Builder withFilter(String filter) {
            this.filters.add(filter);
            return this;
        }

        /**
         * Add multiple filters
         * @param filters List of filter patterns
         * @return This builder for method chaining
         */
        public Builder withFilters(List<String> filters) {
            this.filters.addAll(filters);
            return this;
        }

        /**
         * Build the configured LoaderParam
         * @return Configured LoaderParam instance
         * @throws IllegalStateException if name is not set
         */
        public LoaderParam build() {
            if (name == null || name.trim().isEmpty()) {
                throw new IllegalStateException("Loader name is required");
            }

            LoaderParam param = new LoaderParam();
            param.name = this.name;
            param.classname = this.classname;
            param.sourceDir = this.sourceDir;
            
            if (!sources.isEmpty()) {
                param.sources = new ArrayList<>(this.sources);
            }
            
            if (!filters.isEmpty()) {
                param.filters = new ArrayList<>(this.filters);
            }
            
            return param;
        }
    }
}
