/*
 * Copyright 2003 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */

package com.draagon.meta.field;

import com.draagon.meta.DataTypes;
import com.draagon.meta.MetaData;
import com.draagon.meta.MetaDataNotFoundException;
import com.draagon.meta.util.DataConverter;
import com.draagon.meta.validator.MetaValidator;
import com.draagon.meta.validator.MetaValidatorNotFoundException;
import com.draagon.meta.ValueException;
import com.draagon.meta.view.MetaView;
import com.draagon.meta.view.MetaViewNotFoundException;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.attr.MetaAttributeNotFoundException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * A MetaField represents a field of an object and is contained within a MetaClass.
 * It functions as both a proxy to get/set data within an object and also handles
 * accessing meta data about a field.
 *
 * @author Doug Mealing
 * @version 2.0
 */
public abstract class MetaField<T extends Object> extends MetaData implements MetaFieldTypes {
    //private static Log log = LogFactory.getLog( MetaField.class );

    public final static String TYPE_FIELD = "field";

    public final static String ATTR_LEN = "len";
    public final static String ATTR_VALIDATION = "validation";
    public final static String ATTR_DEFAULT_VIEW = "defaultView";
    public final static String ATTR_DEFAULT_VALUE = "defaultValue";

    //private int mType = 0;
    private T defaultValue = null;
    private int length = -1;

    private DataTypes dataType;

    public MetaField(String subtype, String name, DataTypes dataType) {
        super(TYPE_FIELD, subtype, name);
        this.dataType = dataType;
        //addAttributeDef( new AttributeDef( ATTR_LEN, String.class, false, "Length of the field" ));
        //addAttributeDef( new AttributeDef( ATTR_VALIDATION, String.class, false, "Comma delimited list of validators" ));
        //addAttributeDef( new AttributeDef( ATTR_DEFAULT_VALUE, String.class, false, "Default value for the MetaField" ));
    }

    /**
     * Gets the primary MetaData class
     */
    @Override
    public final Class<? extends MetaData> getMetaDataClass() {
        return MetaField.class;
    }

    /**
     * Returns the specific MetaClass in which this class is declared.<br>
     * WARNING: This may not return the MetaClass from which this MetaField was retrieved.
     *
     * @return The declaring MetaClass
     */
    public MetaObject getDeclaringObject() {
        return (MetaObject) getParent();
    }

    /**
     * Sets the Super Field
     */
    public void setSuperField(MetaField superField) {
        setSuperData(superField);
    }

    /**
     * Gets the Super Field
     */
    public MetaField getSuperField() {
        return (MetaField) getSuperData();
    }

    /**
     * Sets the default field value
     */
    public void setDefaultValue(T defVal) {
        defaultValue = defVal;

        if (!getValueClass().isInstance(defVal)) {
            // Convert as needed
            defVal = (T) DataConverter.toType(getDataType(), defVal);
            String def = defVal.toString();
        }

        defaultValue = (T) defVal;
    }

    /**
     * Gets the default field value
     */
    public T getDefaultValue() {
        return defaultValue;
    }

    /**
     * Returns the type of value
     */
    public DataTypes getDataType() {
        return dataType;
    }

    /**
     * Gets the type of value object class returned
     */
    public Class<?> getValueClass() {
        return dataType.getValueClass();
    }

    /**
     * Sets the object attribute represented by this MetaField
     */
    protected void setObjectAttribute(Object obj, Object val) {

        // Ensure the data types are accurate
        if (val != null && !getValueClass().isInstance(val))
            throw new ValueException("Invalid value [" + val + "], expected class [" + getValueClass().getName() + "]");

        // Perform validation -- Disabled for performance reasons
        //performValidation( obj, val );

        // Set the value on the object
        getDeclaringObject().setValue(this, obj, val);
    }

    /**
     * Gets the object attribute represented by this MetaField
     *
     * @deprecated Use getObjectValue
     */
    protected Object getObjectAttribute(Object obj) {
        return getObjectValue(obj);
    }

    /**
     * Gets the object attribute represented by this MetaField
     */
    protected Object getObjectValue(Object obj) {
        Object val = getDeclaringObject().getValue(this, obj);
        if (!getValueClass().isInstance(val)) {
            val = DataConverter.toType(dataType, val);
        }
        return val;
    }

