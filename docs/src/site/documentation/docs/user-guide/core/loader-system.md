# Loader System Architecture

The MetaObjects core module provides a comprehensive file-based loading system that implements the READ-OPTIMIZED WITH CONTROLLED MUTABILITY pattern. The loader system is responsible for discovering, loading, and parsing metadata files from various sources while maintaining the framework's performance characteristics.

## Core Architecture Overview

The loader system follows a **ClassLoader pattern** analogous to Java's class loading mechanism:

- **Load once at startup**: Metadata loaded into permanent memory structures
- **Read many at runtime**: Ultra-fast cached access with no synchronization
- **OSGi compatibility**: WeakReference patterns for bundle lifecycle management
- **Multiple source support**: Files, URIs, classpath resources, and bundle files

## FileMetaDataLoader

### Purpose and Design

`FileMetaDataLoader` is the primary implementation of the metadata loading system, designed for file-based metadata sources including JSON, XML, and bundle files.

**Key Features**:
- **Multi-format support**: JSON and XML metadata files with automatic parser selection
- **ClassLoader integration**: OSGi-compatible class loading with fallback chains
- **Bundle file support**: Load multiple metadata files from bundle manifests
- **Source flexibility**: Local files, URIs, and classpath resources
- **Performance optimization**: In-memory caching and lazy initialization

### Basic Usage

**Simple File Loading**:
```java
// Create loader with basic configuration
FileMetaDataLoader loader = new FileMetaDataLoader("myLoader");

// Configure file sources
LocalFileMetaDataSources sources = new LocalFileMetaDataSources("/metadata");
sources.add("user-metadata.json");
sources.add("product-metadata.xml");

// Initialize and load
loader.init(sources);

// Access loaded metadata
MetaObject userMeta = loader.getMetaObjectByName("User");
MetaField emailField = userMeta.getMetaField("email");
```

**URI-based Loading**:
```java
// Load from multiple URI sources
List<URI> uriSources = Arrays.asList(
    URI.create("file:///metadata/core-types.json"),
    URI.create("classpath://com/mycompany/metadata/business-types.xml"),
    URI.create("https://api.example.com/metadata/external-types.json")
);

URIFileMetaDataSources uriSourceProvider = new URIFileMetaDataSources(uriSources);
FileMetaDataLoader loader = new FileMetaDataLoader("uriLoader");
loader.init(uriSourceProvider);
```

### Advanced Configuration

**Custom FileLoaderOptions**:
```java
FileLoaderOptions options = new FileLoaderOptions()
    .setVerbose(true)
    .setCacheEnabled(true)
    .setValidationEnabled(true);

FileMetaDataLoader loader = new FileMetaDataLoader(options, "advancedLoader");
```

**OSGi ClassLoader Integration**:
```java
// Provide bundle-specific classloader for OSGi environments
ClassLoader bundleClassLoader = MyBundle.class.getClassLoader();
FileMetaDataLoader loader = new FileMetaDataLoader("osgiLoader");
loader.setMetaDataClassLoader(bundleClassLoader);

// Sources will use the bundle classloader for resource discovery
LocalFileMetaDataSources sources = new LocalFileMetaDataSources(metadataFiles);
sources.setLoaderClassLoader(bundleClassLoader);
loader.init(sources);
```

## File Source System

### FileMetaDataSources Hierarchy

The source system provides flexible metadata discovery with multiple implementations:

#### LocalFileMetaDataSources

**Purpose**: Load metadata from local filesystem with optional base directory.

```java
// Load from specific directory
LocalFileMetaDataSources localSources = new LocalFileMetaDataSources(
    "/project/metadata",  // Base directory
    Arrays.asList("core.json", "extensions.xml", "business-rules.json")
);

// Load from current directory
LocalFileMetaDataSources currentDirSources = new LocalFileMetaDataSources(
    Arrays.asList("metadata.json", "types.xml")
);
```

**Features**:
- **Base directory support**: All relative paths resolved against base directory
- **File existence validation**: Immediate failure for missing files
- **Recursive bundle loading**: Support for bundle files that reference other files

#### URIFileMetaDataSources

