package com.draagon.meta.generator.direct.metadata.file.xsd;

import com.draagon.meta.generator.GeneratorIOException;
import com.draagon.meta.generator.direct.metadata.xml.XMLDirectWriter;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.constraint.ConstraintRegistry;
import com.draagon.meta.constraint.PlacementConstraint;
import com.draagon.meta.constraint.ValidationConstraint;
import com.draagon.meta.constraint.Constraint;
import com.draagon.meta.registry.MetaDataRegistry;
import com.draagon.meta.registry.TypeDefinition;
import com.draagon.meta.registry.ChildRequirement;
import static com.draagon.meta.MetaData.*;
import com.draagon.meta.MetaDataTypeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.OutputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * v6.1.0: Registry-driven XSD Schema writer that creates schemas for validating metadata files.
 * This generates XSD Schema that validates the structure of metadata XML files using dynamic
 * type discovery from the TypeDefinition registry.
 *
 * Features:
 * - Dynamic type enumeration from MetaDataRegistry
 * - Inheritance-aware type definitions with annotations
 * - Type-specific attribute validation
 * - Automatic plugin type support
 * - No hardcoded type lists
 */
public class MetaDataFileXSDWriter extends XMLDirectWriter<MetaDataFileXSDWriter> {

    private static final Logger log = LoggerFactory.getLogger(MetaDataFileXSDWriter.class);

    private String nameSpace;
    private String targetNamespace;
    private String elementFormDefault = "qualified";

    // Registry-based type discovery
    private MetaDataRegistry typeRegistry;
    private ConstraintRegistry constraintRegistry;
    private List<PlacementConstraint> placementConstraints;
    private List<ValidationConstraint> validationConstraints;

    public MetaDataFileXSDWriter(MetaDataLoader loader, OutputStream out) throws GeneratorIOException {
        super(loader, out);
        this.typeRegistry = MetaDataRegistry.getInstance();
        this.constraintRegistry = ConstraintRegistry.getInstance();
        this.placementConstraints = new ArrayList<>();
        this.validationConstraints = new ArrayList<>();

        log.info("Initialized registry-driven XSD writer with {} registered types",
                typeRegistry.getRegisteredTypes().size());
    }

    /////////////////////////////////////////////////////////////////////////
    // Configuration Methods

    public MetaDataFileXSDWriter withNamespace(String nameSpace) {
        this.nameSpace = nameSpace;
        return this;
    }

    public MetaDataFileXSDWriter withTargetNamespace(String targetNamespace) {
        this.targetNamespace = targetNamespace;
        return this;
    }

    public MetaDataFileXSDWriter withElementFormDefault(String elementFormDefault) {
        this.elementFormDefault = elementFormDefault;
        return this;
    }

    // Registry-based generation - no constraint files needed
    @Deprecated
    public MetaDataFileXSDWriter addConstraintFile(String constraintFile) {
        log.warn("Constraint files are deprecated - using registry-based type discovery instead");
        return this;
    }

    /////////////////////////////////////////////////////////////////////////
    // Generator Methods

    @Override
    public String toString() {
        return "MetaDataFileXSDWriter{" +
                "nameSpace='" + nameSpace + '\'' +
                ", targetNamespace='" + targetNamespace + '\'' +
                ", elementFormDefault='" + elementFormDefault + '\'' +
                ", registeredTypes=" + typeRegistry.getRegisteredTypes().size() +
                '}';
    }

    ///////////////////////////////////////////////////////////////////////////
    // XSD Schema Generation Methods

    @Override
    public void writeXML() throws GeneratorIOException {
        try {
            // Load constraint definitions for schema generation
            loadConstraintDefinitions();
            
            // Generate XSD for metadata file structure
            Document xsdDoc = generateMetaDataFileXSD();
            // Copy content to the writer's document
            Document doc = doc();
            Node importedRoot = doc.importNode(xsdDoc.getDocumentElement(), true);
            doc.appendChild(importedRoot);
            
        } catch (Exception e) {
            throw new GeneratorIOException(this, "Failed to generate metadata file XSD schema", e);
        }
    }

