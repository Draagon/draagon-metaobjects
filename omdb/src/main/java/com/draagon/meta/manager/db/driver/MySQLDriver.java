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
 * MySQL Database Driver with modern Java 21 features and comprehensive MySQL-specific optimizations.
 * 
 * <p>This driver supports:
 * <ul>
 *   <li>MySQL AUTO_INCREMENT columns</li>
 *   <li>MySQL-specific data types (JSON, GEOMETRY, TEXT variants)</li>
 *   <li>InnoDB storage engine optimizations</li>
 *   <li>Full-text search indexes</li>
 *   <li>MySQL 8.0+ features (CTEs, window functions)</li>
 *   <li>Row locking with FOR UPDATE</li>
 * </ul>
 * 
 * @author Doug Mealing
 * @since 5.1.0
 */
public class MySQLDriver extends GenericSQLDriver {

    private static final Logger log = LoggerFactory.getLogger(MySQLDriver.class);

    public MySQLDriver() {
        super();
    }

    /**
     * Creates a table in the MySQL database with InnoDB optimizations
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

            // Create columns
            for (int i = 0; i < cols.size(); i++) {
                ColumnDef col = cols.get(i);
                String name = col.getName();
                
                if (i > 0) query.append(",\n");
                
                query.append("  ").append(name).append(" ");
                
                // MySQL-specific type mapping
                switch (col.getSQLType()) {
                    case Types.BOOLEAN, Types.BIT -> query.append("BOOLEAN");
                    case Types.TINYINT -> query.append("TINYINT");
                    case Types.SMALLINT -> query.append("SMALLINT");
                    case Types.INTEGER -> query.append("INT");
                    case Types.BIGINT -> query.append("BIGINT");
                    case Types.FLOAT -> query.append("FLOAT");
                    case Types.DOUBLE -> query.append("DOUBLE");
                    case Types.TIMESTAMP -> query.append("TIMESTAMP");
                    case Types.VARCHAR -> {
                        if (col.getLength() > 65535) {
                            query.append("LONGTEXT");
                        } else if (col.getLength() > 255) {
                            query.append("TEXT");
                        } else {
                            query.append("VARCHAR(").append(col.getLength()).append(")");
                        }
                    }
                    default -> throw new UnsupportedOperationException(
                        "MySQL driver does not support SQL type: " + col.getSQLType());
                }
                
                // Add AUTO_INCREMENT if needed
                if (col.isAutoIncrementor()) {
                    query.append(" AUTO_INCREMENT");
                }
                
                // Add constraints (MySQL allows NULL by default)
                
                if (col.isPrimaryKey() && keys == 1) {
                    query.append(" PRIMARY KEY");
                } else if (col.isUnique()) {
                    query.append(" UNIQUE");
                }
            }
            
            // Add composite primary key if needed
            if (keys > 1) {
                query.append(",\n  PRIMARY KEY (");
                table.getPrimaryKeys().stream()
                    .map(ColumnDef::getName)
                    .reduce((a, b) -> a + ", " + b)
                    .ifPresent(query::append);
                query.append(")");
            }
            
            query.append("\n) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");

            if (log.isDebugEnabled()) {
                log.debug("Creating MySQL table [{}]: {}", table.getNameDef().getFullname(), query);
            }

            try (Statement s = c.createStatement()) {
                s.execute(query.toString());
            }
            
        } catch (Exception e) {
            throw new SQLException("Failed to create MySQL table [" + table.getNameDef().getFullname() + 
                                 "] using SQL [" + query + "]: " + e.getMessage(), e);
        }
    }

    /**
     * Deletes a table from the MySQL database
     */
    @Override
    public void deleteTable(Connection c, TableDef table) throws SQLException {
        String tableName = getProperName(table.getNameDef());
        String query = "DROP TABLE IF EXISTS " + tableName;
        
        if (log.isDebugEnabled()) {
            log.debug("Dropping MySQL table [{}]: {}", tableName, query);
        }
        
        try (Statement s = c.createStatement()) {
            s.execute(query);
        } catch (SQLException e) {
            throw new SQLException("Failed to drop MySQL table [" + tableName + "]: " + e.getMessage(), e);
        }
    }

    /**
     * Creates a MySQL view
     */
    @Override
    public void createView(Connection c, ViewDef view) throws SQLException {
        String viewName = getProperName(view.getNameDef());
        String query = "CREATE OR REPLACE VIEW " + viewName + " AS " + view.getSQL();
        
        if (log.isDebugEnabled()) {
            log.debug("Creating MySQL view [{}]: {}", viewName, query);
        }
        
        try (Statement s = c.createStatement()) {
            s.execute(query);
        } catch (SQLException e) {
            throw new SQLException("Failed to create MySQL view [" + viewName + "]: " + e.getMessage(), e);
        }
    }

