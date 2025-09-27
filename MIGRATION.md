# Migration Guide: v5.1.x → v6.2.5+

This guide helps existing MetaObjects users migrate to the new **metaobjects.com architecture** and **modular structure** introduced in v6.2.5+.

## 🎯 **Migration Overview**

The v6.2.5+ release represents a **major architectural enhancement** with comprehensive package refactoring and modular structure. The primary changes are package refactoring and breaking the monolithic structure into focused modules.

### **What Changed**
- **Package Structure**: `com.draagon.meta.*` → `com.metaobjects.*`
- **Group ID**: `com.draagon` → `com.metaobjects`
- **Domain**: Moved to `metaobjects.com` and `metaobjects.dev`
- **Monolithic JAR** → **Focused modules**
- **Single dependency** → **Choose what you need**
- **Framework coupling** → **Optional integrations**

### **What Stayed the Same**
- ✅ **All APIs remain functionally identical** (just new packages)
- ✅ **Metadata formats unchanged**
- ✅ **Functionality preserved**
- ✅ **Configuration compatible**

## 🔄 **Dependency Migration**

### **Before (v5.1.x)**
```xml
<dependency>
    <groupId>com.draagon</groupId>
    <artifactId>metaobjects</artifactId>
    <version>5.1.0</version>
</dependency>
```

### **After (v6.2.5+) - Choose Your Modules**

#### **Basic Usage (Core Only)**
```xml
<dependency>
    <groupId>com.metaobjects</groupId>
    <artifactId>metaobjects-core</artifactId>
    <version>6.2.5-SNAPSHOT</version>
</dependency>
```

#### **Spring Projects**
```xml
<dependency>
    <groupId>com.metaobjects</groupId>
    <artifactId>metaobjects-core-spring</artifactId>
    <version>6.2.5-SNAPSHOT</version>
</dependency>
```
*Note: Automatically includes `metaobjects-core`*

#### **Code Generation**
```xml
<dependency>
    <groupId>com.metaobjects</groupId>
    <artifactId>metaobjects-codegen-mustache</artifactId>
    <version>6.2.5-SNAPSHOT</version>
</dependency>
```

#### **Database Object Management**
```xml
<dependency>
    <groupId>com.metaobjects</groupId>
    <artifactId>metaobjects-omdb</artifactId>
    <version>6.2.5-SNAPSHOT</version>
</dependency>
```

#### **Complete Feature Set (Web + Spring)**
```xml
<dependency>
    <groupId>com.metaobjects</groupId>
    <artifactId>metaobjects-web-spring</artifactId>
    <version>6.2.5-SNAPSHOT</version>
</dependency>
```

## 📦 **Migration Scenarios**

### **Scenario 1: Basic MetaObjects Usage**

**Before:**
```java
// Using old packages
import com.draagon.meta.loader.simple.SimpleLoader;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.object.value.ValueObject;

SimpleLoader loader = new SimpleLoader("myLoader");
// ... rest of code unchanged
```

**After:**
```java
// Updated to new packages
import com.metaobjects.loader.simple.SimpleLoader;
import com.metaobjects.object.MetaObject;
import com.metaobjects.object.value.ValueObject;

SimpleLoader loader = new SimpleLoader("myLoader");
// ... rest of code remains the same
```
- **Dependency**: Change to `metaobjects-core` with new groupId
- **Code**: Update import statements to new package structure

### **Scenario 2: Spring Integration**

**Before:**
```java
// If you were using Spring features
@Autowired
private MetaDataLoader loader;
```

**After:**
- **Dependency**: Change to `metaobjects-core-spring`
- **Code**: **No changes required** (even better Spring integration available)
- **New Features**: Access to `MetaDataService`, auto-configuration

### **Scenario 3: Code Generation**

**Before:**
```xml
<plugin>
    <groupId>com.draagon</groupId>
    <artifactId>metaobjects-maven-plugin</artifactId>
    <version>5.1.0</version>
</plugin>
```

**After:**
```xml
<plugin>
    <groupId>com.metaobjects</groupId>
    <artifactId>metaobjects-maven-plugin</artifactId>
    <version>6.2.5-SNAPSHOT</version>
</plugin>
```
- **Plugin usage**: **Identical**
- **Templates**: **Fully compatible**

