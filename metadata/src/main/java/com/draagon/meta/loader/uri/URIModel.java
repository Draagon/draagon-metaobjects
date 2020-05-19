package com.draagon.meta.loader.uri;

import java.net.URI;

public class URIModel {

    private final String uriType;
    private final String uriSourceType;
    private final String uriSource;

    /**
     * Assumes construction from URIHelper only where it is pre-validated
     */
    URIModel( String uriType, String uriSourceType, String uriSource ) {
        this.uriType = uriType;
        this.uriSourceType = uriSourceType;
        this.uriSource = uriSource;
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
        return URIHelper.constructValidatedURI( uriType, uriSourceType, uriSource );
    }

    @Override
    public String toString() {
        return "URIModel{" +
                "uriType='" + uriType + '\'' +
                ", uriSourceType='" + uriSourceType + '\'' +
                ", uriSource='" + uriSource + '\'' +
                '}';
    }
}
