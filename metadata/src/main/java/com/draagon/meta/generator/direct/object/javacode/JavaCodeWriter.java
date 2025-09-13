package com.draagon.meta.generator.direct.object.javacode;

import com.draagon.meta.DataTypes;
import com.draagon.meta.field.MetaField;
import com.draagon.meta.generator.direct.object.BaseObjectCodeWriter;
import com.draagon.meta.generator.direct.GenerationContext;
import com.draagon.meta.generator.direct.CodeFragment;
import com.draagon.meta.generator.util.GeneratorUtil;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.object.MetaObject;

import java.io.PrintWriter;
import java.util.*;

/**
 * Java-specific implementation of Object Code Writer
 */
public class JavaCodeWriter extends BaseObjectCodeWriter {

    public final static String ATTR_JAVANAME = "javaName";

    public JavaCodeWriter(MetaDataLoader loader, PrintWriter pw) {
        super(loader, pw);
    }
    
    public JavaCodeWriter(MetaDataLoader loader, PrintWriter pw, GenerationContext context) {
        super(loader, pw, context);
    }

    @Override
    protected String getLanguageType(MetaField field) {
        MetaObject mo = objectReferenceMap.get(field);
        if (mo != null) {
            String typeName = getClassName(mo);
            String pre = pkgPrefixMap.get(mo);
            if (pre != null) typeName = pre + typeName;
            if (field.getDataType().isArray()) typeName = "java.util.List<" + typeName + ">";
            return typeName;
        }

        switch(field.getDataType()) {
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

    @Override
    protected String getGetterMethodName(MetaField field) {
        String name = field.getName();
        String languageAttr = getLanguageNameAttribute();
        if (field.hasMetaAttr(languageAttr)) {
            name = field.getMetaAttr(languageAttr).getValueAsString();
        }

        String prefix = "get";
        if (field.getDataType() == DataTypes.BOOLEAN) {
            prefix = "is";
        }

        return prefix + GeneratorUtil.toCamelCase(name, true);
    }

    @Override
    protected String getSetterMethodName(MetaField field) {
        String name = field.getName();
        String languageAttr = getLanguageNameAttribute();
        if (field.hasMetaAttr(languageAttr)) {
            name = field.getMetaAttr(languageAttr).getValueAsString();
        }

        return "set" + GeneratorUtil.toCamelCase(name, true);
    }

    @Override
    protected String getParameterName(MetaField field) {
        return GeneratorUtil.toCamelCase(field.getName(), false);
    }

    @Override
    protected String getClassName(MetaObject mo) {
        String name = mo.getShortName();
        if (isNotBlank(namePrefix)) name = namePrefix + "-" + name;
        if (isNotBlank(nameSuffix)) name = name + "-" + nameSuffix;
        name = name.replaceAll("--", "-");
        return GeneratorUtil.toCamelCase(name, true);
    }

    @Override
    protected void writeGetter(String getterName, String typeName, MetaField field) {
        if (!type.equals("interface"))
            throw new UnsupportedOperationException("Cannot draw method ["+getterName+"] for type ["+type+"]");

        // Use code fragments if available
        CodeFragment javadocFragment = context.getCodeFragment("java.getter.javadoc");
        if (javadocFragment != null) {
            Map<String, Object> vars = new HashMap<>();
            vars.put("field.javaType", typeName);
            vars.put("field.nameCapitalized", GeneratorUtil.toCamelCase(field.getName(), true));
            
            String javadoc = javadocFragment.generate(context, vars);
            for (String line : javadoc.split("\n")) {
                println(true, line);
            }
        } else {
            // Fallback to hardcoded
            println(true,"/**");
            println(true," * "+getterName+" is a code generated Getter method");
            println(true," * @returns "+typeName+" Value to get");
            println(true," */");
        }

        // Method signature
        CodeFragment signatureFragment = context.getCodeFragment("java.interface.getter");
        if (signatureFragment != null) {
            Map<String, Object> vars = new HashMap<>();
            vars.put("field.javaType", typeName);
            vars.put("field.nameCapitalized", GeneratorUtil.toCamelCase(field.getName(), true));
            
            String signature = signatureFragment.generate(context, vars);
            println(true, signature);
        } else {
            // Fallback
            println(true, "public " + typeName + " " + getterName + "();");
        }
    }

    @Override
    protected void writeSetter(String setterName, String paramName, String typeName, MetaField field) {
        if (!type.equals("interface"))
            throw new UnsupportedOperationException("Cannot draw method ["+setterName+"] for type ["+type+"]");

        // Use code fragments for JavaDoc
        CodeFragment javadocFragment = context.getCodeFragment("java.setter.javadoc");
        if (javadocFragment != null) {
            Map<String, Object> vars = new HashMap<>();
            vars.put("field.javaType", typeName);
            vars.put("field.nameCapitalized", GeneratorUtil.toCamelCase(field.getName(), true));
            
            String javadoc = javadocFragment.generate(context, vars);
            for (String line : javadoc.split("\n")) {
                println(true, line);
            }
        } else {
            // Fallback
            println(true,"/**");
            println(true," * "+setterName+" is a code generated Setter method");
            println(true," * @param "+paramName+" Value to set");
            println(true," */");
        }

        // Method signature
        CodeFragment signatureFragment = context.getCodeFragment("java.interface.setter");
        if (signatureFragment != null) {
            Map<String, Object> vars = new HashMap<>();
            vars.put("field.javaType", typeName);
            vars.put("field.nameCapitalized", GeneratorUtil.toCamelCase(field.getName(), true));
            
            String signature = signatureFragment.generate(context, vars);
            println(true, signature);
        } else {
            // Fallback
            println(true, "public void " + setterName + "(" + typeName + " " + paramName + ");");
        }
    }

    @Override
    protected void writeObjectHeader(List<String> docs, String pkg, String name, List<String> importList, String fullSuperName) {
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

    @Override
    protected void writeObjectFooter() {
        println();
        println("}");
        println();
    }

    @Override
    protected void writeComment(String comment) {
        println(true, "// "+comment);
    }

    @Override
    protected void writeNewLine() {
        println();
    }

    @Override
    protected String getLanguagePackage(MetaObject mo) {
        String path = mo.getPackage().replaceAll("::", ".");
        if (isNotBlank(pkgPrefix)) {
            if (pkgPrefix.endsWith(".")) path = pkgPrefix + path;
            else path = pkgPrefix + "." + path;
        }
        if (isNotBlank(pkgSuffix)) {
            if (pkgSuffix.startsWith(".")) path = path + pkgSuffix;
            else path = path + "." + pkgSuffix;
        }
        return path;
    }

    @Override
    protected String getLanguageNameAttribute() {
        return ATTR_JAVANAME;
    }
}