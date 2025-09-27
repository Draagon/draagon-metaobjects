package com.metaobjects.manager.db.defs;

public abstract class BaseReferenceDef {

	private TableDef table = null;
	private String columnName = null;
	private TableDef refTable = null;
	private ColumnDef refColumn = null;
	
	public BaseReferenceDef( String columnName, TableDef refTable, ColumnDef refColumn ) {
		setColumnName( columnName );
		setRefTable( refTable );
		setRefColumn( refColumn );
	}

	public BaseTableDef getTable() {
		return table;
	}

	public void setTable( TableDef table) {
		this.table = table;
	}

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName( String columnName ) {
		this.columnName = columnName;
	}

	public TableDef getRefTable() {
		return refTable;
	}

	public void setRefTable(TableDef refTable) {
		this.refTable = refTable;
	}

	public ColumnDef getRefColumn() {
		return refColumn;
	}

	public void setRefColumn(ColumnDef refColumn) {
		this.refColumn = refColumn;
	}
}
