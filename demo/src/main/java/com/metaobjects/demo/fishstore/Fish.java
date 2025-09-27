package com.metaobjects.demo.fishstore;

/**
 * Represents a fish
 */
public class Fish extends Base {
    private String breedName;
    private Breed breed;
    private Integer length;
    private Integer weight;
    
    public String getBreedName() {
        return breedName;
    }
    
    public void setBreedName(String breedName) {
        this.breedName = breedName;
    }
    
    public Breed getBreed() {
        return breed;
    }
    
    public void setBreed(Breed breed) {
        this.breed = breed;
    }
    
    public Integer getLength() {
        return length;
    }
    
    public void setLength(Integer length) {
        this.length = length;
    }
    
    public Integer getWeight() {
        return weight;
    }
    
    public void setWeight(Integer weight) {
        this.weight = weight;
    }
    
    @Override
    public String toString() {
        return "Fish{" +
                "id=" + getId() +
                ", breedName='" + breedName + '\'' +
                ", length=" + length +
                ", weight=" + weight +
                '}';
    }
}