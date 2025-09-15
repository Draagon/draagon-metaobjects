/*
 * Copyright 2003 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */

package com.draagon.meta.manager;

//import com.draagon.meta.manager.db.ObjectMapping;
//import com.draagon.meta.manager.db.MappingHandler;

import com.draagon.meta.MetaData;
import com.draagon.meta.MetaDataException;
import com.draagon.meta.attr.MetaAttributeNotFoundException;
import com.draagon.meta.field.MetaField;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.loader.MetaDataRegistry;
import com.draagon.meta.manager.exp.*;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.object.MetaObjectNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;
//import javax.servlet.*;


/**
 * The Object Manager Base is able to add, update, delete,
 * and retrieve objects of those types from a datastore.
 */
public abstract class ObjectManager
{
	private static final Logger log = LoggerFactory.getLogger(ObjectManager.class);

	public final static String IS_KEY       = "isKey";
	public final static String IS_READONLY  = "isReadOnly";
        
	public final static String AUTO         = "auto";
	public final static String AUTO_CREATE  = "create";
	public final static String AUTO_UPDATE  = "update";

	public final static int CREATE = 1;
	public final static int UPDATE = 2;
	public final static int DELETE = 3;

	public static final int AUTO_NONE    =  0;
	public static final int AUTO_PRIOR   =  1;
	public static final int AUTO_DURING  =  2;
	public static final int AUTO_POST    =  3;

	//private MappingHandler mMappingHandler = null;
	
	// Enhanced caching and performance
	private final Map<String, List<MetaField>> autoFieldsCache = new ConcurrentHashMap<>();
	private final Map<String, Collection<MetaField>> primaryKeysCache = new ConcurrentHashMap<>();
	private final Map<String, Collection<MetaField>> modifiedFieldsCache = new ConcurrentHashMap<>();
	
	// Event system
	private final List<PersistenceEventListener> eventListeners = new ArrayList<>();
	
	// Async executor
	private Executor asyncExecutor = ForkJoinPool.commonPool();

	public ObjectManager()
	{
	}
	
	/**
	 * Sets the executor for asynchronous operations
	 */
	public void setAsyncExecutor(Executor executor) {
		this.asyncExecutor = Objects.requireNonNull(executor, "Executor cannot be null");
	}
	
	/**
	 * Adds a persistence event listener
	 */
	public void addPersistenceEventListener(PersistenceEventListener listener) {
		synchronized (eventListeners) {
			eventListeners.add(Objects.requireNonNull(listener, "Listener cannot be null"));
		}
	}
	
	/**
	 * Removes a persistence event listener
	 */
	public void removePersistenceEventListener(PersistenceEventListener listener) {
		synchronized (eventListeners) {
			eventListeners.remove(listener);
		}
	}

	///////////////////////////////////////////////////////
	// MANAGEMENT METHODS
	//

	/**
	 * Initializes the ObjectManager
	 */
	public void init()
	throws Exception
	{
		log.info( "Object Manager " + toString() + " initialized" );
	}

	/**
	 * Destroys the Object Manager
	 */
	public void destroy()
	{
		log.info( "Object Manager " + toString() + " destroyed" );
	}


	///////////////////////////////////////////////////////
	// CONNECTION HANDLING METHODS
	//

	/**
	 * Retrieves a connection object representing the datastore
	 */
	public abstract ObjectConnection getConnection() throws MetaDataException;

	public abstract void releaseConnection( ObjectConnection oc ) throws MetaDataException;

	///////////////////////////////////////////////////////
	// ABSTRACT PERSISTENCE METHODS
	//

	/** Is this a createable class */        
	public abstract boolean isCreateableClass( MetaObject mc );

	/** Is this a readable class */
	public abstract boolean isReadableClass( MetaObject mc );

	/** Gets the update mapping to the DB */
	public abstract boolean isUpdateableClass( MetaObject mc );

	/** Gets the delete mapping to the DB */
	public abstract boolean isDeleteableClass( MetaObject mc );
        
	protected String getPersistenceAttribute( MetaData md, String name )
	{
		try {
			if ( md.hasMetaAttr( name ))
				return (String) md.getMetaAttr( name ).getValue();
		} catch ( MetaAttributeNotFoundException e ) {
			throw new RuntimeException( "[" + md + "] had attribute [" + name + "], but threw exception reading it", e );
		}
		return null;
	}

	protected boolean isReadOnly( MetaData md )
	{
		try {
			if ( "true".equals( md.getMetaAttr( IS_READONLY ).getValue())) return true;
		} catch( MetaAttributeNotFoundException e ) {}
		return false;
	}