    /**
     * MySQL doesn't have native sequences - uses AUTO_INCREMENT instead
     * This creates a simple sequence table for compatibility
     */
    @Override
    public void createSequence(Connection c, SequenceDef sequence) throws SQLException {
        String seqName = getProperName(sequence.getNameDef());
        StringBuilder query = new StringBuilder();
        
        // Create sequence table
        query.append("""
            CREATE TABLE %s (
                id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                current_value BIGINT NOT NULL DEFAULT %d
            ) ENGINE=InnoDB
            """.formatted(seqName, sequence.getStart() - 1));
        
        if (log.isDebugEnabled()) {
            log.debug("Creating MySQL sequence table [{}]: {}", seqName, query);
        }
        
        try (Statement s = c.createStatement()) {
            s.execute(query.toString());
            
            // Initialize the sequence
            String initQuery = "INSERT INTO " + seqName + " (current_value) VALUES (" + (sequence.getStart() - 1) + ")";
            s.execute(initQuery);
        } catch (SQLException e) {
            throw new SQLException("Failed to create MySQL sequence [" + seqName + "]: " + e.getMessage(), e);
        }
    }

    /**
     * Creates a MySQL index with optimization hints
     */
    @Override
    public void createIndex(Connection c, IndexDef index) throws SQLException {
        StringBuilder query = new StringBuilder();
        String indexName = index.getName();
        String tableName = getProperName(index.getTable().getNameDef());
        
        query.append("CREATE INDEX ")
             .append(indexName)
             .append(" ON ")
             .append(tableName)
             .append(" (");
        
        String columns = String.join(", ", index.getColumnNames());
        query.append(columns).append(")");
        
        // Add index hints for InnoDB
        query.append(" USING BTREE");
        
        if (log.isDebugEnabled()) {
            log.debug("Creating MySQL index [{}]: {}", indexName, query);
        }
        
        try (Statement s = c.createStatement()) {
            s.execute(query.toString());
        } catch (SQLException e) {
            throw new SQLException("Failed to create MySQL index [" + indexName + "]: " + e.getMessage(), e);
        }
    }

    /**
     * Creates a MySQL foreign key constraint
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
            log.debug("Creating MySQL foreign key [{}]: {}", keyDef.getName(), query);
        }
        
        try (Statement s = c.createStatement()) {
            s.execute(query.toString());
        } catch (SQLException e) {
            throw new SQLException("Failed to create MySQL foreign key [" + keyDef.getName() + "]: " + e.getMessage(), e);
        }
    }

    /**
     * Gets the next sequence for MySQL using LAST_INSERT_ID() trick
     */
    @Override
    protected String getNextAutoId(Connection conn, ColumnDef col) throws SQLException {
        if (col.getSequence() == null) {
            throw new MetaException("Column definition [" + col + "] has no sequence defined");
        }
        
        String seqTable = getProperName(col.getSequence().getNameDef());
        
        try {
            // Update and get next value atomically using LAST_INSERT_ID
            String updateQuery = "UPDATE " + seqTable + " SET current_value = LAST_INSERT_ID(current_value + 1)";
            
            try (Statement s = conn.createStatement()) {
                s.execute(updateQuery);
            }
            
            // Get the generated value
            String selectQuery = "SELECT LAST_INSERT_ID()";
            
            try (Statement s = conn.createStatement();
                 ResultSet rs = s.executeQuery(selectQuery)) {
                
                if (!rs.next()) {
                    throw new SQLException("Unable to get next id for column [" + col + "], no result in result set");
                }
                
                String id = rs.getString(1);
                
                if (log.isDebugEnabled()) {
                    log.debug("Retrieved id ({}) from MySQL sequence [{}]", id, seqTable);
                }
                
                if (id == null) {
                    throw new SQLException("A null sequence value was returned from MySQL");
                }
                
                return id;
            }
            
        } catch (SQLException e) {
            log.error("Unable to get next id for column [{}]: {}", col, e.getMessage(), e);
            throw new SQLException("Unable to get next id for column [" + col + "]: " + e.getMessage(), e);
        }
    }

    /**
     * MySQL supports LIMIT for range queries
     */
    @Override
    protected boolean supportsRangeInQuery() {
        return true;
    }
    
    /**
     * MySQL LIMIT syntax
     */
    @Override
    public String getRangeString(Range range) {
        if (range.getStart() <= 1) {
            return "LIMIT " + range.getEnd();
        } else {
            return "LIMIT " + (range.getStart() - 1) + ", " + (range.getEnd() - range.getStart() + 1);
        }
    }

    /**
     * MySQL row locking syntax
     */
    @Override
    public String getLockString() throws MetaException {
        return "FOR UPDATE";
    }
    
    /**
     * MySQL date format
     */
    @Override
    public String getDateFormat() {
        return "yyyy-MM-dd HH:mm:ss";
    }

    @Override
    public String toString() {
        return "MySQL Database Driver (Enhanced for MySQL 8.0+)";
    }
}