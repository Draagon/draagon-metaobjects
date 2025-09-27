package com.metaobjects.generator;

import com.metaobjects.MetaData;
import com.metaobjects.io.MetaDataIOException;
import com.metaobjects.util.ErrorFormatter;

import java.util.Map;

/**
 * Exception thrown when I/O operations fail during code generation.
 * Enhanced with structured error reporting capabilities for generation I/O context.
 * 
 * @since 1.0 (enhanced in 5.2.0)
 */
public class GeneratorIOException extends MetaDataIOException {

    private final GeneratorIOWriter writer;
    private final Map<String, Object> context;

    /**
     * Creates a GeneratorIOException with a writer and message.
     * Backward compatible constructor.
     * 
     * @param writer the generator I/O writer involved
     * @param msg the error message
     */
    public GeneratorIOException(GeneratorIOWriter writer, String msg) {
        super(writer, msg);
        this.writer = writer;
        this.context = Map.of("errorType", "generatorIO");
    }

    /**
     * Creates a GeneratorIOException with a writer, message, and cause.
     * Backward compatible constructor.
     * 
     * @param writer the generator I/O writer involved
     * @param msg the error message
     * @param e the underlying exception
     */
    public GeneratorIOException(GeneratorIOWriter writer, String msg, Exception e) {
        super(writer, msg, e);
        this.writer = writer;
        this.context = Map.of("errorType", "generatorIO", "causedBy", e.getClass().getSimpleName());
    }

    /**
     * Creates a GeneratorIOException with enhanced context information.
     * 
     * @param writer the generator I/O writer involved
     * @param message the error message
     * @param source the MetaData object providing context
     * @param operation the I/O operation being performed
     * @param additionalContext additional context information
     */
    public GeneratorIOException(GeneratorIOWriter writer, String message, MetaData source, String operation,
                              Map<String, Object> additionalContext) {
        super(writer, buildEnhancedMessage(message, source, operation, additionalContext));
        this.writer = writer;
        
        // Build combined context
        java.util.Map<String, Object> combinedContext = new java.util.HashMap<>();
        combinedContext.put("errorType", "generatorIO");
        combinedContext.put("operation", operation);
        if (source != null) {
            combinedContext.put("sourceName", source.getName());
            combinedContext.put("sourceType", source.getType());
        }
        if (additionalContext != null) {
            combinedContext.putAll(additionalContext);
        }
        this.context = java.util.Collections.unmodifiableMap(combinedContext);
    }

    /**
     * Factory method for creating a file write exception with enhanced error reporting.
     * 
     * @param writer the generator I/O writer involved
     * @param fileName the name of the file that failed to write
     * @param source the MetaData object providing context
     * @param cause the underlying I/O cause
     * @return a configured GeneratorIOException
     */
    public static GeneratorIOException forFileWrite(GeneratorIOWriter writer, String fileName, MetaData source, Exception cause) {
        String message = ErrorFormatter.formatGenericError(source, "fileWrite", 
                                                          String.format("Failed to write file: %s", fileName),
                                                          Map.of("fileName", fileName));
        return new GeneratorIOException(writer, message, source, "fileWrite",
                                      Map.of("fileName", fileName, "ioOperation", "write"));
    }

    /**
     * Factory method for creating a template read exception.
     * 
     * @param writer the generator I/O writer involved
     * @param templatePath the path of the template that failed to read
     * @param source the MetaData object providing context
     * @param cause the underlying I/O cause
     * @return a configured GeneratorIOException
     */
    public static GeneratorIOException forTemplateRead(GeneratorIOWriter writer, String templatePath, MetaData source, Exception cause) {
        String message = ErrorFormatter.formatGenericError(source, "templateRead", 
                                                          String.format("Failed to read template: %s", templatePath),
                                                          Map.of("templatePath", templatePath));
        return new GeneratorIOException(writer, message, source, "templateRead",
                                      Map.of("templatePath", templatePath, "ioOperation", "read"));
    }

    /**
     * Factory method for creating a directory creation exception.
     * 
     * @param writer the generator I/O writer involved
     * @param directoryPath the path of the directory that failed to create
     * @param source the MetaData object providing context
     * @param cause the underlying I/O cause
     * @return a configured GeneratorIOException
     */
    public static GeneratorIOException forDirectoryCreation(GeneratorIOWriter writer, String directoryPath, MetaData source, Exception cause) {
        String message = ErrorFormatter.formatGenericError(source, "directoryCreation", 
                                                          String.format("Failed to create directory: %s", directoryPath),
                                                          Map.of("directoryPath", directoryPath));
        return new GeneratorIOException(writer, message, source, "directoryCreation",
                                      Map.of("directoryPath", directoryPath, "ioOperation", "createDirectory"));
    }

    /**
     * Builds an enhanced error message with context information.
     */
    private static String buildEnhancedMessage(String message, MetaData source, String operation, Map<String, Object> context) {
        StringBuilder enhanced = new StringBuilder();
        enhanced.append(message);

        // Add enhanced context if available
        if (source != null || operation != null || (context != null && !context.isEmpty())) {
            enhanced.append("\n\n--- Generator I/O Error Details ---");

            if (source != null) {
                enhanced.append("\nMetaData: ").append(source.getName());
                enhanced.append(" (").append(source.getType()).append(")");
            }

            if (operation != null) {
                enhanced.append("\nI/O Operation: ").append(operation);
            }

            if (context != null && !context.isEmpty()) {
                enhanced.append("\nContext:");
                context.forEach((key, value) ->
                    enhanced.append("\n  ").append(key).append(": ").append(value));
            }
        }

        return enhanced.toString();
    }

    /**
     * Returns the generator I/O writer associated with this exception.
     * 
     * @return the generator I/O writer
     */
    public GeneratorIOWriter getGeneratorWriter() {
        return writer;
    }

    /**
     * Returns the context information associated with this error.
     * 
     * @return unmodifiable map of context information
     */
    public Map<String, Object> getContext() {
        return context;
    }

    /**
     * Returns a specific context value by key.
     * 
     * @param key the context key
     * @return the value, or null if not found
     */
    public Object getContextValue(String key) {
        return context.get(key);
    }

    /**
     * Checks if this exception has enhanced context information.
     * 
     * @return true if this exception has additional context beyond the basic message
     */
    public boolean hasEnhancedContext() {
        return context.size() > 1; // More than just errorType
    }
}
