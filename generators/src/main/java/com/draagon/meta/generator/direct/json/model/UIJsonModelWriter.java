package com.draagon.meta.generator.direct.json.model;

import com.draagon.meta.DataTypes;
import com.draagon.meta.MetaData;
import com.draagon.meta.attr.MetaAttribute;
import com.draagon.meta.field.MetaField;
import com.draagon.meta.generator.WriterException;
import com.draagon.meta.generator.direct.MetaDataFilters;
import com.draagon.meta.generator.direct.json.JsonDirectWriter;
import com.draagon.meta.generator.util.GeneratorUtil;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.relation.key.ObjectKey;
import com.draagon.meta.relation.ref.ObjectReference;
import com.draagon.meta.util.MetaDataUtil;
import com.draagon.meta.validator.MetaValidator;
import com.draagon.meta.view.MetaView;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.*;

import static com.draagon.meta.generator.util.GeneratorUtil.getUniquePackages;

public class UIJsonModelWriter extends JsonModelWriter {

    public UIJsonModelWriter(MetaDataLoader loader) {
        super( loader, null );
    }

    public UIJsonModelWriter(MetaDataLoader loader, MetaDataFilters filters) {
        super( loader, filters );
    }

    @Override
    public void writeJson( Context c, String filename ) {

        try {
            c.out.beginObject();
            c.out.name( "metadata" ).beginArray();

            Collection<MetaObject> filtered = GeneratorUtil.getFilteredMetaData(loader,MetaObject.class,filters);
            List<String> packages = getUniquePackages( filtered );

            for ( String p : packages ) {

                c.out.beginObject();
                c.out.name( "package" ).value( p );
                writeMetaDataGroupedByType(c, getMetaDataForPackage( p, filtered ));
                c.out.endObject();
            }

            c.out.endArray();
            c.out.endObject();
        }
        catch (IOException e ) {
            throw new WriterException( "Error writing Json file ["+filename+"]: "+e, c, e );
        }
    }

    protected List<MetaData> getMetaDataForPackage( String pkg, Collection<MetaObject> mds) {
        List<MetaData> out = new ArrayList<>();
        for (MetaData md : mds ) {
            if ( md.getPackage().equals( pkg )) out.add( md );
        }
        return out;
    }

    protected void writeMetaDataForType(Context c, Class clazz, String names, List<MetaData> mds) throws IOException {

        List<MetaData> out = new ArrayList<>();
        for (MetaData md : mds ) {
            if ( clazz.isAssignableFrom( md.getClass() )) out.add( md );
        }

        if ( out.size() > 0 ) {
            c.out.name(names).beginArray();
            for ( MetaData md : out ) {
                writeMetaData(c, md);
            }
            c.out.endArray();
        }
    }

    protected void writeMetaDataGroupedByType( Context c, List<MetaData> mds ) throws IOException {

        writeMetaDataForType( c, MetaObject.class, "objects", mds );
        writeMetaDataForType( c, ObjectKey.class, "keys", mds );
        writeMetaDataForType( c, MetaField.class, "fields", mds );
        writeMetaDataForType( c, ObjectReference.class, "references", mds );
        writeMetaDataForType( c, MetaView.class, "views", mds );
        writeMetaDataForType( c, MetaValidator.class, "validators", mds );
    }
}
