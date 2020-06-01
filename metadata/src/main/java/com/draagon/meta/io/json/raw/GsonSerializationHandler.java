package com.draagon.meta.io.json.raw;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;

public interface GsonSerializationHandler {

    public JsonElement gsonSerialize(Object o, JsonSerializationContext context);
    public Object gsonDeserialize(Object o, JsonElement el, JsonDeserializationContext context);
}
