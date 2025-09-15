package com.draagon.meta.generator.direct.plantuml;
import com.draagon.meta.generator.GeneratorIOException;
import com.draagon.meta.generator.direct.SingleFileDirectGeneratorBase;
import com.draagon.meta.loader.MetaDataLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.util.List;

import static com.draagon.meta.generator.util.GeneratorUtil.getFilteredMetaData;

public class PlantUMLGenerator extends SingleFileDirectGeneratorBase<PlantUMLWriter> {

    private static final Logger log = LoggerFactory.getLogger(PlantUMLGenerator.class);

    public final static String ARG_SHOW_ATTRS = "showAttrs";
    public final static String ARG_SHOW_FIELDS = "showFields";
    public final static String ARG_SHOW_ABSTRACTS = "showAbstracts";
    public final static String ARG_DRAW_KEYS = "drawKeys";
    public final static String ARG_DRAW_REFS = "drawRefs";
    public final static String ARG_EMBEDDED_ATTR = "embeddedAttr";
    public final static String ARG_EMBEDDED_ATTR_VALUES = "embeddedAttrValues";
    public final static String ARG_DEBUG = "debug";

    private boolean showAttrs = false;
    private boolean showFields = false;
    private boolean showAbstracts = true;
    private boolean drawKeys = true;
    private boolean drawRefs = false;
    private String embeddedAttr = null;
    private String embeddedValues = null;
    private boolean debug = false;

    //////////////////////////////////////////////////////////////////////
    // SingleFileDirectorGenerator Execute Override methods

    @Override
    protected void parseArgs() {

        if ( hasArg( ARG_SHOW_ATTRS)) showAttrs = Boolean.parseBoolean( getArg( ARG_SHOW_ATTRS));
        if ( hasArg( ARG_SHOW_FIELDS)) showFields = Boolean.parseBoolean( getArg( ARG_SHOW_FIELDS));
        if ( hasArg( ARG_SHOW_ABSTRACTS)) showAbstracts = Boolean.parseBoolean( getArg( ARG_SHOW_ABSTRACTS));
        if ( hasArg( ARG_DRAW_KEYS)) drawKeys = Boolean.parseBoolean( getArg( ARG_DRAW_KEYS));
        if ( hasArg( ARG_DRAW_REFS)) drawRefs = Boolean.parseBoolean( getArg( ARG_DRAW_REFS));
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
                .drawKeys(drawKeys)
                .drawRefs(drawRefs)
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
        final StringBuilder sb = new StringBuilder(this.getClass().getSimpleName()+"{");
        sb.append("args=").append(getArgs());
        sb.append(", filters=").append(getFilters());
        sb.append(", showAttrs=").append(showAttrs);
        sb.append(", showFields=").append(showFields);
        sb.append(", showAbstracts=").append(showAbstracts);
        sb.append(", drawKeys=").append(drawKeys);
        sb.append(", drawRefs=").append(drawRefs);
        sb.append('}');
        return sb.toString();
    }
}
