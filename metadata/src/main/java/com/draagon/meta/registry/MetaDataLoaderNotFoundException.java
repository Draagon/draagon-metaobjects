package com.draagon.meta.registry;

import com.draagon.meta.MetaDataException;

/**
 * Exception thrown when a MetaDataLoader cannot be found by name.
 * 
 * <p>This exception is thrown by {@link MetaDataLoaderRegistry} when attempting
 * to retrieve a loader that has not been registered.</p>
 * 
 * @since 6.0.0
 */
public class MetaDataLoaderNotFoundException extends MetaDataException {
    
    private static final long serialVersionUID = 1L;
    
    private final String loaderName;
    
    /**
     * Create exception with message
     * 
     * @param message Error message
     */
    public MetaDataLoaderNotFoundException(String message) {
        super(message);
        this.loaderName = null;
    }
    
    /**
     * Create exception with message and loader name
     * 
     * @param message Error message
     * @param loaderName Name of the loader that was not found
     */
    public MetaDataLoaderNotFoundException(String message, String loaderName) {
        super(message);
        this.loaderName = loaderName;
    }
    
    /**
     * Create exception with message and cause
     * 
     * @param message Error message
     * @param cause Underlying cause
     */
    public MetaDataLoaderNotFoundException(String message, Throwable cause) {
        super(message, cause);
        this.loaderName = null;
    }
    
    /**
     * Create exception with message, loader name, and cause
     * 
     * @param message Error message
     * @param loaderName Name of the loader that was not found
     * @param cause Underlying cause
     */
    public MetaDataLoaderNotFoundException(String message, String loaderName, Throwable cause) {
        super(message, cause);
        this.loaderName = loaderName;
    }
    
    /**
     * Get the name of the loader that was not found
     * 
     * @return Loader name, or null if not specified
     */
    public String getLoaderName() {
        return loaderName;
    }
    
    @Override
    public String toString() {
        if (loaderName != null) {
            return super.toString() + " [loaderName=" + loaderName + "]";
        }
        return super.toString();
    }
}