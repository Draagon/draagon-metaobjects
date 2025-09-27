package com.metaobjects.web;

import com.metaobjects.view.MetaView;
import com.metaobjects.web.view.html.TextView;
import com.metaobjects.web.view.html.DateView;
import com.metaobjects.web.view.html.TextAreaView;
import com.metaobjects.web.view.html.MonthView;
import com.metaobjects.web.view.html.HotLinkView;
import com.metaobjects.web.view.WebView;
import com.metaobjects.registry.MetaDataRegistry;
import com.metaobjects.registry.MetaDataTypeProvider;

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
 * <h3>Dependencies:</h3>
 * <p>Self-contained - registers view.base and all web view types.
 * No external dependencies needed since MetaView.registerTypes() creates the base view type.</p>
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
    public String getDescription() {
        return "Web MetaData Provider - HTML form generation and web component extensions";
    }
}