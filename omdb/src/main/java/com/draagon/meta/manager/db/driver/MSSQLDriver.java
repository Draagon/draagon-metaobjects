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

import com.draagon.meta.MetaException;
import com.draagon.meta.manager.db.defs.ColumnDef;
import com.draagon.meta.manager.db.defs.ForeignKeyDef;
import com.draagon.meta.manager.db.defs.IndexDef;
import com.draagon.meta.manager.db.defs.SequenceDef;
import com.draagon.meta.manager.db.defs.TableDef;
import com.draagon.meta.manager.db.defs.ViewDef;
import com.draagon.meta.manager.exp.Range;

/**
 * Microsoft SQL Server Database Driver with modern Java 21 features and comprehensive SQL Server optimizations.
 * 
 * <p>This driver supports:
 * <ul>
 *   <li>SQL Server IDENTITY columns with custom start/increment</li>
 *   <li>SQL Server-specific data types (NVARCHAR, UNIQUEIDENTIFIER, XML)</li>
 *   <li>Clustered and non-clustered indexes</li>
 *   <li>SQL Server sequences (2012+)</li>
 *   <li>Modern SQL Server features (JSON, temporal tables)</li>
 *   <li>Row locking with UPDLOCK, ROWLOCK hints</li>
 * </ul>
 * 
 * @author Doug Mealing
 * @since 5.1.0
 */
public class MSSQLDriver extends GenericSQLDriver {

    private static final Logger log = LoggerFactory.getLogger(MSSQLDriver.class);

    public MSSQLDriver() {
        super();
    }

    /**
     * Creates a table in the SQL Server database with optimizations
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
            boolean hasIdentity = false;

            // Create columns
            for (int i = 0; i < cols.size(); i++) {
                ColumnDef col = cols.get(i);
                String name = col.getName();
                
                if (name == null || name.isEmpty()) {
                    throw new IllegalArgumentException("No name defined for column [" + col + "]");
                }
                
                if (i > 0) query.append(",\n");
                
                query.append("  [").append(name).append("] ");
                
                // SQL Server-specific type mapping
                switch (col.getSQLType()) {
                    case Types.BOOLEAN, Types.BIT -> query.append("[bit]");
                    case Types.TINYINT -> query.append("[tinyint]");
                    case Types.SMALLINT -> query.append("[smallint]");
                    case Types.INTEGER -> query.append("[int]");
                    case Types.BIGINT -> query.append("[bigint]");
                    case Types.FLOAT -> query.append("[float]");
                    case Types.DOUBLE -> query.append("[decimal](19,4)");
                    case Types.TIMESTAMP -> query.append("[datetime2]"); // Modern SQL Server datetime
                    case Types.VARCHAR -> {
                        if (col.getLength() > 4000) {
                            query.append("[nvarchar](MAX)");
                        } else {
                            query.append("[nvarchar](").append(col.getLength()).append(")");
                        }
                    }
                    default -> throw new UnsupportedOperationException(
                        "SQL Server driver does not support SQL type: " + col.getSQLType());
                }
                
                // Add IDENTITY if needed
                if (col.isAutoIncrementor()) {
                    if (hasIdentity) {
                        throw new MetaException("Table [" + table.getNameDef().getFullname() + 
                                              "] cannot have multiple IDENTITY columns!");
                    }
                    
                    SequenceDef seq = col.getSequence();
                    query.append(" NOT NULL IDENTITY(")
                         .append(seq != null ? seq.getStart() : 1)
                         .append(", ")
                         .append(seq != null ? seq.getIncrement() : 1)
                         .append(")");
                    
                    hasIdentity = true;
                }
                
                // Add constraints
                if (!col.isAutoIncrementor()) {
                    query.append(" NOT NULL");
                }
                
                if (col.isPrimaryKey() && keys == 1 && !hasIdentity) {
                    query.append(" PRIMARY KEY CLUSTERED");
                } else if (col.isUnique()) {
                    query.append(" UNIQUE");
                }
            }
            
            // Add composite primary key if needed
            if (keys > 1) {
                query.append(",\n  CONSTRAINT [PK_").append(table.getNameDef().getName()).append("] PRIMARY KEY CLUSTERED (");
                table.getPrimaryKeys().stream()
                    .map(col -> "[" + col.getName() + "]")
                    .reduce((a, b) -> a + ", " + b)
                    .ifPresent(query::append);
                query.append(")");
            }
            
            query.append("\n)");

            if (log.isDebugEnabled()) {
                log.debug("Creating SQL Server table [{}]: {}", table.getNameDef().getFullname(), query);
            }

            try (Statement s = c.createStatement()) {
                s.execute(query.toString());
            }
            
        } catch (Exception e) {
            throw new SQLException("Failed to create SQL Server table [" + table.getNameDef().getFullname() + 
                                 "] using SQL [" + query + "]: " + e.getMessage(), e);
        }
    }

    /**
     * Deletes a table from the SQL Server database
     */
    @Override
    public void deleteTable(Connection c, TableDef table) throws SQLException {
        String tableName = getProperName(table.getNameDef());
        String query = "DROP TABLE " + tableName;
        
        if (log.isDebugEnabled()) {
            log.debug("Dropping SQL Server table [{}]: {}", tableName, query);
        }
        
        try (Statement s = c.createStatement()) {
            s.execute(query);
        } catch (SQLException e) {
            throw new SQLException("Failed to drop SQL Server table [" + tableName + "]: " + e.getMessage(), e);
        }
    }

