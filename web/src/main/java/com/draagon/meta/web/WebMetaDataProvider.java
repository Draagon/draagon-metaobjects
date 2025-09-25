package com.draagon.meta.web;

import com.draagon.meta.view.MetaView;
import com.draagon.meta.web.view.html.TextView;
import com.draagon.meta.web.view.html.DateView;
import com.draagon.meta.web.view.html.TextAreaView;
import com.draagon.meta.web.view.html.MonthView;
import com.draagon.meta.web.view.html.HotLinkView;
import com.draagon.meta.web.view.WebView;
import com.draagon.meta.registry.MetaDataRegistry;
import com.draagon.meta.registry.MetaDataTypeProvider;

/**
 * Web MetaData provider that registers type extensions for web view components.
 *
 * <p>This provider delegates to existing web view classes that contain the actual extension logic
 * and constants for web UI generation and form handling. It extends existing MetaData types
 * with web-specific attributes needed for HTML form generation and web component rendering.</p>
 *
 * <h3>Web Views Supported:</h3>
 * <ul>
 * <li><strong>TextView:</strong> Text input view attributes</li>
 * <li><strong>DateView:</strong> Date picker view attributes</li>
 * <li><strong>TextAreaView:</strong> Multi-line text area attributes</li>
 * <li><strong>WebView:</strong> Base web view attributes</li>
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
        // Register base view type first
        MetaView.registerTypes(registry);

        // Register web view types
        WebView.registerTypes(registry);
        TextView.registerTypes(registry);
        DateView.registerTypes(registry);
        TextAreaView.registerTypes(registry);
        MonthView.registerTypes(registry);
        HotLinkView.registerTypes(registry);
    }

    @Override
    public String getProviderId() {
        return "web-view-types";
    }

    @Override
    public String[] getDependencies() {
        // No dependencies - calls MetaView.registerTypes() to create view.base, which inherits from metadata.base
        return new String[0];
    }

    @Override
    @Deprecated
    public int getPriority() {
        // DEPRECATED: Use getDependencies() instead
        return 300;
    }

    @Override
    public String getDescription() {
        return "Web MetaData Provider - HTML form generation and web component extensions";
    }
}