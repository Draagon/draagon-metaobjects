package com.draagon.meta.test.proxy.fruitbasket;

import com.draagon.meta.object.MetaObjectAware;
import com.draagon.meta.object.Validatable;
import com.draagon.meta.test.proxy.common.FieldId;
import com.draagon.meta.test.proxy.common.FieldName;

public interface Fruit extends FieldId, FieldName, MetaObjectAware, Validatable {

    public Long getBasketId();
    public void setBasketId(Long basketId);

    public Integer getLength();
    public void setLength(Integer length);

    public Integer getWeight();
    public void setWeight(Integer weight);

    public Boolean getInBasket();
    public void setInBasket(Boolean inBasket);
}