package com.metaobjects.generator.direct.object.javacode;

import com.metaobjects.DataTypes;
import com.metaobjects.field.MetaField;
import com.metaobjects.generator.direct.object.BaseObjectCodeWriter;
import com.metaobjects.generator.direct.GenerationContext;
import com.metaobjects.generator.util.GeneratorUtil;
import com.metaobjects.loader.MetaDataLoader;
import com.metaobjects.object.MetaObject;

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
            if (field.isArrayType()) typeName = "java.util.List<" + typeName + ">";
            return typeName;
        }

        // Get base type name
        String baseType;
        switch(field.getDataType()) {
            case BOOLEAN: baseType = "Boolean"; break;
            case BYTE: baseType = "Byte"; break;
            case SHORT: baseType = "Short"; break;
            case INT: baseType = "Integer"; break;
            case LONG: baseType = "Long"; break;
            case FLOAT: baseType = "Float"; break;
            case DOUBLE: baseType = "Double"; break;
            case DATE: baseType = "java.util.Date"; break;
            case STRING: baseType = "String"; break;
            case OBJECT: baseType = "Object"; break;

            // Legacy array types - should not be used with new @isArray approach
            case STRING_ARRAY: baseType = "String"; break;  // Fallback for legacy
            case OBJECT_ARRAY: baseType = "Object"; break;  // Fallback for legacy

            default: baseType = "Object"; break;
        }

        // Wrap in List if this field is marked as array
        if (field.isArrayType()) {
            return "java.util.List<" + baseType + ">";
        }

        return baseType;
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