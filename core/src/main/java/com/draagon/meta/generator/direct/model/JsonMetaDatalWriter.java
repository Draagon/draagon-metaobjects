package com.draagon.meta.generator.direct.model;

import com.draagon.meta.DataTypes;
import com.draagon.meta.MetaData;
import com.draagon.meta.attr.MetaAttribute;
import com.draagon.meta.generator.GeneratorIOException;
import com.draagon.meta.generator.direct.json.JsonDirectWriter;
import com.draagon.meta.generator.util.GeneratorUtil;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.util.MetaDataUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Writer;
import java.util.*;

public class JsonMetaDatalWriter extends JsonDirectWriter<JsonMetaDatalWriter> {

    private static final Logger log = LoggerFactory.getLogger(JsonMetaDatalWriter.class);

    public JsonMetaDatalWriter(MetaDataLoader loader, Writer writer ) throws GeneratorIOException {
        super(loader, writer);
    }
    
    @Override
    public void writeJson() throws GeneratorIOException {

        try {
            out().beginObject().name("metadata").beginObject().name("children").beginArray();

            for (MetaData md : GeneratorUtil.getFilteredMetaData(getLoader(), getFilters())) {
                writeMetaData(md);
            }

            out().endArray().endObject().endObject();
        }
        catch (IOException e ) {
            throw new GeneratorIOException( this, "Error writing Json: "+e, e );
        }
    }

    protected void writeMetaData( MetaData md ) throws IOException {

        out().beginObject().name(md.getTypeName()).beginObject();
        out().name("type").value(md.getSubTypeName());
        if ( !md.getPackage().isEmpty() ) out().name("package").value(md.getPackage());
        out().name("name").value(md.getShortName());
        if ( md.getSuperData() != null ) out().name("super").value( md.getSuperData().getName() );

        if ( md instanceof MetaAttribute ) {
            MetaAttribute attr = (MetaAttribute) md;
            if ( !writeAttrNameValue( "value", attr )) {
                log.warn("Unable to serialize attribute value of type {} for attribute {}, falling back to string representation", 
                         attr.getValue() != null ? attr.getValue().getClass().getName() : "null", attr.getName());
                out().name("value").value( attr.getValueAsString() );
            }
        }
        else {
            List<MetaData> children = writeAndFilterAttributes( md.getChildren());

            if (!children.isEmpty()) {
                out().name("children").beginArray();

                for (MetaData mdc : children) {
                    writeMetaData(mdc);
                }

                out().endArray();
            }
        }

        out().endObject().endObject();
    }

    protected List<MetaData> writeAndFilterAttributes(List<MetaData> mdChildren) throws IOException {

        List<MetaData> children = new ArrayList<MetaData>();

        for ( MetaData mdc :  mdChildren ) {
            if (!( mdc instanceof MetaAttribute )
                    || !writeAttrNameValue( mdc.getShortName(), (MetaAttribute) mdc )) {
                children.add( mdc );
            }
        }

        return children;
    }

    protected boolean writeAttrNameValue( String name, MetaAttribute attr ) throws IOException {

        Object val = attr.getValue();
        if ( val == null ) {
            out().name(name).nullValue();
        }
        else if ( attr.getName().equals(MetaObject.ATTR_OBJECT_REF )) {
            out().name(name).value( MetaDataUtil.expandPackageForPath(MetaDataUtil.findPackageForMetaData( attr ), attr.getValueAsString() ));
        }
        else if ( attr.getDataType() == DataTypes.STRING_ARRAY ) {
            out().name(name).value( val.toString() );
        }
        else if ( val instanceof String) {
            out().name(name).value( (String) val );
        }
        else if ( val instanceof Boolean ) {
            out().name(name).value( (Boolean) val );
        }
        else if ( val instanceof Long ) {
            out().name(name).value( (Long) val );
        }
        else if ( val instanceof Date) {
            out().name(name).value( ((Date) val).getTime() );
        }
        else if ( val instanceof Number ) {
            out().name(name).value( (Number) val );
        }
        else if ( val instanceof Double ) {
            out().name(name).value( (Double) val );
        }
        else if ( val instanceof Properties) {
            StringBuilder b = new StringBuilder();
            for (Map.Entry<Object,Object> e : (((Properties) val).entrySet()) ) {
                if ( b.length() > 0 ) b.append( ',' );
                b.append( e.getKey() ).append(':').append( e.getValue() );
            }
            out().name(name).value( b.toString() );
        }
        else {
            return false;
        }

        return true;
    }
}
