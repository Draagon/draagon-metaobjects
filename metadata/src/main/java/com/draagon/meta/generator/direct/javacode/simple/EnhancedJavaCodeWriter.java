package com.draagon.meta.generator.direct.javacode.simple;

import com.draagon.meta.DataTypes;
import com.draagon.meta.field.MetaField;
import com.draagon.meta.generator.GeneratorIOException;
import com.draagon.meta.generator.direct.FileDirectWriter;
import com.draagon.meta.generator.direct.GenerationContext;
import com.draagon.meta.generator.direct.GenerationPlugin;
import com.draagon.meta.generator.direct.CodeFragment;
import com.draagon.meta.generator.util.GeneratorUtil;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.util.MetaDataUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.util.*;

/**
 * Enhanced version of SimpleJavaCodeWriter that uses GenerationContext and plugins
 */
public class EnhancedJavaCodeWriter extends FileDirectWriter<EnhancedJavaCodeWriter> {

    private static final Logger log = LoggerFactory.getLogger(EnhancedJavaCodeWriter.class);

    public final static String ATTR_JAVANAME ="javaName";

    protected boolean debug = false;
    protected GenerationContext context;

    protected Collection<MetaObject> filteredObjects;

    public EnhancedJavaCodeWriter(MetaDataLoader loader, PrintWriter pw) {
        super(loader, pw);
        this.context = new GenerationContext(loader);
    }
    
    public EnhancedJavaCodeWriter(MetaDataLoader loader, PrintWriter pw, GenerationContext context) {
        super(loader, pw);
        this.context = context;
    }

    //////////////////////////////////////////////////////////////////////
    // Configuration Methods

    protected String type = "interface";
    protected String pkgPrefix="";
    protected String pkgSuffix="";
    protected String namePrefix="";
    protected String nameSuffix="";
    protected boolean addArrays=false;
    protected boolean addKeys=false;

    public EnhancedJavaCodeWriter forType(String type) {
        this.type = type;
        context.setProperty("generator.type", type);
        return this;
    }

    public EnhancedJavaCodeWriter withPkgPrefix(String pkgPrefix) {
        this.pkgPrefix = pkgPrefix;
        context.setProperty("package.prefix", pkgPrefix);
        return this;
    }

    public EnhancedJavaCodeWriter withPkgSuffix(String pkgSuffix) {
        this.pkgSuffix = pkgSuffix;
        context.setProperty("package.suffix", pkgSuffix);
        return this;
    }

    public EnhancedJavaCodeWriter withNamePrefix(String namePrefix) {
        this.namePrefix = namePrefix;
        context.setProperty("name.prefix", namePrefix);
        return this;
    }

    public EnhancedJavaCodeWriter withNameSuffix(String nameSuffix) {
        this.nameSuffix = nameSuffix;
        context.setProperty("name.suffix", nameSuffix);
        return this;
    }

    public EnhancedJavaCodeWriter addArrayMethods(boolean addArrays) {
        this.addArrays = addArrays;
        context.setProperty("generate.arrayMethods", addArrays);
        return this;
    }

    public EnhancedJavaCodeWriter addKeyMethods(boolean addKeys) {
        this.addKeys = addKeys;
        context.setProperty("generate.keyMethods", addKeys);
        return this;
    }
    
    public EnhancedJavaCodeWriter withContext(GenerationContext context) {
        this.context = context;
        return this;
    }
    
    public EnhancedJavaCodeWriter addPlugin(GenerationPlugin plugin) {
        context.addPlugin(plugin);
        return this;
    }
    
    public GenerationContext getContext() {
        return context;
    }

    //////////////////////////////////////////////////////////////////////
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
            for (GenerationPlugin plugin : context.getPlugins()) {
                plugin.contributeImports(mo, context);
            }
            
            // Merge context imports with local imports
            importList.addAll(context.getImports());
            
            // Generate header documentation
            List<String> docs = Arrays.asList(
                    "Enhanced JavaCodeWriter: " + getClass().getName(),
                    "MetaObject:               " + mo.getName(),
                    "SuperObject:              " + (superObject != null ? superObject.getName() : ""),
                    "MetaDataLoader:           " + getLoader().toString(),
                    "Generated On:             " + (new Date()).toString(),
                    "Plugins:                  " + getPluginNames()
            );
            
            // Notify plugins before generation starts
            for (GenerationPlugin plugin : context.getPlugins()) {
                plugin.beforeObjectGeneration(mo, context, this);
            }
            
            drawObjectStart(docs, pkg, name, importList, fullSuperName);

            writeObjectMethods(mo);

            drawObjectEnd();
            
            // Notify plugins after generation completes
            for (GenerationPlugin plugin : context.getPlugins()) {
                plugin.afterObjectGeneration(mo, context, this);
            }

