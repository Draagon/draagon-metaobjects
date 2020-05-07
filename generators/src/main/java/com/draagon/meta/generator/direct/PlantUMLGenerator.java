package com.draagon.meta.generator.direct;

import com.draagon.meta.DataTypes;
import com.draagon.meta.MetaData;
import com.draagon.meta.attr.MetaAttribute;
import com.draagon.meta.attr.MetaAttributeNotFoundException;
import com.draagon.meta.field.MetaField;
import com.draagon.meta.generator.GeneratorMetaException;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.relation.ref.*;
import com.draagon.meta.util.MetaDataUtil;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class PlantUMLGenerator extends DirectGeneratorBase<PlantUMLGenerator> {

    private boolean showAttrs = false;
    private boolean showAbstracts = true;

    @Override
    public void execute(MetaDataLoader loader) {

        File outf = null;
        PrintWriter pw = null;

        if ( hasArg( "showAttrs"))
            showAttrs = Boolean.parseBoolean( getArg( "showAttrs"));
        if ( hasArg( "showAbstracts"))
            showAbstracts = Boolean.parseBoolean( getArg( "showAbstracts"));

        System.out.println( "PlantUML: showAttrs="+showAttrs);
        System.out.println( "PlantUML: showAbstracts="+showAbstracts);

        try {
            File f = getOutputDir();

            // TODO: Use an argument here for the filename
            outf = new File(f, getOutputFilename());
            outf.createNewFile();

            pw = new PrintWriter(outf);

            pw.println("@startuml");
            pw.println();
            pw.println("skinparam class {");
            pw.println("    BackgroundColor PaleGreen");
            pw.println("    ArrowColor SeaGreen");
            pw.println("    BorderColor DarkGreen");
            pw.println("    BackgroundColor<<Abstract>> Wheat");
            pw.println("    BorderColor<<Abstract>> Tomato");
            pw.println("}");
            pw.println("skinparam stereotypeCBackgroundColor YellowGreen");
            pw.println("skinparam stereotypeCBackgroundColor<< Abstract >> DimGray");
            pw.println();
            pw.println("set namespaceSeparator ::");

            List<MetaData> filtered = getFilteredMetaData(loader);
            for ( String p : getUniquePackages( filtered )) {

                if (!p.isEmpty()) pw.println("namespace " + p + " {" );

                // Write MetaObjects
                for (MetaData md : filtered) {
                    if ( md instanceof MetaObject && md.getPackage().equals( p )
                            && ( !isAbstract( md ) || (isAbstract(md) && showAbstracts ))) {
                        MetaObject mo = (MetaObject) md;

                        writerMetaData(pw, "  ", mo);
                    }
                }

                if (!p.isEmpty()) pw.println("}" );

                // Write Object References
                for (MetaData md : filtered) {
                    if ( md instanceof MetaObject && md.getPackage().equals( p )) {
                        writeObjectRelationships(pw, "", (MetaObject) md);
                    }
                }
            }


            pw.println("@enduml");
        }
        catch( IOException e ) {
            throw new GeneratorMetaException( "Unable to write PlantUML to file [" + outf + "]: " + e, e );
        }
        finally {
            if ( pw != null ) pw.close();
        }
    }

    protected void writerMetaData( PrintWriter pw, String indent, MetaData md ) throws IOException {

        boolean isAbstract = isAbstract( md );

        pw.print( indent + "class "+ _pu(md.getShortName()));
        if ( isAbstract ) pw.print( " << (A,#FF7700) Abstract");
        else pw.print( " << (O,#AAAAFF)");
        pw.println( " >> {");

        // Write Attributes
        if ( showAttrs ) {

            pw.println(indent+"  .. Attributes ..");
            pw.println(indent+"  type="+md.getSubTypeName() );

            List<MetaAttribute> attrs = (List<MetaAttribute>) md.getChildren(MetaAttribute.class, true );
            if (!attrs.isEmpty()) {
                writeAttributes(pw, indent + " ", null, attrs);
            }
        }

        // Write Fields
        List<MetaField> fields = (List<MetaField>) md.getChildren(MetaField.class, true );
        if ( !fields.isEmpty() ) {
            pw.println(indent + " .. Fields ..");

            fields = (List<MetaField>) md.getChildren(MetaField.class, false);
            writeFields(pw, indent + "  ", fields);

            if (md.getSuperData() != null && isAbstract(md.getSuperData()) && !showAbstracts) {
                pw.println(indent + " .. "+md.getSuperData().getShortName()+" Fields ..");
                fields = (List<MetaField>) md.getSuperData().getChildren(MetaField.class, true);
                writeFields(pw, indent + "  ", fields);
            }
            else if ( md.getSuperData() != null && fields.isEmpty() ) {
                pw.println(indent+" .. "+md.getSuperData().getShortName()+" ..");
            }
        }
        else {
            pw.println(indent+" .. No Fields ..");
        }

        pw.println(indent+"}");
    }

    protected void writeFields(PrintWriter pw, String indent, List<MetaField> fields) {

        for (MetaField f : fields) {

            if ( !isAbstract(f) && (!showAbstracts && isAbstract( f ))) continue;

            pw.print(indent+"  + " + _pu(f.getShortName()) + " ");

            MetaData oref = null;
            try { oref = MetaDataUtil.getObjectRef( f ); } catch( Exception e ) {}
            if ( oref != null ) {
                pw.println();
                pw.print( "    --> ");
                if ( f.getDataType().isArray() ) pw.print("[] " );
                pw.print( oref.getShortName() );
                //pw.print("}");
            }
            else if ( f.getSuperData() != null ) {
                pw.println("{"+f.getSubTypeName() + "}");
                pw.print( "    super=" + _pu( _slimPkg( f.getSuperField().getPackage(), f.getPackage() )
                        + f.getSuperField().getShortName()));
            }
            else {
                pw.print("{"+f.getSubTypeName() + "}");
            }

            pw.println();

            if ( showAttrs ) {
                List<MetaAttribute> attrs = (List<MetaAttribute>) f.getChildren(MetaAttribute.class, !showAbstracts);
                if (!attrs.isEmpty()) {
                    writeAttributes(pw, indent + "   ", f, attrs);
                }
            }
        }
    }


    protected void writeObjectRelationships( PrintWriter pw, String indent, MetaObject mo ) throws IOException {

        if ( mo.getSuperObject() != null
                && ( !isAbstract( mo.getSuperObject() )
                    || (isAbstract( mo.getSuperObject() ) && showAbstracts))) {
            pw.println( _pu(mo.getName()) + " ..|> " + _pu( mo.getSuperObject().getName()) + " : extends" );
        }

        // Write Fields
        List<MetaField> fields = (List<MetaField>) mo.getChildren(MetaField.class, false);
        if ( !fields.isEmpty() ) {

            pw.println();

            for (MetaField f : fields) {

                ObjectReference oref = (ObjectReference) f.getFirstChildOfType(ObjectReference.TYPE_OBJECTREF);
                if ( oref != null ) {
                    MetaObject objRef = oref.getReferencedObject();

                    if ( !isAbstract( oref ) || ( isAbstract(oref) && showAbstracts)) {

                        if (objRef != null) {
                            pw.print(_pu(f.getParent().getName()));
                            if (oref instanceof OneToOneReference) pw.print("\"1\" --> \"1\"");
                            else if (oref instanceof OneToManyReference) pw.print("\"1\" --> \"many\"");
                            else if (oref instanceof ManyToOneReference) pw.print("\"many\" --> \"1\"");
                            else if (oref instanceof ManyToManyReference) pw.print("\"many\" --> \"many\"");
                            else pw.print(" --> ");
                            pw.print(_pu(objRef.getName()) + " : ");
                            if (oref.getName() == null) pw.println(_pu(f.getShortName()));
                            else pw.println(_pu(f.getShortName()) + ":" + _pu(oref.getName()));
                        }
                    }
                }
                else {
                    try {
                        MetaObject objRef = MetaDataUtil.getObjectRef(f);
                        if ( !isAbstract( objRef ) || (isAbstract( objRef ) && showAbstracts )) {
                            pw.println(_pu(f.getParent().getName()) + " --> " + _pu(objRef.getName()) + " : " + _pu(f.getShortName()));
                        }
                    } catch (MetaAttributeNotFoundException e) {}
                }
            }
        }
    }

    protected void writeAttributes( PrintWriter pw, String indent, MetaField f, List<MetaAttribute> attrs ) {

        String pre = "";
        for (MetaAttribute a : attrs) {
            //pw.println(indent+"{"+a.getSubTypeName() + "} " + a.getShortName() +" = "+a.getValueAsString());
            if ( f != null ) pre = " [attr:"+a.getSubTypeName() + "] ";
            pw.println(indent+pre+ _pu(a.getShortName()) +"="+getAttrValue(a));
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
}