	/**
	 * Gets an objects reference which can be used to later load the object
	 */
	public ObjectRef getObjectRef( Object obj )
	{
		MetaObject mc = MetaDataRegistry.findMetaObject( obj );

		Collection<MetaField> keys = getPrimaryKeys( mc );
		if ( keys.size() == 0 )
			throw new IllegalArgumentException( "MetaClass [" + mc + "] has no primary keys, so no object reference is available" );

		String [] ids = new String[ keys.size() ];
		int j = 0;
		for( Iterator<MetaField> i = keys.iterator(); i.hasNext(); j++ )
		{
			MetaField f = i.next();
			ids[ j ] = f.getString( obj );
		}

		return new ObjectRef( mc, ids );
	}

	/**
	 * Gets an object reference which can be used to later load the object, based on the string reference
	 */
	public ObjectRef getObjectRef( String refStr ) {
		return new ObjectRef( refStr );
	}

	/**
	 * Gets the object by the id; throws exception if it did not exist
	 */
	public abstract Object getObjectByRef( ObjectConnection c, String refStr ) throws MetaDataException;

	///////////////////////////////////////////////////////
	// OPTIONAL-BASED APIs FOR NULL-SAFE ACCESS
	//

	/**
	 * Finds an object by reference, returning Optional to handle null cases safely
	 * @param c Object connection
	 * @param refStr Reference string
	 * @return Optional containing the object, or empty if not found
	 */
	public Optional<Object> findObjectByRef(ObjectConnection c, String refStr) {
		try {
			Object obj = getObjectByRef(c, refStr);
			return Optional.ofNullable(obj);
		} catch (ObjectNotFoundException e) {
			return Optional.empty();
		} catch (MetaDataException e) {
			// Re-throw other exceptions as they indicate real errors
			throw new RuntimeException("Error finding object by reference: " + refStr, e);
		}
	}

	/**
	 * Finds an object by reference using automatic connection management
	 * @param refStr Reference string
	 * @return Optional containing the object, or empty if not found
	 */
	public Optional<Object> findObjectByRef(String refStr) {
		try (ObjectConnection connection = getConnection()) {
			return findObjectByRef(connection, refStr);
		} catch (Exception e) {
			throw new RuntimeException("Error finding object by reference: " + refStr, e);
		}
	}

	/**
	 * Finds an object asynchronously by reference
	 * @param refStr Reference string
	 * @return CompletableFuture containing Optional of the object
	 */
	public CompletableFuture<Optional<Object>> findObjectByRefAsync(String refStr) {
		return CompletableFuture.supplyAsync(() -> findObjectByRef(refStr), asyncExecutor);
	}

	/**
	 * Finds the first object matching the given expression
	 * @param mc MetaObject to search
	 * @param expression Query expression
	 * @return Optional containing the first matching object, or empty if none found
	 */
	public Optional<Object> findFirst(MetaObject mc, Expression expression) {
		try (ObjectConnection connection = getConnection()) {
			QueryOptions options = new QueryOptions(expression);
			options.setRange(new Range(1, 1));
			Collection<?> results = getObjects(connection, mc, options);
			return results.isEmpty() ? Optional.empty() : Optional.of(results.iterator().next());
		} catch (Exception e) {
			throw new RuntimeException("Error finding first object", e);
		}
	}

	/**
	 * Finds the first object matching the given field and value
	 * @param mc MetaObject to search
	 * @param fieldName Field name to match
	 * @param value Value to match
	 * @return Optional containing the first matching object, or empty if none found
	 */
	public Optional<Object> findFirst(MetaObject mc, String fieldName, Object value) {
		return findFirst(mc, new Expression(fieldName, value));
	}

	/**
	 * Asynchronously finds the first object matching the given expression
	 * @param mc MetaObject to search
	 * @param expression Query expression
	 * @return CompletableFuture containing Optional of the first matching object
	 */
	public CompletableFuture<Optional<Object>> findFirstAsync(MetaObject mc, Expression expression) {
		return CompletableFuture.supplyAsync(() -> findFirst(mc, expression), asyncExecutor);
	}

	/**
	 * Load the specified object from the datastore
	 */
	public abstract void loadObject( ObjectConnection c, Object obj ) throws MetaDataException;

	/**
	 * Add the specified object to the datastore
	 */
	public abstract void createObject( ObjectConnection c, Object obj ) throws MetaDataException;

	/**
	 * Update the specified object in the datastore
	 */
	public abstract void updateObject( ObjectConnection c, Object obj ) throws MetaDataException;

	/**
	 * Delete the specified object from the datastore
	 */
	public abstract void deleteObject( ObjectConnection c, Object obj ) throws MetaDataException;

	///////////////////////////////////////////////////////
	// DEFAULT IMPEMENTATIONS
	// May be overridden for enhanced performance

	/**
	 * Gets auto fields for a MetaObject with improved caching
	 */
	protected List<MetaField> getAutoFields(MetaObject mc) {
		String cacheKey = mc.getName() + ":autoFields";
		return autoFieldsCache.computeIfAbsent(cacheKey, key -> 
			mc.getMetaFields().stream()
				.filter(f -> f.hasMetaAttr(AUTO))
				.collect(Collectors.toList())
		);
	}

