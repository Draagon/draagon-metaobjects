package com.draagon.meta.loader.mojo;

import java.net.URI;
import java.util.List;
import java.util.Map;

public interface MojoSupport {

    public final static String ARG_REGISTER     = "register";
    public final static String ARG_VERBOSE      = "verbose";
    public final static String ARG_STRICT       = "strict";

    public void mojoInit( Map<String, String> args );
    public void mojoSetSources( List<String> sourceList );
    public void mojoSetSourceDir( String sourceDir );
}
