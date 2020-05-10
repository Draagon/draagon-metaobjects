package com.draagon.meta.generator.direct;

import com.draagon.meta.DataTypes;
import com.draagon.meta.MetaData;
import com.draagon.meta.attr.MetaAttribute;
import com.draagon.meta.field.MetaField;
import com.draagon.meta.generator.GeneratorMetaException;
import com.draagon.meta.generator.util.GeneratorUtil;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.relation.ref.*;
import com.draagon.meta.util.MetaDataUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;

public class PlantUMLGenerator extends DirectGeneratorBase<PlantUMLGenerator> {

    private Log log = LogFactory.getLog( this.getClass() );

    private boolean showAttrs = false;
    private boolean showAbstracts = true;

    /** Passes at context data for the execution */
    public static class Context {

        public final MetaDataLoader loader;
        public final PrintWriter pw;
        public final String indent;
        public final Collection<MetaObject> objects;

        public Context( MetaDataLoader loader, PrintWriter pw, String indent, Collection<MetaObject> objects ) {
            this.loader = loader;
            this.pw = pw;
            this.indent = indent;
            this.objects = objects;
        }
        public Context incIndent() {
            return new Context( loader, pw, indent + "  ", objects);
        }
    }

    @Override
    public void execute( MetaDataLoader loader ) {

        File outf = null;
        PrintWriter pw = null;

        parseArgs();

        try {
            // Create output file
            outf = new File(getOutputDir(), getOutputFilename());
            outf.createNewFile();

            // Get the printwriter
            pw = new PrintWriter(outf);

            // Get the filtered objects
            Collection<MetaObject> filteredObjects =
                    filterByConfig( getFilteredMetaObjects(loader));

            // Create the Context for passing through the writers
            Context c = new Context(
                    loader,
                    pw,
                    "",
                    filteredObjects );

            // Write start of UML file
            drawFileStart(c);

            // Write Packages
            writeObjectPackages(c);

            drawNewLine(c);

            writeRelationships(c);

            // Write end of UML file
            drawFileEnd(c);
        }
        catch( IOException e ) {
            throw new GeneratorMetaException( "Unable to write PlantUML to file [" + outf + "]: " + e, e );
        }
        finally {
            if ( pw != null ) pw.close();
        }
    }

    /** Filter more based on UML generator configuration */
    protected Collection<MetaObject> filterByConfig( Collection<MetaObject> objects ) {
        return objects
                .stream()
                .filter( mo -> shouldShowObject(mo) )
                .collect( Collectors.toList());
    }

    protected boolean shouldShowObject(MetaObject mo) {
        return !isAbstract(mo)
                || (isAbstract(mo)
                    && showAbstracts);
    }

    protected void parseArgs() {

        if ( hasArg( "showAttrs"))
            showAttrs = Boolean.parseBoolean( getArg( "showAttrs"));
        if ( hasArg( "showAbstracts"))
            showAbstracts = Boolean.parseBoolean( getArg( "showAbstracts"));

        if ( log.isDebugEnabled() ) {
            log.debug("PlantUML: showAttrs=" + showAttrs);
            log.debug("PlantUML: showAbstracts=" + showAbstracts);
        }
    }

    protected void writeObjectPackages(Context c) throws IOException {

        List<String> pkgs = getUniquePackages(c.objects);

        writeEmptyPackageNesting( c, pkgs );

        for ( String p : pkgs ) {

            if (!p.isEmpty()) drawNamespaceStart( c, p );

            // Write MetaObjects
            for (MetaObject mo : c.objects) {
                if ( mo.getPackage().equals( p )) {
                    writeMetaObject( c.incIndent(), mo );
                }
            }

            if (!p.isEmpty()) drawNamespaceEnd( c );
        }
    }

    protected void writeEmptyPackageNesting(Context c, List<String> pkgsIn) {
        List<String> pkgs = new ArrayList<>( pkgsIn );
        for( String pkg : pkgsIn ) {
            String p = pkg.substring( 0, pkg.lastIndexOf( "::" ));
            if ( !p.isEmpty() && !pkgs.contains( p )) {
                drawNamespaceStart( c, p );
                drawNamespaceEnd( c );
                pkgs.add( p );
            }
        }
    }

    protected void writeRelationships(Context c) throws IOException {

        for (MetaObject mo : c.objects) {

            // Write super object relationships (extends)
            if ( mo.getSuperObject() != null
                    && shouldShowObject( mo.getSuperObject() )) {
                drawObjectSuperReference(c, mo);
            }

            // Write ObjectRef relationships
            writeObjectRefRelationships(c, mo);
        }
    }

