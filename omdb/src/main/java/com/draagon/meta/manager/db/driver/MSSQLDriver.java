/*
 * Copyright 2003 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */

package com.draagon.meta.manager.db.driver;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.SQLException;
import java.sql.Types;

import com.draagon.meta.object.MetaObject;
import com.draagon.meta.MetaException;
import com.draagon.meta.field.MetaField;
import com.draagon.meta.manager.db.defs.ColumnDef;
import com.draagon.meta.manager.db.defs.TableDef;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Object Manager Base is able to add, update, delete,
 * and retrieve objects of those types from a datastore.
 */
public class MSSQLDriver extends GenericSQLDriver
{
	private static final Logger log = LoggerFactory.getLogger(MSSQLDriver.class);

	public MSSQLDriver() {
	}

	/**
	 * Creates a table in the database
	 */
	@Override
	public void createTable( Connection c, TableDef table ) throws SQLException
	{
		String query = "CREATE TABLE [" + table + "] (\n";

		boolean multi = ( table.getPrimaryKeys().size() > 1 );

		boolean hasIdentity = false;

		// Create the individual table fields
		int found = 0;
		for ( ColumnDef col : table.getColumns() )
		{
			String name = col.getName();
			if ( name == null || name.length() == 0 ) {
				throw new IllegalArgumentException( "No name defined for column [" + col + "]" );
			}

			if (found > 0) query += ",\n";
			found++;

			String flags = "";
			if ( col.isPrimaryKey() && !multi)
				flags = "PRIMARY KEY ";
			else if ( col.isUnique())
				flags = "UNIQUE ";
			//else if (getManager().isIndex(mf)) flags = "NONCLUSTERED ";

			switch ( col.getSQLType() ) {
			case Types.BOOLEAN:
			case Types.BIT:
				query += "[" + name + "] [bit] " + flags;
				break;
			case Types.TINYINT:
				query += "[" + name + "] [tinyint] " + flags;
				break;
			case Types.SMALLINT:
				query += "[" + name + "] [smallint] " + flags;
				break;
			case Types.INTEGER:
				query += "[" + name + "] [int] " + flags;
				break;
			case Types.BIGINT:
				query += "[" + name + "] [bigint] " + flags;
				break;
			case Types.FLOAT:
				query += "[" + name + "] [float] " + flags;
				break;
			case Types.DOUBLE:
				query += "[" + name + "] [decimal](19,4) " + flags;
				break;
			case Types.TIMESTAMP:
				query += "[" + name + "] [datetime] " + flags;
				break;
			case Types.VARCHAR:
				query += "[" + name + "] [varchar](" + col.getLength() + ") " + flags;
				break;

			default:
				throw new IllegalArgumentException( "Table [" + table + "] with Column [" + col + "] is of SQL type (" + col.getSQLType() + ") which is not support by this database" );
			}

			// Create the identity columns
			if ( col.isAutoIncrementor() )
			{
				if ( hasIdentity )
					throw new MetaException( "Table [" + table + "] cannot have multiple identity (auto id) columns!" );

				query += "NOT NULL IDENTITY( " + col.getSequence().getStart() + ", " + col.getSequence().getIncrement() + " ) ";

				hasIdentity = true;
			}
		}

		query += "\n)";

		// This means there were no columns defined for the table
		if (found == 0) return;

		if ( log.isDebugEnabled() ) {
			log.debug( "Creating table [" + table + "]: " + query);
		}
		//ystem.out.println( ">>>> Creating table [" + table + "]: " + query);

		Statement s = c.createStatement();
		try {
			s.execute(query);
		} finally {
			s.close();
		}
	}

