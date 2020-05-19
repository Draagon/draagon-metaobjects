package com.draagon.meta.mojo;

import java.util.List;

public class LoaderParam {

    private String name = null;
    private String classname = null;
    private String sourceDir = null;
    private List<String> sources = null;
    private List<String> filters = null;

    public LoaderParam() {}

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setLoaderName(String name) {
        this.name = name;
    }

    public String getClassname() {
        return classname;
    }

    public void setClassname(String classname) {
        this.classname = classname;
    }

    //public String getSourceDir() {
    //    return sourceDir;
    //}

    //public void setSourceDir(String sourceDir) {
    //    this.sourceDir = sourceDir;
    //}

    public List<String> getSources() {
        return sources;
    }

    public void setSources(List<String> sources) {
        this.sources = sources;
    }

    public List<String> getFilters() {
        return filters;
    }

    public void setFilters(List<String> filters) {
        this.filters = filters;
    }
}
