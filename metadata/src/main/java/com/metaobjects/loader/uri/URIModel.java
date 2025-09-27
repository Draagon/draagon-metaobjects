package com.metaobjects.loader.uri;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class URIModel {

    private final String uriType;
    private final String uriSourceType;
    private final String uriSource;
    private final Map<String,String> uriArgs;

    /**
     * Assumes construction from URIHelper only where it is pre-validated
     */
    URIModel( String uriType, String uriSourceType, String uriSource, Map<String,String> uriArgs) {
        this.uriType = uriType;
        this.uriSourceType = uriSourceType;
        this.uriSource = uriSource;
        this.uriArgs = uriArgs;
    }

    public String getUriType() {
        return uriType;
    }

    public String getUriSourceType() {
        return uriSourceType;
    }

    public String getUriSource() {
        return uriSource;
    }

    public URI toURI() {
        return URIHelper.constructValidatedURI( uriType, uriSourceType, uriSource, uriArgs );
    }

    public Map<String,String> getUriArgs() {
        return uriArgs;
    }

    public String getUriArg(String key) {
        return uriArgs.get(key);
    }

    @Override
    public String toString() {
        return "URIModel{" +
                "type='" + uriType + '\'' +
                ", sourceType='" + uriSourceType + '\'' +
                ", source='" + uriSource + '\'' +
                ", args=" + uriArgs +
                '}';
    }
}
