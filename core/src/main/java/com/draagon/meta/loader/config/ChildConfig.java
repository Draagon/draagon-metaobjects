package com.draagon.meta.loader.config;

import com.draagon.meta.MetaException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class ChildConfig {

    // Valid type/subType/name combinations that include '*'
    private final String type;
    private final String subType;
    private final String name;

    // Specialized behaviors
    private Set<String> nameAliases = null;     // aliases for the names
    //private Boolean required = null;            // is the attribute required
    //private Boolean autoCreate = null;          // whether to auto create if it doesn't exist

    // Support for value defaults and validations
    //private String defaultValue = null;         // default value if not specified
    //private Integer minValue = null;            // minimum value if numeric
    //private Integer maxValue = null;            // maximum value if numeric

    // Support for inline attributes
    //private String inlineAttr = null;           // values:  disallow, optional, required
    //private String inlineAttrName = null;       // Name when it's an attribute
    //private String inlineAttrValueMap = null;   // What to map the value to

    private String createdFromFile = null;

    public ChildConfig( String type, String subType, String name ) {
        this.type = type;
        this.subType = subType;
        this.name = name;
    }

    ///////////////////////////////////////////////////////////
    // GETTERS AND SETTERS

    public String getType() {
        return type;
    }

    public String getSubType() {
        return subType;
    }

    public String getName() {
        return name;
    }

    public Set<String> getNameAliases() {
        return nameAliases;
    }

    public void setNameAliases(Set<String> nameAliases) {
        this.nameAliases = nameAliases;
    }

    /*public Boolean getRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public Boolean getAutoCreate() {
        return autoCreate;
    }

    public void setAutoCreate(boolean autoCreate) {
        this.autoCreate = autoCreate;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public Integer getMinValue() {
        return minValue;
    }

    public void setMinValue(int minValue) {
        this.minValue = minValue;
    }

    public Integer getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(int maxValue) {
        this.maxValue = maxValue;
    }

    public String getInlineAttr() {
        return inlineAttr;
    }

    public void setInlineAttr(String inlineAttr) {
        this.inlineAttr = inlineAttr;
    }

    public String getInlineAttrName() {
        return inlineAttrName;
    }

    public void setInlineAttrName(String inlineAttrName) {
        this.inlineAttrName = inlineAttrName;
    }

    public String getInlineAttrValueMap() {
        return inlineAttrValueMap;
    }

    public void setInlineAttrValueMap(String inlineAttrValueMap) {
        this.inlineAttrValueMap = inlineAttrValueMap;
    }*/

    public void setAutoCreatedFromFile( String file ) {
        this.createdFromFile = file;
    }

    public boolean wasAutoCreated() {
        return createdFromFile != null;
    }

    public String getCreatedFromFile() {
        return createdFromFile;
    }

    ///////////////////////////////////////////////////////////////////////
    // MISC METHODS

    public void merge( ChildConfig cc ) {
        if ( !type.equals( cc.type )
                || !subType.equals( cc.subType )
                || !name.equals( cc.name )) {
            throw new MetaException("Cannot merge when different type/subtype/name: ["+toString()+"] merged with ["+cc.toString()+"]");
        }
        if ( cc.nameAliases != null ) mergeNameAliases( cc.getNameAliases() );
        //if ( cc.required != null ) required = cc.required;
        //if ( cc.autoCreate != null ) autoCreate = cc.autoCreate;
        //if ( cc.defaultValue != null ) defaultValue = cc.defaultValue;
        //if ( cc.minValue != null ) minValue = cc.minValue;
        //if ( cc.maxValue != null ) maxValue = cc.maxValue;
        //if ( cc.inlineAttr != null ) inlineAttr = cc.inlineAttr;
        //if ( cc.inlineAttrName != null ) inlineAttrName = cc.inlineAttrName;
        //if ( cc.inlineAttrValueMap != null ) inlineAttrValueMap = cc.inlineAttrValueMap;
    }

    private void mergeNameAliases(Set<String> nameAliases) {
        if ( nameAliases == null ) {
            this.nameAliases = nameAliases;
        }
        else if ( nameAliases != null ) {
            this.nameAliases.addAll( nameAliases );
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChildConfig that = (ChildConfig) o;
        return Objects.equals(type, that.type) &&
                Objects.equals(subType, that.subType) &&
                Objects.equals(name, that.name) &&
                Objects.equals(nameAliases, that.nameAliases);// &&
                //Objects.equals(required, that.required) &&
                //Objects.equals(autoCreate, that.autoCreate) &&
                //Objects.equals(defaultValue, that.defaultValue) &&
                //Objects.equals(minValue, that.minValue) &&
                //Objects.equals(maxValue, that.maxValue) &&
                //Objects.equals(inlineAttr, that.inlineAttr) &&
                //Objects.equals(inlineAttrName, that.inlineAttrName) &&
                //Objects.equals(inlineAttrValueMap, that.inlineAttrValueMap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, subType, name, nameAliases); //, required, autoCreate, defaultValue, minValue, maxValue, inlineAttr, inlineAttrName, inlineAttrValueMap);
    }

    @Override
    public String toString() {
        return "ChildConfig{" +
                "type='" + type + '\'' +
                ", subType='" + subType + '\'' +
                ", name='" + name + '\'' +
                ", nameAliases=" + nameAliases +
                ", wasAutoCreated=" + wasAutoCreated() +
                //", required=" + required +
                //", autoCreate=" + autoCreate +
                //", defaultValue='" + defaultValue + '\'' +
                //", minValue=" + minValue +
                //", maxValue=" + maxValue +
                //", inlineAttr='" + inlineAttr + '\'' +
                //", inlineAttrName='" + inlineAttrName + '\'' +
                //", inlineAttrValueMap='" + inlineAttrValueMap + '\'' +
                '}';
    }
}
