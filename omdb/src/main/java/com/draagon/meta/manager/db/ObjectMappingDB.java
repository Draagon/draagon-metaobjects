package com.draagon.meta.manager.db;

import com.draagon.meta.manager.db.defs.BaseDef;

public class ObjectMappingDB extends ObjectMapping {

	  private BaseDef baseDBDef = null;
	  
	  public ObjectMappingDB( BaseDef def ) {
		  this.baseDBDef = def;
	  }
	  	  
	  public BaseDef getDBDef() {
		  return baseDBDef;
	  }
}