	protected void handleAutoFields( ObjectConnection c, MetaObject mc, Object obj, int state, int action ) throws MetaDataException
	{
		for( MetaField f : getAutoFields( mc ))
		{
			Object auto = f.getMetaAttr( AUTO ).getValue();
			handleAutoField( c, mc, f, obj, auto, state, action );
		}
	}

	/**
	 * Handles auto field processing with improved logic
	 */
	public void handleAutoField(ObjectConnection c, MetaObject mc, MetaField mf, Object obj, Object auto, int state, int action) throws MetaDataException {
		if (state == AUTO_PRIOR) {
			long currentTime = System.currentTimeMillis();
			
			if (AUTO_CREATE.equals(auto) && action == CREATE) {
				mf.setLong(obj, currentTime);
			} else if (AUTO_UPDATE.equals(auto)) {
				mf.setLong(obj, currentTime);
			}
		}
	}

	public void prePersistence(ObjectConnection c, MetaObject mc, Object obj, int action) throws MetaDataException {
		handleAutoFields(c, mc, obj, AUTO_PRIOR, action);
		
		// Fire pre-persistence events
		synchronized (eventListeners) {
			try {
				switch (action) {
					case CREATE -> eventListeners.forEach(listener -> listener.onBeforeCreate(mc, obj));
					case UPDATE -> {
						Collection<MetaField> modifiedFields = getModifiedFields(mc, obj);
						eventListeners.forEach(listener -> listener.onBeforeUpdate(mc, obj, modifiedFields));
					}
					case DELETE -> eventListeners.forEach(listener -> listener.onBeforeDelete(mc, obj));
				}
			} catch (Exception e) {
				log.warn("Error firing pre-persistence events", e);
			}
		}
	}

	public void postPersistence(ObjectConnection c, MetaObject mc, Object obj, int action) throws MetaDataException {
		handleAutoFields(c, mc, obj, AUTO_POST, action);

		// Update state for StateAwareMetaObject
		if (mc instanceof StateAwareMetaObject stateAware) {
			switch (action) {
				case CREATE, UPDATE -> {
					stateAware.setNew(obj, false);
					stateAware.setModified(obj, false);
					stateAware.setDeleted(obj, false);
				}
				case DELETE -> stateAware.setDeleted(obj, true);
			}
		}
		
		// Fire post-persistence events
		synchronized (eventListeners) {
			try {
				switch (action) {
					case CREATE -> eventListeners.forEach(listener -> listener.onAfterCreate(mc, obj));
					case UPDATE -> {
						Collection<MetaField> modifiedFields = getModifiedFields(mc, obj);
						eventListeners.forEach(listener -> listener.onAfterUpdate(mc, obj, modifiedFields));
					}
					case DELETE -> eventListeners.forEach(listener -> listener.onAfterDelete(mc, obj));
				}
			} catch (Exception e) {
				log.warn("Error firing post-persistence events", e);
			}
		}
	}
	
	/**
	 * Gets modified fields for state-aware objects
	 */
	private Collection<MetaField> getModifiedFields(MetaObject mc, Object obj) {
		if (mc instanceof StateAwareMetaObject stateAware) {
			return mc.getMetaFields().stream()
				.filter(field -> stateAware.isFieldModified(field, obj))
				.collect(Collectors.toList());
		}
		return Collections.emptyList();
	}

	/**
	 * Determines whether the MetaField is a key
	 */
	public boolean isPrimaryKey( MetaField mf )
	{
		try {
			if ( "true".equals( mf.getMetaAttr( IS_KEY ).getValue())) return true;
		} catch( MetaAttributeNotFoundException e ) {}
		return false;
	}

	/**
	 * Retrieves a new object of the specified class
	 */
	public Object getNewObject( MetaObject mc ) throws PersistenceException
	{
		Object o = mc.newInstance();
		attachManager( o );
		return o;
	}

	/**
	 * Attaches the object to the specified meta manager
	 */
	public void attachManager( Object obj ) 
	{
		MetaObject mc = getMetaObjectFor( obj );                
		if ( mc instanceof ManagerAwareMetaObject ) {
			((ManagerAwareMetaObject) mc ).attachManager( this, obj );
                }
	}

	/**
	 * Verifies the object belongs to this ObjectManager
	 */
	protected void verifyObjectManager( Object obj ) throws PersistenceException
	{
		if ( obj == null ) throw new IllegalArgumentException( "Cannot persist a null object" );
		
		MetaObject mc = getMetaObjectFor( obj );
		if ( !( mc instanceof ManagerAwareMetaObject )) return;

		ManagerAwareMetaObject mmc = (ManagerAwareMetaObject) mc;

		ObjectManager mm = mmc.getManager( obj );

		// WARNING:  Do we care?!
		// Verify that it is the same object manager
		if ( mm != null && !mm.equals( this ))
			throw new PersistenceException( "This object is attached to a different Object Manager [" + mm + "]" );

		// If not attached, then attach it
		if ( mm == null ) mmc.attachManager( this, obj );
	}

