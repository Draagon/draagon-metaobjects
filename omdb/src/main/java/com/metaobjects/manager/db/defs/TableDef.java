package com.metaobjects.manager.db.defs;

import java.util.ArrayList;
import java.util.List;

public class TableDef extends BaseTableDef {
		
	private List<ColumnDef> primaryKeys = new ArrayList<ColumnDef>();
	private List<IndexDef> indexes = new ArrayList<IndexDef>();
	private List<ForeignKeyDef> foreignKeys = new ArrayList<ForeignKeyDef>();
	private InheritenceDef inheritence = null;

	public TableDef( NameDef name ) { // , List<ColumnDef> cols ) {		
		super( name ); //, cols );		
	}
	
	@Override
	public void addColumn( ColumnDef col ) {
		
		// Set the columns
		super.addColumn( col );
		
		// Pull out the primary keys
		if ( col.isPrimaryKey() ) {
			addPrimaryKey( col );
		}
	}
	
	public InheritenceDef getInheritence() {
		return inheritence;
	}

	public void setInheritence(InheritenceDef inheritence) {
		this.inheritence = inheritence;
	}
	
	public boolean hasInheritence() {
		return ( inheritence != null ); 
	}

	protected void addPrimaryKey( ColumnDef primaryKey ) {
		
		// Don't have the same primary key more than once
		for ( ColumnDef col : getPrimaryKeys() ) {
			if ( col.getName().equals( primaryKey.getName() )) return;
		}
		
		// Add the primary key
		primaryKeys.add( primaryKey );
	}

	public List<ColumnDef> getPrimaryKeys() {
		return primaryKeys;
	}

	public void addIndex( IndexDef index ) {
		
		// Don't have the same primary key more than once
		for ( IndexDef ind : getIndexes() ) {
			if ( ind.getName().equals( index.getName() )) return;
		}
		
		// Set this as the table
		index.setTable( this );
		
		// Add the primary key
		indexes.add( index );
	}

	public List<IndexDef> getIndexes() {
		return indexes;
	}

	protected void addForeignKey( ForeignKeyDef foreignKey ) {
		
		// Don't have the same foreign key more than once
		for ( ForeignKeyDef key : getForeignKeys() ) {
			if ( key.getName().equals( foreignKey.getName() )) return;
		}
		
		// Set this as the table
		foreignKey.setTable( this );
		
		// Add the primary key
		foreignKeys.add( foreignKey );
	}

	public List<ForeignKeyDef> getForeignKeys() {
		return foreignKeys;
	}
}