### **Scenario 4: Web/React Components**

**Before:**
```typescript
// React MetaView components
import { MetaViewRenderer } from './metaviews/MetaViewRenderer';
```

**After:**
- **Dependency**: Add `metaobjects-web` or `metaobjects-web-spring`
- **Code**: **No changes required**

## 🔧 **Build Tool Migration**

### **Maven Projects**

#### **Update Parent POM (if using)**
```xml
<parent>
    <groupId>com.metaobjects</groupId>
    <artifactId>metaobjects</artifactId>
    <version>6.2.5-SNAPSHOT</version>
</parent>
```

#### **Update Plugin Versions**
```xml
<plugin>
    <groupId>com.metaobjects</groupId>
    <artifactId>metaobjects-maven-plugin</artifactId>
    <version>6.2.5-SNAPSHOT</version>
</plugin>
```

### **Gradle Projects**

#### **Before**
```gradle
implementation 'com.draagon:metaobjects:5.1.0'
```

#### **After**
```gradle
implementation 'com.metaobjects:metaobjects-core:6.2.5-SNAPSHOT'
// Add additional modules as needed
implementation 'com.metaobjects:metaobjects-core-spring:6.2.5-SNAPSHOT'
```

## 🎁 **Enhanced Features Available**

### **New Spring Integration**
```java
// New optional convenience features
@Autowired
private MetaDataService metaDataService;

// Improved API with Optional support
Optional<MetaObject> userMeta = metaDataService.findMetaObjectByNameOptional("User");
boolean hasUser = metaDataService.metaObjectExists("User");
```

### **Enhanced Examples**
```bash
# Try the new comprehensive examples
cd examples/basic-example && mvn compile exec:java
cd examples/spring-example && mvn compile exec:java
cd examples/osgi-example && mvn compile exec:java
```

### **Better Error Messages**
The new modular architecture provides clearer error messages and better debugging information.

## ⚡ **Performance Improvements**

### **Reduced Dependencies**
- **Smaller JARs**: Include only needed functionality
- **Faster startup**: No unused framework loading
- **Better caching**: Optimized for specific use cases

### **OSGi Enhancements**
- **Improved bundle lifecycle** management
- **Better WeakReference** cleanup patterns
- **Optimized service discovery**

## ⚠️ **Breaking Changes (Rare)**

### **Internal API Changes**
If you were using internal APIs (rare), some package restructuring may require import updates:

```java
// If you were using these internal APIs (uncommon)
// Before: com.metaobjects.generator.direct.*
// After:   com.metaobjects.generator.mustache.*

// Most users won't be affected
```

### **OSGi Bundle Names**
OSGi bundle symbolic names have changed to reflect the new module structure:
- `com.metaobjectsobjects` → `com.metaobjectsobjects.core`
- Add new bundles: `com.metaobjectsobjects.core.spring`, etc.

## 🧪 **Testing Your Migration**

### **Verification Steps**
1. **Update dependencies** to new modules
2. **Clean build**: `mvn clean compile`
3. **Run tests**: `mvn test`
4. **Test functionality**: Verify your features work identically

### **Common Issues & Solutions**

#### **Issue: ClassNotFoundException**
**Solution**: Add the appropriate module dependency

#### **Issue: Spring beans not found**
**Solution**: Switch to `metaobjects-core-spring` for auto-configuration

#### **Issue: Maven plugin not found**
**Solution**: Update plugin version to 6.2.5-SNAPSHOT and groupId to com.metaobjects

## 📞 **Support**

### **Migration Help**
- **Examples**: Check `examples/` for patterns matching your use case
- **Documentation**: See README.md for comprehensive module guide
- **Issues**: Report migration problems at [GitHub Issues](https://github.com/draagon/metaobjects/issues)

### **Recommended Migration Path**
1. **Start simple**: Migrate to `metaobjects-core` first
2. **Add integrations**: Add Spring, web, or other modules as needed
3. **Test thoroughly**: Verify all functionality before deployment
4. **Optimize**: Remove unused dependencies after migration complete

---

**The modular architecture provides the same powerful MetaObjects functionality with better dependency management and enhanced framework integration options.**