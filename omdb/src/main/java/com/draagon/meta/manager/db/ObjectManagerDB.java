/*
 * Copyright 2003 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta.manager.db;

import com.draagon.meta.field.MetaField;
import com.draagon.meta.manager.StateAwareMetaObject;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.*;
import com.draagon.meta.manager.*;
import com.draagon.meta.manager.db.driver.*;
import com.draagon.meta.manager.exp.Expression;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import javax.sql.DataSource;

/**
 * The Object Manager Base is able to add, update, delete, and retrieve objects
 * of those types from a datastore.
 */
public class ObjectManagerDB extends ObjectManager implements DBOperations {

    private static final Logger log = LoggerFactory.getLogger(ObjectManagerDB.class);
    private final static String CREATE_MAP_ATTR = "dbCreateMap";
    private final static String READ_MAP_ATTR = "dbReadMap";
    private final static String UPDATE_MAP_ATTR = "dbUpdateMap";
    private final static String DELETE_MAP_ATTR = "dbDeleteMap";
    private final static String HAS_CREATE_MAP_ATTR = "hasDbCreateMap";
    private final static String HAS_READ_MAP_ATTR = "hasDbReadMap";
    private final static String HAS_UPDATE_MAP_ATTR = "hasDbUpdateMap";
    private final static String HAS_DELETE_MAP_ATTR = "hasDbDeleteMap";
    public final static String ALLOW_DIRTY_WRITE = "dbAllowDirtyWrite";
    public final static String DIRTY_WRITE_CHECK_FIELD = "dbDirtyWriteCheckField";
    public final static String POPULATE_FILE = "dbPopulateFile";
    private MappingHandler mMappingHandler = null;
    private DatabaseDriver mDriver = null;
    private DataSource mSource = null;
    private boolean enforceTransaction = false;
    //private static Cache<String, MetaObject> templateCache = new Cache<String, MetaObject>(true, 3000, 1500);

    //private ArrayList mValidatedClasses = new ArrayList();
    //private boolean autoCreateTables = false;
    //private HashMap mDirtyFieldCache = new HashMap();
    public ObjectManagerDB() {
    }

    /**
     * Handles enforcing transactions on SQL queries
     */
    @Override
    public void setEnforceTransaction(boolean enforce) {
        enforceTransaction = enforce;
    }

    /**
     * Returns whether to enforce transactions
     */
    @Override
    public boolean enforceTransaction() {
        return enforceTransaction;
    }

    /**
     * Checks to see if a transaction exists or not
     */
    protected void checkTransaction(Connection c, boolean throwEx) throws MetaDataException {
        try {
            if (enforceTransaction() && c.getAutoCommit()) {
                MetaDataException me = new MetaDataException("The connection retrieved is not operating under a transaction and transactions are being enforced");
                if (throwEx) {
                    throw me;
                } else {
                    log.warn(me.getMessage(), me);
                }
            }
        } catch (SQLException e) {
            throw new MetaDataException("Error checking connection for transaction enforcement: " + e.getMessage(), e);
        }
    }

    ///////////////////////////////////////////////////////
    // CONNECTION HANDLING METHODS
    //
    /**
     * Retrieves a connection object representing the datastore with enhanced error handling
     */
    @Override
    public ObjectConnection getConnection() throws MetaDataException {
        DataSource ds = getDataSource();
        if (ds == null) {
            throw new IllegalArgumentException("No DataSource was specified for this ObjectManager, cannot request connection");
        }

        try {
            Connection c = ds.getConnection();
            if (c == null) {
                throw new MetaDataException("DataSource returned null connection");
            }
            
            // Verify connection is valid
            if (!c.isValid(5)) { // 5 second timeout
                c.close();
                throw new MetaDataException("Connection is not valid");
            }
            
            return new ObjectConnectionDB(c);
        } catch (SQLException e) {
            throw new MetaDataException("Could not retrieve a connection from the datasource: " + e.getMessage(), e);
        }
    }

    /**
     * Release the Database Connection with improved error handling
     */
    @Override
    public void releaseConnection(ObjectConnection oc) throws MetaDataException {
        if (oc == null) {
            log.warn("Attempting to release null connection");
            return;
        }
        
        try {
            oc.close();
        } catch (Exception e) {
            log.error("Error releasing database connection", e);
            throw new MetaDataException("Failed to release connection properly", e);
        }
    }

    /**
     * Sets the Data Source to use for database connections
     */
    @Override
    public void setDataSource(DataSource ds) {
        mSource = ds;
    }

    /**
     * Retrieves the data source
     */
    @Override
    public DataSource getDataSource() {
        return mSource;
    }

    /**
     * Initializes the ObjectManager
     */
    public void init() throws Exception {
        super.init();

        if (getDataSource() == null) {
            throw new IllegalStateException("No DataSource was specified");
        }
    }

