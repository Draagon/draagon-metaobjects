/*
 * Copyright 2003 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta.manager.db.driver;

import java.sql.Connection;
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
 * Apache Derby Database Driver with modern Java 21 features and Derby-specific optimizations.
 * 
 * <p>This driver supports:
 * <ul>
 *   <li>Derby IDENTITY columns with custom start/increment</li>
 *   <li>Derby-specific data types and constraints</li>
 *   <li>Embedded and network Derby configurations</li>
 *   <li>Derby sequences and generated columns</li>
 *   <li>Row locking and transaction isolation</li>
 *   <li>Derby system procedures and functions</li>
 * </ul>
 * 
 * @author Doug Mealing
 * @since 5.1.0
 */
public class DerbyDriver extends GenericSQLDriver {

    private static final Logger log = LoggerFactory.getLogger(DerbyDriver.class);
    
    public DerbyDriver() {
        super();
    }

    /**
     * Creates a table in the Derby database with modern features
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

            // Create columns with Derby-specific optimizations
            for (int i = 0; i < cols.size(); i++) {
                ColumnDef col = cols.get(i);
                String name = col.getName();
                
                if (name == null) continue;
                
                if (i > 0) query.append(",\n");
                
                query.append("  ").append(name).append(" ");
                
                // Derby-specific type mapping
                switch (col.getSQLType()) {
                    case Types.BOOLEAN, Types.BIT -> query.append("BOOLEAN");
                    case Types.TINYINT, Types.SMALLINT -> query.append("SMALLINT");
                    case Types.INTEGER -> query.append("INTEGER");
                    case Types.BIGINT -> query.append("BIGINT");
                    case Types.FLOAT -> query.append("REAL");
                    case Types.DOUBLE -> query.append("DOUBLE");
                    case Types.TIMESTAMP -> query.append("TIMESTAMP");
                    case Types.VARCHAR -> {
                        if (col.getLength() > 32700) {
                            query.append("CLOB");
                        } else {
                            query.append("VARCHAR(").append(col.getLength()).append(")");
                        }
                    }
                    default -> throw new UnsupportedOperationException(
                        "Derby driver does not support SQL type: " + col.getSQLType());
                }
                
                // Add IDENTITY constraint if needed
                if (col.isAutoIncrementor()) {
                    SequenceDef seq = col.getSequence();
                    query.append(" GENERATED ALWAYS AS IDENTITY");
                    if (seq != null) {
                        query.append(" (START WITH ").append(seq.getStart())
                             .append(", INCREMENT BY ").append(seq.getIncrement()).append(")");
                    }
                    hasIdentity = true;
                }
                
                // Add constraints
                if (!col.isAutoIncrementor()) {
                    query.append(" NOT NULL");
                }
                
                if (col.isPrimaryKey() && keys == 1) {
                    query.append(" CONSTRAINT ")
                         .append(table.getNameDef().getName()).append("_")
                         .append(name).append("_PK PRIMARY KEY");
                } else if (col.isUnique()) {
                    query.append(" UNIQUE");
                }
            }
            
            // Add composite primary key if needed
            if (keys > 1) {
                query.append(",\n  CONSTRAINT ")
                     .append(table.getNameDef().getName())
                     .append("_PK PRIMARY KEY (");
                table.getPrimaryKeys().stream()
                    .map(ColumnDef::getName)
                    .reduce((a, b) -> a + ", " + b)
                    .ifPresent(query::append);
                query.append(")");
            }
            
            query.append("\n)");

            if (log.isDebugEnabled()) {
                log.debug("Creating Derby table [{}]: {}", table.getNameDef().getFullname(), query);
            }

            try (Statement s = c.createStatement()) {
                s.execute(query.toString());
            }
            
        } catch (Exception e) {
            throw new SQLException("Failed to create Derby table [" + table.getNameDef().getFullname() + 
                                 "] using SQL [" + query + "]: " + e.getMessage(), e);
        }
    }

    /**
     * Deletes a table from the Derby database
     */
    @Override
    public void deleteTable(Connection c, TableDef table) throws SQLException {
        String tableName = getProperName(table.getNameDef());
        String query = "DROP TABLE " + tableName;
        
        if (log.isDebugEnabled()) {
            log.debug("Dropping Derby table [{}]: {}", tableName, query);
        }
        
        try (Statement s = c.createStatement()) {
            s.execute(query);
        } catch (SQLException e) {
            throw new SQLException("Failed to drop Derby table [" + tableName + "]: " + e.getMessage(), e);
        }
    }