**Purpose**: Load metadata from URI sources including classpath resources, HTTP endpoints, and file URLs.

```java
List<URI> sources = Arrays.asList(
    URI.create("classpath://metadata/core-types.json"),
    URI.create("file:///opt/metadata/business-types.xml"),
    URI.create("https://config.example.com/metadata/external-types.json")
);

URIFileMetaDataSources uriSources = new URIFileMetaDataSources(sources);
```

**URI Schemes Supported**:
- `file://` - Local filesystem access
- `classpath://` - Classpath resource loading
- `http://` / `https://` - Remote metadata loading
- `jar://` - JAR file resource access

### Bundle File System

**Bundle files** (`.bundle` extension) contain lists of other metadata files to load, enabling modular metadata organization.

**Bundle File Format**:
```
# Core business types bundle
# Lines starting with # are comments

core-fields.json
business-objects.xml
validation-rules.json

# Load another bundle
extended-types.bundle
```

**Bundle Loading Usage**:
```java
LocalFileMetaDataSources sources = new LocalFileMetaDataSources("metadata");
sources.add("complete-system.bundle");  // Loads all files referenced in bundle
loader.init(sources);
```

**Bundle Processing Features**:
- **Recursive loading**: Bundles can reference other bundles
- **Comment support**: Lines starting with `#` are ignored
- **Relative path resolution**: File paths resolved relative to bundle location
- **Error isolation**: Bundle loading failures don't affect other sources

## ClassLoader Integration

### OSGi Compatibility

The loader system is designed for complex ClassLoader scenarios including OSGi bundles:

**ClassLoader Chain Resolution**:
```java
// FileMetaDataSources uses sophisticated ClassLoader chain
URL resource = getResourceViaClassLoaderChain(filename);

// Resolution order:
// 1. Source class ClassLoader (OSGi bundle ClassLoader)
// 2. Configured loader ClassLoader (from setMetaDataClassLoader)
// 3. System ClassLoader (fallback)
```

**Bundle Lifecycle Management**:
```java
public class OSGIMetaDataBundle implements BundleActivator {

    private FileMetaDataLoader loader;

    @Override
    public void start(BundleContext context) throws Exception {
        // Create loader with bundle ClassLoader
        loader = new FileMetaDataLoader("bundleLoader");
        loader.setMetaDataClassLoader(getClass().getClassLoader());

        // Load bundle-specific metadata
        LocalFileMetaDataSources sources = new LocalFileMetaDataSources(
            Arrays.asList("bundle-metadata.json")
        );
        sources.setLoaderClassLoader(getClass().getClassLoader());
        loader.init(sources);

        // Register as OSGi service
        context.registerService(MetaDataLoader.class, loader, null);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        // Loader cleanup handled by WeakReference patterns
        // No explicit cleanup needed
        loader = null;
    }
}
```

### Class Loading for Metadata Types

The loader system supports loading Java classes referenced in metadata:

```java
// loadClass() uses the ClassLoader chain for type resolution
Class<?> customFieldClass = loader.loadClass("com.mycompany.CustomField");

// Useful for dynamic type loading in metadata definitions
{
  "field": {
    "name": "customField",
    "type": "custom",
    "implementationClass": "com.mycompany.CustomField"
  }
}
```

## Parser Integration

### Automatic Parser Selection

The loader automatically selects the appropriate parser based on file extensions:

```java
// FileMetaDataLoader.loadSourceFiles() - automatic parser selection
if (filename.endsWith(".json")) {
    JsonMetaDataParser parser = new JsonMetaDataParser(this, filename);
    parser.loadFromStream(new ByteArrayInputStream(data.getBytes()));
} else if (filename.endsWith(".xml")) {
    XMLMetaDataParser parser = new XMLMetaDataParser(this, filename);
    parser.loadFromStream(new ByteArrayInputStream(data.getBytes()));
}
```

**Supported File Types**:
- `.json` - JSON metadata files with inline attribute support
- `.xml` - XML metadata files with schema validation
- `.bundle` - Bundle manifest files

### Parser Configuration

