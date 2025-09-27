package com.metaobjects.manager.db;

import com.metaobjects.object.MetaObject;

public interface MappingHandler {

	public ObjectMapping getCreateMapping( MetaObject mc );
	
	public ObjectMapping getReadMapping( MetaObject mc );
	
	public ObjectMapping getUpdateMapping( MetaObject mc );

	public ObjectMapping getDeleteMapping( MetaObject mc );
}
