package com.draagon.meta.registry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Core implementation of MetaDataContextProvider that loads context rules from metaobjects.types.xml.
 * 
 * <p>This service restores the context-aware metadata creation behavior that was defined in
 * the original TypesConfig system, where attributes and child elements would automatically
 * use the appropriate subtype based on their parent context.</p>
 * 
 * <p>For example, 'keys' attributes under 'key' elements are automatically created as 
 * 'stringArray' type rather than the default 'string' type.</p>
 */
public class CoreMetaDataContextProvider implements MetaDataContextProvider {

    private static final Logger log = LoggerFactory.getLogger(CoreMetaDataContextProvider.class);
    
    private final Map<String, Map<String, String>> attributeRules = new HashMap<>();
    private final Map<String, Map<String, Map<String, String>>> subTypeAttributeRules = new HashMap<>();
    private final Map<String, Map<String, String>> childRules = new HashMap<>();
    private boolean initialized = false;
    
    @Override
    public String getContextSpecificAttributeSubType(String parentType, String parentSubType, String attributeName) {
        ensureInitialized();
        
        // First check subtype-specific rules
        if (parentSubType != null) {
            Map<String, Map<String, String>> typeSubTypes = subTypeAttributeRules.get(parentType);
            if (typeSubTypes != null) {
                Map<String, String> subTypeRules = typeSubTypes.get(parentSubType);
                if (subTypeRules != null) {
                    String subType = subTypeRules.get(attributeName);
                    if (subType != null) {
                        log.debug("Found subtype-specific attribute rule: {}:{}:{} -> {}", 
                                parentType, parentSubType, attributeName, subType);
                        return subType;
                    }
                }
            }
        }
        
        // Then check general type rules
        Map<String, String> typeRules = attributeRules.get(parentType);
        if (typeRules != null) {
            String subType = typeRules.get(attributeName);
            if (subType != null) {
                log.debug("Found general attribute rule: {}:{} -> {}", parentType, attributeName, subType);
                return subType;
            }
        }
        
        return null; // No specific rule found
    }
    
    @Override
    public String getContextSpecificChildSubType(String parentType, String parentSubType, String childType, String childName) {
        ensureInitialized();
        
        // For now, focus on attribute rules. Child element rules can be added later if needed
        return null;
    }
    
    @Override
    public int getPriority() {
        return 100; // High priority - core system rules
    }
    
    @Override
    public String getDescription() {
        return "Core MetaData context provider that loads rules from metaobjects.types.xml";
    }
    
    private synchronized void ensureInitialized() {
        if (!initialized) {
            try {
                // Load hardcoded essential rules first
                loadEssentialRules();
                
                // Then try to load additional rules from XML file
                loadContextRules();
                
                initialized = true;
                log.info("Loaded {} attribute rules and {} subtype-specific rules from context providers", 
                        attributeRules.size(), subTypeAttributeRules.size());
            } catch (Exception e) {
                log.error("Failed to load context rules", e);
                initialized = true; // Don't retry on every call
            }
        }
    }
    
