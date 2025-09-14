package com.draagon.meta.loader;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Thread-safe state management for MetaData loading lifecycle.
 * Provides atomic transitions between loading phases with error tracking.
 */
public class LoadingState {
    
    /**
     * Loading phases in order of progression
     */
    public enum Phase {
        UNINITIALIZED("Not yet initialized"),
        INITIALIZING("Currently initializing"),
        INITIALIZED("Initialization completed"),
        REGISTERING("Currently registering"),
        REGISTERED("Registration completed"),
        DESTROYED("Destroyed and no longer usable");
        
        private final String description;
        
        Phase(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
        
        @Override
        public String toString() {
            return name() + " (" + description + ")";
        }
    }
    
    private volatile Phase currentPhase = Phase.UNINITIALIZED;
    private final Object stateLock = new Object();
    private volatile Exception lastError = null;
    private final AtomicLong stateVersion = new AtomicLong(0);
    private final long creationTime = System.currentTimeMillis();
    
    /**
     * Attempt to transition from one phase to another atomically
     * @param expectedFrom The expected current phase
     * @param to The target phase
     * @return true if the transition succeeded, false if the current phase wasn't as expected
     */
    public boolean tryTransition(Phase expectedFrom, Phase to) {
        if (expectedFrom == null || to == null) {
            throw new IllegalArgumentException("Phases cannot be null");
        }
        
        synchronized (stateLock) {
            if (currentPhase == expectedFrom) {
                currentPhase = to;
                stateVersion.incrementAndGet();
                lastError = null; // Clear any previous errors on successful transition
                return true;
            }
            return false;
        }
    }
    
    /**
     * Force transition to a new phase (use with caution)
     * @param to The target phase
     */
    public void forceTransition(Phase to) {
        if (to == null) {
            throw new IllegalArgumentException("Target phase cannot be null");
        }
        
        synchronized (stateLock) {
            currentPhase = to;
            stateVersion.incrementAndGet();
            lastError = null;
        }
    }
    
    /**
     * Require that the current phase matches the expected phase
     * @param required The required phase
     * @throws IllegalStateException if the current phase doesn't match
     */
    public void requirePhase(Phase required) throws IllegalStateException {
        if (required == null) {
            throw new IllegalArgumentException("Required phase cannot be null");
        }
        
        Phase current = currentPhase;
        if (current != required) {
            StringBuilder errorMsg = new StringBuilder();
            errorMsg.append("Expected phase ").append(required)
                   .append(" but was ").append(current);
            
            if (lastError != null) {
                errorMsg.append(" (last error: ").append(lastError.getMessage()).append(")");
            }
            
            throw new IllegalStateException(errorMsg.toString());
        }
    }
    
    /**
     * Check if the current phase is one of the allowed phases
     * @param allowedPhases The phases that are acceptable
     * @return true if current phase is in the allowed set
     */
    public boolean isInPhase(Phase... allowedPhases) {
        if (allowedPhases == null || allowedPhases.length == 0) {
            return false;
        }
        
        Phase current = currentPhase;
        for (Phase allowed : allowedPhases) {
            if (current == allowed) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Set an error and optionally transition to a fallback phase
     * @param error The error that occurred
     * @param fallbackPhase Optional fallback phase to transition to
     */
    public void setError(Exception error, Phase fallbackPhase) {
        synchronized (stateLock) {
            this.lastError = error;
            if (fallbackPhase != null) {
                this.currentPhase = fallbackPhase;
            }
            stateVersion.incrementAndGet();
        }
    }
    
    /**
     * Set an error without changing the phase
     * @param error The error that occurred
     */
    public void setError(Exception error) {
        setError(error, null);
    }
    
    /**
     * Get the current loading phase
     * @return The current phase
     */
    public Phase getCurrentPhase() {
        return currentPhase;
    }
    
    /**
     * Get the last error that occurred, if any
     * @return Optional containing the last error
     */
    public Optional<Exception> getLastError() {
        return Optional.ofNullable(lastError);
    }
    
    /**
     * Get the current state version (increments on each change)
     * @return The current state version
     */
    public long getStateVersion() {
        return stateVersion.get();
    }
    
    /**
     * Get the time when this LoadingState was created
     * @return Creation timestamp in milliseconds
     */
    public long getCreationTime() {
        return creationTime;
    }
    
    /**
     * Get the time elapsed since creation
     * @return Elapsed time in milliseconds
     */
    public long getElapsedTime() {
        return System.currentTimeMillis() - creationTime;
    }
    
    /**
     * Check if there's currently an error
     * @return true if there's an active error
     */
    public boolean hasError() {
        return lastError != null;
    }
    
    /**
     * Clear any current error
     */
    public void clearError() {
        synchronized (stateLock) {
            lastError = null;
            stateVersion.incrementAndGet();
        }
    }
    
    /**
     * Check if the loading process is complete (REGISTERED phase)
     * @return true if loading is complete
     */
    public boolean isLoadingComplete() {
        return currentPhase == Phase.REGISTERED;
    }
    
    /**
     * Check if the loading process is in progress
     * @return true if loading is in progress
     */
    public boolean isLoadingInProgress() {
        return isInPhase(Phase.INITIALIZING, Phase.REGISTERING);
    }
    
    /**
     * Check if the loader is usable (initialized or registered)
     * @return true if the loader can be used
     */
    public boolean isUsable() {
        return isInPhase(Phase.INITIALIZED, Phase.REGISTERED);
    }
    
    /**
     * Check if the loader is destroyed
     * @return true if the loader is destroyed
     */
    public boolean isDestroyed() {
        return currentPhase == Phase.DESTROYED;
    }
    
    /**
     * Get a detailed status description
     * @return String describing the current state
     */
    public String getStatusDescription() {
        Phase current = currentPhase;
        StringBuilder status = new StringBuilder();
        status.append("Phase: ").append(current);
        
        if (lastError != null) {
            status.append(", Error: ").append(lastError.getClass().getSimpleName())
                  .append(" - ").append(lastError.getMessage());
        }
        
        status.append(", Elapsed: ").append(getElapsedTime()).append("ms");
        status.append(", Version: ").append(stateVersion.get());
        
        return status.toString();
    }
    
    @Override
    public String toString() {
        return "LoadingState{" +
                "phase=" + currentPhase +
                ", hasError=" + hasError() +
                ", version=" + stateVersion.get() +
                ", elapsed=" + getElapsedTime() + "ms" +
                '}';
    }
}