package com.draagon.meta.generator.direct.object.python;

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
 * Python-specific implementation of Object Code Writer
 */
public class PythonCodeWriter extends BaseObjectCodeWriter {

    public final static String ATTR_PYTHONNAME = "pythonName";

    public PythonCodeWriter(MetaDataLoader loader, PrintWriter pw) {
        super(loader, pw);
    }
    
    public PythonCodeWriter(MetaDataLoader loader, PrintWriter pw, GenerationContext context) {
        super(loader, pw, context);
    }

    @Override
    protected String getLanguageType(MetaField field) {
        MetaObject mo = objectReferenceMap.get(field);
        if (mo != null) {
            String typeName = getClassName(mo);
            String pre = pkgPrefixMap.get(mo);
            if (pre != null) typeName = pre + typeName;
            if (field.getDataType().isArray()) typeName = "List[" + typeName + "]";
            return typeName;
        }

        switch(field.getDataType()) {
            case BOOLEAN: return "bool";
            case BYTE:
            case SHORT:
            case INT:
            case LONG: return "int";
            case FLOAT:
            case DOUBLE: return "float";
            case DATE: return "datetime";
            case STRING: return "str";
            case STRING_ARRAY: return "List[str]";
            case OBJECT: return "Any";
            case OBJECT_ARRAY: return "List[Any]";
            default: return "Any";
        }
    }

    @Override
    protected String getGetterMethodName(MetaField field) {
        // Python typically uses properties, so return the property name in snake_case
        return GeneratorUtil.toCamelCase(field.getName(), false).replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }

    @Override
    protected String getSetterMethodName(MetaField field) {
        // Python typically uses properties, so return the property name in snake_case
        return GeneratorUtil.toCamelCase(field.getName(), false).replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }

    @Override
    protected String getParameterName(MetaField field) {
        return GeneratorUtil.toCamelCase(field.getName(), false).replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
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
        // Python uses properties or simple attributes, write as attribute
        writeAttribute(getterName, typeName, field);
    }

    @Override
    protected void writeSetter(String setterName, String paramName, String typeName, MetaField field) {
        // In Python with dataclasses, setter is handled automatically
        // This method intentionally left empty
    }

    private void writeAttribute(String attrName, String typeName, MetaField field) {
        if (type.equals("protocol")) {
            // Protocol attribute (typing.Protocol)
            println(true, attrName + ": " + typeName);
        } else if (type.equals("dataclass")) {
            // Dataclass field
            println(true, attrName + ": " + typeName);
        } else if (type.equals("class")) {
            // Regular class - we'll use type hints in __init__
            // This will be handled in the class constructor
        }
    }

    @Override
    protected void writeObjectHeader(List<String> docs, String pkg, String name, List<String> importList, String fullSuperName) {
        // Write imports
        Set<String> requiredImports = new HashSet<>();
        
        // Add standard imports based on type annotations used
        requiredImports.add("from typing import List, Any, Optional");
        requiredImports.add("from datetime import datetime");
        
        if (type.equals("dataclass")) {
            requiredImports.add("from dataclasses import dataclass");
        } else if (type.equals("protocol")) {
            requiredImports.add("from typing import Protocol");
        }
        
        // Add custom imports
        if (!importList.isEmpty()) {
            for (String imp : new TreeSet<>(importList)) {
                requiredImports.add("from . import " + imp);
            }
        }
        
        for (String imp : requiredImports) {
            println(imp);
        }
        println();

        // Write docstring
        println("\"\"\"");
        for (String doc : docs) {
            println(doc);
        }
        println("\"\"\"");
        println();

        // Write class decorator and declaration
        if (type.equals("dataclass")) {
            println("@dataclass");
        }
        
        String inheritance = "";
        if (fullSuperName != null && !fullSuperName.isEmpty()) {
            inheritance = "(" + fullSuperName + ")";
        } else if (type.equals("protocol")) {
            inheritance = "(Protocol)";
        }
        
        println("class " + name + inheritance + ":");
        
        // Write class docstring
        inc();
        println("\"\"\"");
        println("Generated " + type + " for " + name);
        println("\"\"\"");
    }

    @Override
    protected void writeObjectFooter() {
        // Python doesn't need explicit closing braces
        dec();
        println();
    }

    @Override
    protected void writeComment(String comment) {
        println(true, "# " + comment);
    }

    @Override
    protected void writeNewLine() {
        println();
    }

    @Override
    protected String getLanguagePackage(MetaObject mo) {
        // Python uses module paths with dots, similar to Java but without prefixes/suffixes typically
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
        return ATTR_PYTHONNAME;
    }
}