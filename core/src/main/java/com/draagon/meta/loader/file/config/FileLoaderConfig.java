package com.draagon.meta.loader.file.config;

import com.draagon.meta.MetaDataException;
import com.draagon.meta.loader.config.LoaderConfig;
import com.draagon.meta.loader.file.FileMetaDataLoader;
import com.draagon.meta.loader.file.MetaDataParser;
import com.draagon.meta.loader.file.MetaDataSources;
import com.draagon.meta.loader.file.json.JsonMetaDataParser;
import com.draagon.meta.loader.file.xml.XMLMetaDataParser;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.regex.Pattern;


/**
 * FileMetaDataLoader Configuration Settings
 */
public class FileLoaderConfig<N extends FileLoaderConfig> extends LoaderConfig<N> {

    /** Holds the array of MetaDataParser filename match patterns */
    protected final class PatternParser {

        public final String patternString;
        public final Pattern pattern;
        public final Class<? extends MetaDataParser> parserClass;
        public final Constructor<? extends MetaDataParser> parserConstructor;

        public PatternParser( String patternString, Class<? extends MetaDataParser> parserClass,
                              Constructor<? extends MetaDataParser> parserConstructor ) {
            this.patternString = patternString;
            this.pattern = Pattern.compile( createRegexFromGlob( patternString ));
            this.parserClass = parserClass;
            this.parserConstructor = parserConstructor;
        }
    }

    public static String createRegexFromGlob(String glob)
    {
        String out = "^";
        for(int i = 0; i < glob.length(); ++i)
        {
            final char c = glob.charAt(i);
            switch(c)
            {
                case '*': out += ".*"; break;
                case '?': out += '.'; break;
                case '.': out += "\\."; break;
                case '\\': out += "\\\\"; break;
                default: out += c;
            }
        }
        out += '$';
        return out;
    }

    private final List<PatternParser> patternParsers = new ArrayList<>();
    private final List<MetaDataSources> sources = new ArrayList<>();

    public FileLoaderConfig() {}

    public static void createFileLoaderConfig( List<MetaDataSources> sources, boolean shouldRegister ) {
        FileLoaderConfig config = new FileLoaderConfig()
                .addParser( "*.xml", XMLMetaDataParser.class )
                .addParser( "*.json", JsonMetaDataParser.class )
                .setSources( sources )
                .setShouldRegister( shouldRegister );
    }

    ///////////////////////////////////////////////////////////////////////////
    // Builder Overloads

    public N setShouldRegister(boolean shouldRegister ) { return super.setShouldRegister( shouldRegister ); }
    public N setVerbose(boolean verbose) { return super.setVerbose( verbose ); }

    ///////////////////////////////////////////////////////////////////////////
    // MetaData Sources

    public N setSources( List<MetaDataSources> sourcesList ) {
        this.sources.clear();
        this.sources.addAll( sourcesList );
        return (N) this;
    }

    public N addSources( MetaDataSources sources ) {
        this.sources.add( sources );
        return (N) this;
    }

    public List<MetaDataSources> getSources() {
        return sources;
    }

    public boolean hasSources() {
        return !sources.isEmpty();
    }

    ///////////////////////////////////////////////////////////////////////////
    // MetaData Parsers

    public FileLoaderConfig setParsers( Map<String, Class<? extends MetaDataParser>> parserMap ) {
        patternParsers.clear();
        for( String matchPattern : parserMap.keySet() ) {
            addParser( matchPattern, parserMap.get( matchPattern ));
        }
        return this;
    }

    public boolean hasParsers() {
        return !patternParsers.isEmpty();
    }

    public N addParser( String matchPattern, Class<? extends MetaDataParser> parserClass ) {

        try {
            Constructor<? extends MetaDataParser> c = parserClass.getConstructor(FileMetaDataLoader.class, String.class);
            patternParsers.add(new PatternParser(matchPattern, parserClass, c));
            return (N) this;
        } catch( NoSuchMethodException e ) {
            throw new IllegalArgumentException( "MetaDataParser class [" + parserClass.getName() + "] has no Constructor (MetaDataLoader, String)" );
        }
    }

    /** For the specified filename, return the MetaDataParser than handles it */
    public MetaDataParser getParserForFile(FileMetaDataLoader loader, String filename ) {

        for ( PatternParser pp : patternParsers ) {
            if ( pp.pattern.matcher( filename ).matches()) {
                try {
                    return pp.parserConstructor.newInstance( loader, filename );
                } catch (ReflectiveOperationException e) {
                    throw new MetaDataException( "Unable to instantiate MetaDataParser [" + pp.parserClass.getName()
                            + "] for file ["+filename+"] and Loader ["+loader+"]: " + e.getMessage(), e );
                }
            }
        }

        throw new MetaDataException( "No MetaDataParser was found for file ["+filename+"] on MetaDataLoader ["+loader+"]" );
    }
}
