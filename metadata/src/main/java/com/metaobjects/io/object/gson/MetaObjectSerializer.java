package com.metaobjects.io.object.gson;

import com.metaobjects.field.MetaField;
import com.metaobjects.io.json.JsonIOConstants;
import com.metaobjects.io.json.raw.GsonSerializationHandler;
import com.metaobjects.io.string.StringSerializationHandler;
import com.metaobjects.loader.MetaDataLoader;
import com.metaobjects.object.MetaObject;
import com.metaobjects.object.MetaObjectAware;
import com.google.gson.*;

import static com.metaobjects.io.json.JsonIOUtil.*;

import java.lang.reflect.Type;

public class MetaObjectSerializer implements JsonSerializer<Object> {

    private final MetaDataLoader loader;
    private final MetaObject metaObject;
    private final boolean requiresType;

    public MetaObjectSerializer(MetaDataLoader loader, boolean requiresType) {
        this.loader = loader;
        this.metaObject = null;
        this.requiresType = requiresType;
    }

    public MetaObjectSerializer(MetaObject metaObject) {
        this.loader = metaObject.getLoader();
        this.metaObject = metaObject;
        this.requiresType = false;
    }

    @Override
    public JsonElement serialize(Object object, Type type, JsonSerializationContext context) {

        MetaObject mo = metaObject;
        if (metaObject == null && object instanceof MetaObjectAware ) {
            mo = ((MetaObjectAware) object).getMetaData();
        }

        JsonObject jsonObject = new JsonObject();

        writeObject( mo, object, jsonObject, context );

        return jsonObject;
    }

    protected void writeObject(MetaObject mo,  Object vo,
                               JsonObject jsonObject, JsonSerializationContext context) {

        jsonObject.addProperty(JsonIOConstants.ATTR_ATTYPE,mo.getName());
        for( MetaField mf : mo.getMetaFields()) {
            writeField( mo, mf, vo, jsonObject, context );
        }
    }

    protected void writeField(MetaObject mo, MetaField mf, Object vo,
                              JsonObject jsonObject, JsonSerializationContext context) {

        String name = getJsonName(mf);
        //boolean isNull = mf.getObject(vo) == null;

        switch (mf.getDataType()) {

            case BOOLEAN:
                jsonObject.addProperty(name, mf.getBoolean(vo));
                break;

            case BYTE:
            case SHORT:
            case INT:
                jsonObject.addProperty(name, mf.getInt(vo));
                break;

            case DATE:      // TODO: should we do somethinf custom here?
                jsonObject.add(name, context.serialize(vo));
                break;

            case LONG:
                jsonObject.addProperty(name, mf.getLong(vo));
                break;

            case FLOAT:
            case DOUBLE:
                jsonObject.addProperty(name, mf.getDouble(vo));
                break;

            case STRING_ARRAY:
                jsonObject.add(name, context.serialize(mf.getStringArray(vo)));
                break;

            case STRING:
                jsonObject.addProperty(name, mf.getString(vo));
                break;

            case OBJECT:
                jsonObject.add(name, context.serialize(mf.getObject(vo)));
                break;

            case OBJECT_ARRAY:
                jsonObject.add(name, context.serialize(mf.getObjectArray(vo)));
                break;

            case CUSTOM:
                writeFieldCustom(mo, mf, vo, jsonObject, context);
                break;

            default:
                throw new UnsupportedOperationException("DataType [" + mf.getDataType() + "] "+
                        "not supported [" + mf + "]");
        }
    }

    protected void writeFieldCustom(MetaObject mo, MetaField mf, Object vo,
                                    JsonObject jsonObject, JsonSerializationContext context) {

        String name = getJsonName(mf);

        if (mf instanceof GsonSerializationHandler) {
            jsonObject.add(name, ((GsonSerializationHandler)mf).gsonSerialize(vo,context));
        }
        else if (mf instanceof StringSerializationHandler){
            jsonObject.addProperty(name, ((StringSerializationHandler)mf).getValueAsString(vo));
        }
        else {
            throw new UnsupportedOperationException(
                    "Custom DataType and does not implement GsonSerializationHandler [" + mf + "]");
        }
    }
}
