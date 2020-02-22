package com.draagon.meta.manager.db;

import java.util.Properties;

import com.draagon.meta.object.MetaObject;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.field.MetaField;

public class InheritanceRef {
	
	public final static String PROP_SUPER_CLASS = "superClass"; 
	public final static String PROP_SUPER_JOINER = "superJoiner";
	public final static String PROP_JOINER = "joiner";
	public final static String PROP_DISCRIMINATOR = "discriminator";
	public final static String PROP_DISCRIMINATOR_VALUE = "discriminatorValue";
	
	private MetaObject superClass = null;
	private MetaField superJoinerField = null;
	private MetaObject metaClass = null;
	private MetaField joinerField = null;
	private MetaField discriminatorField = null;
	private String discriminatorValue = null;
	
	public InheritanceRef( MetaObject mc, Properties def ) {

		String pj = def.getProperty( PROP_JOINER );
		if ( pj == null ) throw new IllegalArgumentException( "Inheritance Properties must have a 'joiner' property" );
		String psc = def.getProperty( PROP_SUPER_CLASS );
		if ( psc == null ) throw new IllegalArgumentException( "Inheritance Properties must have a 'superClass' property" );
		String psj = def.getProperty( PROP_SUPER_JOINER );
		if ( psj == null ) throw new IllegalArgumentException( "Inheritance Properties must have a 'superJoiner' property" );
		String pd = def.getProperty( PROP_DISCRIMINATOR );
		
		metaClass = mc;

		if ( !mc.hasMetaField( pj )) throw new IllegalArgumentException( "Inheritance 'joiner' property [" + pj + "] is not a valid MetaField on MetaClass [" + mc.getName() + "]" );
		joinerField = mc.getMetaField( pj );
		
		superClass = MetaObject.forName( psc );
		if ( superClass == null ) throw new IllegalArgumentException( "Inheritance 'superClass' property [" + psc + "] is not a valid MetaClass" );
		
		if ( !superClass.hasMetaField( psj )) throw new IllegalArgumentException( "Inheritance 'superJoiner' property [" + psj + "] is not a valid MetaField on MetaClass [" + superClass.getName() + "]" );
		superJoinerField = superClass.getMetaField( psj );
		
		if ( pd != null ) {
			
			if ( !mc.hasMetaField( pd )) throw new IllegalArgumentException( "Inheritance 'discriminator' property [" + pd + "] is not a valid MetaField on MetaClass [" + mc.getName() + "]" );
			discriminatorField = mc.getMetaField( pd );
			
			discriminatorValue = def.getProperty( PROP_DISCRIMINATOR_VALUE );
			if ( discriminatorValue == null ) throw new IllegalArgumentException( "Inheritance Properties must have a 'discriminatorValue' property if a 'discriminator' property exists" );
		}			
	}

	public MetaObject getSuperClass() {
		return superClass;
	}

	public void setSuperClass(MetaObject superClass) {
		this.superClass = superClass;
	}

	public MetaField getSuperJoinerField() {
		return superJoinerField;
	}

	public void setSuperJoinerField(MetaField superJoinerField) {
		this.superJoinerField = superJoinerField;
	}

	public MetaObject getMetaClass() {
		return metaClass;
	}

	public void setMetaClass(MetaObject metaClass) {
		this.metaClass = metaClass;
	}

	public MetaField getJoinerField() {
		return joinerField;
	}

	public void setJoinerField(MetaField joinerField) {
		this.joinerField = joinerField;
	}

	public MetaField getDiscriminatorField() {
		return discriminatorField;
	}

	public void setDiscriminatorField(MetaField discriminatorField) {
		this.discriminatorField = discriminatorField;
	}

	public String getDiscriminatorValue() {
		return discriminatorValue;
	}

	public void setDiscriminatorValue(String discriminatorValue) {
		this.discriminatorValue = discriminatorValue;
	}
}