**JSON Parser Integration**:
```java
// JsonMetaDataParser automatically handles:
// - Inline attribute syntax (@-prefixed)
// - Cross-file reference resolution
// - Type-aware attribute casting
// - Package overlay patterns

// Example metadata with inline attributes
{
  "metadata": {
    "package": "com_example_model",
    "children": [
      {
        "field": {
          "name": "email",
          "type": "string",
          "@required": true,           // Boolean attribute
          "@maxLength": 255,           // Integer attribute
          "@pattern": "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
        }
      }
    ]
  }
}
```

**XML Parser Integration**:
```java
// XMLMetaDataParser automatically handles:
// - XSD schema validation
// - Namespace-aware parsing
// - Inline attribute syntax (no prefix required)
// - Complex type hierarchies

<!-- Example XML with inline attributes -->
<metadata package="com_example_model">
  <children>
    <field name="email" type="string" required="true" maxLength="255"
           pattern="^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$" />
  </children>
</metadata>
```

## Performance Characteristics

### Memory Management

The loader system implements the READ-OPTIMIZED architecture:

**Loading Phase (Startup)**:
- **Duration**: 100ms - 1s for typical metadata sets
- **Memory Usage**: 10-50MB for metadata definitions
- **CPU**: Intensive parsing and validation during init()
- **I/O**: Sequential file reading with buffering

**Runtime Phase (Application Lifetime)**:
- **Access Time**: 1-10μs for cached metadata lookups
- **Memory Model**: Permanent residence, no garbage collection
- **CPU**: Zero parsing overhead, direct object access
- **Concurrency**: Unlimited concurrent readers with no contention

### Caching Strategy

**Dual Cache Architecture**:
```java
// FileMetaDataLoader maintains efficient caching
private final Map<String, MetaObject> objectCache = new ConcurrentHashMap<>();
private final Map<String, MetaField> fieldCache = new ConcurrentHashMap<>();

// Computed value cache (WeakHashMap for OSGi compatibility)
private final Map<Object, Object> computedCache =
    Collections.synchronizedMap(new WeakHashMap<>());
```

**Cache Benefits**:
- **Permanent Cache**: Core metadata lookups never require recomputation
- **Computed Cache**: Derived values cached but can be GC'd under memory pressure
- **OSGi Safety**: WeakHashMap allows bundle unloading without memory leaks
- **Thread Safety**: ConcurrentHashMap provides lock-free concurrent access

### File I/O Optimization

**Efficient File Loading**:
```java
// All files loaded into memory during initialization
List<SourceData> loadedFiles = sources.getSourceData();

// SourceData structure optimizes memory usage
public static class SourceData {
    public final String filename;
    public final Class<? extends FileMetaDataSources> sourceClass;
    public final String sourceData;  // Complete file content in memory
}
```

**I/O Strategy Benefits**:
- **Sequential Access**: All files read sequentially during startup
- **Memory Buffering**: Complete file contents loaded into memory
- **No Runtime I/O**: Zero file system access after initialization
- **Error Isolation**: File loading errors detected immediately during init()

## Error Handling and Diagnostics

### Comprehensive Error Context

**MetaDataException Integration**:
```java
try {
    loader.init(sources);
} catch (MetaDataException e) {
    // Rich error context provided
    System.err.println("Loading failed: " + e.getMessage());
    System.err.println("Source: " + e.getSource());

    if (e.getMetaDataPath().isPresent()) {
        System.err.println("Path: " + e.getMetaDataPath().get());
    }

    if (e.getCause() instanceof IOException) {
        System.err.println("File I/O issue: " + e.getCause().getMessage());
    }
}
```

**Common Error Scenarios**:

| Error Type | Cause | Resolution |
|------------|-------|------------|
| `IllegalStateException` | No sources configured | Add FileMetaDataSources before init() |
| `MetaDataException: file not found` | Missing metadata file | Verify file paths and ClassLoader setup |
| `MetaDataException: unsupported file type` | Unknown extension | Use .json, .xml, or .bundle files |
| `ClassNotFoundException` | Missing implementation class | Add required classes to ClassLoader path |
| `ConstraintViolationException` | Invalid metadata structure | Fix metadata to comply with constraints |

