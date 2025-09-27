package com.metaobjects.demo.fishstore;

/**
 * Represents a fish breed
 */
public class Breed extends Base {
    private String name;
    private Integer agressionLevel;
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Integer getAgressionLevel() {
        return agressionLevel;
    }
    
    public void setAgressionLevel(Integer agressionLevel) {
        this.agressionLevel = agressionLevel;
    }
    
    @Override
    public String toString() {
        return "Breed{" +
                "id=" + getId() +
                ", name='" + name + '\'' +
                ", agressionLevel=" + agressionLevel +
                '}';
    }
}