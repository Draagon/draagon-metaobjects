package com.draagon.meta.generator.direct.object;

import com.draagon.meta.field.MetaField;
import com.draagon.meta.generator.GeneratorIOException;
import com.draagon.meta.generator.direct.FileDirectWriter;
import com.draagon.meta.generator.direct.CodeFragment;
import com.draagon.meta.generator.direct.GenerationContext;
import com.draagon.meta.generator.direct.GenerationPlugin;
import com.draagon.meta.generator.direct.BaseGenerationPlugin;
import com.draagon.meta.generator.util.GeneratorUtil;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.util.MetaDataUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.util.*;

/**
 * Base class for Object Code Writers that support language-agnostic object generation.
 * This provides common functionality for writing code from MetaObjects while allowing
 * language-specific implementations to customize syntax and conventions.
 */
public abstract class BaseObjectCodeWriter extends FileDirectWriter<BaseObjectCodeWriter> {

    private static final Logger log = LoggerFactory.getLogger(BaseObjectCodeWriter.class);

    // Language-specific attribute constants should be defined in subclasses

    protected boolean debug = false;
    protected GenerationContext context;
    protected Collection<MetaObject> filteredObjects;

    // Configuration properties (language-agnostic)
    protected String type = "interface";
    protected String pkgPrefix="";
    protected String pkgSuffix="";
    protected String namePrefix="";
    protected String nameSuffix="";
    protected boolean addArrays=false;
    protected boolean addKeys=false;

    // Generation State Variables
    protected MetaObject metaObject = null;
    protected String pkg = null;
    protected String name = null;
    protected String superPkg = null;
    protected String superName = null;
    protected String fullSuperName = null;
    protected MetaObject superObject = null;

    protected Map<MetaField,MetaObject> objectReferenceMap = new HashMap<>();
    protected List<String> importList = new ArrayList<>();
    protected Map<MetaObject,String> pkgPrefixMap = new HashMap<>();

    //////////////////////////////////////////////////////////////////////
    // Abstract methods for language-specific implementation

    /**
     * Get the language-specific type mapping for a MetaField
     */
    protected abstract String getLanguageType(MetaField field);
    
    /**
     * Get the language-specific getter method name for a field
     */
    protected abstract String getGetterMethodName(MetaField field);
    
    /**
     * Get the language-specific setter method name for a field
     */
    protected abstract String getSetterMethodName(MetaField field);
    
    /**
     * Get the language-specific parameter name for a field
     */
    protected abstract String getParameterName(MetaField field);
    
    /**
     * Get the language-specific class/type name for a MetaObject
     */
    protected abstract String getClassName(MetaObject mo);
    
    /**
     * Write language-specific getter method
     */
    protected abstract void writeGetter(String getterName, String typeName, MetaField field);
    
    /**
     * Write language-specific setter method  
     */
    protected abstract void writeSetter(String setterName, String paramName, String typeName, MetaField field);
    
    /**
     * Write language-specific object header (class/interface declaration)
     */
    protected abstract void writeObjectHeader(List<String> docs, String pkg, String name, List<String> importList, String fullSuperName);
    
    /**
     * Write language-specific object footer
     */
    protected abstract void writeObjectFooter();
    
    /**
     * Write language-specific comment
     */
    protected abstract void writeComment(String comment);
    
    /**
     * Write language-specific new line
     */
    protected abstract void writeNewLine();
    
    /**
     * Get the language-specific package name from a MetaObject
     */
    protected abstract String getLanguagePackage(MetaObject mo);
    
    /**
     * Get the language-specific attribute name for custom field names
     */
    protected abstract String getLanguageNameAttribute();

    //////////////////////////////////////////////////////////////////////
    // Constructors

    public BaseObjectCodeWriter(MetaDataLoader loader, PrintWriter pw) {
        super(loader, pw);
        this.context = new GenerationContext(loader);
    }
    
    public BaseObjectCodeWriter(MetaDataLoader loader, PrintWriter pw, GenerationContext context) {
        super(loader, pw);
        this.context = context;
    }

    //////////////////////////////////////////////////////////////////////
    // Configuration Methods (fluent interface)

