package com.draagon.meta.generator.direct;

import com.draagon.meta.DataTypes;
import com.draagon.meta.MetaData;
import com.draagon.meta.attr.MetaAttribute;
import com.draagon.meta.field.MetaField;
import com.draagon.meta.generator.GeneratorMetaException;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.object.MetaObject;
import com.google.gson.stream.JsonWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class PlantUMLGenerator extends DirectGeneratorBase<PlantUMLGenerator> {

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
            pw.println("set namespaceSeparator ::");

            List<MetaData> filtered = getFilteredMetaData(loader);
            for ( String p : getNamespaces( filtered )) {

                if (!p.isEmpty()) pw.println("namespace " + p + " {" );

                for (MetaData md : filtered) {
                    if ( md.getPackage().equals( p )) {
                        writerMetaData(pw, "  ", md);
                    }
                }

                if (!p.isEmpty()) pw.println("}" );
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

    protected List<String> getNamespaces( List<MetaData> filtered ) throws IOException {
        List<String> pkgs = new ArrayList<>();

        filtered.forEach( md -> {
            if ( md instanceof MetaObject
                    && !pkgs.contains( md.getPackage() )) {
                pkgs.add( md.getPackage() );
            }
        });

        return pkgs;
    }

    protected void writerMetaData( PrintWriter pw, String indent, MetaData md ) throws IOException {

        pw.print( indent + "class "+md.getShortName()+" << (O,#FF7700) "+md.getSubTypeName()+" >>");
        if ( md.getSuperData() != null ) pw.print( " extends " + md.getSuperData().getName() );
        pw.println(" {");

        // Write Attributes
        List<MetaAttribute> attrs = (List<MetaAttribute>) md.getChildren(MetaAttribute.class, false);
        if ( !attrs.isEmpty() ) {
            pw.println(indent+"  .. Attributes ..");
            writeAttributes( pw, indent+" ", null, attrs );
        }

        // Write Fields
        List<MetaField> fields = (List<MetaField>) md.getChildren(MetaField.class, false);
        if ( !fields.isEmpty() ) {
            pw.println(indent+" .. Fields ..");
            for (MetaField f : fields) {
                pw.print(indent+"  + " + f.getShortName() + " {"+f.getSubTypeName() + "} ");
                if ( f.getSuperData() != null ) pw.print( "[" + f.getSuperData().getName() + "]");
                pw.println();

                attrs = (List<MetaAttribute>) f.getChildren(MetaAttribute.class, false);
                if ( !attrs.isEmpty() ) writeAttributes( pw, indent+"   ", f, attrs );
            }
        }

        pw.println(indent+"}");
    }

    protected void writeAttributes( PrintWriter pw, String indent, MetaField f, List<MetaAttribute> attrs ) {

        String pre = "";
        if ( f != null ) pre = " --"; //+f.getShortName() + ":";
        for (MetaAttribute a : attrs) {
            //pw.println(indent+"{"+a.getSubTypeName() + "} " + a.getShortName() +" = "+a.getValueAsString());
            pw.println(indent+pre+ a.getShortName() +"="+a.getValueAsString());
        }
    }

    /*protected String writeAttrNameValue( MetaAttribute attr ) throws IOException {

        Object val = attr.getValue();
        if ( val == null ) {
            return null;
        }
        else if ( attr.getDataType() == DataTypes.STRING_ARRAY ) {
            return val.toString() );
        }
        else if ( val instanceof String) {
            jsonWriter.name(name).value( (String) val );
        }
        else if ( val instanceof Boolean ) {
            jsonWriter.name(name).value( (Boolean) val );
        }
        else if ( val instanceof Long ) {
            jsonWriter.name(name).value( (Long) val );
        }
        else if ( val instanceof Date) {
            jsonWriter.name(name).value( ((Date) val).getTime() );
        }
        else if ( val instanceof Number ) {
            jsonWriter.name(name).value( (Number) val );
        }
        else if ( val instanceof Double ) {
            jsonWriter.name(name).value( (Double) val );
        }
        else if ( val instanceof Properties) {
            StringBuilder b = new StringBuilder();
            for (Map.Entry<Object,Object> e : (((Properties) val).entrySet()) ) {
                if ( b.length() > 0 ) b.append( ',' );
                 b.append( e.getKey() ).append(':').append( e.getValue() );
             }
            jsonWriter.name(name).value( b.toString() );
        }
        else {
            return false;
        }

        return true;
    }*/
}
