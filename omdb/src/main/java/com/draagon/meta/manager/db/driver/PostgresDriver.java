/*
 * Created on Feb 1, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.draagon.meta.manager.db.driver;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.draagon.meta.MetaException;
import com.draagon.meta.manager.db.defs.ColumnDef;
import com.draagon.meta.manager.db.defs.IndexDef;
import com.draagon.meta.manager.db.defs.SequenceDef;
import com.draagon.meta.manager.db.defs.TableDef;
import com.draagon.meta.manager.db.defs.ViewDef;
import com.draagon.meta.manager.exp.Range;

/**
 * @author doug
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class PostgresDriver extends GenericSQLDriver {

	private static final Logger log = LoggerFactory.getLogger(PostgresDriver.class);

	/**
	 *
	 */
	public PostgresDriver() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * Creates a table in the database
	 */
	@Override
	public void createTable( Connection c, TableDef table ) throws SQLException
	{
		StringBuilder query = new StringBuilder();

		try
		{
			query.append( "CREATE TABLE " ).append( table.getNameDef().getFullname() ).append( " (\n" );

			String primaryKey = null;

			// Create the individual table fields
			int found = 0;
			//Collection fields = mc.getMetaFields();
			List<ColumnDef> cols = table.getColumns();

			// See if there is more than 1 key
			int keys = table.getPrimaryKeys().size();

			// Create the fields
			for( ColumnDef col : cols ) {

				String name = col.getName();
				if ( name == null ) continue;

				if ( found > 0 ) query.append( ",\n" );
				found++;

				String flags = "";
				if ( col.isPrimaryKey() && keys == 1 ) primaryKey = name;
				else if ( col.isUnique()) flags = "UNIQUE ";

				query.append( " " ).append( name ).append( " " );

				switch( col.getSQLType() )
				{
				case Types.BIT:
				case Types.BOOLEAN: 	query.append( "BOOLEAN" ); break;
				case Types.TINYINT:   query.append( "INT2" ); break;
				case Types.SMALLINT:  query.append( "INT2" ); break;
				case Types.INTEGER:   query.append( "INT4" ); break;
				case Types.BIGINT:    query.append( "INT8" ); break;
				case Types.FLOAT:   	query.append( "FLOAT8" ); break;
				case Types.DOUBLE:  	query.append( "FLOAT12" ); break;
				case Types.TIMESTAMP:    query.append( "TIMESTAMP" ); break;
				case Types.VARCHAR:  {
					if ( col.getLength() > 4095 )
					{ query.append( "TEXT" ); break; }
					else
					{ query.append( "VARCHAR(" ).append( col.getLength() ).append( ")" ); break; }
				}

				default:
					throw new UnsupportedOperationException( "In Table Class [" + table + "] the Column [" + col + "] is of type (" + col.getSQLType() + ") which is not support on this database driver" );
				}

				query.append( " " ).append( flags );
			}

			if ( primaryKey != null ) {
				query.append( ",\nPRIMARY KEY( " ).append( primaryKey ).append( "))" );
			} else {
				query.append( "\n)" );
			}

			// This means there were no columns defined for the table
			if ( found == 0 ) return;

			if ( log.isDebugEnabled() ) {
				log.debug( "Creating table [" + table + "]: " + query );
			}

			Statement s = c.createStatement();
			try { s.execute( query.toString() ); }
			finally {  s.close(); }
		}
		catch( Exception e ) {
			throw new SQLException( "Creation of table [" + table + "] failed using SQL [" + query + "]: " + e.getMessage(), e );
		}
	}

	@Override
	public void createSequence( Connection c, SequenceDef sequence ) throws SQLException {

		StringBuilder query = new StringBuilder();
		try {
			String seq = getProperName( sequence.getNameDef() );
			query.append( "CREATE SEQUENCE " )
			.append( seq )
			.append( " START " )
			.append( sequence.getStart() );
	
			Statement s = c.createStatement();
			try { s.execute( query.toString() ); }
			finally {  s.close(); }
	
			if ( log.isDebugEnabled() ) {
				log.debug( "Creating sequence [" + sequence + "]: " + query );
			}
		}
		catch( Exception e ) {
			throw new SQLException( "Creation of sequence [" + sequence + "] failed using SQL [" + query + "]: " + e.getMessage(), e );
		}
	}

	@Override
	public void createIndex( Connection c, IndexDef index ) throws SQLException {
		
		StringBuilder query = new StringBuilder();
		try {
			String name = index.getName();
			
			query.append( "CREATE INDEX " )
			.append( name )
			.append( " ON " )
			.append( getProperName( index.getTable().getNameDef() ) )
			.append( "(" );
			
			boolean first = true;
			for ( String colName : index.getColumnNames() ) {
				if ( first ) first = false;
				else query.append( "," );
				query.append( colName );
			}
			
			query.append( ")" );
	
			if ( log.isDebugEnabled() ) {
				log.debug( "Creating index [" + index + "]: " + query );
			}
	
			Statement s = c.createStatement();
			try { s.execute( query.toString() ); }
			finally { s.close(); }
		}
		catch( Exception e ) {
			throw new SQLException( "Creation of index [" + index + "] failed using SQL [" + query + "]: " + e.getMessage(), e );
		}
	}
		
	/**
	 * Gets the next sequence for a given MetaClass
	 */
	@Override
	protected String getNextAutoId( Connection conn, ColumnDef col ) throws SQLException
	{
		if ( col.getSequence() == null )
			throw new MetaException( "Column definition [" + col + "] has no sequence defined" );

		String seq = getProperName( col.getSequence().getNameDef() );

		try
		{
			// Increment the ID
			String query = "SELECT nextval(?)";

			PreparedStatement s = conn.prepareStatement( query );
			s.setString( 1, seq );

			try
			{
				ResultSet rs = s.executeQuery();

				if ( !rs.next() )
					throw new SQLException( "Unable to get next id for Column Definition [" + col + "], no result in result set" );

				try { 
					String id = rs.getString( 1 );
					if ( log.isDebugEnabled() ) {
						log.debug( "Retrieved id (" + id + ") from sequence [" + seq + "]" );
					}
					if ( id == null ) throw new SQLException( "A null sequence value was returned" );
					return id;
				}
				finally { rs.close(); }
			}
			finally { s.close(); }
		}
		catch( SQLException e )
		{
			log.error( "Unable to get next id for Column definition [" + col + "]: " + e.getMessage(), e );
			throw new SQLException( "Unable to get next id for Column definition [" + col + "]: " + e.getMessage(), e );
		}
	}
	
	
	/** Returns whether the drive supports the Range within the query, i.e. LIMIT */
	@Override
	protected boolean supportsRangeInQuery() {
		return true;
	}
	
	/** Returns the SQL portion of the range string */
	public String getRangeString( Range range ) {
						
		StringBuilder b = new StringBuilder( "LIMIT " );
		b.append(( range.getEnd() - range.getStart() ) + 1 );
		if ( range.getStart() > 1 ) {
			b.append( " OFFSET " ).append(( range.getStart()-1 ));
		}
		return b.toString();
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
		return "PostgreSQL Database Driver";
	}
}
