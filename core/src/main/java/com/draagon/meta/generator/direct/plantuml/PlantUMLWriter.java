package com.draagon.meta.generator.direct.plantuml;

import com.draagon.meta.DataTypes;
import com.draagon.meta.MetaData;
import com.draagon.meta.attr.MetaAttribute;
import com.draagon.meta.field.MetaField;
import com.draagon.meta.field.ObjectArrayField;
import com.draagon.meta.field.ObjectField;
import com.draagon.meta.generator.GeneratorIOException;
import com.draagon.meta.generator.direct.FileDirectWriter;
import static com.draagon.meta.generator.util.GeneratorUtil.*;

import com.draagon.meta.generator.util.GeneratorUtil;
import com.draagon.meta.key.ForeignKey;
import com.draagon.meta.key.MetaKey;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.util.MetaDataUtil;
import com.draagon.meta.validator.ArrayValidator;
import com.draagon.meta.validator.MetaValidator;
import com.draagon.meta.validator.RequiredValidator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;

public class PlantUMLWriter extends FileDirectWriter<PlantUMLWriter> {

    protected Log log = LogFactory.getLog( getClass().getName() );

    public final static String ATTR_ISEMBEDDED ="isEmbedded";

    protected Boolean showAttrs = null;
    protected Boolean showFields = null;
    protected Boolean showAbstracts = null;
    protected String embeddedName = null;
    protected List<String> embeddedValues = null;
    protected Boolean debug = null;

    protected Collection<MetaObject> filteredObjects;

    public PlantUMLWriter( MetaDataLoader loader, PrintWriter pw ) {
        super( loader, pw );
    }

    //////////////////////////////////////////////////////////////////////
    // Options

    public PlantUMLWriter showAttrs( boolean showAttrs ) {
        this.showAttrs = showAttrs;
        return this;
    }

    public PlantUMLWriter showFields( boolean showFields ) {
        this.showFields = showFields;
        return this;
    }

    public PlantUMLWriter showAbstracts( boolean showAbstracts ) {
        this.showAbstracts = showAbstracts;
        return this;
    }

    public PlantUMLWriter setDebug( boolean debug ) {
        this.debug = debug;
        return this;
    }

    public PlantUMLWriter useEmbeddedNameAndValues( String name, List<String> values ) {
        this.embeddedName = name;
        this.embeddedValues = values;
        return this;
    }

    protected Collection<MetaObject> objects() {
        if ( filteredObjects == null ) {
            filteredObjects = GeneratorUtil.getFilteredMetaData(getLoader(), MetaObject.class, getFilters() )
                    .stream()
                    .filter(mo -> shouldShowObject(mo))
                    .collect(Collectors.toList());
        }
        return filteredObjects;
    }

    protected void setDefaultOptions() {

        if (showAttrs == null) showAttrs = false;
        if (showFields == null) showFields = true;
        if (showAbstracts == null) showAbstracts = true;
        if (embeddedName == null) {
            embeddedName = ATTR_ISEMBEDDED;
            embeddedValues = Arrays.asList(Boolean.TRUE.toString());
        }
        if ( debug == null) debug=false;
    }

    //////////////////////////////////////////////////////////////////////
    // UML Write Logic methods

    public void writeUML() throws GeneratorIOException {

        setDefaultOptions();

        if ( debug ) {
            log.info("Writing PlantUML for file: " + getFilename() );
            log.info("showAttrs:      " + showAttrs );
            log.info("showAbstracts:  " + showAbstracts );
            log.info("embeddedName:   " + embeddedName );
            log.info("embeddedValues: " + embeddedValues );
        }

        try {
            // Write start of UML file
            drawFileStart();

            // Write object packages (namespaces in PlantUML)
            writeObjectPackages();

            drawNewLine();

            // Write object relationships
            writeRelationships();

            // Write end of UML file
            drawFileEnd();
        }
        catch( IOException e ) {
            throw new GeneratorIOException( this, "Error writing PlantUML: "+e, e);
        }
    }

