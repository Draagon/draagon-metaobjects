package com.draagon.meta.generator.direct.javacode.simple;

import com.draagon.meta.DataTypes;
import com.draagon.meta.field.MetaField;
import com.draagon.meta.generator.GeneratorIOException;
import com.draagon.meta.generator.direct.FileDirectWriter;
import com.draagon.meta.generator.util.GeneratorUtil;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.util.MetaDataUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.util.*;

public class SimpleJavaCodeWriter extends FileDirectWriter<SimpleJavaCodeWriter> {

    private static final Logger log = LoggerFactory.getLogger(SimpleJavaCodeWriter.class);

    public final static String ATTR_JAVANAME ="javaName";

    protected boolean debug = false;

    protected Collection<MetaObject> filteredObjects;

    public SimpleJavaCodeWriter(MetaDataLoader loader, PrintWriter pw ) {
        super( loader, pw );
    }

    //////////////////////////////////////////////////////////////////////
    // Options

    protected String type = null;
    protected String pkgPrefix="";
    protected String pkgSuffix="";
    protected String namePrefix="";
    protected String nameSuffix="";
    protected boolean addArrays=false;
    protected boolean addKeys=false;

    public SimpleJavaCodeWriter forType(String type ) {
        this.type = type;
        return this;
    }

    public SimpleJavaCodeWriter withPkgPrefix(String pkgPrefix) {
        this.pkgPrefix = pkgPrefix;
        return this;
    }

    public SimpleJavaCodeWriter withPkgSuffix(String pkgSuffix) {
        this.pkgSuffix = pkgSuffix;
        return this;
    }

    public SimpleJavaCodeWriter withNamePrefix(String namePrefix) {
        this.namePrefix = namePrefix;
        return this;
    }

    public SimpleJavaCodeWriter withNameSuffix(String nameSuffix) {
        this.nameSuffix = nameSuffix;
        return this;
    }

    public SimpleJavaCodeWriter addArrayMethods(boolean addArrays) {
        this.addArrays = addArrays;
        return this;
    }

    public SimpleJavaCodeWriter addKeyMethods(boolean addKeys) {
        this.addKeys = addKeys;
        return this;
    }

    //////////////////////////////////////////////////////////////////////
    // Initialize Variables

    protected MetaObject metaObject = null;
    protected String pkg = null;
    protected String name = null;
    protected String superPkg = null;
    protected String superName = null;
    protected String fullSuperName = null;
    protected MetaObject superObject = null;

    protected Map<MetaField,MetaObject> objectReferenceMap = new HashMap<>();
    protected List<String> importList = new ArrayList<>();
    protected Map<MetaObject,String> pkgPrefixMap = new HashMap<>();

    protected void initVariables( MetaObject mo ) {

        metaObject = mo;
        pkg = getJavaPkg(mo);
        name = getClassName(mo);

        if (hasSuperObject(mo)) {
            superObject = mo.getSuperObject();
            superPkg = getJavaPkg(superObject);
            superName = getClassName(superObject);
        }

        initObjectReferenceMap(mo);
        initImportList(objectReferenceMap.values() );
        initPackagePrefixMap(objectReferenceMap.values() );

        if (superObject != null) {
            fullSuperName = pkgPrefixMap.get(superObject)+superName;
        } else {
            fullSuperName = superName;
        }
    }


    //////////////////////////////////////////////////////////////////////
    // Java Write Logic methods

    public String writeObject( MetaObject mo ) throws GeneratorIOException {

        initVariables(mo);

        try {
            List<String> docs = Arrays.asList(
                    "JavaCodeWriter:   "+getClass().getName(),
                    "MetaObject:       "+mo.getName(),
                    "SuperObject:      "+(superObject!=null?superObject.getName():""),
                    "MetaDataLoader:   "+getLoader().toString(),
                    "Generated On:     "+(new Date()).toString()
            );
            drawObjectStart( docs, pkg, name, importList, fullSuperName);

            //writeFieldVars();

            //writeObjectKeys();

            writeObjectMethods(mo);

            // Write end of Java file
            drawObjectEnd();

            return (pkg != null && !pkg.isEmpty()) ? pkg+"."+name : name;
        }
        catch( Exception e ) {
            throw new GeneratorIOException( this, "Error writing Java: "+e, e);
        }
    }

