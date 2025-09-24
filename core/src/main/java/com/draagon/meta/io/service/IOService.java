package com.draagon.meta.io.service;

import com.draagon.meta.registry.MetaDataRegistry;

/**
 * IO service class that extends MetaData types with input/output attributes.
 *
 * <p>This service adds attributes needed for file I/O, serialization, and data exchange to existing
 * MetaData types. All attribute names are defined as constants for type safety
 * and consistency across the codebase.</p>
 *
 * <h3>IO Operation Attributes:</h3>
 * <ul>
 * <li><strong>IO_FORMAT:</strong> Data format for serialization (JSON, XML, CSV, etc.)</li>
 * <li><strong>IO_ENCODING:</strong> Character encoding for file operations</li>
 * <li><strong>IO_COMPRESSION:</strong> Compression algorithm for data storage</li>
 * <li><strong>IO_BUFFER_SIZE:</strong> Buffer size for I/O operations</li>
 * <li><strong>IO_VALIDATION:</strong> Validation during I/O operations</li>
 * <li><strong>IO_ERROR_HANDLING:</strong> Error handling strategy</li>
 * </ul>
 *
 * @since 6.0.0
 */
public class IOService {

    // Data Format Attributes
    public static final String IO_FORMAT = "ioFormat";
    public static final String IO_CONTENT_TYPE = "ioContentType";
    public static final String IO_FILE_EXTENSION = "ioFileExtension";
    public static final String IO_MIME_TYPE = "ioMimeType";

    // Encoding and Character Set
    public static final String IO_ENCODING = "ioEncoding";
    public static final String IO_CHARACTER_SET = "ioCharacterSet";
    public static final String IO_BYTE_ORDER = "ioByteOrder";
    public static final String IO_LINE_SEPARATOR = "ioLineSeparator";

    // Compression and Optimization
    public static final String IO_COMPRESSION = "ioCompression";
    public static final String IO_COMPRESSION_LEVEL = "ioCompressionLevel";
    public static final String IO_BUFFER_SIZE = "ioBufferSize";
    public static final String IO_BATCH_SIZE = "ioBatchSize";

    // Validation and Integrity
    public static final String IO_VALIDATION = "ioValidation";
    public static final String IO_CHECKSUM = "ioChecksum";
    public static final String IO_SCHEMA_VALIDATION = "ioSchemaValidation";
    public static final String IO_DATA_INTEGRITY = "ioDataIntegrity";

    // Error Handling and Recovery
    public static final String IO_ERROR_HANDLING = "ioErrorHandling";
    public static final String IO_RETRY_COUNT = "ioRetryCount";
    public static final String IO_TIMEOUT = "ioTimeout";
    public static final String IO_FALLBACK_FORMAT = "ioFallbackFormat";

    // Serialization Control
    public static final String IO_INCLUDE_NULLS = "ioIncludeNulls";
    public static final String IO_INCLUDE_DEFAULTS = "ioIncludeDefaults";
    public static final String IO_PRETTY_PRINT = "ioPrettyPrint";
    public static final String IO_DATE_FORMAT = "ioDateFormat";

    // File and Stream Attributes
    public static final String IO_FILE_PATH = "ioFilePath";
    public static final String IO_FILE_NAME_PATTERN = "ioFileNamePattern";
    public static final String IO_DIRECTORY = "ioDirectory";
    public static final String IO_STREAM_TYPE = "ioStreamType";

    // XML-Specific Attributes
    public static final String IO_XML_ROOT_ELEMENT = "ioXmlRootElement";
    public static final String IO_XML_NAMESPACE = "ioXmlNamespace";
    public static final String IO_XML_SCHEMA_LOCATION = "ioXmlSchemaLocation";
    public static final String IO_XML_VERSION = "ioXmlVersion";

    // JSON-Specific Attributes
    public static final String IO_JSON_ARRAY_WRAPPER = "ioJsonArrayWrapper";
    public static final String IO_JSON_PROPERTY_NAMING = "ioJsonPropertyNaming";
    public static final String IO_JSON_IGNORE_UNKNOWN = "ioJsonIgnoreUnknown";
    public static final String IO_JSON_TYPE_INFO = "ioJsonTypeInfo";

