package com.draagon.meta.test.proxy.fruitbasket;

import com.draagon.meta.loader.simple.fruitbasket.Apple;
import com.draagon.meta.loader.simple.fruitbasket.Orange;
import com.draagon.meta.object.MetaObjectAware;
import com.draagon.meta.object.Validatable;
import com.draagon.meta.test.proxy.common.FieldId;
import com.draagon.meta.test.proxy.common.FieldName;

import java.util.List;

public interface Basket extends FieldId, FieldName, MetaObjectAware, Validatable {

    public Integer getNumApples();
    public void setNumApples(Integer val);

    public Integer getNumOranges();
    public void setNumOranges(Integer val);

    public List<Apple> getApples();
    public void setApples(List<Apple> val);

    public List<Orange> getOranges();
    public void setOranges(List<Orange> val);
}
