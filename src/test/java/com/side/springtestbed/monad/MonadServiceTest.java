package com.side.springtestbed.monad;

import com.side.springtestbed.monad.models.MonadicCounterValue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MonadServiceTest {

    private static MonadService service;

    @BeforeAll
    static void setup() {

        service = new MonadService();

    }

    @Test
    @DisplayName("Monad Counter")
    void monadCounter() throws Exception {

        MonadicCounterValue<Integer> values = service.counter();

        assertEquals(8, values.value);
        assertEquals(6, values.counter);

    }

}