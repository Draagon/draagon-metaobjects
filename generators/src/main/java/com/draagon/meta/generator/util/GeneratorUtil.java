package com.draagon.meta.generator.util;

import com.draagon.meta.MetaData;
import com.draagon.meta.loader.MetaDataLoader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

public class GeneratorUtil {


    public static Collection<MetaData> getFilteredMetaData(MetaDataLoader loader, List<String> filters ) {

        return filterMetaData( loader.getChildren(), MetaData.class, filters );
    }

    public static <T extends MetaData> Collection<T> getFilteredMetaData(MetaDataLoader loader, Class<T> clazz, List<String> filters ) {
        return filterMetaData( loader.getMetaData( clazz ), clazz, filters );
    }

    public static <T extends MetaData> Collection<T> filterMetaData( Collection<T> in, Class<T> clazz, List<String> filters ) {

        List<T> out = new ArrayList<>();

        for ( T md : in ) {

            if ( filters == null || filters.isEmpty() ) {
                out.add(md);
            }
            else {
                for (String f : filters) {
                    if (filterMatch(md.getName(), f)) {
                        out.add(md);
                        break;
                    }
                }
            }
        }

        return out;
    }

    public static boolean filterMatch(String metaDataName, String filter ) {
        return  Pattern.compile(createRegexFromGlob(filter)).matcher( metaDataName ).matches();
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
                case '@': out += "[^:]+"; break;
                case '?': out += "."; break;
                case '.': out += "\\."; break;
                case ':': out += "\\:"; break;      // TODO:  This doesn't seem to work on enforcing the ::'s as a separator for *
                case '\\': out += "\\\\"; break;
                default: out += c;
            }
        }
        out += '$';
        return out;
    }

    public static String toRelativePackage( String p1, String p2 ) {

        if (p1.isEmpty() && p2.isEmpty() ) {
            return "";
        }

        if ( p2.isEmpty() ) return "";
        if ( p1.isEmpty() ) return p2 + "::";

        String [] p1s = p1.split("::");
        String [] p2s = p2.split("::");

        if ( !p1s[0].equals(p2s[0])) return p2 + "::";

        int max = p1s.length;
        if ( p2s.length < p1s.length ) max = p2s.length;

        int n = 0;
        while( max > n && p1s[n].equals(p2s[n])) {
            n++;
        }

        StringBuilder out = new StringBuilder();

        if ( p2s.length < p1s.length ) {
            out.append("...::");
        }
        else if (n==max) {
            out.append("::");
        }
        else {
            for (int i = n; i < max; i++) {
                out.append("..::");
            }
        }

        for (int i = n; i < p2s.length; i++ ) {
            out.append( p2s[i] +"::");
        }

        //out.append( "::");

        return out.toString();
    }

    public static String toCamelCase( String text, boolean capitalizeFirstChar ) {

        if (text == null || text.isEmpty()) {
            return text;
        }

        StringBuilder converted = new StringBuilder();

        boolean first = true;
        boolean convertNext = false;
        for (char ch : text.toCharArray()) {
            if (ch == '-') {
                convertNext = true;
            }
            else if (convertNext) {
                ch = Character.toTitleCase(ch);
                convertNext = false;
                converted.append(ch);
            }
            else if (first && capitalizeFirstChar) {
                ch = Character.toTitleCase(ch);
                converted.append(ch);
            }
            else {
                converted.append(ch);
            }
            if ( first ) first = false;
        }

        return converted.toString();
    }
}
