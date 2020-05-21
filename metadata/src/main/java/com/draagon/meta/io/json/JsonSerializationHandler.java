package com.draagon.meta.io.json;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

public interface JsonSerializationHandler {

    public void writeJsonValue(Object o, JsonWriter out) throws IOException;
    public void readJsonValue(Object o, JsonReader in) throws IOException;
}
