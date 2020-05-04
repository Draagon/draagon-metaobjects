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

    @Override
    public void execute(MetaDataLoader loader) {

        File outf = null;
        PrintWriter pw = null;

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
                    if ( md instanceof MetaObject && md.getPackage().equals( p )) {
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

        boolean isAbstract = false;
        if ( md.hasAttr("_isAbstract")) {
            isAbstract = "true".equals( md.getMetaAttr( "_isAbstract").getValueAsString());
        }

        pw.print( indent + "class "+ _pu(md.getShortName()));
        if ( isAbstract ) pw.print( " << (A,#FF7700) Abstract");
        else pw.print( " << (O,#AAAAFF)");
        pw.println( " >> {");

        // Write Attributes
        if ( showAttrs ) {

            pw.println(indent+"  .. Attributes ..");
            pw.println(indent+"  type="+md.getSubTypeName() );

            List<MetaAttribute> attrs = (List<MetaAttribute>) md.getChildren(MetaAttribute.class, false);
            if (!attrs.isEmpty()) {
                writeAttributes(pw, indent + " ", null, attrs);
            }
        }

        // Write Fields
        List<MetaField> fields = (List<MetaField>) md.getChildren(MetaField.class, false);
        if ( !fields.isEmpty() ) {
            pw.println(indent+" .. Fields ..");
            for (MetaField f : fields) {

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
                    pw.print( "  extends " + _pu( f.getSuperData().getName()) );
                }
                else {
                    pw.print("{"+f.getSubTypeName() + "}");
                }

                pw.println();

                if ( showAttrs ) {
                    List<MetaAttribute> attrs = (List<MetaAttribute>) f.getChildren(MetaAttribute.class, false);
                    if (!attrs.isEmpty()) writeAttributes(pw, indent + "   ", f, attrs);
                }
            }
        }
        else {
            pw.println(indent+" .. No Fields ..");
        }

        pw.println(indent+"}");
    }


    protected void writeObjectRelationships( PrintWriter pw, String indent, MetaObject mo ) throws IOException {

        if ( mo.getSuperObject() != null ) {
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
                    if (objRef != null) {
                        pw.print(_pu(f.getParent().getName()));
                        if (oref instanceof OneToOneReference) pw.print("\"1\" --> \"1\"");
                        else if (oref instanceof OneToManyReference) pw.print("\"1\" --> \"many\"");
                        else if (oref instanceof ManyToOneReference) pw.print("\"many\" --> \"1\"");
                        else if (oref instanceof ManyToManyReference) pw.print("\"many\" --> \"many\"");
                        else pw.print(" --> ");
                        pw.print(_pu(objRef.getName()) + " : " );
                        if ( oref.getName() == null ) pw.println( _pu(f.getShortName()));
                        else pw.println( _pu(f.getShortName()) +":"+ _pu(oref.getName()) );
                    }
                }
                else {
                    try {
                        MetaObject objRef = MetaDataUtil.getObjectRef(f);
                        pw.println(_pu(f.getParent().getName()) + " --> " + _pu(objRef.getName()) + " : " + _pu(f.getShortName()) );
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
}
