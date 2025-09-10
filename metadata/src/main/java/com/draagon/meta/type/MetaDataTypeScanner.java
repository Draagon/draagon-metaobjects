package com.draagon.meta.type;

import com.draagon.meta.MetaData;
import com.draagon.meta.MetaDataException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Scanner that automatically discovers and registers MetaData types
 * annotated with @MetaDataType annotation.
 * 
 * This supports both classpath scanning and service loader patterns
 * for plugin architectures.
 */
public class MetaDataTypeScanner {
    
    private static final Logger log = LoggerFactory.getLogger(MetaDataTypeScanner.class);
    
    /**
     * Scan and register all annotated MetaData types from the classpath
     */
    public static void scanAndRegister() {
        scanAndRegister(MetaDataTypeRegistry.getInstance());
    }
    
    /**
     * Scan and register all annotated MetaData types with the given registry
     */
    public static void scanAndRegister(MetaDataTypeRegistry registry) {
        try {
            // Use service loader pattern first (more reliable than reflection scanning)
            loadFromServiceLoader(registry);
            
            // Fallback to annotation scanning if needed
            scanAnnotatedTypes(registry);
            
        } catch (Exception e) {
            log.error("Failed to scan and register MetaData types", e);
        }
    }
    
    /**
     * Load types using Java's ServiceLoader mechanism
     */
    private static void loadFromServiceLoader(MetaDataTypeRegistry registry) {
        try {
            ServiceLoader<MetaData> serviceLoader = ServiceLoader.load(MetaData.class);
            List<Class<? extends MetaData>> annotatedClasses = new ArrayList<>();
            
            for (MetaData service : serviceLoader) {
                Class<? extends MetaData> serviceClass = service.getClass();
                if (serviceClass.isAnnotationPresent(MetaDataType.class)) {
                    annotatedClasses.add(serviceClass);
                }
            }
            
            registerAnnotatedClasses(registry, annotatedClasses);
            log.debug("Loaded {} MetaData types via ServiceLoader", annotatedClasses.size());
            
        } catch (Exception e) {
            log.debug("ServiceLoader scanning failed, continuing with other methods", e);
        }
    }
    
    /**
     * Scan for annotated types using resource discovery
     */
    private static void scanAnnotatedTypes(MetaDataTypeRegistry registry) {
        try {
            // Look for META-INF/metadata-types files that list annotated classes
            Enumeration<URL> resources = MetaDataTypeScanner.class.getClassLoader()
                .getResources("META-INF/metadata-types");
            
            List<Class<? extends MetaData>> annotatedClasses = new ArrayList<>();
            
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                try (InputStream is = resource.openStream();
                     BufferedReader reader = new BufferedReader(
                         new InputStreamReader(is, StandardCharsets.UTF_8))) {
                    
                    List<String> classNames = reader.lines()
                        .map(String::trim)
                        .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                        .collect(Collectors.toList());
                    
                    for (String className : classNames) {
                        try {
                            Class<?> clazz = Class.forName(className);
                            if (MetaData.class.isAssignableFrom(clazz) && 
                                clazz.isAnnotationPresent(MetaDataType.class)) {
                                annotatedClasses.add((Class<? extends MetaData>) clazz);
                            }
                        } catch (ClassNotFoundException e) {
                            log.warn("MetaData type class not found: {}", className);
                        }
                    }
                }
            }
            
            registerAnnotatedClasses(registry, annotatedClasses);
            log.debug("Loaded {} MetaData types via resource scanning", annotatedClasses.size());
            
        } catch (IOException e) {
            log.debug("Resource scanning failed", e);
        }
    }
    
    /**
     * Register a list of annotated classes with the registry
     */
    @SuppressWarnings("unchecked")
    private static void registerAnnotatedClasses(MetaDataTypeRegistry registry, 
                                                List<Class<? extends MetaData>> annotatedClasses) {
        
        // Sort by priority (higher first) then by name for deterministic ordering
        annotatedClasses.sort((a, b) -> {
            MetaDataType annotationA = a.getAnnotation(MetaDataType.class);
            MetaDataType annotationB = b.getAnnotation(MetaDataType.class);
            
            int priorityCompare = Integer.compare(annotationB.priority(), annotationA.priority());
            if (priorityCompare != 0) {
                return priorityCompare;
            }
            
            return annotationA.name().compareTo(annotationB.name());
        });
        
        for (Class<? extends MetaData> clazz : annotatedClasses) {
            try {
                registerAnnotatedClass(registry, clazz);
            } catch (Exception e) {
                log.error("Failed to register MetaData type: {}", clazz.getName(), e);
            }
        }
    }
    
    /**
     * Register a single annotated class
     */
    private static void registerAnnotatedClass(MetaDataTypeRegistry registry, 
                                              Class<? extends MetaData> clazz) {
        
        MetaDataType annotation = clazz.getAnnotation(MetaDataType.class);
        if (annotation == null) {
            return;
        }
        
        // Validate the class can be instantiated
        if (Modifier.isAbstract(clazz.getModifiers()) && !annotation.isAbstract()) {
            throw new MetaDataException(
                "Class " + clazz.getName() + " is abstract but @MetaDataType.isAbstract() is false"
            );
        }
        
        // Create type definition from annotation
        MetaDataTypeDefinition definition = MetaDataTypeDefinition.builder(
            annotation.name(), clazz)
            .description(annotation.description())
            .allowedSubTypes(Set.of(annotation.allowedSubTypes()))
            .allowsChildren(annotation.allowsChildren())
            .isAbstract(annotation.isAbstract())
            .build();
        
        registry.registerType(definition);
        log.debug("Registered annotated MetaData type: {} -> {}", 
                 annotation.name(), clazz.getSimpleName());
    }
    
    /**
     * Validate that required constructors exist for a MetaData class
     */
    private static void validateConstructors(Class<? extends MetaData> clazz) {
        try {
            // Check for the primary constructor (type, subType, name)
            clazz.getConstructor(String.class, String.class, String.class);
        } catch (NoSuchMethodException e) {
            try {
                // Check for legacy constructor (subType, name)
                clazz.getConstructor(String.class, String.class);
            } catch (NoSuchMethodException e2) {
                try {
                    // Check for minimal constructor (name)
                    clazz.getConstructor(String.class);
                } catch (NoSuchMethodException e3) {
                    throw new MetaDataException(
                        "MetaData class " + clazz.getName() + 
                        " must have at least one of these constructors: " +
                        "(String type, String subType, String name), " +
                        "(String subType, String name), or (String name)"
                    );
                }
            }
        }
    }
}