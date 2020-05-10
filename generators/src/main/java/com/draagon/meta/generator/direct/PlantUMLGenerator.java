package com.draagon.meta.generator.direct;

import com.draagon.meta.DataTypes;
import com.draagon.meta.MetaData;
import com.draagon.meta.attr.MetaAttribute;
import com.draagon.meta.field.MetaField;
import com.draagon.meta.generator.GeneratorMetaException;
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

public class PlantUMLGenerator extends DirectGeneratorBase<PlantUMLGenerator> {

    private Log log = LogFactory.getLog( this.getClass() );

    private boolean showAttrs = false;
    private boolean showAbstracts = true;

    /** Passes at context data for the execution */
    public static class Context {

        public final PrintWriter pw;
        public final String indent;
        public final Collection<MetaObject> objects;

        public Context( PrintWriter pw, String indent, Collection<MetaObject> objects ) {
            this.pw = pw;
            this.indent = indent;
            this.objects = objects;
        }
        public Context copyAndInc() {
            return new Context( pw, indent + "  ", objects);
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
            Collection<MetaObject> filteredObjects = getFilteredMetaObjects(loader);

            // Create the Context for passing through the writers
            Context c = new Context( pw, "", filteredObjects );

            // Write start of UML file
            drawFileStart(c);

            // Write Packages
            writePackages(c);

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

    protected void writePackages(Context c) throws IOException {

        for ( String p : getUniquePackages( c.objects )) {

            if (!p.isEmpty()) drawNamespaceStart( c, p );

            // Write MetaObjects
            writeMetaObjects(c, p);

            if (!p.isEmpty()) drawNamespaceEnd( c );

            // Write Object References
            renderObjectRelationships(c, p);
        }
    }

    protected void writeMetaObjects(Context c, String p) throws IOException {

        for (MetaObject mo : c.objects) {
            if ( mo.getPackage().equals( p )
                    && showIfAbstract(mo)) {

                renderMetaObject( c.copyAndInc(), mo );
            }
        }
    }


    protected void renderObjectRelationships(Context c, String p) throws IOException {

        for (MetaObject mo : c.objects) {
            if ( mo.getPackage().equals( p )) {
                renderObjectRelationships(c, mo);
            }
        }
    }

    protected void renderMetaObject(Context c, MetaObject mo ) throws IOException {

        drawObjectStart( c, mo );

        // Write Attributes
        writeObjectAttrSection( c.copyAndInc(), mo );

        writeObjectFieldSection(c, mo);

        drawObjectEnd(c);
    }

    protected void writeObjectAttrSection(Context c, MetaObject mo) {
        if ( showAttrs ) {
            drawObjectAttrHeader(c, mo);

            List<MetaAttribute> attrs = (List<MetaAttribute>) mo.getChildren(MetaAttribute.class, true);
            if (!attrs.isEmpty()) {
                attrs.forEach( a -> drawObjectAttr(c.copyAndInc(), a));
            }
        }
    }

    protected void writeObjectFieldSection(Context c, MetaObject mo) {

        // Write Fields
        Collection<MetaField> fields = mo.getMetaFields();
        if ( !fields.isEmpty() ) {

            drawObjectFieldSection(c, "Fields");

            fields = mo.getMetaFields( false);
            writeObjectFields(c.copyAndInc(), fields);

            MetaObject parent = mo.getSuperObject();
            if ( parent != null ) {
                if (isAbstract(parent) && !showAbstracts) {

                    drawObjectFieldSection(c, parent.getShortName() + " Fields");
                    writeObjectFields(c.copyAndInc(), parent.getMetaFields());
                }
                else if (fields.isEmpty()) {
                    drawObjectFieldSection(c, parent.getShortName());
                }
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
                drawObjectFieldWithRef(c.copyAndInc(), f, oref);
            }
            else if ( f.getSuperField() != null ) {
                drawObjectFieldWithSuper(c.copyAndInc(), f);
            }
            else {
                drawObjectField(c.copyAndInc(), f);
            }

            if ( showAttrs ) {
                List<MetaAttribute> attrs = f.getMetaAttrs( !showAbstracts );
                attrs.forEach( a -> drawFieldAttr(c.copyAndInc(), f, a));
            }
        }
    }

    protected MetaObject getMetaObjectRef(MetaField f) {
        try { return MetaDataUtil.getObjectRef( f ); } 
        catch( Exception e ) { return null; }
    }

    protected void renderObjectRelationships(Context c, MetaObject mo ) throws IOException {

        if ( mo.getSuperObject() != null
                && showIfAbstract(mo.getSuperObject())) {

            drawObjectSuperReference(c, mo);
        }

        // Write Fields
        List<MetaField> fields = (List<MetaField>) mo.getChildren(MetaField.class, false);
        if ( !fields.isEmpty() ) {

            drawNewLine(c);

            for (MetaField f : fields) {

                ObjectReference oref = f.getFirstObjectReference();
                if ( oref != null ) {
                    
                    MetaObject objRef = oref.getReferencedObject();
                    if (objRef != null
                            && c.objects.contains(objRef)
                            && showIfAbstract(objRef)) {

                        drawObjectKeyReference(c, f, oref);
                    }
                }
                else {
                    MetaObject objRef = getMetaObjectRef(f);
                    if ( objRef != null
                            && c.objects.contains(objRef)
                            && showIfAbstract(objRef)) {

                        drawObjectReference(c, f, objRef);
                    }
                }
            }
        }
    }

    protected String _slimPkg( String p1, String p2 ) {

        if (p1 == null || p1.isEmpty()
            || p2 == null || p2.isEmpty() ) {
            return "";
        }

        if ( p2.length() < p1.length() ) {
            return "...::";
        }

        StringBuilder converted = new StringBuilder();

        boolean skip = false;
        for (int i = 0; i < p1.length(); i++ ) {

            if ( !skip && p1.charAt(i) == p2.charAt(i)) continue;
            else {
                skip = true;
                converted.append(p1.charAt(i));
            }
        }

        return converted.toString();
    }

    protected String _pu( String text ) {

        if (text == null || text.isEmpty()) {
            return text;
        }

        StringBuilder converted = new StringBuilder();

        boolean convertNext = false;
        for (char ch : text.toCharArray()) {
            if (ch == '-') {
                convertNext = true;
            } else if (convertNext) {
                ch = Character.toTitleCase(ch);
                convertNext = false;
                converted.append(ch);
            } else {
                ch = ch; //Character.toLowerCase(ch);
                converted.append(ch);
            }
        }

        return converted.toString();
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
    
    protected boolean showIfAbstract(MetaObject superObject) {
        return !isAbstract(superObject)
                || (isAbstract(superObject)
                && showAbstracts);
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
        c.pw.print( c.indent + "class "+ _pu(mo.getShortName()));
        if ( isAbstract( mo ) ) c.pw.print( " << (A,#FF7700) Abstract");
        else c.pw.print( " << (O,#AAAAFF)");
        c.pw.println( " >> {");
    }

    protected void drawObjectEnd(Context c) {
        c.pw.println(c.indent+"}");
    }

    protected void drawObjectAttrHeader(Context c, MetaObject mo) {
        c.pw.println(c.indent+"  .. Attributes ..");
        c.pw.println(c.indent+"  type="+mo.getSubTypeName() );
    }

    protected void drawObjectAttr(Context c, MetaAttribute a) {
        c.pw.println(c.indent+ _pu(a.getShortName()) +"="+getAttrValue(a));
    }

    protected void drawObjectFieldSection(Context c, String s) {
        c.pw.println(c.indent + " .. " + s + " .. ");
    }

    protected void drawObjectField(Context c, MetaField f) {
        c.pw.print(c.indent+"+ " + _pu(f.getShortName()) + " ");
        c.pw.println("{"+f.getSubTypeName() + "}");
    }

    protected void drawObjectFieldWithSuper(Context c, MetaField f) {
        c.pw.print(c.indent+"+ " + _pu(f.getShortName()) + " ");
        c.pw.println("{"+f.getSubTypeName() + "}");
        c.pw.println(c.indent+"super="
                + _pu( _slimPkg( f.getSuperField().getPackage(), f.getPackage() )
                + f.getSuperField().getShortName()));
    }

    protected void drawObjectFieldWithRef(Context c, MetaField f, MetaData oref) {
        c.pw.println(c.indent+"+ " + _pu(f.getShortName()) + " ");
        c.pw.print(c.indent+"--> ");
        if ( f.getDataType().isArray() ) c.pw.print("[] " );
        c.pw.println( oref.getShortName() );
    }

    protected void drawFieldAttr(Context c, MetaField f, MetaAttribute a) {
        c.pw.print(c.indent+" [attr:"+a.getSubTypeName() + "] ");
        c.pw.println( _pu(a.getShortName()) +"="+getAttrValue(a));
    }

    protected void drawObjectSuperReference(Context c, MetaObject mo) {
        c.pw.println( _pu(mo.getName()) +" ..|> " 
                + _pu( mo.getSuperObject().getName()) +" : extends" );
    }

    protected void drawObjectKeyReference(Context c, MetaField f, ObjectReference oref ) {
        c.pw.print(c.indent+_pu(f.getParent().getName()));
        MetaObject objRef = oref.getReferencedObject();
        if (oref instanceof OneToOneReference) c.pw.print("\"1\" --> \"1\"");
        else if (oref instanceof OneToManyReference) c.pw.print("\"1\" --> \"many\"");
        else if (oref instanceof ManyToOneReference) c.pw.print("\"many\" --> \"1\"");
        else if (oref instanceof ManyToManyReference) c.pw.print("\"many\" --> \"many\"");
        else c.pw.print(" --> ");
        c.pw.print(_pu(objRef.getName()) +" : ");
        if (oref.getName() == null) c.pw.println(_pu(f.getShortName()));
        else c.pw.println(_pu(f.getShortName()) + ":" + _pu(oref.getName()));
    }

    protected void drawObjectReference(Context c, MetaField f, MetaObject objRef) {
        c.pw.println(c.indent + _pu(f.getParent().getName()) 
                +" --> "+ _pu(objRef.getName()) +" : "+ _pu(f.getShortName()));
    }

    protected void drawNewLine(Context c) {
        c.pw.println();
    }
}
