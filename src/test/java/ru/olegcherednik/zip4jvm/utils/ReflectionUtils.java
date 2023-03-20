/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package ru.olegcherednik.zip4jvm.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static org.assertj.core.api.Assertions.assertThat;

/** Class provides set of methods based on java reflections: invoke constructors, static and not static methods, set or read fields. */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ReflectionUtils {

    public static <T> T getFieldValue(Object obj, String name) throws NoSuchFieldException, IllegalAccessException {
        Field field = getField(obj.getClass(), name);
        field.setAccessible(true);
        return (T)field.get(obj);
    }

    public static <T> T getStaticFieldValue(Class<?> cls, String name) throws NoSuchFieldException, IllegalAccessException {
        Field field = getField(cls, name);
        field.setAccessible(true);
        return (T)field.get(cls);
    }

    public static <T> T invokeConstructor(Class<T> cls) {
        try {
            Constructor<T> constructor = cls.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (Exception ignored) {
            return null;
        }
    }

    public static <T> T invokeConstructor(Class<T> cls, Class<?>[] types, Object... values) {
        try {
            Constructor<T> constructor = cls.getDeclaredConstructor(types);
            constructor.setAccessible(true);
            return constructor.newInstance(values);
        } catch (Exception ignored) {
            return null;
        }
    }

    public static void setFieldValue(Object obj, String name, Object value) throws NoSuchFieldException, IllegalAccessException {
        Field field = getField(obj.getClass(), name);
        boolean accessible = field.isAccessible();

        try {
            field.setAccessible(true);
            setFileValue(field, obj, value);
        } finally {
            field.setAccessible(accessible);
        }
    }

    public static void setStaticFieldValue(Class<?> cls, String name, Object value) throws NoSuchFieldException, IllegalAccessException {
        Field field = getField(cls, name);
        boolean accessible = field.isAccessible();

        try {
            field.setAccessible(true);
            clearFinalModifier(field);
            setFileValue(field, null, value);
        } finally {
            field.setAccessible(accessible);
        }
    }

    private static void clearFinalModifier(Field field) throws NoSuchFieldException, IllegalAccessException {
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
    }

    private static void setFileValue(Field field, Object obj, Object value) throws IllegalAccessException {
        if (field.getType() == int.class) {
            field.setInt(obj, (Integer)value);
        } else if (field.getType() == boolean.class) {
            field.setBoolean(obj, (Boolean)value);
        } else if (field.getType() == byte.class) {
            field.setByte(obj, (Byte)value);
        } else if (field.getType() == char.class) {
            field.setChar(obj, (Character)value);
        } else if (field.getType() == double.class) {
            field.setDouble(obj, (Double)value);
        } else if (field.getType() == long.class) {
            field.setLong(obj, (Long)value);
        } else if (field.getType() == short.class) {
            field.setShort(obj, (Short)value);
        } else if (field.getType() == float.class) {
            field.setFloat(obj, (Float)value);
        } else {
            field.set(obj, value);
        }
    }

    public static <T> T invokeStaticMethod(Class<?> cls, String name) throws Throwable {
        return invokeStaticMethod(cls, name, null);
    }

    public static <T> T invokeStaticMethod(Class<?> cls, String name, Class<?> type, Object value) throws Throwable {
        return invokeStaticMethod(cls, name, new Class<?>[] { type }, value);
    }

    public static <T> T invokeStaticMethod(Class<?> cls, String name, Class<?> type1, Class<?> type2, Object value1, Object value2) throws Throwable {
        return invokeStaticMethod(cls, name, new Class<?>[] { type1, type2 }, value1, value2);
    }

    public static <T> T invokeStaticMethod(Class<?> cls, String name, Class<?> type1, Class<?> type2, Class<?> type3,
                                           Object value1, Object value2, Object value3) throws Throwable {
        return invokeStaticMethod(cls, name, new Class<?>[] { type1, type2, type3 }, value1, value2, value3);
    }

    public static <T> T invokeStaticMethod(Class<?> cls, String name, Class<?>[] types, Object... values) throws Throwable {
        try {
            Method method = getMethod(cls, name, types);
            method.setAccessible(true);
            return (T)method.invoke(null, values);
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
    }

    public static <T> T invokeMethod(Object obj, String name) throws Throwable {
        assertThat(obj).isNotInstanceOf(Class.class);
        return invokeMethod(obj, name, null);
    }

    public static <T> T invokeMethod(Object obj, String name, Class<?> type, Object value) throws Throwable {
        assertThat(obj).isNotInstanceOf(Class.class);
        return invokeMethod(obj, name, new Class<?>[] { type }, value);
    }

    public static <T> T invokeMethod(Object obj, String name, Class<?> type1, Class<?> type2, Object value1, Object value2) throws Throwable {
        assertThat(obj).isNotInstanceOf(Class.class);
        return invokeMethod(obj, name, new Class<?>[] { type1, type2 }, value1, value2);
    }

    public static <T> T invokeMethod(Object obj, String name, Class<?> type1, Class<?> type2, Class<?> type3, Object value1, Object value2,
                                     Object value3) throws Throwable {
        assertThat(obj).isNotInstanceOf(Class.class);
        return invokeMethod(obj, name, new Class<?>[] { type1, type2, type3 }, value1, value2, value3);
    }

    public static <T> T invokeMethod(Object obj, String name, Class<?>[] types, Object... values) throws Throwable {
        try {
            Method method = getMethod(obj.getClass(), name, types);
            method.setAccessible(true);
            return (T)method.invoke(obj, values);
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
    }

    private static Method getMethod(Class<?> cls, String name, Class<?>... types) throws NoSuchMethodException {
        Method method = null;

        while (method == null && cls != null) {
            try {
                method = cls.getDeclaredMethod(name, types);
            } catch (NoSuchMethodException ignored) {
                cls = cls.getSuperclass();
            }
        }

        if (method == null) {
            throw new NoSuchMethodException();
        }

        return method;
    }

    private static Field getField(Class<?> cls, String name) throws NoSuchFieldException {
        Field field = null;

        while (field == null && cls != null) {
            try {
                field = cls.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {
                cls = cls.getSuperclass();
            }
        }

        if (field == null) {
            throw new NoSuchFieldException();
        }

        return field;
    }
}