    protected void writeObjectMethods(MetaObject mo) {

        inc();

        for (MetaField mf : mo.getMetaFields(false)) {

            String getterName = getGetterOrSetterName(mf, true);
            String setterName = getGetterOrSetterName(mf, false);
            String valueName = getValueName(mf);
            String valueClass = getValueClass(mf);

            drawNewLine();
            drawComment( "////////////////////////////////////////////////////////////////////////////////////" );
            drawComment( "Methods for MetaField: " + mf.getName());
            drawNewLine();
            drawGetter( getterName, valueClass );
            drawSetter( setterName, valueName, valueClass );
        }

        dec();
    }


    //////////////////////////////////////////////////////////////////////
    // Initialization methods

    /**
     * Dig through all fields and add any object references
     */
    protected void initObjectReferenceMap(MetaObject mo) {
        for (MetaField mf : mo.getMetaFields(false) ) {
            if (MetaDataUtil.hasObjectRef(mf)) {
                objectReferenceMap.put(mf, MetaDataUtil.getObjectRef(mf));
            }
        }
    }

    /**
     * Get the map of package prefixes for name collisions on different packages
     */
    protected void initPackagePrefixMap(Collection<MetaObject> refObjects) {

        // Add superclass
        if (superObject != null) {
            addPackagePrefixToMap(superObject);
        }

        // Add referenced objects
        for (MetaObject refmo : refObjects ) {
            addPackagePrefixToMap(refmo);
        }
    }

    private boolean hasSuperObject(MetaObject mo) {
        return mo.getSuperData() != null && mo.getSuperData() instanceof MetaObject;
    }

    protected void addPackagePrefixToMap(MetaObject refmo) {
        String p = getJavaPkg(refmo);
        String n = getClassName(refmo);
        if ( !pkg.equals(p) && name.equals(n)) {  // If diff pkg but same name, then fully qualify
            pkgPrefixMap.put(refmo,p+".");
        } else {
            pkgPrefixMap.put(refmo,"");
        }
    }

    /**
     * Create the import list and handle name collisions on different packages
     */
    protected void initImportList(Collection<MetaObject> refObjects) {

        // Add super data
        if (superObject != null) {
            addImportToList(superObject);
        }

        // Add referenced objects
        for (MetaObject refmo : refObjects ) {
            addImportToList(refmo);
        }
    }

    private void addImportToList(MetaObject mo) {
        String p = getJavaPkg(mo);
        String n = getClassName(mo);
        if ( !pkg.equals(p) &&     // If in the same package, no need to add to imports
                !name.equals(n)    // If the package is different, but same name, then prefix in the methods
                && !importList.contains(p)) {
            importList.add(p+"."+n);
        }
    }


    //////////////////////////////////////////////////////////////////////
    // Logic Utility methods

    protected String getJavaPkg(MetaObject mo) {
        String path = mo.getPackage().replaceAll( "::", ".");
        if ( isNotBlank(pkgPrefix)) {
            if ( pkgPrefix.endsWith(".")) path = pkgPrefix+path;
            else path = pkgPrefix +"."+path;
        }
        if ( isNotBlank(pkgSuffix)) {
            if ( pkgSuffix.startsWith(".")) path = path+pkgSuffix;
            else path = path+"."+pkgSuffix;
        }
        return path;
    }

    protected String getClassName(MetaObject md) {
        String name = md.getShortName();
        if ( isNotBlank(namePrefix)) name = namePrefix+"-"+name;
        if ( isNotBlank(nameSuffix)) name = name+"-"+nameSuffix;
        name = name.replaceAll("--","-");
        return GeneratorUtil.toCamelCase( name, true );
    }

