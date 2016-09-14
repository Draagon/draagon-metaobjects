package com.draagon.meta.manager;

import com.draagon.meta.MetaException;

@SuppressWarnings("serial")
public class PersistenceException extends MetaException {
    
	public PersistenceException() {
        super ("Persistence Exception");
    }

    public PersistenceException(String msg) {
        super(msg);
    }

    public PersistenceException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public PersistenceException(Throwable cause) {
        super(cause);
    }
}