    /**
     * Load essential context rules that are required for core functionality
     */
    private void loadEssentialRules() {
        // Keys attributes on key elements should always be stringArray
        attributeRules.computeIfAbsent("key", k -> new HashMap<>())
                .put("keys", "stringArray");
        
        // ForeignObjectRef attributes on key elements should be string  
        attributeRules.computeIfAbsent("key", k -> new HashMap<>())
                .put("foreignObjectRef", "string");
        
        // Field-specific attribute type mappings (general rules)
        Map<String, String> fieldRules = attributeRules.computeIfAbsent("field", k -> new HashMap<>());
        fieldRules.put("maxLength", "int");
        fieldRules.put("minLength", "int");
        fieldRules.put("precision", "int");
        fieldRules.put("pattern", "string");
        fieldRules.put("defaultValue", "string");
        fieldRules.put("validation", "string");
        fieldRules.put("isAbstract", "string");
        fieldRules.put("required", "string");
        fieldRules.put("defaultView", "string");
        fieldRules.put("dateFormat", "string");
        fieldRules.put("minDate", "string");
        fieldRules.put("maxDate", "string");
        
        // Subtype-specific attribute rules for numeric fields
        Map<String, Map<String, String>> fieldSubTypes = subTypeAttributeRules.computeIfAbsent("field", k -> new HashMap<>());
        
        // Double field specific rules
        Map<String, String> doubleFieldRules = fieldSubTypes.computeIfAbsent("double", k -> new HashMap<>());
        doubleFieldRules.put("minValue", "double");
        doubleFieldRules.put("maxValue", "double");
        
        // Long field specific rules  
        Map<String, String> longFieldRules = fieldSubTypes.computeIfAbsent("long", k -> new HashMap<>());
        longFieldRules.put("minValue", "long");
        longFieldRules.put("maxValue", "long");
        
        // Float field specific rules
        Map<String, String> floatFieldRules = fieldSubTypes.computeIfAbsent("float", k -> new HashMap<>());
        floatFieldRules.put("minValue", "float");
        floatFieldRules.put("maxValue", "float");
        
        // Integer field specific rules (explicit for clarity)
        Map<String, String> intFieldRules = fieldSubTypes.computeIfAbsent("int", k -> new HashMap<>());
        intFieldRules.put("minValue", "int");
        intFieldRules.put("maxValue", "int");
        
        // Validator-specific attribute type mappings
        Map<String, String> validatorRules = attributeRules.computeIfAbsent("validator", k -> new HashMap<>());
        validatorRules.put("min", "string");  // Validators expect string attributes
        validatorRules.put("max", "string");  // Validators expect string attributes
        validatorRules.put("pattern", "string");
        validatorRules.put("values", "string");
        validatorRules.put("message", "string");
                
        log.debug("Loaded essential context rules: {} attribute mappings total", attributeRules.values().stream().mapToInt(Map::size).sum());
    }
    
    private void loadContextRules() throws ParserConfigurationException, IOException, SAXException {
        InputStream xmlStream = getClass().getClassLoader()
                .getResourceAsStream("com/draagon/meta/loader/xml/metaobjects.types.xml");
        
        if (xmlStream == null) {
            log.warn("Could not find metaobjects.types.xml in classpath");
            return;
        }
        
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(xmlStream);
        
        NodeList typeNodes = doc.getElementsByTagName("type");
        for (int i = 0; i < typeNodes.getLength(); i++) {
            Element typeElement = (Element) typeNodes.item(i);
            String typeName = typeElement.getAttribute("name");
            
            // Process direct children of this type
            processChildrenSection(typeElement, typeName, null);
            
            // Process subtype-specific children
            NodeList subTypeNodes = typeElement.getElementsByTagName("subType");
            for (int j = 0; j < subTypeNodes.getLength(); j++) {
                Element subTypeElement = (Element) subTypeNodes.item(j);
                String subTypeName = subTypeElement.getAttribute("name");
                
                processChildrenSection(subTypeElement, typeName, subTypeName);
            }
        }
        
        xmlStream.close();
    }
    
    private void processChildrenSection(Element parentElement, String typeName, String subTypeName) {
        NodeList childrenNodes = parentElement.getElementsByTagName("children");
        for (int i = 0; i < childrenNodes.getLength(); i++) {
            Node childrenNode = childrenNodes.item(i);
            
            // Only process direct children, not nested ones
            if (childrenNode.getParentNode() == parentElement) {
                Element childrenElement = (Element) childrenNode;
                processChildren(childrenElement, typeName, subTypeName);
            }
        }
    }
    
    private void processChildren(Element childrenElement, String typeName, String subTypeName) {
        NodeList childNodes = childrenElement.getElementsByTagName("child");
        for (int i = 0; i < childNodes.getLength(); i++) {
            Element childElement = (Element) childNodes.item(i);
            
            String childType = childElement.getAttribute("type");
            String childSubType = childElement.getAttribute("subType");
            String childName = childElement.getAttribute("name");
            
            // We're mainly interested in attribute rules for now
            if ("attr".equals(childType) && !childName.isEmpty() && !childSubType.isEmpty()) {
                if (subTypeName == null) {
                    // General type rule
                    attributeRules.computeIfAbsent(typeName, k -> new HashMap<>())
                            .put(childName, childSubType);
                    log.debug("Loaded attribute rule: {}:{} -> {}", typeName, childName, childSubType);
                } else {
                    // Subtype-specific rule
                    subTypeAttributeRules.computeIfAbsent(typeName, k -> new HashMap<>())
                            .computeIfAbsent(subTypeName, k -> new HashMap<>())
                            .put(childName, childSubType);
                    log.debug("Loaded subtype attribute rule: {}:{}:{} -> {}", 
                            typeName, subTypeName, childName, childSubType);
                }
            }
        }
    }
}