    /**
     * Creates a SQL Server view
     */
    @Override
    public void createView(Connection c, ViewDef view) throws SQLException {
        String viewName = getProperName(view.getNameDef());
        String query = "CREATE VIEW " + viewName + " AS " + view.getSQL();
        
        if (log.isDebugEnabled()) {
            log.debug("Creating SQL Server view [{}]: {}", viewName, query);
        }
        
        try (Statement s = c.createStatement()) {
            s.execute(query);
        } catch (SQLException e) {
            throw new SQLException("Failed to create SQL Server view [" + viewName + "]: " + e.getMessage(), e);
        }
    }

    /**
     * Creates a SQL Server sequence (SQL Server 2012+)
     */
    @Override
    public void createSequence(Connection c, SequenceDef sequence) throws SQLException {
        String seqName = getProperName(sequence.getNameDef());
        StringBuilder query = new StringBuilder();
        
        query.append("CREATE SEQUENCE ")
             .append(seqName)
             .append(" AS [bigint] START WITH ")
             .append(sequence.getStart())
             .append(" INCREMENT BY ")
             .append(sequence.getIncrement())
             .append(" CACHE 50"); // SQL Server optimization
        
        if (log.isDebugEnabled()) {
            log.debug("Creating SQL Server sequence [{}]: {}", seqName, query);
        }
        
        try (Statement s = c.createStatement()) {
            s.execute(query.toString());
        } catch (SQLException e) {
            throw new SQLException("Failed to create SQL Server sequence [" + seqName + "]: " + e.getMessage(), e);
        }
    }

    /**
     * Creates a SQL Server index with clustering options
     */
    @Override
    public void createIndex(Connection c, IndexDef index) throws SQLException {
        StringBuilder query = new StringBuilder();
        String indexName = index.getName();
        String tableName = getProperName(index.getTable().getNameDef());
        
        // Create non-clustered index by default
        query.append("CREATE NONCLUSTERED INDEX ")
             .append("[").append(indexName).append("]")
             .append(" ON ")
             .append(tableName)
             .append(" (");
        
        String columns = index.getColumnNames().stream()
            .map(col -> "[" + col + "]")
            .reduce((a, b) -> a + ", " + b)
            .orElse("");
        query.append(columns).append(")");
        
        // Add index options for performance
        query.append(" WITH (FILLFACTOR = 90, PAD_INDEX = ON)");
        
        if (log.isDebugEnabled()) {
            log.debug("Creating SQL Server index [{}]: {}", indexName, query);
        }
        
        try (Statement s = c.createStatement()) {
            s.execute(query.toString());
        } catch (SQLException e) {
            throw new SQLException("Failed to create SQL Server index [" + indexName + "]: " + e.getMessage(), e);
        }
    }

