package com.draagon.meta.registry;

import com.draagon.meta.key.ForeignKey;
import com.draagon.meta.key.MetaKey;
import com.draagon.meta.key.PrimaryKey;
import com.draagon.meta.key.SecondaryKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * Provider for all key types in the MetaObjects framework.
 *
 * <p>This provider registers the complete key type hierarchy:</p>
 * <ul>
 *   <li><strong>key.base:</strong> Base key type with common key attributes and children</li>
 *   <li><strong>key.primary:</strong> Primary keys for unique record identification</li>
 *   <li><strong>key.foreign:</strong> Foreign keys for referencing other objects</li>
 *   <li><strong>key.secondary:</strong> Secondary keys for alternative record identification</li>
 * </ul>
 *
 * <p>All concrete key types inherit from key.base, which inherits from metadata.base,
 * providing a clean inheritance hierarchy with shared attributes and child acceptance.</p>
 *
 * <h3>Key Type Hierarchy:</h3>
 * <pre>
 * metadata.base (MetaDataLoader)
 *     └── key.base (MetaKey) - common key attributes, accepts attributes
 *         ├── key.primary (PrimaryKey) - unique record identification
 *         ├── key.foreign (ForeignKey) - references to other objects + foreign-specific attributes
 *         └── key.secondary (SecondaryKey) - alternative record identification
 * </pre>
 *
 * @since 6.3.0
 */
public class KeyTypeProvider implements MetaDataTypeProvider {

    private static final Logger log = LoggerFactory.getLogger(KeyTypeProvider.class);

    @Override
    public void registerTypes(MetaDataRegistry registry) throws Exception {
        log.info("Registering key types...");

        // Force class loading to trigger static block registrations
        // These classes have static blocks that register themselves
        Class.forName(MetaKey.class.getName());
        Class.forName(PrimaryKey.class.getName());
        Class.forName(ForeignKey.class.getName());
        Class.forName(SecondaryKey.class.getName());

        log.info("Successfully registered {} key types", getKeyTypeCount());
    }

    @Override
    public String getProviderName() {
        return "key-types";
    }

    @Override
    public Set<String> getDependencies() {
        // Key types depend on core types being loaded first
        return Set.of("core-types");
    }

    @Override
    public int getPriority() {
        // High priority - fundamental types
        return 700;
    }

    @Override
    public boolean supportsOSGi() {
        return true;
    }

    @Override
    public String getDescription() {
        return "All MetaKey types (key.base + 3 concrete key types)";
    }

    /**
     * Get the total number of key types registered by this provider
     */
    private int getKeyTypeCount() {
        return 4; // key.base + key.primary + key.foreign + key.secondary
    }
}