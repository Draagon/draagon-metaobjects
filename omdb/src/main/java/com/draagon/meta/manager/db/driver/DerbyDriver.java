package com.draagon.meta.manager.db.driver;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.draagon.meta.manager.db.defs.ColumnDef;
import com.draagon.meta.manager.db.defs.TableDef;
import java.sql.Types;
import java.util.List;

public class DerbyDriver extends GenericSQLDriver 
{
	private static Log log = LogFactory.getLog( DerbyDriver.class );

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

			//String primaryKey = null;

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
                                if ( col.isAutoIncrementor() ) {
                                    flags = "GENERATED ALWAYS AS IDENTITY CONSTRAINT "+table.getNameDef().getName()+"_"+name+"_PK PRIMARY KEY " ;                                    
                                }
                                else if ( col.isPrimaryKey() && keys == 1 ) {
                                    flags = "CONSTRAINT "+table.getNameDef().getName()+"_"+name+"_PK PRIMARY KEY " ;
                                }
				else if ( col.isUnique()) {
                                    flags = "UNIQUE ";
                                }
                                

				query.append( " " ).append( name ).append( " " );

				switch( col.getSQLType() )
				{
				case Types.BIT:
				case Types.BOOLEAN: 	query.append( "BOOLEAN" ); break;
				case Types.TINYINT:   query.append( "INT" ); break;
				case Types.SMALLINT:  query.append( "INT" ); break;
				case Types.INTEGER:   query.append( "INT" ); break;
				case Types.BIGINT:    query.append( "INT" ); break;
				case Types.FLOAT:   	query.append( "FLOAT" ); break;
				case Types.DOUBLE:  	query.append( "FLOAT" ); break;
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

			//if ( primaryKey != null ) {
			//	query.append( ",\nPRIMARY KEY( " ).append( primaryKey ).append( "))" );
			//} else {
				query.append( "\n)" );
			//}

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
        
	/**
	 * Creates a table in the database
	 */	
	/*@Override
	  public void createTable( Connection c, MetaClass mc )
	  	throws MetaException
	  {
	      String table = null;

	      try
	      {
	        table = (String) mc.getAttribute( ObjectManagerDB.TABLE_REF );

	        String query = "CREATE TABLE " + table + "(\n";

	        // Create the individual table fields
	        int found = 0;
	        Collection<MetaField> fields = mc.getMetaFields();
	        for( MetaField mf : fields )
	        {
	          String name = getManager().getColumnName( mf );
	          if ( name == null ) continue;

	          if ( mf.hasAttribute( ObjectManagerDB.IS_READONLY )) continue;

	          boolean isIdentity = false;
	          //Collection keys = getManager().getPrimaryKeys( mc );
	          if ( //keys.size() == 1 
	        		  mf.hasAttribute( ObjectManager.AUTO )
	        		  && mf.getAttribute( ObjectManager.AUTO )
	        		  		.equals( ObjectManagerDB.AUTO_ID )) isIdentity = true;

	          if ( found > 0 ) query += ",\n";
	          found++;

	          // Get the starting index for the table
	          int startIndex = 1;
	          if ( mf.hasAttribute( "dbIdentityStart" )) {
	        	  startIndex = Integer.parseInt( (String) mf.getAttribute( "dbIdentityStart" ));
	          }

	          // Get the starting index for the table
	          int increment = 1;
	          if ( mf.hasAttribute( "dbIdentityIncrement" )) {
	        	  startIndex = Integer.parseInt( (String) mf.getAttribute( "dbIdentityIncrement" ));
	          }

	          //if ( tmp[ i ].getSpecial() == Field.AUTONUM )
	          //{
	          //  query += "[" + name + "] [numeric](18,0) IDENTITY (1,1) PRIMARY KEY CLUSTERED NOT NULL";
	          //}
	          //else
	          //{
	            String flags = "";
	            if ( getManager().isPrimaryKey( mf ))
	            {
	            	flags = "NOT NULL ";
	            	if ( isIdentity ) flags += "GENERATED ALWAYS AS IDENTITY (START WITH " + startIndex + ", INCREMENT BY " + increment + ") ";
	            }
	            else if ( getManager().isUnique( mf )) flags = "NOT NULL UNIQUE ";
	            //else if ( getManager().isIndex( mf )) flags = "NONCLUSTERED ";

	            switch( mf.getType() )
	            {
	              case MetaField.BOOLEAN: query += "" + name + " smallint " + flags; break;
	              case MetaField.BYTE: query += "" + name + " smallint " + flags; break;
	              case MetaField.SHORT: query += "" + name + " smallint " + flags; break;
	              case MetaField.INT: query += "" + name + " int " + flags; break;
	              case MetaField.LONG: query += "" + name + " bigint " + flags; break;
	              case MetaField.FLOAT: query += "" + name + " float " + flags; break;
	              case MetaField.DOUBLE: query += "" + name + " decimal(19,4) " + flags; break;
	              case MetaField.DATE: query += "" + name + " timestamp " + flags; break;
	              case MetaField.STRING: query += "" + name + " varchar(" + mf.getLength() + ") " + flags; break;

	              case MetaField.OBJECT:
	                throw new MetaException( "In item [" + mf.getName() + "] the field [" + mf.getName() + "] is of type OBJECT which is not support for this database type" );

	              default: continue;
	            }
	          }
	        //}

	        query += "\n)";

	        // This means there were no columns defined for the table
	        if ( found == 0 ) return;

	        log.debug( "(createTable) Creating table [" + table + "]: " + query );

	        Statement s = c.createStatement();
	        try {
	          s.execute( query );
	        } finally {  s.close(); }

	        // Create indexes
	        for( MetaField mf : fields )
	        {
	          String name = getManager().getColumnName( mf ); 
	          if ( name == null ) continue;

	          if ( !getManager().isPrimaryKey( mf )
	          		&& !getManager().isIndex( mf )) continue;

	          // Create the sequence
	          String query2 = "CREATE INDEX " + table + "_" + name + "_index ON " + table + "(" + name + ")";

	          log.debug( "(createIndex) Creating index for [" + mc.getName() + "] on field [" + mf.getName() + "]: " + query2 );

	          s = c.createStatement();
	          try { s.execute( query2 ); }
	          finally { s.close(); }
	        }

	        // WARNING:  Reimplement this to work with single auto keys

	        // query = "ALTER TABLE [" + table + "] WITH NOCHECK ADD\n" +
	        //        "CONSTRAINT [PK_" + table +"] PRIMARY KEY NONCLUSTERED ( [id] ) ON [PRIMARY]";

	        //s = c.createStatement();
	        //s.execute( query );
	        //s.close();
	      }
	      catch( Exception e )
	      {
	        //System.out.println( "LOGGER: " + getLogger() );
	        //System.out.println( "ITEM:   " + item );

	        //getLogger().log( ZONE, "createTable", Logger.ERROR, "Creation of table [" + item.getItemRef() + "] failed", e );
	        throw new MetaException( "Creation of table [" + table + "] failed", e );
	      }
		}*/

	/**
	 * Creates a view in the database
	 */
	/*@Override
	    public void createView(Connection c, MetaClass mc) throws MetaException
	    {
		  String view = null;

	      view = (String) mc.getAttribute( ObjectManagerDB.VIEW_REF );

	      String sql = (String) mc.getAttribute( ObjectManagerDB.VIEW_SQL_REF );

	      String query = "CREATE VIEW " + view + " AS " + sql;

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
	    } */

	/**
	 * Creates the foreign keys for the table in the database
	 */
	/* @Override
		public void createForeignKeys( Connection c, MetaClass mc ) throws MetaException
		{
			for( ForeignKey fk : getManager().getForeignKeys( mc ))
			{
				String table = getManager().getTableName( mc );
				String col = getManager().getColumnName( fk.getField() );

				String foreignTable = getManager().getTableName( fk.getForeignClass() );
				String foreignCol = getManager().getColumnName( fk.getForeignField() );

	        	String fkstr = "fk_" + table + "_" + col;

	            String query = "ALTER TABLE " + table + ""
	            	+ " ADD CONSTRAINT " + fkstr + ""
	            	+ " FOREIGN KEY (" + col + ")"
	            	+ " REFERENCES " + foreignTable + " (" + foreignCol + ")";

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

	///**
	// * Returns whether the auto id is retrieved prior to creation
	// */
	//@Override
	//public int getAutoType() {
	//	return AUTO_POST;
	//}

	/**
	 * Gets the next sequence for a given MetaClass
	 */
        @Override
	protected String getLastAutoId( Connection conn, ColumnDef col ) throws SQLException
	{
		//String table = getManager().getTableName( mc );
		//if ( table == null )
		//	throw new MetaException( "MetaClass [" + mc + "] has no table defined" );

		//String col = getManager().getColumnName( mf );
		//if ( col == null )
		//	throw new MetaException( "MetaField [" + mf + "] has no column defined" );

		try
		{
			Statement s = conn.createStatement();
			try
			{
				String query =
					"SELECT IDENTITY_VAL_LOCAL() " +
					"FROM " + getProperName( col.getBaseTable().getNameDef() );

				ResultSet rs = s.executeQuery( query );

				try
				{
					if ( !rs.next() ) return "1";

					String tmp = rs.getString( 1 );

					if ( tmp == null ) return "1";

					int i = Integer.parseInt( tmp );

					return "" + i;
				}
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

	/**
	 * Gets the next sequence for a given MetaClass
	 */
	/*public synchronized String getNextFieldId( ObjectConnection c, MetaClass mc, MetaField mf )
			throws MetaException
		{
			//return "" + getNextSequence( getManager().getSequenceName( mf ));
			return super.getNextFieldId( c, mc, mf );
		}*/

	@Override
	public String getDateFormat() {
		return "yyyy-MM-dd HH:mm:ss";
	}

	///////////////////////////////////////////////////////
	// TO STRING METHOD
	public String toString()
	{
		return "Derby Database Driver";
	}
}
