package ru.sberbank.school.task13;

import lombok.NonNull;
import ru.sberbank.school.task13.exception.BeanFieldCopierServiceException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class BeanFieldCopierService implements BeanFieldCopier {

    @Override
    public void copy(@NonNull Object from, @NonNull Object to) {
        Map<String, Method> getters = getGetters(from);
        Map<String, Method> setters = getSetters(to);

        for (Map.Entry<String, Method> getEntry : getters.entrySet()) {
            Method getter = getEntry.getValue();
            Method setter = setters.get(getEntry.getKey());
            if (!Objects.isNull(setter) && compatibleTypes(getter, setter)) {
                boolean getterNotAccessible = false;
                boolean setterNotAccessible = false;
                try {
                    if (!getter.isAccessible()) {
                        getterNotAccessible = true;
                        getter.setAccessible(true);
                    }
                    if (!setter.isAccessible()) {
                        setterNotAccessible = true;
                        setter.setAccessible(true);
                    }
                    setter.invoke(to, getter.invoke(from));
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                    throw new BeanFieldCopierServiceException(
                            "Can't copy field from: " + getter.getName() + " to " + setter.getName(), e);
                } finally {
                    if (getterNotAccessible) {
                        getter.setAccessible(false);
                    }
                    if (setterNotAccessible) {
                        setter.setAccessible(false);
                    }
                }
            }
        }
    }

    private Map<String, Method> getGetters(Object object) {
        Map<String, Method> methods = new HashMap<>();
        for (Method method : object.getClass().getMethods()) {
            if (Modifier.isPublic(method.getModifiers()) && method.getParameterTypes().length == 0) {
                if (method.getName().matches("get[a-zA-Z0-9]+") && !method.getReturnType().equals(Void.TYPE)) {
                    methods.put(method.getName().toLowerCase().substring(3), method);
                }
                if (method.getName().matches("is[a-zA-Z0-9]+") && method.getReturnType().equals(Boolean.TYPE)) {
                    methods.put(method.getName().toLowerCase().substring(2), method);
                }
            }
        }
        return methods;
    }

    private Map<String, Method> getSetters(Object object) {
        Map<String, Method> methods = new HashMap<>();
        for (Method method : object.getClass().getMethods()) {
            if (Modifier.isPublic(method.getModifiers())
                    && method.getName().matches("set[a-zA-Z0-9]+")
                    && method.getParameterTypes().length == 1
                    && method.getReturnType().equals(Void.TYPE)) {
                methods.put(method.getName().toLowerCase().substring(3), method);
            }
        }
        return methods;
    }

    private boolean compatibleTypes(Method getter, Method setter) {
        Class getterReturnType = getter.getReturnType();
        Class setterParameterType = setter.getParameterTypes()[0];
        while (!Objects.isNull(getterReturnType)) {
            if (getterReturnType == setterParameterType) {
                return true;
            }
            getterReturnType = getterReturnType.getSuperclass();
        }
        return false;
    }
}