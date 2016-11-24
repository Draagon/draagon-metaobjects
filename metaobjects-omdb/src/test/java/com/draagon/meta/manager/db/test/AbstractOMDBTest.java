/*
 * Copyright (c) 2012 Doug Mealing LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Doug Mealing LLC - initial API and implementation and/or initial documentation
 */
package com.draagon.meta.manager.db.test;

<<<<<<< HEAD
=======
import com.draagon.meta.loader.xml.XMLFileMetaDataLoader;
>>>>>>> 3dd7119... Revert "Fixes based on recent changes around MetaDataLoader"
import com.draagon.meta.manager.ObjectConnection;
import com.draagon.meta.manager.db.ObjectManagerDB;
import com.draagon.meta.manager.db.driver.DerbyDriver;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLNonTransientConnectionException;
import java.sql.Statement;
import java.util.logging.Logger;
import javax.sql.DataSource;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 *
 * @author dmealing
 */
public class AbstractOMDBTest {
    
    protected static ObjectManagerDB omdb = null;
    protected static String dbFile = null;
    protected ObjectConnection oc = null;
    
    @BeforeClass
    public static void setupDB() throws Exception {
                
        if ( dbFile == null ) {
<<<<<<< HEAD
=======

            XMLFileMetaDataLoader loader = new XMLFileMetaDataLoader( "derby-test" );
>>>>>>> 3dd7119... Revert "Fixes based on recent changes around MetaDataLoader"
            
            dbFile = "omb-testing-"+System.currentTimeMillis();
            
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
            getConnection().close();     

            /** Create a simple DataSource for testing */
            DataSource ds = new DataSource() {

                @Override
                public Connection getConnection() throws SQLException {
                    return AbstractOMDBTest.getConnection();
                }

                @Override
                public Connection getConnection(String username, String password) throws SQLException {
                    return getConnection();
                }

                @Override
                public PrintWriter getLogWriter() throws SQLException {
                    return new PrintWriter( System.out );
                }

                @Override
                public void setLogWriter(PrintWriter out) throws SQLException {                    
                }

                @Override
                public void setLoginTimeout(int seconds) throws SQLException {
                }

                @Override
                public int getLoginTimeout() throws SQLException {
                    return 100;
                }

                @Override
                public Logger getParentLogger() throws SQLFeatureNotSupportedException {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public <T> T unwrap(Class<T> iface) throws SQLException {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public boolean isWrapperFor(Class<?> iface) throws SQLException {
                    throw new UnsupportedOperationException("Not supported yet.");
                }
            };
            
            // Initialize the object manager
<<<<<<< HEAD
            omdb = new ObjectManagerDB();
=======
            omdb = new ObjectManagerDB( loader );
>>>>>>> 3dd7119... Revert "Fixes based on recent changes around MetaDataLoader"
            omdb.setDatabaseDriver( new DerbyDriver() );
            omdb.setDataSource( ds );
            omdb.init();
        }        
    }
    
    /** Returns a new database Connection for the Derby test database */
    protected static Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:derby:memory:"+dbFile+";create=true");
    }
    
    /** Creates a view with the specified name */
    protected static boolean createView( String viewName, String sql ) throws SQLException {        
        try {
            return executeSql( "CREATE VIEW " + viewName + " AS " + sql );
        } catch( SQLException e ) {
            if ( e.getMessage().contains( "already exists" )) return true;
            throw e;
        }
    }
    
    /** Executes the specified SQL */
    protected static boolean executeSql( String sql ) throws SQLException {
        Connection c = getConnection();
        try { 
            Statement s = c.createStatement();
            try {
                return s.execute( sql );
            } finally {
                s.close();
            }
        } finally {
            c.close();
        }
    }
    
    @AfterClass
    public static synchronized void destroyEntityManager() throws Exception {

        try {
            DriverManager.getConnection("jdbc:derby:memory:"+dbFile+";drop=true");
        } catch( SQLNonTransientConnectionException ex ) {}
        System.out.println( "DB Destroyed!" );
    }

    @Before
    public void startTx() throws SQLException {     
        oc = omdb.getConnection();
    }

    @After
    public void endTx() {
        omdb.releaseConnection(oc);
    }
}
