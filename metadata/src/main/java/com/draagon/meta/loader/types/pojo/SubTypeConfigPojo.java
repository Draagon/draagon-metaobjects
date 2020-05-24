package com.draagon.meta.loader.types.pojo;

import com.draagon.meta.InvalidValueException;
import com.draagon.meta.MetaData;
import com.draagon.meta.ValueException;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.loader.types.ChildConfig;
import com.draagon.meta.loader.types.SubTypeConfig;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.object.pojo.PojoObject;

import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Objects;

/** Used to store the MetaData Type config and respective SubTypes and their classes */
public class SubTypeConfigPojo extends PojoObject implements SubTypeConfig {

    private String name = null;
    private String baseClass = null;
    private List<ChildConfig> children = null;

    public SubTypeConfigPojo(MetaObject mo) {
        super(mo);
    }

    /////////////////////////////////////////////////////////////////////
    // GETTERS AND SETTERS

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getBaseClass() {
        return baseClass;
    }

    @Override
    public void setBaseClass( String baseClass ) {
        this.baseClass = baseClass;
    }

    @Override
    public List<ChildConfig> getChildConfigs() {
        return children;
    }

    @Override
    public void setChildConfigs( List<ChildConfig> children ) {
        this.children = children;
    }


    //////////////////////////////////////////////////////////////////////
    // Validation Method

    @Override
    public void validate() throws ValueException {
        super.validate();
        if ( getName() == null ) throw new InvalidValueException( "Type name on SubType cannot be null" );
        if ( getBaseClass() == null ) throw new InvalidValueException( "Base class on SubType ["+getName()+"] cannot be null" );

        // TODO:  Put this in the Parse logic
        //if ( Modifier.isAbstract( getMetaDataClass().getModifiers() )) throw new InvalidValueException(
        //        "Base class ["+getBaseClass()+"] on SubType ["+getName()+"] cannot be Abstract" );

        if ( getChildConfigs() != null ) getChildConfigs().forEach( cc -> cc.validate() );
    }


    /////////////////////////////////////////////////////////////////////
    // TODO:  Replace all of these once the new version exists on the core side

    public static SubTypeConfigPojo create(MetaDataLoader loader,
                                           String subtypeName,
                                           String baseClass ) {
        SubTypeConfigPojo c = (SubTypeConfigPojo) loader.getMetaObjectByName( SubTypeConfigPojo.OBJECT_NAME ).newInstance();
        c.setName( subtypeName );
        c.setBaseClass( baseClass );
        return c;
    }

    private Class<? extends MetaData> toClass( String className )  throws ClassNotFoundException {
        return (Class<? extends MetaData>) Class.forName( className );
    }

    public Class <? extends MetaData> getMetaDataClass() {
        try {
            return toClass( getBaseClass());
        } catch (ClassNotFoundException e) {
            throw new InvalidValueException( "BaseClass ["+getBaseClass()+"] was not found for SubType ["+getName()+"]");
        }
    }


    /////////////////////////////////////////////////////////////////////
    // Misc methods

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SubTypeConfigPojo that = (SubTypeConfigPojo) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(baseClass, that.baseClass) &&
                Objects.equals(children, that.children);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, baseClass, children);
    }

    @Override
    public String toString() {
        return "SubTypeConfigPojo{" +
                "name='" + name + '\'' +
                ", baseClass=" + baseClass +
                ", children=" + children +
                '}';
    }
}
