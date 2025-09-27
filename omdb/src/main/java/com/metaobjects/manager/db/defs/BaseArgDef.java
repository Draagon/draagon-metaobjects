package com.metaobjects.manager.db.defs;

public class BaseArgDef {

	private String name = null;
	private int SQLType = 0;
	
	public BaseArgDef( String name, int type ) {
		setName( name );
		setSQLType( type );
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public int getSQLType() {
		return SQLType;
	}
	
	public void setSQLType(int type) {
		SQLType = type;
	}	

	public String toString() {
		return getClass().getSimpleName()+" ["+getName()+"]("+getSQLType()+")";
	}
}
