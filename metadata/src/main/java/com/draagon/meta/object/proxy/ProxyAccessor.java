package com.draagon.meta.object.proxy;

public interface ProxyAccessor {

    public Object _getValueByName(String name);

    public void _setValueByName(String name, Object val);
}
