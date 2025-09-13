package com.draagon.meta.generator.direct.object.ts;

import com.draagon.meta.generator.GeneratorIOException;
import com.draagon.meta.generator.GeneratorIOWriter;
import com.draagon.meta.generator.direct.object.BaseObjectCodeGenerator;
import com.draagon.meta.generator.direct.object.BaseObjectCodeWriter;
import com.draagon.meta.generator.direct.GenerationContext;
import com.draagon.meta.generator.util.GeneratorUtil;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.object.MetaObject;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Collection;

/**
 * TypeScript-specific implementation of Object Code Generator
 */
public class TypeScriptCodeGenerator extends BaseObjectCodeGenerator {

    public final static String TYPE_INTERFACE  = "interface";
    public final static String TYPE_CLASS      = "class";
    public final static String TYPE_TYPE       = "type";

    @Override
    protected String[] getSupportedTypes() {
        return new String[]{TYPE_INTERFACE, TYPE_CLASS, TYPE_TYPE};
    }

    @Override
    protected String getDefaultType() {
        return TYPE_INTERFACE;
    }

    @Override
    protected BaseObjectCodeWriter createWriter(MetaDataLoader loader, MetaObject md, PrintWriter pw, GenerationContext context) {
        return new TypeScriptCodeWriter(loader, pw, context)
                .forType(getType())
                .withPkgPrefix(getPkgPrefix())
                .withPkgSuffix(getPkgSuffix())
                .withNamePrefix(getNamePrefix())
                .withNameSuffix(getNameSuffix())
                .addArrayMethods(addArrayMethods())
                .addKeyMethods(addKeyMethods())
                .withIndentor("  "); // TypeScript typically uses 2 spaces
    }

    @Override
    protected String getFileExtension() {
        return ".ts";
    }

    @Override
    protected String getLanguageName() {
        return "TypeScript";
    }

    @Override
    protected String convertToLanguageNaming(String name) {
        // TypeScript uses PascalCase for interfaces and classes
        return GeneratorUtil.toCamelCase(name, true);
    }

    @Override
    protected GeneratorIOWriter getFinalWriter(MetaDataLoader loader, OutputStream out) throws GeneratorIOException {
        // TypeScript typically doesn't need overlay files
        return null;
    }

    @Override
    protected void writeFinalFile(Collection<MetaObject> metadata, GeneratorIOWriter<?> writer) throws GeneratorIOException {
        // No final file needed for TypeScript
    }
}