package com.side.springtestbed.monad;

import com.side.springtestbed.monad.models.MonadicCounterValue;
import com.side.springtestbed.monad.models.MonadicCounterFunction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MonadService {

    static MonadicCounterFunction<Void, Integer> Z = (Void v) ->
            new MonadicCounterValue<>(3, 1);

    static MonadicCounterFunction<Integer, Integer> F = (Integer x) ->
            new MonadicCounterValue<>(4, 2);

    static MonadicCounterFunction<Integer, Integer> G = (Integer x) ->
            new MonadicCounterValue<>(8, 3);

    public MonadicCounterValue<Integer> counter() {
        // Z -> F -> G 체이닝
        MonadicCounterValue<Integer> result = Z.apply(null)
                .flatMap(F)
                .flatMap(G);

        log.info("Value : {}, Counter : {}", result.value, result.counter);  // [8, 6]

        return result;
    }

}
