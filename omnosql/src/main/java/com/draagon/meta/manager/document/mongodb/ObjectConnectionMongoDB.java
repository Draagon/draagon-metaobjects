/*
 * Copyright 2003 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */

package com.draagon.meta.manager.document.mongodb;

import com.draagon.meta.MetaDataException;
import com.draagon.meta.manager.ObjectConnection;
import com.draagon.meta.manager.PersistenceException;

/**
 * MongoDB implementation of ObjectConnection.
 * This is a reference implementation showing how to wrap MongoDB connections.
 */
public class ObjectConnectionMongoDB implements ObjectConnection {
    
    private final String connectionString;
    private final String databaseName;
    private Object mongoDatabase; // In real implementation: MongoDatabase
    private boolean closed = false;

    public ObjectConnectionMongoDB(String connectionString, String databaseName) {
        this.connectionString = connectionString;
        this.databaseName = databaseName;
        // In real implementation: 
        // MongoClient mongoClient = MongoClients.create(connectionString);
        // this.mongoDatabase = mongoClient.getDatabase(databaseName);
    }

    @Override
    public Object getDatastoreConnection() {
        return mongoDatabase;
    }

    @Override
    public void setReadOnly(boolean state) throws PersistenceException {
        // MongoDB doesn't have a read-only mode at the connection level
        // This could be implemented by restricting operations
    }

    @Override
    public boolean isReadOnly() throws PersistenceException {
        return false; // MongoDB connections are not read-only by default
    }

    @Override
    public void setAutoCommit(boolean state) throws PersistenceException {
        // MongoDB operations are auto-committed by default
        // Transactions are handled separately
    }

    @Override
    public boolean getAutoCommit() throws PersistenceException {
        return true; // MongoDB operations auto-commit
    }

    @Override
    public void commit() throws PersistenceException {
        // In MongoDB, individual operations auto-commit
        // This would only be meaningful within a transaction
    }

    @Override
    public void rollback() throws PersistenceException {
        // Only meaningful within a transaction
        // In real implementation: session.abortTransaction();
    }

    @Override
    public void close() throws MetaDataException {
        if (!closed) {
            closed = true;
            // In real implementation: mongoClient.close();
        }
    }

    @Override
    public boolean isClosed() throws PersistenceException {
        return closed;
    }

    @Override
    public void beginTransaction() throws PersistenceException {
        // In real implementation:
        // ClientSession session = mongoClient.startSession();
        // session.startTransaction();
    }

    @Override
    public void endTransaction(boolean commit) throws PersistenceException {
        if (commit) {
            commit();
        } else {
            rollback();
        }
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public String getConnectionString() {
        return connectionString;
    }
}