	/**
	 * Retrieves the fields of a MetaObject which are primary keys with improved caching
	 */
	public Collection<MetaField> getPrimaryKeys(MetaObject mc) {
		String cacheKey = mc.getName() + ":primaryKeys";
		return primaryKeysCache.computeIfAbsent(cacheKey, key ->
			mc.getMetaFields().stream()
				.filter(this::isPrimaryKey)
				.collect(Collectors.toList())
		);
	}

	/**
	 * Get all objects of the specified kind from the datastore
	 */
	/*public Collection getObjects( ObjectConnection c, MetaClass mc, Collection fields )
      throws MetaException
    {
      return getObjects( c, mc, fields, new QueryOptions() );
    }*/

	/**
	 * Stores the specified object by adding, updating, or deleting
	 */
	public void storeObject( ObjectConnection c, Object obj ) throws PersistenceException
	{
		StateAwareMetaObject pmc = getStatefulMetaClass( obj );

		if ( pmc.isNew( obj ) ) createObject( c, obj );
		else if ( pmc.isModified( obj ) ) updateObject( c, obj );
		else if ( pmc.isDeleted( obj ) ) deleteObject( c, obj );
	}

	/**
	 * Retrieves a persistable metaclass from the given object
	 */
	protected StateAwareMetaObject getStatefulMetaClass( Object obj ) 
	{
		MetaObject mc = getMetaObjectFor( obj );
		if ( !( mc instanceof StateAwareMetaObject )) return null;

		return (StateAwareMetaObject) mc;
	}

	/**
	 * Gets the object by the reference; throws exception if it did not exist
	 */
	/*public Object getObjectByRef( ObjectConnection c, String refStr )
    throws MetaException
  {
        ObjectRef ref = getObjectRef( refStr );
        Collection fields = getReadableFields( ref.getMetaClass() );
        return getObjectByRef( c, fields, refStr );
  }*/

	/** Gets the total count of objects */
	public long getObjectsCount( ObjectConnection c, MetaObject mc ) throws MetaDataException
	{
		return getObjectsCount( c, mc, null );
	}
	
	/** Gets the total count of objects with the specified options */
	public long getObjectsCount( ObjectConnection c, MetaObject mc, Expression exp ) throws MetaDataException
	{
		Collection<MetaField> fields = new ArrayList<MetaField>();
		fields.add( getPrimaryKeys(mc).iterator().next() );
		QueryOptions options = new QueryOptions( exp );
		options.setFields( fields );
		return getObjects( c, mc, new QueryOptions( exp )).size();
	}
	
	/**
	 * Get all objects of the specified kind from the datastore
	 */
	public Collection<?> getObjects( ObjectConnection c, MetaObject mc ) throws MetaDataException
	{
		// Collection fields = getReadableFields( mc );
		return getObjects( c, mc, new QueryOptions() );
	}

	/**
	 * Get all objects of the specified kind from the datastore
	 */
	public abstract Collection<?> getObjects( ObjectConnection c, MetaObject mc, QueryOptions options ) throws MetaDataException;
	
	///////////////////////////////////////////////////////
	// ASYNCHRONOUS OPERATIONS
	//
	
	/**
	 * Asynchronously retrieves objects from the datastore
	 */
	public CompletableFuture<Collection<?>> getObjectsAsync(MetaObject mc) {
		return getObjectsAsync(mc, new QueryOptions());
	}
	
	/**
	 * Asynchronously retrieves objects from the datastore with options
	 */
	public CompletableFuture<Collection<?>> getObjectsAsync(MetaObject mc, QueryOptions options) {
		return CompletableFuture.supplyAsync(() -> {
			try (var connection = getConnection()) {
				return getObjects(connection, mc, options);
			} catch (Exception e) {
				throw new RuntimeException("Error retrieving objects asynchronously", e);
			}
		}, asyncExecutor);
	}
	
	/**
	 * Asynchronously creates an object
	 */
	public CompletableFuture<Void> createObjectAsync(Object obj) {
		return CompletableFuture.runAsync(() -> {
			try (var connection = getConnection()) {
				createObject(connection, obj);
			} catch (Exception e) {
				MetaObject mc = getMetaObjectFor(obj);
				fireErrorEvent(mc, obj, "create", e);
				throw new RuntimeException("Error creating object asynchronously", e);
			}
		}, asyncExecutor);
	}
	
