package com.draagon.meta.generator.direct.plantuml;
import com.draagon.meta.generator.direct.FileDirectWriter;
import com.draagon.meta.generator.direct.MetaDataFilters;
import com.draagon.meta.generator.direct.SingleFileDirectGeneratorBase;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.object.MetaObject;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;

import static com.draagon.meta.generator.util.GeneratorUtil.getFilteredMetaData;

public class PlantUMLGenerator extends SingleFileDirectGeneratorBase {

    private boolean showAttrs = false;
    private boolean showAbstracts = true;

    //////////////////////////////////////////////////////////////////////
    // SingleFileDirectorGenerator Execute Override methods

    @Override
    protected void parseArgs() {

        if ( hasArg( "showAttrs")) showAttrs = Boolean.parseBoolean( getArg( "showAttrs"));
        if ( hasArg( "showAbstracts")) showAbstracts = Boolean.parseBoolean( getArg( "showAbstracts"));

        if ( log.isDebugEnabled() ) log.debug(toString());
    }

    @Override
    protected FileDirectWriter getWriter(MetaDataLoader loader) {
        return new PlantUMLWriter(loader, MetaDataFilters.create(getFilters()), showAttrs, showAbstracts);
    }


    //////////////////////////////////////////////////////////////////////
    // Misc methods

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer(this.getClass().getSimpleName()+"{");
        sb.append("args=").append(getArgs());
        sb.append(", filters=").append(getFilters());
        //sb.append(", scripts=").append(getScripts());
        sb.append(", showAttrs=").append(showAttrs);
        sb.append(", showAbstracts=").append(showAbstracts);
        sb.append('}');
        return sb.toString();
    }
}
