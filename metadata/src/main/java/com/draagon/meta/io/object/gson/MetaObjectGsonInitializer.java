package com.draagon.meta.io.object.gson;

import com.draagon.meta.MetaDataException;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.object.MetaObject;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MetaObjectGsonInitializer {

    public final static GsonBuilder getBuilderWithAdapters(MetaDataLoader loader) {
        return addAdaptersToBuilder(loader, new GsonBuilder());
    }

    public final static GsonBuilder addSerializersToBuilder(MetaDataLoader loader, GsonBuilder builder) {
        return addAdaptersToBuilder( loader, builder, true, false);
    }

    public final static GsonBuilder addDeserializersToBuilder(MetaDataLoader loader, GsonBuilder builder) {
        return addAdaptersToBuilder( loader, builder, false, true);
    }

    public final static GsonBuilder addAdaptersToBuilder(MetaDataLoader loader, GsonBuilder builder) {
        return addAdaptersToBuilder( loader, builder, true, true);
    }

    private final static GsonBuilder addAdaptersToBuilder(MetaDataLoader loader, GsonBuilder builder,
                                                         boolean addSerializer, boolean addDeserializer) {

        List<Class> classList = new ArrayList<>();

        Map<MetaObject,Class> nameClassMap = getMetaObjectToClassMap(loader);
        for (Map.Entry<MetaObject,Class> entry : nameClassMap.entrySet()) {

            MetaObject mo = entry.getKey();
            Class clazz = entry.getValue();

            Class clazz2;
            try {
                clazz2 = mo.getObjectClass();
            } catch (ClassNotFoundException e) {
                throw new MetaDataException("MetaObject ["+mo.getName()+"] had Object class "+
                        "that was not found: "+e,e);
            }

            // If the clazz2 is an interface, add them
            if (clazz2 != null && clazz2.isInterface()) {
                if (addSerializer)
                    builder.registerTypeAdapter( clazz2, new MetaObjectSerializer(mo));
                if (addDeserializer)
                    builder.registerTypeAdapter( clazz2, new MetaObjectDeserializer(mo));
            }

            // If there are multiple classes for the same type, just add that once
            if (hasMultipleClasses( nameClassMap, entry.getKey(), clazz)) {

                if (!classList.contains( clazz)) {
                    //if (addSerializer)
                        builder.registerTypeAdapter(clazz, new MetaObjectSerializer(loader, true));
                    //if (addDeserializer)
                        builder.registerTypeAdapter(clazz, new MetaObjectDeserializer(loader, true));
                    classList.add(clazz);
                }
            }

            // Otherwise, add the specific class implementation
            else {
                //if (addSerializer)
                    builder.registerTypeAdapter( clazz, new MetaObjectSerializer( mo));
                //if (addDeserializer)
                    builder.registerTypeAdapter( clazz, new MetaObjectDeserializer( mo));
            }
        }

        return builder;
    }

    private static boolean hasMultipleClasses(Map<MetaObject,Class> nameClassMap, MetaObject mo, Class c) {

        boolean found = false;
        for (Map.Entry<MetaObject,Class> entry : nameClassMap.entrySet()) {
            if (!entry.getKey().equals(mo)) {
                if ( c.equals(entry.getValue())) {
                    found = true;
                    break;
                }
            }
        }
        return found;
    }

    public final static Map<MetaObject,Class> getMetaObjectToClassMap(MetaDataLoader loader) {

        Map<MetaObject,Class> nameClassMap = new HashMap<>();

        List<Class<?>> classList = new ArrayList<>();
        for (MetaObject mo : loader.getMetaObjects()) {
            Object o = mo.newInstance();
            Class<?> c = o.getClass();
            nameClassMap.put(mo,c);
        }

        return nameClassMap;
    }
}