    /**
     * Load constraint definitions and type registry data
     */
    private void loadConstraintDefinitions() {
        log.info("Loading registry data for XSD generation: {} types, {} constraints",
                typeRegistry.getRegisteredTypes().size(),
                constraintRegistry.getAllConstraints().size());

        // Get constraints from the unified registry
        this.placementConstraints = constraintRegistry.getPlacementConstraints();
        this.validationConstraints = constraintRegistry.getValidationConstraints();

        log.debug("Registry types: {}", typeRegistry.getRegisteredTypeNames());
    }

    /**
     * Generate XSD schema for validating metadata files
     */
    private Document generateMetaDataFileXSD() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();

        // Create root schema element
        Element schema = doc.createElementNS("http://www.w3.org/2001/XMLSchema", "xs:schema");
        doc.appendChild(schema);
        
        // Set schema attributes
        schema.setAttribute("xmlns:tns", targetNamespace != null ? targetNamespace : nameSpace);
        schema.setAttribute("elementFormDefault", elementFormDefault);
        schema.setAttribute("targetNamespace", targetNamespace != null ? targetNamespace : nameSpace);
        schema.setAttribute("xmlns:xs", "http://www.w3.org/2001/XMLSchema");

        // Create root metadata element using constants
        Element metadataElement = doc.createElement("xs:element");
        metadataElement.setAttribute("name", ATTR_METADATA);
        metadataElement.setAttribute("type", "MetaDataType");
        schema.appendChild(metadataElement);

        // Create core structural types
        schema.appendChild(createMetaDataType(doc));
        schema.appendChild(createMetaDataChildType(doc));

        // Create registry-driven type definitions
        generateDynamicTypeDefinitions(doc, schema);

        // Create constraint-based simple types
        schema.appendChild(createNameConstraintType(doc));