    protected void writeMetaObject(Context c, MetaObject mo ) throws IOException {

        drawObjectStart( c, mo );

        writeObjectInheritenceSection(c.incIndent(), mo);

        writeObjectAttrSection(c.incIndent(), mo);

        writeObjectFieldSection(c.incIndent(), mo);

        drawObjectEnd(c);
    }

    protected void writeObjectInheritenceSection(Context c, MetaObject mo) {
        if ( !showAbstracts ) {
            MetaObject parent = mo.getSuperObject();
            if ( parent != null && isAbstract(parent)) {
                drawObjectInheritanceHeader(c);
                drawObjectExtension(c, mo, parent);
            }
        }
    }

    protected void writeObjectAttrSection(Context c, MetaObject mo) {
        if ( showAttrs ) {
            drawObjectAttrHeader(c, mo);

            List<MetaAttribute> attrs = (List<MetaAttribute>) mo.getChildren(MetaAttribute.class, true);
            if (!attrs.isEmpty()) {
                attrs.forEach( a -> drawObjectAttr(c.incIndent(), a));
            }
        }
    }

    protected void writeObjectFieldSection(Context c, MetaObject mo) {

        // Write Fields
        Collection<MetaField> fields = mo.getMetaFields();
        if ( !fields.isEmpty() ) {

            drawObjectFieldSection(c, "Fields");

            fields = mo.getMetaFields( false);
            writeObjectFields(c, fields);

            MetaObject parent = mo.getSuperObject();
            if ( parent != null ) {
                if (isAbstract(parent) && !showAbstracts) {

                    drawObjectFieldSection(c, parent.getShortName() + " Fields");
                    writeObjectFields(c, parent.getMetaFields());
                }
                //else if (fields.isEmpty()) {
                //   drawObjectFieldSection(c, parent.getShortName());
                //}
            }
        }
        else {
            drawObjectFieldSection(c, "No Fields");
        }
    }

    protected void writeObjectFields(Context c, Collection<MetaField> fields) {

        for (MetaField f : fields) {

            if ( !isAbstract(f) && (!showAbstracts && isAbstract( f ))) continue;

            MetaData oref = getMetaObjectRef(f);
            if ( oref != null ) {
                drawObjectFieldWithRef(c.incIndent(), f, oref);
            }
            else if ( f.getSuperField() != null ) {
                drawObjectFieldWithSuper(c.incIndent(), f);
            }
            else {
                drawObjectField(c.incIndent(), f);
            }

            if ( showAttrs ) {
                List<MetaAttribute> attrs = f.getMetaAttrs( !showAbstracts );
                attrs.forEach( a -> drawFieldAttr(c.incIndent(), f, a));
            }
        }
    }

