package com.metaobjects.manager.db;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.metaobjects.field.MetaField;
import com.metaobjects.manager.db.defs.BaseArgDef;

public class ObjectMapping {

	private ObjectMapping superMap = null;
	private Map<String,MetaField> fieldMapping = new HashMap<String,MetaField>();
	private Map<String,BaseArgDef> nameMapping = new HashMap<String,BaseArgDef>();

	public ObjectMapping() {}

	public void setSuperMapping(ObjectMapping superMap) {
		this.superMap = superMap;		
	}
	
	public ObjectMapping getSuperMapping() {
		return this.superMap;
	}
	
	public Collection<BaseArgDef> getArguments() {
		return nameMapping.values();
	}

	public Collection<MetaField> getMetaFields() {
		return fieldMapping.values();
	}

	public void addMap( BaseArgDef arg, MetaField mf ) {
		fieldMapping.put( arg.getName(), mf );
		nameMapping.put( mf.getName(), arg );
	}

	public MetaField getField( String argName ) {
		MetaField mf = fieldMapping.get( argName );
		if ( mf == null && superMap != null ) {
			return superMap.getField( argName );
		}
		return mf;
	}	

	public MetaField getField( BaseArgDef argDef ) {
		return getField( argDef.getName() );
	}	

	public BaseArgDef getArgDef( String fieldName ) {
		BaseArgDef def = nameMapping.get( fieldName );
		if ( def == null && superMap != null ) {
			return superMap.getArgDef( fieldName );
		}
		return def;
	}	

	public BaseArgDef getArgDef( MetaField mf ) {
		return getArgDef( mf.getName() );
	}
	
	public boolean isInThisMap( MetaField mf ) {
		if ( nameMapping.get( mf.getName() ) != null ) return true;
		return false;
	}
}
