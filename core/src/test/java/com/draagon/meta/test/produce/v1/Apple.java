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
package com.draagon.meta.test.produce.v1;

/**
 *
 * @author dmealing
 */
public class Apple extends Fruit {
    
    private short worms;
    private String orchard;
    
    public Apple() {
    }

    public short getWorms() {
        return worms;
    }

    public void setWorms(short worms) {
        this.worms = worms;
    }

    public String getOrchard() {
        return orchard;
    }

    public void setOrchard(String orchard) {
        this.orchard = orchard;
    }
}