    protected List<String> getUniqueObjectPackages(Collection<MetaObject> filtered ) throws IOException {
        List<String> pkgs = new ArrayList<>();

        filtered.forEach( md -> {
            if ( !isEmbedded( md )
                    && !pkgs.contains( md.getPackage() )) {
                pkgs.add( md.getPackage() );
            }
        });

        return pkgs;
    }

    protected void writeObjectPackages() throws IOException {

        List<String> pkgs = getUniqueObjectPackages(objects());

        writeEmptyPackageNesting( pkgs );

        for ( String p : pkgs ) {

            if ( debug ) log.info("writingObjectPackage: " + p );

            if (!p.isEmpty()) drawNamespaceStart( p );

            // Write MetaObjects
            inc();
            for (MetaObject mo : objects()) {
                if ( mo.getPackage().equals( p )) {
                    writeMetaObject( mo );
                }
            }
            dec();

            if (!p.isEmpty()) drawNamespaceEnd();
        }
    }

    protected void writeEmptyPackageNesting(List<String> pkgsIn) {
        List<String> pkgs = new ArrayList<>( pkgsIn );
        for( String pkg : pkgsIn ) {
            String p = pkg.substring( 0, pkg.lastIndexOf( "::" ));
            if ( !p.isEmpty()
                    && !pkgs.contains(p)) {
                if ( hasDifferentSubPackages( p, pkgsIn )) {
                    drawNamespaceStart(p);
                    drawNamespaceEnd();
                }
                pkgs.add( p );
            }
        }
    }

    protected boolean hasDifferentSubPackages(String p, List<String> pkgsIn) {
        String lastFound  = null;
        for( String in : pkgsIn ) {

            int i = p.length()+2;
            if ( in.startsWith( p ) && in.length() > i) {

                int j = in.indexOf( "::", i);
                if ( j == -1 ) j = in.length();

                String found = in.substring( i, j );
                if ( !found.isEmpty() ) {
                    if (lastFound != null && !lastFound.equals( found )) {
                        return true;
                    }
                    lastFound = found;
                }
            }
        }
        return false;
    }

    protected void writeRelationships() throws IOException {

        for (MetaObject mo : objects()) {

            if ( isEmbedded( mo )) {
                if ( debug ) log.info("writingRelationship: (skipEmbedded)" + mo.getName() );
                continue;
            }

            if ( debug ) log.info("writingRelationship: " + mo.getName() );

            writeObjectParentRelationships(mo);

            // Write ObjectRef relationships
            writeObjectRefRelationships(mo);
        }
    }

    protected void writeObjectParentRelationships(MetaObject mo) {

        // Write super object relationships (extends)
        if ( mo.getSuperObject() != null
                && shouldShowObject( mo.getSuperObject() )
                && objects().contains( mo.getSuperObject() )) {

            drawObjectSuperReference(mo, mo.getSuperObject() );
        }
    }

    protected void writeMetaObject(MetaObject mo ) throws IOException {

        if ( isEmbedded( mo )) {
            if ( debug ) log.info("writingObject: (skipEmbedded)" + mo.getName() );
            return;
        }

        if ( debug ) log.info("writingObject: " + mo.getName() );

        drawObjectStart(mo);

        inc();
        writeObjectSections(mo);
        dec();

        drawObjectEnd();
    }

    protected void writeObjectSections(MetaObject mo) {

        writeObjectDetailsSection(mo);
        writeObjectAttrSection(mo);
        writeObjectFieldSection(mo);
    }

    protected void writeObjectDetailsSection(MetaObject mo) {

        drawObjectDetailsHeader();

        inc();
        drawObjectMetaDataType(mo);

        MetaObject parent = mo.getSuperObject();
        if ( parent != null ) { //&& isAbstract(parent)) {
            drawObjectExtension(mo, parent);
        }
        dec();
    }

