package com.draagon.meta.generator.direct.plantuml;

import com.draagon.meta.DataTypes;
import com.draagon.meta.MetaData;
import com.draagon.meta.attr.MetaAttribute;
import com.draagon.meta.field.MetaField;
import com.draagon.meta.generator.GeneratorMetaException;
import com.draagon.meta.generator.MetaDataWriterException;
import com.draagon.meta.generator.direct.FileDirectWriter;
import static com.draagon.meta.generator.util.GeneratorUtil.*;

import com.draagon.meta.generator.MetaDataFilters;
import com.draagon.meta.generator.util.GeneratorUtil;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.relation.ref.*;
import com.draagon.meta.util.MetaDataUtil;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;

public class PlantUMLWriter extends FileDirectWriter<PlantUMLWriter> {

    protected boolean showAttrs = false;
    protected boolean showAbstracts = true;
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

    public PlantUMLWriter showAbstracts( boolean showAbstracts ) {
        this.showAbstracts = showAbstracts;
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


    //////////////////////////////////////////////////////////////////////
    // UML Write Logic methods

    public void writeUML() throws MetaDataWriterException {

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
            throw new MetaDataWriterException( this, "Error writing PlantUML: "+e, e);
        }
    }

    protected void writeObjectPackages() throws IOException {

        List<String> pkgs = getUniquePackages(objects());

        writeEmptyPackageNesting( pkgs );

        for ( String p : pkgs ) {

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
        iterateWriteObjectFieldSection(mo, true);
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

    protected void writeObjectRefRelationships(MetaObject mo ) throws IOException {

        // Write Fields
        boolean includeParentData = mo.getSuperObject() != null && isAbstract(mo.getSuperObject()) && !showAbstracts;

        for (MetaField f : mo.getMetaFields(includeParentData)) {

            ObjectReference oref = (ObjectReference)f.getFirstChild(ObjectReference.class);
            if ( oref != null ) {

                MetaObject objRef = oref.getReferencedObject();
                if (objRef != null) {

                    if (isAbstract( objRef ) && !showAbstracts ) {
                        getDerivedObjects(objRef)
                                .stream()
                                .filter( o -> !isAbstract(o) || (isAbstract(o) && !showAbstracts))
                                .filter( o -> objects().contains( o ))
                                .forEach( o -> drawObjectKeyReference(mo, f, oref, o));
                    }
                    else if (objects().contains( objRef )){
                        drawObjectKeyReference(mo, f, oref, objRef);
                    }
                }
            }
            else {
                MetaObject objRef = getMetaObjectRef(f);
                if ( objRef != null ) {

                    if (isAbstract( objRef ) && !showAbstracts ) {
                        getDerivedObjects(objRef)
                                .stream()
                                .filter( o -> !isAbstract(o) || (isAbstract(o) && !showAbstracts))
                                .filter( o -> objects().contains( o ))
                                .forEach( o -> drawObjectReference(mo, f, o));
                    }
                    else if (objects().contains( objRef )) {
                        drawObjectReference(mo, f, objRef);
                    }
                }
            }
        }
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
        catch( Exception e ) { return null; }
    }

    protected Collection<MetaObject> getDerivedObjects(MetaObject metaObject) {
        Collection<MetaObject> out = new ArrayList<>();
        for ( MetaObject mo : getLoader().getChildren(MetaObject.class) ) {
            if ( metaObject.equals(mo.getSuperObject())) {
                if ( isAbstract( mo ) && !showAbstracts )
                    out.addAll( getDerivedObjects( mo ));
                else
                    out.add( mo );
            }
        }
        return out;
    }

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
        else if ( attr.getName().equals( ObjectReference.ATTR_REFERENCE)
                || attr.getName().equals( "objectRef" )) {
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

    protected void drawObjectKeyReference(MetaObject mo, MetaField f, ObjectReference oref, MetaObject objRef ) {
        print(true,_pu(mo,true) +" ");
        if (oref instanceof OneToOneReference) print("\"1\" --> \"1\"");
        else if (oref instanceof OneToManyReference) print("\"1\" --> \"many\"");
        else if (oref instanceof ManyToOneReference) print("\"many\" --> \"1\"");
        else if (oref instanceof ManyToManyReference) print("\"many\" --> \"many\"");
        else print("-->");
        println(" "+ _pu(objRef,true) +" : "+ _pu(f));
        //if (oref.getName() == null) println(_pu(f));
        // else println(_pu(f) + ":" + _pu(oref));
    }

    protected void drawObjectReference(MetaObject mo, MetaField f, MetaObject objRef) {
        println(true, _pu(mo, true) +" --> "+ _pu(objRef, true) +" : "+ _pu(f));
    }

    protected void drawNewLine() {
        println();
    }
}
