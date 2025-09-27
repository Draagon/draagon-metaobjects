package com.metaobjects.demo.fishstore;

import java.util.List;

/**
 * Represents a fish tank
 */
public class Tank extends Base {
    private Integer num;
    private Integer maxFish;
    private List<Fish> fishes;
    
    public Integer getNum() {
        return num;
    }
    
    public void setNum(Integer num) {
        this.num = num;
    }
    
    public Integer getMaxFish() {
        return maxFish;
    }
    
    public void setMaxFish(Integer maxFish) {
        this.maxFish = maxFish;
    }
    
    public List<Fish> getFishes() {
        return fishes;
    }
    
    public void setFishes(List<Fish> fishes) {
        this.fishes = fishes;
    }
    
    @Override
    public String toString() {
        return "Tank{" +
                "id=" + getId() +
                ", num=" + num +
                ", maxFish=" + maxFish +
                ", fishCount=" + (fishes != null ? fishes.size() : 0) +
                '}';
    }
}