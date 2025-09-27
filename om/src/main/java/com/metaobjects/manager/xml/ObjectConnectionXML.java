package com.metaobjects.manager.xml;

import com.metaobjects.object.MetaObject;
import com.metaobjects.manager.*;

import java.util.*;

public class ObjectConnectionXML implements ObjectConnection {
    //private static Log log = LogFactory.getLog( ObjectConnectionXML.class );

    private Map<MetaObject, List<Object>> tables;
    private boolean readonly = false;
    private boolean auto = true;
    private boolean closed = false;

    public ObjectConnectionXML(Map<MetaObject, List<Object>> tables) {
        this.tables = tables;
    }

    public Object getDatastoreConnection() {
        return tables;
    }

    public void setReadOnly(boolean state) throws PersistenceException {
        readonly = state;
    }

    public boolean isReadOnly() throws PersistenceException {
        return readonly;
    }

    public void setAutoCommit(boolean state) throws PersistenceException {
        auto = state;
    }

    public boolean getAutoCommit() throws PersistenceException {
        return auto;
    }

    public void commit() throws PersistenceException {
        // Do nothing for now
    }

    public void rollback() throws PersistenceException {
        // Do nothing for now
    }

    public void close() throws PersistenceException {
        closed = true;
    }

    public boolean isClosed() throws PersistenceException {
        return closed;
    }
}
