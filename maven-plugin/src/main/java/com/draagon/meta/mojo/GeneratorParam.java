package com.draagon.meta.mojo;

import java.util.List;
import java.util.Map;

public class GeneratorParam {

    private String classname = null;
    private Map<String,String> args = null;
    private List<String> filters = null;
    private List<String> scripts = null;

    public GeneratorParam() {}

    public String getClassname() {
        return classname;
    }

    public void setClassname(String classname) {
        this.classname = classname;
    }

    public Map<String, String> getArgs() {
        return args;
    }

    public void setArgs(Map<String, String> args) {
        this.args = args;
    }

    public List<String> getFilters() {
        return filters;
    }

    public void setFilters(String filter) {
        this.filters = filters;
    }

    public List<String> getScripts() {
        return scripts;
    }

    public void setScripts(List<String> scripts) {
        this.scripts = scripts;
    }
}
