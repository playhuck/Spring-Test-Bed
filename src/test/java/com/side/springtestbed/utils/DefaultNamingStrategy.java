package com.side.springtestbed.utils;

import com.side.springtestbed.utils.metrics.MetricNamingStrategy;

public class DefaultNamingStrategy implements MetricNamingStrategy {
    public static final DefaultNamingStrategy INSTANCE = new DefaultNamingStrategy();

    public DefaultNamingStrategy() {
    }
}