    protected void writeObjectAttrSection(MetaObject mo) {

        List<MetaAttribute> attrs = mo.getMetaAttrs();
        if ( showAttrs & !attrs.isEmpty()) {

            drawObjectAttrHeader(mo);
            inc();
            attrs.forEach( a -> drawObjectAttr(a));
            dec();
        }
    }

    protected void writeObjectFieldSection(MetaObject mo) {
        if ( showFields ) {
            iterateWriteObjectFieldSection(mo, true);
        }
    }

    protected void iterateWriteObjectFieldSection(MetaObject mo, boolean primary ) {

        // Write Object Fields
        Collection<MetaField> fields = mo.getMetaFields( false);
        if ( !fields.isEmpty() ) {
            drawObjectFieldSection(primary, mo );
            writeObjectFields(primary, mo, fields);
        }

        // Write parent fields if we're not showing abstracts or the parent not on the objects list
        MetaObject parent = mo.getSuperObject();
        if ( parent != null && ((isAbstract(parent) && !showAbstracts)
                || !objects().contains(parent))) {

            iterateWriteObjectFieldSection(parent, false );
        }
    }

    protected void writeObjectFields(boolean primary, MetaObject mo, Collection<MetaField> fields) {

        inc();

        for (MetaField f : fields) {

            if ( !isAbstract(f) && (!showAbstracts && isAbstract( f ))) continue;

            MetaData oref = getMetaObjectRef(f);
            if ( oref != null ) {
                drawObjectFieldWithRef(primary, mo, f, oref);
            }
            else if ( f.getSuperField() != null ) {
                drawObjectFieldWithSuper(primary, mo, f);
            }
            else {
                drawObjectField(primary, mo, f);
            }

            if ( showAttrs ) {
                List<MetaAttribute> attrs = f.getMetaAttrs( !showAbstracts );
                attrs.forEach( a -> drawFieldAttr(f, a));
            }

        }

        dec();
    }

    protected List<ForeignKey> getForeignKeys(MetaObject mo, boolean includeParentData) {
        return mo.getChildren(ForeignKey.class, includeParentData);
    }

    protected void writeObjectRefRelationships(MetaObject mo ) throws IOException {

        // Write Fields
        boolean includeParentData = mo.getSuperObject() != null && isAbstract(mo.getSuperObject()) && !showAbstracts;

        for (ForeignKey foreignKey : getForeignKeys(mo,includeParentData)) {

            MetaObject foreignObject = foreignKey.getForeignObject();
            if (foreignObject != null) {

                if ( debug ) log.info("writeObjectRef: "+mo.getName()+"  -->  "+foreignObject.getName());

                String min = "0";
                String max = "many";

                // If it's just skipping abstract, then look for parents
                //if (isEmbedded(foreignObject)) {
                //    if ( debug ) log.info("writeObjectRef: find superObject !ignore Embedded! "+mo.getName()+"  -->  "+foreignObject.getName());
                //}
                //else
                if ((isAbstract(foreignObject) && !showAbstracts)) {
                    getDerivedObjects(foreignObject)
                            .stream()
                            //.filter(o -> isEmbedded(o) || !isAbstract(o) || (isAbstract(o) && !showAbstracts))
                            .filter(o -> objects().contains(o))
                            .forEach(o -> drawObjectReference(mo, foreignKey, o));
                }
                // It's not abstract, so just draw it here
                else if (objects().contains(foreignObject)) {
                    drawObjectReference(mo, foreignKey, foreignObject);
                }
            }
        }
    }

