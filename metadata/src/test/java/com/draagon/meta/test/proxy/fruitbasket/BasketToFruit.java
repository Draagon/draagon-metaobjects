package com.draagon.meta.test.proxy.fruitbasket;

import com.draagon.meta.object.MetaObjectAware;
import com.draagon.meta.object.Validatable;

public interface BasketToFruit extends MetaObjectAware, Validatable {

    public Long getBasketId();
    public void setBasketId(Long basketId);
    public Long getFruitId();
    public void setFruitId(Long fruitId);
}
