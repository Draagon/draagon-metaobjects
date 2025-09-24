/*
 * Copyright 2002 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta.object.pojo;

import com.draagon.meta.InvalidMetaDataException;
import com.draagon.meta.InvalidValueException;
import com.draagon.meta.MetaData;
import com.draagon.meta.MetaDataException;
import com.draagon.meta.ValidationResult;
import com.draagon.meta.attr.StringAttribute;
// Constraint registration now handled by consolidated MetaDataRegistry
import com.draagon.meta.field.MetaField;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.registry.MetaDataRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.draagon.meta.object.MetaObject.SUBTYPE_BASE;
import java.lang.reflect.*;

/**
 * MetaObject that supports POJO objects with unified registry registration.
 *
 * @version 6.0
 */
@SuppressWarnings("serial")
public class PojoMetaObject extends MetaObject
{
    private static final Logger log = LoggerFactory.getLogger(PojoMetaObject.class);

    public final static String SUBTYPE_POJO = "pojo";
    public final static String ATTR_CLASS_NAME = "className";
    public final static String ATTR_PACKAGE_NAME = "packageName";

    public final static String CACHE_PARAM_GETTER_METHOD = "getterMethod";
    public final static String CACHE_PARAM_SETTER_METHOD = "setterMethod";

    // Unified registry self-registration
    static {
        try {
            MetaDataRegistry.getInstance().registerType(PojoMetaObject.class, def -> def
                .type(TYPE_OBJECT).subType(SUBTYPE_POJO)
                .description("POJO MetaObject with reflection-based field access")

                // INHERIT FROM BASE OBJECT
                .inheritsFrom(TYPE_OBJECT, SUBTYPE_BASE)

                // POJO-SPECIFIC ATTRIBUTES ONLY (base attributes inherited)
                .optionalAttribute(ATTR_CLASS_NAME, "string")
                .optionalAttribute(ATTR_PACKAGE_NAME, "string")
                
                // TEST-SPECIFIC ATTRIBUTES (for codegen tests)
                .optionalAttribute("dbTable", "string")
                .optionalAttribute("hasAuditing", "boolean")
                .optionalAttribute("hasJpa", "boolean")
                .optionalAttribute("hasValidation", "boolean")
                .optionalAttribute("implements", "string")

                // CHILD REQUIREMENTS INHERITED FROM BASE OBJECT:
                // - All field types (field.*)
                // - Other objects (object.*)
                // - Keys (key.*)
                // - Attributes (attr.*)
                // - Validators (validator.*)
                // - Views (view.*)
            );
            
            log.debug("Registered PojoMetaObject type with unified registry");
            
            // Register PojoMetaObject-specific constraints using consolidated registry
            setupPojoMetaObjectConstraints(MetaDataRegistry.getInstance());
            
        } catch (Exception e) {
            log.error("Failed to register PojoMetaObject type with unified registry", e);
        }
    }
    
    /**
     * Setup PojoMetaObject-specific constraints using consolidated registry
     *
     * @param registry The MetaDataRegistry to use for constraint registration
     */
    private static void setupPojoMetaObjectConstraints(MetaDataRegistry registry) {
        try {
            // PLACEMENT CONSTRAINT: POJO objects CAN have className attribute
            registry.registerPlacementConstraint(
                "pojoobject.classname.placement",
                "POJO objects can have className attribute",
                (metadata) -> metadata instanceof PojoMetaObject,
                (child) -> child instanceof StringAttribute &&
                          child.getName().equals(ATTR_CLASS_NAME)
            );

            // PLACEMENT CONSTRAINT: POJO objects CAN have packageName attribute
            registry.registerPlacementConstraint(
                "pojoobject.packagename.placement",
                "POJO objects can have packageName attribute",
                (metadata) -> metadata instanceof PojoMetaObject,
                (child) -> child instanceof StringAttribute &&
                          child.getName().equals(ATTR_PACKAGE_NAME)
            );

            log.debug("Registered PojoMetaObject-specific constraints using consolidated registry");

        } catch (Exception e) {
            log.error("Failed to register PojoMetaObject constraints", e);
        }
    }

    /**
     * Constructs a bean MetaClass
     */
    public PojoMetaObject( String name ) {
        super( SUBTYPE_POJO, name );
    }

    /**
     * Constructs a bean MetaClass
     */
    protected PojoMetaObject( String subType, String name ) {
        super( subType, name );
    }

    /**
     * Manually create a PojoMetaObject with the specified name
     * @param name Name for the PojoMetaObject
     * @return Created Object
     */
    public static PojoMetaObject create( String name ) {
        return new PojoMetaObject( name );
    }

    /**
     * Uppercase the first character of the field name
     * @param b StringBuilder to use for upper case output
     * @param name Name of the field
     */
    protected void uppercase( StringBuilder b, String name ) {
        if (name.length() > 0) {
            b.append(Character.toUpperCase(name.charAt(0)));
            if (name.length() > 1) {
                b.append(name.substring(1));
            }
        }
    }

