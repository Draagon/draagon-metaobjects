package com.draagon.meta.generator;

import com.draagon.meta.MetaData;
import com.draagon.meta.generator.direct.MetaDataFilters;
import com.draagon.meta.generator.direct.xml.XMLDirectWriter;
import com.draagon.meta.generator.util.GeneratorUtil;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.object.MetaObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Collection;
import java.util.List;

public abstract class WriterBase {

    protected Log log = LogFactory.getLog( this.getClass() );

    protected final MetaDataLoader loader;
    protected final MetaDataFilters filters;

    protected WriterBase(MetaDataLoader loader, MetaDataFilters filters ) {
        this.loader = loader;
        this.filters = filters;
    }
}
