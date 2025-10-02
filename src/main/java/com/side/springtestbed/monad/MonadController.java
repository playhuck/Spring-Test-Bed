package com.side.springtestbed.monad;

import com.side.springtestbed.monad.models.MonadicCounterValue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/monad")
@Slf4j
public class MonadController {

    private final MonadService monadService;

    @RequestMapping("/counter")
    public ResponseEntity<Object> counter() {

        MonadicCounterValue<Integer> values = monadService.counter();

        return ResponseEntity
                .ok()
                .body(values);

    }
}
