package com.draagon.meta.generator.direct.json.model;

import com.draagon.meta.DataTypes;
import com.draagon.meta.MetaData;
import com.draagon.meta.attr.MetaAttribute;
import com.draagon.meta.generator.WriterException;
import com.draagon.meta.generator.direct.DirectWriter;
import com.draagon.meta.generator.direct.MetaDataFilters;
import com.draagon.meta.generator.direct.json.JsonDirectWriter;
import com.draagon.meta.generator.direct.xml.XMLDirectWriter;
import com.draagon.meta.generator.util.GeneratorUtil;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.relation.ref.ObjectReference;
import com.draagon.meta.util.MetaDataUtil;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.*;

public class JsonModelWriter extends JsonDirectWriter<String> {

    public JsonModelWriter(MetaDataLoader loader) {
        super( loader, null );
    }

    public JsonModelWriter(MetaDataLoader loader, MetaDataFilters filters) {
        super( loader, filters );
    }

    @Override
    public void writeJson( Context c, String filename ) {

        try {
            c.out.beginObject();
            c.out.name("metadata").beginArray();

            for (MetaData md : GeneratorUtil.getFilteredMetaData(loader, filters)) {
                writeMetaData(c.inc(md.getShortName()), md);
            }

            c.out.endArray();
            c.out.endObject();
        }
        catch (IOException e ) {
            throw new WriterException( "Error writing Json file ["+filename+"]: "+e, c, e );
        }
    }

    protected void writeMetaData(Context c, MetaData md ) throws IOException {

        c.out.beginObject();
        c.out.name("type").value(md.getTypeName());
        c.out.name("subType").value(md.getSubTypeName());
        if ( !md.getPackage().isEmpty() ) c.out.name("package").value(md.getPackage());
        c.out.name("name").value(md.getShortName());
        if ( md.getSuperData() != null ) c.out.name("super").value( md.getSuperData().getName() );

        if ( md instanceof MetaAttribute ) {
            MetaAttribute attr = (MetaAttribute) md;
            if ( !writeAttrNameValue( c, "value", attr )) {
                // TODO:  Log an error or throw a warning here?
                c.out.name("value").value( attr.getValueAsString() );
            }
        }
        else {
            List<MetaData> children = writeAndFilterAttributes(c, md.getChildren());

            if (!children.isEmpty()) {
                c.out.name("children").beginArray();

                for (MetaData mdc : children) {
                    writeMetaData(c.inc(mdc.getShortName()), mdc);
                }

                c.out.endArray();
            }
        }

        c.out.endObject();
    }

    protected List<MetaData> writeAndFilterAttributes(Context c, List<MetaData> mdChildren ) throws IOException {

        List<MetaData> children = new ArrayList<MetaData>();

        for ( MetaData mdc :  mdChildren ) {

            if (!( mdc instanceof MetaAttribute )
                    || !writeAttrNameValue( c, mdc.getShortName(), (MetaAttribute) mdc )) {
                children.add( mdc );
            }
        }

        return children;
    }

    protected boolean writeAttrNameValue( Context c, String name, MetaAttribute attr ) throws IOException {

        Object val = attr.getValue();
        if ( val == null ) {
            c.out.name(name).nullValue();
        }
        // TODO:  This should be handled differently
        else if ( attr.getName().equals(  ObjectReference.ATTR_REFERENCE)
                || attr.getName().equals( "objectRef" )) {
            c.out.name(name).value( MetaDataUtil.expandPackageForPath(MetaDataUtil.findPackageForMetaData( attr ), attr.getValueAsString() ));
        }
        else if ( attr.getDataType() == DataTypes.STRING_ARRAY ) {
            c.out.name(name).value( val.toString() );
        }
        else if ( val instanceof String) {
            c.out.name(name).value( (String) val );
        }
        else if ( val instanceof Boolean ) {
            c.out.name(name).value( (Boolean) val );
        }
        else if ( val instanceof Long ) {
            c.out.name(name).value( (Long) val );
        }
        else if ( val instanceof Date) {
            c.out.name(name).value( ((Date) val).getTime() );
        }
        else if ( val instanceof Number ) {
            c.out.name(name).value( (Number) val );
        }
        else if ( val instanceof Double ) {
            c.out.name(name).value( (Double) val );
        }
        else if ( val instanceof Properties) {
            StringBuilder b = new StringBuilder();
            for (Map.Entry<Object,Object> e : (((Properties) val).entrySet()) ) {
                if ( b.length() > 0 ) b.append( ',' );
                b.append( e.getKey() ).append(':').append( e.getValue() );
            }
            c.out.name(name).value( b.toString() );
        }
        else {
            return false;
        }

        return true;
    }
}
