package com.draagon.meta.io.util;

import com.draagon.meta.MetaData;

import java.util.ArrayList;
import java.util.List;

public class PathTracker {

    private List<String> pathList = new ArrayList<>();

    public void inc( MetaData md ) {
        if ( md == null ) inc( "[null]" );
        else inc( "["+md.getTypeName()+":"+md.getName()+"]" );
    }

    public void inc( String path ) {
        pathList.add( "/"+path );
    }

    public String dec() {
        return pathList.remove( pathList.size()-1 );
    }

    public String getPathAndClear() {
        String path = toString();
        pathList.clear();
        return path;
    }

    public boolean isAtRoot() {
        return pathList.isEmpty();
    }

    public String toString() {
        StringBuilder b = new StringBuilder();
        for ( String p : pathList ) b.append( p );
        return b.toString();
    }
}
