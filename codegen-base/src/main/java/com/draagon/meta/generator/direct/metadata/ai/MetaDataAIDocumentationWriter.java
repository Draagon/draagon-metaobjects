package com.draagon.meta.generator.direct.metadata.ai;

import com.draagon.meta.generator.GeneratorIOException;
import com.draagon.meta.generator.direct.metadata.json.JsonDirectWriter;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.registry.MetaDataRegistry;
import com.draagon.meta.registry.TypeDefinition;
import com.draagon.meta.registry.ChildRequirement;
import com.draagon.meta.util.MetaDataConstants;
import com.draagon.meta.MetaDataTypeId;
import com.google.gson.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * v6.1.0: AI-optimized documentation generator for MetaData type system.
 *
 * Generates comprehensive documentation designed for AI consumption with:
 * - Clear inheritance hierarchy visualization
 * - Attribute classification (inherited vs type-specific)
 * - Extension point identification for plugin development
 * - Complete type mapping and implementation guidance
 * - Cross-language compatibility information
 *
 * Output format optimized for:
 * - AI-assisted metadata development
 * - Plugin development guidance
 * - Cross-language implementation (C#, TypeScript)
 * - Enterprise extension scenarios
 */
public class MetaDataAIDocumentationWriter extends JsonDirectWriter<MetaDataAIDocumentationWriter> {

    private static final Logger log = LoggerFactory.getLogger(MetaDataAIDocumentationWriter.class);

    private String version = "6.1.0";
    private boolean includeInheritance = true;
    private boolean includeImplementationDetails = true;
    private boolean includeExtensionGuidance = true;
    private boolean includeCrossLanguageInfo = false;

    // Registry for type discovery
    private MetaDataRegistry typeRegistry;

    public MetaDataAIDocumentationWriter(MetaDataLoader loader, OutputStream out) throws GeneratorIOException {
        super(loader, out);
        this.typeRegistry = MetaDataRegistry.getInstance();

        log.info("Initialized AI documentation writer with {} registered types for comprehensive analysis",
                typeRegistry.getRegisteredTypes().size());
    }

    /////////////////////////////////////////////////////////////////////////
    // Configuration Methods

    public MetaDataAIDocumentationWriter withVersion(String version) {
        this.version = version;
        return this;
    }

    public MetaDataAIDocumentationWriter withInheritance(boolean includeInheritance) {
        this.includeInheritance = includeInheritance;
        return this;
    }

    public MetaDataAIDocumentationWriter withImplementationDetails(boolean includeImplementationDetails) {
        this.includeImplementationDetails = includeImplementationDetails;
        return this;
    }

    public MetaDataAIDocumentationWriter withExtensionGuidance(boolean includeExtensionGuidance) {
        this.includeExtensionGuidance = includeExtensionGuidance;
        return this;
    }

    public MetaDataAIDocumentationWriter withCrossLanguageInfo(boolean includeCrossLanguageInfo) {
        this.includeCrossLanguageInfo = includeCrossLanguageInfo;
        return this;
    }

    @Override
    public String toString() {
        return "MetaDataAIDocumentationWriter{" +
                "version='" + version + '\'' +
                ", registeredTypes=" + typeRegistry.getRegisteredTypes().size() +
                ", includeInheritance=" + includeInheritance +
                ", includeImplementation=" + includeImplementationDetails +
                '}';
    }

    ///////////////////////////////////////////////////////////////////////////
    // AI Documentation Generation Methods

    @Override
    public void writeJson() throws GeneratorIOException {
        try {
            log.info("Generating AI-optimized documentation for {} types with inheritance analysis",
                    typeRegistry.getRegisteredTypes().size());

            // Generate comprehensive AI documentation
            JsonObject documentation = generateAIDocumentation();
            setJsonObject(documentation);

            log.info("Generated AI documentation with {} inheritance relationships and {} extension points",
                    countInheritanceRelationships(), countExtensionPoints());

        } catch (Exception e) {
            throw new GeneratorIOException(this, "Failed to generate AI documentation", e);
        }
    }

    /**
     * Generate complete AI-optimized documentation structure
     */
    private JsonObject generateAIDocumentation() {
        JsonObject doc = new JsonObject();

        // Add documentation metadata
        doc.add("documentationInfo", createDocumentationInfo());

        // Add inheritance analysis if enabled
        if (includeInheritance) {
            doc.add("inheritanceHierarchy", generateInheritanceHierarchy());
        }

        // Add comprehensive type catalog
        doc.add("typeCatalog", generateTypeCatalog());

        // Add extension guidance if enabled
        if (includeExtensionGuidance) {
            doc.add("extensionGuidance", generateExtensionGuidance());
        }

        // Add implementation mapping if enabled
        if (includeImplementationDetails) {
            doc.add("implementationMapping", generateImplementationMapping());
        }

        // Add cross-language info if enabled
        if (includeCrossLanguageInfo) {
            doc.add("crossLanguageSupport", generateCrossLanguageSupport());
        }

        return doc;
    }

    /**
     * Create documentation metadata section
     */
    private JsonObject createDocumentationInfo() {
        JsonObject info = new JsonObject();
        info.addProperty("version", version);
        info.addProperty("generatedFrom", "TypeDefinition Registry");
        info.addProperty("generationTimestamp", System.currentTimeMillis());
        info.addProperty("aiOptimized", true);
        info.addProperty("inheritanceSupport", includeInheritance);
        info.addProperty("totalTypes", typeRegistry.getRegisteredTypes().size());

        // Add registry statistics
        JsonObject stats = new JsonObject();
        Map<String, Integer> typesByPrimary = typeRegistry.getAllTypeDefinitions().stream()
                .collect(Collectors.groupingBy(TypeDefinition::getType,
                        Collectors.collectingAndThen(Collectors.counting(), Math::toIntExact)));

        for (Map.Entry<String, Integer> entry : typesByPrimary.entrySet()) {
            stats.addProperty(entry.getKey() + "Types", entry.getValue());
        }
        info.add("typeStatistics", stats);

        info.addProperty("purpose", "AI-assisted metadata development, plugin creation, and cross-language implementation");

        return info;
    }

    /**
     * Generate inheritance hierarchy visualization
     */
    private JsonObject generateInheritanceHierarchy() {
        JsonObject hierarchy = new JsonObject();

        // Group types by inheritance relationships
        Map<String, List<TypeDefinition>> inheritanceGroups = typeRegistry.getAllTypeDefinitions().stream()
                .collect(Collectors.groupingBy(def ->
                    def.hasParent() ? def.getParentQualifiedName() : "root"));

        for (Map.Entry<String, List<TypeDefinition>> entry : inheritanceGroups.entrySet()) {
            String parentKey = entry.getKey();
            List<TypeDefinition> children = entry.getValue();

            if (!"root".equals(parentKey)) {
                JsonObject parentInfo = new JsonObject();

                // Find parent definition
                TypeDefinition parentDef = typeRegistry.getTypeDefinition(
                    parentKey.split("\\.")[0],
                    parentKey.split("\\.")[1]
                );

                if (parentDef != null) {
                    parentInfo.addProperty("isAbstract", true);
                    parentInfo.addProperty("description", parentDef.getDescription());

                    // Add children information
                    JsonArray childArray = new JsonArray();
                    Set<String> childNames = children.stream()
                            .map(TypeDefinition::getQualifiedName)
                            .collect(Collectors.toSet());
                    childNames.forEach(childArray::add);
                    parentInfo.add("children", childArray);

                    // Add common attributes (inherited by all children)
                    JsonArray commonAttrs = new JsonArray();
                    if (parentDef.getChildRequirements() != null) {
                        parentDef.getChildRequirements().stream()
                                .filter(req -> MetaDataConstants.TYPE_ATTR.equals(req.getExpectedType()))
                                .map(ChildRequirement::getName)
                                .forEach(commonAttrs::add);
                    }
                    parentInfo.add("commonAttributes", commonAttrs);
                }

                hierarchy.add(parentKey, parentInfo);
            }
        }

        return hierarchy;
    }

    /**
     * Generate comprehensive type catalog with detailed information
     */
    private JsonObject generateTypeCatalog() {
        JsonObject catalog = new JsonObject();

        for (TypeDefinition typeDef : typeRegistry.getAllTypeDefinitions()) {
            JsonObject typeInfo = new JsonObject();

            // Basic type information
            typeInfo.addProperty("qualifiedName", typeDef.getQualifiedName());
            typeInfo.addProperty("implementationClass", typeDef.getImplementationClass().getName());
            typeInfo.addProperty("description", typeDef.getDescription());

            // Inheritance information
            if (typeDef.hasParent()) {
                typeInfo.addProperty("inheritsFrom", typeDef.getParentQualifiedName());

                // Classify attributes as inherited vs specific
                JsonObject attributeClassification = new JsonObject();

                JsonArray inherited = new JsonArray();
                Map<String, ChildRequirement> inheritedReqs = typeDef.getInheritedChildRequirements();
                inheritedReqs.values().stream()
                        .filter(req -> MetaDataConstants.TYPE_ATTR.equals(req.getExpectedType()))
                        .map(ChildRequirement::getName)
                        .forEach(inherited::add);
                attributeClassification.add("inherited", inherited);

                JsonArray specific = new JsonArray();
                typeDef.getDirectChildRequirements().stream()
                        .filter(req -> MetaDataConstants.TYPE_ATTR.equals(req.getExpectedType()))
                        .map(ChildRequirement::getName)
                        .forEach(specific::add);
                attributeClassification.add("specific", specific);

                typeInfo.add("attributeClassification", attributeClassification);
            }

            // Child requirements
            JsonArray childRequirements = new JsonArray();
            for (ChildRequirement req : typeDef.getChildRequirements()) {
                JsonObject reqInfo = new JsonObject();
                reqInfo.addProperty("name", req.getName());
                reqInfo.addProperty("type", req.getExpectedType());
                reqInfo.addProperty("subType", req.getExpectedSubType());
                reqInfo.addProperty("required", req.isRequired());
                reqInfo.addProperty("description", req.getDescription());
                childRequirements.add(reqInfo);
            }
            typeInfo.add("childRequirements", childRequirements);

            // Extension information
            typeInfo.addProperty("extensible", true);
            typeInfo.addProperty("pluginSupport", true);

            catalog.add(typeDef.getQualifiedName(), typeInfo);
        }

        return catalog;
    }

    /**
     * Generate extension guidance for plugin developers
     */
    private JsonObject generateExtensionGuidance() {
        JsonObject guidance = new JsonObject();

        // Extension patterns
        JsonObject patterns = new JsonObject();
        patterns.addProperty("newFieldType", "Extend MetaField base class, inherit common attributes");
        patterns.addProperty("newObjectType", "Extend MetaObject base class, define specific behavior");
        patterns.addProperty("customAttributes", "Use attr type with custom subTypes");
        patterns.addProperty("inheritance", "Use inheritsFrom() in TypeDefinitionBuilder for attribute inheritance");

        guidance.add("extensionPatterns", patterns);

        // Common extension points
        JsonArray extensionPoints = new JsonArray();

        // Field type extensions
        JsonObject fieldExtensions = new JsonObject();
        fieldExtensions.addProperty("baseType", MetaDataConstants.TYPE_FIELD);
        fieldExtensions.addProperty("description", "Create custom field types with validation and formatting");
        fieldExtensions.addProperty("example", "CurrencyField, EmailField, PhoneField");
        JsonArray fieldAttributes = new JsonArray();
        fieldAttributes.add("precision");
        fieldAttributes.add("format");
        fieldAttributes.add("validation");
        fieldExtensions.add("commonCustomAttributes", fieldAttributes);
        extensionPoints.add(fieldExtensions);

        // Object type extensions
        JsonObject objectExtensions = new JsonObject();
        objectExtensions.addProperty("baseType", MetaDataConstants.TYPE_OBJECT);
        objectExtensions.addProperty("description", "Create domain-specific object types");
        objectExtensions.addProperty("example", "AuditableObject, VersionedObject, CacheableObject");
        JsonArray objectAttributes = new JsonArray();
        objectAttributes.add("auditing");
        objectAttributes.add("versioning");
        objectAttributes.add("caching");
        objectExtensions.add("commonCustomAttributes", objectAttributes);
        extensionPoints.add(objectExtensions);

        guidance.add("extensionPoints", extensionPoints);

        // Registration example
        JsonObject registrationExample = new JsonObject();
        registrationExample.addProperty("pattern", "MetaDataRegistry.registerType()");
        registrationExample.addProperty("description", "Use fluent TypeDefinitionBuilder API");
        registrationExample.addProperty("example",
            "MetaDataRegistry.registerType(CurrencyField.class, def -> def" +
            ".type(\"field\").subType(\"currency\")" +
            ".inheritsFromBaseField()" +
            ".optionalAttribute(\"precision\", \"int\"))");

        guidance.add("registrationExample", registrationExample);

        return guidance;
    }

    /**
     * Generate implementation mapping for Java classes
     */
    private JsonObject generateImplementationMapping() {
        JsonObject mapping = new JsonObject();

        // Class to type mapping
        JsonObject classMapping = new JsonObject();
        for (TypeDefinition typeDef : typeRegistry.getAllTypeDefinitions()) {
            classMapping.addProperty(typeDef.getImplementationClass().getSimpleName(),
                                   typeDef.getQualifiedName());
        }
        mapping.add("classToTypeMapping", classMapping);

        // Package organization
        JsonObject packageInfo = new JsonObject();
        Map<String, List<TypeDefinition>> packageGroups = typeRegistry.getAllTypeDefinitions().stream()
                .collect(Collectors.groupingBy(def -> def.getImplementationClass().getPackage().getName()));

        for (Map.Entry<String, List<TypeDefinition>> entry : packageGroups.entrySet()) {
            JsonArray types = new JsonArray();
            entry.getValue().stream()
                    .map(TypeDefinition::getQualifiedName)
                    .forEach(types::add);
            packageInfo.add(entry.getKey(), types);
        }
        mapping.add("packageOrganization", packageInfo);

        return mapping;
    }

    /**
     * Generate cross-language support information
     */
    private JsonObject generateCrossLanguageSupport() {
        JsonObject crossLang = new JsonObject();

        crossLang.addProperty("stringBasedTypes", true);
        crossLang.addProperty("description", "Type system uses string identifiers for cross-language compatibility");

        // Type mapping examples
        JsonObject typeMappings = new JsonObject();

        JsonObject javaMapping = new JsonObject();
        javaMapping.addProperty("fieldString", "StringField.class");
        javaMapping.addProperty("fieldInt", "IntegerField.class");
        javaMapping.addProperty("objectPojo", "PojoMetaObject.class");
        typeMappings.add("java", javaMapping);

        JsonObject csharpMapping = new JsonObject();
        csharpMapping.addProperty("fieldString", "StringField");
        csharpMapping.addProperty("fieldInt", "IntegerField");
        csharpMapping.addProperty("objectPojo", "PojoMetaObject");
        typeMappings.add("csharp", csharpMapping);

        JsonObject tsMapping = new JsonObject();
        tsMapping.addProperty("fieldString", "StringFieldType");
        tsMapping.addProperty("fieldInt", "IntegerFieldType");
        tsMapping.addProperty("objectPojo", "PojoMetaObjectType");
        typeMappings.add("typescript", tsMapping);

        crossLang.add("languageMappings", typeMappings);

        return crossLang;
    }

    /**
     * Count inheritance relationships for statistics
     */
    private int countInheritanceRelationships() {
        return (int) typeRegistry.getAllTypeDefinitions().stream()
                .filter(def -> def.hasParent())
                .count();
    }

    /**
     * Count extension points for statistics
     */
    private int countExtensionPoints() {
        // Count base types that can be extended
        return (int) typeRegistry.getAllTypeDefinitions().stream()
                .filter(def -> MetaDataConstants.SUBTYPE_BASE.equals(def.getSubType()))
                .count();
    }
}