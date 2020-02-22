package com.draagon.meta.manager.db.defs;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseTableDef extends BaseDef {
	
	private List<ColumnDef> columns = new ArrayList<ColumnDef>();
	
	public BaseTableDef( NameDef name ) { //, List<ColumnDef> cols ) {
		super( name );
		//setColumns( cols );
	}

	public List<ColumnDef> getColumns() {
		return columns;
	}
	
	public void addColumn( ColumnDef col ) {
		col.setBaseTable( this );
		columns.add( col );
	}

	public ColumnDef getColumn( String colName ) {
		for( ColumnDef col : columns ) {
			if ( col.getName().equals( colName )) return col;
		}
		return null;
	}
	
	public String toString() {
		
		StringBuilder sb = new StringBuilder( getClass().getSimpleName() );
		sb.append( " [" ).append( getNameDef() ).append( "]{" );
		
		boolean first = true;
		for( ColumnDef col : columns ) {
			if ( first ) first = false;
			else sb.append( "," );
			sb.append( col.getName() );
		}
		 
		return sb.toString();
	}
}
