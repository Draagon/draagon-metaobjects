# AI-Optimized Type System Refactoring Plan

## 🎯 **Project Overview**

**Objective**: Consolidate fragmented field and attribute types into AI-optimized semantic categories to dramatically improve code generation across Java, C#, TypeScript, and Node.js.

**Priority**: #1 Ease of AI code generation, #2 Cross-language compatibility
**Timeline**: Before TypeScript/C# versions are implemented
**Breaking Changes**: Acceptable (major version)

## 🔍 **Current Problems**

### Field Type Fragmentation
- **Current**: 10+ specific types (StringField, IntegerField, LongField, DoubleField, FloatField, BooleanField, DateField, ShortField, ByteField, StringArrayField, ObjectArrayField)
- **Problem**: AI must handle 10+ decision branches, language-specific types that don't exist in all targets

### Attribute Type Duplication
- **Current**: Parallel fragmentation (StringAttribute, IntAttribute, LongAttribute, DoubleAttribute, StringArrayAttribute)
- **Problem**: Same fragmentation issues, inconsistent with field types

### Cross-Language Compatibility Issues
- **Java/C#**: Distinguish int/long/float/double precisely
- **TypeScript/Node.js**: Only has "number" type
- **Current**: Templates must handle these differences manually

## ✅ **Architecture Decisions**

### Type Consolidation Strategy
- **Numeric Consolidation**: int, long, float, double → `numeric` with `@primitiveType` attribute
- **Array Strategy**: `stringArray`, `numericArray` subtypes (not `@isArray` modifier)
- **Naming**: Keep familiar terms (`string` not `text`, `object` not `reference`)
- **Consistency**: Same type system for both MetaField and MetaAttribute

### Target Type Categories
1. **`numeric`** - All number types with `@primitiveType` specification
2. **`string`** - Text data (unchanged)
3. **`boolean`** - True/false (unchanged)
4. **`object`** - Object references (unchanged)
5. **`temporal`** - Date/time values (consolidates date/timestamp)
6. **`binary`** - Byte arrays (new)

### Array Support
- **Field Arrays**: `numericArray`, `stringArray`, `objectArray`, `temporalArray`, `binaryArray`
- **Attribute Arrays**: Same pattern for MetaAttribute classes

## 📋 **Phase Breakdown**

---

## **PHASE 1: Numeric Type Foundation** ⭐ *START HERE*

**Scope**: Implement core numeric consolidation with array support
**Duration**: 1-2 chat sessions
**Files to Create/Modify**: ~12 files

### 1.1 Core Type Implementation

#### Create New Classes:

**`NumericField.java`**
```java
public class NumericField extends PrimitiveField<Object> {
    public static final String SUBTYPE_NUMERIC = "numeric";
    public static final String ATTR_PRIMITIVE_TYPE = "primitiveType";

    // Supported primitiveType values:
    // "int32", "int64", "float32", "float64", "decimal"

    public static void registerTypes(MetaDataRegistry registry) {
        registry.registerType(NumericField.class, def -> def
            .type(TYPE_FIELD).subType(SUBTYPE_NUMERIC)
            .description("Unified numeric field supporting int32, int64, float32, float64, decimal")
            .inheritsFrom(TYPE_FIELD, SUBTYPE_BASE)
            .optionalAttribute(ATTR_PRIMITIVE_TYPE, "string")
            .optionalAttribute("precision", "string") // For decimal: "10,2"
        );
    }
}
```

**`NumericArrayField.java`**
```java
public class NumericArrayField extends ArrayField<Object,List<Object>> {
    public static final String SUBTYPE_NUMERIC_ARRAY = "numericArray";
    public static final String ATTR_PRIMITIVE_TYPE = "primitiveType";

    // Same primitiveType support as NumericField
}
```

**`NumericAttribute.java`**
```java
public class NumericAttribute extends MetaAttribute<Object> {
    public static final String SUBTYPE_NUMERIC = "numeric";
    public static final String ATTR_PRIMITIVE_TYPE = "primitiveType";

    // Same primitiveType support as NumericField
}
```

**`NumericArrayAttribute.java`**
```java
public class NumericArrayAttribute extends MetaAttribute<List<Object>> {
    public static final String SUBTYPE_NUMERIC_ARRAY = "numericArray";
    public static final String ATTR_PRIMITIVE_TYPE = "primitiveType";
}
```

### 1.2 Provider Registration

