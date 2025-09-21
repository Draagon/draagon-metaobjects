package com.draagon.meta.generator.direct.object.javacode;

import com.draagon.meta.DataTypes;
import com.draagon.meta.field.MetaField;
import com.draagon.meta.generator.direct.object.BaseObjectCodeWriter;
import com.draagon.meta.generator.direct.GenerationContext;
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

        // Simple JavaDoc for deprecated generator
        println(true,"/**");
        println(true," * "+getterName+" is a code generated Getter method");
        println(true," * @returns "+typeName+" Value to get");
        println(true," */");

        // Method signature
        println(true, "public " + typeName + " " + getterName + "();");
    }

    @Override
    protected void writeSetter(String setterName, String paramName, String typeName, MetaField field) {
        if (!type.equals("interface"))
            throw new UnsupportedOperationException("Cannot draw method ["+setterName+"] for type ["+type+"]");

        // Simple JavaDoc for deprecated generator
        println(true,"/**");
        println(true," * "+setterName+" is a code generated Setter method");
        println(true," * @param "+paramName+" Value to set");
        println(true," */");

        // Method signature
        println(true, "public void " + setterName + "(" + typeName + " " + paramName + ");");
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

        // Simple class header for deprecated generator
        String extendsClause = (fullSuperName != null && !fullSuperName.isEmpty()) ? " extends " + fullSuperName : "";
        println("public " + type + " " + name + extendsClause + " {");
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