	/*@Override
		public void createSequence(...) { 

			// CREATE SEQUENCES
			for (Iterator i = fields.iterator(); i.hasNext();)
            {
                MetaField mf = (MetaField) i.next();

                String seq = getManager().getSequenceName(mf);
                if (seq != null)
                {
                  String type = "INT";

                  // If the MetaField type is long, then use a BIGINT
                  if ( mf.getType() == MetaField.LONG )
                    type = "BIGINT";

                    query = "CREATE TABLE " + seq + " (id " + type + " NOT NULL IDENTITY( " + ( getManager().getSequenceStart(mf) - 1 ) + ", 1 ), value INT )";
                    s = c.createStatement();
                    try {
                        s.execute(query);
                    } finally {
                        s.close();
                    }

                    if (log.isDebugEnabled())
                        log.debug("Creating sequence [" + seq + "] on field [" + mf.getName() + "]: " + query);

                    //query = "INSERT INTO " + seq + " VALUES(" + ( getManager().getSequenceStart(mf) - 1 ) + ")";
                    //s = c.createStatement();
                    //try {
                    //    s.execute(query);
                    //} finally {
                    //    s.close();
                    //}

                    //if (log.isDebugEnabled())
                    //    log.debug("Initializing sequence [" + seq + "] on field [" + mf.getName() + "]: " + query);
                }
            }
		}*/

	// Create indexes
	/* public void createIndex( ... ) {
	for ( MetaField mf : getManager().getWriteableFields( mc ))
	{
		String name = getManager().getColumnName(mf);
		if (name == null) continue;

		if (!getManager().isPrimaryKey(mf)
				&& !getManager().isIndex(mf))
			continue;

		// Create the sequence
		String query2 = "CREATE INDEX " + table + "_" + name + "_index ON " + table + "(" + name + ")";

		log.debug("(createIndex) Creating index for [" + mc.getName() + "] on field [" + mf.getName() + "]: " + query2);

		s = c.createStatement();
		try {
			s.execute(query2);
		} finally {
			s.close();
		}

	// WARNING:  Reimplement this to work with single auto keys

	// query = "ALTER TABLE [" + table + "] WITH NOCHECK ADD\n" +
    //                "CONSTRAINT [PK_" + table +"] PRIMARY KEY NONCLUSTERED ( [id] ) ON [PRIMARY]";

    //        s = c.createStatement();
    //        s.execute( query );
    //        s.close(); 
	}*/


	/**
	 * Creates a view in the database
	 */
	/* @Override
	public void createView(Connection c, MetaClass mc) throws MetaException
	{
		String view = null;

		view = (String) mc.getAttribute( ObjectManagerDB.VIEW_REF );

		String sql = (String) mc.getAttribute( ObjectManagerDB.VIEW_SQL_REF );

		String query = "CREATE VIEW [" + view + "] AS " + sql;

		log.debug( "Creating view: " + query);
		//ystem.out.println( ">>>> Creating View: " + query);

		try
		{
			Statement s = c.createStatement();
			try {
				s.execute(query);
			} finally {
				s.close();
			}
		}
		catch (Exception e) {
			throw new MetaException( "Creation of view [" + view + "] failed [" + query + ": " + e.getMessage(), e );
		}
	}*/

	/**
	 * Creates the foreign keys for the table in the database
	 */
	/*@Override
	public void createForeignKeys( Connection c, MetaClass mc ) throws MetaException
	{
		for( ForeignKeyDef fk : getManager().getForeignKeys( mc ))
		{
			String table = getManager().getTableName( mc );
			String col = getManager().getColumnName( fk.getField() );

			String foreignTable = getManager().getTableName( fk.getForeignClass() );
			String foreignCol = getManager().getColumnName( fk.getForeignField() );

			String fkstr = "fk_" + table + "_" + col;

			String query = "ALTER TABLE [" + table + "]"
			+ " ADD CONSTRAINT [" + fkstr + "]"
			+ " FOREIGN KEY (" + col + ")"
			+ " REFERENCES [" + foreignTable + "] (" + foreignCol + ")";

			log.debug( "Creating foreign key: " + query);
			//ystem.out.println( ">>>> Creating foreign key: " + query);

			try
			{
				Statement s = c.createStatement();
				try {
					s.execute(query);
				} finally {
					s.close();
				}
			}
			catch (Exception e) {
				throw new MetaException( "Creation of foreign key [" + fk + "] failed [" + query + "]: " + e.getMessage(), e );
			}
		}
	}*/