    protected void writeObjectRefRelationshipsViaObjectRef(MetaObject mo ) throws IOException {

        // Write Fields
        boolean includeParentData = mo.getSuperObject() != null && isAbstract(mo.getSuperObject()) && !showAbstracts;

        for (MetaField f : mo.getMetaFields(includeParentData)) {

            MetaObject objRef = getMetaObjectRef(f);
            if (objRef != null) {

                if ( debug ) log.info("writeObjectRef: "+mo.getName()+"  -->  "+objRef.getName());

                String min = getRefMinOrMax( f, false );
                String max = getRefMinOrMax( f, true );

                // If it's just skipping abstract, then look for parents
                if (isEmbedded(objRef)) {
                    if ( debug ) log.info("writeObjectRef: find superObject !ignore Embedded! "+mo.getName()+"  -->  "+objRef.getName());
                    /*MetaObject superObject = objRef.getSuperObject();
                    while ( superObject != null ) {
                        if ((isAbstract(superObject) && !showAbstracts) || isEmbedded( superObject)) {
                            superObject = superObject.getSuperObject();
                        } else {
                            break;
                        }
                    }
                    if ( superObject != null ) {
                        drawObjectReference(mo, f, min, max, superObject);
                        //getDerivedObjects(superObject)
                        //        .stream()
                                //.filter(o -> isEmbedded(o) || !isAbstract(o) || (isAbstract(o) && !showAbstracts))
                        //        .filter(o -> objects().contains(o))
                        //        .forEach(o -> drawObjectReference(mo, f, min, max, o));
                    }
                    else {
                        if ( debug ) log.info("writeObjectRef: find superObject !Not Found! "+mo.getName()+"  -->  "+objRef.getName());
                    }*/
                }
                else if ((isAbstract(objRef) && !showAbstracts)) {
                    getDerivedObjects(objRef)
                            .stream()
                            //.filter(o -> isEmbedded(o) || !isAbstract(o) || (isAbstract(o) && !showAbstracts))
                            .filter(o -> objects().contains(o))
                            .forEach(o -> drawObjectReference(mo, f, min, max, o));
                }
                // If it's Embedded, then find the first valid super parent
                else if (objects().contains(objRef)) {
                    drawObjectReference(mo, f, min, max, objRef);
                }
            }
        }
    }

    protected String getRefMinOrMax( MetaField<?> f, boolean forMax ) {

        // Object Arrays
        if ( f instanceof ObjectArrayField ) {
            ArrayValidator v = getValidatorOfType( f, ArrayValidator.class );
            if (v != null) {
                if (!forMax)
                    return String.valueOf(v.getMinSize());
                else if (v.hasMaxSize())
                    return String.valueOf(v.getMaxSize());
            }
            else if (!forMax) return "0";

            return "*";
        }
        // Other fields
        else {
            RequiredValidator v = getValidatorOfType( f, RequiredValidator.class );
            if ( v == null && !forMax ) return "0";
            else return "1";
        }
        //else {
        //    throw new IllegalStateException("Field must be ObjectField or ObjectArrayField to get Min or Max references");
        //}
    }

    protected <T extends MetaValidator> T getValidatorOfType( MetaField<?> f, Class<T> clazz ) {
        for (MetaValidator v : f.getValidators() ) {
            if ( clazz.isAssignableFrom( v.getClass() )) return (T) v;
        }
        return null;
    }

    protected boolean isEmbedded( MetaObject mo ) {
        if ( embeddedName != null && mo.hasMetaAttr( embeddedName )) {
            if ( embeddedValues != null ) {
                MetaAttribute a = mo.getMetaAttr(embeddedName);
                // NOTE:  Surround ""'s are to capture null as a string
                return embeddedValues.contains( ""+a.getValueAsString()+"" );
            } else {
                return true;
            }
        }
        return false;
    }


    //////////////////////////////////////////////////////////////////////
    // Logic Utility methods

    protected boolean shouldShowObject(MetaObject mo) {
        return !isAbstract(mo)
                || (isAbstract(mo)
                && showAbstracts);
    }

    protected MetaObject getMetaObjectRef(MetaField f) {
        try { return MetaDataUtil.getObjectRef( f ); }
        catch( Exception ignore ) { return null; }
    }

