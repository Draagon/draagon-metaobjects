/*
 * Created on Jul 13, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.draagon.meta.manager.db.driver;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.draagon.meta.object.MetaObject;
import com.draagon.meta.MetaException;
import com.draagon.meta.field.MetaField;
import com.draagon.meta.manager.db.ObjectMappingDB;
import com.draagon.meta.manager.db.defs.BaseTableDef;
import com.draagon.meta.manager.db.defs.ColumnDef;

/**
 * @author dmealing
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class OracleDriver extends GenericSQLDriver {

  private static Log log = LogFactory.getLog( OracleDriver.class );

  public OracleDriver()
  {
  }

  /**
   * Creates a table in the database
   */
  public void createTable( Connection c, MetaObject mc )
    throws MetaException
  {
    log.warn( "Create Table not yet implemented" );
  }

  /**
   * Deletes a table from the database
   */
  public void deleteTable( Connection c, MetaObject mc )
    throws MetaException
  {
    log.warn( "Delete Table not yet implemented" );
  }

  /**
   * Gets the next sequence for a given MetaClass
   */
  private String getNextAutoId( Connection conn, MetaObject mc, ObjectMappingDB mapping, MetaField mf ) throws SQLException
  {
	ColumnDef colDef = (ColumnDef) mapping.getArgDef( mf );
	  
    String seq = getProperName( colDef.getSequence().getNameDef() ); //getManager().getSequenceName( mf );
    if ( seq == null )
      throw new MetaException( "MetaField[" + mf + "] has no sequence defined" );

    try
    {
      // Get next next MAX() sequence
      Statement s = conn.createStatement();
      try
      {
        String query = "SELECT " + seq + ".nextval FROM dual";

        ResultSet rs = s.executeQuery( query );

        if ( !rs.next() )
          throw new MetaException( "Unable to get next id for MetaField[" + mf + "], no result in result set" );

        try
        {
          return rs.getString( 1 );
        }
        finally { rs.close(); }
      }
      finally { s.close(); }
    }
    catch( SQLException e )
    {
      log.error( "Unable to get next id for MetaField[" + mf + "]: " + e.getMessage() );
      throw new MetaException( "Unable to get next id for MetaField[" + mf + "]: " + e.getMessage(), e );
    }
  }

  /**
   * The SQL query to append to a SQL SELECT to lock the returned rows
   */
  @Override
  public String getLockString() throws MetaException
  {
	  return "FOR UPDATE";
  }

  ///////////////////////////////////////////////////////
  // TO STRING METHOD
  public String toString()
  {
    return "Oracle Database Driver";
  }
}
