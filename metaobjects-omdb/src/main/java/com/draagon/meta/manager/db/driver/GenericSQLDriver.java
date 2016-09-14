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
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.draagon.meta.object.MetaObject;
import com.draagon.meta.MetaException;
import com.draagon.meta.field.MetaField;
import com.draagon.meta.manager.QueryOptions;
import com.draagon.meta.manager.db.DatabaseDriver;
import com.draagon.meta.manager.db.ObjectManagerDB;
import com.draagon.meta.manager.db.ObjectMappingDB;
import com.draagon.meta.manager.db.SubSelectValue;
import com.draagon.meta.manager.db.defs.BaseDef;
import com.draagon.meta.manager.db.defs.BaseTableDef;
import com.draagon.meta.manager.db.defs.ColumnDef;
import com.draagon.meta.manager.db.defs.ForeignKeyDef;
import com.draagon.meta.manager.db.defs.IndexDef;
import com.draagon.meta.manager.db.defs.InheritenceDef;
import com.draagon.meta.manager.db.defs.NameDef;
import com.draagon.meta.manager.db.defs.SequenceDef;
import com.draagon.meta.manager.db.defs.TableDef;
import com.draagon.meta.manager.db.defs.ViewDef;
import com.draagon.meta.manager.exp.Expression;
import com.draagon.meta.manager.exp.ExpressionGroup;
import com.draagon.meta.manager.exp.ExpressionOperator;
import com.draagon.meta.manager.exp.Range;
import com.draagon.meta.manager.exp.SortOrder;

/**
 * The Object Manager Base is able to add, update, delete, and retrieve objects
 * of those types from a datastore.
 */
public class GenericSQLDriver implements DatabaseDriver {

    private static Log log = LogFactory.getLog(GenericSQLDriver.class);
    private ObjectManagerDB mManager = null;

    /**
     * This is to handle arguments for the constructed SQL query to ensure
     * proper type conversion
     *
     * @author Doug
     */
    public class SQLArg {

        private MetaField metaField = null;
        private Object value = null;

        public SQLArg(MetaField f, Object value) {
            this.metaField = f;
            this.value = value;
        }

        public MetaField getMetaField() {
            return metaField;
        }

        public Object getValue() {
            return value;
        }
    }

    public GenericSQLDriver() {
    }

    public void setManager(ObjectManagerDB man) {
        mManager = man;
    }

    /**
     * Returns the Object Manager
     */
    public ObjectManagerDB getManager() {
        return mManager;
    }

    // /**
    // * Returns whether the auto id is retrieved prior to creation
    // */
    // @Override
    // public int getAutoType() {
    // return AUTO_PRIOR;
    // }
    @Override
    public boolean checkTable(Connection c, TableDef table) throws SQLException {
        return checkBaseTable(c, table);
    }

    @Override
    public boolean checkView(Connection c, ViewDef view) throws SQLException {
        return checkBaseTable(c, view);
    }

