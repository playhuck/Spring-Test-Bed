package com.side.springtestbed.utils.conn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.ServiceLoader;

public final class ConnectionDecoratorFactoryResolver {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionDecoratorFactoryResolver.class);
    public static final ConnectionDecoratorFactoryResolver INSTANCE = new ConnectionDecoratorFactoryResolver();
    private ServiceLoader<ConnectionDecoratorFactoryService> serviceLoader = ServiceLoader.load(ConnectionDecoratorFactoryService.class);

    private ConnectionDecoratorFactoryResolver() {
    }

    public ConnectionDecoratorFactory resolve() {
        int loadingIndex = Integer.MIN_VALUE;
        ConnectionDecoratorFactory connectionDecoratorFactory = null;
        Iterator<ConnectionDecoratorFactoryService> connectionDecoratorFactoryServiceIterator = this.serviceLoader.iterator();

        while(connectionDecoratorFactoryServiceIterator.hasNext()) {
            try {
                ConnectionDecoratorFactoryService connectionDecoratorFactoryService = (ConnectionDecoratorFactoryService)connectionDecoratorFactoryServiceIterator.next();
                int currentLoadingIndex = connectionDecoratorFactoryService.loadingIndex();
                if (currentLoadingIndex > loadingIndex) {
                    ConnectionDecoratorFactory currentConnectionDecoratorFactory = connectionDecoratorFactoryService.load();
                    if (currentConnectionDecoratorFactory != null) {
                        connectionDecoratorFactory = currentConnectionDecoratorFactory;
                        loadingIndex = currentLoadingIndex;
                    }
                }
            } catch (LinkageError var7) {
                LinkageError e = var7;
                LOGGER.info("Couldn't load ConnectionDecoratorFactoryService on the current JVM", e);
            }
        }

        if (connectionDecoratorFactory != null) {
            return connectionDecoratorFactory;
        } else {
            throw new IllegalStateException("No ConnectionDecoratorFactory could be loaded!");
        }
    }
}

