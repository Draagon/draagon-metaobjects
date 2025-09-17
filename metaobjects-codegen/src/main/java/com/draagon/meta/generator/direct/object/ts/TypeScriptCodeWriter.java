package com.draagon.meta.generator.direct.object.ts;

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
 * TypeScript-specific implementation of Object Code Writer
 */
public class TypeScriptCodeWriter extends BaseObjectCodeWriter {

    public final static String ATTR_TSNAME = "tsName";

    public TypeScriptCodeWriter(MetaDataLoader loader, PrintWriter pw) {
        super(loader, pw);
    }
    
    public TypeScriptCodeWriter(MetaDataLoader loader, PrintWriter pw, GenerationContext context) {
        super(loader, pw, context);
    }

    @Override
    protected String getLanguageType(MetaField field) {
        MetaObject mo = objectReferenceMap.get(field);
        if (mo != null) {
            String typeName = getClassName(mo);
            String pre = pkgPrefixMap.get(mo);
            if (pre != null) typeName = pre + typeName;
            if (field.getDataType().isArray()) typeName = typeName + "[]";
            return typeName;
        }

        switch(field.getDataType()) {
            case BOOLEAN: return "boolean";
            case BYTE:
            case SHORT:
            case INT:
            case LONG:
            case FLOAT:
            case DOUBLE: return "number";
            case DATE: return "Date";
            case STRING: return "string";
            case STRING_ARRAY: return "string[]";
            case OBJECT: return "any";
            case OBJECT_ARRAY: return "any[]";
            default: return "any";
        }
    }

    @Override
    protected String getGetterMethodName(MetaField field) {
        // TypeScript properties don't need getter methods, just the property name
        return GeneratorUtil.toCamelCase(field.getName(), false);
    }

    @Override
    protected String getSetterMethodName(MetaField field) {
        // TypeScript properties don't need setter methods, just the property name
        return GeneratorUtil.toCamelCase(field.getName(), false);
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
        // TypeScript uses properties, so we write a property instead of getter/setter methods
        writeProperty(getterName, typeName, field);
    }

    @Override
    protected void writeSetter(String setterName, String paramName, String typeName, MetaField field) {
        // In TypeScript, setter is part of the property, so we don't write it separately
        // This method intentionally left empty
    }

    private void writeProperty(String propertyName, String typeName, MetaField field) {
        // Write JSDoc comment
        println(true, "/**");
        println(true, " * " + field.getName() + " property");
        println(true, " */");
        
        if (type.equals("interface")) {
            // Interface property
            println(true, propertyName + ": " + typeName + ";");
        } else if (type.equals("type")) {
            // Type alias property
            println(true, propertyName + ": " + typeName + ";");
        } else if (type.equals("class")) {
            // Class property
            println(true, "public " + propertyName + ": " + typeName + ";");
        }
    }

    @Override
    protected void writeObjectHeader(List<String> docs, String pkg, String name, List<String> importList, String fullSuperName) {
        // Write imports
        if (!importList.isEmpty()) {
            for (String imp : new TreeSet<>(importList)) {
                println("import { " + imp + " } from './" + imp + "';");
            }
            println();
        }

        // Write JSDoc documentation
        println("/**");
        for (String doc : docs) {
            println(" * " + doc);
        }
        println(" */");

        // Write interface/class/type declaration
        String inheritance = "";
        if (fullSuperName != null && !fullSuperName.isEmpty()) {
            if (type.equals("interface") || type.equals("class")) {
                inheritance = " extends " + fullSuperName;
            }
        }
        
        if (type.equals("interface")) {
            println("export " + type + " " + name + inheritance + " {");
        } else if (type.equals("class")) {
            println("export " + type + " " + name + inheritance + " {");
        } else if (type.equals("type")) {
            println("export " + type + " " + name + " = {");
        }
    }

    @Override
    protected void writeObjectFooter() {
        if (type.equals("type")) {
            println("};");
        } else {
            println("}");
        }
        println();
    }

    @Override
    protected void writeComment(String comment) {
        println(true, "// " + comment);
    }

    @Override
    protected void writeNewLine() {
        println();
    }

    @Override
    protected String getLanguagePackage(MetaObject mo) {
        // TypeScript uses module paths, which are typically file paths
        String path = mo.getPackage().replaceAll("::", "/");
        if (isNotBlank(pkgPrefix)) {
            if (pkgPrefix.endsWith("/")) path = pkgPrefix + path;
            else path = pkgPrefix + "/" + path;
        }
        if (isNotBlank(pkgSuffix)) {
            if (pkgSuffix.startsWith("/")) path = path + pkgSuffix;
            else path = path + "/" + pkgSuffix;
        }
        return path;
    }

    @Override
    protected String getLanguageNameAttribute() {
        return ATTR_TSNAME;
    }
}