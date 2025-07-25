package com.side.springtestbed.utils;

import java.util.concurrent.Callable;

@FunctionalInterface
public interface VoidCallable extends Callable<Void> {

    void execute();

    default Void call() throws Exception {
        execute();
        return null;
    }
}
