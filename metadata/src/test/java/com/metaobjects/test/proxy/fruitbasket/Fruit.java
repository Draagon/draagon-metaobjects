package com.metaobjects.test.proxy.fruitbasket;

import com.metaobjects.object.MetaObjectAware;
import com.metaobjects.object.Validatable;
import com.metaobjects.test.proxy.common.FieldId;
import com.metaobjects.test.proxy.common.FieldName;

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