package com.draagon.meta.event;

import com.draagon.meta.MetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

/**
 * Thread-safe event publisher for MetaData events.
 * Manages listeners and publishes events to interested parties.
 */
public class MetaDataEventPublisher {
    
    private static final Logger log = LoggerFactory.getLogger(MetaDataEventPublisher.class);
    
    private final List<MetaDataEventListener> listeners = new CopyOnWriteArrayList<>();
    private final Executor executor;
    private final boolean asyncPublishing;
    private volatile boolean enabled = true;
    
    /**
     * Create a synchronous event publisher
     */
    public MetaDataEventPublisher() {
        this(false, null);
    }
    
    /**
     * Create an event publisher with specified configuration
     * 
     * @param asyncPublishing Whether to publish events asynchronously
     * @param executor Custom executor for async publishing (null = use common pool)
     */
    public MetaDataEventPublisher(boolean asyncPublishing, Executor executor) {
        this.asyncPublishing = asyncPublishing;
        this.executor = executor != null ? executor : ForkJoinPool.commonPool();
    }
    
    /**
     * Add an event listener
     * 
     * @param listener The listener to add
     */
    public void addListener(MetaDataEventListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener cannot be null");
        }
        
        listeners.add(listener);
        
        // Sort by priority (higher priority first)
        listeners.sort((l1, l2) -> Integer.compare(l2.getPriority(), l1.getPriority()));
        
        log.debug("Added event listener: {} (total: {})", listener.getName(), listeners.size());
    }
    
    /**
     * Remove an event listener
     * 
     * @param listener The listener to remove
     * @return true if the listener was removed
     */
    public boolean removeListener(MetaDataEventListener listener) {
        boolean removed = listeners.remove(listener);
        if (removed) {
            log.debug("Removed event listener: {} (total: {})", listener.getName(), listeners.size());
        }
        return removed;
    }
    
    /**
     * Remove all listeners
     */
    public void clearListeners() {
        int count = listeners.size();
        listeners.clear();
        log.debug("Cleared {} event listeners", count);
    }
    
    /**
     * Get the number of registered listeners
     */
    public int getListenerCount() {
        return listeners.size();
    }
    
    /**
     * Check if publishing is enabled
     */
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * Enable or disable event publishing
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        log.debug("Event publishing {}", enabled ? "enabled" : "disabled");
    }
    
    /**
     * Publish an event to all interested listeners
     * 
     * @param event The event to publish
     */
    public void publishEvent(MetaDataEvent<?> event) {
        if (!enabled || event == null) {
            return;
        }
        
        if (listeners.isEmpty()) {
            log.trace("No listeners registered for event: {}", event.getEventType());
            return;
        }
        
        log.trace("Publishing event: {} from source: {}", event.getEventType(), event.getSource().getName());
        
        if (asyncPublishing) {
            executor.execute(() -> doPublishEvent(event));
        } else {
            doPublishEvent(event);
        }
    }
    
    /**
     * Internal method to actually publish the event
     */
    private void doPublishEvent(MetaDataEvent<?> event) {
        int deliveredCount = 0;
        
        for (MetaDataEventListener listener : listeners) {
            try {
                // Check if listener is interested in this event type and source
                if (listener.isInterestedIn(event.getEventType()) && 
                    listener.isInterestedIn(event.getSource())) {
                    
                    // Dispatch to specific method if available
                    dispatchToSpecificMethod(listener, event);
                    deliveredCount++;
                    
                    log.trace("Delivered event to listener: {}", listener.getName());
                }
            } catch (Exception e) {
                log.error("Error delivering event to listener: {}", listener.getName(), e);
            }
        }
        
        log.trace("Published event {} to {} listeners", event.getEventType(), deliveredCount);
    }
    
    /**
     * Dispatch event to specific listener method if available
     */
    private void dispatchToSpecificMethod(MetaDataEventListener listener, MetaDataEvent<?> event) {
        switch (event.getEventType()) {
            case CHILD_ADDED -> {
                if (event instanceof MetaDataEvent.ChildAdded childAdded) {
                    listener.onChildAdded(childAdded);
                }
            }
            case CHILD_REMOVED -> {
                if (event instanceof MetaDataEvent.ChildRemoved childRemoved) {
                    listener.onChildRemoved(childRemoved);
                }
            }
            case CHILD_REPLACED -> {
                if (event instanceof MetaDataEvent.ChildReplaced childReplaced) {
                    listener.onChildReplaced(childReplaced);
                }
            }
            case PROPERTY_CHANGED -> {
                if (event instanceof MetaDataEvent.PropertyChanged propertyChanged) {
                    listener.onPropertyChanged(propertyChanged);
                }
            }
            case VALIDATION_COMPLETED -> {
                if (event instanceof MetaDataEvent.ValidationCompleted validationCompleted) {
                    listener.onValidationCompleted(validationCompleted);
                }
            }
            default -> listener.onEvent(event);
        }
    }
    
    /**
     * Convenience method to publish child added event
     */
    public void publishChildAdded(MetaData source, MetaData child) {
        publishEvent(new MetaDataEvent.ChildAdded(source, child));
    }
    
    /**
     * Convenience method to publish child removed event
     */
    public void publishChildRemoved(MetaData source, MetaData child) {
        publishEvent(new MetaDataEvent.ChildRemoved(source, child));
    }
    
    /**
     * Convenience method to publish child replaced event
     */
    public void publishChildReplaced(MetaData source, MetaData oldChild, MetaData newChild) {
        publishEvent(new MetaDataEvent.ChildReplaced(source, oldChild, newChild));
    }
    
    /**
     * Convenience method to publish property changed event
     */
    public void publishPropertyChanged(MetaData source, String propertyName, Object oldValue, Object newValue) {
        publishEvent(new MetaDataEvent.PropertyChanged(source, propertyName, oldValue, newValue));
    }
    
    /**
     * Convenience method to publish validation completed event
     */
    public void publishValidationCompleted(MetaData source, boolean isValid, int errorCount) {
        publishEvent(new MetaDataEvent.ValidationCompleted(source, isValid, errorCount));
    }
    
    /**
     * Get publisher statistics
     */
    public PublisherStats getStats() {
        return new PublisherStats(
            listeners.size(),
            enabled,
            asyncPublishing,
            executor.getClass().getSimpleName()
        );
    }
    
    /**
     * Publisher statistics record
     */
    public record PublisherStats(
        int listenerCount,
        boolean enabled,
        boolean asyncPublishing,
        String executorType
    ) {}
}