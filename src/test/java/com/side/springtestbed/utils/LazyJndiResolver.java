package com.side.springtestbed.utils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public final class LazyJndiResolver implements InvocationHandler {
    private final String name;
    private Object target;

    private LazyJndiResolver() {
        throw new UnsupportedOperationException("ReflectionUtils is not instantiable!");
    }

    private LazyJndiResolver(String name) {
        this.name = name;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (this.target == null) {
            this.target = JndiUtils.lookup(this.name);
        }

        return method.invoke(this.target, args);
    }

    public static <T> T newInstance(String name, Class<?> objectType) {
        return (T) Proxy.newProxyInstance(ClassLoaderUtils.getClassLoader(), new Class[]{objectType}, new LazyJndiResolver(name));
    }
}

