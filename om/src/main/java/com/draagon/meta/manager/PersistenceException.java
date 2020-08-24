package com.draagon.meta.manager;

@SuppressWarnings("serial")
public class PersistenceException extends RuntimeException {
    
    public PersistenceException(String msg) {
        super(msg);
    }

    public PersistenceException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
