package com.draagon.meta.generator.direct.plantuml;
import com.draagon.meta.generator.GeneratorIOException;
import com.draagon.meta.generator.direct.SingleFileDirectGeneratorBase;
import com.draagon.meta.loader.MetaDataLoader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.PrintWriter;
import java.util.List;

import static com.draagon.meta.generator.util.GeneratorUtil.getFilteredMetaData;

public class PlantUMLGenerator extends SingleFileDirectGeneratorBase<PlantUMLWriter> {

    protected Log log = LogFactory.getLog( this.getClass());

    public final static String ARG_SHOW_ATTRS = "showAttrs";
    public final static String ARG_SHOW_FIELDS = "showFields";
    public final static String ARG_SHOW_ABSTRACTS = "showAbstracts";
    public final static String ARG_EMBEDDED_ATTR = "embeddedAttr";
    public final static String ARG_EMBEDDED_ATTR_VALUES = "embeddedAttrValues";
    public final static String ARG_DEBUG = "debug";

    private boolean showAttrs = false;
    private boolean showFields = false;
    private boolean showAbstracts = true;
    private String embeddedAttr = null;
    private String embeddedValues = null;
    private boolean debug = false;

    //////////////////////////////////////////////////////////////////////
    // SingleFileDirectorGenerator Execute Override methods

    @Override
    protected void parseArgs() {

        if ( hasArg( ARG_SHOW_ATTRS)) showAttrs = Boolean.parseBoolean( getArg( ARG_SHOW_ATTRS));
        if ( hasArg( ARG_SHOW_FIELDS)) showAttrs = Boolean.parseBoolean( getArg( ARG_SHOW_FIELDS));
        if ( hasArg( ARG_SHOW_ABSTRACTS)) showAbstracts = Boolean.parseBoolean( getArg( ARG_SHOW_ABSTRACTS));
        if ( hasArg( ARG_EMBEDDED_ATTR)) embeddedAttr = getArg( ARG_EMBEDDED_ATTR);
        if ( hasArg( ARG_EMBEDDED_ATTR_VALUES)) embeddedValues = getArg( ARG_EMBEDDED_ATTR_VALUES);
        if ( hasArg( ARG_DEBUG)) showAttrs = Boolean.parseBoolean( getArg( ARG_DEBUG));

        if ( log.isDebugEnabled() ) log.debug(toString());
    }

    @Override
    protected PlantUMLWriter getWriter(MetaDataLoader loader, PrintWriter pw ) {

        PlantUMLWriter w = new PlantUMLWriter(loader, pw)
                .showAttrs(showAttrs)
                .showFields(showFields)
                .showAbstracts(showAbstracts)
                .setDebug(debug);

        if ( embeddedAttr != null ) {
            List<String> values = null;
            if ( embeddedValues != null ) {
                if ( embeddedValues.contains(",")) {
                    String [] split = embeddedValues.split(",");
                    for ( String s : split ) {
                        if ( !s.trim().isEmpty() ) {
                            values.add( s.trim() );
                        }
                    }
                } else {
                    values.add( embeddedValues );
                }
            }
            w.useEmbeddedNameAndValues( embeddedAttr, values );
        }

        return w;
    }

    @Override
    protected void writeFile(PlantUMLWriter writer) throws GeneratorIOException {
        log.info( "Writing PlantUML File: " + getOutputFilename() );
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
