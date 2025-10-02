package com.side.springtestbed.monad.models;

public interface MonadicCounterFunction<X, Y> {
    MonadicCounterValue<Y> apply(X x);
}
