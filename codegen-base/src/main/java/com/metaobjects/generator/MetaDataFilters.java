package com.metaobjects.generator;

import java.util.ArrayList;
import java.util.List;

public class MetaDataFilters {

    protected List<String> filters = new ArrayList<>();

    public MetaDataFilters() {
    }

    public MetaDataFilters addFilters( List<String> filters ) {
        if ( filters != null ) this.filters.addAll( filters );
        return this;
    }

    public MetaDataFilters addFilter( String filter ) {
        filters.add( filter );
        return this;
    }

    public static MetaDataFilters create( List<String> filters ) {
        return new MetaDataFilters().addFilters( filters );
    }

    public static MetaDataFilters create( String filter ) {
        return new MetaDataFilters().addFilter( filter );
    }

    public List<String> getFilters() {
        return filters;
    }
}
