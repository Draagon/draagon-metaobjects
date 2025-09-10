package com.draagon.meta.event;

import com.draagon.meta.MetaData;

/**
 * Interface for listening to MetaData events.
 * Implementations can handle specific types of events or all events.
 */
public interface MetaDataEventListener {
    
    /**
     * Handle a MetaData event
     * 
     * @param event The event that occurred
     */
    void onEvent(MetaDataEvent<?> event);
    
    /**
     * Check if this listener is interested in the given event type
     * Default implementation returns true for all events
     * 
     * @param eventType The event type to check
     * @return true if this listener should receive events of this type
     */
    default boolean isInterestedIn(MetaDataEventType eventType) {
        return true;
    }
    
    /**
     * Check if this listener is interested in events from the given source
     * Default implementation returns true for all sources
     * 
     * @param source The MetaData object that would generate the event
     * @return true if this listener should receive events from this source
     */
    default boolean isInterestedIn(MetaData source) {
        return true;
    }
    
    /**
     * Get the priority of this listener (higher numbers = higher priority)
     * Default implementation returns 0 (normal priority)
     * 
     * @return The listener priority
     */
    default int getPriority() {
        return 0;
    }
    
    /**
     * Get a name for this listener (for debugging/logging)
     * Default implementation returns the class name
     * 
     * @return The listener name
     */
    default String getName() {
        return getClass().getSimpleName();
    }
    
    // Convenience methods for specific event types
    
    /**
     * Handle child added event
     */
    default void onChildAdded(MetaDataEvent.ChildAdded event) {
        onEvent(event);
    }
    
    /**
     * Handle child removed event
     */
    default void onChildRemoved(MetaDataEvent.ChildRemoved event) {
        onEvent(event);
    }
    
    /**
     * Handle child replaced event
     */
    default void onChildReplaced(MetaDataEvent.ChildReplaced event) {
        onEvent(event);
    }
    
    /**
     * Handle property changed event
     */
    default void onPropertyChanged(MetaDataEvent.PropertyChanged event) {
        onEvent(event);
    }
    
    /**
     * Handle validation completed event
     */
    default void onValidationCompleted(MetaDataEvent.ValidationCompleted event) {
        onEvent(event);
    }
}