	/**
	 * Asynchronously updates an object
	 */
	public CompletableFuture<Void> updateObjectAsync(Object obj) {
		return CompletableFuture.runAsync(() -> {
			try (var connection = getConnection()) {
				updateObject(connection, obj);
			} catch (Exception e) {
				MetaObject mc = getMetaObjectFor(obj);
				fireErrorEvent(mc, obj, "update", e);
				throw new RuntimeException("Error updating object asynchronously", e);
			}
		}, asyncExecutor);
	}
	
	/**
	 * Asynchronously deletes an object
	 */
	public CompletableFuture<Void> deleteObjectAsync(Object obj) {
		return CompletableFuture.runAsync(() -> {
			try (var connection = getConnection()) {
				deleteObject(connection, obj);
			} catch (Exception e) {
				MetaObject mc = getMetaObjectFor(obj);
				fireErrorEvent(mc, obj, "delete", e);
				throw new RuntimeException("Error deleting object asynchronously", e);
			}
		}, asyncExecutor);
	}
	
	/**
	 * Batch creates multiple objects asynchronously
	 */
	public CompletableFuture<Void> createObjectsAsync(Collection<?> objects) {
		return CompletableFuture.runAsync(() -> {
			try (var connection = getConnection()) {
				createObjects(connection, objects);
			} catch (Exception e) {
				throw new RuntimeException("Error creating objects in batch asynchronously", e);
			}
		}, asyncExecutor);
	}
	
	/**
	 * Fires error events to listeners
	 */
	private void fireErrorEvent(MetaObject mc, Object obj, String operation, Exception error) {
		synchronized (eventListeners) {
			try {
				eventListeners.forEach(listener -> listener.onError(mc, obj, operation, error));
			} catch (Exception e) {
				log.warn("Error firing error events", e);
			}
		}
	}
	
	///////////////////////////////////////////////////////
	// QUERY BUILDER PATTERN
	//
	
	/**
	 * Creates a query builder for the specified MetaObject
	 */
	public QueryBuilder query(MetaObject metaObject) {
		return new QueryBuilder(this, metaObject);
	}
	
	/**
	 * Creates a query builder for the specified class name
	 */
	public QueryBuilder query(String className) throws MetaDataException {
		try {
			MetaObject metaObject = MetaDataRegistry.findMetaObjectByName(className);
			return new QueryBuilder(this, metaObject);
		} catch (Exception e) {
			throw new MetaDataException("Could not find MetaObject for class: " + className, e);
		}
	}
	/*  {
        Collection fields = null;

        if ( options.getWriteableOnly() )
          fields = getWriteableFields( mc );
        else
          fields = getReadableFields( mc );

        return getObjects( c, mc, fields, options );
    }*/

	/**
	 * Get all objects of the specified kind from the datastore
	 */
	/*public Collection getObjects( ObjectConnection c, MetaClass mc, QueryOptions options )
    throws MetaException
    {
        Collection results = getObjects( c, mc, fields );

        Expression exp = options.getExpression();
        SortOrder sort = options.getSortOrder();
        Range range = options.getRange();

        if ( exp != null )
            results = filterObjects( results, exp );

        if ( sort != null )
            results = sortObjects( results, sort );

        if ( range != null )
            results = clipObjects( results, range );

        if( options.isDistinct() )
            results = distinctObjects( results );

        return results;
    }*/

	/**
	 * Loads the specified object from the datastore
	 */
	/*public void loadObject( ObjectConnection c, Object obj )
    throws MetaException
  {
        Collection fields = getReadableFields( getClassForObject( obj ));
        loadObject( c, fields, obj );
  }*/

	/**
	 * Loads the specified fields for the objects from the datastore
	 */
	/*public void loadObjects( ObjectConnection c, Collection fields, Collection objs )
    throws MetaException
  {
    for( Iterator i = objs.iterator(); i.hasNext(); )
      loadObject( c, fields, i.next() );
  }*/

	/**
	 * Loads the specified objects from the datastore
	 */
	public void loadObjects( ObjectConnection c, Collection<?> objs )
	throws MetaDataException
	{
		for( Iterator<?> i = objs.iterator(); i.hasNext(); )
		{
			Object obj = i.next();
			//Collection fields = getReadableFields( getClassForObject( obj ));
			loadObject( c, obj );
		}
	}

	/**
	 * Add the specified objects to the datastore with enhanced bulk processing
	 */
	public void createObjects(ObjectConnection c, Collection<?> objs) throws MetaDataException {
		if (objs == null || objs.isEmpty()) {
			return;
		}
		
		log.debug("Creating {} objects in bulk", objs.size());
		
		// Group objects by MetaObject type for better performance
		var objectsByType = objs.stream()
			.collect(Collectors.groupingBy(obj -> getMetaObjectFor(obj)));
		
		for (var entry : objectsByType.entrySet()) {
			MetaObject mc = entry.getKey();
			@SuppressWarnings("unchecked")
			List<Object> objectsOfType = (List<Object>) entry.getValue();
			
			// Fire bulk pre-create events
			synchronized (eventListeners) {
				for (Object obj : objectsOfType) {
					eventListeners.forEach(listener -> listener.onBeforeCreate(mc, obj));
				}
			}
			
			// Process bulk creation (can be overridden in implementations for true bulk operations)
			createObjectsBulk(c, mc, objectsOfType);
			
			// Fire bulk post-create events
			synchronized (eventListeners) {
				for (Object obj : objectsOfType) {
					eventListeners.forEach(listener -> listener.onAfterCreate(mc, obj));
				}
			}
		}
	}
	
