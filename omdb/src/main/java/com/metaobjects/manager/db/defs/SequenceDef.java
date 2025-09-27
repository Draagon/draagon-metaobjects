package com.metaobjects.manager.db.defs;

public class SequenceDef extends BaseDef {

	private int start = 1;
	private int increment = 1;
	
	public SequenceDef( NameDef name, int start, int increment ) {
		super( name );
		setStart( start );
		setIncrement( increment );
	}
	
	public int getStart() {
		return start;
	}
	
	public void setStart(int start) {
		this.start = start;
	}
	
	public int getIncrement() {
		return increment;
	}
	
	public void setIncrement(int increment) {
		this.increment = increment;
	}
}
