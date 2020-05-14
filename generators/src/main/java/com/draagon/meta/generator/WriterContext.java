package com.draagon.meta.generator;

public abstract class WriterContext <C extends WriterContext, R, N> {

    public final WriterContext parent;
    public final R out;
    public final N node;

    protected WriterContext(R out) {
        this( null, out, null );
    }

    protected WriterContext(WriterContext parent, R out, N node ) {
        this.parent = parent;
        this.out = out;
        this.node = node;
    }

    public abstract C newInstance( C parent, R root, N node );

    public C inc(N node) {
        return newInstance((C) this, out, node );
    }

    public String getNodePathName() {
        return node.toString();
    }

    public String getPath() {
        return (parent==null?"":parent.getPath()+"/")+getNodePathName();
    }

    public String toString() {
        return this.getClass().getSimpleName()+".Context["+getPath()+"]";
    }
}
