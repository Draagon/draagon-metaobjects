package com.draagon.meta.manager.db.defs;

public class ViewDef extends BaseTableDef {

	private String sql = null;
	
	public ViewDef( NameDef name ) {
		super( name );
		//setSQL( sql );
	}

	public String getSQL() {
		return sql;
	}
	
	public void setSQL(String sql) {
		this.sql = sql;
	}
}
