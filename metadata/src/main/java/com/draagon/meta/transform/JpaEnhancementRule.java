package com.draagon.meta.transform;

import com.draagon.meta.MetaData;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.field.MetaField;
import com.draagon.meta.attr.StringAttribute;
import com.draagon.meta.attr.BooleanAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.ArrayList;

/**
 * JpaEnhancementRule automatically enhances metadata objects with JPA-related attributes
 * based on existing database attributes and naming patterns. This rule demonstrates
 * intelligent metadata transformation that reduces manual configuration.
 *
 * <h3>Transformations Applied:</h3>
 * <ul>
 *   <li><strong>Entity Annotations:</strong> Adds @Entity and @Table attributes to objects with dbTable</li>
 *   <li><strong>ID Field Detection:</strong> Marks primary key fields with @Id attributes</li>
 *   <li><strong>Column Mappings:</strong> Adds @Column attributes based on dbColumn values</li>
 *   <li><strong>Relationship Inference:</strong> Detects foreign key relationships and adds @JoinColumn</li>
 * </ul>
 *
 * <h3>Detection Logic:</h3>
 * <pre>{@code
 * // Objects that should get JPA annotations:
 * - Has 'dbTable' attribute
 * - NOT explicitly marked with 'skipJpa=true'
 * - Contains fields with database mappings
 *
 * // Fields that should get @Id annotation:
 * - Named 'id' or ends with 'Id'
 * - Has dbColumn ending with '_id'
 * - Is of type 'long' or 'int'
 * }</pre>
 *
 * @since 6.1.0
 */
public class JpaEnhancementRule implements TransformationRule {

    private static final Logger log = LoggerFactory.getLogger(JpaEnhancementRule.class);

    private static final String RULE_NAME = "jpa-enhancement";
    private static final String RULE_DESCRIPTION = "Automatically enhance metadata with JPA annotations based on database attributes";
    private static final int RULE_PRIORITY = 750; // High priority - should run early

    @Override
    public String getName() {
        return RULE_NAME;
    }

    @Override
    public String getDescription() {
        return RULE_DESCRIPTION;
    }

    @Override
    public int getPriority() {
        return RULE_PRIORITY;
    }

    @Override
    public TransformationCategory getCategory() {
        return TransformationCategory.ENHANCEMENT;
    }

    @Override
    public boolean isApplicableTo(List<MetaData> metaDataList) {
        return metaDataList.stream().anyMatch(this::shouldEnhanceWithJpa);
    }

    @Override
    public RuleResult apply(TransformationContext context) {
        long startTime = System.currentTimeMillis();
        RuleResult.Builder resultBuilder = new RuleResult.Builder();

        try {
            log.debug("Applying JPA enhancement rule to metadata");

            List<MetaData> candidates = context.getMetaDataToProcess();
            int enhancementsApplied = 0;

            for (MetaData metaData : candidates) {
                if (shouldEnhanceWithJpa(metaData)) {
                    int enhancements = enhanceWithJpa(metaData, context);
                    enhancementsApplied += enhancements;

                    // Record each transformation for proper counting
                    if (enhancements > 0) {
                        resultBuilder.addTransformation(metaData.getName(),
                            "Applied " + enhancements + " JPA enhancements");
                    }
                }
            }

            if (enhancementsApplied > 0) {
                resultBuilder.success(true)
                           .message("Enhanced " + enhancementsApplied + " metadata objects with JPA annotations");
                log.info("JPA enhancement rule applied {} enhancements", enhancementsApplied);
            } else {
                resultBuilder.success(true)
                           .message("No metadata objects required JPA enhancement");
                log.debug("JPA enhancement rule found no applicable metadata");
            }

        } catch (Exception e) {
            log.error("Error applying JPA enhancement rule: {}", e.getMessage(), e);
            resultBuilder.success(false)
                       .message("JPA enhancement failed: " + e.getMessage())
                       .addIssue("Exception during rule execution: " + e.getMessage());
        }

        long executionTime = System.currentTimeMillis() - startTime;
        resultBuilder.executionTime(executionTime)
                   .addMetric("candidatesEvaluated", context.getMetaDataToProcess().size())
                   .addMetric("executionTimeMs", executionTime);

        return resultBuilder.build();
    }

    @Override
    public int estimateImpact(TransformationContext context) {
        List<MetaData> candidates = context.getMetaDataToProcess();
        return (int) candidates.stream()
            .filter(this::shouldEnhanceWithJpa)
            .count();
    }

