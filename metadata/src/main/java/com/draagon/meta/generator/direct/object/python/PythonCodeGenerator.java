package com.draagon.meta.generator.direct.object.python;

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
 * Python-specific implementation of Object Code Generator
 */
public class PythonCodeGenerator extends BaseObjectCodeGenerator {

    public final static String TYPE_CLASS      = "class";
    public final static String TYPE_DATACLASS = "dataclass";
    public final static String TYPE_PROTOCOL  = "protocol";

    @Override
    protected String[] getSupportedTypes() {
        return new String[]{TYPE_CLASS, TYPE_DATACLASS, TYPE_PROTOCOL};
    }

    @Override
    protected String getDefaultType() {
        return TYPE_DATACLASS;
    }

    @Override
    protected BaseObjectCodeWriter createWriter(MetaDataLoader loader, MetaObject md, PrintWriter pw, GenerationContext context) {
        return new PythonCodeWriter(loader, pw, context)
                .forType(getType())
                .withPkgPrefix(getPkgPrefix())
                .withPkgSuffix(getPkgSuffix())
                .withNamePrefix(getNamePrefix())
                .withNameSuffix(getNameSuffix())
                .addArrayMethods(addArrayMethods())
                .addKeyMethods(addKeyMethods())
                .withIndentor("    "); // Python uses 4 spaces
    }

    @Override
    protected String getFileExtension() {
        return ".py";
    }

    @Override
    protected String getLanguageName() {
        return "Python";
    }

    @Override
    protected String convertToLanguageNaming(String name) {
        // Python uses PascalCase for class names
        return GeneratorUtil.toCamelCase(name, true);
    }

    @Override
    protected char getPathSeparator() {
        // Python uses forward slashes for module paths
        return '/';
    }

    @Override
    protected GeneratorIOWriter getFinalWriter(MetaDataLoader loader, OutputStream out) throws GeneratorIOException {
        // Python typically doesn't need overlay files
        return null;
    }

    @Override
    protected void writeFinalFile(Collection<MetaObject> metadata, GeneratorIOWriter<?> writer) throws GeneratorIOException {
        // No final file needed for Python
    }
}