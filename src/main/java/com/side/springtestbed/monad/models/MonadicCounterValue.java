package com.side.springtestbed.monad.models;

import lombok.Getter;

@Getter
public class MonadicCounterValue<X> {

    private final X value;
    private final int counter;
    private final boolean isSuccess;
    private final String errorMessage;

    // 성공 케이스
    public MonadicCounterValue(X value, int counter) {
        this.value = value;
        this.counter = counter;
        this.isSuccess = true;
        this.errorMessage = null;
    }

    // 실패 케이스 (private)
    private MonadicCounterValue(X value, int counter, boolean isSuccess, String errorMessage) {
        this.value = value;
        this.counter = counter;
        this.isSuccess = isSuccess;
        this.errorMessage = errorMessage;
    }

    public <Y> MonadicCounterValue<Y> flatMap(MonadicCounterFunction<X, Y> f) {

        if(!isSuccess) {
            return MonadicCounterValue.error(errorMessage);
        }

        MonadicCounterValue<Y> result = f.apply(this.value);

        if(!result.isSuccess) {
            return MonadicCounterValue.error(result.errorMessage);
        }

        return new MonadicCounterValue<>(result.value, this.counter + result.counter);
    }

    public static <X> MonadicCounterValue<X> error(String errorMessage) {
        return new MonadicCounterValue<>(null, 0, false, errorMessage);
    }

}