    protected String getGetterName( MetaField f, String prefix ) {
        // Create the getter name
        StringBuilder m = new StringBuilder();
        m.append( prefix);
        uppercase( m, f.getName() );
        return m.toString();
    }

    /**
     * Get the getter method name for the object for this MetaField
     * @param f MetaField to get the getter for
     * @return Name of getter method
     */
    protected Method findGetterName(Class objClass, MetaField f) {

        Method method = null;
        try {
            method = objClass.getMethod( getGetterName(f,"get"));
        } catch (NoSuchMethodException e) {
            try {
                method = objClass.getMethod( getGetterName(f,"is"));
            } catch (NoSuchMethodException e2) {
                throw new NoSuchMethodError("No getter exists named [(get/is)" +f.getName()+ "] on object [" +objClass.getName()+ "]");
            }
        }
        return method;
    }

    /**
     * Retrieve GET Method
     */
    protected Method retrieveGetterMethod(MetaField f, Class<?> objClass) //throws MetaException
    {
        Method method = (Method) f.getCacheValue(CACHE_PARAM_GETTER_METHOD + "." + objClass.getName());
        if (method == null) {
            method = findGetterName(objClass, f);

            f.setCacheValue(CACHE_PARAM_GETTER_METHOD + "." + objClass.getName(), method);
        }

        return method;
    }

    /**
     * Get the setter method on the object for this MetaField
     * @param f MetaField to get the setter for
     * @return Setter method name
     */
    protected String getSetterName(MetaField f) {

        // Create the getter name
        StringBuilder b = new StringBuilder();
        b.append("set");
        uppercase( b, f.getName() );
        return b.toString();
    }

    /**
     * Retrieve SET Method
     */
    protected Method retrieveSetterMethod(MetaField f, Class<?> objClass) //throws MetaException
    {
        synchronized (f) {
            Method method = (Method) f.getCacheValue(CACHE_PARAM_SETTER_METHOD + "." + objClass.getName());
            if (method == null) {

                String name = getSetterName(f);

                try {
                    method = objClass.getMethod( name,f.getValueClass() );
                } catch (NoSuchMethodException e) {
                    throw new NoSuchMethodError("No setter with a single variable exists named [" + name + "] with argument class [" + f.getValueClass().getSimpleName() + "] on object [" + objClass.getName() + "]");
                }

                f.setCacheValue(CACHE_PARAM_SETTER_METHOD + "." + objClass.getName(), method);
            }

            return method;
        }
    }

    /**
     * Sets the object attribute represented by this MetaField
     */
    @Override
    public void setValue(MetaField f, Object obj, Object val)  {
        setValueWithReflection(f, obj, val);
    }

    protected void setValueWithReflection(MetaField f, Object obj, Object val) {

        if (obj == null)
            throw new IllegalArgumentException("Cannot set value on a null Object for field [" + f + "]");

        Method method = retrieveSetterMethod(f, obj.getClass());

        Class<?> c = method.getParameterTypes()[ 0];

        if (val != null && !c.isAssignableFrom(val.getClass() )) {
            throw new InvalidValueException("Setter expected class [" + c.getName() + "] but value was of type [" + val.getClass() + "]");
        }

        Object[] arglist = new Object[1];
        arglist[0] = val;
        try {
            method.invoke(obj, arglist);
        } catch (InvocationTargetException e) {
            throw new RuntimeException("Invocation Target Exception setting field [" + f + "] on object [" + obj.getClass() + "]: " + e.getMessage(), e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Illegal Access Exception setting field [" + f + "] on object [" + obj.getClass() + "]: " + e.getMessage(), e);
        }
    }

    /**
     * Gets the object attribute represented by this MetaField
     */
    @Override
    public Object getValue(MetaField f, Object obj)  {
        return setValueWithReflection(f, obj);
    }

    protected Object setValueWithReflection(MetaField f, Object obj) {

        if (obj == null)
            throw new IllegalArgumentException("Null object found, Object expected for field [" + f + "]");

        Method method = retrieveGetterMethod(f, obj.getClass());

        try {
            return method.invoke(obj); //, new Object[0]);
        } catch (InvocationTargetException e) {
            throw new RuntimeException("Invocation Target Exception setting field [" + f + "] on object [" + obj.getClass() + "]: " + e.getMessage(), e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Illegal Access Exception setting field [" + f + "] on object [" + obj.getClass() + "]: " + e.getMessage(), e);
        }
    }

    /**
     * Whether the MetaObject produces the object specified
     */
    @Override
    public boolean produces(Object obj) {
        try {
            return obj.getClass().equals( getObjectClass() );
        }
        catch (ClassNotFoundException e) {
            return false;
        }
    }

    public boolean hasObjectInstanceAttr() {
        return hasMetaAttr(MetaObject.ATTR_OBJECT);
    }

    
}