    ////////////////////////////////////////////////////
    // VIEW METHODS

    /**
     * Whether the named MetaView exists
     */
    public boolean hasView(String name) {
        try {
            getView(name);
            return true;
        } catch (MetaViewNotFoundException e) {
            return false;
        }
    }

    /**
     * Adds a MetaView to this MetaField
     *
     * @param view MetaView to add
     */
    public void addMetaView(MetaView view) {
        addChild(view);
    }

    /**
     * Adds a MetaView to this MetaField
     *
     * @param view MetaView to add
     */
    public void addView(MetaView view) {
        addChild(view);
    }

    public Collection<MetaView> getViews() {
        return getChildren(MetaView.class, true);
    }

    public MetaView getDefaultView() {
        if (hasAttr(ATTR_DEFAULT_VIEW))
            return getView(getAttr(ATTR_DEFAULT_VIEW).getValueAsString());
        else
            return getFirstChild(MetaView.class);
    }

    public MetaView getView(String name) {
        try {
            return (MetaView) getChild(name, MetaView.class);
        } catch (MetaDataNotFoundException e) {
            throw new MetaViewNotFoundException("MetaView with name [" + name + "] not found in MetaField [" + toString() + "]", name);
        }
    }

    ////////////////////////////////////////////////////
    // VALIDATOR METHODS

    protected void performValidation(Object obj, Object val)  {
        // Run any defined validators
        if (hasAttr(ATTR_VALIDATION)) {
            getValidatorList(getAttr(ATTR_VALIDATION).getValueAsString()).forEach(v -> v.validate(obj, val));
        }
    }

    /**
     * Whether the named MetaValidator exists
     */
    public boolean hasValidator(String name) {
        try {
            getValidator(name);
            return true;
        } catch (MetaValidatorNotFoundException e) {
            return false;
        }
    }

    public void addMetaValidator(MetaValidator validator) {
        addChild(validator);
    }

    public Collection<?> getValidators() {
        return getChildren(MetaValidator.class, true);
    }

    /**
     * This method returns the list of validators based on the
     * comma delimited string name provided
     */
    public List<MetaValidator> getValidatorList(String list)
    //throws MetaValidatorNotFoundException
    {
        ArrayList<MetaValidator> validators = new ArrayList<MetaValidator>();

        while (list != null) {

            String validator = null;

            int i = list.indexOf(',');
            if (i >= 0) {
                validator = list.substring(0, i).trim();
                list = list.substring(i + 1);
            } else {
                validator = list.trim();
                list = null;
            }

            if (validator.length() > 0)
                validators.add(getValidator(validator));
        }

        return validators;
    }


    public MetaValidator getValidator(String name) {
        try {
            return (MetaValidator) getChild(name, MetaValidator.class);
        } catch (MetaDataNotFoundException e) {
            throw new MetaValidatorNotFoundException("MetaValidator with name [" + name + "] not found in MetaField [" + toString() + "]", name);
        }
    }

    ////////////////////////////////////////////////////
    // OBJECT SETTER METHODS

    public void setBoolean(Object obj, Boolean boolval)
    //throws MetaException
    {
        Object bv = boolval;

        if (boolval != null) {
            bv = DataConverter.toType(getDataType(), boolval);
        }

        setObjectAttribute(obj, bv);
    }

    public void setByte(Object obj, Byte byteval)
    //throws MetaException
    {
        Object bv = byteval;

        if (byteval != null) {
            bv = DataConverter.toType(getDataType(), byteval);
        }

        setObjectAttribute(obj, bv);
    }

    public void setShort(Object obj, Short shortval)
    //throws MetaException
    {
        Object sv = shortval;

        if (shortval != null) {
            sv = DataConverter.toType(getDataType(), shortval);
        }

        setObjectAttribute(obj, sv);
    }

    public void setInt(Object obj, Integer intval)
    //throws MetaException
    {
        Object iv = intval;

        if (intval != null) {
            iv = DataConverter.toType(getDataType(), intval);
        }

        setObjectAttribute(obj, iv);
    }

