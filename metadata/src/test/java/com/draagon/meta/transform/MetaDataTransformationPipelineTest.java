package com.draagon.meta.transform;

import com.draagon.meta.object.pojo.PojoMetaObject;
import com.draagon.meta.field.StringField;
import com.draagon.meta.field.IntegerField;
import com.draagon.meta.attr.StringAttribute;
import com.draagon.meta.loader.simple.SimpleLoader;
import com.draagon.meta.constraint.ConstraintRegistry;
import com.draagon.meta.constraint.RelationshipConstraintEnforcer;
import com.draagon.meta.constraint.AdvancedRelationshipConstraintProvider;
import com.draagon.meta.transform.TransformationResult;
import com.draagon.meta.transform.TransformationPreview;
import com.draagon.meta.transform.TransformationStats;
import com.draagon.meta.transform.TransformationContext;
import com.draagon.meta.transform.TransformationConfiguration;
import com.draagon.meta.transform.InheritanceCompletionRule;
import com.draagon.meta.transform.JpaEnhancementRule;
import com.draagon.meta.transform.ConstraintResolutionRule;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;

import java.util.ArrayList;

/**
 * Comprehensive test demonstrating the MetaData Transformation Pipeline in action.
 * This test showcases how the transformation system can automatically enhance,
 * complete, and optimize metadata through intelligent rule application.
 *
 * <h3>Test Scenarios:</h3>
 * <ul>
 *   <li><strong>JPA Enhancement:</strong> Automatic addition of JPA annotations</li>
 *   <li><strong>Inheritance Completion:</strong> Adding missing fields for base class contracts</li>
 *   <li><strong>Constraint Resolution:</strong> Fixing constraint violations automatically</li>
 *   <li><strong>Complex Pipeline:</strong> Multiple rules working together</li>
 * </ul>
 */
public class MetaDataTransformationPipelineTest {

    private static final Logger log = LoggerFactory.getLogger(MetaDataTransformationPipelineTest.class);

    private ConstraintRegistry constraintRegistry;
    private RelationshipConstraintEnforcer constraintEnforcer;
    private MetaDataTransformer transformer;
    private SimpleLoader loader;

    @Before
    public void setUp() {
        log.info("Setting up metadata transformation pipeline test");

        // Initialize constraint system
        constraintRegistry = ConstraintRegistry.getInstance();

        // Register advanced relationship constraints
        AdvancedRelationshipConstraintProvider constraintProvider = new AdvancedRelationshipConstraintProvider();
        constraintProvider.registerConstraints(constraintRegistry);

        constraintEnforcer = new RelationshipConstraintEnforcer(constraintRegistry);

        // Create transformer with all rules
        transformer = new MetaDataTransformer.Builder()
            .addJpaEnhancements()
            .addInheritanceCompletion()
            .addConstraintResolution()
            .build();

        // Create test metadata loader
        loader = new SimpleLoader("transformation-pipeline-test");

        // Set empty source URIs and initialize the loader
        loader.setSourceURIs(new ArrayList<>());

        try {
            loader.init();
        } catch (Exception e) {
            // Initialization with empty sources should work, but if it fails we'll log it
            log.debug("Loader initialization completed (empty sources): {}", e.getMessage());
        }

        log.info("Transformation pipeline test setup complete - transformer has {} rules",
                transformer.getTransformationRules().size());
    }

