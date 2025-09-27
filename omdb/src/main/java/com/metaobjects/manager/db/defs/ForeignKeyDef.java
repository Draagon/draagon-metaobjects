package com.metaobjects.manager.db.defs;

public class ForeignKeyDef extends BaseReferenceDef
{
	private String name = null;
	
	public ForeignKeyDef( String name, String columnName, TableDef refTable, ColumnDef refColumn ) {
		super( columnName, refTable, refColumn );
		setName( name );
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