    public void setLong(Object obj, Long longval)
    //throws MetaException
    {
        Object lv = longval;

        if (longval != null) {
            lv = DataConverter.toType(getDataType(), longval);
        }

        setObjectAttribute(obj, lv);
    }

    public void setFloat(Object obj, Float floatval)
    //throws MetaException
    {
        Object fv = floatval;

        if (floatval != null) {
            fv = DataConverter.toType(getDataType(), floatval);
        }

        setObjectAttribute(obj, fv);
    }

    public void setDouble(Object obj, Double doubval)
    //throws MetaException
    {
        Object dv = doubval;

        if (doubval != null) {
            dv = DataConverter.toType(getDataType(), doubval);
        }

        setObjectAttribute(obj, dv);
    }

    public void setString(Object obj, String strval)
    //throws MetaException
    {
        Object s = strval;

        DataTypes dt = getDataType();

        // If an empty string, convert to a null if numeric or a date
        if (strval != null && strval.trim().isEmpty() && (dt.isNumeric() || dt.isDate()))
            strval = null;

        // Handle the values
        if (strval != null) {
            s = DataConverter.toType(getDataType(), strval);
        }

        setObjectAttribute(obj, s);
    }

    public void setDate(Object obj, Date dateval)
    //throws MetaException
    {
        Object lv = dateval;

        if (dateval != null) {
            lv = DataConverter.toType(getDataType(), dateval);
        }

        setObjectAttribute(obj, lv);
    }

    public void setObject(Object obj, Object objval)
    //throws MetaException
    {
        Object o = objval;

        if (objval != null) {
            o = DataConverter.toType(getDataType(), objval);
        }

        setObjectAttribute(obj, o);
    }


    ////////////////////////////////////////////////////
    // OBJECT GETTER METHODS


    public Boolean getBoolean(Object obj)
    //throws MetaException
    {
        return DataConverter.toBoolean(getObjectAttribute(obj));
    }

    public Byte getByte(Object obj)
    //throws MetaException
    {
        return DataConverter.toByte(getObjectAttribute(obj));
    }

    public Short getShort(Object obj)
    //throws MetaException
    {
        return DataConverter.toShort(getObjectAttribute(obj));
    }

    public Integer getInt(Object obj)
    //throws MetaException
    {
        return DataConverter.toInt(getObjectAttribute(obj));
    }

    public Long getLong(Object obj)
    //throws MetaException
    {
        return DataConverter.toLong(getObjectAttribute(obj));
    }

    public Float getFloat(Object obj)
    //throws MetaException
    {
        return DataConverter.toFloat(getObjectAttribute(obj));
    }

    public Double getDouble(Object obj)
    //throws MetaException
    {
        return DataConverter.toDouble(getObjectAttribute(obj));
    }

    public String getString(Object obj)
    //throws MetaException
    {
        return DataConverter.toString(getObjectAttribute(obj));
    }

    public Date getDate(Object obj)
    //throws MetaException
    {
        return DataConverter.toDate(getObjectAttribute(obj));
    }

    public Object getObject(Object obj)
    //throws MetaException
    {
        return getObjectAttribute(obj);
    }

    ////////////////////////////////////////////////////
    // MISC METHODS

    // WARNING:  Where should this really go?
  /*public int getLength()
  {
    if ( this.length >= 0 ) return this.length;

    int len = 0;

    if ( getSuperField() != null )
        len = getSuperField().getLength();

    try {
        len = Integer.parseInt( (String) getAttribute( ATTR_LEN ));
    } catch( Exception e ) {}

    if ( len <= 0 )
      switch( getDataType() )
      {
        case BOOLEAN: len = 1; break;
        case BYTE: len = 4; break;
        case SHORT: len = 6; break;
        case INT: len = 10; break;
        case LONG: len = 15; break;
        case FLOAT: len = 12; break;
        case DOUBLE: len = 16; break;
        case STRING: len = 50; break;
        case DATE: len = 15; break;
        default:  len = 10; break;
      }

    this.length = length;
    return this.length;
  }*/

    public Object clone() {
        MetaField mf = (MetaField) super.clone();
        mf.defaultValue = defaultValue;
        return mf;
    }
}
