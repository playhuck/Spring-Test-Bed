package com.side.springtestbed.utils.metrics;

import com.side.springtestbed.utils.ConfigurationProperties;

public interface MetricsFactory {
    Metrics newInstance(ConfigurationProperties var1);
}

