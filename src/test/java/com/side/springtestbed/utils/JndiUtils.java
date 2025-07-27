package com.side.springtestbed.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

public final class JndiUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(JndiUtils.class);

    private JndiUtils() {
        throw new UnsupportedOperationException("JndiUtils is not instantiable!");
    }

    public static <T> T lookup(String name) {
        InitialContext initialContext = initialContext();

        Object var3;
        try {
            T object = (T) initialContext.lookup(name);
            if (object == null) {
                throw new NameNotFoundException(name + " was found but is null");
            }

            var3 = object;
        } catch (NameNotFoundException var8) {
            NameNotFoundException e = var8;
            throw new IllegalArgumentException(name + " was not found in JNDI", e);
        } catch (NamingException var9) {
            NamingException e = var9;
            throw new IllegalArgumentException("JNDI lookup failed", e);
        } finally {
            closeContext(initialContext);
        }

        return (T) var3;
    }

    protected static InitialContext initialContext() {
        try {
            return new InitialContext();
        } catch (NamingException var1) {
            throw new IllegalStateException("Can't create the InitialContext object");
        }
    }

    protected static void closeContext(InitialContext initialContext) {
        if (initialContext != null) {
            try {
                initialContext.close();
            } catch (NamingException var2) {
                NamingException e = var2;
                LOGGER.debug("Can't close InitialContext", e);
            }
        }

    }
}

