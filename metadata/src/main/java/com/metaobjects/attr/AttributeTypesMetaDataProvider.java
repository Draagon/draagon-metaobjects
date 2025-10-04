package com.metaobjects.attr;

import com.metaobjects.registry.MetaDataRegistry;
import com.metaobjects.registry.MetaDataTypeProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Attribute Types MetaData provider that registers all concrete attribute type implementations.
 *
 * <p>This provider registers all the concrete attribute types that extend attr.base.
 * It calls the registerTypes() methods on each concrete attribute class to ensure proper registration.</p>
 *
 * <strong>Attribute Types Registered:</strong>
 * <ul>
 * <li><strong>attr.string:</strong> String attributes (supports @isArray for string arrays)</li>
 * <li><strong>attr.int:</strong> Integer attributes (supports @isArray for integer arrays)</li>
 * <li><strong>attr.long:</strong> Long attributes</li>
 * <li><strong>attr.double:</strong> Double attributes</li>
 * <li><strong>attr.boolean:</strong> Boolean attributes (supports @isArray for boolean arrays)</li>
 * <li><strong>REMOVED attr.stringarray:</strong> Use StringAttribute with @isArray=true instead</li>
 * <li><strong>attr.class:</strong> Class attributes</li>
 * <li><strong>attr.properties:</strong> Properties attributes</li>
 * </ul>
 *
 * <strong>Array Support:</strong>
 * <p>All attribute types support the universal @isArray modifier:</p>
 * <ul>
 * <li><strong>JSON Format:</strong> Use any attribute type with @isArray=true</li>
 * <li><strong>XML Format:</strong> Use any attribute type with isArray="true"</li>
 * </ul>
 *
 * <strong>Priority:</strong>
 * <p>Priority 15 - Runs after field types (10) but before validators (20).
 * This ensures attr.base is available before concrete attribute types are registered.</p>
 *
 * @since 6.0.0
 */
public class AttributeTypesMetaDataProvider implements MetaDataTypeProvider {

    private static final Logger log = LoggerFactory.getLogger(AttributeTypesMetaDataProvider.class);

    @Override
    public void registerTypes(MetaDataRegistry registry) {
        // FIRST: Register the base attribute type that all others inherit from
        MetaAttribute.registerTypes(registry);

        // THEN: Register concrete attribute types that inherit from attr.base
        StringAttribute.registerTypes(registry);
        IntAttribute.registerTypes(registry);
        LongAttribute.registerTypes(registry);
        DoubleAttribute.registerTypes(registry);
        BooleanAttribute.registerTypes(registry);
        // StringArrayAttribute removed - use StringAttribute with @isArray instead
        ClassAttribute.registerTypes(registry);
        PropertiesAttribute.registerTypes(registry);

        log.info("Attribute types registered via provider");
    }

    @Override
    public String getProviderId() {
        return "attribute-types";
    }

    @Override
    public String[] getDependencies() {
        // Depends on core base types to ensure metadata.base is available for attr.base inheritance
        return new String[]{"core-base-types"};
    }

    @Override
    public String getDescription() {
        return "Attribute Types MetaData Provider - Registers all concrete attribute type implementations";
    }
}