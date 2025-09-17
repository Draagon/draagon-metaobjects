package com.draagon.meta.generator;

import com.draagon.meta.MetaData;
import com.draagon.meta.MetaDataException;
import com.draagon.meta.util.ErrorFormatter;

import java.util.Map;

/**
 * Exception thrown when code generation operations fail.
 * Enhanced with structured error reporting capabilities for code generation context.
 * 
 * @since 1.0 (enhanced in 5.2.0)
 */
public class GeneratorException extends MetaDataException {

    /**
     * Creates a GeneratorException with a message.
     * Backward compatible constructor.
     * 
     * @param msg the error message
     */
    public GeneratorException(String msg) {
        super(msg);
    }

    /**
     * Creates a GeneratorException with a message and cause.
     * Backward compatible constructor.
     * 
     * @param msg the error message
     * @param e the underlying exception
     */
    public GeneratorException(String msg, Exception e) {
        super(msg, e);
    }

    /**
     * Creates a GeneratorException with enhanced context information.
     * 
     * @param message the error message
     * @param source the MetaData object providing context (e.g., MetaObject being generated)
     * @param operation the generation operation being performed when the error occurred
     */
    public GeneratorException(String message, MetaData source, String operation) {
        super(message, source, operation);
    }

    /**
     * Creates a GeneratorException with full context information.
     * 
     * @param message the error message
     * @param source the MetaData object providing context
     * @param operation the generation operation being performed
     * @param cause the underlying cause (may be null)
     * @param additionalContext additional context information (may be empty)
     */
    public GeneratorException(String message, MetaData source, String operation,
                            Throwable cause, Map<String, Object> additionalContext) {
        super(message, source, operation, cause, additionalContext);
    }

    /**
     * Factory method for creating a template processing exception with enhanced error reporting.
     * 
     * @param templateName the name of the template that failed to process
     * @param source the MetaData object providing context
     * @param cause the underlying cause
     * @return a configured GeneratorException
     */
    public static GeneratorException forTemplate(String templateName, MetaData source, Throwable cause) {
        String message = ErrorFormatter.formatGenericError(source, "templateProcessing", 
                                                          String.format("Failed to process template '%s'", templateName),
                                                          Map.of("templateName", templateName));
        return new GeneratorException(message, source, "templateProcessing", cause,
                                    Map.of("templateName", templateName, "errorType", "template"));
    }

    /**
     * Factory method for creating a code generation exception.
     * 
     * @param generatorType the type of generator that failed
     * @param targetFile the target file being generated
     * @param source the MetaData object providing context
     * @param cause the underlying cause
     * @return a configured GeneratorException
     */
    public static GeneratorException forCodeGeneration(String generatorType, String targetFile, MetaData source, Throwable cause) {
        String message = ErrorFormatter.formatGenericError(source, "codeGeneration", 
                                                          String.format("Failed to generate %s code for '%s'", generatorType, targetFile),
                                                          Map.of("generatorType", generatorType, "targetFile", targetFile));
        return new GeneratorException(message, source, "codeGeneration", cause,
                                    Map.of("generatorType", generatorType,
                                          "targetFile", targetFile,
                                          "errorType", "codeGeneration"));
    }

    /**
     * Factory method for creating a configuration exception.
     * 
     * @param configProperty the configuration property that has an issue
     * @param configValue the problematic configuration value
     * @param source the MetaData object providing context
     * @return a configured GeneratorException
     */
    public static GeneratorException forConfiguration(String configProperty, Object configValue, String issue, MetaData source) {
        String message = ErrorFormatter.formatConfigurationError(source, configProperty, configValue, issue);
        return new GeneratorException(message, source, "configuration", null,
                                    Map.of("configProperty", configProperty,
                                          "configValue", configValue != null ? configValue.toString() : "<null>",
                                          "issue", issue,
                                          "errorType", "configuration"));
    }

    /**
     * Factory method for creating a validation exception during generation.
     * 
     * @param validationErrors the validation errors that occurred
     * @param source the MetaData object providing context
     * @return a configured GeneratorException
     */
    public static GeneratorException forValidation(Map<String, String> validationErrors, MetaData source) {
        String message = ErrorFormatter.formatGenericError(source, "metadataValidation", 
                                                          "Metadata validation failed during generation",
                                                          new java.util.HashMap<String, Object>(validationErrors));
        return new GeneratorException(message, source, "metadataValidation", null,
                                    Map.of("validationErrors", validationErrors,
                                          "errorCount", validationErrors.size(),
                                          "errorType", "validation"));
    }

    /**
     * Factory method for creating a dependency resolution exception.
     * 
     * @param dependencyType the type of dependency that could not be resolved
     * @param dependencyName the name of the dependency
     * @param source the MetaData object providing context
     * @return a configured GeneratorException
     */
    public static GeneratorException forDependency(String dependencyType, String dependencyName, MetaData source) {
        String message = ErrorFormatter.formatGenericError(source, "dependencyResolution", 
                                                          String.format("Could not resolve %s dependency: %s", dependencyType, dependencyName),
                                                          Map.of("dependencyType", dependencyType, "dependencyName", dependencyName));
        return new GeneratorException(message, source, "dependencyResolution", null,
                                    Map.of("dependencyType", dependencyType,
                                          "dependencyName", dependencyName,
                                          "errorType", "dependency"));
    }

    /**
     * Factory method for creating a syntax error exception.
     * 
     * @param syntaxError the syntax error description
     * @param fileName the file where the syntax error occurred
     * @param lineNumber the line number where the error occurred
     * @param source the MetaData object providing context
     * @return a configured GeneratorException
     */
    public static GeneratorException forSyntax(String syntaxError, String fileName, int lineNumber, MetaData source) {
        String message = ErrorFormatter.formatGenericError(source, "syntaxValidation", 
                                                          String.format("Syntax error in %s at line %d: %s", fileName, lineNumber, syntaxError),
                                                          Map.of("fileName", fileName, "lineNumber", lineNumber, "syntaxError", syntaxError));
        return new GeneratorException(message, source, "syntaxValidation", null,
                                    Map.of("fileName", fileName,
                                          "lineNumber", lineNumber,
                                          "syntaxError", syntaxError,
                                          "errorType", "syntax"));
    }

    /**
     * Factory method for creating a file system exception.
     * 
     * @param fileOperation the file operation that failed
     * @param filePath the path of the file involved
     * @param source the MetaData object providing context
     * @param cause the underlying I/O cause
     * @return a configured GeneratorException
     */
    public static GeneratorException forFileSystem(String fileOperation, String filePath, MetaData source, Throwable cause) {
        String message = ErrorFormatter.formatGenericError(source, "fileSystem", 
                                                          String.format("File system operation '%s' failed for: %s", fileOperation, filePath),
                                                          Map.of("fileOperation", fileOperation, "filePath", filePath));
        return new GeneratorException(message, source, "fileSystem", cause,
                                    Map.of("fileOperation", fileOperation,
                                          "filePath", filePath,
                                          "errorType", "fileSystem"));
    }
}
