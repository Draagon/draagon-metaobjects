package com.draagon.meta.manager.db.defs;

public class InheritenceDef extends BaseReferenceDef {

	private String discriminatorName = null;
	private String discriminatorValue = null;
	
	public InheritenceDef( String columnName, TableDef refTable, ColumnDef refColumn, String discrName, String discrValue ) {
		super( columnName, refTable, refColumn );
		setDiscriminatorName( discrName );
		setDiscriminatorValue( discrValue );
	}

	public String getDiscriminatorName() {
		return discriminatorName;
	}

	public void setDiscriminatorName( String discriminator ) {
		this.discriminatorName = discriminator;
	}

	public String getDiscriminatorValue() {
		return discriminatorValue;
	}

	public void setDiscriminatorValue(String discriminatorValue) {
		this.discriminatorValue = discriminatorValue;
	}
}
