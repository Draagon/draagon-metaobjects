package com.draagon.meta.manager.db.defs;

import java.util.ArrayList;
import java.util.List;

public class IndexDef {

	private String name = null;
	private TableDef table = null;
	private List<String> columnNames = null;
	
	public IndexDef( String name, String columnName ) {

		setName( name );

		List<String> cols = new ArrayList<String>();
		cols.add( columnName );
		
		setColumnNames( cols );
	}
	
	public IndexDef( String name, List<String> columns ) {
		
		setName( name );
		setColumnNames( columns );
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public TableDef getTable() {
		return table;
	}
	
	public void setTable(TableDef table) {
		this.table = table;
	}
	
	public List<String> getColumnNames() {
		return columnNames;
	}

	public void setColumnNames(List<String> columns) {
		this.columnNames = columns;
	}
}
