package com.side.springtestbed.utils;

import java.io.InputStream;
import java.net.URL;

public final class ClassLoaderUtils {
    private ClassLoaderUtils() {
        throw new UnsupportedOperationException("ClassLoaderUtils is not instantiable!");
    }

    public static ClassLoader getClassLoader() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        return classLoader != null ? classLoader : ClassLoaderUtils.class.getClassLoader();
    }

    public static <T> Class<T> loadClass(String className) throws ClassNotFoundException {
        return (Class<T>) getClassLoader().loadClass(className);
    }

    public static boolean findClass(String className) {
        try {
            return getClassLoader().loadClass(className) != null;
        } catch (ClassNotFoundException var2) {
            return false;
        } catch (NoClassDefFoundError var3) {
            return false;
        }
    }

    public static URL getResource(String resourceName) {
        return getClassLoader().getResource(resourceName);
    }

    public static InputStream getResourceAsStream(String resourceName) {
        return getClassLoader().getResourceAsStream(resourceName);
    }
}
