package com.draagon.meta.manager;

import com.draagon.meta.MetaDataException;

/**
 * Enhanced ObjectConnection interface with AutoCloseable support for better resource management
 */
public interface ObjectConnection extends AutoCloseable {
    
    /**
     * Gets the underlying datastore connection
     */
    Object getDatastoreConnection();

    /**
     * Sets the connection read-only state
     */
    void setReadOnly(boolean state) throws PersistenceException;
    
    /**
     * Returns whether the connection is read-only
     */
    boolean isReadOnly() throws PersistenceException;

    /**
     * Sets the auto-commit state
     */
    void setAutoCommit(boolean state) throws PersistenceException;
    
    /**
     * Returns the auto-commit state
     */
    boolean getAutoCommit() throws PersistenceException;

    /**
     * Commits the current transaction
     */
    void commit() throws PersistenceException;
    
    /**
     * Rolls back the current transaction
     */
    void rollback() throws PersistenceException;

    /**
     * Closes the connection and releases resources
     * Enhanced to support try-with-resources
     */
    @Override
    void close() throws MetaDataException;
    
    /**
     * Returns whether the connection is closed
     */
    boolean isClosed() throws PersistenceException;
    
    /**
     * Begins a new transaction (if supported by the underlying datastore)
     */
    default void beginTransaction() throws PersistenceException {
        setAutoCommit(false);
    }
    
    /**
     * Ends the current transaction with commit or rollback
     */
    default void endTransaction(boolean commit) throws PersistenceException {
        if (commit) {
            commit();
        } else {
            rollback();
        }
    }
}
