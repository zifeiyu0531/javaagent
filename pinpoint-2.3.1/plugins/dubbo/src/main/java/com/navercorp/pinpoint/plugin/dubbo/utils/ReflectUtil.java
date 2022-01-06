package com.navercorp.pinpoint.plugin.dubbo.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class ReflectUtil {
    public static Object getObjectByFieldName(Object target, String fieldName) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(target);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Object getSuperObjectByFieldName(Object target, String fieldName) {
        try {
            Field field = target.getClass().getSuperclass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(target);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void setFinalValueByFieldName(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            setFinalValue(target, field, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setSuperFinalValueByFieldName(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getSuperclass().getDeclaredField(fieldName);
            setFinalValue(target, field, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void setFinalValue(Object target, Field field, Object value) {
        try {
            Field modifiers = Field.class.getDeclaredField("modifiers");
            modifiers.setAccessible(true);
            modifiers.setInt(field, field.getModifiers() & ~Modifier.FINAL);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Object invokeMethodByName(Object target, String methodName, Object arg) {
        try {
            Method m;
            if (arg == null) {
                m = target.getClass().getDeclaredMethod(methodName);
            } else {
                m = target.getClass().getDeclaredMethod(methodName, arg.getClass());
            }
            m.setAccessible(true);
            return arg == null ? m.invoke(target) : m.invoke(target, arg);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
