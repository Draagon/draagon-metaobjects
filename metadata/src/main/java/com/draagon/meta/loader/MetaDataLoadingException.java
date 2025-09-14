package com.draagon.meta.loader;

import com.draagon.meta.MetaDataException;

/**
 * Exception thrown during MetaData loading operations.
 * Provides enhanced error information for loading failures.
 */
public class MetaDataLoadingException extends MetaDataException {
    
    private final String loaderName;
    private final LoadingState.Phase failedPhase;
    private final long loadingTime;
    
    /**
     * Create a loading exception with basic information
     * @param message The error message
     */
    public MetaDataLoadingException(String message) {
        super(message);
        this.loaderName = null;
        this.failedPhase = null;
        this.loadingTime = 0;
    }
    
    /**
     * Create a loading exception with a cause
     * @param message The error message
     * @param cause The underlying cause
     */
    public MetaDataLoadingException(String message, Throwable cause) {
        super(message, cause);
        this.loaderName = null;
        this.failedPhase = null;
        this.loadingTime = 0;
    }
    
    /**
     * Create a loading exception with detailed loading context
     * @param message The error message
     * @param loaderName The name of the loader that failed
     * @param failedPhase The phase where loading failed
     * @param loadingTime Time spent loading before failure (in milliseconds)
     */
    public MetaDataLoadingException(String message, String loaderName, 
                                   LoadingState.Phase failedPhase, long loadingTime) {
        super(enhanceMessage(message, loaderName, failedPhase, loadingTime));
        this.loaderName = loaderName;
        this.failedPhase = failedPhase;
        this.loadingTime = loadingTime;
    }
    
    /**
     * Create a loading exception with detailed loading context and cause
     * @param message The error message
     * @param loaderName The name of the loader that failed
     * @param failedPhase The phase where loading failed
     * @param loadingTime Time spent loading before failure (in milliseconds)
     * @param cause The underlying cause
     */
    public MetaDataLoadingException(String message, String loaderName, 
                                   LoadingState.Phase failedPhase, long loadingTime, 
                                   Throwable cause) {
        super(enhanceMessage(message, loaderName, failedPhase, loadingTime), cause);
        this.loaderName = loaderName;
        this.failedPhase = failedPhase;
        this.loadingTime = loadingTime;
    }
    
    /**
     * Get the name of the loader that failed
     * @return The loader name, or null if not available
     */
    public String getLoaderName() {
        return loaderName;
    }
    
    /**
     * Get the phase where loading failed
     * @return The failed phase, or null if not available
     */
    public LoadingState.Phase getFailedPhase() {
        return failedPhase;
    }
    
    /**
     * Get the time spent loading before failure
     * @return Loading time in milliseconds
     */
    public long getLoadingTime() {
        return loadingTime;
    }
    
    /**
     * Check if this exception has detailed loading context
     * @return true if loader name and failed phase are available
     */
    public boolean hasLoadingContext() {
        return loaderName != null && failedPhase != null;
    }
    
    /**
     * Enhance the error message with loading context
     */
    private static String enhanceMessage(String message, String loaderName, 
                                        LoadingState.Phase failedPhase, long loadingTime) {
        if (loaderName == null && failedPhase == null) {
            return message;
        }
        
        StringBuilder enhanced = new StringBuilder(message);
        enhanced.append("\n--- Loading Context ---");
        
        if (loaderName != null) {
            enhanced.append("\nLoader: ").append(loaderName);
        }
        
        if (failedPhase != null) {
            enhanced.append("\nFailed Phase: ").append(failedPhase);
        }
        
        if (loadingTime > 0) {
            enhanced.append("\nLoading Time: ").append(loadingTime).append("ms");
        }
        
        return enhanced.toString();
    }
}