**Update `FieldTypesMetaDataProvider.java`**
```java
@Override
public void registerTypes(MetaDataRegistry registry) {
    // NEW: Consolidated numeric types
    NumericField.registerTypes(registry);
    NumericArrayField.registerTypes(registry);

    // EXISTING: Keep current types during transition
    StringField.registerTypes(registry);
    BooleanField.registerTypes(registry);
    // ... others
}
```

**Update `AttributeTypesMetaDataProvider.java`**
```java
@Override
public void registerTypes(MetaDataRegistry registry) {
    // NEW: Consolidated numeric attributes
    NumericAttribute.registerTypes(registry);
    NumericArrayAttribute.registerTypes(registry);

    // EXISTING: Keep current types during transition
    StringAttribute.registerTypes(registry);
    BooleanAttribute.registerTypes(registry);
    // ... others
}
```

### 1.3 Database Integration

**Update `SimpleMappingHandlerDB.java`**
```java
protected int getSQLType(MetaField mf) {
    if (mf instanceof NumericField || mf instanceof NumericArrayField) {
        String primitiveType = getPrimitiveType(mf);
        switch (primitiveType) {
            case "int32": return Types.INTEGER;
            case "int64": return Types.BIGINT;
            case "float32": return Types.FLOAT;
            case "float64": return Types.DOUBLE;
            case "decimal": return Types.DECIMAL;
            default: return Types.INTEGER; // fallback
        }
    }

    // ... existing logic for other types
}

private String getPrimitiveType(MetaField mf) {
    if (mf.hasMetaAttr(NumericField.ATTR_PRIMITIVE_TYPE)) {
        return mf.getMetaAttr(NumericField.ATTR_PRIMITIVE_TYPE).getValueAsString();
    }
    return "int32"; // default
}
```

### 1.4 Testing

**Create Test Files**:
- `NumericFieldTest.java` - Basic functionality, primitiveType handling
- `NumericArrayFieldTest.java` - Array operations, type safety
- `NumericAttributeTest.java` - Attribute-specific behavior
- `NumericArrayAttributeTest.java` - Array attribute behavior

**Test Metadata Files**:
```json
{
  "metadata": {
    "package": "test::numeric",
    "children": [
      {
        "field": {
          "name": "count",
          "subType": "numeric",
          "@primitiveType": "int32"
        }
      },
      {
        "field": {
          "name": "price",
          "subType": "numeric",
          "@primitiveType": "decimal",
          "@precision": "10,2"
        }
      },
      {
        "field": {
          "name": "scores",
          "subType": "numericArray",
          "@primitiveType": "float32"
        }
      }
    ]
  }
}
```

### 1.5 Success Criteria - Phase 1
- ✅ NumericField/NumericAttribute classes implemented with full primitiveType support
- ✅ Array versions working correctly
- ✅ Provider-based registration complete
- ✅ Database mapping functional for all primitive types
- ✅ Test coverage for core scenarios
- ✅ Build successful with all modules

---

## **PHASE 2: Code Generation Enhancement**

**Scope**: Update templates and helpers to use numeric types
**Duration**: 1 chat session
**Files to Modify**: ~8 files

### 2.1 Helper Registry Updates

**Update `HelperRegistry.java`**
```java
// NEW: Numeric type helpers
register("isNumericField", this::isNumericField);
register("isNumericArrayField", this::isNumericArrayField);
register("getPrimitiveType", this::getPrimitiveType);
register("getLanguageType", this::getLanguageType);

private Object isNumericField(Object input) {
    return input instanceof NumericField;
}

private Object isNumericArrayField(Object input) {
    return input instanceof NumericArrayField;
}

private Object getPrimitiveType(Object input) {
    if (input instanceof NumericField || input instanceof NumericArrayField) {
        MetaField field = (MetaField) input;
        if (field.hasMetaAttr(NumericField.ATTR_PRIMITIVE_TYPE)) {
            return field.getMetaAttr(NumericField.ATTR_PRIMITIVE_TYPE).getValueAsString();
        }
    }
    return "int32"; // default
}

private Object getLanguageType(Object input, String language) {
    if (isNumericField(input) || isNumericArrayField(input)) {
        String primitiveType = (String) getPrimitiveType(input);
        boolean isArray = isNumericArrayField(input);

        switch (language) {
            case "java":
                return mapToJavaType(primitiveType, isArray);
            case "typescript":
                return isArray ? "number[]" : "number";
            case "csharp":
                return mapToCSharpType(primitiveType, isArray);
        }
    }
    // ... handle other field types
}

private String mapToJavaType(String primitiveType, boolean isArray) {
    String baseType;
    switch (primitiveType) {
        case "int32": baseType = "Integer"; break;
        case "int64": baseType = "Long"; break;
        case "float32": baseType = "Float"; break;
        case "float64": baseType = "Double"; break;
        case "decimal": baseType = "BigDecimal"; break;
        default: baseType = "Integer";
    }
    return isArray ? baseType + "[]" : baseType;
}

private String mapToCSharpType(String primitiveType, boolean isArray) {
    String baseType;
    switch (primitiveType) {
        case "int32": baseType = "int"; break;
        case "int64": baseType = "long"; break;
        case "float32": baseType = "float"; break;
        case "float64": baseType = "double"; break;
        case "decimal": baseType = "decimal"; break;
        default: baseType = "int";
    }
    return isArray ? baseType + "[]" : baseType;
}
```

