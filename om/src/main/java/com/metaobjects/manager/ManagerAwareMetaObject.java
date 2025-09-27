/*
 * Copyright (c) 2012 Doug Mealing LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Doug Mealing LLC - initial API and implementation and/or initial documentation
 */
package com.metaobjects.manager;

/**
 *
 * @author dmealing
 */
public interface ManagerAwareMetaObject {
 
    void attachManager( ObjectManager om, Object obj );
    
    ObjectManager getManager( Object obj );
}