            return (pkg != null && !pkg.isEmpty()) ? pkg + "." + name : name;
        }
        catch(Exception e) {
            throw new GeneratorIOException(this, "Error writing Enhanced Java: " + e, e);
        }
    }
    
    private String getPluginNames() {
        return context.getPlugins().stream()
                .map(GenerationPlugin::getName)
                .reduce((a, b) -> a + ", " + b)
                .orElse("none");
    }

    protected void writeObjectMethods(MetaObject mo) {
        inc();

        for (MetaField mf : mo.getMetaFields(false)) {
            context.setCurrentField(mf);
            
            // Notify plugins before field generation
            for (GenerationPlugin plugin : context.getPlugins()) {
                plugin.beforeFieldGeneration(mf, context, this);
            }

            // Check if we should generate getters/setters (plugins might disable this)
            boolean generateGetters = context.getBooleanProperty("generate.getters", true);
            boolean generateSetters = context.getBooleanProperty("generate.setters", true);

            if (generateGetters || generateSetters) {
                String getterName = getGetterOrSetterName(mf, true);
                String setterName = getGetterOrSetterName(mf, false);
                String valueName = getValueName(mf);
                String valueClass = getValueClass(mf);

                // Allow plugins to customize method names and types
                for (GenerationPlugin plugin : context.getPlugins()) {
                    getterName = plugin.customizeMethodName(mf, "getter", getterName, context);
                    setterName = plugin.customizeMethodName(mf, "setter", setterName, context);
                    valueClass = plugin.customizeFieldType(mf, valueClass, context);
                }

                drawNewLine();
                drawComment("////////////////////////////////////////////////////////////////////////////////////" );
                drawComment("Methods for MetaField: " + mf.getName());
                drawNewLine();
                
                if (generateGetters) {
                    drawGetter(getterName, valueClass, mf);
                }
                
                if (generateSetters) {
                    drawSetter(setterName, valueName, valueClass, mf);
                }
            }
            
            // Notify plugins after field generation
            for (GenerationPlugin plugin : context.getPlugins()) {
                plugin.afterFieldGeneration(mf, context, this);
            }
        }

        dec();
    }

    //////////////////////////////////////////////////////////////////////
    // Drawing Methods Enhanced with Code Fragments

    protected void drawGetter(String getterName, String valueClass, MetaField field) {
        if (!type.equals("interface"))
            throw new UnsupportedOperationException("Cannot draw method ["+getterName+"] for type ["+type+"]");

        // Use code fragments if available
        CodeFragment javadocFragment = context.getCodeFragment("java.getter.javadoc");
        if (javadocFragment != null) {
            Map<String, Object> vars = new HashMap<>();
            vars.put("field.javaType", valueClass);
            vars.put("field.nameCapitalized", GeneratorUtil.toCamelCase(field.getName(), true));
            
            String javadoc = javadocFragment.generate(context, vars);
            for (String line : javadoc.split("\n")) {
                println(true, line);
            }
        } else {
            // Fallback to hardcoded
            println(true,"/**");
            println(true," * "+getterName+" is a code generated Getter method");
            println(true," * @returns "+valueClass+" Value to get");
            println(true," */");
        }

        // Method signature
        CodeFragment signatureFragment = context.getCodeFragment("java.interface.getter");
        if (signatureFragment != null) {
            Map<String, Object> vars = new HashMap<>();
            vars.put("field.javaType", valueClass);
            vars.put("field.nameCapitalized", GeneratorUtil.toCamelCase(field.getName(), true));
            
            String signature = signatureFragment.generate(context, vars);
            println(true, signature);
        } else {
            // Fallback
            println(true, "public " + valueClass + " " + getterName + "();");
        }
    }

    protected void drawSetter(String setterName, String valueName, String valueClass, MetaField field) {
        if (!type.equals("interface"))
            throw new UnsupportedOperationException("Cannot draw method ["+setterName+"] for type ["+type+"]");

        // Use code fragments for JavaDoc
        CodeFragment javadocFragment = context.getCodeFragment("java.setter.javadoc");
        if (javadocFragment != null) {
            Map<String, Object> vars = new HashMap<>();
            vars.put("field.javaType", valueClass);
            vars.put("field.nameCapitalized", GeneratorUtil.toCamelCase(field.getName(), true));
            
            String javadoc = javadocFragment.generate(context, vars);
            for (String line : javadoc.split("\n")) {
                println(true, line);
            }
        } else {
            // Fallback
            println(true,"/**");
            println(true," * "+setterName+" is a code generated Setter method");
            println(true," * @param "+valueName+" Value to set");
            println(true," */");
        }

        // Method signature
        CodeFragment signatureFragment = context.getCodeFragment("java.interface.setter");
        if (signatureFragment != null) {
            Map<String, Object> vars = new HashMap<>();
            vars.put("field.javaType", valueClass);
            vars.put("field.nameCapitalized", GeneratorUtil.toCamelCase(field.getName(), true));
            
            String signature = signatureFragment.generate(context, vars);
            println(true, signature);
        } else {
            // Fallback
            println(true, "public void " + setterName + "(" + valueClass + " " + valueName + ");");
        }
    }

    protected void drawObjectStart(List<String> docs, String pkg, String name, List<String> importList, String fullSuperName) {
        // Print package declaration
        println("package "+pkg+";");

        // Print Imports
        if (!importList.isEmpty()) {
            println();
            // Remove duplicates and sort
            Set<String> uniqueImports = new TreeSet<>(importList);
            for (String imp : uniqueImports) {
                println("import " + imp + ";");
            }
        }

        // Print JavaDoc header
        println();
        println("/**");
        for (String doc : docs) {
            println(" * " + doc);
        }
        println(" */");

        // Use code fragment for class header if available
        CodeFragment headerFragment = context.getCodeFragment("java.class.header");
        if (headerFragment != null) {
            Map<String, Object> vars = new HashMap<>();
            vars.put("classType", type);
            vars.put("superClass", (fullSuperName != null && !fullSuperName.isEmpty()) ? " extends " + fullSuperName : "");
            
            String header = headerFragment.generate(context, vars);
            println(header);
        } else {
            // Fallback
            String extendsClause = (fullSuperName != null && !fullSuperName.isEmpty()) ? " extends " + fullSuperName : "";
            println("public " + type + " " + name + extendsClause + " {");
        }
    }

    protected void drawObjectEnd() {
        println();
        println("}");
        println();
    }

    protected void drawComment(String comment) {
        println(true, "// "+comment);
    }

    protected void drawNewLine() {
        println();
    }

    //////////////////////////////////////////////////////////////////////
    // Utility Methods (same as original but with plugin integration)

    protected void initVariables(MetaObject mo) {
        metaObject = mo;
        pkg = getJavaPkg(mo);
        name = getClassName(mo);

        if (hasSuperObject(mo)) {
            superObject = mo.getSuperObject();
            superPkg = getJavaPkg(superObject);
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

    // ... [Include all the other utility methods from SimpleJavaCodeWriter with minimal changes]
    
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
        String p = getJavaPkg(refmo);
        String n = getClassName(refmo);
        if (!pkg.equals(p) && name.equals(n)) {
            pkgPrefixMap.put(refmo,p+".");
        } else {
            pkgPrefixMap.put(refmo,"");
        }
    }

    private void addImportToList(MetaObject mo) {
        String p = getJavaPkg(mo);
        String n = getClassName(mo);
        if (!pkg.equals(p) &&
                !name.equals(n)
                && !importList.contains(p)) {
            importList.add(p+"."+n);
        }
    }

    protected String getJavaPkg(MetaObject mo) {
        String path = mo.getPackage().replaceAll("::", ".");
        if (isNotBlank(pkgPrefix)) {
            if (pkgPrefix.endsWith(".")) path = pkgPrefix+path;
            else path = pkgPrefix +"."+path;
        }
        if (isNotBlank(pkgSuffix)) {
            if (pkgSuffix.startsWith(".")) path = path+pkgSuffix;
            else path = path+"."+pkgSuffix;
        }
        return path;
    }

    protected String getClassName(MetaObject md) {
        String name = md.getShortName();
        if (isNotBlank(namePrefix)) name = namePrefix+"-"+name;
        if (isNotBlank(nameSuffix)) name = name+"-"+nameSuffix;
        name = name.replaceAll("--","-");
        return GeneratorUtil.toCamelCase(name, true);
    }

    protected String getValueName(MetaField mf) {
        return GeneratorUtil.toCamelCase(mf.getName(),false);
    }

    protected String getValueClass(MetaField mf) {
        MetaObject mo = objectReferenceMap.get(mf);
        if (mo != null) {
            String vn = getClassName(mo);
            String pre = pkgPrefixMap.get(mo);
            if (pre != null) vn = pre+vn;
            if (mf.getDataType().isArray()) vn = "java.util.List<"+vn+">";
            return vn;
        }

        switch(mf.getDataType()) {
            case BOOLEAN: return "Boolean";
            case BYTE: return "Byte";
            case SHORT: return "Short";
            case INT: return "Integer";
            case LONG: return "Long";
            case FLOAT: return "Float";
            case DOUBLE: return "Double";
            case DATE: return "java.util.Date";
            case STRING: return "String";
            case STRING_ARRAY: return "java.util.List<String>";
            case OBJECT: return "Object";
            case OBJECT_ARRAY: return "java.util.List<Object>";
            default: return "Object";
        }
    }

    protected String getGetterOrSetterName(MetaField mf, boolean getter) {
        String name = mf.getName();
        if (mf.hasMetaAttr(ATTR_JAVANAME)) name = mf.getMetaAttr(ATTR_JAVANAME).getValueAsString();

        String prefix = getter ? "get" : "set";
        if (getter && mf.getDataType() == DataTypes.BOOLEAN) {
            prefix = "is";
        }

        return prefix + GeneratorUtil.toCamelCase(name, true);
    }

    private boolean isNotBlank(String str) {
        return str != null && !str.trim().isEmpty();
    }
}