package com.draagon.meta.generator.direct;

import com.draagon.meta.DataTypes;
import com.draagon.meta.MetaData;
import com.draagon.meta.attr.MetaAttribute;
import com.draagon.meta.field.MetaField;
import com.draagon.meta.generator.GeneratorMetaException;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.relation.key.ObjectKey;
import com.draagon.meta.relation.ref.ObjectReference;
import com.draagon.meta.util.MetaDataUtil;
import com.draagon.meta.validator.MetaValidator;
import com.draagon.meta.view.MetaView;
import com.google.gson.stream.JsonWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class UIJsonModelGenerator extends JsonModelGenerator {

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

            Collection<MetaObject> filtered = getFilteredMetaObjects(loader);
            List<String> packages = getUniquePackages( filtered );

            for ( String p : packages ) {

                jsonWriter.beginObject();
                jsonWriter.name( "package" ).value( p );
                writeMetaDataGroupedByType(jsonWriter, getMetaDataForPackage( p, filtered ));
                jsonWriter.endObject();
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

    protected List<MetaData> getMetaDataForPackage( String pkg, Collection<MetaObject> mds) {
        List<MetaData> out = new ArrayList<>();
        for (MetaData md : mds ) {
            if ( md.getPackage().equals( pkg )) out.add( md );
        }
        return out;
    }

    protected void writeMetaDataForType( JsonWriter jsonWriter, Class clazz, String names, List<MetaData> mds) throws IOException {

        List<MetaData> out = new ArrayList<>();
        for (MetaData md : mds ) {
            if ( clazz.isAssignableFrom( md.getClass() )) out.add( md );
        }

        if ( out.size() > 0 ) {
            jsonWriter.name(names).beginArray();
            for ( MetaData md : out ) {
                writeMetaData(jsonWriter, md);
            }
            jsonWriter.endArray();
        }
    }

    protected void writeMetaDataGroupedByType( JsonWriter jsonWriter, List<MetaData> mds ) throws IOException {

        writeMetaDataForType( jsonWriter, MetaObject.class, "objects", mds );
        writeMetaDataForType( jsonWriter, ObjectKey.class, "keys", mds );
        writeMetaDataForType( jsonWriter, MetaField.class, "fields", mds );
        writeMetaDataForType( jsonWriter, ObjectReference.class, "references", mds );
        writeMetaDataForType( jsonWriter, MetaView.class, "views", mds );
        writeMetaDataForType( jsonWriter, MetaValidator.class, "validators", mds );
    }

    @Override
    protected void writeMetaData(JsonWriter jsonWriter, MetaData md ) throws IOException {

        jsonWriter.beginObject();
        jsonWriter.name("name").value(md.getShortName());
        jsonWriter.name("type").value(md.getSubTypeName());
        if ( md.getSuperData() != null ) jsonWriter.name("super").value( md.getSuperData().getName() );

        List<MetaAttribute> attrs = md.getMetaAttrs();
        if ( !attrs.isEmpty() ) {
            for ( MetaAttribute attr : attrs ) {
                if (!writeAttrNameValue(jsonWriter, attr.getName(), attr)) {
                    // TODO:  Log an error or throw a warning here?
                    jsonWriter.name(attr.getName()).value(attr.getValueAsString());
                }
            }
        }

        if ( !md.getChildren().isEmpty() ) {
            writeMetaDataGroupedByType(jsonWriter, md.getChildren());
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
        // TODO:  This should be handled differently
        else if ( attr.getName().equals(  ObjectReference.ATTR_REFERENCE)
                || attr.getName().equals( "objectRef" )) {
            jsonWriter.name(name).value( MetaDataUtil.expandPackageForPath(MetaDataUtil.findPackageForMetaData( attr ), attr.getValueAsString() ));
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
