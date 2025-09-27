package com.metaobjects.registry.osgi;

/**
 * Mock OSGI Bundle for testing bundle lifecycle scenarios.
 * 
 * <p>This class simulates an OSGI Bundle without requiring actual OSGI dependencies,
 * allowing us to test bundle lifecycle management in unit tests.</p>
 * 
 * @since 6.0.0
 */
public class MockBundle {
    
    private final String symbolicName;
    private final String version;
    private final long bundleId;
    private final long lastModified;
    
    /**
     * Create mock bundle
     * 
     * @param symbolicName Bundle symbolic name
     * @param version Bundle version
     * @param bundleId Bundle ID
     */
    public MockBundle(String symbolicName, String version, long bundleId) {
        this.symbolicName = symbolicName;
        this.version = version;
        this.bundleId = bundleId;
        this.lastModified = System.currentTimeMillis();
    }
    
    /**
     * Get bundle symbolic name (simulates Bundle.getSymbolicName())
     * 
     * @return Bundle symbolic name
     */
    public String getSymbolicName() {
        return symbolicName;
    }
    
    /**
     * Get bundle version (simulates Bundle.getVersion())
     * 
     * @return Bundle version as string
     */
    public String getVersion() {
        return version;
    }
    
    /**
     * Get bundle ID (simulates Bundle.getBundleId())
     * 
     * @return Bundle ID
     */
    public long getBundleId() {
        return bundleId;
    }
    
    /**
     * Get last modified time (simulates Bundle.getLastModified())
     * 
     * @return Last modified timestamp
     */
    public long getLastModified() {
        return lastModified;
    }
    
    /**
     * Get bundle state (simulates Bundle.getState())
     * 
     * @return Mock bundle state (always returns ACTIVE = 32)
     */
    public int getState() {
        return 32; // Bundle.ACTIVE
    }
    
    @Override
    public String toString() {
        return String.format("MockBundle[%s:%s-%d]", symbolicName, version, bundleId);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        MockBundle that = (MockBundle) obj;
        return bundleId == that.bundleId;
    }
    
    @Override
    public int hashCode() {
        return Long.hashCode(bundleId);
    }
}