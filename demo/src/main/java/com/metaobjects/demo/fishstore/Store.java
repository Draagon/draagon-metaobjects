package com.metaobjects.demo.fishstore;

/**
 * Represents a fish store
 */
public class Store extends Base {
    private String name;
    private Integer maxTanks;
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Integer getMaxTanks() {
        return maxTanks;
    }
    
    public void setMaxTanks(Integer maxTanks) {
        this.maxTanks = maxTanks;
    }
    
    @Override
    public String toString() {
        return "Store{" +
                "id=" + getId() +
                ", name='" + name + '\'' +
                ", maxTanks=" + maxTanks +
                '}';
    }
}