    /**
     * Creates a Derby view
     */
    @Override
    public void createView(Connection c, ViewDef view) throws SQLException {
        String viewName = getProperName(view.getNameDef());
        String query = "CREATE VIEW " + viewName + " AS " + view.getSQL();
        
        if (log.isDebugEnabled()) {
            log.debug("Creating Derby view [{}]: {}", viewName, query);
        }
        
        try (Statement s = c.createStatement()) {
            s.execute(query);
        } catch (SQLException e) {
            throw new SQLException("Failed to create Derby view [" + viewName + "]: " + e.getMessage(), e);
        }
    }

    /**
     * Creates a Derby sequence
     */
    @Override
    public void createSequence(Connection c, SequenceDef sequence) throws SQLException {
        String seqName = getProperName(sequence.getNameDef());
        StringBuilder query = new StringBuilder();
        
        query.append("CREATE SEQUENCE ")
             .append(seqName)
             .append(" AS BIGINT START WITH ")
             .append(sequence.getStart())
             .append(" INCREMENT BY ")
             .append(sequence.getIncrement());
        
        if (log.isDebugEnabled()) {
            log.debug("Creating Derby sequence [{}]: {}", seqName, query);
        }
        
        try (Statement s = c.createStatement()) {
            s.execute(query.toString());
        } catch (SQLException e) {
            throw new SQLException("Failed to create Derby sequence [" + seqName + "]: " + e.getMessage(), e);
        }
    }

    /**
     * Creates a Derby index
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
        
        if (log.isDebugEnabled()) {
            log.debug("Creating Derby index [{}]: {}", indexName, query);
        }
        
        try (Statement s = c.createStatement()) {
            s.execute(query.toString());
        } catch (SQLException e) {
            throw new SQLException("Failed to create Derby index [" + indexName + "]: " + e.getMessage(), e);
        }
    }

    /**
     * Creates a Derby foreign key constraint
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
             .append(")");
        
        if (log.isDebugEnabled()) {
            log.debug("Creating Derby foreign key [{}]: {}", keyDef.getName(), query);
        }
        
        try (Statement s = c.createStatement()) {
            s.execute(query.toString());
        } catch (SQLException e) {
            throw new SQLException("Failed to create Derby foreign key [" + keyDef.getName() + "]: " + e.getMessage(), e);
        }
    }
    /**
     * Gets the last inserted IDENTITY value using Derby's IDENTITY_VAL_LOCAL()
     */
    @Override
    protected String getLastAutoId(Connection conn, ColumnDef col) throws SQLException {
        String query = "SELECT IDENTITY_VAL_LOCAL() FROM " + getProperName(col.getBaseTable().getNameDef());
        
        try (Statement s = conn.createStatement();
             ResultSet rs = s.executeQuery(query)) {
            
            if (!rs.next()) {
                return "1";
            }
            
            String tmp = rs.getString(1);
            if (tmp == null) {
                return "1";
            }
            
            if (log.isDebugEnabled()) {
                log.debug("Retrieved last IDENTITY ({}) for Derby column [{}]", tmp, col.getName());
            }
            
            return tmp;
            
        } catch (SQLException e) {
            log.error("Unable to get last id for Derby column [{}]: {}", col, e.getMessage(), e);
            throw new SQLException("Unable to get last id for Derby column [" + col + "]: " + e.getMessage(), e);
        }
    }

    /**
     * Derby supports FETCH FIRST/OFFSET for range queries (Derby 10.5+)
     */
    @Override
    protected boolean supportsRangeInQuery() {
        return true;
    }
    
    /**
     * Derby OFFSET/FETCH syntax
     */
    @Override
    public String getRangeString(Range range) {
        if (range.getStart() <= 1) {
            return "FETCH FIRST " + range.getEnd() + " ROWS ONLY";
        } else {
            return "OFFSET " + (range.getStart() - 1) + " ROWS FETCH NEXT " + 
                   (range.getEnd() - range.getStart() + 1) + " ROWS ONLY";
        }
    }

    /**
     * Derby row locking syntax
     */
    @Override
    public String getLockString() throws MetaDataException {
        return "FOR UPDATE";
    }
    
    /**
     * Derby date format
     */
    @Override
    public String getDateFormat() {
        return "yyyy-MM-dd HH:mm:ss";
    }

    @Override
    public String toString() {
        return "Apache Derby Database Driver (Enhanced for Derby 10.15+)";
    }
}
