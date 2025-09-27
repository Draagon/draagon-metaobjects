package com.metaobjects.test.proxy.fruitbasket;

import com.metaobjects.object.MetaObjectAware;
import com.metaobjects.object.Validatable;

public interface BasketToFruit extends MetaObjectAware, Validatable {

    public Long getBasketId();
    public void setBasketId(Long basketId);
    public Long getFruitId();
    public void setFruitId(Long fruitId);
}
