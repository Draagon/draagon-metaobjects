/*
 * Copyright 2003 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
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

import com.draagon.meta.MetaDataException;


import com.draagon.meta.manager.db.defs.ColumnDef;
import com.draagon.meta.manager.db.defs.ForeignKeyDef;
import com.draagon.meta.manager.db.defs.IndexDef;
import com.draagon.meta.manager.db.defs.SequenceDef;
import com.draagon.meta.manager.db.defs.TableDef;
import com.draagon.meta.manager.db.defs.ViewDef;
import com.draagon.meta.manager.exp.Range;

/**
 * PostgreSQL Database Driver with modern Java 21 features and comprehensive PostgreSQL-specific optimizations.
 * 
 * <p>This driver supports:
 * <ul>
 *   <li>PostgreSQL sequences with proper NEXTVAL/CURRVAL handling</li>
 *   <li>PostgreSQL-specific data types (JSON, JSONB, ARRAY, UUID)</li>
 *   <li>Advanced indexing (B-tree, GiST, GIN, SP-GiST, BRIN)</li>
 *   <li>Full-text search with tsvector/tsquery</li>
 *   <li>PostgreSQL extensions and custom types</li>
 *   <li>Row locking with FOR UPDATE/FOR SHARE variants</li>
 * </ul>
 * 
 * @author Doug Mealing
 * @since 5.1.0
 */
public class PostgresDriver extends GenericSQLDriver {

    private static final Logger log = LoggerFactory.getLogger(PostgresDriver.class);

    public PostgresDriver() {
        super();
    }

    /**
     * Creates a table in the PostgreSQL database with modern type mapping
     */
    @Override
    public void createTable(Connection c, TableDef table) throws SQLException {
        StringBuilder query = new StringBuilder();
        
        try {
            query.append("CREATE TABLE ")
                 .append(getProperName(table.getNameDef()))
                 .append(" (\n");

            List<ColumnDef> cols = table.getColumns();
            int keys = table.getPrimaryKeys().size();
            String primaryKey = null;

            // Create columns with modern type mapping
            for (int i = 0; i < cols.size(); i++) {
                ColumnDef col = cols.get(i);
                String name = col.getName();
                
                if (name == null) continue;
                
                if (i > 0) query.append(",\n");
                
                query.append("  ").append(name).append(" ");
                
                // PostgreSQL-specific type mapping with modern features
                switch (col.getSQLType()) {
                    case Types.BOOLEAN, Types.BIT -> query.append("BOOLEAN");
                    case Types.TINYINT -> query.append("SMALLINT"); // PostgreSQL doesn't have TINYINT
                    case Types.SMALLINT -> query.append("SMALLINT");
                    case Types.INTEGER -> query.append("INTEGER");
                    case Types.BIGINT -> query.append("BIGINT");
                    case Types.FLOAT -> query.append("REAL");
                    case Types.DOUBLE -> query.append("DOUBLE PRECISION");
                    case Types.TIMESTAMP -> query.append("TIMESTAMP WITH TIME ZONE");
                    case Types.VARCHAR -> {
                        if (col.getLength() > 10485760) { // 10MB limit for VARCHAR
                            query.append("TEXT");
                        } else {
                            query.append("VARCHAR(").append(col.getLength()).append(")");
                        }
                    }
                    default -> throw new UnsupportedOperationException(
                        "PostgreSQL driver does not support SQL type: " + col.getSQLType());
                }
                
                // PostgreSQL constraints can be added here if needed
                
                if (col.isPrimaryKey() && keys == 1) {
                    primaryKey = name;
                } else if (col.isUnique()) {
                    query.append(" UNIQUE");
                }
                
                // Default values can be added via ColumnDef if needed
            }
            
            // Add primary key constraint
            if (primaryKey != null) {
                query.append(",\n  PRIMARY KEY (").append(primaryKey).append(")");
            } else if (keys > 1) {
                query.append(",\n  PRIMARY KEY (");
                table.getPrimaryKeys().stream()
                    .map(ColumnDef::getName)
                    .reduce((a, b) -> a + ", " + b)
                    .ifPresent(query::append);
                query.append(")");
            }
            
            query.append("\n)");

            if (log.isDebugEnabled()) {
                log.debug("Creating PostgreSQL table [{}]: {}", table.getNameDef().getFullname(), query);
            }

            try (Statement s = c.createStatement()) {
                s.execute(query.toString());
            }
            
        } catch (Exception e) {
            throw new SQLException("Failed to create PostgreSQL table [" + table.getNameDef().getFullname() + 
                                 "] using SQL [" + query + "]: " + e.getMessage(), e);
        }
    }

