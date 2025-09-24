package com.draagon.meta.web.service;

import com.draagon.meta.registry.MetaDataRegistry;

/**
 * Web service class that extends MetaData types with web framework-specific attributes.
 *
 * <p>This service adds attributes needed for web UI generation and form handling to existing
 * MetaData types. All attribute names are defined as constants for type safety
 * and consistency across the codebase.</p>
 *
 * <h3>Web Framework Attributes:</h3>
 * <ul>
 * <li><strong>WEB_LABEL:</strong> Display label for form fields</li>
 * <li><strong>WEB_PLACEHOLDER:</strong> Placeholder text for input fields</li>
 * <li><strong>WEB_VALIDATION_MESSAGE:</strong> Custom validation error message</li>
 * <li><strong>WEB_CSS_CLASS:</strong> CSS class for styling</li>
 * <li><strong>WEB_INPUT_TYPE:</strong> HTML input type (text, email, password, etc.)</li>
 * <li><strong>WEB_READONLY:</strong> Whether field is read-only in forms</li>
 * <li><strong>WEB_HIDDEN:</strong> Whether field is hidden in forms</li>
 * <li><strong>WEB_TAB_ORDER:</strong> Tab order for form navigation</li>
 * </ul>
 *
 * @since 6.0.0
 */
public class WebService {

    // Display Attributes
    public static final String WEB_LABEL = "webLabel";
    public static final String WEB_DESCRIPTION = "webDescription";
    public static final String WEB_PLACEHOLDER = "webPlaceholder";
    public static final String WEB_HELP_TEXT = "webHelpText";
    public static final String WEB_TOOLTIP = "webTooltip";

    // Form Input Attributes
    public static final String WEB_INPUT_TYPE = "webInputType";
    public static final String WEB_INPUT_SIZE = "webInputSize";
    public static final String WEB_MAX_LENGTH = "webMaxLength";
    public static final String WEB_AUTOCOMPLETE = "webAutocomplete";
    public static final String WEB_PATTERN = "webPattern";

    // Form Behavior Attributes
    public static final String WEB_REQUIRED = "webRequired";
    public static final String WEB_READONLY = "webReadonly";
    public static final String WEB_DISABLED = "webDisabled";
    public static final String WEB_HIDDEN = "webHidden";
    public static final String WEB_AUTOFOCUS = "webAutofocus";

    // Layout and Styling Attributes
    public static final String WEB_CSS_CLASS = "webCssClass";
    public static final String WEB_STYLE = "webStyle";
    public static final String WEB_WIDTH = "webWidth";
    public static final String WEB_HEIGHT = "webHeight";
    public static final String WEB_COLUMN_SPAN = "webColumnSpan";
    public static final String WEB_ROW_SPAN = "webRowSpan";

    // Navigation and Grouping Attributes
    public static final String WEB_TAB_ORDER = "webTabOrder";
    public static final String WEB_GROUP = "webGroup";
    public static final String WEB_SECTION = "webSection";
    public static final String WEB_FIELDSET = "webFieldset";

    // Validation and Error Handling
    public static final String WEB_VALIDATION_MESSAGE = "webValidationMessage";
    public static final String WEB_ERROR_CSS_CLASS = "webErrorCssClass";
    public static final String WEB_SUCCESS_MESSAGE = "webSuccessMessage";
    public static final String WEB_WARNING_MESSAGE = "webWarningMessage";

    // Data Binding Attributes
    public static final String WEB_BIND_TO = "webBindTo";
    public static final String WEB_VALUE_CONVERTER = "webValueConverter";
    public static final String WEB_FORMAT = "webFormat";
    public static final String WEB_LOCALE = "webLocale";

    // Interactive Attributes
    public static final String WEB_ONCLICK = "webOnclick";
    public static final String WEB_ONCHANGE = "webOnchange";
    public static final String WEB_ONBLUR = "webOnblur";
    public static final String WEB_ONFOCUS = "webOnfocus";

    // List and Selection Attributes
    public static final String WEB_OPTIONS_SOURCE = "webOptionsSource";
    public static final String WEB_OPTION_VALUE_FIELD = "webOptionValueField";
    public static final String WEB_OPTION_LABEL_FIELD = "webOptionLabelField";
    public static final String WEB_MULTIPLE_SELECTION = "webMultipleSelection";

