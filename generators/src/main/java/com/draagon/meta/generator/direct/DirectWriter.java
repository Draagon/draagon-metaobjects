package com.draagon.meta.generator.direct;

import com.draagon.meta.MetaData;
import com.draagon.meta.generator.WriterBase;
import com.draagon.meta.loader.MetaDataLoader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public abstract class DirectWriter<OUT, D> extends WriterBase {

    protected Log log = LogFactory.getLog( this.getClass() );

    protected DirectWriter(MetaDataLoader loader, MetaDataFilters filters ) {
        super( loader, filters );
    }

    protected abstract void write( OUT out, D data );
}