        return doc;
    }

    /**
     * Create MetaDataType complex type
     */
    private Element createMetaDataType(Document doc) {
        Element complexType = doc.createElement("xs:complexType");
        complexType.setAttribute("name", "MetaDataType");
        
        // Add annotation
        Element annotation = doc.createElement("xs:annotation");
        Element documentation = doc.createElement("xs:documentation");
        documentation.setTextContent("Root metadata container with optional package and required children");
        annotation.appendChild(documentation);
        complexType.appendChild(annotation);
        
        Element sequence = doc.createElement("xs:sequence");
        complexType.appendChild(sequence);
        
        // Children element (required) - using constants
        Element childrenElement = doc.createElement("xs:element");
        childrenElement.setAttribute("name", ATTR_CHILDREN);
        childrenElement.setAttribute("minOccurs", "1");
        childrenElement.setAttribute("maxOccurs", "1");
        sequence.appendChild(childrenElement);
        
        Element childrenComplexType = doc.createElement("xs:complexType");
        childrenElement.appendChild(childrenComplexType);
        
        Element childrenSequence = doc.createElement("xs:sequence");
        childrenComplexType.appendChild(childrenSequence);
        
        Element childElement = doc.createElement("xs:element");
        childElement.setAttribute("name", "child");
        childElement.setAttribute("type", "MetaDataChildType");
        childElement.setAttribute("minOccurs", "0");
        childElement.setAttribute("maxOccurs", "unbounded");
        childrenSequence.appendChild(childElement);
        
        // Package attribute (optional) - using constants
        Element packageAttr = doc.createElement("xs:attribute");
        packageAttr.setAttribute("name", ATTR_PACKAGE);
        packageAttr.setAttribute("type", "xs:string");
        packageAttr.setAttribute("use", "optional");
        complexType.appendChild(packageAttr);
        
        return complexType;
    }

    /**
     * Create MetaDataChildType complex type with dynamic choice elements from registry
     */
    private Element createMetaDataChildType(Document doc) {
        Element complexType = doc.createElement("xs:complexType");
        complexType.setAttribute("name", "MetaDataChildType");

        Element choice = doc.createElement("xs:choice");
        complexType.appendChild(choice);

        // Generate choice elements dynamically from all registered primary types
        Set<String> primaryTypes = typeRegistry.getAllTypeDefinitions().stream()
                .map(TypeDefinition::getType)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        for (String primaryType : primaryTypes) {
            Element element = doc.createElement("xs:element");
            element.setAttribute("name", primaryType);
            element.setAttribute("type", capitalizeFirstLetter(primaryType) + "Type");
            choice.appendChild(element);
        }

        log.debug("Generated dynamic child choice for types: {}", primaryTypes);
        return complexType;
    }

    /**
     * Generate dynamic type definitions from registry
     */
    private void generateDynamicTypeDefinitions(Document doc, Element schema) {
        // Group type definitions by primary type
        Map<String, List<TypeDefinition>> typeGroups = typeRegistry.getAllTypeDefinitions().stream()
                .collect(Collectors.groupingBy(TypeDefinition::getType));

        for (Map.Entry<String, List<TypeDefinition>> entry : typeGroups.entrySet()) {
            String primaryType = entry.getKey();
            List<TypeDefinition> typeDefs = entry.getValue();

            // Create XSD complex type for this primary type
            Element complexType = createPrimaryTypeComplexType(doc, primaryType, typeDefs);
            schema.appendChild(complexType);

            // Create enum type for subtypes
            Element enumType = createSubTypeEnumType(doc, primaryType, typeDefs);
            schema.appendChild(enumType);

            log.debug("Generated XSD types for '{}' with {} subtypes",
                    primaryType, typeDefs.size());
        }
    }

    /**
     * Create XSD complex type for a primary type (object, field, etc.)
     */
    private Element createPrimaryTypeComplexType(Document doc, String primaryType, List<TypeDefinition> typeDefs) {
        Element complexType = doc.createElement("xs:complexType");
        complexType.setAttribute("name", capitalizeFirstLetter(primaryType) + "Type");

        // Add registry-based annotation with inheritance info
        Element annotation = createRegistryAnnotation(doc, primaryType, typeDefs);
        complexType.appendChild(annotation);

        // Add children sequence if this type accepts children
        if (hasChildRequirements(typeDefs)) {
            Element sequence = doc.createElement("xs:sequence");
            complexType.appendChild(sequence);

            Element childrenElement = doc.createElement("xs:element");
            childrenElement.setAttribute("name", ATTR_CHILDREN);
            childrenElement.setAttribute("minOccurs", "0");
            childrenElement.setAttribute("maxOccurs", "1");
            sequence.appendChild(childrenElement);

            Element childrenComplexType = doc.createElement("xs:complexType");
            childrenElement.appendChild(childrenComplexType);

            Element childrenSequence = doc.createElement("xs:sequence");
            childrenComplexType.appendChild(childrenSequence);

            Element childElement = doc.createElement("xs:element");
            childElement.setAttribute("name", "child");
            childElement.setAttribute("type", "MetaDataChildType");
            childElement.setAttribute("minOccurs", "0");
            childElement.setAttribute("maxOccurs", "unbounded");
            childrenSequence.appendChild(childElement);
        }

        // Name attribute (required, with constraints) - using constants
        Element nameAttr = doc.createElement("xs:attribute");
        nameAttr.setAttribute("name", ATTR_NAME);
        nameAttr.setAttribute("type", "NameConstraintType");
        nameAttr.setAttribute("use", "required");
        complexType.appendChild(nameAttr);

        // Type attribute (required) - using dynamic enum
        Element typeAttr = doc.createElement("xs:attribute");
        typeAttr.setAttribute("name", ATTR_SUBTYPE);
        typeAttr.setAttribute("type", capitalizeFirstLetter(primaryType) + "TypeEnum");
        typeAttr.setAttribute("use", "required");
        complexType.appendChild(typeAttr);

        // Add support for inline attributes
        Element anyAttribute = doc.createElement("xs:anyAttribute");
        anyAttribute.setAttribute("processContents", "lax");
        Element anyAttrAnnotation = doc.createElement("xs:annotation");
        Element anyAttrDoc = doc.createElement("xs:documentation");
        anyAttrDoc.setTextContent("Inline attributes support: Additional attributes are allowed when attr type has default subType configured.");
        anyAttrAnnotation.appendChild(anyAttrDoc);
        anyAttribute.appendChild(anyAttrAnnotation);
        complexType.appendChild(anyAttribute);

        return complexType;
    }

    /**
     * Create name constraint type using MetaDataConstants
     */
    private Element createNameConstraintType(Document doc) {
        Element simpleType = doc.createElement("xs:simpleType");
        simpleType.setAttribute("name", "NameConstraintType");

        Element restriction = doc.createElement("xs:restriction");
        restriction.setAttribute("base", "xs:string");
        simpleType.appendChild(restriction);

        // Use pattern from MetaDataConstants
        Element patternFacet = doc.createElement("xs:pattern");
        patternFacet.setAttribute("value", VALID_NAME_PATTERN);
        restriction.appendChild(patternFacet);

        // Add length facets
        Element minLengthFacet = doc.createElement("xs:minLength");
        minLengthFacet.setAttribute("value", "1");
        restriction.appendChild(minLengthFacet);

        Element maxLengthFacet = doc.createElement("xs:maxLength");
        maxLengthFacet.setAttribute("value", "64");
        restriction.appendChild(maxLengthFacet);

        return simpleType;
    }

    /**
     * Create subtype enumeration for a primary type using registry data
     */
    private Element createSubTypeEnumType(Document doc, String primaryType, List<TypeDefinition> typeDefs) {
        Element simpleType = doc.createElement("xs:simpleType");
        simpleType.setAttribute("name", capitalizeFirstLetter(primaryType) + "TypeEnum");

        Element restriction = doc.createElement("xs:restriction");
        restriction.setAttribute("base", "xs:string");
        simpleType.appendChild(restriction);

        // Generate enumeration values from actual registered subtypes
        Set<String> subTypes = typeDefs.stream()
                .map(TypeDefinition::getSubType)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        for (String subType : subTypes) {
            Element enumeration = doc.createElement("xs:enumeration");
            enumeration.setAttribute("value", subType);
            restriction.appendChild(enumeration);
        }

        log.debug("Generated enum for {}: {}", primaryType, subTypes);
        return simpleType;
    }

    /**
     * Create registry-based annotation with inheritance and type information
     */
    private Element createRegistryAnnotation(Document doc, String primaryType, List<TypeDefinition> typeDefs) {
        Element annotation = doc.createElement("xs:annotation");
        Element documentation = doc.createElement("xs:documentation");

        StringBuilder info = new StringBuilder();
        info.append(String.format("%s type with %d registered subtypes from registry. ",
                capitalizeFirstLetter(primaryType), typeDefs.size()));

        // Add inheritance information
        List<TypeDefinition> inheritedTypes = typeDefs.stream()
                .filter(def -> def.hasParent())
                .collect(Collectors.toList());

        if (!inheritedTypes.isEmpty()) {
            info.append(String.format("Inheritance: %d types inherit from base types. ",
                    inheritedTypes.size()));
        }

        // Add subtype list
        Set<String> subTypes = typeDefs.stream()
                .map(TypeDefinition::getSubType)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        info.append(String.format("Subtypes: %s", String.join(", ", subTypes)));

        documentation.setTextContent(info.toString());
        annotation.appendChild(documentation);

        return annotation;
    }

    /**
     * Helper method to capitalize first letter of a string
     */
    private String capitalizeFirstLetter(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    /**
     * Check if any type definition in the list has child requirements
     */
    private boolean hasChildRequirements(List<TypeDefinition> typeDefs) {
        return typeDefs.stream()
                .anyMatch(def -> !def.getChildRequirements().isEmpty());
    }

    /**
     * Create constraint annotation with constraint information (deprecated - replaced by registry annotation)
     */
    @Deprecated
    private Element createConstraintAnnotation(Document doc, String description) {
        return createRegistryAnnotation(doc, description, Collections.emptyList());
    }
}