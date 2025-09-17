package com.draagon.meta.generator.util;

public class FileIndentor {

    private final FileIndentor parent;
    private final String indentor;
    private final String prefix;
    private final int level;

    public FileIndentor(String indendtor ) {
        this(null, indendtor, "", 0);
    }

    public FileIndentor(FileIndentor parent, String indentor, String prefix, int level ) {
        this.parent = parent;
        this.indentor = indentor;
        this.prefix = prefix;
        this.level = level;
    }

    public FileIndentor inc() {
        return new FileIndentor( this, indentor, prefix + indentor, level+1 );
    }

    public FileIndentor dec() {
        return parent;
    }

    public String pre() {
        return prefix;
    }

    public int getLevel() {
        return level;
    }

    public boolean isIndented() {
        return parent != null;
    }

    @Override
    public String toString() {
        return "FileIndentor{" +
                "parent=" + (parent!=null) +
                ", indentor='" + indentor + '\'' +
                ", level='" + level + '\'' +
                '}';
    }
}
