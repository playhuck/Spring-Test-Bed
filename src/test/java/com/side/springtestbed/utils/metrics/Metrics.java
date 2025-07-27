package com.side.springtestbed.utils.metrics;

import com.side.springtestbed.utils.utils.Histogram;
import com.side.springtestbed.utils.utils.LifeCycleCallback;
import com.side.springtestbed.utils.utils.Timer;

public interface Metrics extends LifeCycleCallback {
    Histogram histogram(String var1);

    Timer timer(String var1);
}

