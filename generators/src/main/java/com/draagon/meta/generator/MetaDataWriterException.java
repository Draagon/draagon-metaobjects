package com.draagon.meta.generator;

public class MetaDataWriterException extends Exception {

    public MetaDataWriterException( MetaDataWriter writer, String msg ) {
        this( writer, msg, null );
    }

    public MetaDataWriterException( MetaDataWriter writer, String msg, Exception e ) {
        super( getPrefix( writer ) + " " + msg, e );
    }

    protected static String getPrefix( MetaDataWriter writer ) {
        StringBuilder b = new StringBuilder();
        b.append("[");
        b.append( writer.getClass().getSimpleName() ).append("{");
        b.append("loader=").append(writer.getLoader().getName());
        if ( writer.getName() != null ) b.append(",name=").append(writer.getName());
        b.append("}]");
        return b.toString();
    }
}
