package com.draagon.meta.examples.osgi;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * OSGi Bundle Activator for MetaObjects example.
 * 
 * Demonstrates proper OSGi bundle lifecycle management
 * with MetaObjects integration.
 */
public class OSGiExampleActivator implements BundleActivator {
    
    private OSGiMetaObjectsExample example;
    
    @Override
    public void start(BundleContext context) throws Exception {
        System.out.println("Starting MetaObjects OSGI Example Bundle...");
        
        try {
            example = new OSGiMetaObjectsExample();
            example.activate();
            
            // Demonstrate MetaObject lookup after activation
            example.demonstrateMetaObjectLookup();
            
            System.out.println("MetaObjects OSGI Example Bundle started successfully");
            
        } catch (Exception e) {
            System.err.println("Failed to start MetaObjects OSGI Example Bundle: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    @Override
    public void stop(BundleContext context) throws Exception {
        System.out.println("Stopping MetaObjects OSGI Example Bundle...");
        
        try {
            if (example != null) {
                example.deactivate();
                example = null;
            }
            
            System.out.println("MetaObjects OSGI Example Bundle stopped successfully");
            
        } catch (Exception e) {
            System.err.println("Error stopping MetaObjects OSGI Example Bundle: " + e.getMessage());
            e.printStackTrace();
            // Don't rethrow - we want to allow the bundle to stop
        }
    }
}