### Debugging and Monitoring

**Verbose Logging Configuration**:
```java
FileLoaderOptions options = new FileLoaderOptions().setVerbose(true);
FileMetaDataLoader loader = new FileMetaDataLoader(options, "debugLoader");

// Produces detailed logging:
// INFO - METADATA - (3) Source Files Loaded in FileMetaDataLoader{name=debugLoader}
// DEBUG - LOADING: user-metadata.json
// DEBUG - Retrieved from Source LocalFileMetaDataSources's ClassLoader: user-metadata.json
```

**Load Monitoring**:
```java
// Track loading performance
long startTime = System.currentTimeMillis();
loader.init(sources);
long loadTime = System.currentTimeMillis() - startTime;

System.out.println("Loaded " + loader.getMetaObjects().size() +
                   " metadata objects in " + loadTime + "ms");
```

## Integration Patterns

### Spring Framework Integration

**Spring Bean Configuration**:
```java
@Configuration
public class MetaDataConfiguration {

    @Bean
    @Primary
    public FileMetaDataLoader primaryMetaDataLoader() {
        FileMetaDataLoader loader = new FileMetaDataLoader("primary");

        // Load from classpath resources
        LocalFileMetaDataSources sources = new LocalFileMetaDataSources(
            Arrays.asList("core-metadata.json", "business-metadata.xml")
        );

        return loader.init(sources);
    }

    @Bean
    @Qualifier("external")
    public FileMetaDataLoader externalMetaDataLoader() {
        // Load from external configuration
        String metadataUrl = environment.getProperty("metadata.external.url");
        List<URI> uriSources = Arrays.asList(URI.create(metadataUrl));

        URIFileMetaDataSources sources = new URIFileMetaDataSources(uriSources);
        return new FileMetaDataLoader("external").init(sources);
    }
}
```

**Spring Boot Auto-Configuration**:
```java
@ConfigurationProperties(prefix = "metaobjects.loader")
public class MetaDataLoaderProperties {
    private List<String> sources = new ArrayList<>();
    private String sourceDirectory = "metadata";
    private boolean verbose = false;

    // Getters and setters...
}

@AutoConfiguration
@EnableConfigurationProperties(MetaDataLoaderProperties.class)
public class MetaDataLoaderAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public FileMetaDataLoader fileMetaDataLoader(MetaDataLoaderProperties properties) {
        FileLoaderOptions options = new FileLoaderOptions()
            .setVerbose(properties.isVerbose());

        FileMetaDataLoader loader = new FileMetaDataLoader(options, "autoConfigured");

        LocalFileMetaDataSources sources = new LocalFileMetaDataSources(
            properties.getSourceDirectory(),
            properties.getSources()
        );

        return loader.init(sources);
    }
}
```

### Maven Plugin Integration

**Plugin Configuration**:
```xml
<plugin>
    <groupId>com.metaobjects</groupId>
    <artifactId>metaobjects-maven-plugin</artifactId>
    <configuration>
        <loader>
            <classname>com.metaobjects.loader.file.FileMetaDataLoader</classname>
            <name>maven-build-loader</name>
            <sourceDir>${project.basedir}/src/main/resources/metadata</sourceDir>
            <sources>
                <source>core-types.json</source>
                <source>business-types.xml</source>
                <source>database-config.bundle</source>
            </sources>
        </loader>
    </configuration>
</plugin>
```

**Plugin Configuration Processing**:
```java
// FileMetaDataLoader.configure() handles Maven plugin args
@Override
public void configure(LoaderConfiguration config) {
    // Process sourceDir and sources from plugin configuration
    Map<String, String> args = config.getArguments();
    String sourceDir = args.get("sourceDir");
    List<String> sources = config.getSources();

    // Create appropriate FileMetaDataSources
    processSources(sourceDir, sources);

    // Initialize with processed sources
    super.configure(config);
}
```

### Multi-Module Projects

