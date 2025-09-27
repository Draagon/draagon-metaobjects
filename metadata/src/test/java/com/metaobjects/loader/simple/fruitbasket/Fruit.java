package com.metaobjects.loader.simple.fruitbasket;

import com.metaobjects.object.MetaObject;
import com.metaobjects.object.mapped.MappedObject;

public abstract class Fruit extends MappedObject {
    protected Fruit(MetaObject mo) {
        super(mo);
    }
}