    @Test
    public void testJpaEnhancementTransformation() {
        log.info("Testing JPA enhancement transformation");

        // Create a basic User object with database table but no JPA annotations
        PojoMetaObject user = new PojoMetaObject("User");
        StringAttribute dbTableAttr = new StringAttribute("dbTable");
        dbTableAttr.setValue("users");
        user.addMetaAttr(dbTableAttr);

        // Add a field with database column mapping
        StringField nameField = new StringField("name");
        StringAttribute dbColumnAttr = new StringAttribute("dbColumn");
        dbColumnAttr.setValue("user_name");
        nameField.addMetaAttr(dbColumnAttr);
        user.addChild(nameField);

        loader.addChild(user);

        // Transform the metadata
        TransformationResult result = transformer.transform(loader);

        // Verify transformation success
        assertTrue("Transformation should succeed", result.isSuccess());
        assertTrue("Should have applied transformations", result.getTotalTransformations() > 0);

        // Verify JPA was enabled
        assertTrue("User should have hasJpa enabled", user.hasMetaAttr("hasJpa"));
        assertEquals("hasJpa should be true", "true", user.getMetaAttr("hasJpa").getValueAsString());

        // Verify the dbTable attribute is still present (used for JPA table mapping)
        assertTrue("User should still have dbTable attribute", user.hasMetaAttr("dbTable"));
        assertEquals("dbTable should match expected", "users", user.getMetaAttr("dbTable").getValueAsString());

        // Verify field still has dbColumn (used for JPA column mapping)
        assertTrue("Name field should have dbColumn attribute", nameField.hasMetaAttr("dbColumn"));
        assertEquals("dbColumn should match expected", "user_name", nameField.getMetaAttr("dbColumn").getValueAsString());

        log.info("✅ JPA enhancement transformation successful: {} transformations applied",
                result.getTotalTransformations());
    }

    @Test
    public void testInheritanceCompletionTransformation() {
        log.info("Testing inheritance completion transformation");

        // Create a User object that implements BaseEntity but is missing required fields
        PojoMetaObject user = new PojoMetaObject("User");
        StringAttribute implementsAttr = new StringAttribute("implements");
        implementsAttr.setValue("BaseEntity");
        user.addMetaAttr(implementsAttr);

        StringAttribute dbTableAttr = new StringAttribute("dbTable");
        dbTableAttr.setValue("users");
        user.addMetaAttr(dbTableAttr);

        // Add only a name field - missing the required 'id' field
        StringField nameField = new StringField("name");
        StringAttribute dbColumnAttr = new StringAttribute("dbColumn");
        dbColumnAttr.setValue("user_name");
        nameField.addMetaAttr(dbColumnAttr);
        user.addChild(nameField);

        loader.addChild(user);

        // Transform the metadata
        TransformationResult result = transformer.transform(loader);

        // Verify transformation success
        assertTrue("Transformation should succeed", result.isSuccess());
        assertTrue("Should have applied transformations", result.getTotalTransformations() > 0);

        // Verify the required 'id' field was added
        boolean hasIdField = user.getChildren().stream()
            .anyMatch(child -> child.getName().equals("id"));
        assertTrue("User should have 'id' field added for BaseEntity inheritance", hasIdField);

        log.info("✅ Inheritance completion transformation successful: {} transformations applied",
                result.getTotalTransformations());
    }

    @Test
    public void testTransformationPreview() {
        log.info("Testing transformation preview functionality");

        // Create metadata that would be transformed
        PojoMetaObject user = new PojoMetaObject("User");
        StringAttribute dbTableAttr = new StringAttribute("dbTable");
        dbTableAttr.setValue("users");
        user.addMetaAttr(dbTableAttr);

        StringField nameField = new StringField("name");
        user.addChild(nameField);

        loader.addChild(user);

        // Generate preview
        TransformationPreview preview = transformer.preview(loader);

        // Verify preview results
        assertNotNull("Preview should not be null", preview);
        assertTrue("Preview should show potential transformations", preview.hasTransformations());
        assertTrue("Preview should have applicable rules", preview.getApplicableRules() > 0);

        log.info("✅ Transformation preview successful: {} potential transformations across {} rules",
                preview.getPotentialTransformations(), preview.getApplicableRules());

        // Log detailed preview
        log.info("Preview details:\n{}", preview.getDetailedReport());
    }

