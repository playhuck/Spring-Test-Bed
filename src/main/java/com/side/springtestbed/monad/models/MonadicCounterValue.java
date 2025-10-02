package com.side.springtestbed.monad.models;

public class MonadicCounterValue<X> {

    public final X value;
    public final int counter;

    public MonadicCounterValue(X value, int counter) {
        this.value = value;
        this.counter = counter;
    }

    public <Y> MonadicCounterValue<Y> flatMap(MonadicCounterFunction<X, Y> f) {
        MonadicCounterValue<Y> result = f.apply(this.value);
        return new MonadicCounterValue<>(result.value, this.counter + result.counter);
    }

}
