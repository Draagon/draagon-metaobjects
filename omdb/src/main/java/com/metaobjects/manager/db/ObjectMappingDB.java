package com.metaobjects.manager.db;

import com.metaobjects.manager.db.defs.BaseDef;

public class ObjectMappingDB extends ObjectMapping {

	  private BaseDef baseDBDef = null;
	  
	  public ObjectMappingDB( BaseDef def ) {
		  this.baseDBDef = def;
	  }
	  	  
	  public BaseDef getDBDef() {
		  return baseDBDef;
	  }
}
