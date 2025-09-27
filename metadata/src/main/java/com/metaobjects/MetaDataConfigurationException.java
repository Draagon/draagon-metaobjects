/*
 * Copyright 2003 Doug Mealing LLC dba Meta Objects. All Rights Reserved.
 *
 * This software is the proprietary information of Doug Mealing LLC dba Meta Objects.
 * Use is subject to license terms.
 */

package com.metaobjects;

import java.util.Collections;
import java.util.Map;

/**
 * Exception thrown when there are configuration-related issues with MetaData setup.
 * This includes type registry configuration, service discovery problems, 
 * invalid metadata type definitions, and other configuration issues.
 * 
 * @since 5.2.0
 */
@SuppressWarnings("serial")
public class MetaDataConfigurationException extends MetaDataException {

    private final String configurationType;
    private final String configurationLocation;

    /**
     * Creates a MetaDataConfigurationException with a simple message.
     * Backward compatible constructor.
     * 
     * @param msg the error message
     */
    public MetaDataConfigurationException(String msg) {
        super(msg);
        this.configurationType = null;
        this.configurationLocation = null;
    }

    /**
     * Creates a MetaDataConfigurationException with a message and cause.
     * 
     * @param msg the error message
     * @param cause the underlying cause
     */
    public MetaDataConfigurationException(String msg, Throwable cause) {
        super(msg, cause);
        this.configurationType = null;
        this.configurationLocation = null;
    }

    /**
     * Creates a MetaDataConfigurationException with enhanced context information.
     * 
     * @param message the error message
     * @param configurationType the type of configuration that failed (e.g., "type-registry", "service-discovery")
     * @param configurationLocation the location or source of the configuration (e.g., file path, class name)
     * @param additionalContext additional context information
     */
    public MetaDataConfigurationException(String message, String configurationType, 
                                        String configurationLocation, Map<String, Object> additionalContext) {
        super(buildEnhancedMessage(message, configurationType, configurationLocation), 
              null, "configuration", null, mergeContext(configurationType, configurationLocation, additionalContext));
        this.configurationType = configurationType;
        this.configurationLocation = configurationLocation;
    }

    /**
     * Factory method for type registry configuration errors.
     * 
     * @param message the error message
     * @param typeName the name of the type that failed to register
     * @param cause the underlying cause (may be null)
     * @return a configured MetaDataConfigurationException
     */
    public static MetaDataConfigurationException forTypeRegistry(String message, String typeName, Throwable cause) {
        Map<String, Object> context = Map.of("typeName", typeName);
        MetaDataConfigurationException ex = new MetaDataConfigurationException(
            message, "type-registry", "MetaDataTypeRegistry", context);
        if (cause != null) {
            ex.initCause(cause);
        }
        return ex;
    }

    /**
     * Factory method for service discovery configuration errors.
     * 
     * @param message the error message
     * @param serviceClass the service class that failed to be discovered
     * @param cause the underlying cause (may be null)
     * @return a configured MetaDataConfigurationException
     */
    public static MetaDataConfigurationException forServiceDiscovery(String message, Class<?> serviceClass, Throwable cause) {
        Map<String, Object> context = Map.of("serviceClass", serviceClass.getName());
        MetaDataConfigurationException ex = new MetaDataConfigurationException(
            message, "service-discovery", "ServiceLoader", context);
        if (cause != null) {
            ex.initCause(cause);
        }
        return ex;
    }

    /**
     * Factory method for metadata type definition errors.
     * 
     * @param message the error message
     * @param typeDefinition the type definition that is invalid
     * @param location the location of the invalid definition
     * @return a configured MetaDataConfigurationException
     */
    public static MetaDataConfigurationException forTypeDefinition(String message, String typeDefinition, String location) {
        Map<String, Object> context = Map.of("typeDefinition", typeDefinition);
        return new MetaDataConfigurationException(message, "type-definition", location, context);
    }

    /**
     * Factory method for loader configuration errors.
     * 
     * @param message the error message
     * @param loaderClass the loader class that failed to configure
     * @param configurationSource the source of the configuration
     * @param cause the underlying cause (may be null)
     * @return a configured MetaDataConfigurationException
     */
    public static MetaDataConfigurationException forLoaderConfiguration(String message, Class<?> loaderClass, 
                                                                       String configurationSource, Throwable cause) {
        Map<String, Object> context = Map.of("loaderClass", loaderClass.getName());
        MetaDataConfigurationException ex = new MetaDataConfigurationException(
            message, "loader-configuration", configurationSource, context);
        if (cause != null) {
            ex.initCause(cause);
        }
        return ex;
    }

    /**
     * Returns the type of configuration that failed.
     * 
     * @return the configuration type, or null if not specified
     */
    public String getConfigurationType() {
        return configurationType;
    }

    /**
     * Returns the location or source of the configuration that failed.
     * 
     * @return the configuration location, or null if not specified
     */
    public String getConfigurationLocation() {
        return configurationLocation;
    }

    /**
     * Builds an enhanced error message with configuration context.
     */
    private static String buildEnhancedMessage(String message, String configurationType, String configurationLocation) {
        if (configurationType == null && configurationLocation == null) {
            return message;
        }
        
        StringBuilder enhanced = new StringBuilder(message);
        enhanced.append(" [Configuration Error]");
        
        if (configurationType != null) {
            enhanced.append(" Type: ").append(configurationType);
        }
        
        if (configurationLocation != null) {
            enhanced.append(" Location: ").append(configurationLocation);
        }
        
        return enhanced.toString();
    }

    /**
     * Merges configuration context with additional context.
     */
    private static Map<String, Object> mergeContext(String configurationType, String configurationLocation, 
                                                   Map<String, Object> additionalContext) {
        Map<String, Object> context = new java.util.HashMap<>();
        
        if (configurationType != null) {
            context.put("configurationType", configurationType);
        }
        
        if (configurationLocation != null) {
            context.put("configurationLocation", configurationLocation);
        }
        
        if (additionalContext != null) {
            context.putAll(additionalContext);
        }
        
        return context.isEmpty() ? Collections.emptyMap() : context;
    }
}