    protected Collection<MetaObject> getDerivedObjects(MetaObject metaObject) {
        if ( debug ) log.info("-- derivedObjects: metaObject="+metaObject);
        Collection<MetaObject> out = new ArrayList<>();
        for ( MetaObject mo : getLoader().getChildren(MetaObject.class) ) {
            if ( metaObject.equals(mo.getSuperObject())) {
                if (isEmbedded(mo)) {
                    // Do nothing
                }
                else if (( isAbstract( mo ) && !showAbstracts ))
                    out.addAll( getDerivedObjects( mo ));
                else
                    out.add( mo );
            }
        }
        if ( debug ) log.info("-----[] "+out);
        return out;
    }

    /*
    protected Collection<MetaObject> getDerivedObjects(MetaObject metaObject) {
        List<MetaObject> out = new ArrayList<>();
        List<String> lineage = new ArrayList<>();
        lineage.add( metaObject.getName() );
        getDerivedObjects( out, lineage );
        return out;
    }

    protected void getDerivedObjects( List<MetaObject> out, List<String> lineage ) {

        if ( debug ) log.info("-- derivedObjects: lineage="+lineage);

        for ( MetaObject mo : getLoader().getChildren(MetaObject.class) ) {

            if ( mo.getSuperObject() != null && lineage.contains(mo.getSuperObject().getName())) {

                if (( isAbstract( mo ) && !showAbstracts ) || isEmbedded(mo)) {

                    //if ( !lineage.contains( mo.getName())) lineage.add( mo.getName() );
                    //getDerivedObjects( out, lineage );
                }
                else if ( !out.contains( mo )) {
                    out.add(mo);
                }
            }
        }

        if ( debug ) log.info("-----[] "+out);
    }
     */

    //////////////////////////////////////////////////////////////////////
    // Draw Helper methods

    protected String _slimPkg( String p1, String p2 ) {
        return toRelativePackage( p1, p2 );
    }

    protected String _pu( MetaData md ) {
        return _pu( md, false );
    }

    protected String _pu( MetaData md, boolean showPkg ) {
        return (showPkg ? _pp(md) : "" ) + toCamelCase( md.getShortName(), md instanceof MetaObject );
    }

    protected String _pp( MetaData md ) {
        return (md.getPackage().isEmpty()) ? "" : md.getPackage() + "::";
    }

    protected String getAttrValue(  MetaAttribute attr ) {

        Object val = attr.getValue();
        if ( val == null ) {
            return null;
        }
        // TODO:  This should be handled differently
        else if (attr.getName().equals(ObjectField.ATTR_OBJECTREF)) {
            return "\""+MetaDataUtil.expandPackageForPath(MetaDataUtil.findPackageForMetaData( attr ), attr.getValueAsString() )+"\"";
        }
        else if ( attr.getDataType() == DataTypes.STRING_ARRAY ) {
            return "\""+val.toString()+"\"";
        }
        else if ( val instanceof String) {
            return "\""+val.toString()+"\"";
        }
        else if ( val instanceof Boolean ) {
            return val.toString();
        }
        else if ( val instanceof Date) {
            return val.toString();
        }
        else if ( val instanceof Number ) {
            return val.toString();
        }
        else if ( val instanceof Float ) {
            return val.toString();
        }
        else if ( val instanceof Double ) {
            return val.toString();
        }
        else if ( val instanceof Properties) {
            StringBuilder b = new StringBuilder();
            for (Map.Entry<Object,Object> e : (((Properties) val).entrySet()) ) {
                if ( b.length() > 0 ) b.append( ',' );
                b.append( e.getKey() ).append(':').append( e.getValue() );
            }
            return b.toString();
        }
        else if ( val instanceof Class ) {
            return ((Class)val).getName();
        }
        else {
            return "...";
        }
    }

    //////////////////////////////////////////////////////////////////////
    // UML Draw methods

