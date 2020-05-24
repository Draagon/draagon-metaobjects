package com.draagon.meta.loader.file;

import com.draagon.meta.MetaDataException;
import com.draagon.meta.loader.LoaderOptions;
import com.draagon.meta.loader.file.json.JsonMetaDataParser;
import com.draagon.meta.loader.file.xml.XMLMetaDataParser;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.regex.Pattern;


/**
 * FileMetaDataLoader Configuration Settings
 */
public class FileLoaderOptions<T extends FileLoaderOptions> extends LoaderOptions<T> {

    /** Holds the array of FileMetaDataParser filename match patterns */
    protected final class PatternParser {

        public final String patternString;
        public final Pattern pattern;
        public final Class<? extends FileMetaDataParser> parserClass;
        public final Constructor<? extends FileMetaDataParser> parserConstructor;

        public PatternParser( String patternString, Class<? extends FileMetaDataParser> parserClass,
                              Constructor<? extends FileMetaDataParser> parserConstructor ) {
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

    private boolean allowAutoAttrs = false;
    private final List<PatternParser> patternParsers = new ArrayList<>();
    private final List<FileMetaDataSources> sources = new ArrayList<>();

    public FileLoaderOptions() {}

    public static void createFileLoaderConfig(List<FileMetaDataSources> sources, boolean shouldRegister ) {
        FileLoaderOptions config = new FileLoaderOptions()
                .addParser( "*.xml", XMLMetaDataParser.class )
                .addParser( "*.json", JsonMetaDataParser.class )
                .setSources( sources )
                .setAllowAutoAttrs( true )
                .setShouldRegister( shouldRegister );
    }

    ///////////////////////////////////////////////////////////////////////////
    // Builder Overloads

    public T setShouldRegister(boolean shouldRegister ) { return super.setShouldRegister( shouldRegister ); }
    public T setVerbose(boolean verbose) { return super.setVerbose( verbose ); }
    public T setStrict(boolean strict) { return super.setStrict( strict ); }

    ///////////////////////////////////////////////////////////////////////////
    // MetaData Sources

    public T setSources( List<FileMetaDataSources> sourcesList ) {
        this.sources.clear();
        this.sources.addAll( sourcesList );
        return (T) this;
    }

    public T addSources( FileMetaDataSources sources ) {
        this.sources.add( sources );
        return (T) this;
    }

    public List<FileMetaDataSources> getSources() {
        return sources;
    }

    public boolean hasSources() {
        return !sources.isEmpty();
    }

    ///////////////////////////////////////////////////////////////////////////
    // MetaData Parsers

    public FileLoaderOptions setParsers(Map<String, Class<? extends FileMetaDataParser>> parserMap ) {
        patternParsers.clear();
        for( String matchPattern : parserMap.keySet() ) {
            addParser( matchPattern, parserMap.get( matchPattern ));
        }
        return this;
    }

    public boolean hasParsers() {
        return !patternParsers.isEmpty();
    }

    public T addParser( String matchPattern, Class<? extends FileMetaDataParser> parserClass ) {

        try {
            Constructor<? extends FileMetaDataParser> c = parserClass.getConstructor(FileMetaDataLoader.class, String.class);
            patternParsers.add(new PatternParser(matchPattern, parserClass, c));
            return (T) this;
        } catch( NoSuchMethodException e ) {
            throw new IllegalArgumentException( "FileMetaDataParser class [" + parserClass.getName() + "] has no Constructor (MetaDataLoader, String)" );
        }
    }

    /** For the specified filename, return the FileMetaDataParser than handles it */
    public FileMetaDataParser getParserForFile(FileMetaDataLoader loader, String filename ) {

        for ( PatternParser pp : patternParsers ) {
            if ( pp.pattern.matcher( filename ).matches()) {
                try {
                    return pp.parserConstructor.newInstance( loader, filename );
                } catch (ReflectiveOperationException e) {
                    throw new MetaDataException( "Unable to instantiate FileMetaDataParser [" + pp.parserClass.getName()
                            + "] for file ["+filename+"] and Loader ["+loader+"]: " + e.getMessage(), e );
                }
            }
        }

        throw new MetaDataException( "No FileMetaDataParser was found for file ["+filename+"] on Loader ["+loader+"]" );
    }

    public boolean allowsAutoAttrs() {
        return allowAutoAttrs;
    }

    public FileLoaderOptions setAllowAutoAttrs(boolean allowAutoAttrs) {
        this.allowAutoAttrs = allowAutoAttrs;
        return this;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Misc Methods

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        FileLoaderOptions<?> config = (FileLoaderOptions<?>) o;
        return allowAutoAttrs == config.allowAutoAttrs &&
                Objects.equals(patternParsers, config.patternParsers) &&
                Objects.equals(sources, config.sources);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), allowAutoAttrs, patternParsers, sources);
    }

    @Override
    public String toString() {
        return "FileLoaderConfig{" +
                "shouldRegister=" + shouldRegister() +
                ", verbose=" + isVerbose() +
                ", strict=" + isStrict() +
                ", allowAutoAttrs=" + allowAutoAttrs +
                ", patternParsers=" + patternParsers +
                ", sources=" + sources +
                '}';
    }
}