	/**
	 * Template method for bulk object creation - can be overridden for database-specific bulk operations
	 */
	protected void createObjectsBulk(ObjectConnection c, MetaObject mc, Collection<Object> objects) throws MetaDataException {
		// Default implementation - individual creates
		for (Object obj : objects) {
			createObject(c, obj);
		}
	}

	/**
	 * Update the specified objects in the datastore with enhanced bulk processing
	 */
	public void updateObjects(ObjectConnection c, Collection<?> objs) throws MetaDataException {
		if (objs == null || objs.isEmpty()) {
			return;
		}
		
		log.debug("Updating {} objects in bulk", objs.size());
		
		// Group objects by MetaObject type for better performance
		var objectsByType = objs.stream()
			.collect(Collectors.groupingBy(obj -> getMetaObjectFor(obj)));
		
		for (var entry : objectsByType.entrySet()) {
			MetaObject mc = entry.getKey();
			@SuppressWarnings("unchecked")
			List<Object> objectsOfType = (List<Object>) entry.getValue();
			
			// Filter to only modified objects if state-aware
			List<Object> modifiedObjects = objectsOfType;
			if (mc instanceof StateAwareMetaObject stateAware) {
				modifiedObjects = objectsOfType.stream()
					.filter(obj -> stateAware.isModified(obj))
					.collect(Collectors.toList());
			}
			
			if (modifiedObjects.isEmpty()) {
				log.debug("No modified objects found for {}", mc.getName());
				continue;
			}
			
			// Process bulk update (can be overridden in implementations)
			updateObjectsBulk(c, mc, modifiedObjects);
		}
	}
	
	/**
	 * Template method for bulk object updates - can be overridden for database-specific bulk operations
	 */
	protected void updateObjectsBulk(ObjectConnection c, MetaObject mc, Collection<Object> objects) throws MetaDataException {
		// Default implementation - individual updates
		for (Object obj : objects) {
			updateObject(c, obj);
		}
	}

	/**
	 * Delete the specified object from the datastore
	 */
	public void deleteObjectByRef( ObjectConnection c, String ref )
	throws MetaDataException
	{
		deleteObject( c, getObjectByRef( c, ref ));
	}

	/**
	 * Delete the specified objects from the datastore
	 */
	public int deleteObjects( ObjectConnection c, Collection<?> objs )
	throws MetaDataException
	{
		int j = 0;
		for( Iterator<?> i = objs.iterator(); i.hasNext(); ) {
			deleteObject( c, i.next() );
			j++;
		}
		return j;
	}

	/**
	 * Delete the objects from the datastore where the field has the specified value
	 */
	public int deleteObjects( ObjectConnection c, MetaObject mc, Expression exp )
	throws MetaDataException
	{
		return deleteObjects( c, getObjects( c, mc, new QueryOptions( exp )));
	}

	/**
	 * Stores all objecs in the Collection by adding or updating
	 */
	public void storeObjects( ObjectConnection c, Collection<?> objs )
	throws MetaDataException
	{
		for( Iterator<?> i = objs.iterator(); i.hasNext(); )
			storeObject( c, i.next() );
	}


	///////////////////////////////////////////////////////
	// HELPER METHODS
	// May be overridden for enhanced performance

	/**
	 * Sorts the specified objects by the provided sort order
	 */
	@SuppressWarnings("unchecked")
	public static Collection<Object> sortObjects( Collection<Object> objs, SortOrder sort )
	throws MetaDataException
	{
		if ( objs.size() == 0 ||
				sort.getOrder() == SortOrder.NONE ) return objs;

		ArrayList<Object> a = new ArrayList<Object>();
		a.addAll( 0, objs );

		ObjectComparator comp = new ObjectComparator( sort );

		Collections.sort( a, comp );

		return a;
	}


	/**
	 * Retrieves the fields of a MetaClass which are persistable
	 *  and have been modified.
	 */
	protected Collection<MetaField> getModifiedPersistableFields( StateAwareMetaObject smc, Collection<MetaField> fields, Object o ) 
	{
		List<MetaField> dirtyFields = new ArrayList<MetaField>();
		// Iterate each field on the object that is updateable and see if anything has changed
		for( MetaField f : fields ) {
			if ( smc.isFieldModified( f, o )) {
				dirtyFields.add( f );
			}
		}

		return dirtyFields;
	}

