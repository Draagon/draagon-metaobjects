package com.metaobjects.manager.db.validator;

import com.metaobjects.MetaData;
import com.metaobjects.MetaDataException;
import com.metaobjects.util.ErrorFormatter;

import java.util.Map;

/**
 * Exception thrown when a required database table does not exist.
 * Enhanced with structured error reporting capabilities.
 * 
 * @since 1.0 (enhanced in 5.2.0)
 */
@SuppressWarnings("serial")
public class TableDoesNotExistException extends MetaDataException {

    /**
     * Creates a TableDoesNotExistException with a message and cause.
     * Backward compatible constructor.
     * 
     * @param msg the error message
     * @param e the underlying exception
     */
    public TableDoesNotExistException(String msg, Exception e) {
        super(msg, e);
    }

    /**
     * Creates a TableDoesNotExistException with a message.
     * Backward compatible constructor.
     * 
     * @param msg the error message
     */
    public TableDoesNotExistException(String msg) {
        super(msg);
    }

    /**
     * Creates a TableDoesNotExistException with enhanced context information.
     * 
     * @param message the error message
     * @param source the MetaData object providing context (e.g., MetaObject for the table)
     * @param operation the database operation being performed when the error occurred
     */
    public TableDoesNotExistException(String message, MetaData source, String operation) {
        super(message, source, operation);
    }

    /**
     * Creates a TableDoesNotExistException with full context information.
     * 
     * @param message the error message
     * @param source the MetaData object providing context
     * @param operation the database operation being performed
     * @param cause the underlying cause (may be null)
     * @param additionalContext additional context information (may be empty)
     */
    public TableDoesNotExistException(String message, MetaData source, String operation,
                                    Throwable cause, Map<String, Object> additionalContext) {
        super(message, source, operation, cause, additionalContext);
    }

    /**
     * Factory method for creating a table not found exception with enhanced error reporting.
     * 
     * @param tableName the name of the table that does not exist
     * @param source the MetaData object providing context (e.g., MetaObject)
     * @param operation the database operation being performed
     * @return a configured TableDoesNotExistException
     */
    public static TableDoesNotExistException create(String tableName, MetaData source, String operation) {
        String message = ErrorFormatter.formatGenericError(source, operation, 
                                                          String.format("Table '%s' does not exist", tableName),
                                                          Map.of("tableName", tableName));
        return new TableDoesNotExistException(message, source, operation, null,
                                            Map.of("tableName", tableName, "errorType", "missingTable"));
    }

    /**
     * Factory method for creating a table not found exception with database context.
     * 
     * @param tableName the name of the table that does not exist
     * @param source the MetaData object providing context
     * @param operation the database operation being performed
     * @param databaseInfo information about the database connection
     * @return a configured TableDoesNotExistException
     */
    public static TableDoesNotExistException create(String tableName, MetaData source, String operation, 
                                                  Map<String, Object> databaseInfo) {
        String message = ErrorFormatter.formatGenericError(source, operation, 
                                                          String.format("Table '%s' does not exist in database", tableName),
                                                          databaseInfo);
        
        Map<String, Object> context = Map.of(
            "tableName", tableName,
            "errorType", "missingTable",
            "databaseUrl", databaseInfo.getOrDefault("url", "unknown"),
            "databaseType", databaseInfo.getOrDefault("type", "unknown")
        );
        
        return new TableDoesNotExistException(message, source, operation, null, context);
    }

    /**
     * Factory method for creating a table validation exception with SQL cause.
     * 
     * @param tableName the name of the table that does not exist
     * @param source the MetaData object providing context
     * @param sqlCause the SQL exception that revealed the missing table
     * @return a configured TableDoesNotExistException
     */
    public static TableDoesNotExistException forSQLError(String tableName, MetaData source, Exception sqlCause) {
        String message = ErrorFormatter.formatGenericError(source, "tableValidation", 
                                                          String.format("SQL error indicates table '%s' does not exist", tableName),
                                                          Map.of("sqlError", sqlCause.getMessage()));
        
        return new TableDoesNotExistException(message, source, "tableValidation", sqlCause,
                                            Map.of("tableName", tableName, 
                                                  "errorType", "sqlError",
                                                  "sqlState", sqlCause.getClass().getSimpleName()));
    }

    /**
     * Factory method for creating a table schema validation exception.
     * 
     * @param tableName the name of the table that does not exist
     * @param schemaName the schema where the table was expected
     * @param source the MetaData object providing context
     * @return a configured TableDoesNotExistException
     */
    public static TableDoesNotExistException forSchema(String tableName, String schemaName, MetaData source) {
        String message = ErrorFormatter.formatGenericError(source, "schemaValidation", 
                                                          String.format("Table '%s' does not exist in schema '%s'", tableName, schemaName),
                                                          Map.of("tableName", tableName, "schemaName", schemaName));
        
        return new TableDoesNotExistException(message, source, "schemaValidation", null,
                                            Map.of("tableName", tableName, 
                                                  "schemaName", schemaName,
                                                  "errorType", "schemaValidation"));
    }
}