### 2.2 Template Updates

**Update Mustache Templates**:

**Java Templates** (`*.java.vm`):
```java
{{#isNumericField}}
private {{getLanguageType this "java"}} {{name}};
{{/isNumericField}}

{{#isNumericArrayField}}
private {{getLanguageType this "java"}} {{name}};
{{/isNumericArrayField}}
```

**TypeScript Templates** (`*.ts.vm`):
```typescript
{{#isNumericField}}
{{name}}: {{getLanguageType this "typescript"}};
{{/isNumericField}}

{{#isNumericArrayField}}
{{name}}: {{getLanguageType this "typescript"}};
{{/isNumericArrayField}}
```

### 2.3 Success Criteria - Phase 2
- ✅ Helper methods correctly identify numeric types
- ✅ Language mapping works for Java, TypeScript, C#
- ✅ Templates generate correct code for all primitive types
- ✅ Array handling works in all languages
- ✅ Generated code compiles successfully

---

## **PHASE 3: Remaining Type Consolidation**

**Scope**: Extend consolidation pattern to other type families
**Duration**: 2-3 chat sessions
**Files to Create**: ~15 files

### 3.1 Temporal Type Consolidation

**Create Classes**:
- `TemporalField.java` - Consolidates DateField, TimestampField
- `TemporalArrayField.java` - Array support
- `TemporalAttribute.java` - Attribute version
- `TemporalArrayAttribute.java` - Array attribute version

**Temporal Types**:
- `@temporalType`: "date", "time", "datetime", "timestamp"
- `@timezone`: "utc", "local"

### 3.2 Binary Type Introduction

**Create Classes**:
- `BinaryField.java` - For byte arrays, files
- `BinaryArrayField.java` - Array support
- `BinaryAttribute.java` - Attribute version
- `BinaryArrayAttribute.java` - Array attribute version

**Binary Types**:
- `@mimeType`: "image/jpeg", "application/pdf", "text/plain"
- `@maxSize`: "1048576" (1MB)

### 3.3 Object Type Enhancement

**Enhance Existing**:
- `ObjectField.java` - Keep but enhance with better `@objectRef` support
- `ObjectArrayField.java` - Keep current functionality
- Add `ObjectAttribute.java` if needed for consistency

### 3.4 Success Criteria - Phase 3
- ✅ All 6 core types implemented (numeric, string, boolean, object, temporal, binary)
- ✅ Array versions for all types
- ✅ Consistent attribute/field pattern across all types
- ✅ Database mapping functional for new types
- ✅ Code generation working for all types

---

## **PHASE 4: Database Layer Complete Integration**

**Scope**: Full database mapping for all new types
**Duration**: 1 chat session
**Files to Modify**: ~5 files

### 4.1 Complete SimpleMappingHandlerDB

**Add Support For**:
- TemporalField → appropriate SQL date/time types
- BinaryField → BLOB/VARBINARY types
- Array types → database-specific array handling

### 4.2 Database Driver Updates

**Extend Database Drivers** for new types:
- DerbyDriver, PostgresDriver, MySQLDriver, etc.
- Handle array storage strategies (native arrays vs JSON vs separate tables)

### 4.3 Success Criteria - Phase 4
- ✅ All semantic types map correctly to database types
- ✅ Array storage working (prefer native array support where available)
- ✅ Database schema generation handles all new types
- ✅ ObjectManagerDB integration tests pass

---

## **PHASE 5: Legacy Cleanup & Documentation**

**Scope**: Remove old types, create migration documentation
**Duration**: 1-2 chat sessions
**Files to Remove**: ~15 files

