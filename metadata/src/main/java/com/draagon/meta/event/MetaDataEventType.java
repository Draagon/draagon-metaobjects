package com.draagon.meta.event;

/**
 * Enumeration of MetaData event types for categorizing different
 * kinds of events that can occur in the MetaData system.
 */
public enum MetaDataEventType {
    
    /**
     * A child MetaData object was added
     */
    CHILD_ADDED("child.added", "Child Added"),
    
    /**
     * A child MetaData object was removed
     */
    CHILD_REMOVED("child.removed", "Child Removed"),
    
    /**
     * A child MetaData object was replaced
     */
    CHILD_REPLACED("child.replaced", "Child Replaced"),
    
    /**
     * A property of the MetaData object was changed
     */
    PROPERTY_CHANGED("property.changed", "Property Changed"),
    
    /**
     * Validation was completed on the MetaData object
     */
    VALIDATION_COMPLETED("validation.completed", "Validation Completed"),
    
    /**
     * The MetaData object was created
     */
    CREATED("metadata.created", "MetaData Created"),
    
    /**
     * The MetaData object was destroyed/deleted
     */
    DESTROYED("metadata.destroyed", "MetaData Destroyed"),
    
    /**
     * The MetaData object was cloned
     */
    CLONED("metadata.cloned", "MetaData Cloned"),
    
    /**
     * Cache was cleared for the MetaData object
     */
    CACHE_CLEARED("cache.cleared", "Cache Cleared"),
    
    /**
     * An error occurred during MetaData operations
     */
    ERROR("error.occurred", "Error Occurred");
    
    private final String eventCode;
    private final String displayName;
    
    MetaDataEventType(String eventCode, String displayName) {
        this.eventCode = eventCode;
        this.displayName = displayName;
    }
    
    /**
     * Get the unique code for this event type
     */
    public String getEventCode() {
        return eventCode;
    }
    
    /**
     * Get the human-readable display name
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Find event type by code
     */
    public static MetaDataEventType fromCode(String code) {
        for (MetaDataEventType type : values()) {
            if (type.eventCode.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown event code: " + code);
    }
    
    /**
     * Check if this is a child-related event
     */
    public boolean isChildEvent() {
        return this == CHILD_ADDED || this == CHILD_REMOVED || this == CHILD_REPLACED;
    }
    
    /**
     * Check if this is a lifecycle event
     */
    public boolean isLifecycleEvent() {
        return this == CREATED || this == DESTROYED || this == CLONED;
    }
    
    /**
     * Check if this is an error event
     */
    public boolean isErrorEvent() {
        return this == ERROR;
    }
    
    @Override
    public String toString() {
        return displayName + " (" + eventCode + ")";
    }
}