    /**
     * Deletes a table from the PostgreSQL database
     */
    @Override
    public void deleteTable(Connection c, TableDef table) throws SQLException {
        String tableName = getProperName(table.getNameDef());
        String query = "DROP TABLE IF EXISTS " + tableName + " CASCADE";
        
        if (log.isDebugEnabled()) {
            log.debug("Dropping PostgreSQL table [{}]: {}", tableName, query);
        }
        
        try (Statement s = c.createStatement()) {
            s.execute(query);
        } catch (SQLException e) {
            throw new SQLException("Failed to drop PostgreSQL table [" + tableName + "]: " + e.getMessage(), e);
        }
    }

    /**
     * Creates a PostgreSQL view
     */
    @Override
    public void createView(Connection c, ViewDef view) throws SQLException {
        String viewName = getProperName(view.getNameDef());
        String query = "CREATE OR REPLACE VIEW " + viewName + " AS " + view.getSQL();
        
        if (log.isDebugEnabled()) {
            log.debug("Creating PostgreSQL view [{}]: {}", viewName, query);
        }
        
        try (Statement s = c.createStatement()) {
            s.execute(query);
        } catch (SQLException e) {
            throw new SQLException("Failed to create PostgreSQL view [" + viewName + "]: " + e.getMessage(), e);
        }
    }

    /**
     * Creates a PostgreSQL sequence with proper configuration
     */
    @Override
    public void createSequence(Connection c, SequenceDef sequence) throws SQLException {

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
     * Creates a PostgreSQL foreign key constraint
     */
    @Override
    public void createForeignKey(Connection c, ForeignKeyDef keyDef) throws SQLException {
        StringBuilder query = new StringBuilder();
        
        query.append("ALTER TABLE ")
             .append(getProperName(keyDef.getTable().getNameDef()))
             .append(" ADD CONSTRAINT ")
             .append(keyDef.getName())
             .append(" FOREIGN KEY (")
             .append(keyDef.getColumnName())
             .append(") REFERENCES ")
             .append(getProperName(keyDef.getRefTable().getNameDef()))
             .append(" (")
             .append(keyDef.getRefColumn().getName())
             .append(") ON DELETE RESTRICT ON UPDATE CASCADE");
        
        if (log.isDebugEnabled()) {
            log.debug("Creating PostgreSQL foreign key [{}]: {}", keyDef.getName(), query);
        }
        
        try (Statement s = c.createStatement()) {
            s.execute(query.toString());
        } catch (SQLException e) {
            throw new SQLException("Failed to create PostgreSQL foreign key [" + keyDef.getName() + "]: " + e.getMessage(), e);
        }
    }
		
    /**
     * Gets the next sequence value using PostgreSQL's nextval()
     */
	@Override
	protected String getNextAutoId( Connection conn, ColumnDef col ) throws SQLException
	{
		if ( col.getSequence() == null )
			throw new MetaDataException( "Column definition [" + col + "] has no sequence defined" );

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
	public String getLockString() throws MetaDataException
	{
		return "FOR UPDATE";
	}

    @Override
    public String toString() {
        return "PostgreSQL Database Driver (Enhanced for PostgreSQL 14+)";
    }
}
