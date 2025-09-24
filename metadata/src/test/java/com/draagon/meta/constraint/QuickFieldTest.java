package com.draagon.meta.constraint;

import com.draagon.meta.constraint.ConstraintEnforcer;
import com.draagon.meta.constraint.ConstraintFlattener;
import com.draagon.meta.loader.simple.SimpleLoader;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Quick test to verify field constraint fix
 */
public class QuickFieldTest {

    private static final Logger log = LoggerFactory.getLogger(QuickFieldTest.class);

    @Test
    public void testFieldConstraintFix() throws Exception {
        // Force class loading
        Class.forName("com.draagon.meta.loader.MetaDataLoader");
        Class.forName("com.draagon.meta.field.MetaField");

        // Create SimpleLoader to trigger full initialization
        SimpleLoader loader = new SimpleLoader("test");

        // Get constraint flattener
        ConstraintEnforcer enforcer = ConstraintEnforcer.getInstance();
        ConstraintFlattener flattener = enforcer.getConstraintFlattener();

        // Check specific field.long rule
        boolean canPlace = flattener.isPlacementAllowed("loader", "simple", "field", "long", "testField");
        log.info("Can loader.simple accept field.long 'testField': {}", canPlace);

        // Check what rules exist for this specific case
        Map<String, ConstraintFlattener.PlacementRule> allRules = flattener.getAllRules();

        String ruleKey = "loader.simple -> field.long [*]";
        ConstraintFlattener.PlacementRule rule = allRules.get(ruleKey);
        if (rule != null) {
            log.info("Rule '{}' = [allowed={}, reason={}]", ruleKey, rule.isAllowed(), rule.getReason());
        } else {
            log.info("Rule '{}' NOT FOUND", ruleKey);
        }

        // Check valid child types for loader.simple
        log.info("Valid child types for loader.simple: {}", flattener.getValidChildTypes("loader", "simple"));
    }
}