    protected MetaObject getMetaObjectRef(MetaField f) {
        try { return MetaDataUtil.getObjectRef( f ); } 
        catch( Exception e ) { return null; }
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
                                .filter( o -> c.objects.contains( o ))
                                .forEach( o -> drawObjectKeyReference(c, f, oref, o));
                    }
                    else if (c.objects.contains( objRef )){
                        drawObjectKeyReference(c, f, oref, objRef);
                    }
                }
            }
            else {
                MetaObject objRef = getMetaObjectRef(f);
                if ( objRef != null
                        && c.objects.contains(objRef)) {

                    drawObjectReference(c, f, objRef);
                }
            }
        }
    }

    protected Collection<MetaObject> getDerivedObjects(Context c, MetaObject metaObject ) {
         Collection<MetaObject> out = new ArrayList<>();
         for ( MetaObject mo : c.loader.getChildren(MetaObject.class) ) {
             if ( metaObject.equals(mo.getSuperObject())) {
                 if ( isAbstract( mo ) && !showAbstracts )
                     out.addAll( getDerivedObjects( c, mo ));
                 else
                    out.add( mo );
             }
         }
         return out;
    }

    protected String _slimPkg( String p1, String p2 ) {
       return GeneratorUtil.toRelativePackage( p1, p2 );
    }

    protected String _pu( MetaData md ) {
        return _pu( md, false );
    }

    protected String _pu( MetaData md, boolean showPkg ) {
        return (showPkg ? _pp(md) : "" ) + GeneratorUtil.toCamelCase( md.getShortName(), md instanceof MetaObject );
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

    protected boolean isAbstract( MetaData md ) {
        if ( md.hasAttr("_isAbstract")
                && Boolean.TRUE.equals( md.getMetaAttr( "_isAbstract" ).getValue())) {
            return true;
        }
        return false;
    }

    //////////////////////////////////////////////////////////////////////
    // UML Output methods

    protected void drawFileStart(Context c ) {

        c.pw.println("@startuml");
        c.pw.println();
        c.pw.println("skinparam class {");
        c.pw.println("    BackgroundColor PaleGreen");
        c.pw.println("    ArrowColor SeaGreen");
        c.pw.println("    BorderColor DarkGreen");
        c.pw.println("    BackgroundColor<<Abstract>> Wheat");
        c.pw.println("    BorderColor<<Abstract>> Tomato");
        c.pw.println("}");
        c.pw.println("skinparam stereotypeCBackgroundColor YellowGreen");
        c.pw.println("skinparam stereotypeCBackgroundColor<< Abstract >> DimGray");
        c.pw.println();
        c.pw.println("set namespaceSeparator ::");
        c.pw.println();
    }

    protected void drawFileEnd(Context c ) {
        c.pw.println("@enduml");
    }

    protected void drawNamespaceStart(Context c, String pkg ) {
        c.pw.println("namespace " + pkg + " {" );
    }

    protected void drawNamespaceEnd(Context c ) {
        c.pw.println("}" );
    }

    protected void drawObjectStart( Context c, MetaObject mo ) {
        c.pw.print( c.indent + "class "+ _pu(mo));
        if ( isAbstract( mo ) ) c.pw.print( " << (A,#FF7700) Abstract");
        else c.pw.print( " << (O,#AAAAFF)");
        c.pw.println( " >> {");
    }

    protected void drawObjectEnd(Context c) {
        c.pw.println(c.indent+"}");
    }

    protected void drawObjectInheritanceHeader(Context c) {
        c.pw.println(c.indent+".. Inheritance ..");
    }

    protected void drawObjectExtension(Context c, MetaObject mo, MetaObject parent ) {
        c.pw.println(c.indent+"extends "+_slimPkg( mo.getPackage(), parent.getPackage() )
                +parent.getShortName() );
    }

    protected void drawObjectAttrHeader(Context c, MetaObject mo) {
        c.pw.println(c.indent+".. Attributes ..");
        c.pw.println(c.indent+"@type="+mo.getSubTypeName() );
    }

    protected void drawObjectAttr(Context c, MetaAttribute a) {
        c.pw.println(c.indent+ _pu(a) +"="+getAttrValue(a));
    }

    protected void drawObjectFieldSection(Context c, String s) {
        c.pw.println(c.indent + ".. " + s + " ..");
    }

    protected void drawObjectField(Context c, MetaField f) {
        c.pw.print(c.indent+"+ " + _pu(f) + " ");
        c.pw.println("{"+f.getSubTypeName() + "}");
    }

    protected void drawObjectFieldWithSuper(Context c, MetaField f) {
        c.pw.print(c.indent+"+ " + _pu(f) + " ");
        c.pw.println("{"+f.getSubTypeName() + "}");
        c.pw.println(c.indent+"super="
                +  _slimPkg( f.getParent().getPackage(), f.getSuperField().getPackage() )
                + _pu( f.getSuperField()));
    }

    protected void drawObjectFieldWithRef(Context c, MetaField f, MetaData oref) {
        c.pw.print(c.indent+"+ " + _pu(f) +" {");
        if ( f.getDataType().isArray() ) c.pw.print("[] " );
        c.pw.print( _slimPkg( f.getParent().getPackage(), oref.getPackage()));
        c.pw.print(oref.getShortName());
        c.pw.println("}");
    }

    protected void drawFieldAttr(Context c, MetaField f, MetaAttribute a) {
        c.pw.print(c.indent+" [attr:"+a.getSubTypeName() + "] ");
        c.pw.println( _pu(a) +"="+getAttrValue(a));
    }

    protected void drawObjectSuperReference(Context c, MetaObject mo) {
        c.pw.println( _pu(mo, true) +" ..|> "
                + _pu( mo.getSuperObject(), true) +" : extends" );
    }

    protected void drawObjectKeyReference(Context c, MetaField f, ObjectReference oref, MetaObject objRef ) {
        c.pw.print(c.indent+_pu((MetaObject)f.getParent(),true));
        if (oref instanceof OneToOneReference) c.pw.print("\"1\" --> \"1\"");
        else if (oref instanceof OneToManyReference) c.pw.print("\"1\" --> \"many\"");
        else if (oref instanceof ManyToOneReference) c.pw.print("\"many\" --> \"1\"");
        else if (oref instanceof ManyToManyReference) c.pw.print("\"many\" --> \"many\"");
        else c.pw.print(" --> ");
        c.pw.print(_pu(objRef,true) +" : ");
        if (oref.getName() == null) c.pw.println(_pu(f));
        else c.pw.println(_pu(f) + ":" + _pu(oref));
    }

    protected void drawObjectReference(Context c, MetaField f, MetaObject objRef) {
        c.pw.println(c.indent + _pu(f.getParent())
                +" --> "+ _pu(objRef) +" : "+ _pu(f));
    }

    protected void drawNewLine(Context c) {
        c.pw.println();
    }
}