    // Accessibility Attributes
    public static final String WEB_ARIA_LABEL = "webAriaLabel";
    public static final String WEB_ARIA_DESCRIBEDBY = "webAriaDescribedby";
    public static final String WEB_ROLE = "webRole";
    public static final String WEB_TABINDEX = "webTabindex";

    /**
     * Register Web-specific type extensions with the MetaData registry.
     *
     * <p>This method extends existing MetaData types with attributes needed
     * for web UI generation and form handling. It follows the extension pattern of finding
     * existing types and adding optional attributes.</p>
     *
     * @param registry The MetaData registry to extend
     */
    public static void registerTypeExtensions(MetaDataRegistry registry) {
        try {
            // Extend field types for web form generation
            registerFieldExtensions(registry);

            // Extend object types for web form/page generation
            registerObjectExtensions(registry);

            // Extend view types for web component rendering
            registerViewExtensions(registry);

        } catch (Exception e) {
            // Log error but don't fail - service provider pattern should be resilient
            System.err.println("Warning: Failed to register Web type extensions: " + e.getMessage());
        }
    }

    /**
     * Extend field types with web form attributes.
     */
    private static void registerFieldExtensions(MetaDataRegistry registry) {
        // String fields get comprehensive web input attributes
        registry.findType("field", "string")
            .optionalAttribute(WEB_LABEL, "string")
            .optionalAttribute(WEB_PLACEHOLDER, "string")
            .optionalAttribute(WEB_INPUT_TYPE, "string")
            .optionalAttribute(WEB_INPUT_SIZE, "int")
            .optionalAttribute(WEB_MAX_LENGTH, "int")
            .optionalAttribute(WEB_PATTERN, "string")
            .optionalAttribute(WEB_AUTOCOMPLETE, "string")
            .optionalAttribute(WEB_REQUIRED, "boolean")
            .optionalAttribute(WEB_READONLY, "boolean")
            .optionalAttribute(WEB_HIDDEN, "boolean")
            .optionalAttribute(WEB_CSS_CLASS, "string")
            .optionalAttribute(WEB_TAB_ORDER, "int")
            .optionalAttribute(WEB_VALIDATION_MESSAGE, "string")
            .optionalAttribute(WEB_HELP_TEXT, "string");

        // Numeric fields get numeric input attributes
        registry.findType("field", "int")
            .optionalAttribute(WEB_LABEL, "string")
            .optionalAttribute(WEB_INPUT_TYPE, "string") // number, range
            .optionalAttribute(WEB_PLACEHOLDER, "string")
            .optionalAttribute(WEB_REQUIRED, "boolean")
            .optionalAttribute(WEB_READONLY, "boolean")
            .optionalAttribute(WEB_HIDDEN, "boolean")
            .optionalAttribute(WEB_CSS_CLASS, "string")
            .optionalAttribute(WEB_TAB_ORDER, "int")
            .optionalAttribute(WEB_VALIDATION_MESSAGE, "string")
            .optionalAttribute(WEB_FORMAT, "string");

        registry.findType("field", "long")
            .optionalAttribute(WEB_LABEL, "string")
            .optionalAttribute(WEB_INPUT_TYPE, "string")
            .optionalAttribute(WEB_PLACEHOLDER, "string")
            .optionalAttribute(WEB_REQUIRED, "boolean")
            .optionalAttribute(WEB_READONLY, "boolean")
            .optionalAttribute(WEB_HIDDEN, "boolean")
            .optionalAttribute(WEB_CSS_CLASS, "string")
            .optionalAttribute(WEB_TAB_ORDER, "int")
            .optionalAttribute(WEB_VALIDATION_MESSAGE, "string")
            .optionalAttribute(WEB_FORMAT, "string");

        registry.findType("field", "double")
            .optionalAttribute(WEB_LABEL, "string")
            .optionalAttribute(WEB_INPUT_TYPE, "string")
            .optionalAttribute(WEB_PLACEHOLDER, "string")
            .optionalAttribute(WEB_REQUIRED, "boolean")
            .optionalAttribute(WEB_READONLY, "boolean")
            .optionalAttribute(WEB_HIDDEN, "boolean")
            .optionalAttribute(WEB_CSS_CLASS, "string")
            .optionalAttribute(WEB_TAB_ORDER, "int")
            .optionalAttribute(WEB_VALIDATION_MESSAGE, "string")
            .optionalAttribute(WEB_FORMAT, "string");

        // Date fields get date/time input attributes
        registry.findType("field", "date")
            .optionalAttribute(WEB_LABEL, "string")
            .optionalAttribute(WEB_INPUT_TYPE, "string") // date, datetime-local, time
            .optionalAttribute(WEB_PLACEHOLDER, "string")
            .optionalAttribute(WEB_REQUIRED, "boolean")
            .optionalAttribute(WEB_READONLY, "boolean")
            .optionalAttribute(WEB_HIDDEN, "boolean")
            .optionalAttribute(WEB_CSS_CLASS, "string")
            .optionalAttribute(WEB_TAB_ORDER, "int")
            .optionalAttribute(WEB_VALIDATION_MESSAGE, "string")
            .optionalAttribute(WEB_FORMAT, "string")
            .optionalAttribute(WEB_LOCALE, "string");

        // Boolean fields get checkbox/radio attributes
        registry.findType("field", "boolean")
            .optionalAttribute(WEB_LABEL, "string")
            .optionalAttribute(WEB_INPUT_TYPE, "string") // checkbox, radio
            .optionalAttribute(WEB_REQUIRED, "boolean")
            .optionalAttribute(WEB_READONLY, "boolean")
            .optionalAttribute(WEB_HIDDEN, "boolean")
            .optionalAttribute(WEB_CSS_CLASS, "string")
            .optionalAttribute(WEB_TAB_ORDER, "int")
            .optionalAttribute(WEB_VALIDATION_MESSAGE, "string");
    }

