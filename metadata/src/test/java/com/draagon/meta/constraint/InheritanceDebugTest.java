package com.draagon.meta.constraint;

import com.draagon.meta.registry.MetaDataRegistry;
import com.draagon.meta.registry.TypeDefinition;
import com.draagon.meta.registry.AcceptsChildrenDeclaration;
import com.draagon.meta.registry.AcceptsParentsDeclaration;
import com.draagon.meta.registry.SharedTestRegistry;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.loader.simple.SimpleLoader;
import com.draagon.meta.object.pojo.PojoMetaObject;
import com.draagon.meta.field.StringField;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.Map;

public class InheritanceDebugTest {

    private static final Logger log = LoggerFactory.getLogger(InheritanceDebugTest.class);

    @Before
    public void setUp() {
        // Use SharedTestRegistry to ensure proper provider discovery timing
        SharedTestRegistry.getInstance();
        log.debug("InheritanceDebugTest setup with shared registry: {}", SharedTestRegistry.getStatus());
    }

    @Test
    public void debugConstraintEnforcementTiming() {
        log.info("=== CONSTRAINT ENFORCEMENT TIMING DEBUG ===");

        // Force all class loading
        try {
            Class.forName("com.draagon.meta.loader.MetaDataLoader");
            Class.forName("com.draagon.meta.loader.simple.SimpleLoader");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        MetaDataRegistry registry = MetaDataRegistry.getInstance();

        log.info("=== BEFORE CONSTRAINT ENFORCEMENT ===");
        TypeDefinition simpleLoader = registry.getTypeDefinition("loader", "simple");
        if (simpleLoader != null) {
            log.info("loader.simple accepts children count: {}", simpleLoader.getAcceptsChildren().size());
            simpleLoader.getAcceptsChildren().forEach(child ->
                log.info("  Accepts: {}:{} named '{}'",
                    child.getChildType(), child.getChildSubType(), child.getChildName())
            );
        } else {
            log.error("loader.simple NOT FOUND before constraint enforcement!");
        }

        // Now trigger constraint enforcement (which creates ConstraintEnforcer singleton)
        try {
            ConstraintEnforcer enforcer = ConstraintEnforcer.getInstance();
            log.info("=== AFTER CONSTRAINT ENFORCEMENT INITIALIZATION ===");

            // Check the same loader type definition again
            TypeDefinition simpleLoaderAfter = registry.getTypeDefinition("loader", "simple");
            if (simpleLoaderAfter != null) {
                log.info("loader.simple accepts children count AFTER: {}", simpleLoaderAfter.getAcceptsChildren().size());
                simpleLoaderAfter.getAcceptsChildren().forEach(child ->
                    log.info("  Accepts AFTER: {}:{} named '{}'",
                        child.getChildType(), child.getChildSubType(), child.getChildName())
                );
            }

            // Test by trying to simulate the actual enforcement
            try {
                MetaDataRegistry testRegistry = MetaDataRegistry.getInstance();
                // Look up the types that are actually being used
                log.info("Registry contains loader.simple: {}", testRegistry.getTypeDefinition("loader", "simple") != null);
                log.info("Registry contains field.long: {}", testRegistry.getTypeDefinition("field", "long") != null);

                // Check if the registry has the proper accepts children relationships
                TypeDefinition loaderDef = testRegistry.getTypeDefinition("loader", "simple");
                if (loaderDef != null) {
                    log.info("loader.simple from registry has {} accepts children", loaderDef.getAcceptsChildren().size());

                    // Test the registry's acceptsChild method directly
                    boolean registryAccepts = testRegistry.acceptsChild("loader", "simple", "field", "long", "testField");
                    log.info("Registry.acceptsChild says loader.simple can accept field.long named 'testField': {}", registryAccepts);

                    // Test with package-qualified names like the failing test
                    boolean packagedAccepts = testRegistry.acceptsChild("loader", "simple", "field", "long", "acme::common::id");
                    log.info("Registry.acceptsChild says loader.simple can accept field.long named 'acme::common::id': {}", packagedAccepts);

                    boolean packagedAccepts2 = testRegistry.acceptsChild("loader", "simple", "field", "long", "simple::common::id");
                    log.info("Registry.acceptsChild says loader.simple can accept field.long named 'simple::common::id': {}", packagedAccepts2);
                }
            } catch (Exception e) {
                log.error("Error testing registry directly", e);
            }

        } catch (Exception e) {
            log.error("Error during constraint enforcement", e);
        }
    }

    @Test
    public void debugInheritanceResolution() {
        log.info("=== INHERITANCE DEBUG TEST ===");

        // Force class loading to trigger static blocks
        try {
            Class.forName("com.draagon.meta.loader.MetaDataLoader");
            Class.forName("com.draagon.meta.loader.simple.SimpleLoader");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        MetaDataRegistry registry = MetaDataRegistry.getInstance();

        log.info("=== ALL REGISTERED TYPES ===");
        registry.getRegisteredTypes().forEach(typeId -> {
            TypeDefinition typeDef = registry.getTypeDefinition(typeId);
            log.info("Type: {}, Parent: {}, Description: {}",
                typeId, typeDef.getParentQualifiedName(), typeDef.getDescription());
        });

        log.info("=== LOADER TYPES SPECIFICALLY ===");
        registry.getRegisteredTypes().stream()
            .filter(typeId -> typeId.type().equals("loader"))
            .forEach(typeId -> {
                TypeDefinition typeDef = registry.getTypeDefinition(typeId);
                log.info("Loader Type: {}", typeId);
                log.info("  Parent: {}", typeDef.getParentQualifiedName());
                log.info("  Has Parent: {}", typeDef.hasParent());
                log.info("  Accepts Children Count: {}", typeDef.getAcceptsChildren().size());
                typeDef.getAcceptsChildren().forEach(child ->
                    log.info("    Accepts: {}:{} named '{}'",
                        child.getChildType(), child.getChildSubType(), child.getChildName())
                );
            });

        log.info("=== METADATA.BASE TYPE ===");
        TypeDefinition metadataBase = registry.getTypeDefinition("metadata", "base");
        if (metadataBase != null) {
            log.info("metadata.base found:");
            log.info("  Description: {}", metadataBase.getDescription());
            log.info("  Accepts Children Count: {}", metadataBase.getAcceptsChildren().size());
            metadataBase.getAcceptsChildren().forEach(child ->
                log.info("    Accepts: {}:{} named '{}'",
                    child.getChildType(), child.getChildSubType(), child.getChildName())
            );
        } else {
            log.error("metadata.base NOT FOUND!");
        }
    }

    @Test
    public void debugFieldInheritance() {
        log.info("=== FIELD INHERITANCE DEBUG TEST ===");

        // CRITICAL: Force MetaDataLoader class loading FIRST before any registry access
        try {
            log.info("Forcing MetaDataLoader class loading...");
            Class.forName("com.draagon.meta.loader.MetaDataLoader");

            // Small delay to ensure registration completes
            Thread.sleep(10);

            Class.forName("com.draagon.meta.loader.simple.SimpleLoader");
            log.info("MetaDataLoader and SimpleLoader forced loaded");
        } catch (ClassNotFoundException | InterruptedException e) {
            e.printStackTrace();
        }

        MetaDataRegistry registry = MetaDataRegistry.getInstance();

        log.info("=== CHECKING FIELD.BASE PARENT DECLARATIONS ===");
        TypeDefinition fieldBaseDef = registry.getTypeDefinition("field", "base");
        if (fieldBaseDef != null) {
            log.info("field.base found - Direct accepts parents: {}", fieldBaseDef.getDirectAcceptsParents().size());
            fieldBaseDef.getDirectAcceptsParents().forEach(parent ->
                log.info("  field.base directly accepts parent: {}:{} when named '{}'",
                    parent.getParentType(), parent.getParentSubType(), parent.getExpectedChildName()));

            log.info("field.base inherited accepts parents: {}", fieldBaseDef.getInheritedAcceptsParents().size());
            fieldBaseDef.getInheritedAcceptsParents().forEach(parent ->
                log.info("  field.base inherits parent: {}:{} when named '{}'",
                    parent.getParentType(), parent.getParentSubType(), parent.getExpectedChildName()));
        }

        log.info("=== CHECKING FIELD.LONG PARENT DECLARATIONS ===");
        TypeDefinition fieldLongDef = registry.getTypeDefinition("field", "long");
        if (fieldLongDef != null) {
            log.info("field.long found - Direct accepts parents: {}", fieldLongDef.getDirectAcceptsParents().size());
            fieldLongDef.getDirectAcceptsParents().forEach(parent ->
                log.info("  field.long directly accepts parent: {}:{} when named '{}'",
                    parent.getParentType(), parent.getParentSubType(), parent.getExpectedChildName()));

            log.info("field.long inherited accepts parents: {}", fieldLongDef.getInheritedAcceptsParents().size());
            fieldLongDef.getInheritedAcceptsParents().forEach(parent ->
                log.info("  field.long inherits parent: {}:{} when named '{}'",
                    parent.getParentType(), parent.getParentSubType(), parent.getExpectedChildName()));

            log.info("field.long ALL accepts parents: {}", fieldLongDef.getAcceptsParents().size());
            fieldLongDef.getAcceptsParents().forEach(parent ->
                log.info("  field.long ALL accepts parent: {}:{} when named '{}'",
                    parent.getParentType(), parent.getParentSubType(), parent.getExpectedChildName()));
        }

        // Test the acceptsParent method directly
        log.info("=== TESTING FIELD.LONG ACCEPTS PARENT DIRECTLY ===");
        if (fieldLongDef != null) {
            boolean acceptsLoaderSimple = fieldLongDef.acceptsParent("loader", "simple", "testField");
            log.info("field.long.acceptsParent('loader', 'simple', 'testField') = {}", acceptsLoaderSimple);

            boolean acceptsMetadataBase = fieldLongDef.acceptsParent("metadata", "base", "testField");
            log.info("field.long.acceptsParent('metadata', 'base', 'testField') = {}", acceptsMetadataBase);
        }

        // Get the constraint enforcer and flattener
        ConstraintEnforcer enforcer = ConstraintEnforcer.getInstance();
        ConstraintFlattener flattener = enforcer.getConstraintFlattener();

        // Test the constraint flattener lookup
        log.info("=== TESTING CONSTRAINT FLATTENER LOOKUP ===");
        boolean canPlace = flattener.isPlacementAllowed("loader", "simple", "field", "long", "testField");
        log.info("ConstraintFlattener.isPlacementAllowed('loader', 'simple', 'field', 'long', 'testField') = {}", canPlace);
    }

    @Test
    public void debugObjectPojoInheritance() {
        log.info("=== OBJECT.POJO INHERITANCE DEBUG TEST ===");

        MetaDataRegistry registry = MetaDataRegistry.getInstance();

        // Check object.base definition
        TypeDefinition objectBase = registry.getTypeDefinition("object", "base");
        log.info("object.base definition: {}", objectBase);

        if (objectBase != null) {
            log.info("object.base direct acceptsChildren: {} declarations", objectBase.getDirectAcceptsChildren().size());
            for (AcceptsChildrenDeclaration decl : objectBase.getDirectAcceptsChildren()) {
                log.info("  - object.base directly accepts: {}:{}:{}",
                    decl.getChildType(), decl.getChildSubType(), decl.getChildName());
            }

            log.info("object.base inherited acceptsChildren: {} declarations", objectBase.getInheritedAcceptsChildren().size());
            for (AcceptsChildrenDeclaration decl : objectBase.getInheritedAcceptsChildren()) {
                log.info("  - object.base inherited accepts: {}:{}:{}",
                    decl.getChildType(), decl.getChildSubType(), decl.getChildName());
            }
        }

        // Check object.pojo definition
        TypeDefinition objectPojo = registry.getTypeDefinition("object", "pojo");
        log.info("object.pojo definition: {}", objectPojo);

        if (objectPojo != null) {
            log.info("object.pojo has parent: {}, parent type: {}", objectPojo.hasParent(), objectPojo.getParentQualifiedName());

            log.info("object.pojo direct acceptsChildren: {} declarations", objectPojo.getDirectAcceptsChildren().size());
            for (AcceptsChildrenDeclaration decl : objectPojo.getDirectAcceptsChildren()) {
                log.info("  - object.pojo directly accepts: {}:{}:{}",
                    decl.getChildType(), decl.getChildSubType(), decl.getChildName());
            }

            log.info("object.pojo inherited acceptsChildren: {} declarations", objectPojo.getInheritedAcceptsChildren().size());
            for (AcceptsChildrenDeclaration decl : objectPojo.getInheritedAcceptsChildren()) {
                log.info("  - object.pojo inherited accepts: {}:{}:{}",
                    decl.getChildType(), decl.getChildSubType(), decl.getChildName());
            }

            log.info("object.pojo ALL acceptsChildren: {} declarations", objectPojo.getAcceptsChildren().size());
            for (AcceptsChildrenDeclaration decl : objectPojo.getAcceptsChildren()) {
                log.info("  - object.pojo ALL accepts: {}:{}:{}",
                    decl.getChildType(), decl.getChildSubType(), decl.getChildName());
            }
        }

        // Check field.string definition for comparison
        TypeDefinition fieldString = registry.getTypeDefinition("field", "string");
        log.info("field.string definition: {}", fieldString);

        if (fieldString != null) {
            log.info("field.string has parent: {}, parent type: {}", fieldString.hasParent(), fieldString.getParentQualifiedName());
            log.info("field.string accepts parents: {} declarations", fieldString.getAcceptsParents().size());
            for (AcceptsParentsDeclaration decl : fieldString.getAcceptsParents()) {
                log.info("  - field.string accepts parent: {}:{}:{}",
                    decl.getParentType(), decl.getParentSubType(), decl.getExpectedChildName());
            }
        }

        // Test the bidirectional constraint evaluation
        log.info("=== TESTING BIDIRECTIONAL CONSTRAINT ===");
        ConstraintEnforcer enforcer = ConstraintEnforcer.getInstance();
        ConstraintFlattener flattener = enforcer.getConstraintFlattener();

        boolean objectPojoAcceptsFieldString = flattener.isPlacementAllowed("object", "pojo", "field", "string", "validName");
        log.info("ConstraintFlattener.isPlacementAllowed('object', 'pojo', 'field', 'string', 'validName') = {}", objectPojoAcceptsFieldString);

        boolean objectBaseAcceptsFieldString = flattener.isPlacementAllowed("object", "base", "field", "string", "validName");
        log.info("ConstraintFlattener.isPlacementAllowed('object', 'base', 'field', 'string', 'validName') = {}", objectBaseAcceptsFieldString);

        // Also test the raw constraint enforcer
        log.info("=== TESTING CONSTRAINT ENFORCER DIRECTLY ===");
        try {
            PojoMetaObject testObject = new PojoMetaObject("testObject");
            StringField testField = new StringField("validName");

            log.info("About to call enforcer.enforceConstraintsOnAddChild with object.pojo and field.string");
            enforcer.enforceConstraintsOnAddChild(testObject, testField);
            log.info("ConstraintEnforcer.enforceConstraintsOnAddChild succeeded!");

        } catch (Exception e) {
            log.error("ConstraintEnforcer.enforceConstraintsOnAddChild failed: {}", e.getMessage());
            log.error("Exception details: ", e);
        }
    }
}