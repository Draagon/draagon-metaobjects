# Migration Guide: v5.1.x → v5.2.0+

This guide helps existing MetaObjects users migrate to the new **modular architecture** introduced in v5.2.0.

## 🎯 **Migration Overview**

The v5.2.0 release represents a **major architectural enhancement** with **full backward compatibility**. The primary change is breaking the monolithic structure into focused modules.

### **What Changed**
- **Monolithic JAR** → **Focused modules**
- **Single dependency** → **Choose what you need**
- **Framework coupling** → **Optional integrations**

### **What Stayed the Same**
- ✅ **All APIs remain identical**
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

### **After (v5.2.0+) - Choose Your Modules**

#### **Basic Usage (Core Only)**
```xml
<dependency>
    <groupId>com.draagon</groupId>
    <artifactId>metaobjects-core</artifactId>
    <version>5.2.0-SNAPSHOT</version>
</dependency>
```

#### **Spring Projects**
```xml
<dependency>
    <groupId>com.draagon</groupId>
    <artifactId>metaobjects-core-spring</artifactId>
    <version>5.2.0-SNAPSHOT</version>
</dependency>
```
*Note: Automatically includes `metaobjects-core`*

#### **Code Generation**
```xml
<dependency>
    <groupId>com.draagon</groupId>
    <artifactId>metaobjects-codegen-mustache</artifactId>
    <version>5.2.0-SNAPSHOT</version>
</dependency>
```

#### **Database Object Management**
```xml
<dependency>
    <groupId>com.draagon</groupId>
    <artifactId>metaobjects-omdb</artifactId>
    <version>5.2.0-SNAPSHOT</version>
</dependency>
```

#### **Complete Feature Set (Web + Spring)**
```xml
<dependency>
    <groupId>com.draagon</groupId>
    <artifactId>metaobjects-web-spring</artifactId>
    <version>5.2.0-SNAPSHOT</version>
</dependency>
```

## 📦 **Migration Scenarios**

### **Scenario 1: Basic MetaObjects Usage**

**Before:**
```java
// Code remains identical
import com.draagon.meta.loader.simple.SimpleLoader;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.object.value.ValueObject;

SimpleLoader loader = new SimpleLoader("myLoader");
// ... rest of code unchanged
```

**After:**
- **Dependency**: Change to `metaobjects-core`
- **Code**: **No changes required**

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
    <groupId>com.draagon</groupId>
    <artifactId>metaobjects-maven-plugin</artifactId>
    <version>5.2.0-SNAPSHOT</version>
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
    <groupId>com.draagon</groupId>
    <artifactId>metaobjects</artifactId>
    <version>5.2.0-SNAPSHOT</version>
</parent>
```

#### **Update Plugin Versions**
```xml
<plugin>
    <groupId>com.draagon</groupId>
    <artifactId>metaobjects-maven-plugin</artifactId>
    <version>5.2.0-SNAPSHOT</version>
</plugin>
```

### **Gradle Projects**

#### **Before**
```gradle
implementation 'com.draagon:metaobjects:5.1.0'
```

#### **After**
```gradle
implementation 'com.draagon:metaobjects-core:5.2.0-SNAPSHOT'
// Add additional modules as needed
implementation 'com.draagon:metaobjects-core-spring:5.2.0-SNAPSHOT'
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
// Before: com.draagon.meta.generator.direct.*
// After:   com.draagon.meta.generator.mustache.*

// Most users won't be affected
```

### **OSGi Bundle Names**
OSGi bundle symbolic names have changed to reflect the new module structure:
- `com.draagon.metaobjects` → `com.draagon.metaobjects.core`
- Add new bundles: `com.draagon.metaobjects.core.spring`, etc.

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
**Solution**: Update plugin version to 5.2.0-SNAPSHOT

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