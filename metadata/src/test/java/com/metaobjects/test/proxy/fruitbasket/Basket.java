package com.metaobjects.test.proxy.fruitbasket;

import com.metaobjects.loader.simple.fruitbasket.Apple;
import com.metaobjects.loader.simple.fruitbasket.Orange;
import com.metaobjects.object.MetaObjectAware;
import com.metaobjects.object.Validatable;
import com.metaobjects.test.proxy.common.FieldId;
import com.metaobjects.test.proxy.common.FieldName;

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
