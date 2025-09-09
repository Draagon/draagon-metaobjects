package com.draagon.meta.generator.direct.javacode.simple;

import com.draagon.meta.generator.GeneratorException;
import com.draagon.meta.generator.GeneratorIOException;
import com.draagon.meta.generator.GeneratorIOWriter;
import com.draagon.meta.generator.direct.MultiFileDirectGeneratorBase;
import com.draagon.meta.generator.direct.javacode.overlay.JavaCodeOverlayXMLWriter;
import com.draagon.meta.generator.util.GeneratorUtil;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.object.MetaObject;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class SimpleJavaCodeGenerator extends MultiFileDirectGeneratorBase<MetaObject> {

    public final static String ARG_TYPE        = "type";      // [interface]
    public final static String TYPE_INTERFACE  = "interface";

    public final static String ARG_PKGPREFIX   = "pkgPrefix";
    public final static String ARG_PKGSUFFIX   = "pkgSuffix";
    public final static String ARG_NAMEPREFIX  = "namePrefix";
    public final static String ARG_NAMESUFFIX  = "nameSuffix";

    public final static String ARG_OPTARRAYS   = "optArrays";
    public final static String ARG_OPTKEYS     = "optKeys";      // [true|f]

    protected Map<MetaObject,String> objectNameMap = new LinkedHashMap<>();

    //////////////////////////////////////////////////////////////////////
    // Argument Methods

    @Override
    protected void parseArgs() {
        if ( hasArg(ARG_OUTPUTFILENAME) && !hasArg(ARG_FINALOUTPUTDIR)) throw new GeneratorException(
                ARG_FINALOUTPUTDIR+" argument is required when a "+ARG_OUTPUTFILENAME+" is specified" );
        if ( !hasArg( ARG_TYPE)) throw new GeneratorException(
                ARG_TYPE+" argument is required, valid values=["+TYPE_INTERFACE+"]" );
        if ( !getArg( ARG_TYPE).equals(TYPE_INTERFACE)) throw new GeneratorException(
                ARG_TYPE+" argument only supports the following values: ["+TYPE_INTERFACE+"]" );

        super.parseArgs();

        if ( log.isDebugEnabled() ) log.debug("ParseArgs: "+toString());
    }

    public String getType() {
        return getArg(ARG_TYPE,TYPE_INTERFACE);
    }

    public String getPkgPrefix() {
        return getArg(ARG_PKGPREFIX, "");
    }

    public String getPkgSuffix() {
        return getArg(ARG_PKGSUFFIX, "");
    }

    public String getNamePrefix() {
        return getArg(ARG_NAMEPREFIX, "");
    }

    public String getNameSuffix() {
        return getArg(ARG_NAMESUFFIX, "");
    }

    public boolean addArrayMethods() {
        return Boolean.valueOf( getArg(ARG_OPTARRAYS,"false"));
    }

    public boolean addKeyMethods() {
        return Boolean.valueOf( getArg(ARG_OPTKEYS,"false"));
    }

    ///////////////////////////////////////////////////
    // Mutli-File Methods

    @Override
    protected Class<MetaObject> getFilterClass() {
        return MetaObject.class;
    }

    @Override
    protected SimpleJavaCodeWriter getSingleWriter(MetaDataLoader loader, MetaObject md, PrintWriter pw) throws GeneratorIOException {
        return new SimpleJavaCodeWriter(loader, pw)
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
    protected void writeSingleFile(MetaObject mo, GeneratorIOWriter<?> writer) throws GeneratorIOException {
        log.info("Writing JavaCode ["+getType()+"] to file: " + writer.getFilename() );

        String className = ((SimpleJavaCodeWriter)writer).writeObject(mo);
        objectNameMap.put(mo, className);
    }


    @Override
    protected GeneratorIOWriter getFinalWriter(MetaDataLoader loader, OutputStream out) throws GeneratorIOException {
        return new JavaCodeOverlayXMLWriter( loader, out )
                .forObjects(objectNameMap);
    }

    @Override
    protected void writeFinalFile(Collection<MetaObject> metadata, GeneratorIOWriter<?> writer) throws GeneratorIOException {
        log.info("Writing JavaCode Overlay XML to file: " + writer.getFilename() );
        ((JavaCodeOverlayXMLWriter)writer).writeXML();
    }

    protected String getSingleOutputFilePath(MetaObject md) {

        String path = md.getPackage().replaceAll( "::", ".");
        if ( isNotBlank(getPkgPrefix())) {
            String pre = getPkgPrefix();
            if ( pre.endsWith(".")) path = pre+path;
            else path = pre+"."+path;
        }
        if ( isNotBlank(getPkgSuffix())) {
            String suf = getPkgSuffix();
            if ( suf.startsWith(".")) path = path+suf;
            else path = path+"."+suf;
        }
        path=path.replace('.', File.separatorChar);
        return path;
    }

    protected String getSingleOutputFilename(MetaObject md) {
        String name = md.getShortName();
        if ( isNotBlank(getNamePrefix())) name = getNamePrefix()+"-"+name;
        if ( isNotBlank(getNameSuffix())) name = name+"-"+getNameSuffix();
        name = name.replaceAll("--","-");
        name = GeneratorUtil.toCamelCase( name, true )+".java";
        return name;
    }

    private boolean isNotBlank(String str) {
        return str != null && !str.trim().isEmpty();
    }
}
