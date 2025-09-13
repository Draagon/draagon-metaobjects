package com.draagon.meta.generator.direct.object.javacode;

import com.draagon.meta.generator.GeneratorIOException;
import com.draagon.meta.generator.GeneratorIOWriter;
import com.draagon.meta.generator.direct.object.BaseObjectCodeGenerator;
import com.draagon.meta.generator.direct.object.BaseObjectCodeWriter;
import com.draagon.meta.generator.direct.GenerationContext;
import com.draagon.meta.generator.direct.metadata.overlay.JavaCodeOverlayXMLWriter;
import com.draagon.meta.generator.util.GeneratorUtil;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.object.MetaObject;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Collection;

/**
 * Java-specific implementation of Object Code Generator
 */
public class JavaCodeGenerator extends BaseObjectCodeGenerator {

    public final static String TYPE_INTERFACE  = "interface";
    public final static String TYPE_CLASS      = "class";

    @Override
    protected String[] getSupportedTypes() {
        return new String[]{TYPE_INTERFACE, TYPE_CLASS};
    }

    @Override
    protected String getDefaultType() {
        return TYPE_INTERFACE;
    }

    @Override
    protected BaseObjectCodeWriter createWriter(MetaDataLoader loader, MetaObject md, PrintWriter pw, GenerationContext context) {
        return new JavaCodeWriter(loader, pw, context)
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
        return ".java";
    }

    @Override
    protected String getLanguageName() {
        return "Java";
    }

    @Override
    protected String convertToLanguageNaming(String name) {
        // Java uses PascalCase for class names
        return GeneratorUtil.toCamelCase(name, true);
    }

    @Override
    protected GeneratorIOWriter getFinalWriter(MetaDataLoader loader, OutputStream out) throws GeneratorIOException {
        return new JavaCodeOverlayXMLWriter(loader, out)
                .forObjects(objectNameMap);
    }

    @Override
    protected void writeFinalFile(Collection<MetaObject> metadata, GeneratorIOWriter<?> writer) throws GeneratorIOException {
        log.info("Writing Java Code Overlay XML to file: " + writer.getFilename());
        ((JavaCodeOverlayXMLWriter)writer).writeXML();
    }
}