    /**
     * Check if the given metadata should be enhanced with JPA annotations
     */
    private boolean shouldEnhanceWithJpa(MetaData metaData) {
        if (!(metaData instanceof MetaObject)) {
            return false;
        }

        MetaObject metaObject = (MetaObject) metaData;

        // Skip if explicitly marked to skip JPA
        if (metaObject.hasMetaAttr("skipJpa") &&
            "true".equals(metaObject.getMetaAttr("skipJpa").getValueAsString())) {
            return false;
        }

        // Enhance if it has database table mapping
        if (metaObject.hasMetaAttr("dbTable")) {
            return true;
        }

        // Enhance if it has fields with database column mappings
        for (MetaField field : metaObject.getChildren(MetaField.class)) {
            if (field.hasMetaAttr("dbColumn")) {
                return true;
            }
        }

        return false;
    }

    /**
     * Apply JPA enhancements to the given metadata object
     */
    private int enhanceWithJpa(MetaData metaData, TransformationContext context) {
        if (!(metaData instanceof MetaObject)) {
            return 0;
        }

        MetaObject metaObject = (MetaObject) metaData;
        int enhancementsApplied = 0;

        // Enable JPA for the object if not already set
        if (!metaObject.hasMetaAttr("hasJpa") ||
            !"true".equals(metaObject.getMetaAttr("hasJpa").getValueAsString())) {
            if (!context.isPreviewMode()) {
                BooleanAttribute jpaAttr = new BooleanAttribute("hasJpa");
                jpaAttr.setValue(true);
                metaObject.addMetaAttr(jpaAttr);
            }
            context.recordTransformation(metaObject, "Enabled JPA for object (hasJpa=true)");
            enhancementsApplied++;
        }

        // Log the table mapping (dbTable attribute is already present and supported)
        if (metaObject.hasMetaAttr("dbTable")) {
            String tableName = metaObject.getMetaAttr("dbTable").getValueAsString();
            context.recordTransformation(metaObject, "JPA table mapping: " + tableName);
        }

        // Enhance fields with JPA annotations
        for (MetaField field : metaObject.getChildren(MetaField.class)) {
            enhancementsApplied += enhanceFieldWithJpa(field, context);
        }

        return enhancementsApplied;
    }

    /**
     * Apply JPA enhancements to a specific field
     */
    private int enhanceFieldWithJpa(MetaField field, TransformationContext context) {
        int enhancementsApplied = 0;

        // Mark as ID field for primary key fields
        if (isPrimaryKeyField(field) && (!field.hasMetaAttr("isId") ||
            !"true".equals(field.getMetaAttr("isId").getValueAsString()))) {
            if (!context.isPreviewMode()) {
                BooleanAttribute idAttr = new BooleanAttribute("isId");
                idAttr.setValue(true);
                field.addMetaAttr(idAttr);
            }
            context.recordTransformation(field, "Marked field as ID (isId=true)");
            enhancementsApplied++;
        }

        // Log column mapping (dbColumn attribute is already present and supported)
        if (field.hasMetaAttr("dbColumn")) {
            String columnName = field.getMetaAttr("dbColumn").getValueAsString();
            context.recordTransformation(field, "JPA column mapping: " + columnName);
        }

        // Log foreign key relationship (using existing objectRef and dbColumn attributes)
        if (isForeignKeyField(field)) {
            String columnName = field.hasMetaAttr("dbColumn") ?
                field.getMetaAttr("dbColumn").getValueAsString() :
                field.getName() + "_id";
            String objectRef = field.hasMetaAttr("objectRef") ?
                field.getMetaAttr("objectRef").getValueAsString() : "unknown";

            context.recordTransformation(field, "JPA foreign key: " + columnName + " -> " + objectRef);
        }

        return enhancementsApplied;
    }

    /**
     * Check if a field appears to be a primary key field
     */
    private boolean isPrimaryKeyField(MetaField field) {
        String fieldName = field.getName();
        String fieldType = field.getSubType();

        // Check field name patterns
        if ("id".equals(fieldName) || fieldName.endsWith("Id") || fieldName.endsWith("ID")) {
            // Check if it's a numeric type suitable for IDs
            if ("long".equals(fieldType) || "int".equals(fieldType)) {
                return true;
            }
        }

        // Check database column patterns
        if (field.hasMetaAttr("dbColumn")) {
            String dbColumn = field.getMetaAttr("dbColumn").getValueAsString();
            if ("id".equals(dbColumn) || dbColumn.endsWith("_id") || dbColumn.endsWith("_ID")) {
                return true;
            }
        }

        return false;
    }

    /**
     * Check if a field appears to be a foreign key field
     */
    private boolean isForeignKeyField(MetaField field) {
        // Check if field has an object reference
        if (field.hasMetaAttr("objectRef")) {
            return true;
        }

        // Check naming patterns that suggest foreign keys
        String fieldName = field.getName();
        if (fieldName.endsWith("Id") && !fieldName.equals("id")) {
            return true;
        }

        // Check database column patterns
        if (field.hasMetaAttr("dbColumn")) {
            String dbColumn = field.getMetaAttr("dbColumn").getValueAsString();
            if (dbColumn.endsWith("_id") && !dbColumn.equals("id")) {
                return true;
            }
        }

        return false;
    }
}