    /**
     * Checks for the existence of the base table and optionally creates it if
     * it doesn't exist
     *
     * @param c Database connection to use
     * @param baseTable Base Table Definition (Table or View)
     * @param autoCreate Whether to auto create the table or view
     * @return Whether the table or view exists
     * @throws SQLException Exception if it exists in an invalid format
     */
    protected boolean checkBaseTable(Connection c, BaseTableDef baseTable)
            throws SQLException {
        String schema = baseTable.getNameDef().getSchema();
        String name = baseTable.getNameDef().getName().toUpperCase();

        // VALIDATE TABLE OR VIEW
        ResultSet rs = c.getMetaData().getTables(null, schema, name, null);
        try {
            boolean found = false;
            while (rs.next()) {
                if (name.equalsIgnoreCase(rs.getString(3))) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                // throw new TableDoesNotExistException( "Table or View [" +
                // baseTable.getNameDef() + "] does not exist" );
                return false;
            }
        } finally {
            rs.close();
        }

        // VALIDATE THE COLUMNS
        for (ColumnDef col : baseTable.getColumns()) {
            rs = c.getMetaData().getColumns(null, schema, name, col.getName().toUpperCase());
            try {
                boolean found = false;
                while (rs.next()) {
                    if (col.getName().equalsIgnoreCase(rs.getString(4))) {
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    throw new SQLException("Table or View ["
                            + baseTable.getNameDef()
                            + "] does not have a Column [" + col.getName()
                            + "]");
                }
            } finally {
                rs.close();
            }
        }

        return true;
    }

    protected String getLastAutoId(Connection conn, ColumnDef col) throws SQLException {
        throw new IllegalStateException("This should not get called");
    }

    /**
     * Gets the next sequence for a given MetaClass
     */
    protected String getNextAutoId(Connection conn, ColumnDef col) throws SQLException {
        // String table = getManager().getTableName( mc );
        // if ( table == null )
        // throw new MetaException( "MetaClass [" + mc + "] has no table
        // defined" );

        // String col = getManager().getColumnName( mf );
        // if ( col == null )
        // throw new MetaException( "MetaField [" + mf + "] has no column
        // defined" );

        StringBuilder query = new StringBuilder();
        try {
            // Get next next MAX() sequence
            Statement s = conn.createStatement();
            try {
                query.append("SELECT MAX( ").append(col.getName()).append(" )")
                        .append("FROM ").append(
                        getProperName(col.getBaseTable().getNameDef()));

                ResultSet rs = s.executeQuery(query.toString());

                try {
                    if (!rs.next()) {
                        return "1";
                    }

                    String tmp = rs.getString(1);

                    if (tmp == null) {
                        return "1";
                    }

                    int i = Integer.parseInt(tmp) + 1;

                    return "" + i;
                } finally {
                    rs.close();
                }
            } finally {
                s.close();
            }
        } catch (SQLException e) {
            log.error("Unable to get next id for column [" + col
                    + "] with query [" + query + "]: " + e.getMessage(), e);
            throw new SQLException("Unable to get next id for column [" + col
                    + "]: " + e.getMessage(), e);
        }
    }

    /**
     * Creates a table in the database
     */
    @Override
    public void createTable(Connection c, TableDef tableDef)
            throws SQLException {
        throw new UnsupportedOperationException("CREATE TABLE NOT IMPLEMENTED!");
    }

    /**
     * Deletes a table from the database
     */
    @Override
    public void deleteTable(Connection c, TableDef tableDef)
            throws SQLException {
        throw new UnsupportedOperationException("DELETE TABLE NOT IMPLEMENTED!");
    }

/**
	 * Creates a view in the database
	 */
	@Override
	public void createView( Connection c, ViewDef view ) throws SQLException
	{
            StringBuilder query = new StringBuilder();
            try {
                String name = getProperName( view.getNameDef() );
                query.append( "CREATE VIEW " )
                    .append( name )
                    .append( " AS " )
                    .append( view.getSQL() );

                if ( log.isDebugEnabled() ) {
                    log.debug( "Creating view: " + query.toString() );
                }
                //ystem.out.println( ">>>> Creating View: " + query);

                Statement s = c.createStatement();
                try {
                     s.execute( query.toString() );
                } finally {
                     s.close();
                }
            }
            catch (Exception e) {
                    throw new SQLException( "Creation of view [" + view + "] failed using SQL [" + query + "]: " + e.getMessage(), e );
            }
    }


    /**
     * Creates the sequence in the database
     */
    public void createSequence(Connection c, SequenceDef sequenceDef)
            throws SQLException {
        throw new UnsupportedOperationException(
                "CREATE SEQUENCE NOT IMPLEMENTED!");
    }

    /**
     * Creates the index in the database
     */
    public void createIndex(Connection c, IndexDef indexDef)
            throws SQLException {
        // Do Nothing
    }

    /**
     * Creates the foreign keys for the table in the database
     */
    @Override
    public void createForeignKey(Connection c, ForeignKeyDef foreignKeyDef)
            throws SQLException {
        // DO Nothing
    }

    /**
     * Returns the proper name of the table or view
     *
     * @param nameDef
     * @return
     */
    public String getProperName(NameDef nameDef) {
        return nameDef.getFullname();
    }

    @Override
    public boolean create(Connection c, MetaObject mc, ObjectMappingDB omdb,
            Object o) throws SQLException {

        // Check if there is table inheritence going on, and if so create the super table first
        BaseDef base = omdb.getDBDef();
        if (base instanceof TableDef) {
            TableDef table = (TableDef) base;

            InheritenceDef inheritence = table.getInheritence();
            if (inheritence != null) {
                ObjectMappingDB smom = (ObjectMappingDB) omdb.getSuperMapping();

                // Set the discriminator values
                if (inheritence.getDiscriminatorName() != null) {
                    MetaField df = omdb.getField(inheritence.getDiscriminatorName());
                    df.setString(o, inheritence.getDiscriminatorValue());
                }

                // Create the super classes table entry
                if (!create(c, mc, smom, o)) {
                    throw new SQLException("Super table entry could not be created for mapping [" + smom + "]");
                }

                // Set the mapping field
                MetaField rf = omdb.getField(inheritence.getRefColumn());
                MetaField f = omdb.getField(inheritence.getColumnName());
                f.setObject(o, rf.getObject(o));
            }
        }

        // Now create the entry for this object
        PreparedStatement s = getInsertStatement(c, mc, omdb, o);

        try {
            s.execute();

            if (s.getUpdateCount() < 1) {
                return false;
            } else {

                setLastIds( c, mc, omdb, o);

                return true;
            }
        } finally {
            s.close();
        }
    }

    /** 
     * Sets any ids that were auto populated 
     */
    protected void setLastIds( Connection c, MetaObject mc, ObjectMappingDB omdb, Object o ) throws SQLException {
        
        for (MetaField f : omdb.getMetaFields() ) {

            ColumnDef colDef = (ColumnDef) omdb.getArgDef(f);        

            if ( colDef.getAutoType() == ColumnDef.AUTO_LAST_ID ) {
                f.setString( o, getLastAutoId(c, colDef));
            }
        }
    }
    
    @Override
    public boolean read(Connection c, MetaObject mc, ObjectMappingDB mapping,
            Object o, Expression exp) throws SQLException {

        // Get the fields to read
        Collection<MetaField> fields = mapping.getMetaFields();

        QueryOptions qo = new QueryOptions(exp);
        qo.setRange(1, 1);

        PreparedStatement s = getSelectStatementWhere(c, mc, mapping, fields, qo);

        try {
            ResultSet rs = s.executeQuery();

            try {
                if (rs.next()) {

                    // Parse the Object
                    parseObject(rs, fields, mc, o);

                    return true;
                }

                return false;
            } finally {
                rs.close();
            }
        } finally {
            s.close();
        }
    }

    @Override
    public boolean update(Connection c, MetaObject mc, ObjectMappingDB omdb,
            Object o, Collection<MetaField> fields, Collection<MetaField> keys,
            MetaField dirtyField, Object dirtyValue) throws SQLException {

        Expression exp = null;

        // Check if there is table inheritence going on, and if so delete the super table first
        BaseDef base = omdb.getDBDef();
        if (base instanceof TableDef) {
            TableDef table = (TableDef) base;

            InheritenceDef inheritence = table.getInheritence();
            if (inheritence != null) {

                ObjectMappingDB smom = (ObjectMappingDB) omdb.getSuperMapping();

                // Setup the key
                MetaField rf = omdb.getField(inheritence.getRefColumn());
                Collection<MetaField> pkeys = new ArrayList<MetaField>();
                pkeys.add(rf);

                // Create the super classes table entry
                if (!update(c, mc, smom, o, fields, pkeys, dirtyField, dirtyValue)) {
                    return false;
                    // throw new SQLException( "Super table entry could not be updated for mapping [" + smom + "]" );
                }

                // Set the mapping field
                MetaField f = omdb.getField(inheritence.getColumnName());

                // Set the expression to delete
                exp = new Expression(f.getName(), rf.getObject(o));

                // Can only have dirty fields on the highest level of inheritence
                dirtyField = null;
                dirtyValue = null;
            }
        }

        // If there wasn't inheritence, then generate the delete where clause
        if (exp == null) {
            // Generate the keys expression
            for (MetaField mf : keys) {
                //if ( omdb.isInThisMap( mf )) {
                Expression e = new Expression(mf.getName(), mf.getObject(o));
                if (exp == null) {
                    exp = e;
                } else {
                    exp = exp.and(e);
                }
                //}
            }
        }

        // Add the dirty field expression
        if (dirtyField != null) {

            Expression e = new Expression(dirtyField.getName(), dirtyValue);
            if (exp == null) {
                exp = e;
            } else {
                exp = exp.and(e);
            }
        }

        //setAutoFields( c, mc, omdb, fields, o, ObjectManager.UPDATE );

        Collection<MetaField> fieldsInMap = new ArrayList<MetaField>();
        for (MetaField mf : fields) {
            if (omdb.isInThisMap(mf)) {
                fieldsInMap.add(mf);
            }
        }

        if (fieldsInMap.size() == 0) {
            return true;
        }

        PreparedStatement s = getUpdateStatement(c, omdb, fieldsInMap, mc, o, exp);

        try {
            int rc = s.executeUpdate();
            if (rc > 0) {
                return true;
            } else {
                return false;
            }
        } finally {
            s.close();
        }
    }

    @Override
    public boolean delete(Connection c, MetaObject mc, ObjectMappingDB omdb,
            Object o, Collection<MetaField> keys)
            throws SQLException {

        // Check if there is table inheritence going on, and if so delete the super table first
        BaseDef base = omdb.getDBDef();
        if (base instanceof TableDef) {
            TableDef table = (TableDef) base;

            InheritenceDef inheritence = table.getInheritence();
            if (inheritence != null) {
                ObjectMappingDB smom = (ObjectMappingDB) omdb.getSuperMapping();

                MetaField rf = omdb.getField(inheritence.getRefColumn());
                Collection<MetaField> pkeys = new ArrayList<MetaField>();
                pkeys.add(rf);

                // Set the mapping field
                MetaField f = omdb.getField(inheritence.getColumnName());

                // Set the expression to delete
                Expression exp = new Expression(f.getName(), rf.getObject(o));

                // Delete the higher level table first
                boolean result = executeDelete(c, mc, omdb, exp);

                // Return a false if it was not deleteable
                if (result == false) {
                    return false;
                }

                // Delete the super classes table entry
                if (!delete(c, mc, smom, o, pkeys)) {
                    throw new SQLException("Super table entry could not be deleted for mapping [" + smom + "]");
                }

                return result;
            }
        }

        // If there wasn't inheritence, then generate the delete where clause

        // Generate the keys expression
        Expression exp = null;
        for (MetaField mf : keys) {
            Expression e = new Expression(mf.getName(), mf.getObject(o));
            if (exp == null) {
                exp = e;
            } else {
                exp = exp.and(e);
            }
        }

        //setAutoFields( c, mc, omdb, fields, o, ObjectManager.UPDATE );

        return executeDelete(c, mc, omdb, exp);
    }

    /**
     * Perform the actual delete call
     */
    protected boolean executeDelete(Connection c, MetaObject mc, ObjectMappingDB omdb, Expression exp) throws MetaException, SQLException {

        PreparedStatement s = getDeleteStatementWhere(c, mc, omdb, exp);

        try {
            int rc = s.executeUpdate();
            if (rc == 1) {
                return true;
            } else {
                return false;
            }
        } finally {
            s.close();
        }
    }

    @Override
    public long getCount(Connection c, MetaObject mc,
            ObjectMappingDB mapping, Expression exp) throws SQLException {

        PreparedStatement s = getCountStatementWhere(c, mc, mapping, exp);

        try {
            ResultSet rs = s.executeQuery();
            try {
                if (rs.next()) {
                    return rs.getLong(1);
                } else {
                    return 0;
                }
            } finally {
                rs.close();
            }
        } finally {
            s.close();
        }
    }

    @Override
    public Collection<Object> readMany(Connection c, MetaObject mc,
            ObjectMappingDB mapping, QueryOptions options) throws SQLException {

        if (options.withLock() && !(mapping.getDBDef() instanceof TableDef)) {
            throw new SQLException("Can only lock when reading from a Table, not a [" + mapping.getDBDef() + "]");
        }

        // Get the fields to read
        Collection<MetaField> fields = options.getFields();
        if (fields == null || fields.size() == 0) {
            fields = mapping.getMetaFields();
        }

        PreparedStatement s = getSelectStatementWhere(c, mc, mapping, fields, options);

        ArrayList<Object> objects = new ArrayList<Object>();

        try {
            ResultSet rs = s.executeQuery();

            // If the range is not supported in the query, then we need to do it manually
            Range range = null;
            if (!supportsRangeInQuery()) {
                options.getRange();
            }

            int index = 1;

            try {
                while (rs.next()) {
                    if (range == null
                            || (index >= range.getStart() && index <= range
                            .getEnd())) {
                        // Get a new Object
                        Object o = getManager().getNewObject(mc);

                        // Parse the Object
                        parseObject(rs, fields, mc, o);

                        // Add the object to the returned array
                        objects.add(o);
                    }

                    index++;
                }
            } finally {
                rs.close();
            }
        } finally {
            s.close();
        }

        return objects;
    }

    @Override
    public int updateMany(Connection c, MetaObject mc, ObjectMappingDB mapping,
            Map<MetaField, Object> values, Expression exp) throws SQLException {

        throw new UnsupportedOperationException("UpdateMany not supported by this Driver");

        /*PreparedStatement s = getUpdateStatement( c, mapping, fields, mc, o, exp);

         try {
         return s.executeUpdate();
         } finally {
         s.close();
         }*/
    }

    @Override
    public int deleteMany(Connection c, MetaObject mc, ObjectMappingDB mapping,
            Expression exp) throws SQLException {

        PreparedStatement s = getDeleteStatementWhere(c, mc, mapping, exp);

        try {
            return s.executeUpdate();
        } finally {
            s.close();
        }
    }

    /**
     * Gets the string to append to the INSERT call to request the generated ids
     */
    // public String getInsertAppendString( MetaClass mc )
    // {
    // throw new UnsupportedOperationException( "The GenericDriver cannot
    // retrieve field ids" );
    // }
    /**
     * Used if AUTO_DURING to retrieve the ids for the specified class
     */
    // @Override
    // public void getAutoIdentifiers( PreparedStatement s, MetaClass mc, Object
    // o ) throws SQLException {
    // throw new UnsupportedOperationException( "The GenericDriver cannot
    // retrieve field ids" );
    // }
    /**
     * Gets the next sequence for a given MetaClass
     */
    // @Override
    // public String getLastAutoId( Connection c, ColumnDef col ) throws
    // SQLException {
    // throw new UnsupportedOperationException( "The GenericDriver cannot
    // retrieve field ids" );
    // }
    /**
     * The SQL query to append to a SQL SELECT to lock the returned rows
     */
    public String getLockString() {
        throw new UnsupportedOperationException(
                "The GenericDriver cannot lock rows for updating");
    }

    public String getDateFormat() {
        return "yyyy-MM-dd HH:mm:ss";
    }

    /**
     * Returns whether the drive supports the Range within the query, i.e. LIMIT
     */
    protected boolean supportsRangeInQuery() {
        return false;
    }

    /**
     * Returns the SQL portion of the range string
     */
    public String getRangeString(Range range) {
        throw new UnsupportedOperationException(
                "The GenericDriver cannot perform a range during select");
    }

    /*private void setAutoFields(Connection c, MetaClass mc,
     ObjectMappingDB omdb, Collection<MetaField> fields, Object o,
     int mode ) throws SQLException {
		
     // Iterate the fields looking for auto creation fields
     for ( MetaField f : fields ) {
			
     // Switch off the column's auto type field
     ColumnDef colDef = (ColumnDef) omdb.getArgDef( f );
     switch( colDef.getAutoType() ) {
			
     // If an id, get the next sequence 
     case ColumnDef.AUTO_ID:
     if ( ObjectManager.CREATE == mode ) f.setString( o, getNextAutoId( c , colDef ));
     break;
				
     // If the create date, then set it on creates
     case ColumnDef.AUTO_DATE_CREATE:
     if ( ObjectManager.CREATE == mode ) f.setDate( o, new Date() );
     break;
				
     // If it's the update date, then set that on create and update
     case ColumnDef.AUTO_DATE_UPDATE:
     f.setDate( o, new Date() );
     break;
     }
     }
     }*/
    /**
     * Concatonates the list of field names together
     */
    protected String getFieldString(ObjectMappingDB omdb, Collection<MetaField> fields, String prefix) {
        return getFieldString(omdb, fields, true, prefix);
    }

    /**
     * Concatonates the list of field names together
     */
    protected String getFieldString(ObjectMappingDB omdb, Collection<MetaField> fields, boolean selectOnly, String prefix) {

        // Setup the select fields string
        StringBuilder buf = new StringBuilder();
        int j = 0;
        for (MetaField mf : fields) {

            //BaseTableDef base = (BaseTableDef) omdb.getDBDef();
            
            ColumnDef colDef = (ColumnDef) omdb.getArgDef(mf);
            //while ( colDef == null ) {
            //	if ( base instanceof TableDef ) {					
            //	}
            //}
            
            // Do not use the field if it's an auto id that is set after after
            // creation/update -- only relevant on some drivers (MSSQL)
            if ( !selectOnly && colDef.getAutoType() == ColumnDef.AUTO_LAST_ID ) continue;

            if (j > 0) {
                buf.append(",");
            }

            if (prefix != null) {
                buf.append(prefix).append(".");
            }
            buf.append(colDef.getName());

            j++;
        }

        return buf.toString();
    }

    /**
     * <p> Concatonates the list of ?'s for each field in the collection </p>
     */
    protected String getValueStringForFields(ObjectMappingDB omdb, Collection<MetaField> fields) {

        //return getQuestionCommaString(fields.size());
        
        int j = 0;
         for ( MetaField mf : fields ) {
			
            ColumnDef colDef = (ColumnDef) omdb.getArgDef(mf);

            // Do not use the field if it's an auto id that is set after after
            // creation/update
            if ( colDef.getAutoType() != ColumnDef.AUTO_LAST_ID ) j++;
         }

         return getQuestionCommaString(j);
    }

    /**
     * <p> Concatonates the list of ?'s for each field in the collection </p>
     */
    protected String getQuestionCommaString(int size) throws MetaException {
        // Setup the select fields string
        StringBuilder buf = new StringBuilder();
        for (int j = 0; j < size; j++) {
            if (j > 0) {
                buf.append(",");
            }
            buf.append('?');
        }

        return buf.toString();
    }

    /**
     * Gets the SQL SET clause for the fields of a class
     */
    protected String getSetString(ObjectMappingDB omdb, Collection<MetaField> fields) {

        StringBuilder set = new StringBuilder();

        // Setup the SET clause
        for (MetaField mf : fields) {

            ColumnDef colDef = (ColumnDef) omdb.getArgDef(mf);

            // Don't try to set the id field
            if (colDef.isAutoIncrementor()) {
                continue;
            }

            if (set.length() > 0) {
                set.append(", ");
            }
            set.append(colDef.getName());
            set.append("=?");
        }

        return set.toString();
    }

    /**
     * Gets the ORDER BY clause
     */
    protected String getOrderString(MetaObject mc, ObjectMappingDB omdb, SortOrder order) throws MetaException {

        StringBuilder b = new StringBuilder();

        boolean first = true;
        while (order != null) {

            if (first) {
                first = false;
            } else {
                b.append(", ");
            }

            MetaField mf = mc.getMetaField(order.getField());

            char prefix = 'A';
            ColumnDef colDef = (ColumnDef) omdb.getArgDef(mf);
            if (colDef == null) {
                throw new MetaException("MetaField [" + mf + "] has no column mapping for [" + omdb + "]");
            }

            if (mf.getType() == MetaField.STRING) {
                b.append("UPPER(");
                b.append(prefix).append(".");
                b.append(colDef.getName());
                b.append(')');
            } else {
                b.append(prefix).append(".");
                b.append(colDef.getName());
            }

            if (order.getOrder() == SortOrder.DESC) {
                b.append(" DESC");
            }

            order = order.getNext();
        }

        return b.toString();
    }

    /**
     * Gets the SQL WHERE clause for the fields of a class
     */
    protected String getExpressionString(MetaObject mc, ObjectMappingDB omdb, Expression exp,
            ArrayList<SQLArg> args, String prefix) throws MetaException {
        StringBuilder set = new StringBuilder();

        if (exp instanceof ExpressionGroup) {
            set.append("( ");
            set.append(getExpressionString(mc, omdb, ((ExpressionGroup) exp)
                    .getGroup(), args, prefix));
            set.append(" )");
        } else if (exp instanceof ExpressionOperator) {
            ExpressionOperator oper = (ExpressionOperator) exp;

            set.append(getExpressionString(mc, omdb, oper.getExpressionA(), args, prefix));

            if (oper.getOperator() == ExpressionOperator.AND) {
                set.append(" AND ");
            } else {
                set.append(" OR ");
            }

            set.append(getExpressionString(mc, omdb, oper.getExpressionB(), args, prefix));
        } else if (exp.isSpecial()) {
            throw new IllegalArgumentException(
                    "Unsupported Special Expression [" + exp + "]");
        } else {
            MetaField f = mc.getMetaField(exp.getField());

            ColumnDef colDef = (ColumnDef) omdb.getArgDef(f);
            if (colDef == null) {
                throw new MetaException("MetaField [" + f + "] has no column mapping defined for [" + omdb + "]");
            }

            int c = exp.getCondition();

            if (c == Expression.CONTAIN || c == Expression.NOT_CONTAIN
                    || c == Expression.START_WITH
                    || c == Expression.NOT_START_WITH
                    || c == Expression.END_WITH
                    || c == Expression.NOT_END_WITH
                    || c == Expression.EQUALS_IGNORE_CASE) {
                set.append("UPPER(");
                if (prefix != null) {
                    set.append(prefix).append(".");
                }
                set.append(colDef.getName());
                set.append(')');
            } else {
                if (prefix != null) {
                    set.append(prefix).append(".");
                }
                set.append(colDef.getName());
            }

            set.append(' ');

            if (exp.getValue() == null) {
                switch (c) {
                    case Expression.EQUAL:
                        set.append("IS NULL");
                        break;
                    case Expression.NOT_EQUAL:
                        set.append("IS NOT NULL");
                        break;
                    default:
                        throw new IllegalArgumentException(
                                "Null values not allowed for condition ["
                                + Expression.condStr(c) + "]");
                }
            } else if (exp.getValue() instanceof SubSelectValue) {

                if (c != Expression.EQUAL && c != Expression.NOT_EQUAL) {
                    throw new IllegalArgumentException("Can only have a value of type Collection for EQUAL or NOT_EQUAL comparisons");
                }

                if (c == Expression.NOT_EQUAL) {
                    set.append("NOT ");
                }
                set.append("IN (");
                set.append(((SubSelectValue) exp.getValue()).getSql());
                set.append(")");
            } else if (exp.getValue() instanceof Collection) {

                if (c != Expression.EQUAL && c != Expression.NOT_EQUAL) {
                    throw new IllegalArgumentException("Can only have a value of type Collection for EQUAL or NOT_EQUAL comparisons");
                }

                if (c == Expression.NOT_EQUAL) {
                    set.append("NOT ");
                }
                set.append("IN (");

                boolean first = true;
                for (Object o : ((Collection<?>) exp.getValue())) {
                    if (first) {
                        first = false;
                    } else {
                        set.append(",");
                    }
                    set.append("?");
                    args.add(new SQLArg(f, o));
                }

                set.append(")");
            } else {
                // Add these on as arguments
                switch (c) {
                    case Expression.EQUAL:
                        set.append("= ?");
                        break;
                    case Expression.NOT_EQUAL:
                        set.append("<> ?");
                        break;
                    case Expression.GREATER:
                        set.append("> ?");
                        break;
                    case Expression.LESSER:
                        set.append("< ?");
                        break;
                    case Expression.EQUAL_GREATER:
                        set.append(">= ?");
                        break;
                    case Expression.EQUAL_LESSER:
                        set.append("<= ?");
                        break;

                    case Expression.EQUALS_IGNORE_CASE:
                    case Expression.CONTAIN:
                    case Expression.START_WITH:
                    case Expression.END_WITH:
                        set.append("LIKE UPPER(?)");
                        break;

                    case Expression.NOT_CONTAIN:
                    case Expression.NOT_START_WITH:
                    case Expression.NOT_END_WITH:
                        set.append("NOT LIKE UPPER(?)");
                        break;

                    default:
                        throw new IllegalArgumentException(
                                "Unsupported Expression Conditional ("
                                + exp.getCondition() + ") for Expression ["
                                + exp + "]");
                }

                // Only add an argument if it's not null

                if (c == Expression.EQUAL || c == Expression.NOT_EQUAL
                        || c == Expression.GREATER || c == Expression.LESSER
                        || c == Expression.EQUAL_GREATER
                        || c == Expression.EQUAL_LESSER) {
                    args.add(new SQLArg(f, exp.getValue()));
                } else if (c == Expression.CONTAIN
                        || c == Expression.NOT_CONTAIN
                        || c == Expression.START_WITH
                        || c == Expression.NOT_START_WITH
                        || c == Expression.END_WITH
                        || c == Expression.NOT_END_WITH
                        || c == Expression.EQUALS_IGNORE_CASE) {
                    String val = (exp.getValue() == null) ? null : exp
                            .getValue().toString();

                    switch (c) {
                        case Expression.CONTAIN:
                        case Expression.NOT_CONTAIN:
                            val = "%" + val + "%";
                            break;

                        case Expression.END_WITH:
                        case Expression.NOT_END_WITH:
                            val = "%" + val;
                            break;

                        case Expression.START_WITH:
                        case Expression.NOT_START_WITH:
                            val = val + "%";
                            break;

                        case Expression.EQUALS_IGNORE_CASE:
                            // val = val;   
                            break;
                    }

                    args.add(new SQLArg(f, val));
                } else {
                    throw new IllegalStateException(
                            "Invalud Expression Condition ["
                            + Expression.condStr(c) + "]");
                }
            }

            // set.append( " ?" );
        }

        return set.toString();
    }

    /**
     * Gets the SQL WHERE clause for the keys of a class
     */
    protected String getWhereStringForKeys(ObjectMappingDB omdb, Collection<MetaField> keys)
            throws MetaException {
        StringBuilder where = new StringBuilder();

        // Setup the WHERE clause
        for (MetaField f : keys) {
            ColumnDef colDef = (ColumnDef) omdb.getArgDef(f);
            if (colDef == null) {
                throw new MetaException("MetaField [" + f
                        + "] has no column mapping defined for [" + omdb + "]");
            }

            if (where.length() > 0) {
                where.append(" AND ");
            }
            where.append(colDef.getName());
            where.append("=?");
        }

        return where.toString();
    }

    /**
     * Gets the id clause for a unique transaction
     */
    /*protected PreparedStatement getSelectStatementForRef(Connection c,
     MetaClass mc, Collection<MetaField> fields, ObjectRef ref)
     throws SQLException, MetaException {
     // TODO: The query construction should be cached for each MetaClass &
     // field combo

     if (!isReadableClass(mc))
     throw new MetaException("MetaClass [" + mc + "] is not readable");

     // validateMetaClass( c, mc );

     Collection<MetaField> keys = getPrimaryKeys(mc);

     // Get the components of the SELECT query
     String tableStr = getViewName(mc);
     if (tableStr == null)
     throw new MetaException("MetaClass [" + mc
     + "] has no table or view defined");

     String whereStr = getWhereStringForKeys(keys);
     String fieldStr = getFieldString(fields);

     // Construct the SELECT query
     String query = "SELECT " + fieldStr + " FROM " + tableStr + " WHERE "
     + whereStr;

     PreparedStatement s = c.prepareStatement(query);

     // ystem.out.println( ">>> QUERY: " + query );

     StringBuilder valStr = new StringBuilder(" ");
     try {
     // Set the statement values form the id
     // setStatementValuesForRef( s, keys, 1, ref );
     String[] ids = ref.getIds();

     int k = 0;
     int j = 1;
     // Iterator v = values.iterator();
     for (MetaField f : keys) {
     String value = ids[k++];

     setStatementValue(s, f, j++, value);

     valStr.append("(" + value + ")");
     }

     if (log.isDebugEnabled())
     log.debug("SQL (" + c.hashCode()
     + ") - getSelectStatementForRef: [" + query
     + valStr.toString() + "]");

     // Return the prepared statement
     return s;
     } catch (SQLException e) {
     s.close();
     throw e;
     }
     }*/
    /**
     * Gets the id clause for a unique transaction
     */
    /* protected PreparedStatement getSelectStatementForObject(Connection c,
     MetaClass mc, Collection<MetaField> fields, Object obj)
     throws SQLException, MetaException {
     // TODO: The query construction should be cached for each MetaClass &
     // field combo

     if (!isReadableClass(mc))
     throw new MetaException("MetaClass [" + mc + "] is not persistable");
     // PersistableMetaClass pmc = (PersistableMetaClass) mc;

     // validateMetaClass( c, mc );

     Collection<MetaField> keys = getPrimaryKeys(mc);

     // Get the components of the SELECT query
     String tableStr = getViewName(mc);
     if (tableStr == null)
     throw new MetaException("MetaClass [" + mc
     + "] has no table or view defined");

     String whereStr = getWhereStringForKeys(keys);
     String fieldStr = getFieldString(fields);

     // Construct the SELECT query
     String query = "SELECT " + fieldStr + " FROM " + tableStr + " WHERE "
     + whereStr;

     PreparedStatement s = c.prepareStatement(query);

     // ystem.out.println( ">>> QUERY: " + query );

     StringBuilder valStr = new StringBuilder(" ");

     try {
     int j = 1;
     // Set the statement values form the id
     for (MetaField f : keys) {
     setStatementValue(s, f, j, f.getObject(obj));
     valStr.append("(" + f.getString(obj) + ")");
     j++;
     }

     if (log.isDebugEnabled())
     log.debug("SQL (" + c.hashCode()
     + ") - getSelectStatementforObject: [" + query
     + valStr.toString() + "]");

     // Return the prepared statement
     return s;
     } catch (SQLException e) {
     s.close();
     throw e;
     }
     } */
    /**
     * Gets the id clause for a unique transaction
     */
    protected PreparedStatement getCountStatementWhere(Connection c, MetaObject mc,
            ObjectMappingDB omdb, Expression where)
            throws SQLException, MetaException {

        // Construct the SELECT query
        StringBuilder query = new StringBuilder();
        query.append("SELECT COUNT(*) FROM ");

        char prefix = 'A';
        BaseTableDef base = (BaseTableDef) omdb.getDBDef();
        while (base != null) {
            // Get the components of the SELECT query
            String tableStr = getProperName(base.getNameDef());
            query.append(tableStr).append(' ').append(prefix);

            if (base instanceof TableDef) {

                TableDef table = (TableDef) base;
                base = null;

                InheritenceDef idef = table.getInheritence();
                if (idef != null) {
                    base = idef.getRefTable();
                    prefix++;

                    query.append(" LEFT JOIN ");
                    tableStr = getProperName(base.getNameDef());
                    query.append(tableStr).append(' ').append(prefix);
                    query.append(" ON ");
                    query.append(prefix--).append(idef.getColumnName())
                            .append("=").append(prefix).append(idef.getRefColumn().getName());
                }
            } else {
                break;
            }
        }

        ArrayList<SQLArg> args = new ArrayList<SQLArg>();

        if (where != null) {
            query.append(" WHERE ").append(getExpressionString(mc, omdb, where, args, "A"));
        }

        PreparedStatement s = c.prepareStatement(query.toString());

        int index = 1;

        StringBuilder valStr = null;
        if (log.isDebugEnabled()) {
            valStr = new StringBuilder(" ");
        }

        if (where != null) {
            for (SQLArg arg : args) {

                MetaField f = arg.getMetaField();
                Object value = arg.getValue();

                if (log.isDebugEnabled() && index > 1) {
                    valStr.append(", ");
                }

                setStatementValue(s, f, index++, value);

                if (log.isDebugEnabled()) {
                    valStr.append("(" + value + ")");
                }
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("SQL (" + c.hashCode()
                    + ") - getCountStatementWhere: [ " + query.toString()
                    + valStr.toString() + " ]");
        }

        // ystem.out.println( ">>> QUERY: " + query.toString() + " " +
        // valStr.toString() );

        return s;
    }

    /**
     * Gets the id clause for a unique transaction
     */
    protected PreparedStatement getSelectStatementWhere(Connection c, MetaObject mc,
            ObjectMappingDB omdb, Collection<MetaField> fields, QueryOptions options)
            throws SQLException, MetaException {

        Expression where = options.getExpression();
        SortOrder order = options.getSortOrder();

        // Construct the SELECT query
        StringBuilder query = new StringBuilder();
        query.append("SELECT ");
        if (options.isDistinct()) {
            query.append("DISTINCT ");
        }
        if (options.getRange() != null) {
            // TODO: Make this a little smarter for christ's sake!
            // Recommendation: Have the driver form the final SELECT call, then
            // it can insert the limit where needed
            //if (getDatabaseDriver() instanceof MSSQLDriver) {
            //	query.append("TOP ").append(options.getRange().getEnd())
            //			.append(' ');
            //}
        }

        String fieldStr = getFieldString(omdb, fields, "A");

        query.append(fieldStr);

        query.append(" FROM ");

        char prefix = 'A';
        BaseTableDef base = (BaseTableDef) omdb.getDBDef();
        while (base != null) {
            // Get the components of the SELECT query
            String tableStr = getProperName(base.getNameDef());
            query.append(tableStr).append(' ').append(prefix);

            if (base instanceof TableDef) {

                TableDef table = (TableDef) base;
                base = null;

                InheritenceDef idef = table.getInheritence();
                if (idef != null) {
                    base = idef.getRefTable();
                    prefix++;

                    query.append(" LEFT JOIN ");
                    tableStr = getProperName(base.getNameDef());
                    query.append(tableStr).append(' ').append(prefix);
                    query.append(" ON ");
                    query.append(prefix--).append(idef.getColumnName())
                            .append("=").append(prefix).append(idef.getRefColumn().getName());
                }
            } else {
                break;
            }
        }

        ArrayList<SQLArg> args = new ArrayList<SQLArg>();

        if (where != null) {
            query.append(" WHERE ").append(getExpressionString(mc, omdb, where, args, "A"));
        }

        if (order != null) {
            query.append(" ORDER BY ").append(getOrderString(mc, omdb, order));
        }

        if (options.withLock()) {
            query.append(" ").append(getLockString());
        }

        // Add on the range
        Range range = options.getRange();
        if (supportsRangeInQuery() && options.getRange() != null
                && range.getStart() > 0 && range.getEnd() > 0) {

            if (range.getStart() > range.getEnd()) {
                throw new IllegalArgumentException("The range end (" + range.getEnd() + ") cannot be greater than the start value (" + range.getStart() + ")");
            }

            query.append(" ").append(getRangeString(range));
        }

        PreparedStatement s = c.prepareStatement(query.toString());

        int index = 1;

        StringBuilder valStr = null;
        if (log.isDebugEnabled()) {
            valStr = new StringBuilder(" ");
        }

        if (where != null) {
            for (SQLArg arg : args) {

                MetaField f = arg.getMetaField();
                Object value = arg.getValue();

                if (log.isDebugEnabled() && index > 1) {
                    valStr.append(", ");
                }

                setStatementValue(s, f, index++, value);

                if (log.isDebugEnabled()) {
                    valStr.append("(" + value + ")");
                }
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("SQL (" + c.hashCode()
                    + ") - getSelectStatementWhere: [ " + query.toString()
                    + valStr.toString() + " ]");
        }

        // ystem.out.println( ">>> QUERY: " + query.toString() + " " +
        // valStr.toString() );

        return s;
    }

    /**
     * Gets the id clause for a unique transaction
     */
    protected PreparedStatement getDeleteStatementWhere(Connection c,
            MetaObject mc, ObjectMappingDB omdb, Expression where) throws SQLException,
            MetaException {

        // Get the components of the SELECT query
        String tableStr = getProperName(omdb.getDBDef().getNameDef());

        // Construct the SELECT query
        StringBuilder query = new StringBuilder("DELETE FROM ");
        query.append(tableStr); //.append( " A");

        ArrayList<SQLArg> args = new ArrayList<SQLArg>();

        if (where != null) {
            query.append(" WHERE ");
            query.append(getExpressionString(mc, omdb, where, args, null));
        }

        PreparedStatement s = c.prepareStatement(query.toString());

        int index = 1;

        StringBuilder valStr = new StringBuilder(" ");

        if (where != null) {
            for (SQLArg arg : args) {

                MetaField f = arg.getMetaField();
                Object value = arg.getValue();

                if (index > 1) {
                    valStr.append(", ");
                }

                setStatementValue(s, f, index++, value);

                valStr.append("(" + value + ")");
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("SQL (" + c.hashCode()
                    + ") - getDeleteStatementWhere: [ " + query.toString()
                    + valStr.toString() + " ]");
        }

        // ystem.out.println( ">>> QUERY: " + query.toString() + " " +
        // valStr.toString() );

        return s;
    }

    /**
     * Sets a specific value on a prepared statement
     */
    protected void setStatementValue(PreparedStatement s, MetaField f, int index, Object value) throws SQLException {
        int j = index;

        switch (f.getType()) {
            case MetaField.BOOLEAN: {
                if (value == null) {
                    s.setNull(j, Types.BIT);
                } else if (value instanceof Boolean) {
                    s.setBoolean(j, ((Boolean) value).booleanValue());
                } else {
                    s.setBoolean(j, Boolean.valueOf(value.toString()).booleanValue());
                }
            }
            break;

            case MetaField.BYTE: {
                if (value == null) {
                    s.setNull(j, Types.TINYINT);
                } else if (value instanceof Byte) {
                    s.setByte(j, ((Byte) value).byteValue());
                } else {
                    s.setByte(j, Byte.valueOf(value.toString()).byteValue());
                }
            }
            break;

            case MetaField.SHORT: {
                if (value == null) {
                    s.setNull(j, Types.SMALLINT);
                } else if (value instanceof Short) {
                    s.setShort(j, ((Short) value).shortValue());
                } else {
                    s.setShort(j, Short.valueOf(value.toString()).shortValue());
                }
            }
            break;

            case MetaField.INT: {
                if (value == null) {
                    s.setNull(j, Types.INTEGER);
                } else if (value instanceof Integer) {
                    s.setInt(j, ((Integer) value).intValue());
                } else {
                    s.setInt(j, Integer.valueOf(value.toString()).intValue());
                }
            }
            break;

            case MetaField.DATE: // NOTE DATE IS TREATED AS LONG!
            {
                if (value == null) {
                    s.setNull(j, Types.TIMESTAMP);
                } else if (value instanceof java.util.Date) {
                    s.setTimestamp(j, new Timestamp(((java.util.Date) value).getTime()));
                } else {
                    s.setTimestamp(j, new Timestamp(Long.valueOf(value.toString()).longValue()));
                }
            }
            break;

            case MetaField.LONG: {
                if (value == null) {
                    s.setNull(j, Types.BIGINT);
                } else if (value instanceof Long) {
                    s.setLong(j, ((Long) value).longValue());
                } else {
                    s.setLong(j, Long.valueOf(value.toString()).longValue());
                }
            }
            break;

            // WARNING:  This should not be a valid key
            case MetaField.FLOAT: {
                if (value == null) {
                    s.setNull(j, Types.FLOAT);
                } else if (value instanceof Float) {
                    s.setFloat(j, ((Float) value).floatValue());
                } else {
                    s.setFloat(j, Float.valueOf(value.toString()).floatValue());
                }
            }
            break;

            // WARNING:  This should not be a valid key
            case MetaField.DOUBLE: {
                if (value == null) {
                    s.setNull(j, Types.DOUBLE);
                } else if (value instanceof Double) {
                    s.setDouble(j, ((Double) value).doubleValue());
                } else {
                    s.setDouble(j, Double.valueOf(value.toString()).doubleValue());
                }
            }
            break;

            case MetaField.STRING:
                if (value == null) {
                    s.setNull(j, Types.VARCHAR);
                } else {
                    s.setString(j, value.toString());
                }
                break;

            case MetaField.OBJECT:
                //if ( value == null )
                //  s.setNull( j, Types.BLOB );
                //else
                s.setObject(j, value);
                break;
        }
    }

    /**
     * Gets the id clause for a unique transaction
     */
    protected PreparedStatement getInsertStatement(Connection c, MetaObject mc,
            ObjectMappingDB omdb, Object o) throws SQLException, MetaException {

        Collection<MetaField> fields = omdb.getMetaFields();

        // Get the components of the SELECT query
        String tableStr = getProperName(omdb.getDBDef().getNameDef());

        String fieldStr = getFieldString(omdb, fields, false, null);
        String valueStr = getValueStringForFields(omdb, fields);

        // Construct the SELECT query
        String query = "INSERT INTO " + tableStr + " (" + fieldStr + ")"
                + " VALUES (" + valueStr + ")";

        PreparedStatement s = null;

        // Tack on any needed queries to get the keys
        // if ( getDatabaseDriver().getAutoType() == AUTO_DURING )
        // {
        // String add = getDatabaseDriver().getInsertAppendString( mc );
        // if ( add != null && add.length() > 0 ) query += add;

        // s = c.prepareCall( query );
        // }
        // else
        // {
        s = c.prepareStatement(query);
        // }

        StringBuilder valStr = new StringBuilder(" ");

        try {
            int j = 1;
            for (MetaField f : fields) {

                ColumnDef colDef = (ColumnDef) omdb.getArgDef(f);

                // Do not use the field if it's an auto id that is set after
                // after creation/update -- only valid on some drivers (MSSQL)
                //if ( colDef.isAutoIncrementor() )
                //	continue;

                // Set the statement id value
                if (colDef.getAutoType() == ColumnDef.AUTO_ID) {
                    f.setString(o, getNextAutoId(c, colDef));
                }
                else if (colDef.getAutoType() == ColumnDef.AUTO_LAST_ID) {
                    continue;
                }
                // Set the create date (if auto)
                //else if ( colDef.getAutoType() == ColumnDef.AUTO_DATE_CREATE ) {
                //	f.setDate( o, new Date() );
                //}

                setStatementValue(s, f, j, f.getObject(o));

                valStr.append("(" + f.getString(o) + ")");

                j++;
            }

            if (log.isDebugEnabled()) {
                log.debug("SQL (" + c.hashCode() + ") - getInsertStatement: ["
                        + query + valStr.toString() + "]");
            }

            // ystem.out.println( ">>> QUERY: [" + query + "] [" +
            // valStr.toString() + "]" );

            // Return the prepared statement
            return s;
        } catch (SQLException e) {
            s.close();
            throw e;
        }
    }

    /**
     * Gets the update statement for a unique object
     */
    protected PreparedStatement getUpdateStatement(Connection c, ObjectMappingDB omdb,
            Collection<MetaField> fields, MetaObject mc, Object o, Expression exp)
            throws SQLException {

        // WARNING: The query construction should be cached for each MetaClass &
        // field combo

        // validateMetaClass( c, mc );

        // String id = pmc.getId( o );
        //Collection<MetaField> keys = getPrimaryKeys(mc);

        // Get the components of the SELECT query
        String tableStr = getProperName(omdb.getDBDef().getNameDef());

        ArrayList<SQLArg> args = new ArrayList<SQLArg>();

        String setStr = getSetString(omdb, fields);
        String whereStr = getExpressionString(mc, omdb, exp, args, null);

        // Construct the SELECT query
        String query = "UPDATE " + tableStr + " SET " + setStr + " WHERE "
                + whereStr;

        // Used to prevent dirty writes
        //if (dirtyField != null) {
        //	query += " AND ( " + dirtyField.getAttribute(COL_REF) + "=? )";
        //}

        PreparedStatement s = c.prepareStatement(query);

        // ystem.out.println( ">>> QUERY: " + query );

        StringBuilder valStr = new StringBuilder(" ");

        try {
            // Set the update values
            int j = 1;
            for (MetaField f : fields) {

                ColumnDef colDef = (ColumnDef) omdb.getArgDef(f);

                if (colDef.isAutoIncrementor()) {
                    continue;
                }

                // Set the create date (if auto)
                //if ( colDef.getAutoType() == ColumnDef.AUTO_DATE_CREATE ||
                //		colDef.getAutoType() == ColumnDef.AUTO_DATE_UPDATE ) {
                //	f.setDate( o, new Date() );
                //}

                setStatementValue(s, f, j++, f.getObject(o));

                valStr.append("(" + f.getString(o) + ")");
            }

            // Set the WHERE clause arguments
            for (SQLArg arg : args) {

                MetaField f = arg.getMetaField();
                Object value = arg.getValue();

                if (j > 1) {
                    valStr.append(", ");
                }

                setStatementValue(s, f, j++, value);

                // If we're logging, then get the values to log
                if (log.isDebugEnabled()) {

                    String v = null;
                    if (value != null && value instanceof Date) {
                        v = "" + ((Date) value).getTime();
                    } else {
                        v = "" + value;
                    }
                    valStr.append("(" + v + ")");
                }
            }

            // Set the key values - replaced by WHERE clause
            //for (MetaField f : keys) {
            //	setStatementValue(s, f, j++, f.getObject(o));
            //	valStr.append("{" + f.getString(o) + "}");
            //}

            // Used to prevent dirty writes
            //if (dirtyField != null)
            //	setStatementValue(s, dirtyField, j, dirtyFieldValue);

            if (log.isDebugEnabled()) {
                log.debug("SQL (" + c.hashCode() + ") - getUpdateStatement: ["
                        + query + valStr + "]");
            }

            // Return the prepared statement
            return s;
        } catch (SQLException e) {
            s.close();
            throw e;
        }
    }

    /**
     * Gets the delete statement for a specific id
     */
    protected PreparedStatement getDeleteStatement(Connection c, MetaObject mc,
            ObjectMappingDB omdb, Collection<MetaField> keys, Object obj) throws SQLException, MetaException {

        // Get the components of the SELECT query
        String tableStr = getProperName(omdb.getDBDef().getNameDef());

        String whereStr = null;//getWhereStringForKeys(keys);

        // Construct the SELECT query
        String query = "DELETE FROM " + tableStr + " WHERE " + whereStr;

        PreparedStatement s = c.prepareStatement(query);

        // ystem.out.println( ">>> QUERY: " + query );

        StringBuilder valStr = new StringBuilder(" ");

        try {
            // Set the key values
            int j = 1;
            for (Iterator<MetaField> i = keys.iterator(); i.hasNext(); j++) {
                MetaField f = i.next();
                setStatementValue(s, f, j, f.getObject(obj));

                valStr.append("(" + f.getString(obj) + ")");
            }

            if (log.isDebugEnabled()) {
                log.debug("SQL (" + c.hashCode() + ") - getDeleteStatement: ["
                        + query + valStr + "]");
            }

            // Return the prepared statement
            return s;
        } catch (SQLException e) {
            s.close();
            throw e;
        }
    }

    /**
     * Parses an Object returned from the database
     */
    protected void parseObject(ResultSet rs, Collection<MetaField> fields,
            MetaObject mc, Object o) throws SQLException, MetaException {
        int j = 1;
        for (MetaField f : fields) {
            parseField(o, f, rs, j++);
        }

        // This should be done in the ObjectManager
        // Update the state of the object
		/*
         * if ( mc instanceof StatefulMetaClass ) { // It was pulled from the
         * database, so it doesn't need to be flagged as modified
         * ((StatefulMetaClass) mc ).setModified( o, false );
         *  // It is also no longer a new item ((StatefulMetaClass) mc ).setNew(
         * o, false ); }
         */
    }

    protected void parseField(Object o, MetaField f, ResultSet rs, int j)
            throws SQLException {
        switch (f.getType()) {
            case MetaField.BOOLEAN: {
                boolean bv = rs.getBoolean(j);
                if (rs.wasNull()) {
                    f.setBoolean(o, null);
                } else {
                    f.setBoolean(o, new Boolean(bv));
                }
            }
            break;

            case MetaField.BYTE: {
                byte bv = rs.getByte(j);
                if (rs.wasNull()) {
                    f.setByte(o, null);
                } else {
                    f.setByte(o, new Byte(bv));
                }
            }
            break;

            case MetaField.SHORT: {
                short sv = rs.getShort(j);
                if (rs.wasNull()) {
                    f.setShort(o, null);
                } else {
                    f.setShort(o, new Short(sv));
                }
            }
            break;

            case MetaField.INT: {
                int iv = rs.getInt(j);
                if (rs.wasNull()) {
                    f.setInt(o, null);
                } else {
                    f.setInt(o, new Integer(iv));
                }
            }
            break;

            case MetaField.DATE: {
                Timestamp tv = rs.getTimestamp(j);
                if (rs.wasNull()) {
                    f.setDate(o, null);
                } else {
                    f.setDate(o, new java.util.Date(tv.getTime()));
                }
            }
            break;

            case MetaField.LONG: {
                long lv = rs.getLong(j);
                if (rs.wasNull()) {
                    f.setLong(o, null);
                } else {
                    f.setLong(o, new Long(lv));
                }
            }
            break;

            case MetaField.FLOAT: {
                float fv = rs.getFloat(j);
                if (rs.wasNull()) {
                    f.setFloat(o, null);
                } else {
                    f.setFloat(o, new Float(fv));
                }
            }
            break;

            case MetaField.DOUBLE: {
                double dv = rs.getDouble(j);
                if (rs.wasNull()) {
                    f.setDouble(o, null);
                } else {
                    f.setDouble(o, new Double(dv));
                }
            }
            break;

            case MetaField.STRING:
                f.setString(o, rs.getString(j));
                break;

            case MetaField.OBJECT:
                f.setObject(o, rs.getObject(j));
                break;
        }
    }

    // /////////////////////////////////////////////////////
    // TO STRING METHOD
    public String toString() {
        return getClass().getSimpleName();
    }
}