**Shared Metadata Loading**:
```java
// Parent module: Core metadata loader
public class CoreMetaDataModule {

    public static FileMetaDataLoader createCoreLoader() {
        FileMetaDataLoader loader = new FileMetaDataLoader("core");

        LocalFileMetaDataSources sources = new LocalFileMetaDataSources(
            Arrays.asList("base-types.json", "core-constraints.json")
        );

        return loader.init(sources);
    }
}

// Child module: Extended metadata loader
public class BusinessMetaDataModule {

    public static FileMetaDataLoader createBusinessLoader() {
        FileMetaDataLoader loader = new FileMetaDataLoader("business");

        // Load core types first
        FileMetaDataLoader coreLoader = CoreMetaDataModule.createCoreLoader();
        loader.addParentLoader(coreLoader);

        // Add business-specific metadata
        LocalFileMetaDataSources sources = new LocalFileMetaDataSources(
            Arrays.asList("business-objects.xml", "workflow-types.json")
        );

        return loader.init(sources);
    }
}
```

## Best Practices

### Loader Configuration

**Choose LocalFileMetaDataSources when**:
- Loading from development environment with local files
- Using Maven/Gradle build-time metadata processing
- Working with file-based metadata editing workflows
- Requiring immediate file system feedback during development

**Choose URIFileMetaDataSources when**:
- Loading from classpath resources in packaged applications
- Integrating with external metadata services
- Building cloud-native applications with remote configuration
- Supporting multiple metadata source types in a single loader

**Choose Bundle Files when**:
- Managing complex metadata with multiple interdependent files
- Building modular metadata systems with optional components
- Simplifying deployment with metadata manifests
- Supporting environment-specific metadata loading

### Performance Optimization

**For Large Metadata Sets**:
```java
// Use streaming initialization for very large metadata sets
FileLoaderOptions options = new FileLoaderOptions()
    .setVerbose(false)          // Reduce logging overhead
    .setCacheEnabled(true)      // Enable all caching optimizations
    .setParallelLoading(true);  // Process files in parallel if supported

FileMetaDataLoader loader = new FileMetaDataLoader(options, "optimized");
```

**For Memory-Constrained Environments**:
```java
// Minimize memory usage during loading
FileLoaderOptions options = new FileLoaderOptions()
    .setCompactMode(true)       // Use compact internal representations
    .setEagerGC(true);          // Trigger GC after loading phases

// Load only essential metadata
LocalFileMetaDataSources sources = new LocalFileMetaDataSources(
    Arrays.asList("essential-types.json")  // Avoid loading optional metadata
);
```

### Error Resilience

**Graceful Degradation**:
```java
public class ResilientMetaDataLoader {

    private FileMetaDataLoader primaryLoader;
    private FileMetaDataLoader fallbackLoader;

    public MetaDataLoader createResilientLoader() {
        try {
            // Attempt primary loading
            primaryLoader = new FileMetaDataLoader("primary");
            primaryLoader.init(createPrimarySources());
            return primaryLoader;

        } catch (MetaDataException e) {
            log.warn("Primary metadata loading failed, using fallback", e);

            // Fall back to embedded metadata
            fallbackLoader = new FileMetaDataLoader("fallback");
            fallbackLoader.init(createFallbackSources());
            return fallbackLoader;
        }
    }

    private FileMetaDataSources createFallbackSources() {
        // Load from embedded classpath resources
        return new URIFileMetaDataSources(Arrays.asList(
            URI.create("classpath://metadata/embedded-types.json")
        ));
    }
}
```

**Validation and Recovery**:
```java
public class ValidatingMetaDataLoader {

    public FileMetaDataLoader createValidatedLoader() {
        FileMetaDataLoader loader = new FileMetaDataLoader("validated");

        try {
            loader.init(sources);

            // Validate loaded metadata
            validateMetaDataConsistency(loader);
            validateConstraintIntegrity(loader);

            return loader;

        } catch (ValidationException e) {
            // Attempt recovery or provide detailed diagnostics
            generateValidationReport(e);
            throw new MetaDataException("Metadata validation failed", e);
        }
    }
}
```

This loader system provides the foundation for all metadata access in MetaObjects applications, implementing the READ-OPTIMIZED WITH CONTROLLED MUTABILITY pattern while maintaining flexibility for diverse deployment scenarios.