package com.draagon.meta.web;

import com.draagon.meta.web.service.WebService;
import com.draagon.meta.registry.MetaDataRegistry;
import com.draagon.meta.registry.MetaDataTypeProvider;

/**
 * Web MetaData provider that registers type extensions for web framework services.
 *
 * <p>This provider delegates to service classes that contain the actual extension logic
 * and constants for web UI generation and form handling. It extends existing MetaData types
 * with web-specific attributes needed for HTML form generation and web component rendering.</p>
 *
 * <h3>Services Provided:</h3>
 * <ul>
 * <li><strong>WebService:</strong> Web framework attributes (labels, inputs, styling, validation)</li>
 * </ul>
 *
 * <h3>Priority:</h3>
 * <p>Priority 300 - Runs after database services (100-199) and code generation services (200-299).
 * This ensures web attributes can leverage database and schema information for better form generation.</p>
 *
 * @since 6.0.0
 */
public class WebMetaDataProvider implements MetaDataTypeProvider {

    @Override
    public void registerTypes(MetaDataRegistry registry) {
        // Delegate to service class that contains the extension logic and constants
        WebService.registerTypeExtensions(registry);
    }

    @Override
    public int getPriority() {
        // Priority 300: After database (150) and code generation (200), before business plugins (400+)
        return 300;
    }

    @Override
    public String getDescription() {
        return "Web MetaData Provider - HTML form generation and web component extensions";
    }
}