    public BaseObjectCodeWriter forType(String type) {
        this.type = type;
        context.setProperty("generator.type", type);
        return this;
    }

    public BaseObjectCodeWriter withPkgPrefix(String pkgPrefix) {
        this.pkgPrefix = pkgPrefix;
        context.setProperty("package.prefix", pkgPrefix);
        return this;
    }

    public BaseObjectCodeWriter withPkgSuffix(String pkgSuffix) {
        this.pkgSuffix = pkgSuffix;
        context.setProperty("package.suffix", pkgSuffix);
        return this;
    }

    public BaseObjectCodeWriter withNamePrefix(String namePrefix) {
        this.namePrefix = namePrefix;
        context.setProperty("name.prefix", namePrefix);
        return this;
    }

    public BaseObjectCodeWriter withNameSuffix(String nameSuffix) {
        this.nameSuffix = nameSuffix;
        context.setProperty("name.suffix", nameSuffix);
        return this;
    }

    public BaseObjectCodeWriter addArrayMethods(boolean addArrays) {
        this.addArrays = addArrays;
        context.setProperty("generate.arrayMethods", addArrays);
        return this;
    }

    public BaseObjectCodeWriter addKeyMethods(boolean addKeys) {
        this.addKeys = addKeys;
        context.setProperty("generate.keyMethods", addKeys);
        return this;
    }
    
    public BaseObjectCodeWriter withContext(GenerationContext context) {
        this.context = context;
        return this;
    }
    
    public BaseObjectCodeWriter addPlugin(GenerationPlugin plugin) {
        context.addPlugin(plugin);
        return this;
    }
    
    public GenerationContext getContext() {
        return context;
    }

    //////////////////////////////////////////////////////////////////////
    // Main Generation Method

    public String writeObject(MetaObject mo) throws GeneratorIOException {
        initVariables(mo);
        
        // Set up context
        context.setCurrentObject(mo)
               .setCurrentPackage(pkg)
               .setCurrentClassName(name);

        try {
            // Initialize object reference mappings
            initObjectReferenceMap(mo);
            initPackagePrefixMap(objectReferenceMap.values());
            initImportList(objectReferenceMap.values());
            
            // Let plugins contribute imports
            for (BaseGenerationPlugin<MetaObject> plugin : context.getPlugins()) {
                plugin.contributeImports(mo, context);
            }
            
            // Merge context imports with local imports
            importList.addAll(context.getImports());
            
            // Generate header documentation
            List<String> docs = Arrays.asList(
                    "ObjectCodeWriter:         " + getClass().getName(),
                    "MetaObject:               " + mo.getName(),
                    "SuperObject:              " + (superObject != null ? superObject.getName() : ""),
                    "MetaDataLoader:           " + getLoader().toString(),
                    "Generated On:             " + (new Date()).toString(),
                    "Plugins:                  " + getPluginNames()
            );
            
            // Notify plugins before generation starts
            for (BaseGenerationPlugin<MetaObject> plugin : context.getPlugins()) {
                plugin.beforeItemGeneration(mo, context, this);
            }
            
            writeObjectHeader(docs, pkg, name, importList, fullSuperName);

            writeObjectMethods(mo);

            writeObjectFooter();
            
            // Notify plugins after generation completes
            for (BaseGenerationPlugin<MetaObject> plugin : context.getPlugins()) {
                plugin.afterItemGeneration(mo, context, this);
            }

            return (pkg != null && !pkg.isEmpty()) ? pkg + "." + name : name;
        }
        catch(Exception e) {
            throw new GeneratorIOException(this, "Error writing Object Code: " + e, e);
        }
    }
    
    private String getPluginNames() {
        return context.getPlugins().stream()
                .map(BaseGenerationPlugin::getName)
                .reduce((a, b) -> a + ", " + b)
                .orElse("none");
    }