### 5.1 Legacy Type Removal

**Remove Old Classes**:
- IntegerField, LongField, DoubleField, FloatField → Replaced by NumericField
- DateField, TimestampField → Replaced by TemporalField
- IntAttribute, LongAttribute, DoubleAttribute → Replaced by NumericAttribute
- StringArrayField → Replaced by StringArrayField (might keep if different from StringField)

### 5.2 Provider Cleanup

**Update Providers** to remove old type registrations:
- Remove from FieldTypesMetaDataProvider
- Remove from AttributeTypesMetaDataProvider
- Clean up any META-INF/services references

### 5.3 Migration Documentation

**Create Documentation**:
- `TYPE_MIGRATION_GUIDE.md` - How to migrate existing metadata
- Update main README with new type system
- Update API documentation with new patterns

### 5.4 Success Criteria - Phase 5
- ✅ All legacy types removed from codebase
- ✅ Clean build with no references to old types
- ✅ Migration documentation complete
- ✅ All tests passing with new type system only

---

## **PHASE 6: Cross-Language Validation**

**Scope**: Ensure templates work perfectly across all target languages
**Duration**: 1 chat session
**Files to Test**: Templates and generated code

### 6.1 Template Validation

**Test Generation For**:
- Java code generation with all 6 semantic types
- TypeScript code generation with proper type mapping
- C# code generation (when available)

### 6.2 Cross-Language Consistency

**Validate**:
- Same metadata generates appropriate types in each language
- Array handling consistent across languages
- Database mapping works regardless of target language

### 6.3 Success Criteria - Phase 6
- ✅ Generated Java code compiles and runs
- ✅ Generated TypeScript code type-checks correctly
- ✅ Same metadata works across all target languages
- ✅ AI code generation significantly simplified

---

## 🔧 **Implementation Guidelines**

### File Organization
```
metadata/src/main/java/com/metaobjects/
├── field/
│   ├── NumericField.java ⭐ NEW
│   ├── NumericArrayField.java ⭐ NEW
│   ├── TemporalField.java ⭐ NEW
│   ├── BinaryField.java ⭐ NEW
│   └── ... (enhanced existing)
├── attr/
│   ├── NumericAttribute.java ⭐ NEW
│   ├── NumericArrayAttribute.java ⭐ NEW
│   └── ... (enhanced existing)
```

### Testing Strategy
- **Unit Tests**: Each new type class gets comprehensive test coverage
- **Integration Tests**: Database mapping, code generation
- **Metadata Tests**: JSON parsing with new type definitions
- **Cross-Language Tests**: Generated code compilation verification

### Success Metrics
- **AI Complexity Reduction**: 10+ field types → 6 semantic categories
- **Decision Tree Simplification**: 50% reduction in template complexity
- **Cross-Language Consistency**: Same metadata works in Java, TypeScript, C#
- **Database Compatibility**: All existing ObjectManagerDB functionality preserved

---

## 📅 **Timeline Estimate**

- **Phase 1**: 2-3 hours (1-2 chat sessions)
- **Phase 2**: 1-2 hours (1 chat session)
- **Phase 3**: 3-4 hours (2-3 chat sessions)
- **Phase 4**: 1-2 hours (1 chat session)
- **Phase 5**: 2-3 hours (1-2 chat sessions)
- **Phase 6**: 1 hour (1 chat session)

**Total Estimated Time**: 10-15 hours across 7-12 chat sessions

---

## 🚨 **Critical Dependencies**

1. **Provider System**: Must maintain current provider-based registration
2. **Database Compatibility**: ObjectManagerDB must continue working
3. **OSGI Compliance**: All new types must follow OSGI patterns
4. **Thread Safety**: Maintain read-optimized architecture
5. **Constraint System**: New types must integrate with existing constraints

---

## 🎯 **Next Session Action Items**

**For Phase 1 Start**:
1. Create `NumericField.java` class
2. Create `NumericArrayField.java` class
3. Create `NumericAttribute.java` class
4. Create `NumericArrayAttribute.java` class
5. Update provider registration
6. Create basic test files
7. Verify build success

**Questions for Next Session**:
- Should we keep existing numeric types during transition or remove immediately?
- Any specific primitive type names to adjust ("int32" vs "integer")?
- Database array storage preferences (JSON, separate tables, native arrays)?

This plan provides a clear roadmap for transforming MetaObjects into an AI-optimized, cross-language metadata system while maintaining architectural integrity.