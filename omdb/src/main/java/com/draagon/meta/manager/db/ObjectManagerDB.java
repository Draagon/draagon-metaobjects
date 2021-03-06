/*
 * Copyright 2003 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta.manager.db;

import com.draagon.meta.field.MetaField;
import com.draagon.meta.field.MetaFieldNotFoundException;
import com.draagon.meta.manager.StateAwareMetaObject;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.*;
import com.draagon.meta.object.value.ValueMetaObject;
import com.draagon.meta.manager.*;
import com.draagon.meta.manager.db.driver.*;
//import com.draagon.util.InitializationException;
//import com.draagon.cache.Cache;
import com.draagon.meta.manager.exp.Expression;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
public class ObjectManagerDB extends ObjectManager {

    private static Log log = LogFactory.getLog(ObjectManagerDB.class);
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
    public void setEnforceTransaction(boolean enforce) {
        enforceTransaction = enforce;
    }

    /**
     * Returns whether to enforce transactions
     */
    public boolean enforceTransaction() {
        return enforceTransaction;
    }

    /**
     * Checks to see if a transaction exists or not
     */
    protected void checkTransaction(Connection c, boolean throwEx) throws MetaException {
        try {
            if (enforceTransaction() && c.getAutoCommit()) {
                MetaException me = new MetaException("The connection retrieved is not operating under a transaction and transactions are being enforced");
                if (throwEx) {
                    throw me;
                } else {
                    log.warn(me.getMessage(), me);
                }
            }
        } catch (SQLException e) {
            throw new MetaException("Error checking connection for transaction enforcement: " + e.getMessage(), e);
        }
    }

    ///////////////////////////////////////////////////////
    // CONNECTION HANDLING METHODS
    //
    /**
     * Retrieves a connection object representing the datastore
     */
    public ObjectConnection getConnection() {
        DataSource ds = getDataSource();
        if (ds == null) {
            throw new IllegalArgumentException("No DataSource was specified for this ObjectManager, cannot request connection");
        }

        Connection c;
        try {
            c = ds.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException("Could not retrieve a connection from the datasource: " + e.getMessage(), e);
        }

        return new ObjectConnectionDB(c);
    }

    /**
     * Release the Database Connection
     */
    public void releaseConnection(ObjectConnection oc)
            throws MetaException {
        oc.close();
    }

    /**
     * Sets the Data Source to use for database connections
     */
    public void setDataSource(DataSource ds) {
        mSource = ds;
    }

    /**
     * Retrieves the data source
     */
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
    public void setDriverClass(String className) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        Class<?> c = Class.forName(className);
        setDatabaseDriver((DatabaseDriver) c.newInstance());
    }

    public void setDatabaseDriver(DatabaseDriver dd) {
        mDriver = dd;
        mDriver.setManager(this);
    }

    public synchronized DatabaseDriver getDatabaseDriver() {
        if (mDriver == null) {
            mDriver = new GenericSQLDriver();
            mDriver.setManager(this);
        }

        return mDriver;
    }

    ///////////////////////////////////////////////////////
    // PERSISTENCE METHODS
    //
    public MappingHandler getDefaultMappingHandler() {
        return new SimpleMappingHandlerDB();
    }

    public void setMappingHandler(MappingHandler handler) {
        mMappingHandler = handler;
    }

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
     throws MetaException
     {
     ArrayList values = new ArrayList();

     String tmp = ref;

     // Split apart the id field
     for ( Iterator i = keys.iterator(); i.hasNext(); )
     {
     MetaField f = (MetaField) i.next();

     if ( tmp == null || tmp.length() == 0 )
     throw new MetaException( "Invalid Reference [" + ref + "] for MetaClass [" + mc + "]" );

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
     throws MetaException, SQLException
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
    public void parseObject(ResultSet rs, Collection<MetaField> fields, MetaObject mc, Object o) throws SQLException, MetaException {
        if (!isReadableClass(mc)) {
            throw new MetaException("MetaClass [" + mc + "] is not readable");
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

    protected void parseField(Object o, MetaField f, ResultSet rs, int j) throws SQLException {
        switch (f.getType()) {
            case MetaField.BOOLEAN: {
                boolean bv = rs.getBoolean(j);
                if (rs.wasNull()) {
                    f.setBoolean(o, null);
                } else {
                    f.setBoolean(o, new Boolean(bv));
                }
            }
            break;

            case MetaField.BYTE: {
                byte bv = rs.getByte(j);
                if (rs.wasNull()) {
                    f.setByte(o, null);
                } else {
                    f.setByte(o, new Byte(bv));
                }
            }
            break;

            case MetaField.SHORT: {
                short sv = rs.getShort(j);
                if (rs.wasNull()) {
                    f.setShort(o, null);
                } else {
                    f.setShort(o, new Short(sv));
                }
            }
            break;

            case MetaField.INT: {
                int iv = rs.getInt(j);
                if (rs.wasNull()) {
                    f.setInt(o, null);
                } else {
                    f.setInt(o, new Integer(iv));
                }
            }
            break;


            case MetaField.DATE: {
                Timestamp tv = rs.getTimestamp(j);
                if (rs.wasNull()) {
                    f.setDate(o, null);
                } else {
                    f.setDate(o, new java.util.Date(tv.getTime()));
                }
            }
            break;

            case MetaField.LONG: {
                long lv = rs.getLong(j);
                if (rs.wasNull()) {
                    f.setLong(o, null);
                } else {
                    f.setLong(o, new Long(lv));
                }
            }
            break;

            case MetaField.FLOAT: {
                float fv = rs.getFloat(j);
                if (rs.wasNull()) {
                    f.setFloat(o, null);
                } else {
                    f.setFloat(o, new Float(fv));
                }
            }
            break;

            case MetaField.DOUBLE: {
                double dv = rs.getDouble(j);
                if (rs.wasNull()) {
                    f.setDouble(o, null);
                } else {
                    f.setDouble(o, new Double(dv));
                }
            }
            break;

            case MetaField.STRING:
                f.setString(o, rs.getString(j));
                break;

            case MetaField.OBJECT:
                f.setObject(o, rs.getObject(j));
                break;
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
            Expression exp = null;
            int i = 0;
            for (MetaField mf : getPrimaryKeys(mc)) {
                Expression e = new Expression(mf.getName(), ref.getIds()[ i]);
                if (exp == null) {
                    exp = e;
                } else {
                    exp = exp.and(e);
                }
                i++;
            }

            if (exp == null) {
                throw new PersistenceException("MetaClass [" + mc + "] has no primary keys get object by reference [" + ref + "]");
            }

            // Create the QueryOptions and limit to the first 1
            QueryOptions qo = new QueryOptions();
            qo.setRange(1, 1);

            // Read the objects from the database driver
            Collection<Object> objects = getDatabaseDriver().readMany(conn, mc, readMap, qo);

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
            return getDatabaseDriver().deleteMany(conn, mc, mapping, exp);
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
    public long getObjectsCount(ObjectConnection c, MetaObject mc, Expression exp) throws MetaException {
        if (!isReadableClass(mc)) {
            throw new PersistenceException("MetaClass [" + mc + "] is not persistable");
        }

        Connection conn = (Connection) c.getDatastoreConnection();

        ObjectMappingDB mapping = (ObjectMappingDB) getReadMapping(mc);

        // Check for a valid transaction if enforced
        checkTransaction(conn, false);

        try {
            // Read the objects
            return getDatabaseDriver().getCount(conn, mc, mapping, exp);
        } catch (SQLException e) {
            throw new PersistenceException("Unable to get objects count of class [" + mc.getName() + "] with expression [" + exp + "]: " + e.getMessage(), e);
        }
    }

    /**
     * Gets the objects by the field with the specified search criteria
     */
    @Override
    public Collection<?> getObjects(ObjectConnection c, MetaObject mc, QueryOptions options) throws MetaException {
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
            Collection<Object> objects = getDatabaseDriver().readMany(conn, mc, mapping, options);

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
    public void loadObject(ObjectConnection c, Object o) throws MetaException {
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
        Expression exp = null;
        for (MetaField mf : getPrimaryKeys(mc)) {
            Expression e = new Expression(mf.getName(), mf.getObject(o));
            if (exp == null) {
                exp = e;
            } else {
                exp = exp.and(e);
            }
        }

        if (exp == null) {
            throw new PersistenceException("MetaClass [" + mc + "] has no primary keys defined to load object [" + o + "]");
        }

        // Try to read the object
        try {
            // Read the object from the mapping
            boolean found = getDatabaseDriver().read(conn, mc, mapping, o, exp);

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
            if (!getDatabaseDriver().create(conn, mc, mapping, obj)) {
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
            if (!getDatabaseDriver().update(conn, mc, mapping, obj, fields, getPrimaryKeys(mc), dirtyField, dirtyFieldValue)) {

                // If no dirty writes, see if that was the issue
                if (!allowsDirtyWrites) {

                    mapping = (ObjectMappingDB) getReadMapping(mc);

                    // Create the Expression for the Primary Keys
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
                        throw new PersistenceException("MetaClass [" + mc + "] has no primary keys defined to update object [" + obj + "]");
                    }

                    Collection<Object> results = getDatabaseDriver().readMany(conn, mc, mapping, new QueryOptions(exp));
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
            boolean success = getDatabaseDriver().delete(conn, mc, mapping, obj, getPrimaryKeys(mc));

            if (!success) {

                // If no dirty writes, see if that was the issue
                if (!allowsDirtyWrites) {

                    // Create the Expression for the Primary Keys
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
                        throw new PersistenceException("MetaClass [" + mc + "] has no primary keys defined to delete object [" + obj + "]");
                    }

                    Collection<Object> results = getDatabaseDriver().readMany(conn, mc, mapping, new QueryOptions(exp));
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

    /*protected Map<String,MetaClass> getMetaClassMap( String query ) throws MetaException
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

    /* private String convertToSQL( String query, Map<String,MetaClass> m ) throws MetaException
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
    public boolean allowsDirtyWrites(MetaObject mc) {
        if (!mc.hasAttribute(ALLOW_DIRTY_WRITE)
                || !("false".equals(mc.getAttribute(ALLOW_DIRTY_WRITE)))) {
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
            if (mc.hasAttribute(DIRTY_WRITE_CHECK_FIELD)) {
                field = mc.getMetaField(mc.getAttribute(DIRTY_WRITE_CHECK_FIELD).toString());
            } else {
                for (MetaField f : getAutoFields(mc)) {
                    if (AUTO_UPDATE.equals(f.getAttribute(AUTO))) {
                        field = f;
                        break;
                    }
                }
            }

            if (field == null) {
                throw new MetaException("No MetaField that is useable to prevent dirty writes was found");
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
    protected PreparedStatement getPreparedStatement(Connection c, String query, Collection<?> args) throws MetaException, SQLException {
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

    public int execute(ObjectConnection c, String query, Collection<?> arguments) throws MetaException {
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
            throw new MetaException("Unable to execute object query [" + query + "]", e);
        }
    }

    protected MetaField getFieldForColumn(MetaObject resultClass, ObjectMapping mapping, String col) throws MetaException {
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
                } catch (MetaFieldNotFoundException e) {
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
    public Collection<?> executeQuery(ObjectConnection c, String query, Collection<?> arguments) throws MetaException {
        Connection conn = (Connection) c.getDatastoreConnection();

        // Check for a valid transaction if enforced
        checkTransaction(conn, false);


        try {
            MetaObject resultClass = null;

            query = query.trim();
            /*if (query.startsWith("[{")) {
                int i = query.indexOf("}]");
                if (i <= 0) {
                    throw new MetaException("OQL does not contain a closing '}]': [" + query + "]");
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
                    throw new MetaException("OQL does not contain a closing ']': [" + query + "]");
                }

                String className = query.substring(1, i).trim();
                query = query.substring(i + 1).trim();

                resultClass = MetaObject.forName(className);
            } else {
                throw new MetaException("OQL does not contain a result set definition using []'s or {}'s: [" + query + "]");
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
            throw new MetaException("Unable to execute object query [" + query + " (" + arguments + ")]: " + e.getMessage(), e);
        }
    }

    ///////////////////////////////////////////////////////
    // TO STRING METHOD
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
