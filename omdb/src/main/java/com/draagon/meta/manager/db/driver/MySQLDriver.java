/*
 * Created on Dec 14, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.draagon.meta.manager.db.driver;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.draagon.meta.manager.db.defs.ColumnDef;
import com.draagon.meta.manager.db.defs.TableDef;

/**
 * @author doug
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MySQLDriver extends GenericSQLDriver {

    private static final Logger log = LoggerFactory.getLogger(MySQLDriver.class);

    /**
     *
     */
    public MySQLDriver() {
        super();
    }

    /**
     * Creates a table in the database
     */
    @Override
    public void createTable( Connection c, TableDef def ) throws SQLException
    {
        /*String table = null;

        try
        {
          table = (String) mc.getAttribute( ObjectManagerDB.TABLE_REF );

          /////////////////////////////////////////////////////
          // CREATE TABLE

          String query = "CREATE TABLE " + table + " (\n";

          // Create the individual table fields
          int found = 0;
          //Collection fields = mc.getMetaFields();
          Collection<MetaField> fields = getManager().getWriteableFields( mc );

          // See if there is more than 1 key
          int keys = 0;
          for( MetaField mf : fields )
          {
            if ( getManager().isPrimaryKey( mf )) keys++;
          }

          // Create the fields
          for( MetaField mf : fields )
          {
            String name = getManager().getColumnName( mf );
            if ( name == null ) continue;

            if ( found > 0 ) query += ",\n";
            found++;

            String flags = "";
            if ( getManager().isPrimaryKey( mf ) && keys == 1 )
                flags = "PRIMARY KEY ";
            else if ( getManager().isUnique( mf )) flags = "UNIQUE ";
            //else if ( getManager().isIndex( mf )) flags = "NONCLUSTERED ";

            switch( mf.getType() )
            {
              case MetaField.BOOLEAN: query += " " + name + " BOOL " + flags; break;
              case MetaField.BYTE:    query += " " + name + " TINYINT " + flags; break;
              case MetaField.SHORT:   query += " " + name + " SMALLINT " + flags; break;
              case MetaField.INT:     query += " " + name + " INT " + flags; break;
              case MetaField.LONG:    query += " " + name + " BIGINT " + flags; break;
              case MetaField.FLOAT:   query += " " + name + " FLOAT " + flags; break;
              case MetaField.DOUBLE:  query += " " + name + " DOUBLE " + flags; break;
              case MetaField.DATE:    query += " " + name + " DATETIME " + flags; break;
              case MetaField.STRING:  {
                  if ( mf.getLength() > 255 )
                      { query += " " + name + " TEXT " + flags; break; }
                  else
                      { query += " " + name + " VARCHAR(" + mf.getLength() + ") " + flags; break; }
              }

              case MetaField.OBJECT:
                throw new MetaException( "In MetaClass [" + mc.getName() + "] the MetaField [" + mf.getName() + "] is of type OBJECT which is not support for this database type" );

              default: continue;
            }
          }

          query += "\n)";

          // This means there were no columns defined for the table
          if ( found == 0 ) return;

          if ( log.isDebugEnabled() )
              log.debug( "Creating table [" + table + "]: " + query );

          Statement s = c.createStatement();
          try { s.execute( query ); }
          finally {  s.close(); }

          /////////////////////////////////////////////////////
          // CREATE SEQUENCES

          for( MetaField mf : fields )
          {
            String seq = getManager().getSequenceName( mf );
            if ( seq != null )
            {
                query = "CREATE TABLE " + seq + " (id BIGINT NOT NULL)";
                s = c.createStatement();
                try { s.execute( query ); }
                finally {  s.close(); }

                if ( log.isDebugEnabled() )
                    log.debug( "Creating sequence [" + seq + "] on field [" + mf.getName() + "]: " + query );

                query = "INSERT INTO " + seq + " VALUES(" + ( getManager().getSequenceStart(mf) - 1 ) + ")";
                s = c.createStatement();
                try { s.execute( query ); }
                finally {  s.close(); }

                if ( log.isDebugEnabled() )
                    log.debug( "Initializing sequence [" + seq + "] on field [" + mf.getName() + "]: " + query );
            }
          }

          /////////////////////////////////////////////////////
          // CREATE INDEXES

          for( MetaField mf : fields )
          {
            String name = getManager().getColumnName( mf );
            if ( name == null ) continue;

            if ( !getManager().isPrimaryKey( mf )
                && !getManager().isIndex( mf )) continue;

            // Create the sequence
            query = "CREATE INDEX " + table + "_" + name + "_index ON " + table + "(" + name + ")";

            if ( log.isDebugEnabled() )
                log.debug( "Creating index on field [" + mf.getName() + "]: " + query );

            s = c.createStatement();
            try { s.execute( query ); }
            finally { s.close(); }
          }
        }
        catch( Exception e )
        {
          throw new MetaException( "Creation of table [" + table + "] failed: " + e.getMessage(), e );
        }*/
    }

    /**
     * Gets the next sequence for a given MetaClass
     */
    public String getNextAutoId( Connection conn, ColumnDef col ) throws SQLException
    {
      //String seq = getManager().getSequenceName( mf );
      //if ( seq == null )
      //  throw new MetaException( "MetaField[" + mf + "] has no sequence defined" );

      try
      {
        // Increment the ID
        String query = "UPDATE " + getProperName( col.getSequence().getNameDef() ) + " SET id=LAST_INSERT_ID(id+1)";

        Statement s = conn.createStatement();
        try { s.execute( query ); }
        finally { s.close(); }

        // Get next next MAX() sequence
        s = conn.createStatement();
        try
        {

          query = "SELECT LAST_INSERT_ID()";

          ResultSet rs = s.executeQuery( query );

          if ( !rs.next() )
            throw new SQLException( "Unable to get next id for column [" + col + "], no result in result set" );

          try { return rs.getString( 1 ); }
          finally { rs.close(); }
        }
        finally { s.close(); }
      }
      catch( SQLException e )
      {
        log.error( "Unable to get next id for column [" + col + "]: " + e.getMessage(), e );
        throw new SQLException( "Unable to get next id for column [" + col + "]: " + e.getMessage(), e );
      }
    }

    ///////////////////////////////////////////////////////
    // TO STRING METHOD
    public String toString()
    {
      return "MySQL Database Driver";
    }
}
