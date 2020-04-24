package com.draagon.meta.generator.direct;

import com.draagon.meta.DataTypes;
import com.draagon.meta.MetaData;
import com.draagon.meta.MetaException;
import com.draagon.meta.attr.MetaAttribute;
import com.draagon.meta.generator.Generator;
import com.draagon.meta.generator.GeneratorMetaException;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.util.DataConverter;
import com.google.gson.stream.JsonWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class JsonModelGenerator extends DirectGeneratorBase<JsonModelGenerator> {

    @Override
    public void execute(MetaDataLoader loader) {

        File outf = null;
        FileWriter fileWriter = null;
        //PrintWriter out = null;
        JsonWriter jsonWriter = null;

        try {
            File f = getOutputDir();

            // TODO: Use an argument here for the filename
            outf = new File(f, getOutputFilename());
            outf.createNewFile();

            fileWriter = new FileWriter(outf);
            jsonWriter = new JsonWriter( fileWriter);

            jsonWriter.setIndent("  ");
            jsonWriter.beginObject();
            jsonWriter.name( "metadata" ).beginArray();

            for (MetaData md : getFilteredMetaData(loader)) {
                writerMetaData( jsonWriter, md );
            }

            jsonWriter.endArray();
            jsonWriter.endObject();
        }
        catch( IOException e ) {
            throw new GeneratorMetaException( "Unable to write Json Model to file [" + outf + "]: " + e, e );
        }
        finally {
            if ( fileWriter != null ) try { fileWriter.close(); } catch( IOException e ) {}
        }
    }

    protected void writerMetaData( JsonWriter jsonWriter, MetaData md ) throws IOException {

        jsonWriter.beginObject();
        jsonWriter.name("type").value(md.getTypeName());
        jsonWriter.name("subType").value(md.getSubTypeName());
        if ( !md.getPackage().isEmpty() ) jsonWriter.name("package").value(md.getPackage());
        jsonWriter.name("name").value(md.getShortName());
        if ( md.getSuperData() != null ) jsonWriter.name("super").value( md.getSuperData().getName() );

        if ( md instanceof MetaAttribute ) {
            MetaAttribute attr = (MetaAttribute) md;
            if ( !writeAttrNameValue( jsonWriter, "value", attr )) {
                // TODO:  Log an error or throw a warning here?
                jsonWriter.name("value").value( attr.getValueAsString() );
            }
        }
        else {
            List<MetaData> children = writeAndFilterAttributes(jsonWriter, md.getChildren());

            if (!children.isEmpty()) {
                jsonWriter.name("children").beginArray();

                for (MetaData mdc : children) {
                    writerMetaData(jsonWriter, mdc);
                }

                jsonWriter.endArray();
            }
        }

        jsonWriter.endObject();
    }

    protected List<MetaData> writeAndFilterAttributes(JsonWriter jsonWriter, List<MetaData> mdChildren ) throws IOException {

        List<MetaData> children = new ArrayList<MetaData>();

        for ( MetaData mdc :  mdChildren ) {

            if (!( mdc instanceof MetaAttribute )
                    || !writeAttrNameValue( jsonWriter, mdc.getShortName(), (MetaAttribute) mdc )) {
                children.add( mdc );
            }
        }

        return children;
    }

    protected boolean writeAttrNameValue( JsonWriter jsonWriter, String name, MetaAttribute attr ) throws IOException {

        Object val = attr.getValue();
        if ( val == null ) {
            jsonWriter.name(name).nullValue();
        }
        else if ( attr.getDataType() == DataTypes.STRING_ARRAY ) {
            jsonWriter.name(name).value( val.toString() );
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
    }
}