    /**
     * Extend object types with web form/page attributes.
     */
    private static void registerObjectExtensions(MetaDataRegistry registry) {
        registry.findType("object", "pojo")
            .optionalAttribute(WEB_LABEL, "string")
            .optionalAttribute(WEB_DESCRIPTION, "string")
            .optionalAttribute(WEB_CSS_CLASS, "string")
            .optionalAttribute(WEB_STYLE, "string")
            .optionalAttribute(WEB_GROUP, "string")
            .optionalAttribute(WEB_SECTION, "string")
            .optionalAttribute(WEB_FIELDSET, "string")
            .optionalAttribute(WEB_WIDTH, "string")
            .optionalAttribute(WEB_HEIGHT, "string")
            .optionalAttribute(WEB_COLUMN_SPAN, "int")
            .optionalAttribute(WEB_ROW_SPAN, "int");

        registry.findType("object", "proxy")
            .optionalAttribute(WEB_LABEL, "string")
            .optionalAttribute(WEB_CSS_CLASS, "string")
            .optionalAttribute(WEB_GROUP, "string");

        registry.findType("object", "map")
            .optionalAttribute(WEB_LABEL, "string")
            .optionalAttribute(WEB_CSS_CLASS, "string")
            .optionalAttribute(WEB_GROUP, "string");
    }

    /**
     * Extend view types with web rendering attributes.
     */
    private static void registerViewExtensions(MetaDataRegistry registry) {
        // Note: Views might be in a different module, so we use try-catch
        try {
            registry.findType("view", "text")
                .optionalAttribute(WEB_CSS_CLASS, "string")
                .optionalAttribute(WEB_STYLE, "string")
                .optionalAttribute(WEB_WIDTH, "string")
                .optionalAttribute(WEB_HEIGHT, "string");

            // Add more view extensions as needed
        } catch (Exception e) {
            // View types might not be available - this is OK
            System.out.println("Info: View types not available for web extensions");
        }
    }

    /**
     * Check if an attribute name is web-related.
     *
     * @param attributeName The attribute name to check
     * @return True if the attribute is web-related
     */
    public static boolean isWebAttribute(String attributeName) {
        return attributeName != null && attributeName.startsWith("web");
    }

    /**
     * Get standard HTML input types for different field types.
     *
     * @param fieldType The MetaData field type
     * @return Suggested HTML input type, or null if none applies
     */
    public static String getStandardInputType(String fieldType) {
        switch (fieldType) {
            case "string":
                return "text";
            case "int":
            case "long":
            case "double":
                return "number";
            case "date":
                return "date";
            case "boolean":
                return "checkbox";
            default:
                return "text";
        }
    }

    /**
     * Get standard web input sizes.
     *
     * @return Array of standard input size values
     */
    public static String[] getStandardInputSizes() {
        return new String[]{"small", "medium", "large", "extra-large"};
    }
}