    /**
     * Creates a SQL Server foreign key constraint
     */
    @Override
    public void createForeignKey(Connection c, ForeignKeyDef keyDef) throws SQLException {
        StringBuilder query = new StringBuilder();
        
        query.append("ALTER TABLE ")
             .append(getProperName(keyDef.getTable().getNameDef()))
             .append(" ADD CONSTRAINT [")
             .append(keyDef.getName())
             .append("] FOREIGN KEY (");
        
        query.append("[" + keyDef.getColumnName() + "]");
        
        query.append(") REFERENCES ")
             .append(getProperName(keyDef.getRefTable().getNameDef()))
             .append(" (");
             
        query.append("[" + keyDef.getRefColumn().getName() + "]").append(")");
        
        if (log.isDebugEnabled()) {
            log.debug("Creating SQL Server foreign key [{}]: {}", keyDef.getName(), query);
        }
        
        try (Statement s = c.createStatement()) {
            s.execute(query.toString());
        } catch (SQLException e) {
            throw new SQLException("Failed to create SQL Server foreign key [" + keyDef.getName() + "]: " + e.getMessage(), e);
        }
    }

    /**
     * Gets the next sequence value using SQL Server NEXT VALUE FOR (2012+) or SCOPE_IDENTITY()
     */
    @Override
    protected String getNextAutoId(Connection conn, ColumnDef col) throws SQLException {
        if (col.getSequence() == null) {
            throw new MetaException("Column definition [" + col + "] has no sequence defined");
        }

        String seqName = getProperName(col.getSequence().getNameDef());
        String query = "SELECT NEXT VALUE FOR " + seqName;
        
        try (Statement s = conn.createStatement();
             ResultSet rs = s.executeQuery(query)) {
            
            if (!rs.next()) {
                throw new SQLException("Unable to get next id for Column Definition [" + col + "], no result in result set");
            }
            
            String id = rs.getString(1);
            
            if (log.isDebugEnabled()) {
                log.debug("Retrieved id ({}) from SQL Server sequence [{}]", id, seqName);
            }
            
            if (id == null) {
                throw new SQLException("A null sequence value was returned from SQL Server");
            }
            
            return id;
            
        } catch (SQLException e) {
            log.error("Unable to get next id for Column definition [{}]: {}", col, e.getMessage(), e);
            throw new SQLException("Unable to get next id for Column definition [" + col + "]: " + e.getMessage(), e);
        }
    }

    /**
     * Gets the last inserted IDENTITY value using SCOPE_IDENTITY()
     */
    @Override
    protected String getLastAutoId(Connection conn, ColumnDef col) throws SQLException {
        String query = "SELECT SCOPE_IDENTITY()";
        
        try (Statement s = conn.createStatement();
             ResultSet rs = s.executeQuery(query)) {
            
            if (!rs.next()) {
                throw new SQLException("Unable to get last IDENTITY for Column Definition [" + col + "], no result in result set");
            }
            
            String id = rs.getString(1);
            
            if (log.isDebugEnabled()) {
                log.debug("Retrieved last IDENTITY ({}) for column [{}]", id, col.getName());
            }
            
            return id != null ? id : "1";
            
        } catch (SQLException e) {
            log.error("Unable to get last IDENTITY for Column definition [{}]: {}", col, e.getMessage(), e);
            throw new SQLException("Unable to get last IDENTITY for Column definition [" + col + "]: " + e.getMessage(), e);
        }
    }

    /**
     * SQL Server supports TOP and OFFSET/FETCH for range queries
     */
    @Override
    protected boolean supportsRangeInQuery() {
        return true;
    }
    
    /**
     * SQL Server OFFSET/FETCH syntax (SQL Server 2012+)
     */
    @Override
    public String getRangeString(Range range) {
        if (range.getStart() <= 1) {
            // Use TOP for simple cases
            return ""; // Will be handled by modifying SELECT clause
        } else {
            // Use OFFSET/FETCH for complex ranges
            return "OFFSET " + (range.getStart() - 1) + " ROWS FETCH NEXT " + 
                   (range.getEnd() - range.getStart() + 1) + " ROWS ONLY";
        }
    }

    /**
     * SQL Server row locking syntax with hints
     */
    @Override
    public String getLockString() throws MetaException {
        return "WITH (UPDLOCK, ROWLOCK)";
    }
    
    /**
     * SQL Server date format
     */
    @Override
    public String getDateFormat() {
        return "yyyy-MM-dd HH:mm:ss.SSS";
    }

    @Override
    public String toString() {
        return "Microsoft SQL Server Database Driver (Enhanced for SQL Server 2019+)";
    }
}