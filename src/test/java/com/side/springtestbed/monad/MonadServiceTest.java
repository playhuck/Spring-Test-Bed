package com.side.springtestbed.monad;

import com.side.springtestbed.monad.models.CartWarehouse;
import com.side.springtestbed.monad.models.MonadicCounterValue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class MonadServiceTest {

    private static MonadService service;

    @BeforeAll
    static void setup() {

        service = new MonadService();

    }

    @Test
    @DisplayName("Monad Counter, Basic")
    void monadCounter() {

        MonadicCounterValue<Integer> values = service.counter();

        assertEquals(8, values.getValue());
        assertEquals(6, values.getCounter());

    }

    @Test
    @DisplayName("Monad Counter, Cart Warehouse Isn't Available")
    void monadCounterCartWarehouseIsNotAvailable() {

        String productId = UUID.randomUUID().toString();

        RuntimeException exception = assertThrows(RuntimeException.class, () -> service.exampleCartSystem(
                productId, 1,
                false, Integer.MAX_VALUE
        ));
        assertEquals("Warehouse isn't enough", exception.getMessage());

    }

    @Test
    @DisplayName("Monad Counter, Cart Warehouse Isn't Enough")
    void monadCounterCartWarehouseIsNotEnough() {

        String productId = UUID.randomUUID().toString();

        RuntimeException exception = assertThrows(RuntimeException.class, () -> service.exampleCartSystem(
                productId, 2,
                true, 1
        ));
        assertEquals("Warehouse isn't enough", exception.getMessage());

    }

    @Test
    @DisplayName("Monad Counter, Payment Success")
    void monadCounterPaymentSuccess() {

        String productId = UUID.randomUUID().toString();

        MonadicCounterValue<Boolean> result = service.exampleCartSystem(
                productId, 1,
                true, 2
        );
        assertTrue(result.getValue());

    }



}