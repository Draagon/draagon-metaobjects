package com.draagon.meta.generator.util;

import com.draagon.meta.MetaData;
import com.draagon.meta.loader.MetaDataLoader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

public class GeneratorUtil {

    public static <T extends MetaData> Collection<T> getFilteredMetaData(MetaDataLoader loader, Class<T> clazz, List<String> filters ) {

        List<T> out = new ArrayList<>();
        for ( T md : loader.getMetaData( clazz, true )) {
            for ( String f : filters ) {
                if ( filterMatch( md.getName(), f ));
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
}