	/**
	 * Gets the SQL WHERE clause for the fields of a class
	 */
	private static boolean getExpressionResult( MetaObject mc, Expression exp, Object obj )
	throws MetaDataException
	{
		if ( exp instanceof ExpressionGroup )
		{
			return getExpressionResult( mc, ((ExpressionGroup) exp ).getGroup(), obj );
		}
		else if ( exp instanceof ExpressionOperator )
		{
			ExpressionOperator oper = (ExpressionOperator) exp;

			boolean a = getExpressionResult( mc, oper.getExpressionA(), obj );
			boolean b = getExpressionResult( mc, oper.getExpressionB(), obj );

			boolean rc = false;
			if ( oper.getOperator() == ExpressionOperator.AND )
				rc = a & b;
			else
				rc = a | b;

			//ystem.out.println( "[" + oper.getExpressionA() + "](" + a + ") {" + oper.getOperator() + "} [" + oper.getExpressionB() + "](" + b + ") = " + rc );

			return rc;
		}
		else if ( exp.isSpecial() )
		{
			throw new MetaDataException( "Unsupported Special Expression [" + exp + "]" );
		}
		else
		{
			MetaField f = mc.getMetaField( exp.getField() );

			Object val = f.getObject( obj );
			Object val2 = exp.getValue();
			
			int condition = exp.getCondition();

			if ( condition == Expression.EQUAL
					|| condition == Expression.NOT_EQUAL
					|| condition == Expression.GREATER
					|| condition == Expression.LESSER
					|| condition == Expression.EQUAL_GREATER
					|| condition == Expression.EQUAL_LESSER
			)
			{
				return compareValue( val, val2, condition );
			}
			else if ( condition == Expression.CONTAIN
					|| condition == Expression.NOT_CONTAIN
					|| condition == Expression.START_WITH
					|| condition == Expression.NOT_START_WITH
					|| condition == Expression.END_WITH
					|| condition == Expression.NOT_END_WITH
					|| condition == Expression.EQUALS_IGNORE_CASE
			)
			{
				String s1 = (val==null)?null:val.toString();
				String s2 = (val2==null)?null:val2.toString();

				if ( s1 == null || s2 == null ) return false;

				boolean rc = compareString( s1.toLowerCase(), s2.toLowerCase(), condition );
				//ystem.out.println( "[" + s1 + "] (" + condition + ") [" + s2 + "] = " + rc );
				return rc;
			}
			else
				throw new MetaDataException( "Unsupported Expression Condition (" + Expression.condStr(condition) + ") on Expression [" + exp + "]" );
		}
	}

	protected static boolean compareString( String s1, String s2, int condition )
	{
		switch( condition )
		{
		case Expression.CONTAIN:
			if ( s1.indexOf( s2 ) >= 0 ) return true;
			return false;

		case Expression.NOT_CONTAIN:
			if ( s1.indexOf( s2 ) < 0 ) return true;
			return false;

		case Expression.START_WITH:
			return s1.startsWith( s2 );

		case Expression.NOT_START_WITH:
			return !s1.startsWith( s2 );

		case Expression.END_WITH:
			return s1.endsWith( s2 );

		case Expression.NOT_END_WITH:
			return !s1.endsWith( s2 );
			
		case Expression.EQUALS_IGNORE_CASE:
			return s1.equalsIgnoreCase( s2 );

		default:
			throw new IllegalStateException( "compareString with unsupported condition type (" + Expression.condStr(condition) + ")" );
		}
	}

