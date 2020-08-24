package com.draagon.meta.manager.db;

import com.draagon.meta.manager.PersistenceException;

public class DirtyWriteException extends PersistenceException {
	
	private static final long serialVersionUID = 103419229085271187L;
	
	private Object object = null;
	
    public DirtyWriteException() {
        super ("DirtyWriteException");
    }

    public DirtyWriteException(String msg) {
        super(msg);
    }

    public DirtyWriteException( Object o ) {
        super("DirtyWriteException");
    	object = o;
    }
        
    public Object getObject() {
  	  return object;
    }
    
    @Override
    public String toString() {
  	  if ( object == null ) {
  		  return super.toString();
  	  } else {
  		  return "[" + object.toString() + "]" + super.toString();
  	  }
    }
}
