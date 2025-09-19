package com.draagon.meta.generator.direct.metadata.file.xsd;

import com.draagon.meta.generator.GeneratorIOException;
import com.draagon.meta.generator.direct.metadata.xml.XMLDirectWriter;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.constraint.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.OutputStream;
import java.util.*;

/**
 * v6.0.0: XSD Schema writer that creates schemas for validating metadata files themselves.
 * This generates XSD Schema that validates the structure of metadata XML files,
 * not the data instances they describe.
 * 
 * Reads constraint definitions to understand what constitutes valid metadata structure
 * and generates appropriate validation schemas.
 */
public class MetaDataFileXSDWriter extends XMLDirectWriter<MetaDataFileXSDWriter> {

    private static final Logger log = LoggerFactory.getLogger(MetaDataFileXSDWriter.class);

    private String nameSpace;
    private String targetNamespace;
    private String elementFormDefault = "qualified";
    private List<String> constraintFiles = new ArrayList<>();
    
    // Constraint system for reading definitions
    private ConstraintDefinitionParser constraintParser;
    private Map<String, ConstraintDefinitionParser.AbstractConstraintDefinition> abstractConstraints;
    private List<ConstraintDefinitionParser.ConstraintInstance> constraintInstances;

    public MetaDataFileXSDWriter(MetaDataLoader loader, OutputStream out) throws GeneratorIOException {
        super(loader, out);
        this.constraintParser = new ConstraintDefinitionParser();
        this.abstractConstraints = new HashMap<>();
        this.constraintInstances = new ArrayList<>();
        
        // Default constraint files to load
        this.constraintFiles.add("META-INF/constraints/core-constraints.json");
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

    public MetaDataFileXSDWriter addConstraintFile(String constraintFile) {
        this.constraintFiles.add(constraintFile);
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
                ", constraintFiles=" + constraintFiles +
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
     * Load constraint definitions from configured files
     */
    private void loadConstraintDefinitions() throws ConstraintParseException {
        log.info("Loading constraint definitions for XSD schema generation from {} files", constraintFiles.size());
        
        for (String constraintFile : constraintFiles) {
            try {
                ConstraintDefinitionParser.ConstraintDefinitions definitions = 
                    constraintParser.parseFromResource(constraintFile);
                    
                // Collect abstract definitions
                for (ConstraintDefinitionParser.AbstractConstraintDefinition abstractDef : definitions.getAbstracts()) {
                    abstractConstraints.put(abstractDef.getId(), abstractDef);
                }
                
                // Collect constraint instances
                constraintInstances.addAll(definitions.getInstances());
                
                log.debug("Loaded {} abstracts and {} instances from {}", 
                    definitions.getAbstracts().size(), definitions.getInstances().size(), constraintFile);
                    
            } catch (ConstraintParseException e) {
                log.warn("Could not load constraint file [{}] for XSD generation: {}", constraintFile, e.getMessage());
                // Continue with other files - constraint files are optional for schema generation
            }
        }
        
        log.info("Loaded total {} abstract constraints and {} constraint instances for XSD", 
            abstractConstraints.size(), constraintInstances.size());
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

        // Create root metadata element
        Element metadataElement = doc.createElement("xs:element");
        metadataElement.setAttribute("name", "metadata");
        metadataElement.setAttribute("type", "MetaDataType");
        schema.appendChild(metadataElement);

        // Create MetaDataType complex type
        schema.appendChild(createMetaDataType(doc));
        
        // Create MetaDataChild complex type
        schema.appendChild(createMetaDataChildType(doc));
        
        // Create MetaObject complex type
        schema.appendChild(createMetaObjectType(doc));
        
        // Create MetaField complex type
        schema.appendChild(createMetaFieldType(doc));
        
        // Create constraint-based simple types
        schema.appendChild(createNameConstraintType(doc));
        schema.appendChild(createObjectTypeEnumType(doc));
        schema.appendChild(createFieldTypeEnumType(doc));

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
        
        // Children element (required)
        Element childrenElement = doc.createElement("xs:element");
        childrenElement.setAttribute("name", "children");
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
        
        // Package attribute (optional)
        Element packageAttr = doc.createElement("xs:attribute");
        packageAttr.setAttribute("name", "package");
        packageAttr.setAttribute("type", "xs:string");
        packageAttr.setAttribute("use", "optional");
        complexType.appendChild(packageAttr);
        
        return complexType;
    }

    /**
     * Create MetaDataChildType complex type
     */
    private Element createMetaDataChildType(Document doc) {
        Element complexType = doc.createElement("xs:complexType");
        complexType.setAttribute("name", "MetaDataChildType");
        
        Element choice = doc.createElement("xs:choice");
        complexType.appendChild(choice);
        
        // Object element
        Element objectElement = doc.createElement("xs:element");
        objectElement.setAttribute("name", "object");
        objectElement.setAttribute("type", "MetaObjectType");
        choice.appendChild(objectElement);
        
        // Field element  
        Element fieldElement = doc.createElement("xs:element");
        fieldElement.setAttribute("name", "field");
        fieldElement.setAttribute("type", "MetaFieldType");
        choice.appendChild(fieldElement);
        
        return complexType;
    }

    /**
     * Create MetaObjectType complex type
     */
    private Element createMetaObjectType(Document doc) {
        Element complexType = doc.createElement("xs:complexType");
        complexType.setAttribute("name", "MetaObjectType");
        
        // Add constraint annotation
        Element annotation = createConstraintAnnotation(doc, "MetaObject constraints");
        complexType.appendChild(annotation);
        
        Element sequence = doc.createElement("xs:sequence");
        complexType.appendChild(sequence);
        
        // Children element (optional)
        Element childrenElement = doc.createElement("xs:element");
        childrenElement.setAttribute("name", "children");
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
        
        // Name attribute (required, with constraints)
        Element nameAttr = doc.createElement("xs:attribute");
        nameAttr.setAttribute("name", "name");
        nameAttr.setAttribute("type", "NameConstraintType");
        nameAttr.setAttribute("use", "required");
        complexType.appendChild(nameAttr);
        
        // Type attribute (required)
        Element typeAttr = doc.createElement("xs:attribute");
        typeAttr.setAttribute("name", "type");
        typeAttr.setAttribute("type", "ObjectTypeEnum");
        typeAttr.setAttribute("use", "required");
        complexType.appendChild(typeAttr);
        
        // Add support for inline attributes (any additional attributes beyond reserved ones)
        Element anyAttribute = doc.createElement("xs:anyAttribute");
        anyAttribute.setAttribute("processContents", "lax");
        Element anyAttrAnnotation = doc.createElement("xs:annotation");
        Element anyAttrDoc = doc.createElement("xs:documentation");
        anyAttrDoc.setTextContent("Inline attributes support: Additional attributes are allowed when attr type has default subType configured. Values are type-cast based on content (boolean, number, string).");
        anyAttrAnnotation.appendChild(anyAttrDoc);
        anyAttribute.appendChild(anyAttrAnnotation);
        complexType.appendChild(anyAttribute);
        
        return complexType;
    }

    /**
     * Create MetaFieldType complex type
     */
    private Element createMetaFieldType(Document doc) {
        Element complexType = doc.createElement("xs:complexType");
        complexType.setAttribute("name", "MetaFieldType");
        
        // Add constraint annotation
        Element annotation = createConstraintAnnotation(doc, "MetaField constraints");
        complexType.appendChild(annotation);
        
        // Name attribute (required, with constraints)
        Element nameAttr = doc.createElement("xs:attribute");
        nameAttr.setAttribute("name", "name");
        nameAttr.setAttribute("type", "NameConstraintType");
        nameAttr.setAttribute("use", "required");
        complexType.appendChild(nameAttr);
        
        // Type attribute (required)
        Element typeAttr = doc.createElement("xs:attribute");
        typeAttr.setAttribute("name", "type");
        typeAttr.setAttribute("type", "FieldTypeEnum");
        typeAttr.setAttribute("use", "required");
        complexType.appendChild(typeAttr);
        
        // Add support for inline attributes (any additional attributes beyond reserved ones)
        Element anyAttribute = doc.createElement("xs:anyAttribute");
        anyAttribute.setAttribute("processContents", "lax");
        Element anyAttrAnnotation = doc.createElement("xs:annotation");
        Element anyAttrDoc = doc.createElement("xs:documentation");
        anyAttrDoc.setTextContent("Inline attributes support: Additional attributes are allowed when attr type has default subType configured. Values are type-cast based on content (boolean, number, string).");
        anyAttrAnnotation.appendChild(anyAttrDoc);
        anyAttribute.appendChild(anyAttrAnnotation);
        complexType.appendChild(anyAttribute);
        
        return complexType;
    }

    /**
     * Create name constraint type based on loaded constraints
     */
    private Element createNameConstraintType(Document doc) {
        Element simpleType = doc.createElement("xs:simpleType");
        simpleType.setAttribute("name", "NameConstraintType");
        
        Element restriction = doc.createElement("xs:restriction");
        restriction.setAttribute("base", "xs:string");
        simpleType.appendChild(restriction);
        
        // Apply constraints from loaded constraint definitions
        String pattern = "^[a-zA-Z][a-zA-Z0-9_]*$"; // Default
        int minLength = 1;
        int maxLength = 64;
        
        for (ConstraintDefinitionParser.ConstraintInstance constraint : constraintInstances) {
            String constraintType = constraint.getAbstractRef() != null ? constraint.getAbstractRef() : constraint.getInlineType();
            String targetName = constraint.getTargetName();
            
            if ("pattern".equals(constraintType) && "name".equals(targetName)) {
                Map<String, Object> params = constraint.getParameters();
                if (params.containsKey("pattern")) {
                    pattern = params.get("pattern").toString();
                }
            }
            
            if ("length".equals(constraintType) && "name".equals(targetName)) {
                Map<String, Object> params = constraint.getParameters();
                if (params.containsKey("min")) {
                    minLength = ((Number) params.get("min")).intValue();
                }
                if (params.containsKey("max")) {
                    maxLength = ((Number) params.get("max")).intValue();
                }
            }
        }
        
        // Add pattern facet
        Element patternFacet = doc.createElement("xs:pattern");
        patternFacet.setAttribute("value", pattern);
        restriction.appendChild(patternFacet);
        
        // Add length facets
        Element minLengthFacet = doc.createElement("xs:minLength");
        minLengthFacet.setAttribute("value", String.valueOf(minLength));
        restriction.appendChild(minLengthFacet);
        
        Element maxLengthFacet = doc.createElement("xs:maxLength");
        maxLengthFacet.setAttribute("value", String.valueOf(maxLength));
        restriction.appendChild(maxLengthFacet);
        
        return simpleType;
    }

    /**
     * Create object type enumeration
     */
    private Element createObjectTypeEnumType(Document doc) {
        Element simpleType = doc.createElement("xs:simpleType");
        simpleType.setAttribute("name", "ObjectTypeEnum");
        
        Element restriction = doc.createElement("xs:restriction");
        restriction.setAttribute("base", "xs:string");
        simpleType.appendChild(restriction);
        
        String[] objectTypes = {"pojo", "value", "data", "proxy", "mapped"};
        for (String type : objectTypes) {
            Element enumeration = doc.createElement("xs:enumeration");
            enumeration.setAttribute("value", type);
            restriction.appendChild(enumeration);
        }
        
        return simpleType;
    }

    /**
     * Create field type enumeration
     */
    private Element createFieldTypeEnumType(Document doc) {
        Element simpleType = doc.createElement("xs:simpleType");
        simpleType.setAttribute("name", "FieldTypeEnum");
        
        Element restriction = doc.createElement("xs:restriction");
        restriction.setAttribute("base", "xs:string");
        simpleType.appendChild(restriction);
        
        String[] fieldTypes = {"string", "int", "long", "double", "float", "boolean", "date", "timestamp"};
        for (String type : fieldTypes) {
            Element enumeration = doc.createElement("xs:enumeration");
            enumeration.setAttribute("value", type);
            restriction.appendChild(enumeration);
        }
        
        return simpleType;
    }

    /**
     * Create constraint annotation with constraint information
     */
    private Element createConstraintAnnotation(Document doc, String description) {
        Element annotation = doc.createElement("xs:annotation");
        Element documentation = doc.createElement("xs:documentation");
        
        StringBuilder constraintInfo = new StringBuilder(description);
        constraintInfo.append(": ");
        
        for (ConstraintDefinitionParser.ConstraintInstance constraint : constraintInstances) {
            String constraintType = constraint.getAbstractRef() != null ? constraint.getAbstractRef() : constraint.getInlineType();
            constraintInfo.append(constraintType).append("(");
            constraint.getParameters().forEach((key, value) -> 
                constraintInfo.append(key).append("=").append(value).append(", "));
            constraintInfo.append(") ");
        }
        
        documentation.setTextContent(constraintInfo.toString());
        annotation.appendChild(documentation);
        
        return annotation;
    }
}