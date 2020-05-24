package com.draagon.meta.generator.direct.model;

import com.draagon.meta.MetaData;
import com.draagon.meta.field.MetaField;
import com.draagon.meta.generator.GeneratorIOException;
import com.draagon.meta.generator.util.GeneratorUtil;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.relation.key.ObjectKey;
import com.draagon.meta.relation.ref.ObjectReference;
import com.draagon.meta.validator.MetaValidator;
import com.draagon.meta.view.MetaView;

import java.io.IOException;
import java.io.Writer;
import java.util.*;

import static com.draagon.meta.generator.util.GeneratorUtil.getUniquePackages;

public class UIJsonModelWriter extends JsonModelWriter<UIJsonModelWriter> {

    public UIJsonModelWriter(MetaDataLoader loader, Writer writer) throws GeneratorIOException {
        super(loader, writer);
    }

    @Override
    public void writeJson() throws GeneratorIOException {

        try {
            out().beginObject();
            out().name( "metadata" ).beginArray();

            Collection<MetaObject> filtered = GeneratorUtil.getFilteredMetaData(getLoader(),MetaObject.class,getFilters());
            List<String> packages = getUniquePackages( filtered );

            for ( String p : packages ) {

                out().beginObject();
                out().name( "package" ).value( p );
                writeMetaDataGroupedByType( getMetaDataForPackage( p, filtered ));
                out().endObject();
            }

            out().endArray();
            out().endObject();
        }
        catch (IOException e ) {
            throw new GeneratorIOException( this, "Error writing Json: "+e, e );
        }
    }

    protected List<MetaData> getMetaDataForPackage( String pkg, Collection<MetaObject> mds) {
        List<MetaData> out = new ArrayList<>();
        for (MetaData md : mds ) {
            if ( md.getPackage().equals( pkg )) out.add( md );
        }
        return out;
    }

    protected void writeMetaDataForType( Class clazz, String names, List<MetaData> mds) throws IOException {

        List<MetaData> out = new ArrayList<>();
        for (MetaData md : mds ) {
            if ( clazz.isAssignableFrom( md.getClass() )) out.add( md );
        }

        if ( out.size() > 0 ) {
            out().name(names).beginArray();
            for ( MetaData md : out ) {
                writeMetaData(md);
            }
            out().endArray();
        }
    }

    protected void writeMetaDataGroupedByType( List<MetaData> mds ) throws IOException {

        writeMetaDataForType( MetaObject.class, "objects", mds );
        writeMetaDataForType( ObjectKey.class, "keys", mds );
        writeMetaDataForType( MetaField.class, "fields", mds );
        writeMetaDataForType( ObjectReference.class, "references", mds );
        writeMetaDataForType( MetaView.class, "views", mds );
        writeMetaDataForType( MetaValidator.class, "validators", mds );
    }
}
