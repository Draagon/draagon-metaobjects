/*
 * Copyright 2003 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */

package com.draagon.meta.manager.db;

import com.draagon.meta.MetaException;
import com.draagon.meta.manager.ObjectConnection;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ObjectConnectionDB implements ObjectConnection
{
    private static Log log = LogFactory.getLog(ObjectConnectionDB.class);

    private Connection mConn;

    public ObjectConnectionDB( Connection c ) {
        if ( c == null ) throw new IllegalArgumentException("Connection cannot be null");
        mConn = c;
    }

    public Object getDatastoreConnection() {
        return mConn;
    }

    public void setReadOnly(boolean state)
            throws MetaException {
        try {
            mConn.setReadOnly(state);
        }
        catch (Exception e) {
            throw new MetaException("Could not set JDBC Connection to read-only", e);
        }
    }

    public boolean isReadOnly()
            throws MetaException {
        try {
            return mConn.isReadOnly();
        }
        catch (Exception e) {
            throw new MetaException("Could not determine if JDBC was read-only", e);
        }
    }

    public void setAutoCommit(boolean state)
            throws MetaException {
        try {
            mConn.setAutoCommit(state);
        }
        catch (Exception e) {
            throw new MetaException("Could not set JDBC connection to auto commit", e);
        }
    }

    public boolean getAutoCommit()
            throws MetaException {
        try {
            return mConn.getAutoCommit();
        }
        catch (Exception e) {
            throw new MetaException("Could not get JDBC auto commit status", e);
        }
    }

    public void commit()
            throws MetaException {
        try {
            mConn.commit();
        }
        catch (Exception e) {
            throw new MetaException("Could not commit on JDBC connection", e);
        }
    }

    public void rollback()
            throws MetaException {
        try {
            mConn.rollback();
        }
        catch (Exception e) {
            throw new MetaException("Could not rollback on JDBC Connection", e);
        }
    }

    public void close() {
    	
    	try {
			mConn.close();
		} catch (SQLException e) {
			log.warn( "(close) Problem commiting when closing connection", e );
		}
    }

    public boolean isClosed()
            throws MetaException {
        try {
            return mConn.isClosed();
        }
        catch (Exception e) {
            throw new MetaException("Could not determine if JDBC connection is closed", e);
        }
    }
}
