package com.draagon.meta.mojo;

import java.util.List;
import java.util.Map;

public class Generator {

    private String classname = null;
    private Map<String,String> args = null;
    private String filter = null;
    private List<String> scripts = null;

    public Generator() {}

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

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public List<String> getScripts() {
        return scripts;
    }

    public void setScripts(List<String> scripts) {
        this.scripts = scripts;
    }
}
