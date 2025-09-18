package com.draagon.meta.constraint;

import com.google.gson.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * v6.0.0: Parses constraint definition files with abstract definitions and specific instances.
 * Supports reference-based loading to avoid duplicate constraint files and handles graceful
 * degradation for unknown constraint types in enterprise scenarios.
 */
public class ConstraintDefinitionParser {
    
    private static final Logger log = LoggerFactory.getLogger(ConstraintDefinitionParser.class);
    
    private final Gson gson;
    private final Set<String> loadedFiles;
    private final Map<String, AbstractConstraintDefinition> abstractDefinitions;
    
    public ConstraintDefinitionParser() {
        this.gson = createGson();
        this.loadedFiles = new HashSet<>();
        this.abstractDefinitions = new HashMap<>();
    }
    
    /**
     * Parse constraint definitions from a resource file
     * @param resourcePath Path to JSON constraint file in classpath
     * @return Parsed constraint definitions
     * @throws ConstraintParseException If parsing fails
     */
    public ConstraintDefinitions parseFromResource(String resourcePath) throws ConstraintParseException {
        if (loadedFiles.contains(resourcePath)) {
            log.debug("Constraint file [{}] already loaded, skipping", resourcePath);
            return new ConstraintDefinitions(Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        }
        
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new ConstraintParseException("Constraint file not found: " + resourcePath);
            }
            
            loadedFiles.add(resourcePath);
            return parseFromStream(is, resourcePath);
            
        } catch (IOException e) {
            throw new ConstraintParseException("Error reading constraint file: " + resourcePath, e);
        }
    }
    
    /**
     * Parse constraint definitions from an input stream
     * @param inputStream Stream containing JSON constraint definitions
     * @param sourceName Source name for error reporting
     * @return Parsed constraint definitions
     * @throws ConstraintParseException If parsing fails
     */
    public ConstraintDefinitions parseFromStream(InputStream inputStream, String sourceName) throws ConstraintParseException {
        try (InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
            JsonObject root = gson.fromJson(reader, JsonObject.class);
            
            if (root == null) {
                throw new ConstraintParseException("Invalid JSON in constraint file: " + sourceName);
            }
            
            return parseConstraintDefinitions(root, sourceName);
            
        } catch (JsonSyntaxException e) {
            throw new ConstraintParseException("JSON syntax error in constraint file: " + sourceName, e);
        } catch (IOException e) {
            throw new ConstraintParseException("Error reading constraint file: " + sourceName, e);
        }
    }
    
    /**
     * Get all loaded abstract constraint definitions
     * @return Map of abstract definition ID to definition
     */
    public Map<String, AbstractConstraintDefinition> getAbstractDefinitions() {
        return Collections.unmodifiableMap(abstractDefinitions);
    }
    
    /**
     * Clear all loaded definitions (for testing)
     */
    public void clearLoaded() {
        loadedFiles.clear();
        abstractDefinitions.clear();
    }
    
    private ConstraintDefinitions parseConstraintDefinitions(JsonObject root, String sourceName) throws ConstraintParseException {
        List<String> references = new ArrayList<>();
        List<AbstractConstraintDefinition> abstracts = new ArrayList<>();
        List<ConstraintInstance> instances = new ArrayList<>();
        
        // Parse references first
        if (root.has("references")) {
            JsonArray referencesArray = root.getAsJsonArray("references");
            for (JsonElement ref : referencesArray) {
                references.add(ref.getAsString());
            }
        }
        
        // Parse abstract definitions
        if (root.has("abstracts")) {
            JsonArray abstractsArray = root.getAsJsonArray("abstracts");
            for (JsonElement abstractEl : abstractsArray) {
                AbstractConstraintDefinition abstractDef = parseAbstractDefinition(abstractEl.getAsJsonObject(), sourceName);
                abstracts.add(abstractDef);
                abstractDefinitions.put(abstractDef.getId(), abstractDef);
            }
        }
        
        // Parse constraint instances
        if (root.has("constraints")) {
            JsonArray constraintsArray = root.getAsJsonArray("constraints");
            for (JsonElement constraintEl : constraintsArray) {
                ConstraintInstance instance = parseConstraintInstance(constraintEl.getAsJsonObject(), sourceName);
                instances.add(instance);
            }
        }
        
        return new ConstraintDefinitions(references, abstracts, instances);
    }
    
    private AbstractConstraintDefinition parseAbstractDefinition(JsonObject obj, String sourceName) throws ConstraintParseException {
        try {
            String id = getRequiredString(obj, "id", sourceName);
            String type = getRequiredString(obj, "type", sourceName);
            String description = obj.has("description") ? obj.get("description").getAsString() : null;
            
            Map<String, Object> parameters = parseParameters(obj.get("parameters"));
            
            return new AbstractConstraintDefinition(id, type, description, parameters);
            
        } catch (Exception e) {
            throw new ConstraintParseException("Error parsing abstract constraint definition in " + sourceName, e);
        }
    }
    
    private ConstraintInstance parseConstraintInstance(JsonObject obj, String sourceName) throws ConstraintParseException {
        try {
            String targetType = getRequiredString(obj, "targetType", sourceName);
            String targetSubType = obj.has("targetSubType") ? obj.get("targetSubType").getAsString() : null;
            String targetName = obj.has("targetName") ? obj.get("targetName").getAsString() : null;
            
            // Can have either abstractRef or inline constraint definition
            if (obj.has("abstractRef")) {
                String abstractRef = obj.get("abstractRef").getAsString();
                Map<String, Object> overrides = parseParameters(obj.get("overrides"));
                return ConstraintInstance.forAbstractRef(targetType, targetSubType, targetName, abstractRef, overrides);
            } else {
                // Inline constraint definition
                String type = getRequiredString(obj, "type", sourceName);
                Map<String, Object> parameters = parseParameters(obj.get("parameters"));
                return ConstraintInstance.forInlineDefinition(targetType, targetSubType, targetName, type, parameters);
            }
            
        } catch (Exception e) {
            throw new ConstraintParseException("Error parsing constraint instance in " + sourceName, e);
        }
    }
    
    private Map<String, Object> parseParameters(JsonElement parametersEl) {
        if (parametersEl == null || parametersEl.isJsonNull()) {
            return Collections.emptyMap();
        }
        
        Map<String, Object> parameters = new HashMap<>();
        JsonObject paramObj = parametersEl.getAsJsonObject();
        
        for (Map.Entry<String, JsonElement> entry : paramObj.entrySet()) {
            parameters.put(entry.getKey(), parseJsonValue(entry.getValue()));
        }
        
        return parameters;
    }
    
    private Object parseJsonValue(JsonElement element) {
        if (element.isJsonNull()) {
            return null;
        } else if (element.isJsonPrimitive()) {
            JsonPrimitive primitive = element.getAsJsonPrimitive();
            if (primitive.isBoolean()) {
                return primitive.getAsBoolean();
            } else if (primitive.isNumber()) {
                // Try to preserve number type
                Number number = primitive.getAsNumber();
                if (number.doubleValue() == number.intValue()) {
                    return number.intValue();
                } else {
                    return number.doubleValue();
                }
            } else {
                return primitive.getAsString();
            }
        } else if (element.isJsonArray()) {
            JsonArray array = element.getAsJsonArray();
            List<Object> list = new ArrayList<>();
            for (JsonElement item : array) {
                list.add(parseJsonValue(item));
            }
            return list;
        } else if (element.isJsonObject()) {
            JsonObject obj = element.getAsJsonObject();
            Map<String, Object> map = new HashMap<>();
            for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
                map.put(entry.getKey(), parseJsonValue(entry.getValue()));
            }
            return map;
        }
        return null;
    }
    
    private String getRequiredString(JsonObject obj, String field, String sourceName) throws ConstraintParseException {
        if (!obj.has(field) || obj.get(field).isJsonNull()) {
            throw new ConstraintParseException("Required field '" + field + "' missing in " + sourceName);
        }
        return obj.get(field).getAsString();
    }
    
    private Gson createGson() {
        return new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();
    }
    
    /**
     * Container for parsed constraint definitions
     */
    public static class ConstraintDefinitions {
        private final List<String> references;
        private final List<AbstractConstraintDefinition> abstracts;
        private final List<ConstraintInstance> instances;
        
        public ConstraintDefinitions(List<String> references, 
                                   List<AbstractConstraintDefinition> abstracts,
                                   List<ConstraintInstance> instances) {
            this.references = Collections.unmodifiableList(new ArrayList<>(references));
            this.abstracts = Collections.unmodifiableList(new ArrayList<>(abstracts));
            this.instances = Collections.unmodifiableList(new ArrayList<>(instances));
        }
        
        public List<String> getReferences() { return references; }
        public List<AbstractConstraintDefinition> getAbstracts() { return abstracts; }
        public List<ConstraintInstance> getInstances() { return instances; }
    }
    
    /**
     * Abstract constraint definition that can be referenced by multiple instances
     */
    public static class AbstractConstraintDefinition {
        private final String id;
        private final String type;
        private final String description;
        private final Map<String, Object> parameters;
        
        public AbstractConstraintDefinition(String id, String type, String description, Map<String, Object> parameters) {
            this.id = id;
            this.type = type;
            this.description = description;
            this.parameters = Collections.unmodifiableMap(new HashMap<>(parameters));
        }
        
        public String getId() { return id; }
        public String getType() { return type; }
        public String getDescription() { return description; }
        public Map<String, Object> getParameters() { return parameters; }
    }
    
    /**
     * Specific constraint instance targeting a type/subtype/name combination
     */
    public static class ConstraintInstance {
        private final String targetType;
        private final String targetSubType;
        private final String targetName;
        private final String abstractRef;
        private final String inlineType;
        private final Map<String, Object> parameters;
        
        // Private constructor
        private ConstraintInstance(String targetType, String targetSubType, String targetName, 
                                 String abstractRef, String inlineType, Map<String, Object> parameters) {
            this.targetType = targetType;
            this.targetSubType = targetSubType;
            this.targetName = targetName;
            this.abstractRef = abstractRef;
            this.inlineType = inlineType;
            this.parameters = parameters != null ? Collections.unmodifiableMap(new HashMap<>(parameters)) : Collections.emptyMap();
        }
        
        // Factory method for abstract reference
        public static ConstraintInstance forAbstractRef(String targetType, String targetSubType, String targetName, 
                                                       String abstractRef, Map<String, Object> overrides) {
            return new ConstraintInstance(targetType, targetSubType, targetName, abstractRef, null, 
                overrides != null ? overrides : Collections.emptyMap());
        }
        
        // Factory method for inline definition
        public static ConstraintInstance forInlineDefinition(String targetType, String targetSubType, String targetName,
                                                           String inlineType, Map<String, Object> parameters) {
            return new ConstraintInstance(targetType, targetSubType, targetName, null, inlineType,
                parameters != null ? parameters : Collections.emptyMap());
        }
        
        public String getTargetType() { return targetType; }
        public String getTargetSubType() { return targetSubType; }
        public String getTargetName() { return targetName; }
        public String getAbstractRef() { return abstractRef; }
        public String getInlineType() { return inlineType; }
        public Map<String, Object> getParameters() { return parameters; }
        
        public boolean isAbstractReference() { return abstractRef != null; }
        public boolean isInlineDefinition() { return inlineType != null; }
    }
}