	/**
	 * Returns whether the auto id is retrieved prior to creation, and MSSQL is not
	 */
	//@Override
	//public int getAutoType() {
	//	return AUTO_DURING;
	//}

	/**
	 * Gets the string to append to the INSERT call to request the generated ids
	 */
	private String getInsertAppendString( MetaObject mc ) throws MetaException
	{
		//if ( autoIdField( mc ) == null ) return "";
		//else 
			return ";SELECT @@IDENTITY"; // Identity_Scope()";
	}

	protected MetaField autoIdField( ColumnDef col ) throws MetaException
	{
		/*Object val = mc.getCacheValue( "MSSQL_IDENTITY" );
		if ( val != null )
		{
			if ( val instanceof MetaField ) {
				return (MetaField) val;
			}
		}
		else
		{
			for( MetaField mf : mc.getMetaFields() ) {
				if ( mf.hasAttribute( ObjectManager.AUTO )) {
					if ( mf.getAttribute( ObjectManager.AUTO ).toString().equals( ObjectManagerDB.AUTO_ID ))
					{
						mc.setCacheValue( "MSSQL_IDENTITY", mf );
						return mf;
					}
				}
			}

			mc.setCacheValue( "MSSQL_IDENTITY", new Object() );
		}*/

		return null;
	}

	/**
	 * Used if AUTO_DURING to retrieve the ids for the specified class
	 */
	//@Override
	private void getAutoIdentifiers( PreparedStatement s, ColumnDef col ) throws MetaException
	{
		/*MetaField mf = autoIdField( col );
		if ( mf == null ) return;

		try
		{
			ResultSet rs = s.getResultSet();

			if ( rs == null && s.getMoreResults() )
				rs = s.getResultSet();

			if ( rs == null )
				throw new MetaException( "No identifiers were returned in the result set" );

			try
			{
				if ( rs.next() )
				{
					switch( mf.getType() )
					{
					case MetaFieldTypes.INT:
						mf.setInt( o, rs.getInt( 1 ));
						break;
					case MetaFieldTypes.LONG:
						mf.setLong( o, rs.getLong( 1 ));
						break;
					case MetaFieldTypes.SHORT:
						mf.setShort( o, rs.getShort( 1 ));
						break;
					default:
						mf.setString( o, rs.getString( 1 ));
					}
				}
			}
			finally {
				rs.close();
			}
		}
		catch( SQLException e ) {
			log.error( "Unable to get identity for MetaClass[" + mc + "]: " + e.getMessage());
			throw new MetaException("Unable to get identity for MetaClass[" + mc + "]: " + e.getMessage(), e);
		}*/
	}

	/**
	 * Gets the last auto id for the given MetaClass
	 */
	/*	@Override
	public String getLastAutoId( Connection conn, MetaClass mc, MetaField mf ) throws MetaException
    {
        try
        {
            String identity = null;

            // Get the last identity value inserted
            Statement s = conn.createStatement();
            try {

            	// This only works on SQL 2000
                String query = "SELECT SCOPE_IDENTITY()";

                // This works on older versions of SQL but can cause problems
                //String query = "SELECT @@IDENTITY";

                ResultSet rs = s.executeQuery(query);

                if (!rs.next())
                    throw new MetaException( "Unable to get last id for MetaField[" + mf + "], no result in result set" );

                try {
                	identity = rs.getString( 1 );
                } finally {
                    rs.close();
                }
            } finally {
                s.close();
            }

            return identity;
        }
        catch ( SQLException e ) {
            log.error( "Unable to get last id for MetaField[" + mf + "]: " + e.getMessage());
            throw new MetaException("Unable to get last id for MetaField[" + mf + "]: " + e.getMessage(), e);
        }
     }*/

	@Override
	public String getDateFormat() {
		return "MM/dd/yyyy hh:mm:ss:SSS aaa";
	}

	///////////////////////////////////////////////////////
	//	TO STRING METHOD
	public String toString() {
		return "MSSQL Database Driver";
	}
}
