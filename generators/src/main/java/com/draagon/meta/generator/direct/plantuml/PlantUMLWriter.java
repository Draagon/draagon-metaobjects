package com.draagon.meta.generator.direct.plantuml;

import com.draagon.meta.DataTypes;
import com.draagon.meta.MetaData;
import com.draagon.meta.attr.MetaAttribute;
import com.draagon.meta.field.MetaField;
import com.draagon.meta.generator.GeneratorMetaException;
import com.draagon.meta.generator.direct.FileDirectWriter;
import static com.draagon.meta.generator.util.GeneratorUtil.*;

import com.draagon.meta.generator.direct.MetaDataFilters;
import com.draagon.meta.generator.util.GeneratorUtil;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.relation.ref.*;
import com.draagon.meta.util.MetaDataUtil;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class PlantUMLWriter extends FileDirectWriter<String> {

    protected final boolean showAttrs;
    protected final boolean showAbstracts;
    protected final List<MetaObject> objects;

    public PlantUMLWriter( MetaDataLoader loader ) {
        this( loader, null, false, true );
    }

    public PlantUMLWriter( MetaDataLoader loader, MetaDataFilters filters ) {
        this( loader, filters, false, true );
    }

    public PlantUMLWriter( MetaDataLoader loader, MetaDataFilters filters,
                           boolean showAttrs, boolean showAbstracts ) {

        super( loader, filters );

        this.showAttrs = showAttrs;
        this.showAbstracts = showAbstracts;

        objects = GeneratorUtil.getFilteredMetaData( loader, MetaObject.class, filters )
                .stream()
                .filter( mo -> shouldShowObject(mo) )
                .collect( Collectors.toList());
    }

    //////////////////////////////////////////////////////////////////////
    // UML Write Logic methods

    @Override
    protected void writeFile(Context c, String filename) {

        try {
            // Write start of UML file
            drawFileStart(c);

            // Write object packages (namespaces in PlantUML)
            writeObjectPackages(c);

            drawNewLine(c);

            // Write object relationships
            writeRelationships(c);

            // Write end of UML file
            drawFileEnd(c);
        }
        catch( IOException e ) {
            throw new GeneratorMetaException( "Error writing file ["+filename+"]: "+e, e);
        }
    }

    protected void writeObjectPackages(Context c) throws IOException {

        List<String> pkgs = getUniquePackages(objects);

        writeEmptyPackageNesting( c, pkgs );

        for ( String p : pkgs ) {

            if (!p.isEmpty()) drawNamespaceStart( c, p );

            // Write MetaObjects
            for (MetaObject mo : objects) {
                if ( mo.getPackage().equals( p )) {
                    writeMetaObject( c.inc(p), mo );
                }
            }

            if (!p.isEmpty()) drawNamespaceEnd( c );
        }
    }

    protected void writeEmptyPackageNesting(Context c, List<String> pkgsIn) {
        List<String> pkgs = new ArrayList<>( pkgsIn );
        for( String pkg : pkgsIn ) {
            String p = pkg.substring( 0, pkg.lastIndexOf( "::" ));
            if ( !p.isEmpty()
                    && !pkgs.contains( p )) {
                if ( hasDifferentSubPackages( p, pkgsIn )) {
                    drawNamespaceStart(c, p);
                    drawNamespaceEnd(c);
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

    protected void writeRelationships(Context c) throws IOException {

        for (MetaObject mo : objects) {

            writeObjectParentRelationships(c, mo);

            // Write ObjectRef relationships
            writeObjectRefRelationships(c, mo);
        }
    }

    protected void writeObjectParentRelationships(Context c, MetaObject mo) {

        // Write super object relationships (extends)
        if ( mo.getSuperObject() != null
                && shouldShowObject( mo.getSuperObject() )
                && objects.contains( mo.getSuperObject() )) {

            drawObjectSuperReference(c, mo, mo.getSuperObject() );
        }
    }

    protected void writeMetaObject(Context c, MetaObject mo ) throws IOException {

        drawObjectStart( c, mo );

        writeObjectSections(c.inc(mo.getName()), mo);

        drawObjectEnd(c);
    }

    protected void writeObjectSections(Context c, MetaObject mo) {

        writeObjectDetailsSection(c, mo);
        writeObjectAttrSection(c, mo);
        writeObjectFieldSection(c, mo);
    }

    protected void writeObjectDetailsSection(Context c, MetaObject mo) {

        drawObjectDetailsHeader(c);
        drawObjectMetaDataType(c.inc("@type"), mo);

        MetaObject parent = mo.getSuperObject();
        if ( parent != null ) { //&& isAbstract(parent)) {
            drawObjectExtension(c.inc("@super"), mo, parent);
        }
    }

    protected void writeObjectAttrSection(Context c, MetaObject mo) {

        List<MetaAttribute> attrs = mo.getMetaAttrs();
        if ( showAttrs & !attrs.isEmpty()) {

            drawObjectAttrHeader(c, mo);
            attrs.forEach( a -> drawObjectAttr(c.inc(a.getShortName()), a));
        }
    }

    protected void writeObjectFieldSection(Context c, MetaObject mo) {
        iterateWriteObjectFieldSection(c, mo, true);
    }

    protected void iterateWriteObjectFieldSection(Context c, MetaObject mo, boolean primary ) {

        // Write Object Fields
        Collection<MetaField> fields = mo.getMetaFields( false);
        if ( !fields.isEmpty() ) {
            drawObjectFieldSection(c, primary, mo );
            writeObjectFields(c, primary, mo, fields);
        }

        // Write parent fields if we're not showing abstracts or the parent not on the objects list
        MetaObject parent = mo.getSuperObject();
        if ( parent != null && ((isAbstract(parent) && !showAbstracts)
                || !objects.contains(parent))) {

            iterateWriteObjectFieldSection( c, parent, false );
        }
    }

    protected void writeObjectFields(Context c2, boolean primary, MetaObject mo, Collection<MetaField> fields) {

        for (MetaField f : fields) {

            final Context c = c2.inc( f.getShortName() );

            if ( !isAbstract(f) && (!showAbstracts && isAbstract( f ))) continue;

            MetaData oref = getMetaObjectRef(f);
            if ( oref != null ) {
                drawObjectFieldWithRef(c, primary, mo, f, oref);
            }
            else if ( f.getSuperField() != null ) {
                drawObjectFieldWithSuper(c, primary, mo, f);
            }
            else {
                drawObjectField(c, primary, mo, f);
            }

            if ( showAttrs ) {
                List<MetaAttribute> attrs = f.getMetaAttrs( !showAbstracts );
                attrs.forEach( a -> drawFieldAttr(c, f, a));
            }
        }
    }

    protected void writeObjectRefRelationships(Context c, MetaObject mo ) throws IOException {

        // Write Fields
        boolean includeParentData = mo.getSuperObject() != null && isAbstract(mo.getSuperObject()) && !showAbstracts;

        for (MetaField f : mo.getMetaFields(includeParentData)) {

            ObjectReference oref = f.getFirstObjectReference();
            if ( oref != null ) {

                MetaObject objRef = oref.getReferencedObject();
                if (objRef != null) {

                    if (isAbstract( objRef ) && !showAbstracts ) {
                        getDerivedObjects(c, objRef)
                                .stream()
                                .filter( o -> !isAbstract(o) || (isAbstract(o) && !showAbstracts))
                                .filter( o -> objects.contains( o ))
                                .forEach( o -> drawObjectKeyReference(c, mo, f, oref, o));
                    }
                    else if (objects.contains( objRef )){
                        drawObjectKeyReference(c, mo, f, oref, objRef);
                    }
                }
            }
            else {
                MetaObject objRef = getMetaObjectRef(f);
                if ( objRef != null ) {

                    if (isAbstract( objRef ) && !showAbstracts ) {
                        getDerivedObjects(c, objRef)
                                .stream()
                                .filter( o -> !isAbstract(o) || (isAbstract(o) && !showAbstracts))
                                .filter( o -> objects.contains( o ))
                                .forEach( o -> drawObjectReference(c, mo, f, o));
                    }
                    else if (objects.contains( objRef )) {
                        drawObjectReference(c, mo, f, objRef);
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

    protected Collection<MetaObject> getDerivedObjects(Context c, MetaObject metaObject ) {
        Collection<MetaObject> out = new ArrayList<>();
        for ( MetaObject mo : loader.getChildren(MetaObject.class) ) {
            if ( metaObject.equals(mo.getSuperObject())) {
                if ( isAbstract( mo ) && !showAbstracts )
                    out.addAll( getDerivedObjects( c, mo ));
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

    protected void drawFileStart(Context c ) {

        pn(c,"@startuml");
        pn(c);
        pn(c,"skinparam class {");
        pn(c,"    BackgroundColor PaleGreen");
        pn(c,"    ArrowColor SeaGreen");
        pn(c,"    BorderColor DarkGreen");
        pn(c,"    BackgroundColor<<Abstract>> Wheat");
        pn(c,"    BorderColor<<Abstract>> Tomato");
        pn(c,"}");
        pn(c,"skinparam stereotypeCBackgroundColor YellowGreen");
        pn(c,"skinparam stereotypeCBackgroundColor<< Abstract >> DimGray");
        pn(c);
        pn(c,"set namespaceSeparator ::");
        pn(c);
    }

    protected void drawFileEnd(Context c ) {
        pn(c,"@enduml");
    }

    protected void drawNamespaceStart(Context c, String pkg ) {
        pn(c,"namespace " + pkg + " {" );
    }

    protected void drawNamespaceEnd(Context c ) {
        pn(c,"}" );
    }

    protected void drawObjectStart(Context c, MetaObject mo ) {
        pr(c, c.indent + "class "+ _pu(mo));
        if ( isAbstract( mo ) ) pr(c, " << (A,#FF7700) Abstract");
        else pr(c, " << (O,#AAAAFF)");
        pn(c, " >> {");
    }

    protected void drawObjectEnd(Context c) {
        pn(c,c.indent+"}");
    }

    protected void drawObjectDetailsHeader(Context c) {
        pn(c,c.indent+".. Details ..");
    }

    protected void drawObjectMetaDataType(Context c, MetaObject mo) {
        pn(c,c.indent+"@type="+mo.getSubTypeName() );
    }

    protected void drawObjectExtension(Context c, MetaObject mo, MetaObject parent ) {
        pn(c,c.indent+"@extends="+_slimPkg( mo.getPackage(), parent.getPackage() )
                +parent.getShortName() );
    }

    protected void drawObjectAttrHeader(Context c, MetaObject mo) {
        pn(c,c.indent+".. Attributes ..");
    }

    protected void drawObjectAttr(Context c, MetaAttribute a) {
        pn(c,c.indent+ _pu(a) +"="+getAttrValue(a));
    }

    protected void drawObjectFieldSection(Context c, boolean primary, MetaObject mo) {
        pn(c,c.indent + ".. " + (!primary?mo.getShortName()+" ":"") + "Fields ..");
    }

    protected void drawObjectField(Context c, boolean primary, MetaObject mo, MetaField f) {
        pn(c,c.indent+(primary?"+":"#")+ _pu(f) + " {"+f.getSubTypeName() + "}");
    }

    protected void drawObjectFieldWithSuper(Context c, boolean primary, MetaObject mo, MetaField f) {
        pn(c,c.indent+(primary?"+":"#")+ _pu(f) + " {"+f.getSubTypeName() + "}");
        pn(c,c.indent+"@super=" + _slimPkg( mo.getPackage(), f.getSuperField().getPackage() )
                + _pu( f.getSuperField()));
    }

    protected void drawObjectFieldWithRef(Context c, boolean primary, MetaObject mo, MetaField f, MetaData oref) {
        pr(c,c.indent+(primary?"+":"#")+" " + _pu(f) +" {");
        if ( f.getDataType().isArray() ) pr(c,"[] ");
        pr(c, _slimPkg( mo.getPackage(), oref.getPackage()));
        pr(c,oref.getShortName());
        pn(c,"}");
    }

    protected void drawFieldAttr(Context c, MetaField f, MetaAttribute a) {
        pn(c,c.indent+"  "+ _pu(a) + " {"+a.getSubTypeName()+"} ="+getAttrValue(a));
    }

    protected void drawObjectSuperReference(Context c, MetaObject mo, MetaObject parent ) {
        pn(c, _pu(mo, true) +" ..|> " + _pu( parent, true) +" : extends" );
    }

    protected void drawObjectKeyReference(Context c, MetaObject mo, MetaField f, ObjectReference oref, MetaObject objRef ) {
        pr(c,c.indent+_pu(mo,true) +" ");
        if (oref instanceof OneToOneReference) pr(c,"\"1\" --> \"1\"");
        else if (oref instanceof OneToManyReference) pr(c,"\"1\" --> \"many\"");
        else if (oref instanceof ManyToOneReference) pr(c,"\"many\" --> \"1\"");
        else if (oref instanceof ManyToManyReference) pr(c,"\"many\" --> \"many\"");
        else pr(c,"-->");
        pn(c," "+ _pu(objRef,true) +" : "+ _pu(f));
        //if (oref.getName() == null) pn(c,_pu(f));
        // else pn(c,_pu(f) + ":" + _pu(oref));
    }

    protected void drawObjectReference(Context c, MetaObject mo, MetaField f, MetaObject objRef) {
        pn(c,c.indent + _pu(mo, true) +" --> "+ _pu(objRef, true) +" : "+ _pu(f));
    }

    protected void drawNewLine(Context c) {
        pn(c);
    }
}
