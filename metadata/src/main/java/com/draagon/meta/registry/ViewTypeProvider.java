package com.draagon.meta.registry;

import com.draagon.meta.view.MetaView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * Provider for base view types in the MetaObjects framework.
 *
 * <p>This provider registers the base view type for the metadata module:</p>
 * <ul>
 *   <li><strong>view.base:</strong> Base view type with common view attributes and children</li>
 * </ul>
 *
 * <p>Note: Concrete view implementations (TextView, DateView, etc.) are registered by
 * their respective module providers (e.g., WebViewTypeProvider in the web module).</p>
 *
 * <h3>View Type Hierarchy:</h3>
 * <pre>
 * metadata.base (MetaDataLoader)
 *     └── view.base (MetaView) - common view attributes, accepts attributes
 *         ├── view.text (TextView) - registered by WebViewTypeProvider
 *         ├── view.date (DateView) - registered by WebViewTypeProvider
 *         ├── view.textarea (TextAreaView) - registered by WebViewTypeProvider
 *         ├── view.month (MonthView) - registered by WebViewTypeProvider
 *         └── view.hotlink (HotLinkView) - registered by WebViewTypeProvider
 * </pre>
 *
 * @since 6.3.0
 */
public class ViewTypeProvider implements MetaDataTypeProvider {

    private static final Logger log = LoggerFactory.getLogger(ViewTypeProvider.class);

    @Override
    public void registerTypes(MetaDataRegistry registry) throws Exception {
        log.info("Registering base view types...");

        // Force class loading to trigger static block registrations
        // MetaView has a static block that registers itself
        Class.forName(MetaView.class.getName());

        log.info("Successfully registered {} base view type", getViewTypeCount());
    }

    @Override
    public String getProviderName() {
        return "view-types";
    }

    @Override
    public Set<String> getDependencies() {
        // View types depend on core types being loaded first
        return Set.of("core-types");
    }

    @Override
    public int getPriority() {
        // High priority - fundamental types
        return 680;
    }

    @Override
    public boolean supportsOSGi() {
        return true;
    }

    @Override
    public String getDescription() {
        return "Base MetaView type (view.base) - concrete view types registered by module providers";
    }

    /**
     * Get the total number of view types registered by this provider
     */
    private int getViewTypeCount() {
        return 1; // view.base only - concrete types in other modules
    }
}