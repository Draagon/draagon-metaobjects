package com.draagon.meta.manager.db.validator;

import com.draagon.meta.MetaException;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.loader.MetaDataRegistry;
import com.draagon.meta.manager.ObjectConnection;
import com.draagon.meta.manager.db.*;
import com.draagon.meta.manager.db.defs.*;
import com.draagon.meta.object.MetaObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MetaClassDBValidatorService
{
  private static final Logger log = LoggerFactory.getLogger(MetaClassDBValidatorService.class);

  //private static final long WAIT_TIME = 60000;

  private ObjectManagerDB mObjectManager = null;

  private List<BaseDef> createdDefs = null;
  private List<BaseDef> checkedDefs = new ArrayList<BaseDef>();

  private boolean autoCreate = false;
  private boolean shouldWait = false;

  public void setAutoCreate( boolean auto ) {
    autoCreate = auto;
  }

  public boolean shouldAutoCreate()
  {
	return autoCreate;
  }

  public void setShouldWait( boolean wait ) {
    shouldWait = wait;
  }

  public boolean shouldWait()
  {
    return shouldWait;
  }

  public void init() throws Exception
  {
	if ( getObjectManager() == null )
		throw new IllegalStateException( "No ObjectManagerDB was specified!" );
	
    ObjectConnection oc = getObjectManager().getConnection();

    log.info( "VALIDATION - START" );

    try {
      Connection c = (Connection) oc.getDatastoreConnection();

      // This will hold any tables that were created
      createdDefs = new ArrayList<BaseDef>();

      MappingHandler mh = getObjectManager().getMappingHandler();
      DatabaseDriver dd = getObjectManager().getDatabaseDriver();
      
      // Validate all Writeable (TABLE) MetaClasses
      for( MetaDataLoader loader : MetaDataRegistry.getDataLoaders() )
      {
    	// Verify the Mutable Mappings
        for( MetaObject mc : loader.getMetaObjects() )            
        {        	
          verifyMapping( c, mc, dd, mh.getCreateMapping( mc ));
          verifyMapping( c, mc, dd, mh.getUpdateMapping( mc ));
          verifyMapping( c, mc, dd, mh.getDeleteMapping( mc ));
        }
        
        // Verify the Read-only Mappings (This could be views)
        for( MetaObject mc : loader.getMetaObjects() )
        {        	
          verifyMapping( c, mc, dd, mh.getReadMapping( mc ));
        }
      }

      // Create foreign keys for newly created tables
      for( BaseDef def : createdDefs )
      {
    	  if ( def instanceof TableDef ) {
    		  
    		  TableDef table = (TableDef) def;

	          log.info( "VALIDATION - CREATING SEQUENCES FOR TABLE " + def );
	    	  createSequences( c, dd, table );
    		  
	          log.info( "VALIDATION - CREATING INDEXES FOR TABLE " + def );
	    	  createIndexes( c, dd, table );
	    	  
	    	  log.info( "VALIDATION - CREATING FOREIGN KEYS FOR TABLE " + def );
	    	  createForeignKeys( c, dd, table );
    	  }
      }

      log.info( "VALIDATION - COMPLETE" );
    }
    //catch( Exception e ) {
    //  throw new InitializationException( "Error validating meta classes: " + e.getMessage(), e );
    //}
    finally {
      getObjectManager().releaseConnection( oc );
    }
  }

  private void createSequences( Connection c, DatabaseDriver dd, TableDef table ) throws SQLException {

	  for( ColumnDef colDef : table.getColumns() ) {		  

		  if ( colDef.getSequence() != null ) {
			  dd.createSequence( c, colDef.getSequence() );
		  }
	  }
  }

  private void createIndexes( Connection c, DatabaseDriver dd, TableDef table ) throws SQLException {

	  for( IndexDef indexDef : table.getIndexes() ) {		  
		  
		  // TODO: Check if it already exists		  
		  
		  dd.createIndex( c, indexDef );
	  }
  }

  private void createForeignKeys( Connection c, DatabaseDriver dd, TableDef table) throws SQLException {
	
	  for( ForeignKeyDef fkDef : table.getForeignKeys() ) {		  
		  
		  // TODO: Check if it already exists		  
		  
		  dd.createForeignKey( c, fkDef );
	  }
  }

  private void verifyMapping(Connection c, MetaObject mc, DatabaseDriver dd, ObjectMapping mapping) {
	  
	  if ( mapping == null ) return;

	  if (!( mapping instanceof ObjectMappingDB )) {
		  throw new MetaException( "Expected an ObjectMappingDB instance, not a [" + mapping.getClass().getSimpleName() + "]" );
	  }
	  	  
	  ObjectMappingDB omdb = (ObjectMappingDB) mapping;
	  
	  try {
	      if ( validateDefinition( c, dd, omdb.getDBDef() )) {
	    	  createdDefs.add( omdb.getDBDef() );
	      }
	  }
	  catch( Exception e ) {
		  //throw new MetaException( "Error validating mapping [" + mapping + "] for MetaClass [" + mc + "]: " + e.getMessage(), e );
	  }
  }

  public void destroy() throws Exception
  {
  }

  /**
   * Validates whether the MetaClass actually exists inthe Database
   * @throws SQLException
   */
  protected boolean validateDefinition( Connection c, DatabaseDriver dd, BaseDef def ) throws SQLException
  {
	  // See if we checked this one before
	  if ( checkedDefs.contains( def )) {
		  //ystem.out.println( ">>>> SKIPPING   - " + def );
		  return false;
	  } 
	  // If not, add it to the list as we're checking it now
	  else {
		  //ystem.out.println( ">>>> VALIDATING - " + def );
		  checkedDefs.add( def );
	  }
	  
	  if ( def instanceof ViewDef ) {

		  // Check if the view exists and is valid
		  ViewDef view = (ViewDef) def;
		  if ( !dd.checkView( c, view ) ) {
			
			  // If not, then auto create it or throw a not found exception
			  if ( shouldAutoCreate() ) {
				  log.info( "VALIDATION - CREATING VIEW " + def );
				  dd.createView( c, view );
				  return true;
			  } else {
				  throw new TableDoesNotExistException( "View [" + def + "] does not exist" );
			  }			  
		  }		  
	  }
	  else if ( def instanceof TableDef ) {
		  
		  // Check if the table exists and is valid
		  TableDef table = (TableDef) def;
		  if ( !dd.checkTable( c, table ) ) {
			
			  // If not, then auto create it or throw a not found exception
			  if ( shouldAutoCreate() ) {
				  log.info( "VALIDATION - CREATING TABLE " + table );
				  dd.createTable( c, table );
				  return true;
			  } else {
				  throw new TableDoesNotExistException( "Table [" + def + "] does not exist" );
			  }			  
		  }		  
	  }
	  else {
		  throw new IllegalArgumentException( "Unknown definition [" + def + "], so cannot validate" );
	  }

	  return false;
  }

  /*private void validateTableOrView( Connection c, String name, Collection<MetaField> fields, MetaClass mc ) throws SQLException, MetaException
  {
    // VALIDATE TABLE OR VIEW
	String schema = null;
	
	int n = name.indexOf( '.' );
	if ( n > 0 ) 
	{
		schema = name.substring( 0, n );
		name = name.substring( n + 1 );
	}
	
	//if ( schema != null ) schema = schema.toUpperCase();
	//name = name.toUpperCase();
	  
    ResultSet rs = c.getMetaData().getTables( null, schema, name, null );
    try {
      boolean found = false;
      while ( rs.next() ) {
        if ( name.equalsIgnoreCase( rs.getString( 3 ))) {
          found = true;
          break;
        }
      }

      if ( !found )
        throw new TableDoesNotExistException( "Table [" + name + "] does not exist" );
    }
    finally {
      rs.close();
    }

    for( MetaField f : fields )
    {
      String col = getObjectManager().getColumnName( f );

      if ( col == null ) continue;
      //col = col.toUpperCase();

      rs = c.getMetaData().getColumns( null, schema, name, col );
      try {
        boolean found = false;
        while ( rs.next() ) {
          if ( col.equalsIgnoreCase( rs.getString( 4 ))) {
            found = true;
            break;
          }
        }

        if ( !found )
          throw new SQLException( "Table [" + name + "] does not have a Column [" + col + "]" );

        //int type = rs.getInt( 5 );
      }
      finally {
        rs.close();
      }
    }
  }*/

  //protected Connection getConnection() throws MetaException {
  //  return (Connection) getObjectManager().getConnection().getDatastoreConnection();
  //}

  public void setObjectManager( ObjectManagerDB manager ) {
    mObjectManager = manager;
  }

  public ObjectManagerDB getObjectManager() {
    return mObjectManager;
  }
}
