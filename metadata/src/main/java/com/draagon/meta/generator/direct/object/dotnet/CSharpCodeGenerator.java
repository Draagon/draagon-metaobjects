package com.draagon.meta.generator.direct.object.dotnet;

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
 * C# .NET-specific implementation of Object Code Generator
 */
public class CSharpCodeGenerator extends BaseObjectCodeGenerator {

    public final static String TYPE_INTERFACE  = "interface";
    public final static String TYPE_CLASS      = "class";
    public final static String TYPE_STRUCT     = "struct";
    public final static String TYPE_RECORD     = "record";

    @Override
    protected String[] getSupportedTypes() {
        return new String[]{TYPE_INTERFACE, TYPE_CLASS, TYPE_STRUCT, TYPE_RECORD};
    }

    @Override
    protected String getDefaultType() {
        return TYPE_CLASS;
    }

    @Override
    protected BaseObjectCodeWriter createWriter(MetaDataLoader loader, MetaObject md, PrintWriter pw, GenerationContext context) {
        return new CSharpCodeWriter(loader, pw, context)
                .forType(getType())
                .withPkgPrefix(getPkgPrefix())
                .withPkgSuffix(getPkgSuffix())
                .withNamePrefix(getNamePrefix())
                .withNameSuffix(getNameSuffix())
                .addArrayMethods(addArrayMethods())
                .addKeyMethods(addKeyMethods())
                .withIndentor("    ");
    }

    @Override
    protected String getFileExtension() {
        return ".cs";
    }

    @Override
    protected String getLanguageName() {
        return "C#";
    }

    @Override
    protected String convertToLanguageNaming(String name) {
        // C# uses PascalCase for class names
        return GeneratorUtil.toCamelCase(name, true);
    }

    @Override
    protected GeneratorIOWriter getFinalWriter(MetaDataLoader loader, OutputStream out) throws GeneratorIOException {
        // C# typically doesn't need overlay XML files like Java
        return null;
    }

    @Override
    protected void writeFinalFile(Collection<MetaObject> metadata, GeneratorIOWriter<?> writer) throws GeneratorIOException {
        // No final file needed for C#
    }
}