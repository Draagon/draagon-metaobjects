/*
 * Copyright 2003 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta.web.view;

import com.draagon.meta.*;
import com.draagon.meta.util.ErrorFormatter;

import java.util.Map;

/**
 * Exception thrown when web view operations fail.
 * Enhanced with structured error reporting capabilities for web-specific context.
 * 
 * @since 1.0 (enhanced in 5.2.0)
 */
public class WebViewException extends MetaDataException {

    /**
     * Creates a WebViewException with a message and cause.
     * Backward compatible constructor.
     * 
     * @param msg the error message
     * @param t the underlying cause
     */
    public WebViewException(String msg, Throwable t) {
        super(msg, t);
    }

    /**
     * Creates a WebViewException with a message.
     * Backward compatible constructor.
     * 
     * @param msg the error message
     */
    public WebViewException(String msg) {
        super(msg);
    }

    /**
     * Creates a WebViewException with default message.
     * Backward compatible constructor.
     */
    public WebViewException() {
        super("WebView Exception");
    }

    /**
     * Creates a WebViewException with enhanced context information.
     * 
     * @param message the error message
     * @param source the MetaData object providing context (e.g., MetaView or MetaField)
     * @param operation the web operation being performed when the error occurred
     */
    public WebViewException(String message, MetaData source, String operation) {
        super(message, source, operation);
    }

    /**
     * Creates a WebViewException with full context information.
     * 
     * @param message the error message
     * @param source the MetaData object providing context
     * @param operation the web operation being performed
     * @param cause the underlying cause (may be null)
     * @param additionalContext additional context information (may be empty)
     */
    public WebViewException(String message, MetaData source, String operation,
                          Throwable cause, Map<String, Object> additionalContext) {
        super(message, source, operation, cause, additionalContext);
    }

    /**
     * Factory method for creating a view rendering exception with enhanced error reporting.
     * 
     * @param viewName the name of the view that failed to render
     * @param source the MetaData object providing context
     * @param cause the underlying cause
     * @return a configured WebViewException
     */
    public static WebViewException forRendering(String viewName, MetaData source, Throwable cause) {
        String message = ErrorFormatter.formatGenericError(source, "viewRendering", 
                                                          String.format("Failed to render view '%s'", viewName),
                                                          Map.of("viewName", viewName));
        return new WebViewException(message, source, "viewRendering", cause,
                                   Map.of("viewName", viewName, "errorType", "rendering"));
    }

    /**
     * Factory method for creating a template processing exception.
     * 
     * @param templateName the name of the template that failed to process
     * @param source the MetaData object providing context
     * @param cause the underlying cause
     * @return a configured WebViewException
     */
    public static WebViewException forTemplate(String templateName, MetaData source, Throwable cause) {
        String message = ErrorFormatter.formatGenericError(source, "templateProcessing", 
                                                          String.format("Failed to process template '%s'", templateName),
                                                          Map.of("templateName", templateName));
        return new WebViewException(message, source, "templateProcessing", cause,
                                   Map.of("templateName", templateName, "errorType", "template"));
    }

    /**
     * Factory method for creating a form validation exception.
     * 
     * @param fieldName the name of the field with validation errors
     * @param validationErrors the validation errors that occurred
     * @param source the MetaData object providing context
     * @return a configured WebViewException
     */
    public static WebViewException forValidation(String fieldName, Map<String, String> validationErrors, MetaData source) {
        String message = ErrorFormatter.formatGenericError(source, "formValidation", 
                                                          String.format("Form validation failed for field '%s'", fieldName),
                                                          new java.util.HashMap<String, Object>(validationErrors));
        return new WebViewException(message, source, "formValidation", null,
                                   Map.of("fieldName", fieldName, 
                                         "validationErrors", validationErrors,
                                         "errorCount", validationErrors.size(),
                                         "errorType", "validation"));
    }

    /**
     * Factory method for creating a data binding exception.
     * 
     * @param fieldName the name of the field with binding errors
     * @param expectedType the expected data type
     * @param actualValue the actual value that failed to bind
     * @param source the MetaData object providing context
     * @return a configured WebViewException
     */
    public static WebViewException forDataBinding(String fieldName, String expectedType, Object actualValue, MetaData source) {
        String message = ErrorFormatter.formatGenericError(source, "dataBinding", 
                                                          String.format("Data binding failed for field '%s'", fieldName),
                                                          Map.of("expectedType", expectedType, "actualValue", actualValue));
        return new WebViewException(message, source, "dataBinding", null,
                                   Map.of("fieldName", fieldName,
                                         "expectedType", expectedType,
                                         "actualValue", actualValue != null ? actualValue.toString() : "<null>",
                                         "actualType", actualValue != null ? actualValue.getClass().getSimpleName() : "<null>",
                                         "errorType", "dataBinding"));
    }

    /**
     * Factory method for creating a request processing exception.
     * 
     * @param requestPath the request path that failed to process
     * @param httpMethod the HTTP method used
     * @param source the MetaData object providing context
     * @param cause the underlying cause
     * @return a configured WebViewException
     */
    public static WebViewException forRequestProcessing(String requestPath, String httpMethod, MetaData source, Throwable cause) {
        String message = ErrorFormatter.formatGenericError(source, "requestProcessing", 
                                                          String.format("Failed to process %s request to '%s'", httpMethod, requestPath),
                                                          Map.of("requestPath", requestPath, "httpMethod", httpMethod));
        return new WebViewException(message, source, "requestProcessing", cause,
                                   Map.of("requestPath", requestPath,
                                         "httpMethod", httpMethod,
                                         "errorType", "requestProcessing"));
    }

    /**
     * Factory method for creating a configuration exception.
     * 
     * @param configProperty the configuration property that has an issue
     * @param configValue the problematic configuration value
     * @param source the MetaData object providing context
     * @return a configured WebViewException
     */
    public static WebViewException forConfiguration(String configProperty, Object configValue, String issue, MetaData source) {
        String message = ErrorFormatter.formatConfigurationError(source, configProperty, configValue, issue);
        return new WebViewException(message, source, "configuration", null,
                                   Map.of("configProperty", configProperty,
                                         "configValue", configValue != null ? configValue.toString() : "<null>",
                                         "issue", issue,
                                         "errorType", "configuration"));
    }

    /**
     * Factory method for creating a security exception.
     * 
     * @param securityCheck the security check that failed
     * @param userContext information about the user context
     * @param source the MetaData object providing context
     * @return a configured WebViewException
     */
    public static WebViewException forSecurity(String securityCheck, Map<String, Object> userContext, MetaData source) {
        String message = ErrorFormatter.formatGenericError(source, "securityCheck", 
                                                          String.format("Security check '%s' failed", securityCheck),
                                                          userContext);
        return new WebViewException(message, source, "securityCheck", null,
                                   Map.of("securityCheck", securityCheck,
                                         "userContext", userContext,
                                         "errorType", "security"));
    }
}