	protected static boolean compareValue( Object val, Object val2, int condition )
	{
		// If it's a collection, then iterate the whole array
		if ( val2 instanceof Collection ) {
			
			// Has to be EQUAL or NOT EQUAL
			if ( condition !=  Expression.EQUAL && condition != Expression.NOT_EQUAL ) {
				throw new IllegalArgumentException( "Can only compare with EQUAL or NOT EQUAL against a collection!" );
			}
			
			// Iterate each value in the collection
			for( Object v : ((Collection<?>)val2)) {
				
				// Get the resulting condition
				boolean ret = compareValue( val, v, condition );
				
				// return true if there are any matches
				if ( condition == Expression.EQUAL && ret ) return true;
				
				// if looking for not equal, return false when finding the first one
				else if ( condition == Expression.NOT_EQUAL && !ret ) return false;
			}
			
			// Return true or false based on the condition 
			if ( condition == Expression.EQUAL ) return false;
			else if ( condition == Expression.NOT_EQUAL ) return true;
			else throw new IllegalStateException( "This has to be EQUAL or NOT EQUAL" );
		}
		
		int result = 0;
		if ( val == null || val2 == null )
		{
			if ( val == null && val2 == null ) result = 0;
			else if ( val == null ) result = -1;
			else result = 1;
		}
		else if ( val instanceof Boolean )
		{
			if ( ((Boolean) val ).equals( val2 )) result = 0;
			else result = 1;
		}
		else if ( val instanceof Byte )
		{
			Byte v = (val2 instanceof Byte)?(Byte)val2:Byte.valueOf( val2.toString() );
			result = ((Byte) val ).compareTo( v );
		}
		else if ( val instanceof Short )
		{
			Short v = (val2 instanceof Short)?(Short)val2:Short.valueOf( val2.toString() );
			result = ((Short) val ).compareTo( v );
		}
		else if ( val instanceof Integer )
		{
			Integer v = (val2 instanceof Integer)?(Integer)val2:Integer.valueOf( val2.toString() );
			result = ((Integer) val ).compareTo( v );
		}
		else if ( val instanceof Long )
		{
			Long v = (val2 instanceof Long)?(Long)val2:Long.valueOf( val2.toString() );
			result = ((Long) val ).compareTo( v );
		}
		else if ( val instanceof Float )
		{
			Float v = (val2 instanceof Float)?(Float)val2:Float.valueOf( val2.toString() );
			result = ((Float) val ).compareTo( v );
		}
		else if ( val instanceof Double )
		{
			Double v = (val2 instanceof Double)?(Double)val2:Double.valueOf( val2.toString() );
			result = ((Double) val ).compareTo( v );
		}
		else if ( val instanceof Date )
		{
			Date v = (val2 instanceof Date)?(Date)val2:new Date( Long.parseLong( val2.toString() ));
			result = ((Date) val ).compareTo( v );
		}
		else
		{
			result = val.toString().toLowerCase().compareTo( val2.toString().toLowerCase() );
		}

		switch( condition )
		{
		case Expression.EQUAL:
			if ( result == 0 ) return true;
			else return false;
		case Expression.GREATER:
			if ( result > 0 ) return true;
			else return false;
		case Expression.LESSER:
			if ( result < 0 ) return true;
			else return false;
		case Expression.EQUAL_GREATER:
			if ( result >= 0 ) return true;
			else return false;
		case Expression.EQUAL_LESSER:
			if ( result <= 0 ) return true;
			else return false;
		case Expression.NOT_EQUAL:
			if ( result != 0 ) return true;
			else return false;
		default:
			throw new IllegalArgumentException( "Invalid expression condition [" + Expression.condStr(condition) + "]" );
		}
	}


	/**
	 * Filters the specified objects by the provided expression
	 */
	public static Collection<Object> filterObjects( Collection<Object> objs, Expression exp )
	throws MetaDataException
	{
		ArrayList<Object> a = new ArrayList<Object>();

		// Check each object for validity
		for( Object o : objs )
		{
			MetaObject mc = MetaDataRegistry.findMetaObject( o );

			if ( getExpressionResult( mc, exp, o ))
			{
				//ystem.out.println( "ADDING: " + o );
				a.add( o );
			}
		}

		return a;
	}

	/**
	 * Clips the specified objects by the provided range
	 */
	public static Collection<Object> clipObjects( Collection<Object> objs, Range range )
	throws MetaDataException
	{
		ArrayList<Object> a = new ArrayList<Object>();
		int j = 1;
		for( Object o : objs )
		{
			if ( j >= range.getStart() && j <= range.getEnd() )
				a.add( o );
		}

		return a;
	}

	/**
	 * Clips the specified objects by the provided range
	 */
	public static Collection<Object> distinctObjects( Collection<?> objs )
	throws MetaDataException
	{
		// TODO:  Check this, as I don't think it works!

		ArrayList<Object> a = new ArrayList<Object>( objs );
		for( Iterator<Object> i = a.iterator(); i.hasNext(); )
		{
			Object o = i.next();

			// Find the first index of the object
			int fi = a.indexOf( o );

			// Remove all objects of the same value
			int li = 0;
			while (( li = a.lastIndexOf( o )) != fi )
				a.remove( li );
		}

		return a;
	}


	/**
	 * Attempts to retrieve the MetaClass for a specified object,
	 *  and throws an exception if one does not exist
	 */
	public MetaObject getMetaObjectFor( Object o ) throws MetaObjectNotFoundException
	{
		MetaObject mc = MetaDataRegistry.findMetaObject( o );
		if ( mc == null )
			throw new MetaObjectNotFoundException( "No MetaClass exists for object [" + o + "]", o );

		return mc;
	}

	///////////////////////////////////////////////////////
	// OBJECT QUERY LANGUAGE METHODS
	//

	public abstract int execute( ObjectConnection c, String query, Collection<?> arguments ) throws MetaDataException;

	public abstract Collection<?> executeQuery( ObjectConnection c, String query, Collection<?> arguments ) throws MetaDataException;


	///////////////////////////////////////////////////////
	// TO STRING METHOD
	public String toString()
	{
		return "Unknown";
	}
}