    protected void drawFileStart() {

        println("@startuml");
        println();
        println("skinparam class {");
        println("    BackgroundColor PaleGreen");
        println("    ArrowColor SeaGreen");
        println("    BorderColor DarkGreen");
        println("    BackgroundColor<<Abstract>> Wheat");
        println("    BorderColor<<Abstract>> Tomato");
        println("}");
        println("skinparam stereotypeCBackgroundColor YellowGreen");
        println("skinparam stereotypeCBackgroundColor<< Abstract >> DimGray");
        println();
        println("set namespaceSeparator ::");
        println();
    }

    protected void drawFileEnd() {
        println("@enduml");
    }

    protected void drawNamespaceStart(String pkg) {
        println("namespace " + pkg + " {" );
    }

    protected void drawNamespaceEnd() {
        println("}" );
    }

    protected void drawObjectStart(MetaObject mo) {
        print(true, "class "+ _pu(mo));
        if ( isAbstract( mo ) ) print(" << (A,#FF7700) Abstract");
        else print(" << (O,#AAAAFF)");
        println(" >> {");
    }

    protected void drawObjectEnd() {
        println(true,"}");
    }

    protected void drawObjectDetailsHeader() {
        println(true,".. Details ..");
    }

    protected void drawObjectMetaDataType(MetaObject mo) {
        println(true,"@type="+mo.getSubTypeName() );
    }

    protected void drawObjectExtension(MetaObject mo, MetaObject parent ) {
        println(true,"@extends="+_slimPkg( mo.getPackage(), parent.getPackage() )
                +parent.getShortName() );
    }

    protected void drawObjectAttrHeader(MetaObject mo) {
        println(true,".. Attributes ..");
    }

    protected void drawObjectAttr(MetaAttribute a) {
        println(true, _pu(a) +"="+getAttrValue(a));
    }

    protected void drawObjectFieldSection(boolean primary, MetaObject mo) {
        println(true, ".. " + (!primary?mo.getShortName()+" ":"") + "Fields ..");
    }

    protected void drawObjectField(boolean primary, MetaObject mo, MetaField f) {
        println(true,(primary?"+":"#")+ _pu(f) + " {"+f.getSubTypeName() + "}");
    }

    protected void drawObjectFieldWithSuper(boolean primary, MetaObject mo, MetaField f) {
        println(true,(primary?"+":"#")+ _pu(f) + " {"+f.getSubTypeName() + "}");
        println(true,"@super=" + _slimPkg( mo.getPackage(), f.getSuperField().getPackage() )
                + _pu( f.getSuperField()));
    }

    protected void drawObjectFieldWithRef(boolean primary, MetaObject mo, MetaField f, MetaData oref) {
        print(true,(primary?"+":"#")+" " + _pu(f) +" {");
        if ( f.getDataType().isArray() ) print("[] ");
        print( _slimPkg( mo.getPackage(), oref.getPackage()));
        print(oref.getShortName());
        println("}");
    }

    protected void drawFieldAttr(MetaField f, MetaAttribute a) {
        println(true,"  "+ _pu(a) + " {"+a.getSubTypeName()+"} ="+getAttrValue(a));
    }

    protected void drawObjectSuperReference(MetaObject mo, MetaObject parent ) {
        println( _pu(mo, true) +" ..|> " + _pu( parent, true) +" : extends" );
    }

    protected void drawObjectReference(MetaObject mo, MetaField f, String min, String max, MetaObject objRef ) {
        if ( debug ) log.info("-- drawObjectRef: "+mo.getName()+"  -->  "+objRef.getName());

        print(true,_pu(mo,true) +" ");
        print("\""+min+"\" --> \""+max+"\"");
        println(" "+ _pu(objRef,true) +" : "+ _pu(f));
    }

    protected void drawObjectReference(MetaObject mo, MetaKey key, MetaObject objRef) {
        println(true, _pu(mo, true) +" --> "+ _pu(objRef, true) +" : "+ _pu(key));
    }

    protected void drawNewLine() {
        println();
    }
}
