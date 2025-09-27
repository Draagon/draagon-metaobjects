package com.metaobjects.object;

import com.metaobjects.ValueException;

public interface Validatable {
    public void validate() throws ValueException;
}