    // CSV-Specific Attributes
    public static final String IO_CSV_DELIMITER = "ioCsvDelimiter";
    public static final String IO_CSV_QUOTE_CHAR = "ioCsvQuoteChar";
    public static final String IO_CSV_ESCAPE_CHAR = "ioCsvEscapeChar";
    public static final String IO_CSV_HEADER = "ioCsvHeader";

    // Performance and Caching
    public static final String IO_CACHE_ENABLED = "ioCacheEnabled";
    public static final String IO_CACHE_SIZE = "ioCacheSize";
    public static final String IO_CACHE_TTL = "ioCacheTtl";
    public static final String IO_LAZY_LOADING = "ioLazyLoading";

    /**
     * Register IO-specific type extensions with the MetaData registry.
     *
     * <p>This method extends existing MetaData types with attributes needed
     * for file I/O operations and data serialization. It follows the extension pattern of finding
     * existing types and adding optional attributes.</p>
     *
     * @param registry The MetaData registry to extend
     */
    public static void registerTypeExtensions(MetaDataRegistry registry) {
        try {
            // Extend object types for serialization control
            registerObjectExtensions(registry);

            // Extend field types for field-specific I/O attributes
            registerFieldExtensions(registry);

            // Extend attribute types for metadata I/O
            registerAttributeExtensions(registry);

        } catch (Exception e) {
            // Log error but don't fail - service provider pattern should be resilient
            System.err.println("Warning: Failed to register IO type extensions: " + e.getMessage());
        }
    }

    /**
     * Extend object types with serialization and I/O attributes.
     */
    private static void registerObjectExtensions(MetaDataRegistry registry) {
        registry.findType("object", "pojo")
            .optionalAttribute(IO_FORMAT, "string")
            .optionalAttribute(IO_CONTENT_TYPE, "string")
            .optionalAttribute(IO_ENCODING, "string")
            .optionalAttribute(IO_COMPRESSION, "string")
            .optionalAttribute(IO_VALIDATION, "string")
            .optionalAttribute(IO_ERROR_HANDLING, "string")
            .optionalAttribute(IO_INCLUDE_NULLS, "boolean")
            .optionalAttribute(IO_INCLUDE_DEFAULTS, "boolean")
            .optionalAttribute(IO_PRETTY_PRINT, "boolean")
            .optionalAttribute(IO_XML_ROOT_ELEMENT, "string")
            .optionalAttribute(IO_XML_NAMESPACE, "string")
            .optionalAttribute(IO_JSON_ARRAY_WRAPPER, "string")
            .optionalAttribute(IO_JSON_PROPERTY_NAMING, "string")
            .optionalAttribute(IO_CACHE_ENABLED, "boolean")
            .optionalAttribute(IO_LAZY_LOADING, "boolean");

        registry.findType("object", "proxy")
            .optionalAttribute(IO_FORMAT, "string")
            .optionalAttribute(IO_VALIDATION, "string")
            .optionalAttribute(IO_ERROR_HANDLING, "string")
            .optionalAttribute(IO_CACHE_ENABLED, "boolean")
            .optionalAttribute(IO_LAZY_LOADING, "boolean");

        registry.findType("object", "map")
            .optionalAttribute(IO_FORMAT, "string")
            .optionalAttribute(IO_INCLUDE_NULLS, "boolean")
            .optionalAttribute(IO_JSON_PROPERTY_NAMING, "string")
            .optionalAttribute(IO_CACHE_ENABLED, "boolean");
    }

