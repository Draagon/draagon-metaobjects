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

import com.draagon.meta.object.MetaObject;
import com.draagon.meta.object.MetaObjectAware;

import java.util.Objects;

/**
 *
 * @author dmealing
 */
public abstract class Fruit implements MetaObjectAware {
    
    private Long id = null;
    private String name = null;
    private Integer length = null;
    private Integer weight = null;
    private Boolean inBasket = null;

    private MetaObject metaObject = null;
    
    public Fruit() {
    }
    
    @Override
    public MetaObject getMetaData() {
        return metaObject;
    }

    @Override
    public void setMetaData(MetaObject metaObject) {
        this.metaObject = metaObject;
    }
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getLength() {
        return length;
    }

    public void setLength(Integer length) {
        this.length = length;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    public Boolean getInBasket() {
        return inBasket;
    }

    public void setInBasket(Boolean inBasket) {
        this.inBasket = inBasket;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Fruit fruit = (Fruit) o;
        return Objects.equals(id, fruit.id) &&
                Objects.equals(name, fruit.name) &&
                Objects.equals(length, fruit.length) &&
                Objects.equals(weight, fruit.weight) &&
                Objects.equals(inBasket, fruit.inBasket) &&
                Objects.equals(metaObject, fruit.metaObject);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, length, weight, inBasket, metaObject);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Fruit{");
        sb.append("id=").append(id);
        sb.append(", name='").append(name).append('\'');
        if ( length != null ) sb.append(", length=").append(length);
        if ( weight != null ) sb.append(", weight=").append(weight);
        if ( inBasket != null ) sb.append(", inBasket=").append(inBasket);
        if ( metaObject != null )  sb.append(", metaObject=").append(metaObject.getName());
        sb.append('}');
        return sb.toString();
    }
}