    ///////////////////////////////////////////////////////
    // DATABASE DRIVER METHODS
    //
    @Override
    public void setDriverClass(String className) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        Class<?> c = Class.forName(className);
        setDatabaseDriver((DatabaseDriver) c.newInstance());
    }

    @Override
    public void setDatabaseDriver(Object dd) {
        mDriver = (DatabaseDriver) dd;
        mDriver.setManager(this);
    }

    @Override
    public synchronized Object getDatabaseDriver() {
        if (mDriver == null) {
            mDriver = new GenericSQLDriver();
            mDriver.setManager(this);
        }

        return mDriver;
    }

    /**
     * Internal method to get the typed database driver
     */
    protected DatabaseDriver getTypedDatabaseDriver() {
        return (DatabaseDriver) getDatabaseDriver();
    }

    ///////////////////////////////////////////////////////
    // PERSISTENCE METHODS
    //
    @Override
    public MappingHandler getDefaultMappingHandler() {
        return new SimpleMappingHandlerDB();
    }

    @Override
    public void setMappingHandler(MappingHandler handler) {
        mMappingHandler = handler;
    }

    @Override
    public MappingHandler getMappingHandler() {
        if (mMappingHandler == null) {
            mMappingHandler = getDefaultMappingHandler();
        }
        return mMappingHandler;
    }

    /**
     * Gets the create mapping
     */
    protected ObjectMapping getCreateMapping(MetaObject mc) {
        ObjectMapping mapping = (ObjectMapping) mc.getCacheValue(CREATE_MAP_ATTR);
        if (mapping == null) {
            mapping = getMappingHandler().getCreateMapping(mc);
            if (mapping != null) {
                mc.setCacheValue(CREATE_MAP_ATTR, mapping);
            }
        }
        return mapping;
    }

    /**
     * Gets the read mapping
     */
    protected ObjectMapping getReadMapping(MetaObject mc) {
        ObjectMapping mapping = (ObjectMapping) mc.getCacheValue(READ_MAP_ATTR);
        if (mapping == null) {
            mapping = getMappingHandler().getReadMapping(mc);
            if (mapping != null) {
                mc.setCacheValue(READ_MAP_ATTR, mapping);
                mc.setCacheValue(HAS_READ_MAP_ATTR, Boolean.TRUE);
            } else {
                mc.setCacheValue(HAS_READ_MAP_ATTR, Boolean.FALSE);
            }
        }
        return mapping;
    }

    /**
     * Gets the update mapping
     */
    protected ObjectMapping getUpdateMapping(MetaObject mc) {
        ObjectMapping mapping = (ObjectMapping) mc.getCacheValue(UPDATE_MAP_ATTR);
        if (mapping == null) {
            mapping = getMappingHandler().getUpdateMapping(mc);
            if (mapping != null) {
                mc.setCacheValue(UPDATE_MAP_ATTR, mapping);
                mc.setCacheValue(HAS_UPDATE_MAP_ATTR, Boolean.TRUE);
            } else {
                mc.setCacheValue(HAS_UPDATE_MAP_ATTR, Boolean.FALSE);
            }
        }
        return mapping;
    }

    /**
     * Gets the delete mapping
     */
    protected ObjectMapping getDeleteMapping(MetaObject mc) {
        ObjectMapping mapping = (ObjectMapping) mc.getCacheValue(DELETE_MAP_ATTR);
        if (mapping == null) {
            mapping = getMappingHandler().getUpdateMapping(mc);
            if (mapping != null) {
                mc.setCacheValue(DELETE_MAP_ATTR, mapping);
                mc.setCacheValue(HAS_DELETE_MAP_ATTR, Boolean.TRUE);
            } else {
                mc.setCacheValue(HAS_DELETE_MAP_ATTR, Boolean.FALSE);
            }
        }
        return mapping;
    }

    /**
     * Is this a createable class
     */
    public boolean isCreateableClass(MetaObject mc) {
        Boolean hasMapping = (Boolean) mc.getCacheValue(HAS_CREATE_MAP_ATTR);
        if (hasMapping == null) {
            if (getCreateMapping(mc) == null) {
                return false;
            } else {
                return true;
            }
        }
        return hasMapping.booleanValue();
    }

    /**
     * Is this a readable class
     */
    public boolean isReadableClass(MetaObject mc) {
        Boolean hasMapping = (Boolean) mc.getCacheValue(HAS_READ_MAP_ATTR);
        if (hasMapping == null) {
            if (getReadMapping(mc) == null) {
                return false;
            } else {
                return true;
            }
        }
        return hasMapping.booleanValue();
    }

    /**
     * Gets the update mapping to the DB
     */
    public boolean isUpdateableClass(MetaObject mc) {
        Boolean hasMapping = (Boolean) mc.getCacheValue(HAS_UPDATE_MAP_ATTR);
        if (hasMapping == null) {
            if (getUpdateMapping(mc) == null) {
                return false;
            } else {
                return true;
            }
        }
        return hasMapping.booleanValue();
    }

    /**
     * Gets the delete mapping to the DB
     */
    public boolean isDeleteableClass(MetaObject mc) {
        Boolean hasMapping = (Boolean) mc.getCacheValue(HAS_DELETE_MAP_ATTR);
        if (hasMapping == null) {
            if (getDeleteMapping(mc) == null) {
                return false;
            } else {
                return true;
            }
        }
        return hasMapping.booleanValue();
    }

    /**
     * Breaks apart the values from the id field represented by the keys
     */
    /*protected Collection getKeyValuesFromRef( MetaClass mc, Collection keys, String ref )
     throws MetaDataException
     {
     ArrayList values = new ArrayList();

     String tmp = ref;

     // Split apart the id field
     for ( Iterator i = keys.iterator(); i.hasNext(); )
     {
     MetaField f = (MetaField) i.next();

     if ( tmp == null || tmp.length() == 0 )
     throw new MetaDataException( "Invalid Reference [" + ref + "] for MetaClass [" + mc + "]" );

     String val = null;
     int j = tmp.indexOf( '-' );
     if ( j >= 0 )
     {
     val = tmp.substring( 0, j );
     tmp = tmp.substring( j + 1 );
     }
     else
     {
     val = tmp;
     tmp = null;
     }

     values.add( val );
     }

     return values;
     }*/
    /**
     * Sets the prepared statement values for the keys of a class and a specifed
     * id.
     */
    /*    protected void setStatementValuesForRef( PreparedStatement s, Collection keys, int start, ObjectRef ref )
     throws MetaDataException, SQLException
     {
     //Collection values = getKeyValuesFromRef( keys, ref );
     String [] ids = ref.getIds();

     int k = 0;
     int j = start;
     //Iterator v = values.iterator();
     for ( Iterator i = keys.iterator(); i.hasNext(); j++, k++ )
     {
     MetaField f = (MetaField) i.next();
     String value = ids[ k ];

     setStatementValue( s, f, j, value );
     }
     }*/
    /**
     * Parses an Object returned from the database
     */
    protected void parseObject(ResultSet rs, Collection<MetaField> fields, MetaObject mc, Object o) throws SQLException, MetaDataException {
        if (!isReadableClass(mc)) {
            throw new MetaDataException("MetaClass [" + mc + "] is not readable");
        }

        int j = 1;
        for (MetaField f : fields) {
            parseField(o, f, rs, j++);
        }

        if (mc instanceof StateAwareMetaObject) {
            // It was pulled from the database, so it doesn't need to be flagged as modified
            ((StateAwareMetaObject) mc).setModified(o, false);

            // It is also no longer a new item
            ((StateAwareMetaObject) mc).setNew(o, false);
        }
    }

    /**
     * Enhanced field parsing with better type handling and null safety
     */
    protected void parseField(Object o, MetaField f, ResultSet rs, int j) throws SQLException {
        if (f instanceof com.draagon.meta.field.BooleanField) {
            boolean bv = rs.getBoolean(j);
            f.setBoolean(o, rs.wasNull() ? null : bv);
        } else if (f instanceof com.draagon.meta.field.ByteField) {
            byte bv = rs.getByte(j);
            f.setByte(o, rs.wasNull() ? null : bv);
        } else if (f instanceof com.draagon.meta.field.ShortField) {
            short sv = rs.getShort(j);
            f.setShort(o, rs.wasNull() ? null : sv);
        } else if (f instanceof com.draagon.meta.field.IntegerField) {
            int iv = rs.getInt(j);
            f.setInt(o, rs.wasNull() ? null : iv);
        } else if (f instanceof com.draagon.meta.field.DateField) {
            Timestamp tv = rs.getTimestamp(j);
            f.setDate(o, rs.wasNull() ? null : new java.util.Date(tv.getTime()));
        } else if (f instanceof com.draagon.meta.field.LongField) {
            long lv = rs.getLong(j);
            f.setLong(o, rs.wasNull() ? null : lv);
        } else if (f instanceof com.draagon.meta.field.FloatField) {
            float fv = rs.getFloat(j);
            f.setFloat(o, rs.wasNull() ? null : fv);
        } else if (f instanceof com.draagon.meta.field.DoubleField) {
            double dv = rs.getDouble(j);
            f.setDouble(o, rs.wasNull() ? null : dv);
        } else if (f instanceof com.draagon.meta.field.StringField) {
            f.setString(o, rs.getString(j));
        } else if (f instanceof com.draagon.meta.field.ObjectField) {
            f.setObject(o, rs.getObject(j));
        } else {
            log.warn("Unknown field type {} for field {}", f.getClass().getSimpleName(), f.getName());
            f.setObject(o, rs.getObject(j));
        }
    }

    /**
     * Reset the objects to not be new or modified
     */
    protected void resetObjects(MetaObject mc, Collection<Object> objects) {

        if (!(mc instanceof StateAwareMetaObject)) {
            return;
        }

        // Reset all the objects
        for (Object o : objects) {
            resetObject(mc, o);
        }
    }

    /**
     * Reset the object to not be new or modified
     */
    protected void resetObject(MetaObject mc, Object o) {

        if (mc instanceof StateAwareMetaObject) {

            // It was pulled from the database, so it doesn't need to be flagged as modified
            ((StateAwareMetaObject) mc).setModified(o, false);

            // It is also no longer a new item
            ((StateAwareMetaObject) mc).setNew(o, false);
        }
    }

    /**
     * Gets the object by the id; throws exception if it did not exist
     */
    @Override
    public Object getObjectByRef(ObjectConnection c, String refStr) {
        ObjectRef ref = getObjectRef(refStr);
        MetaObject mc = ref.getMetaClass();

        if (!isReadableClass(mc)) {
            throw new PersistenceException("MetaClass [" + mc + "] is not readable");
        }

        ObjectMappingDB readMap = (ObjectMappingDB) getReadMapping(mc);

        Connection conn = (Connection) c.getDatastoreConnection();

        // Check for a valid transaction if enforced
        checkTransaction(conn, false);

        if (log.isDebugEnabled()) {
            log.debug("Loading object [" + mc + "] with reference [" + ref + "]");
        }

        try {

            // Create the Expression for the Primary Keys
            Expression exp = buildPrimaryKeyExpressionFromRef(mc, ref);

            // Create the QueryOptions and limit to the first 1
            QueryOptions qo = new QueryOptions();
            qo.setRange(1, 1);

            // Read the objects from the database driver
            Collection<Object> objects = getTypedDatabaseDriver().readMany(conn, mc, readMap, qo);

            // Reset the object persistence states
            resetObjects(mc, objects);

            // Return the object if found
            if (objects.size() > 0) {
                return objects.iterator().next();
            } else {
                throw new ObjectNotFoundException(refStr);
            }
        } catch (SQLException e) {
            //log.error( "Unable to load object [" + mc + "] with reference [" + ref + "]: " + e.getMessage(), e );
            throw new PersistenceException("Unable to load object [" + mc + "] with reference [" + ref + "]: " + e.getMessage(), e);
        }
    }

    /**
     * Delete the objects from the datastore where the field has the specified
     * value
     */
    @Override
    public int deleteObjects(ObjectConnection c, MetaObject mc, Expression exp) {

        if (!isDeleteableClass(mc)) {
            throw new PersistenceException("MetaClass [" + mc + "] is not deletable");
        }

        ObjectMappingDB mapping = (ObjectMappingDB) getDeleteMapping(mc);

        Connection conn = (Connection) c.getDatastoreConnection();

        // Check for a valid transaction if enforced
        checkTransaction(conn, true);

        if (log.isDebugEnabled()) {
            log.debug("Deleting Objects of Class [" + mc + "] where [" + exp + "]");
        }

        //int failures = 0;
        //while( true )
        //{
        try {
            return getTypedDatabaseDriver().deleteMany(conn, mc, mapping, exp);
        } catch (SQLException e) {
            //log.error( "Unable to delete objects of class [" + mc.getName() + "] with expression [" + exp + "]: " + e.getMessage() );
            //if ( ++failures > 5 )
            throw new PersistenceException("Unable to delete objects of class [" + mc.getName() + "] with expression [" + exp + "]: " + e.getMessage(), e);
        }

        // Sleep on a delete failure
        //try { Thread.sleep( 200 * failures ); }
        //catch (InterruptedException e) {}
        //}
    }

    /**
     * Gets the total count of objects with the specified search criteria
     */
    @Override
    public long getObjectsCount(ObjectConnection c, MetaObject mc, Expression exp) throws MetaDataException {
        if (!isReadableClass(mc)) {
            throw new PersistenceException("MetaClass [" + mc + "] is not persistable");
        }

        Connection conn = (Connection) c.getDatastoreConnection();

        ObjectMappingDB mapping = (ObjectMappingDB) getReadMapping(mc);

        // Check for a valid transaction if enforced
        checkTransaction(conn, false);

        try {
            // Read the objects
            return getTypedDatabaseDriver().getCount(conn, mc, mapping, exp);
        } catch (SQLException e) {
            throw new PersistenceException("Unable to get objects count of class [" + mc.getName() + "] with expression [" + exp + "]: " + e.getMessage(), e);
        }
    }

    /**
     * Gets the objects by the field with the specified search criteria
     */
    @Override
    public Collection<?> getObjects(ObjectConnection c, MetaObject mc, QueryOptions options) throws MetaDataException {
        if (!isReadableClass(mc)) {
            throw new PersistenceException("MetaClass [" + mc + "] is not persistable");
        }

        Connection conn = (Connection) c.getDatastoreConnection();

        ObjectMappingDB mapping = (ObjectMappingDB) getReadMapping(mc);

        // Check for a valid transaction if enforced
        checkTransaction(conn, false);

        //int failures = 0;
        //while( true )
        //{
        try {
            // Read the objects
            Collection<Object> objects = getTypedDatabaseDriver().readMany(conn, mc, mapping, options);

            // Reset the object persistence states
            resetObjects(mc, objects);

            // Return the objects
            return objects;
        } catch (SQLException e) {
            //log.error( "Unable to load objects of class [" + mc.getName() + "]: " + e.getMessage() );

            //if ( ++failures > 5 )
            throw new PersistenceException("Unable to get objects of class [" + mc.getName() + "] with options [" + options + "]: " + e.getMessage(), e);
        }

        // Sleep on a read failure
        //try { Thread.sleep( 200 * failures ); }
        //catch (InterruptedException e) {}
        //}
    }

    /**
     * Load the specified object from the database
     */
    public void loadObject(ObjectConnection c, Object o) throws MetaDataException {
        // Verify this object was loaded by the same object manager
        //verifyObjectManager( o );

        // Get the MetaClass for the object
        MetaObject mc = getMetaObjectFor(o);

        // If it's not a readable class throw an exception
        if (!isReadableClass(mc)) {
            throw new PersistenceException("MetaClass [" + mc + "] is not persistable");
        }

        // Get the read mapping
        ObjectMappingDB mapping = (ObjectMappingDB) getReadMapping(mc);

        // Get the connection
        Connection conn = (Connection) c.getDatastoreConnection();

        // Check for a valid transaction if enforced
        checkTransaction(conn, false);

        if (log.isDebugEnabled()) {
            log.debug("Loading object [" + o + "] of class [" + mc + "]");
        }

        // Create the Expression for the Primary Keys
        Expression exp = buildPrimaryKeyExpressionFromObject(mc, o);

        // Try to read the object
        try {
            // Read the object from the mapping
            boolean found = getTypedDatabaseDriver().read(conn, mc, mapping, o, exp);

            // If not found throw an exception
            if (!found) {
                throw new ObjectNotFoundException(o);
            }

            // Reset the object after it's loaded
            resetObject(mc, o);
        } catch (SQLException e) {
            //log.error( "Unable to load object [" + o + "]: " + e.getMessage(), e );
            throw new PersistenceException("Unable to load object [" + o + "]: " + e.getMessage(), e);
        }
    }

    /**
     * Add the specified object to the datastore
     */
    @Override
    public void createObject(ObjectConnection c, Object obj) throws PersistenceException {
        Connection conn = (Connection) c.getDatastoreConnection();

        // Check for a valid transaction if enforced
        checkTransaction(conn, true);

        MetaObject mc = getMetaObjectFor(obj);

        if (!isCreateableClass(mc)) {
            throw new PersistenceException("Object of class [" + mc + "] is not createable");
        }

        // Get the create mapping
        ObjectMappingDB mapping = (ObjectMappingDB) getCreateMapping(mc);

        //verifyObjectManager( obj );

        prePersistence(c, mc, obj, CREATE);

        if (log.isDebugEnabled()) {
            log.debug("Adding object [" + obj + "] of class [" + mc + "]");
        }

        try {
            if (!getTypedDatabaseDriver().create(conn, mc, mapping, obj)) {
                throw new PersistenceException("Now rows created for object [" + obj + "] of class [" + mc + "]");
            }

            postPersistence(c, mc, obj, CREATE);
        } catch (SQLException e) {
            //log.error( "Unable to add object of class [" + mc + "]: " + e.getMessage() );
            throw new PersistenceException("Unable to add object of class [" + mc + "]:" + e.getMessage(), e);
        }
    }

    /**
     * Update the specified object in the datastore
     */
    public void updateObject(ObjectConnection c, Object obj) throws PersistenceException {
        Connection conn = (Connection) c.getDatastoreConnection();

        // Check for a valid transaction if enforced
        checkTransaction(conn, true);

        // Get the metaclass and make sure it is updateable
        MetaObject mc = getMetaObjectFor(obj);
        if (!isUpdateableClass(mc)) {
            throw new PersistenceException("Object of class [" + mc + "] is not writeable");
        }

        // check the object manager
        //verifyObjectManager( obj );

        // Get the update mapping
        ObjectMappingDB mapping = (ObjectMappingDB) getUpdateMapping(mc);

        // Check whether there are dirty writes
        boolean allowsDirtyWrites = allowsDirtyWrites(mc);

        MetaField dirtyField = null;
        Object dirtyFieldValue = null;

        // If we don't allow dirty writes then get the field we're filtering from
        if (!allowsDirtyWrites) {
            dirtyField = getDirtyField(mc);
            dirtyFieldValue = dirtyField.getObject(obj);
        }

        if (log.isDebugEnabled()) {
            log.debug("Updating object [" + obj + "] of class [" + mc + "]");
        }

        // Run the pre-peristence methods
        prePersistence(c, mc, obj, UPDATE);

        // Get the modified fields
        Collection<MetaField> fields = mc.getMetaFields(); //mapping.getMetaFields();
        if (mc instanceof StateAwareMetaObject) {
            fields = getModifiedPersistableFields((StateAwareMetaObject) mc, fields, obj);
        }

        // If nothing needs to be persisted, then don't bother
        if (fields.size() == 0) {
            if (log.isDebugEnabled()) {
                log.debug("No need to update object of class [" + mc + "]");
            }
            return;
        }

        try {
            // Update the object
            if (!getTypedDatabaseDriver().update(conn, mc, mapping, obj, fields, getPrimaryKeys(mc), dirtyField, dirtyFieldValue)) {

                // If no dirty writes, see if that was the issue
                if (!allowsDirtyWrites) {

                    mapping = (ObjectMappingDB) getReadMapping(mc);

                    // Create the Expression for the Primary Keys
                    Expression exp = buildPrimaryKeyExpressionFromObject(mc, obj);

                    Collection<Object> results = getTypedDatabaseDriver().readMany(conn, mc, mapping, new QueryOptions(exp));
                    if (results.size() > 0) {
                        throw new DirtyWriteException(obj);
                    }
                }

                throw new ObjectNotFoundException(obj);
            }

            postPersistence(c, mc, obj, UPDATE);
        } catch (SQLException e) {
            //log.error( "Unable to update object of class [" + mc + "]: " + e.getMessage() );
            throw new PersistenceException("Unable to update object [" + obj + "] of class [" + mc + "]: " + e.getMessage(), e);
        }
    }

    /**
     * Delete the specified object from the datastore
     */
    public void deleteObject(ObjectConnection c, Object obj) throws PersistenceException {
        Connection conn = (Connection) c.getDatastoreConnection();

        // Check for a valid transaction if enforced
        checkTransaction(conn, true);

        MetaObject mc = getMetaObjectFor(obj);

        if (!isDeleteableClass(mc)) {
            throw new PersistenceException("Object [" + obj + "] of class [" + mc + "] is not deleteable");
        }

        //verifyObjectManager( obj );

        // Get the update mapping
        ObjectMappingDB mapping = (ObjectMappingDB) getDeleteMapping(mc);

        // Check whether there are dirty writes
        boolean allowsDirtyWrites = allowsDirtyWrites(mc);

        //MetaField dirtyField = null;
        //Object dirtyFieldValue = null;

        // If we don't allow dirty writes then get the field we're filtering from
        //if ( !allowsDirtyWrites ) {
        //dirtyField = getDirtyField( mc );
        //dirtyFieldValue = dirtyField.getObject( obj );
        //}

        prePersistence(c, mc, obj, DELETE);

        if (log.isDebugEnabled()) {
            log.debug("Deleting object [" + obj + "] of class [" + mc + "]");
        }

        try {
            boolean success = getTypedDatabaseDriver().delete(conn, mc, mapping, obj, getPrimaryKeys(mc));

            if (!success) {

                // If no dirty writes, see if that was the issue
                if (!allowsDirtyWrites) {

                    // Create the Expression for the Primary Keys
                    Expression exp = buildPrimaryKeyExpressionFromObject(mc, obj);

                    Collection<Object> results = getTypedDatabaseDriver().readMany(conn, mc, mapping, new QueryOptions(exp));
                    if (results.size() > 0) {
                        throw new DirtyWriteException(obj);
                    }
                }

                throw new ObjectNotFoundException(obj);
            }

            postPersistence(c, mc, obj, DELETE);
        } catch (SQLException e) {
            //log.error( "Unable to delete object of class [" + mc + "]: " + e.getMessage() );
            throw new PersistenceException("Unable to delete object [" + obj + "] of class [" + mc + "]: " + e.getMessage(), e);
        }
    }

    /*public boolean isAutoCreateTables() {
     return autoCreateTables;
     }

     public void setAutoCreateTables(boolean autoCreateTables) {
     this.autoCreateTables = autoCreateTables;
     }*/
    ///////////////////////////////////////////////////////
    // OBJECT QUERY LANGUAGE METHODS
    //
    //private final static String ROOT_CLASS_KEY = "*ROOT_CLASS_KEY*";

    /*protected Map<String,MetaClass> getMetaClassMap( String query ) throws MetaDataException
     {
     Map<String,MetaClass> m = new HashMap<String,MetaClass>();

     int i = -1;

     while( ( i = query.indexOf( '[', i + 1 )) > 0 )
     {
     int j = query.indexOf( ']', i );
     int k = query.indexOf( '=', i );
     if ( j <= 0 )
     throw new IllegalArgumentException( "Malformed OQL, missing closing '}': [" + query + "]" );

     if ( k >= 0 && k < j )
     {
     String cn = query.substring( i + 1, k );
     String var = query.substring( k + 1, j );
     m.put( var, MetaClass.forName( cn ));
     }
     else
     {
     String cn = query.substring( i + 1, j );
     m.put( ROOT_CLASS_KEY, MetaClass.forName( cn ));
     }

     i = j;
     }

     //ystem.out.println( "MAP: " + m );

     return m;
     }*/

    /* private String convertToSQL( String query, Map<String,MetaClass> m ) throws MetaDataException
     {
     //StringBuffer b = new StringBuffer();
     //ystem.out.println( "IN: " + query );

     int i = -1;

     // Replace tables
     while( ( i = query.indexOf( '{', i + 1 )) > 0 )
     {
     int j = query.indexOf( '}', i );
     if ( j <= 0 )
     throw new IllegalArgumentException( "Malformed OQL, missing closing '}': [" + query + "]" );

     String field = null;
     String var = null;
     MetaClass mc = null;

     int k = query.indexOf( '.', i );
     if ( k >= 0 && k < j )
     {
     var = query.substring( i + 1, k );
     field = query.substring( k + 1, j );

     mc = (MetaClass) m.get( var );

     if ( mc == null )
     throw new IllegalArgumentException( "Malformed OQL, unmapped metaclass variable '" + var + "': [" + query + "]" );

     var += ".";
     }
     else
     {
     field = query.substring( i + 1, j );
     mc = (MetaClass) m.get( ROOT_CLASS_KEY );
     var = "";

     if ( mc == null )
     throw new IllegalArgumentException( "Malformed OQL, no default metaclass defined: [" + query + "]" );
     }

     String colName = null;
     if ( field.equals( "*" ))
     colName = "*";
     else
     colName = getColumnName( mc.getMetaField( field ));

     query = query.substring( 0, i ) +
     var + colName +
     query.substring( j + 1 );
     }

     // Replace fields
     i = -1;
     while( ( i = query.indexOf( '[', i + 1 )) > 0 )
     {
     int j = query.indexOf( ']', i );
     if ( j <= 0 )
     throw new IllegalArgumentException( "Malformed OQL, missing closing '}': [" + query + "]" );

     String var = null;
     MetaClass mc = null;

     int k = query.indexOf( '=', i );
     if ( k >= 0 && k < j )
     {
     var = query.substring( k + 1, j );

     mc = (MetaClass) m.get( var );

     if ( mc == null )
     throw new IllegalArgumentException( "Malformed OQL, unmapped metaclass variable '" + var + "': [" + query + "]" );

     var = " " + var;
     }
     else
     {
     mc = (MetaClass) m.get( ROOT_CLASS_KEY );

     if ( mc == null )
     throw new IllegalArgumentException( "Malformed OQL, no default metaclass defined: [" + query + "]" );

     var = "";
     }

     query = query.substring( 0, i ) +
     getViewName( mc ) + var +
     query.substring( j + 1 );
     }

     //ystem.out.println( "OUT: " + query );

     //return b.toString();
     return query;
     }*/
    /**
     * Returns whether a MetaClass allows dirty writes
     */
    @Override
    public boolean allowsDirtyWrites(MetaObject mc) {
        if (!mc.hasMetaAttr(ALLOW_DIRTY_WRITE)
                || !("false".equals(mc.getMetaAttr(ALLOW_DIRTY_WRITE).getValue()))) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Gets the name of the dirty field of a metaclass
     */
    protected MetaField getDirtyField(MetaObject mc) {
        final String KEY = "getDirtyField()";

        MetaField field = (MetaField) mc.getCacheValue(KEY);

        if (field == null) {
            if (mc.hasMetaAttr(DIRTY_WRITE_CHECK_FIELD)) {
                field = mc.getMetaField(mc.getMetaAttr(DIRTY_WRITE_CHECK_FIELD).getValue().toString());
            } else {
                for (MetaField f : getAutoFields(mc)) {
                    if (AUTO_UPDATE.equals(f.getMetaAttr(AUTO).getValue())) {
                        field = f;
                        break;
                    }
                }
            }

            if (field == null) {
                throw new MetaDataException("No MetaField that is useable to prevent dirty writes was found");
            }

            mc.setCacheValue(KEY, field);
        }

        return field;
    }

    ///**
    // * Retrieves the value of the dirty field for an object
    // */
    //protected Object getDirtyFieldValue( MetaClass mc, Object obj ) {
    //	return getDirtyField( mc ).getObject( obj );
    //}
    //private Cache<String,String> mOQLCache = new Cache<String,String>( true, 900, 3600 );
    protected PreparedStatement getPreparedStatement(Connection c, String query, Collection<?> args) throws MetaDataException, SQLException {
        String sql = query; // (String) mOQLCache.get( query );

        // If it's not in the cache, then parse it and put it there
        //if ( sql == null )
        //{
        //Map<String,MetaClass> m = getMetaClassMap( query );

        //if ( m.size() > 0 ) {
        //	sql = convertToSQL( query, m );
        //}
        //else sql = query;

        //mOQLCache.put( query, sql );
        //}

        PreparedStatement s = c.prepareStatement(sql);

        if (args != null) {
            int i = 1;
            for (Object o : args) {
                if (o instanceof Boolean) {
                    s.setBoolean(i, (Boolean) o);
                } else if (o instanceof Byte) {
                    s.setByte(i, (Byte) o);
                } else if (o instanceof Short) {
                    s.setShort(i, (Short) o);
                } else if (o instanceof Integer) {
                    s.setInt(i, (Integer) o);
                } else if (o instanceof Long) {
                    s.setLong(i, (Long) o);
                } else if (o instanceof Float) {
                    s.setFloat(i, (Float) o);
                } else if (o instanceof Double) {
                    s.setDouble(i, (Double) o);
                } else if (o instanceof Date) {
                    s.setTimestamp(i, new Timestamp(((Date) o).getTime()));
                } else if (o == null) {
                    s.setString(i, null);
                } else {
                    s.setString(i, o.toString());
                }

                // Increment the i
                i++;
            }
        }

        return s;
    }

    @Override
    public int execute(ObjectConnection c, String query, Collection<?> arguments) throws MetaDataException {
        Connection conn = (Connection) c.getDatastoreConnection();

        // Check for a valid transaction if enforced
        checkTransaction(conn, true);

        try {
            PreparedStatement s = getPreparedStatement(conn, query, arguments);

            try {
                if (log.isDebugEnabled()) {
                    log.debug("SQL (" + conn.hashCode() + ") - execute: [" + query + " " + arguments + "]");
                }

                return s.executeUpdate();
            } finally {
                s.close();
            }
        } catch (SQLException e) {
            log.error("Unable to execute object query [" + query + "]: " + e.getMessage());
            throw new MetaDataException("Unable to execute object query [" + query + "]", e);
        }
    }

    protected MetaField getFieldForColumn(MetaObject resultClass, ObjectMapping mapping, String col) throws MetaDataException {
        // Generate a cache key
        //final String KEY = ( new StringBuilder( "getFieldForColumn(" ) ).append( col ).append( ")" ).toString();

        MetaField rc = null; //(MetaField) resultClass.getCacheValue( KEY );

        if (rc == null) {
            // First check against the read mapping
            //ObjectMapping om = getReadMapping( resultClass );
            if (mapping != null) {
                rc = mapping.getField(col);
            }

            // Next try to match by the metafield name
            if (rc == null) {
                try {
                    rc = resultClass.getMetaField(col);
                } catch (MetaDataNotFoundException e) {
                }
            }

            // Add it to the cache to speed things up
            //if ( rc != null ) resultClass.setCacheValue( KEY, rc );
        }

        return rc;
    }

    /**
     * Executes the specified query and maps it to the given object.
     *
     * String oql = "[" + Product.CLASSNAME + "]" + " SELECT {P.*}, {M.name} AS
     * manuName" + " FROM [" + Product.CLASSNAME + "=P]," + " [" +
     * Manufacturer.CLASSNAME + "=M]" + " WHERE {M.id}={P.manuId} AND {M.id} >
     * ?";
     *
     * String oql = "[{min:int,max:int,num:int}]" + " SELECT MIN({extra2}) AS
     * min, MAX({extra2}) AS max, COUNT(1) AS num" + " FROM [" +
     * Product.CLASSNAME + "]";
     */
    @Override
    public Collection<?> executeQuery(ObjectConnection c, String query, Collection<?> arguments) throws MetaDataException {
        Connection conn = (Connection) c.getDatastoreConnection();

        // Check for a valid transaction if enforced
        checkTransaction(conn, false);


        try {
            MetaObject resultClass = null;

            query = query.trim();
            /*if (query.startsWith("[{")) {
                int i = query.indexOf("}]");
                if (i <= 0) {
                    throw new MetaDataException("OQL does not contain a closing '}]': [" + query + "]");
                }

                String classTemplate = query.substring(2, i).trim();
                query = query.substring(i + 2).trim();
                String templateClassname = "draagon::meta::manager::db::OQL" + classTemplate.hashCode();

                // Get the result class, try it from the cache first
                resultClass = templateCache.get(templateClassname);
                if (resultClass == null) {
                    resultClass = ValueMetaObject.createFromTemplate(templateClassname, classTemplate);
                    templateCache.put(templateClassname, resultClass);
                }
            } else*/
            if (query.startsWith("[")) {
                int i = query.indexOf("]");
                if (i <= 0) {
                    throw new MetaDataException("OQL does not contain a closing ']': [" + query + "]");
                }

                String className = query.substring(1, i).trim();
                query = query.substring(i + 1).trim();

                resultClass = com.draagon.meta.loader.MetaDataRegistry.findMetaObjectByName(className);
            } else {
                throw new MetaDataException("OQL does not contain a result set definition using []'s or {}'s: [" + query + "]");
            }

            PreparedStatement s = getPreparedStatement(conn, query, arguments);

            try {
                if (log.isDebugEnabled()) {
                    log.debug("SQL (" + conn.hashCode() + ") - executeQuery: [" + query + " " + arguments + "]");
                }

                ResultSet rs = s.executeQuery();

                LinkedList<Object> data = new LinkedList<Object>();
                try {
                    ObjectMappingDB mapping = (ObjectMappingDB) getReadMapping(resultClass);

                    while (rs.next()) {
                        Object o = resultClass.newInstance();

                        for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                            String col = rs.getMetaData().getColumnName(i);

                            MetaField mf = getFieldForColumn(resultClass, mapping, col);

                            if (mf != null) {
                                parseField(o, mf, rs, i);
                            }
                        }

                        data.add(o);
                    }

                    return data;
                } finally {
                    rs.close();
                }
            } finally {
                s.close();
            }
        } catch (SQLException e) {
            log.error("Unable to execute object query [" + query + " (" + arguments + ")]: " + e.getMessage());
            throw new MetaDataException("Unable to execute object query [" + query + " (" + arguments + ")]: " + e.getMessage(), e);
        }
    }

    ///////////////////////////////////////////////////////
    // BULK OPERATIONS OPTIMIZATION
    //
    
    /**
     * Enhanced bulk object creation using database-specific batch operations
     */
    @Override
    public void createObjectsBulk(ObjectConnection c, MetaObject mc, Collection<Object> objects) throws MetaDataException {
        if (!isCreateableClass(mc)) {
            throw new PersistenceException("Object of class [" + mc + "] is not createable");
        }
        
        Connection conn = (Connection) c.getDatastoreConnection();
        checkTransaction(conn, true);
        
        ObjectMappingDB mapping = (ObjectMappingDB) getCreateMapping(mc);
        
        try {
            // Use database driver for bulk creation if supported
            if (getDatabaseDriver() instanceof BulkOperationSupport bulkDriver) {
                bulkDriver.createBulk(conn, mc, mapping, objects);
            } else {
                // Fallback to individual creates with better batching
                createObjectsBatchFallback(conn, mc, mapping, objects);
            }
        } catch (SQLException e) {
            throw new PersistenceException("Unable to bulk create objects of class [" + mc + "]: " + e.getMessage(), e);
        }
    }
    
    /**
     * Enhanced bulk object updates using database-specific batch operations
     */
    @Override
    public void updateObjectsBulk(ObjectConnection c, MetaObject mc, Collection<Object> objects) throws MetaDataException {
        if (!isUpdateableClass(mc)) {
            throw new PersistenceException("Object of class [" + mc + "] is not updateable");
        }
        
        Connection conn = (Connection) c.getDatastoreConnection();
        checkTransaction(conn, true);
        
        ObjectMappingDB mapping = (ObjectMappingDB) getUpdateMapping(mc);
        
        try {
            // Use database driver for bulk updates if supported
            if (getDatabaseDriver() instanceof BulkOperationSupport bulkDriver) {
                bulkDriver.updateBulk(conn, mc, mapping, objects);
            } else {
                // Fallback to individual updates
                for (Object obj : objects) {
                    updateObject(c, obj);
                }
            }
        } catch (SQLException e) {
            throw new PersistenceException("Unable to bulk update objects of class [" + mc + "]: " + e.getMessage(), e);
        }
    }
    
    /**
     * Batch fallback for create operations when bulk support is not available
     */
    private void createObjectsBatchFallback(Connection conn, MetaObject mc, ObjectMappingDB mapping, Collection<Object> objects) throws SQLException {
        // Disable auto-commit for better performance
        boolean originalAutoCommit = conn.getAutoCommit();
        if (originalAutoCommit) {
            conn.setAutoCommit(false);
        }
        
        try {
            int batchCount = 0;
            for (Object obj : objects) {
                prePersistence(null, mc, obj, CREATE);
                
                if (!getTypedDatabaseDriver().create(conn, mc, mapping, obj)) {
                    throw new PersistenceException("No rows created for object [" + obj + "] of class [" + mc + "]");
                }
                
                postPersistence(null, mc, obj, CREATE);
                
                // Commit in batches of 1000 for memory management
                if (++batchCount % 1000 == 0) {
                    conn.commit();
                }
            }
            
            // Commit any remaining
            if (batchCount % 1000 != 0) {
                conn.commit();
            }
        } finally {
            // Restore original auto-commit
            if (originalAutoCommit) {
                conn.setAutoCommit(true);
            }
        }
    }

    ///////////////////////////////////////////////////////
    // UTILITY METHODS FOR EXPRESSION BUILDING

    /**
     * Build an Expression chain for primary keys with values from an object reference
     * @param mc MetaObject to get primary keys from
     * @param ref ObjectRef containing the key values
     * @return Expression chain combining all primary key conditions
     * @throws PersistenceException if MetaObject has no primary keys
     */
    protected Expression buildPrimaryKeyExpressionFromRef(MetaObject mc, ObjectRef ref) throws PersistenceException {
        Expression exp = null;
        int i = 0;
        
        for (MetaField mf : getPrimaryKeys(mc)) {
            Expression e = new Expression(mf.getName(), ref.getIds()[i]);
            if (exp == null) {
                exp = e;
            } else {
                exp = exp.and(e);
            }
            i++;
        }
        
        if (exp == null) {
            throw new PersistenceException("MetaObject [" + mc + "] has no primary keys");
        }
        
        return exp;
    }

    /**
     * Build an Expression chain for primary keys with values from an object
     * @param mc MetaClass to get primary keys from
     * @param obj Object containing the key values
     * @return Expression chain combining all primary key conditions
     * @throws PersistenceException if MetaObject has no primary keys
     */
    protected Expression buildPrimaryKeyExpressionFromObject(MetaObject mc, Object obj) throws PersistenceException {
        Expression exp = null;
        
        for (MetaField mf : getPrimaryKeys(mc)) {
            Expression e = new Expression(mf.getName(), mf.getObject(obj));
            if (exp == null) {
                exp = e;
            } else {
                exp = exp.and(e);
            }
        }
        
        if (exp == null) {
            throw new PersistenceException("MetaObject [" + mc + "] has no primary keys");
        }
        
        return exp;
    }

    ///////////////////////////////////////////////////////
    // TO STRING METHOD
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName())
                .append("[")
                .append(getMappingHandler().getClass().getSimpleName())
                .append("][")
                .append(getDatabaseDriver().getClass().getSimpleName())
                .append("]");
        return sb.toString();
    }
}
