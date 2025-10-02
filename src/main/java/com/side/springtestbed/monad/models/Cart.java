package com.side.springtestbed.monad.models;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

@Slf4j
@Getter
public class Cart {

    private final String productId;
    private final int quantity;
    private final CartWarehouse warehouse;

    public Cart(String productId, int quantity, CartWarehouse warehouse) {
        this.productId = productId;
        this.quantity = quantity;
        this.warehouse = warehouse;
    }

    // Cart → Cart (장바구니 검증 또는 추가 로직)
    public static MonadicCounterFunction<Cart, Cart> addToCart = cart -> {
        log.info("장바구니 처리: productId={}, quantity={}", cart.productId, cart.quantity);
        if(cart.quantity == 0) {
            return MonadicCounterValue.error("Quantity is zero");
        }
        return new MonadicCounterValue<>(cart, 1);
    };

    public static MonadicCounterFunction<Cart, Boolean> checkWarehouse = cart -> {
        CartWarehouse warehouse = cart.warehouse;
        log.info("재고 조회: available={}, count={}", warehouse.available, warehouse.count);
        if(warehouse.count < cart.quantity || !warehouse.available) {
            return MonadicCounterValue.error("Warehouse isn't enough");
        }
        return new MonadicCounterValue<>(true, 1);
    };

    public static MonadicCounterFunction<Boolean, String> moveToPayment = hasStock -> {
        if(hasStock) {
            return new MonadicCounterValue<>("MOVE_TO_PAYMENT", 1);
        } else {
            return MonadicCounterValue.error("Stock not available");
        }
    };

    public static MonadicCounterFunction<String, Boolean> approveToPayment = paymentPage -> {
        if(paymentPage.equals("MOVE_TO_PAYMENT")) {
            return new MonadicCounterValue<>(true, 1);
        }
        return MonadicCounterValue.error("Payment page invalid");
    };

    public static MonadicCounterFunction<Boolean, Boolean> buyToCart = isApproved -> {
        if(isApproved) {
            return new MonadicCounterValue<>(true, 1);
        }
        return MonadicCounterValue.error("Payment not approved");
    };

}