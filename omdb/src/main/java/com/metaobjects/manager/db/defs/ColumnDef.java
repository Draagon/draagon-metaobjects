package com.metaobjects.manager.db.defs;

public class ColumnDef extends BaseArgDef {

	public final static int DEFAULT_LENGTH = -1;
	
	public final static int AUTO_NONE 		= 0;
	public final static int AUTO_ID   		= 1;
	public final static int AUTO_LAST_ID   		= 2;
	public final static int AUTO_DATE_CREATE 	= 3;
	public final static int AUTO_DATE_UPDATE 	= 4;

	private int length = DEFAULT_LENGTH;
	private boolean isPrimaryKey = false;
	private boolean isUnique = false;
	private int autoType = AUTO_NONE;
	
	private SequenceDef sequence = null;
	private BaseTableDef baseTable = null;
	//private boolean autoIncrementor = false; 
	
	public ColumnDef( String name, int type ) {
		super( name, type );
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}
	
	public boolean isAutoIncrementor() {
		return getAutoType() == AUTO_ID || getAutoType() == AUTO_LAST_ID;
	}
        
	//protected void setAutoIncrementor( boolean autoInc ) {
	//	this.autoIncrementor = autoInc;
	//}

	public SequenceDef getSequence() {
		return sequence;
	}

	public void setSequence(SequenceDef sequence) {
		this.sequence = sequence;
		// NOTE: This is not correct!
		//setAutoIncrementor( sequence != null );
		//setAutoType( AUTO_ID );
	}

	public BaseTableDef getBaseTable() {
		return baseTable;
	}

	public void setBaseTable(BaseTableDef table) {
		this.baseTable = table;
	}

	public boolean isPrimaryKey() {
		return isPrimaryKey;
	}

	public void setPrimaryKey( boolean isPrimaryKey ) {
		this.isPrimaryKey = isPrimaryKey;
	}

	public boolean isUnique() {
		return isUnique;
	}

	public void setUnique( boolean isUnique ) {
		this.isUnique = isUnique;
	}

	public int getAutoType() {
		return autoType;
	}

	public void setAutoType(int autoType) {
		this.autoType = autoType;
	}	
}
