package com.draagon.meta.manager;

import com.draagon.meta.MetaDataException;

@SuppressWarnings("serial")
public class PersistenceException extends MetaDataException {
    
    public PersistenceException(String msg) {
        super(msg);
    }

    public PersistenceException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
