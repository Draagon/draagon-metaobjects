package com.draagon.meta.io.object.gson;

import com.draagon.meta.MetaDataException;
import com.draagon.meta.field.MetaField;
import com.draagon.meta.io.MetaDataIOException;
import com.draagon.meta.io.json.JsonIOConstants;
import com.draagon.meta.io.json.JsonSerializationHandler;
import com.draagon.meta.io.json.raw.GsonSerializationHandler;
import com.draagon.meta.io.string.StringSerializationHandler;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.object.MetaObject;

import com.draagon.meta.util.MetaDataUtil;
import com.google.gson.*;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.draagon.meta.io.json.JsonIOUtil.*;
import static com.draagon.meta.io.xml.XMLIOUtil.getObjectRef;

public class MetaObjectDeserializer implements JsonDeserializer<Object> {

    private final MetaDataLoader loader;
    private final MetaObject metaObject;
    private final boolean requiresType;

    public MetaObjectDeserializer(MetaDataLoader loader, boolean requiresType) {
        this.loader = loader;
        this.metaObject = null;
        this.requiresType = requiresType;
    }

    public MetaObjectDeserializer(MetaObject metaObject) {
        this.loader = metaObject.getLoader();
        this.metaObject = metaObject;
        this.requiresType = false;
    }

    @Override
    public Object deserialize(JsonElement json, Type type, JsonDeserializationContext context)
            throws JsonParseException {

        MetaObject mo = metaObject;

        // Check the @type if it exists, and use it to override the local one
        final JsonObject jsonObject = json.getAsJsonObject();
        if (jsonObject.has(JsonIOConstants.ATTR_ATTYPE)) {
            JsonPrimitive prim = (JsonPrimitive) jsonObject.get(JsonIOConstants.ATTR_ATTYPE);
            String metaObjectName = prim.getAsString();
            if (metaObject == null || !metaObject.getName().equals(metaObjectName)) {
                mo = loader.getMetaObjectByName(metaObjectName);
            }
        }

        if ( mo == null ) throw new JsonParseException("No '@type' attribute was found, and MetaObject not "+
                "specified in the MetaObjectDeserializer");

        Object o = mo.newInstance();

        readObjectFields( mo, o, jsonObject, context );

        return o;
    }

    protected void readObjectFields(MetaObject mo, Object vo, JsonObject json, JsonDeserializationContext context) {

        for (MetaField mf : mo.getMetaFields() ) {
            readFieldValue( mo, mf, vo, json, context );
        }
    }


    protected void readFieldValue(MetaObject mo, MetaField mf, Object vo,
                                  JsonObject json, JsonDeserializationContext context)  {

        String jsonName = getJsonName(mf);
        if ( !json.has( jsonName)) return;

        JsonElement el = json.get(jsonName);
        if ( el.isJsonNull()) {
            mf.setObject( vo, null);
        }
        else {
            switch (mf.getDataType()) {
                case BOOLEAN:
                    mf.setBoolean(vo, el.getAsBoolean());
                    break;

                case BYTE:
                case SHORT:
                case INT:
                    mf.setInt(vo, el.getAsInt());
                    break;

                case DATE:
                case LONG:
                    mf.setLong(vo, el.getAsLong());
                    break;

                case FLOAT:
                case DOUBLE:
                    mf.setDouble(vo, el.getAsDouble());
                    break;

                case STRING_ARRAY:
                    if (el.isJsonArray()) mf.setStringArray(vo, context.deserialize(el, List.class));
                    else mf.setString(vo, el.getAsString());
                    break;

                case STRING:
                    mf.setString(vo, el.getAsString());
                    break;

                case OBJECT:
                    readFieldObject(mo, mf, vo, el, context);
                    break;

                case OBJECT_ARRAY:
                    readFieldObjectArray(mo, mf, vo, el, context);
                    break;

                case CUSTOM:
                    readFieldCustom(mo, mf, vo, el, context);
                    break;

                default:
                    throw new UnsupportedOperationException(
                            "DataType [" + mf.getDataType() + "] not supported [" + mf + "]");
            }
        }
    }

    private Class getObjectRefClass(MetaField mf) {

        MetaObject refmo = null;
        if (MetaDataUtil.hasObjectRef(mf)) {
            refmo = MetaDataUtil.getObjectRef(mf);
        }
        if (refmo == null) throw new MetaDataException("Cannot read Object as MetaField "+
                "["+mf+"] had no objectRef attribute set");

        Class clazz;
        try {
            clazz = refmo.getObjectClass();
        } catch (ClassNotFoundException e) {
            throw new MetaDataException("Cannot read Object as field ["+mf.getName()+"] had an ObjectRef "+
                    "without a valid ObjectClass: "+refmo.getName());
        }

        return clazz;
    }

    protected void readFieldObjectArray(MetaObject mo, MetaField mf, Object vo,
                                        JsonElement el, JsonDeserializationContext context) {

        List<Object> objects = new ArrayList<>();
        mf.setObject( vo, objects );

        if (!el.isJsonArray()) throw new MetaDataException("Expected JsonArray when reading MetaField "+
                "["+mf+"], but found JsonElement: "+el);

        Iterator<JsonElement> iter = ((JsonArray)el).iterator();
        while(iter.hasNext()) {
            JsonElement elo = iter.next();
            if (!elo.isJsonObject()) throw new MetaDataException("Expected JsonObject when reading MetaField "+
                    "["+mf+"], but found JsonElement: "+elo);

            objects.add( context.deserialize( elo, getObjectRefClass(mf)));
        }
    }

    protected void readFieldObject(MetaObject mo, MetaField mf, Object vo,
                                   JsonElement el, JsonDeserializationContext context) {

        if (!el.isJsonObject()) throw new MetaDataException("Expected JsonObject when reading MetaField "+
                "["+mf+"], but found JsonElement: "+el);

        mf.setObject( vo, context.deserialize( el, getObjectRefClass(mf)));
    }

    protected void readFieldCustom(MetaObject mo, MetaField mf, Object vo,
                                   JsonElement el, JsonDeserializationContext context) {

        if ( mf instanceof GsonSerializationHandler) {
            ((GsonSerializationHandler)mf).gsonDeserialize(vo,el,context);
        }
        else if (mf instanceof StringSerializationHandler){
            ((StringSerializationHandler)mf).setValueAsString(vo,el.getAsString());
        }
        else {
            throw new UnsupportedOperationException(
                    "Custom DataType and does not implement GsonSerializationHandler [" + mf + "]");
        }
    }
}
