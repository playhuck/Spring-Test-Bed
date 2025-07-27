package com.side.springtestbed.utils.metrics;

import java.util.Iterator;
import java.util.ServiceLoader;

public final class MetricsFactoryResolver {
    public static final MetricsFactoryResolver INSTANCE = new MetricsFactoryResolver();
    private ServiceLoader<MetricsFactoryService> serviceLoader = ServiceLoader.load(MetricsFactoryService.class);

    private MetricsFactoryResolver() {
    }

    public MetricsFactory resolve() {
        Iterator var1 = this.serviceLoader.iterator();

        MetricsFactory metricsFactory;
        do {
            if (!var1.hasNext()) {
                throw new IllegalStateException("No MetricsFactory could be loaded!");
            }

            MetricsFactoryService metricsFactoryService = (MetricsFactoryService)var1.next();
            metricsFactory = metricsFactoryService.load();
        } while(metricsFactory == null);

        return metricsFactory;
    }
}