    protected String getValueName(MetaField mf) {
        return GeneratorUtil.toCamelCase(mf.getName(),false);
    }

    protected String getValueClass(MetaField mf) {

        MetaObject mo = objectReferenceMap.get(mf);
        if (mo != null) {
            String vn = getClassName(mo);
            String pre = pkgPrefixMap.get(mo);
            if (pre != null) vn = pre+vn;
            if ( mf.getDataType().isArray()) vn = "java.util.List<"+vn+">";
            return vn;
        }

        // TODO: Handle if required, but don't forget to ensure defaultValue exists
        switch(mf.getDataType()) {
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

            // TODO: Handle other types and Custom
            default: return "Object";
        }
    }

    protected String getGetterOrSetterName(MetaField mf, boolean getter ) {
        String n = GeneratorUtil.toCamelCase(mf.getName(),true);
        if ( getter ) {
            if ( mf.getDataType() == DataTypes.BOOLEAN ) return "is"+n;
            return "get"+n;
        }
        return "set"+n;
    }

    protected MetaObject getMetaObjectRef(MetaField f) {
        try { return MetaDataUtil.getObjectRef( f ); }
        catch( Exception ignore ) { return null; }
    }


    //////////////////////////////////////////////////////////////////////
    // Draw Helper methods



    //////////////////////////////////////////////////////////////////////
    // JavaCode Draw methods

    protected void drawComment(String comment) {
        println( true, "// "+comment);
    }

    /**
     *
     * @param docs
     * @param pkg
     * @param name
     * @param importList
     * @param fullSuperName
     */
    protected void drawObjectStart(List<String> docs, String pkg, String name, List<String> importList, String fullSuperName) {

        // Print package declartion
        println("package "+pkg+";");

        // Print Imports
        if (!importList.isEmpty()) {
            println();
            for (String imp : importList) {
                println("import " + imp + ";");
            }
        }

        // Print JavaDocs
        println();
        println("/**");
        println(" * "+name+" is a code generated "+type+" sourced from the following:");
        println(" *");
        for (String doc : docs) println(" * "+doc);
        println(" */");

        // Print Java Type Declaration
        print("public "+type+" "+name);
        if ( fullSuperName != null ) {
            if ( type.equals(SimpleJavaCodeGenerator.TYPE_INTERFACE)) print( " extends "+fullSuperName);
            else throw new UnsupportedOperationException("Cannot draw object start for type ["+type+"]");
        }
        println(" {");
    }

    protected void drawObjectEnd() {
        println(true,"}");
    }

    protected void drawGetter(String getterName, String valueClass) {
        if ( !type.equals(SimpleJavaCodeGenerator.TYPE_INTERFACE))
            throw new UnsupportedOperationException("Cannot draw method ["+getterName+"] for type ["+type+"]");

        // Print JavaDocs
        println(true,"/**");
        println(true," * "+getterName+" is a code generated Getter method");
        println(true," * @returns "+valueClass+" Value to get");
        println(true," */");

        // Print Method declaration
        println( true, "public "+valueClass+" "+getterName+"();");
    }

    protected void drawSetter(String setterName, String valueName, String valueClass) {
        if ( !type.equals(SimpleJavaCodeGenerator.TYPE_INTERFACE))
            throw new UnsupportedOperationException("Cannot draw method ["+setterName+"] for type ["+type+"]");

        // Print JavaDocs
        println(true,"/**");
        println(true," * "+setterName+" is a code generated Setter method");
        println(true," * @param "+valueName+" Value to set");
        println(true," */");

        // Print Method declaration
        println( true, "public void "+setterName+"("+valueClass+" "+valueName+");");
    }

    protected void drawNewLine() {
        println();
    }

    private boolean isNotBlank(String str) {
        return str != null && !str.trim().isEmpty();
    }
}
