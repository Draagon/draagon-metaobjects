/*
 * Copyright 2001 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */

package com.draagon.meta.manager.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

import com.draagon.meta.object.MetaObject;
import com.draagon.meta.field.MetaField;
import com.draagon.meta.manager.QueryOptions;
import com.draagon.meta.manager.db.defs.ForeignKeyDef;
import com.draagon.meta.manager.db.defs.IndexDef;
import com.draagon.meta.manager.db.defs.SequenceDef;
import com.draagon.meta.manager.db.defs.TableDef;
import com.draagon.meta.manager.db.defs.ViewDef;
import com.draagon.meta.manager.exp.Expression;

/**
 * A Database Driver has specific methods to handle database specific SQL
 * calls.
 */
public interface DatabaseDriver
{
  //public final static int AUTO_NONE    = ObjectManager.AUTO_NONE;
  //public final static int AUTO_PRIOR   = ObjectManager.AUTO_PRIOR;
  //public final static int AUTO_POST    = ObjectManager.AUTO_POST;

  public void setManager( ObjectManagerDB man );

  /**
   * Checks for the existence of the table 
   * @param c	Database connection to use
   * @param table	Table Definition	
   * @return Whether the table exists
   * @throws SQLException If an invalid table structure exists in the DB
   */
  public boolean checkTable( Connection c, TableDef table ) throws SQLException;

  /**
   * Checks for the existence of the view in the database
   * @param c	Database connection to use
   * @param view	View Definition	
   * @return Whether the view exists
   * @throws SQLException If an invalid view structure exists in the DB
   */
  public boolean checkView( Connection c, ViewDef view ) throws SQLException;
  
  /**
   * Creates a table in the database
   */
  public void createTable( Connection c, TableDef tableDef ) throws SQLException;

  /**
   * Deletes a table from the database
   */
  public void deleteTable( Connection c, TableDef tableDef ) throws SQLException;

  /**
   * Creates a view in the database
   */
  public void createView(Connection c, ViewDef viewDef ) throws SQLException;

  /**
   * Creates the sequence in the database
   */
  public void createSequence( Connection c, SequenceDef sequenceDef ) throws SQLException;

  /**
   * Creates the index in the database
   */
  public void createIndex( Connection c, IndexDef indexDef ) throws SQLException;

  /**
   * Creates the foreign key in the database
   */
  public void createForeignKey( Connection c, ForeignKeyDef keyDef ) throws SQLException;

  /**
   * Creates the specified object
   * @param c SQLConnection to use
   * @param mc MetaClass for the object to create
   * @param mapping ObjectMappingDB to use for the create
   * @param o Object to create
   * @return True if the object was created
   */
  public boolean create( Connection c, MetaObject mc, ObjectMappingDB mapping, Object o ) throws SQLException;

  /**
   * Reads the object using the specified Mapping for the specified MetaClass
   * @param c SQLConnection to use
   * @param mc MetaClass that will be read
   * @param mapping ObjectMappingDB to use for the reading
   * @param o The object to read
   * @param exp Expression to filter on when reading
   * @return True if the object was read, false if it was not
   */
  public boolean read( Connection c, MetaObject mc, ObjectMappingDB mapping, Object o, Expression exp ) throws SQLException;

  /**
   * Updates the provided object
   * @param c SQLConnection to use
   * @param mc The MetaClass for the object being updates
   * @param mapping The mapping to use for the update
   * @param o The object to update
   * @param fields A list of specific fields to update (if null, updates all fields)
   * @param keys The keys to use for the expression
   * @param dirtyField The dirty field to use for optimistic locking (if not null)
   * @param dirtyValue The value to use for the dirty field
   */
  public boolean update( Connection c, MetaObject mc, ObjectMappingDB mapping, Object o, Collection<MetaField> fields, Collection<MetaField> keys, MetaField dirtyField, Object dirtyValue ) throws SQLException;

  /**
   * Deletes the provided object
   * @param c SQLConnection to use
   * @param mc The MetaClass for the object being updates
   * @param mapping The mapping to use for the update
   * @param o The object to update
   * @param fields A list of specific fields to update (if null, updates all fields)
   * @param keys The keys to use for the expression
   */
  public boolean delete( Connection c, MetaObject mc, ObjectMappingDB mapping, Object o, Collection<MetaField> keys ) throws SQLException;
  
  /**
   * Gets the total count based on the options provided
   * @param c SQLConnection to use
   * @param mc MetaClass that will be read
   * @param mapping ObjectMappingDB to use for the counting
   * @param exp Expression to use when counting
   * @return The total count of objects
   */
  public long getCount( Connection c, MetaObject mc, ObjectMappingDB mapping, Expression exp ) throws SQLException;
  
  /**
   * Reads data using the specified Mapping for the specified MetaClass
   * @param c SQLConnection to use
   * @param mc MetaClass that will be read
   * @param mapping ObjectMappingDB to use for the reading
   * @param options QueryOptions to use when reading
   * @return Collection of Object instances based on the MetaClass provided
   */
  public Collection<Object> readMany( Connection c, MetaObject mc, ObjectMappingDB mapping, QueryOptions options ) throws SQLException;
  
  /**
   * Updates multiple objects with the specified values
   * @param c SQLConnection to use
   * @param mc The MetaClass representing the objects to update
   * @param mapping The mapping to use for the update call
   * @param values The values to update on the objects
   * @param exp The expression filtering which objects to update
   */
  public int updateMany( Connection c, MetaObject mc, ObjectMappingDB mapping, Map<MetaField,Object> values, Expression exp ) throws SQLException;

  /**
   * Deletes multiple objects with the specified values
   * @param c SQLConnection to use
   * @param mc The MetaClass representing the object to delete
   * @param mapping The mapping to use for the delete
   * @param exp The expression to filter which objects to delete
   */
  public int deleteMany( Connection c, MetaObject mc, ObjectMappingDB mapping, Expression exp ) throws SQLException;
  
  
  ///**
  // * Returns whether to get the automatic field id before or after the creation
  // */
  //public int getAutoType();

  ///**
  // * Gets the string to append to the INSERT call to request the generated ids
  // */
  //public String getInsertAppendString( MetaClass mc );

  ///**
  // * Used if AUTO_DURING to retrieve the ids for the specified class
  // */
  //public void getAutoIdentifiers( PreparedStatement s, MetaClass mc, Object o ) throws SQLException;

  ///**
  // * Gets the next sequence for a given MetaClass
  // */
  //public String getNextAutoId( Connection c, ColumnDef def ) throws SQLException;

  ///**
  // * Gets the next sequence for a given MetaClass
  // */
  //public String getLastAutoId( Connection c, ColumnDef def ) throws SQLException;
  
  /**
   * Gets the date format string
   */
  public String getDateFormat();

  ///**
  // * The SQL query to append to a SQL SELECT to lock the returned rows
  // */
  //public String getLockString();
}

