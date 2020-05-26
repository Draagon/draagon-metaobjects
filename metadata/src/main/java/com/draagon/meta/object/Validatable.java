package com.draagon.meta.object;

import com.draagon.meta.ValueException;

public interface Validatable {
    public void validate() throws ValueException;
}
