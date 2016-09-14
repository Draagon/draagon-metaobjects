package com.draagon.meta.manager.db;

public class SubSelectValue {

	private String sql = null;
	
	public SubSelectValue( String sql ) {
		this.sql = sql;
	}
	
	public String getSql() {
		return sql;
	}
	
	public String toString() {
		return "{SQL: " + getSql() + "}";
	}
}