    protected void writeObjectMethods(MetaObject mo) {
        inc();

        for (MetaField mf : mo.getMetaFields(false)) {
            context.setCurrentField(mf);
            
            // Notify plugins before field generation
            for (BaseGenerationPlugin<MetaObject> plugin : context.getPlugins()) {
                if (plugin instanceof GenerationPlugin) {
                    ((GenerationPlugin) plugin).beforeFieldGeneration(mf, context, this);
                }
            }

            // Check if we should generate getters/setters (plugins might disable this)
            boolean generateGetters = context.getBooleanProperty("generate.getters", true);
            boolean generateSetters = context.getBooleanProperty("generate.setters", true);

            if (generateGetters || generateSetters) {
                String getterName = getGetterMethodName(mf);
                String setterName = getSetterMethodName(mf);
                String paramName = getParameterName(mf);
                String typeName = getLanguageType(mf);

                // Allow plugins to customize method names and types
                for (BaseGenerationPlugin<MetaObject> plugin : context.getPlugins()) {
                    if (plugin instanceof GenerationPlugin) {
                        GenerationPlugin genPlugin = (GenerationPlugin) plugin;
                        getterName = genPlugin.customizeMethodName(mf, "getter", getterName, context);
                        setterName = genPlugin.customizeMethodName(mf, "setter", setterName, context);
                        typeName = genPlugin.customizeFieldType(mf, typeName, context);
                    }
                }

                writeNewLine();
                writeComment("Methods for MetaField: " + mf.getName());
                writeNewLine();
                
                if (generateGetters) {
                    writeGetter(getterName, typeName, mf);
                }
                
                if (generateSetters) {
                    writeSetter(setterName, paramName, typeName, mf);
                }
            }
            
            // Notify plugins after field generation
            for (BaseGenerationPlugin<MetaObject> plugin : context.getPlugins()) {
                if (plugin instanceof GenerationPlugin) {
                    ((GenerationPlugin) plugin).afterFieldGeneration(mf, context, this);
                }
            }
        }

        dec();
    }

    //////////////////////////////////////////////////////////////////////
    // Utility Methods

    protected void initVariables(MetaObject mo) {
        metaObject = mo;
        pkg = getLanguagePackage(mo);
        name = getClassName(mo);

        if (hasSuperObject(mo)) {
            superObject = mo.getSuperObject();
            superPkg = getLanguagePackage(superObject);
            superName = getClassName(superObject);

            if (!pkg.equals(superPkg)) {
                fullSuperName = superPkg + "." + superName;
            } else {
                fullSuperName = superName;
            }
        } else {
            superObject = null;
            superPkg = null;
            superName = null;
            fullSuperName = null;
        }
    }
    
    protected void initObjectReferenceMap(MetaObject mo) {
        for (MetaField mf : mo.getMetaFields(false)) {
            if (MetaDataUtil.hasObjectRef(mf)) {
                objectReferenceMap.put(mf, MetaDataUtil.getObjectRef(mf));
            }
        }
    }

    protected void initPackagePrefixMap(Collection<MetaObject> refObjects) {
        if (superObject != null) {
            addPackagePrefixToMap(superObject);
        }

        for (MetaObject refmo : refObjects) {
            addPackagePrefixToMap(refmo);
        }
    }

    protected void initImportList(Collection<MetaObject> refObjects) {
        if (superObject != null) {
            addImportToList(superObject);
        }

        for (MetaObject refmo : refObjects) {
            addImportToList(refmo);
        }
    }

    private boolean hasSuperObject(MetaObject mo) {
        return mo.getSuperData() != null && mo.getSuperData() instanceof MetaObject;
    }

    protected void addPackagePrefixToMap(MetaObject refmo) {
        String p = getLanguagePackage(refmo);
        String n = getClassName(refmo);
        if (!pkg.equals(p) && name.equals(n)) {
            pkgPrefixMap.put(refmo,p+".");
        } else {
            pkgPrefixMap.put(refmo,"");
        }
    }

    private void addImportToList(MetaObject mo) {
        String p = getLanguagePackage(mo);
        String n = getClassName(mo);
        if (!pkg.equals(p) &&
                !name.equals(n)
                && !importList.contains(p)) {
            importList.add(p+"."+n);
        }
    }

    // Package handling method moved to language-specific implementations

    protected boolean isNotBlank(String str) {
        return str != null && !str.trim().isEmpty();
    }
}