package com.metaobjects.manager.db.defs;

public abstract class BaseDef {

	public NameDef name = null;
	
	public BaseDef( NameDef name ) {
		setNameDef( name );
	}

	public NameDef getNameDef() {
		return name;
	}

	public void setNameDef(NameDef name) {
		this.name = name;
	}
	
	public String toString() {
		return getClass().getSimpleName() + " [" + getNameDef() + "]";
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}
	
	@Override
	public boolean equals( Object o ) {
		if (!( o instanceof BaseDef )) return false;
		if ( toString().equals( o.toString() )) return true;
		return false;
	}
}
