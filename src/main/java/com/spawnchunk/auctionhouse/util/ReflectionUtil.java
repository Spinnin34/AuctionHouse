package com.spawnchunk.auctionhouse.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ReflectionUtil {
    public static Field getField(Class<?> clazz, String field) throws Exception {
        Field f;
        try {
            f = clazz.getDeclaredField(field);
        }
        catch (NoSuchFieldException e) {
            f = clazz.getField(field);
        }
        if (f == null) {
            throw new NoSuchFieldException();
        }
        f.setAccessible(true);
        return f;
    }

    public static Method getMethod(Class<?> clazz, String method, Class<?> ... classes) throws Exception {
        Method m;
        try {
            m = clazz.getDeclaredMethod(method, classes);
        }
        catch (NoSuchMethodException e) {
            m = clazz.getMethod(method, classes);
        }
        if (m == null) {
            throw new NoSuchFieldException();
        }
        m.setAccessible(true);
        return m;
    }

    public static Constructor getConstructor(Class<?> clazz, Class<?> ... classes) throws Exception {
        Constructor<?> c;
        try {
            c = clazz.getDeclaredConstructor(classes);
        }
        catch (NoSuchMethodException e) {
            c = clazz.getConstructor(classes);
        }
        if (c == null) {
            throw new NoSuchFieldException();
        }
        c.setAccessible(true);
        return c;
    }

    public static Field setFinal(Field f, Boolean value) throws Exception {
        f.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        if (value.booleanValue()) {
            modifiersField.setInt(f, f.getModifiers() & 0x10);
        } else {
            modifiersField.setInt(f, f.getModifiers() & 0xFFFFFFEF);
        }
        return f;
    }
}

