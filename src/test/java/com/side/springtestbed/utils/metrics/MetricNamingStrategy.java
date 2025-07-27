package com.side.springtestbed.utils.metrics;

public interface MetricNamingStrategy {
    default String getMetricName(String name) {
        return name;
    }

    default boolean useUniquePoolName() {
        return false;
    }
}
