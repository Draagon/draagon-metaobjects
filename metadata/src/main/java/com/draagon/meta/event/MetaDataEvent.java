package com.draagon.meta.event;

import com.draagon.meta.MetaData;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * Base class for all MetaData-related events.
 * Provides immutable event data with timestamp and source information.
 * 
 * @param <T> The type of MetaData object that generated this event
 */
public abstract class MetaDataEvent<T extends MetaData> {
    
    private final T source;
    private final Instant timestamp;
    private final String eventId;
    
    protected MetaDataEvent(T source, String eventId) {
        this.source = Objects.requireNonNull(source, "Event source cannot be null");
        this.eventId = Objects.requireNonNull(eventId, "Event ID cannot be null");
        this.timestamp = Instant.now();
    }
    
    /**
     * Get the MetaData object that generated this event
     */
    public T getSource() {
        return source;
    }
    
    /**
     * Get the timestamp when this event was created
     */
    public Instant getTimestamp() {
        return timestamp;
    }
    
    /**
     * Get the unique identifier for this event
     */
    public String getEventId() {
        return eventId;
    }
    
    /**
     * Get the type of event
     */
    public abstract MetaDataEventType getEventType();
    
    /**
     * Get additional event-specific data
     */
    public abstract Optional<Object> getEventData();
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        MetaDataEvent<?> that = (MetaDataEvent<?>) obj;
        return Objects.equals(eventId, that.eventId) &&
               Objects.equals(timestamp, that.timestamp) &&
               Objects.equals(source, that.source);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(eventId, timestamp, source);
    }
    
    @Override
    public String toString() {
        return String.format("%s[id=%s, source=%s, timestamp=%s]",
            getClass().getSimpleName(), eventId, source.getName(), timestamp);
    }
    
    /**
     * Child added event
     */
    public static class ChildAdded extends MetaDataEvent<MetaData> {
        private final MetaData addedChild;
        
        public ChildAdded(MetaData source, MetaData addedChild) {
            super(source, "child-added-" + System.nanoTime());
            this.addedChild = Objects.requireNonNull(addedChild, "Added child cannot be null");
        }
        
        public MetaData getAddedChild() {
            return addedChild;
        }
        
        @Override
        public MetaDataEventType getEventType() {
            return MetaDataEventType.CHILD_ADDED;
        }
        
        @Override
        public Optional<Object> getEventData() {
            return Optional.of(addedChild);
        }
    }
    
    /**
     * Child removed event
     */
    public static class ChildRemoved extends MetaDataEvent<MetaData> {
        private final MetaData removedChild;
        
        public ChildRemoved(MetaData source, MetaData removedChild) {
            super(source, "child-removed-" + System.nanoTime());
            this.removedChild = Objects.requireNonNull(removedChild, "Removed child cannot be null");
        }
        
        public MetaData getRemovedChild() {
            return removedChild;
        }
        
        @Override
        public MetaDataEventType getEventType() {
            return MetaDataEventType.CHILD_REMOVED;
        }
        
        @Override
        public Optional<Object> getEventData() {
            return Optional.of(removedChild);
        }
    }
    
    /**
     * Child replaced event
     */
    public static class ChildReplaced extends MetaDataEvent<MetaData> {
        private final MetaData oldChild;
        private final MetaData newChild;
        
        public ChildReplaced(MetaData source, MetaData oldChild, MetaData newChild) {
            super(source, "child-replaced-" + System.nanoTime());
            this.oldChild = Objects.requireNonNull(oldChild, "Old child cannot be null");
            this.newChild = Objects.requireNonNull(newChild, "New child cannot be null");
        }
        
        public MetaData getOldChild() {
            return oldChild;
        }
        
        public MetaData getNewChild() {
            return newChild;
        }
        
        @Override
        public MetaDataEventType getEventType() {
            return MetaDataEventType.CHILD_REPLACED;
        }
        
        @Override
        public Optional<Object> getEventData() {
            return Optional.of(new ChildReplacementData(oldChild, newChild));
        }
        
        public record ChildReplacementData(MetaData oldChild, MetaData newChild) {}
    }
    
    /**
     * Property changed event
     */
    public static class PropertyChanged extends MetaDataEvent<MetaData> {
        private final String propertyName;
        private final Object oldValue;
        private final Object newValue;
        
        public PropertyChanged(MetaData source, String propertyName, Object oldValue, Object newValue) {
            super(source, "property-changed-" + System.nanoTime());
            this.propertyName = Objects.requireNonNull(propertyName, "Property name cannot be null");
            this.oldValue = oldValue;
            this.newValue = newValue;
        }
        
        public String getPropertyName() {
            return propertyName;
        }
        
        public Object getOldValue() {
            return oldValue;
        }
        
        public Object getNewValue() {
            return newValue;
        }
        
        @Override
        public MetaDataEventType getEventType() {
            return MetaDataEventType.PROPERTY_CHANGED;
        }
        
        @Override
        public Optional<Object> getEventData() {
            return Optional.of(new PropertyChangeData(propertyName, oldValue, newValue));
        }
        
        public record PropertyChangeData(String propertyName, Object oldValue, Object newValue) {}
    }
    
    /**
     * Validation completed event
     */
    public static class ValidationCompleted extends MetaDataEvent<MetaData> {
        private final boolean isValid;
        private final int errorCount;
        
        public ValidationCompleted(MetaData source, boolean isValid, int errorCount) {
            super(source, "validation-completed-" + System.nanoTime());
            this.isValid = isValid;
            this.errorCount = Math.max(0, errorCount);
        }
        
        public boolean isValid() {
            return isValid;
        }
        
        public int getErrorCount() {
            return errorCount;
        }
        
        @Override
        public MetaDataEventType getEventType() {
            return MetaDataEventType.VALIDATION_COMPLETED;
        }
        
        @Override
        public Optional<Object> getEventData() {
            return Optional.of(new ValidationData(isValid, errorCount));
        }
        
        public record ValidationData(boolean isValid, int errorCount) {}
    }
}