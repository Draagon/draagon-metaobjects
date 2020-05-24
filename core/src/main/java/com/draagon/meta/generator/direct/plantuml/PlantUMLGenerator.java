package com.draagon.meta.generator.direct.plantuml;
import com.draagon.meta.generator.GeneratorIOException;
import com.draagon.meta.generator.direct.SingleFileDirectGeneratorBase;
import com.draagon.meta.loader.MetaDataLoader;

import java.io.PrintWriter;

import static com.draagon.meta.generator.util.GeneratorUtil.getFilteredMetaData;

public class PlantUMLGenerator extends SingleFileDirectGeneratorBase<PlantUMLWriter> {

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
    protected PlantUMLWriter getWriter(MetaDataLoader loader, PrintWriter pw ) {
        return new PlantUMLWriter(loader, pw)
                .showAttrs(showAttrs)
                .showAbstracts(showAbstracts);
    }

    @Override
    protected void writeFile(PlantUMLWriter writer) throws GeneratorIOException {
        writer.writeUML();
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
