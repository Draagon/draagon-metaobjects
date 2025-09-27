package com.metaobjects.demo.fishstore;

/**
 * Base class for all fishstore entities
 */
public abstract class Base {
    private Long id;
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Base base = (Base) obj;
        return id != null ? id.equals(base.id) : base.id == null;
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}