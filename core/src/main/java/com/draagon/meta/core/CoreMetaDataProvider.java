package com.draagon.meta.core;

import com.draagon.meta.io.object.xml.XMLObjectWriter;
import com.draagon.meta.io.xml.XMLMetaDataWriter;
import com.draagon.meta.registry.MetaDataRegistry;
import com.draagon.meta.registry.MetaDataTypeProvider;

/**
 * Core MetaData provider that registers type extensions for core framework I/O operations.
 *
 * <p>This provider delegates to existing I/O writer classes that contain the actual extension logic
 * and constants for core framework operations. It extends existing MetaData types with
 * attributes needed for XML I/O and serialization operations.</p>
 *
 * <h3>I/O Writers Supported:</h3>
 * <ul>
 * <li><strong>XMLObjectWriter:</strong> XML object serialization attributes</li>
 * <li><strong>XMLMetaDataWriter:</strong> XML metadata serialization attributes</li>
 * </ul>
 *
 * <h3>Priority:</h3>
 * <p>Priority 100 - Runs after basic type registration and core objects but before database services.
 * This ensures core I/O attributes are available for other service extensions.</p>
 *
 * @since 6.0.0
 */
public class CoreMetaDataProvider implements MetaDataTypeProvider {

    @Override
    public void registerTypes(MetaDataRegistry registry) {
        // Delegate to existing I/O writer classes that contain the extension logic and constants

        // Register XML object I/O type extensions
        XMLObjectWriter.registerXMLObjectAttributes(registry);

        // Register XML metadata I/O type extensions
        XMLMetaDataWriter.registerXMLMetaDataAttributes(registry);
    }

    @Override
    public int getPriority() {
        // Priority 100: After basic types, before database services (150+)
        return 100;
    }

    @Override
    public String getDescription() {
        return "Core MetaData Provider - Object creation, ValueObject, DataObject, and I/O extensions";
    }
}