    /**
     * Extend field types with field-specific I/O attributes.
     */
    private static void registerFieldExtensions(MetaDataRegistry registry) {
        // String fields get comprehensive I/O attributes
        registry.findType("field", "string")
            .optionalAttribute(IO_ENCODING, "string")
            .optionalAttribute(IO_MAX_LENGTH, "int")
            .optionalAttribute(IO_VALIDATION, "string")
            .optionalAttribute(IO_DATE_FORMAT, "string")
            .optionalAttribute(IO_CSV_QUOTE_CHAR, "string")
            .optionalAttribute(IO_CSV_ESCAPE_CHAR, "string")
            .optionalAttribute(IO_LAZY_LOADING, "boolean")
            .optionalAttribute(IO_INCLUDE_NULLS, "boolean");

        // Numeric fields get numeric I/O attributes
        registry.findType("field", "int")
            .optionalAttribute(IO_VALIDATION, "string")
            .optionalAttribute(IO_INCLUDE_NULLS, "boolean")
            .optionalAttribute(IO_LAZY_LOADING, "boolean");

        registry.findType("field", "long")
            .optionalAttribute(IO_VALIDATION, "string")
            .optionalAttribute(IO_INCLUDE_NULLS, "boolean")
            .optionalAttribute(IO_LAZY_LOADING, "boolean");

        registry.findType("field", "double")
            .optionalAttribute(IO_VALIDATION, "string")
            .optionalAttribute(IO_PRECISION, "int")
            .optionalAttribute(IO_INCLUDE_NULLS, "boolean")
            .optionalAttribute(IO_LAZY_LOADING, "boolean");

        // Date fields get date/time I/O attributes
        registry.findType("field", "date")
            .optionalAttribute(IO_DATE_FORMAT, "string")
            .optionalAttribute(IO_VALIDATION, "string")
            .optionalAttribute(IO_INCLUDE_NULLS, "boolean")
            .optionalAttribute(IO_LAZY_LOADING, "boolean");

        // Boolean fields get boolean I/O attributes
        registry.findType("field", "boolean")
            .optionalAttribute(IO_VALIDATION, "string")
            .optionalAttribute(IO_INCLUDE_NULLS, "boolean");

        // Array fields get array I/O attributes
        registry.findType("field", "stringarray")
            .optionalAttribute(IO_CSV_DELIMITER, "string")
            .optionalAttribute(IO_JSON_ARRAY_WRAPPER, "string")
            .optionalAttribute(IO_VALIDATION, "string")
            .optionalAttribute(IO_INCLUDE_NULLS, "boolean");
    }

    /**
     * Extend attribute types with I/O metadata support.
     */
    private static void registerAttributeExtensions(MetaDataRegistry registry) {
        registry.findType("attr", "string")
            .optionalAttribute(IO_ENCODING, "string")
            .optionalAttribute(IO_VALIDATION, "string");

        registry.findType("attr", "int")
            .optionalAttribute(IO_VALIDATION, "string");

        registry.findType("attr", "boolean")
            .optionalAttribute(IO_VALIDATION, "string");
    }

    /**
     * Check if an attribute name is I/O-related.
     *
     * @param attributeName The attribute name to check
     * @return True if the attribute is I/O-related
     */
    public static boolean isIOAttribute(String attributeName) {
        return attributeName != null && attributeName.startsWith("io");
    }

    /**
     * Get standard I/O formats.
     *
     * @return Array of standard I/O format values
     */
    public static String[] getStandardIOFormats() {
        return new String[]{"json", "xml", "csv", "yaml", "binary", "text", "properties"};
    }

    /**
     * Get standard character encodings.
     *
     * @return Array of standard encoding values
     */
    public static String[] getStandardEncodings() {
        return new String[]{"UTF-8", "UTF-16", "ISO-8859-1", "US-ASCII", "UTF-32"};
    }

    /**
     * Get standard compression algorithms.
     *
     * @return Array of standard compression values
     */
    public static String[] getStandardCompressions() {
        return new String[]{"none", "gzip", "deflate", "bzip2", "lz4", "snappy"};
    }

    // Add constant for precision attribute (referenced above but not defined)
    public static final String IO_PRECISION = "ioPrecision";
    public static final String IO_MAX_LENGTH = "ioMaxLength";
}