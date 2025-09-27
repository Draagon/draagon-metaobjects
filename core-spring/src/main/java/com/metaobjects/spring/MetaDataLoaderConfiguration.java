package com.metaobjects.spring;

import com.metaobjects.loader.MetaDataLoader;
import com.metaobjects.loader.simple.SimpleLoader;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Example Spring configuration showing how to set up MetaDataLoaders as beans.
 * 
 * <p>Spring Boot applications can use this pattern to automatically configure
 * MetaDataLoaders that will be discovered and registered by {@link MetaDataAutoConfiguration}.</p>
 * 
 * <p><strong>application.yml Example:</strong></p>
 * <pre>{@code
 * metaobjects:
 *   metadata-sources:
 *     - "classpath:metadata/core-metadata.json"
 *     - "classpath:metadata/user-metadata.json"
 *     - "file:/path/to/external-metadata.xml"
 *   auto-attrs: true
 *   strict: false
 * }</pre>
 * 
 * <p><strong>Or use @Bean methods directly:</strong></p>
 * <pre>{@code
 * @Configuration
 * public class MyMetaDataConfig {
 *     
 *     @Bean
 *     public MetaDataLoader userMetaDataLoader() {
 *         SimpleLoader loader = new SimpleLoader("userLoader");
 *         loader.setSourceURIs(Arrays.asList(
 *             URI.create("classpath:metadata/users.json")
 *         ));
 *         loader.init();
 *         return loader;
 *     }
 * }
 * }</pre>
 * 
 * @since 6.0.0
 */
@Configuration
@ConfigurationProperties(prefix = "metaobjects")
public class MetaDataLoaderConfiguration {
    
    private List<String> metadataSources = new ArrayList<>();
    private boolean autoAttrs = true;
    private boolean strict = false;
    private boolean verbose = false;
    
    /**
     * Creates MetaDataLoader from application properties.
     * 
     * <p>This bean will be automatically discovered and registered by 
     * {@link MetaDataAutoConfiguration}.</p>
     */
    @Bean
    public MetaDataLoader applicationMetaDataLoader() throws Exception {
        if (metadataSources.isEmpty()) {
            // Default: look for metadata files in classpath
            metadataSources = discoverDefaultMetadataSources();
        }
        
        if (metadataSources.isEmpty()) {
            // No metadata sources configured - return null so it's optional
            return null;
        }
        
        // Convert string sources to URIs
        List<URI> sourceURIs = new ArrayList<>();
        for (String source : metadataSources) {
            sourceURIs.add(URI.create(source));
        }
        
        // Create and configure loader
        SimpleLoader loader = new SimpleLoader("applicationLoader");
        
        loader.setSourceURIs(sourceURIs);
        loader.init();
        
        return loader;
    }
    
    /**
     * Example: Database metadata loader bean
     * 
     * This shows how to create specialized loaders for different data sources.
     */
    // @Bean
    // public MetaDataLoader databaseMetaDataLoader(DataSource dataSource) {
    //     DatabaseMetaDataLoader loader = new DatabaseMetaDataLoader("dbLoader", dataSource);
    //     loader.init();
    //     return loader;
    // }
    
    /**
     * Example: External file system loader
     */
    // @Bean
    // @ConditionalOnProperty("metaobjects.external-path")
    // public MetaDataLoader externalMetaDataLoader(@Value("${metaobjects.external-path}") String path) {
    //     FileMetaDataLoader loader = new FileMetaDataLoader("externalLoader");
    //     loader.setSourceURIs(Arrays.asList(URI.create("file:" + path)));
    //     loader.init(); 
    //     return loader;
    // }
    
    /**
     * Discover default metadata sources from classpath
     */
    private List<String> discoverDefaultMetadataSources() {
        List<String> sources = new ArrayList<>();
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        
        try {
            // Look for common metadata file patterns
            String[] patterns = {
                "classpath*:metadata/*.json",
                "classpath*:metadata/*.xml", 
                "classpath*:META-INF/metadata/*.json",
                "classpath*:META-INF/metadata/*.xml"
            };
            
            for (String pattern : patterns) {
                Resource[] resources = resolver.getResources(pattern);
                for (Resource resource : resources) {
                    sources.add(resource.getURI().toString());
                }
            }
        } catch (Exception e) {
            // Ignore discovery errors - sources will be empty
        }
        
        return sources;
    }
    
    // Property setters for Spring Boot configuration binding
    public List<String> getMetadataSources() { return metadataSources; }
    public void setMetadataSources(List<String> metadataSources) { this.metadataSources = metadataSources; }
    
    public boolean isAutoAttrs() { return autoAttrs; }
    public void setAutoAttrs(boolean autoAttrs) { this.autoAttrs = autoAttrs; }
    
    public boolean isStrict() { return strict; }
    public void setStrict(boolean strict) { this.strict = strict; }
    
    public boolean isVerbose() { return verbose; }
    public void setVerbose(boolean verbose) { this.verbose = verbose; }
}