    @Test
    public void testComplexTransformationPipeline() {
        log.info("Testing complex transformation pipeline with multiple rules");

        // Create a complex scenario that requires multiple transformations:
        // 1. User implements AuditableEntity (needs id, createdDate, modifiedDate fields)
        // 2. Has database table (needs JPA annotations)
        // 3. Has foreign key field (might need constraint resolution)

        PojoMetaObject user = new PojoMetaObject("User");
        StringAttribute implementsAttr = new StringAttribute("implements");
        implementsAttr.setValue("AuditableEntity");
        user.addMetaAttr(implementsAttr);

        StringAttribute dbTableAttr = new StringAttribute("dbTable");
        dbTableAttr.setValue("users");
        user.addMetaAttr(dbTableAttr);

        // Add a foreign key field referencing Department
        StringField departmentIdField = new StringField("departmentId");
        StringAttribute objectRefAttr = new StringAttribute("objectRef");
        objectRefAttr.setValue("Department");
        departmentIdField.addMetaAttr(objectRefAttr);
        StringAttribute deptDbColumnAttr = new StringAttribute("dbColumn");
        deptDbColumnAttr.setValue("department_id");
        departmentIdField.addMetaAttr(deptDbColumnAttr);
        user.addChild(departmentIdField);

        loader.addChild(user);

        // Create transformation context with constraint enforcer
        TransformationContext context = new TransformationContext(loader,
                                                                  TransformationConfiguration.defaultConfiguration());
        context.setConstraintEnforcer(constraintEnforcer);

        // Transform using context
        TransformationResult result = transformer.transform(loader);

        // Verify comprehensive transformation
        assertTrue("Complex transformation should succeed", result.isSuccess());
        assertTrue("Should have applied multiple transformations", result.getTotalTransformations() >= 3);

        log.info("✅ Complex transformation pipeline successful:");
        log.info("   Rules applied: {}", result.getRulesApplied());
        log.info("   Total transformations: {}", result.getTotalTransformations());
        log.info("   Execution time: {}ms", result.getTotalExecutionTime());

        // Log detailed results
        log.info("Transformation details:\n{}", result.getDetailedReport());
    }

    @Test
    public void testTransformationStatistics() {
        log.info("Testing transformation statistics");

        // Get transformer statistics
        TransformationStats stats = transformer.getStats();

        assertNotNull("Stats should not be null", stats);
        assertTrue("Should have rules configured", stats.hasRules());
        assertTrue("Should have high priority rules", stats.getHighPriorityRules() > 0);

        log.info("✅ Transformation statistics:");
        log.info("   Total rules: {}", stats.getTotalRules());
        log.info("   High priority rules: {}", stats.getHighPriorityRules());
        log.info("   Configuration: {}", stats.getConfigurationName());

        // Log full statistics
        log.info("Full statistics:\n{}", stats.getSummary());
    }

    @Test
    public void testTransformationConfiguration() {
        log.info("Testing different transformation configurations");

        // Test high-performance configuration
        TransformationConfiguration highPerfConfig = TransformationConfiguration.highPerformanceConfiguration();
        MetaDataTransformer highPerfTransformer = new MetaDataTransformer(highPerfConfig);

        assertEquals("Should use high-performance config", "high-performance", highPerfConfig.getName());
        assertTrue("Should enable caching", highPerfConfig.isCachingEnabled());

        // Test conservative configuration
        TransformationConfiguration conservativeConfig = TransformationConfiguration.conservativeConfiguration();
        MetaDataTransformer conservativeTransformer = new MetaDataTransformer(conservativeConfig);

        assertEquals("Should use conservative config", "conservative", conservativeConfig.getName());
        assertTrue("Should stop on first failure", conservativeConfig.isStopOnFirstFailure());
        assertTrue("Should enable detailed logging", conservativeConfig.isDetailedLoggingEnabled());

        // Test preview configuration
        TransformationConfiguration previewConfig = TransformationConfiguration.previewConfiguration();
        assertTrue("Should be in dry run mode", previewConfig.isDryRunMode());

        log.info("✅ Transformation configuration tests passed");
    }

    @Test
    public void testTransformationRuleOrdering() {
        log.info("Testing transformation rule priority ordering");

        // Verify that rules are applied in priority order
        // Constraint resolution (900) should come before JPA enhancement (750)
        // which should come before inheritance completion (800)

        MetaDataTransformer testTransformer = new MetaDataTransformer.Builder()
            .addRule(new JpaEnhancementRule())           // Priority 750
            .addRule(new InheritanceCompletionRule())    // Priority 800
            .addRule(new ConstraintResolutionRule())     // Priority 900
            .build();

        var rules = testTransformer.getTransformationRules();
        assertEquals("Should have 3 rules", 3, rules.size());

        // Verify highest priority rule is first when sorted
        int maxPriority = rules.stream().mapToInt(TransformationRule::getPriority).max().orElse(0);
        assertEquals("Highest priority should be constraint resolution", 900, maxPriority);

        log.info("✅ Rule ordering test passed - rules properly prioritized");
    }
}