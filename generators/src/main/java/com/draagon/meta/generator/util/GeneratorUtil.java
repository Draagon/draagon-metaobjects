package com.draagon.meta.generator.util;

import com.draagon.meta.MetaData;
import com.draagon.meta.generator.MetaDataFilters;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.object.MetaObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class GeneratorUtil {

    public static Collection<MetaData> getFilteredMetaData(MetaDataLoader loader, MetaDataFilters filters ) {
        return filterMetaData( loader.getChildren(), MetaData.class, filters );
    }

    public static <T extends MetaData> Collection<T> getFilteredMetaData(MetaDataLoader loader, Class<T> clazz, MetaDataFilters filters ) {
        return filterMetaData( loader.getMetaData( clazz ), clazz, filters );
    }

    private static <T extends MetaData> Collection<T> filterMetaData( Collection<T> in, Class<T> clazz, MetaDataFilters filtersIn ) {

        List<T> out = new ArrayList<>();

        List<String> filters = filtersIn==null?null:filtersIn.getFilters();

        if ( filters == null || filters.isEmpty() ) {
            out.addAll(in);
        }
        else {
            List<String> incFilters = filters.stream()
                    .filter( f -> !f.startsWith("!"))
                    .collect(Collectors.toList());
            List<String> excFilters = filters.stream()
                    .filter( f -> f.startsWith("!"))
                    .collect(Collectors.toList());

            for (T md : in) {

                boolean shouldAdd = incFilters.isEmpty();

                // Check inclusive filters
                for (String f : incFilters) {
                    if ( filterMatch(md, f)) {
                        shouldAdd = true;
                        break;
                    }
                }

                // Check inclusive filters
                for (String f : excFilters) {
                    if ( filterMatch(md, f.substring(1))) {
                        shouldAdd = false;
                        break;
                    }
                }

                if (shouldAdd) out.add(md);
            }
        }

        return out;
    }

    public static boolean filterMatch(MetaData md, String filter ) {

        String f = filter;
        int i = filter.indexOf( ".[");
        if ( i > 0 ) f = filter.substring( 0, i );

        boolean match = filterByName(md.getName(), f);

        if ( match && i > 0 ) match = filterByMetaData( md, filter.substring( i+1 ).trim());

        return match;
    }

    public static boolean filterByMetaData(MetaData md, String metaDataFilter) {

        // TODO:  Add better error handling
        String [] v1 = metaDataFilter.split( "=");
        if ( v1.length > 0 ) {

            String[] attrs = v1[0].split(":");
            if ( attrs.length == 2 && attrs[0].equals("[attr")) {

                String an = attrs[1].substring( 0, attrs[1].length()-1);
                if ( md.hasMetaAttr( an ) ) {
                    if (v1.length > 1) {
                        String[] vals = v1[1].split("\"");
                        if ( vals.length > 0 ) {
                            return vals[0].equals( md.getMetaAttr(an).getValueAsString() );
                        }
                    }
                    else {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public static boolean filterByName( String metaDataName, String nameFilter) {
        return Pattern.compile(createRegexFromGlob(nameFilter)).matcher( metaDataName ).matches();
    }

    private static String createRegexFromGlob(String glob)
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

    public static List<String> getUniquePackages(Collection<? extends MetaData> filtered ) throws IOException {
        List<String> pkgs = new ArrayList<>();

        filtered.forEach( md -> {
            if ( md instanceof MetaObject
                    && !pkgs.contains( md.getPackage() )) {
                pkgs.add( md.getPackage() );
            }
        });

        return pkgs;
    }

    public static boolean isAbstract( MetaData md ) {
        if ( md.hasMetaAttr("_isAbstract")
                && Boolean.TRUE.equals( md.getMetaAttr( "_isAbstract" ).getValue())) {
            return true;
        }
        return false;
    }
}
