package com.metaobjects.generator;

import com.metaobjects.generator.direct.metadata.file.xsd.MetaDataFileXSDGenerator;
import com.metaobjects.generator.direct.metadata.file.json.MetaDataFileJsonSchemaGenerator;
import com.metaobjects.generator.direct.metadata.ai.MetaDataAIDocumentationGenerator;
import com.metaobjects.registry.MetaDataRegistry;
import com.metaobjects.registry.MetaDataTypeProvider;

/**
 * Code Generation MetaData provider that registers type extensions for code generation.
 *
 * <p>This provider delegates to existing generator classes that contain the actual extension logic
 * and constants. It supports XSD generation, JSON Schema generation, and AI documentation
 * generation by extending existing MetaData types with generation-specific attributes.</p>
 *
 * <h3>Generators Supported:</h3>
 * <ul>
 * <li><strong>MetaDataFileXSDGenerator:</strong> Attributes for XSD schema generation</li>
 * <li><strong>MetaDataFileJsonSchemaGenerator:</strong> Attributes for JSON Schema generation</li>
 * <li><strong>MetaDataAIDocumentationGenerator:</strong> Attributes for AI documentation generation</li>
 * </ul>
 *
 * <h3>Priority:</h3>
 * <p>Priority 200 - Runs after database services (100-199) but before web services (300+).
 * This ensures that code generation attributes are available for web framework extensions.</p>
 *
 * @since 6.0.0
 */
public class CodeGenMetaDataProvider implements MetaDataTypeProvider {

    @Override
    public void registerTypes(MetaDataRegistry registry) {
        // Delegate to existing generator classes that contain the extension logic and constants

        // Register XSD generation type extensions
        MetaDataFileXSDGenerator.registerXSDAttributes(registry);

        // Register JSON Schema generation type extensions
        MetaDataFileJsonSchemaGenerator.registerJsonSchemaAttributes(registry);

        // Register AI Documentation generation type extensions
        MetaDataAIDocumentationGenerator.registerAIDocAttributes(registry);
    }

    @Override
    public String getProviderId() {
        return "codegen-extensions";
    }

    @Override
    public String[] getDependencies() {
        // Depends on core types since it generates from field and object metadata
        return new String[]{"field-types", "object-types", "attribute-types"};
    }

    @Override
    public String getDescription() {
        return "Code Generation MetaData Provider - XSD, JSON Schema, and AI Documentation extensions";
    }
}