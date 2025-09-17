package com.draagon.meta.generator.direct.object.dotnet;

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
 * C# .NET-specific implementation of Object Code Writer
 */
public class CSharpCodeWriter extends BaseObjectCodeWriter {

    public final static String ATTR_CSHARPNAME = "csharpName";

    public CSharpCodeWriter(MetaDataLoader loader, PrintWriter pw) {
        super(loader, pw);
    }
    
    public CSharpCodeWriter(MetaDataLoader loader, PrintWriter pw, GenerationContext context) {
        super(loader, pw, context);
    }

    @Override
    protected String getLanguageType(MetaField field) {
        MetaObject mo = objectReferenceMap.get(field);
        if (mo != null) {
            String typeName = getClassName(mo);
            String pre = pkgPrefixMap.get(mo);
            if (pre != null) typeName = pre + typeName;
            if (field.getDataType().isArray()) typeName = "List<" + typeName + ">";
            return typeName;
        }

        switch(field.getDataType()) {
            case BOOLEAN: return "bool";
            case BYTE: return "byte";
            case SHORT: return "short";
            case INT: return "int";
            case LONG: return "long";
            case FLOAT: return "float";
            case DOUBLE: return "double";
            case DATE: return "DateTime";
            case STRING: return "string";
            case STRING_ARRAY: return "List<string>";
            case OBJECT: return "object";
            case OBJECT_ARRAY: return "List<object>";
            default: return "object";
        }
    }

    @Override
    protected String getGetterMethodName(MetaField field) {
        // C# uses properties, so this is just the property name
        return GeneratorUtil.toCamelCase(field.getName(), true);
    }

    @Override
    protected String getSetterMethodName(MetaField field) {
        // C# uses properties, so this is just the property name
        return GeneratorUtil.toCamelCase(field.getName(), true);
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
        // C# uses properties instead of separate getter/setter methods
        writeProperty(getterName, typeName, field);
    }

    @Override
    protected void writeSetter(String setterName, String paramName, String typeName, MetaField field) {
        // In C#, setter is part of the property, so we don't write it separately
        // This method intentionally left empty
    }

    private void writeProperty(String propertyName, String typeName, MetaField field) {
        // Write XML documentation comment
        println(true, "/// <summary>");
        println(true, "/// Gets or sets the " + field.getName() + " value");
        println(true, "/// </summary>");
        
        if (type.equals("interface")) {
            // Interface property
            println(true, typeName + " " + propertyName + " { get; set; }");
        } else if (type.equals("class")) {
            // Auto-implemented property
            println(true, "public " + typeName + " " + propertyName + " { get; set; }");
        } else if (type.equals("record")) {
            // Record property (init-only)
            println(true, "public " + typeName + " " + propertyName + " { get; init; }");
        } else if (type.equals("struct")) {
            // Struct property
            println(true, "public " + typeName + " " + propertyName + " { get; set; }");
        }
    }

    @Override
    protected void writeObjectHeader(List<String> docs, String pkg, String name, List<String> importList, String fullSuperName) {
        // Write using statements (C# equivalent of imports)
        if (!importList.isEmpty()) {
            for (String imp : new TreeSet<>(importList)) {
                println("using " + imp + ";");
            }
            println();
        }

        // Write namespace
        println("namespace " + pkg.replace('/', '.'));
        println("{");
        inc();

        // Write XML documentation
        println("/// <summary>");
        for (String doc : docs) {
            println("/// " + doc);
        }
        println("/// </summary>");

        // Write class/interface/struct/record declaration
        String inheritance = (fullSuperName != null && !fullSuperName.isEmpty()) ? " : " + fullSuperName : "";
        
        if (type.equals("interface")) {
            println("public " + type + " I" + name + inheritance);
        } else {
            println("public " + type + " " + name + inheritance);
        }
        println("{");
    }

    @Override
    protected void writeObjectFooter() {
        println("}"); // Close class/interface/struct/record
        dec();
        println("}"); // Close namespace
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
        return ATTR_CSHARPNAME;
    }
}