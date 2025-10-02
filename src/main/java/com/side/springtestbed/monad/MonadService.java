package com.side.springtestbed.monad;

import com.side.springtestbed.monad.models.Cart;
import com.side.springtestbed.monad.models.CartWarehouse;
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
        MonadicCounterValue<Integer> result = Z.apply(null) // [3,1]
                .flatMap(F) // [3, 1]
                .flatMap(G);

        log.info(
                "Value : {}, Counter : {}, isSuccess : {}",
                result.getValue(), result.getCounter(), result.isSuccess()
        );  // [8, 6]

        return result;
    }

    public MonadicCounterValue<Boolean> exampleCartSystem(
            String productId,
            Integer wantToBuyItemCount,
            Boolean available,
            Integer wareHouseStockCount
    ) {

        Cart cart = new Cart(
                productId,
                wantToBuyItemCount,
                new CartWarehouse(available, wareHouseStockCount)
        );

        MonadicCounterValue<Cart> monad = new MonadicCounterValue<>(cart, 0);

        MonadicCounterValue<Boolean> result = monad
                .flatMap(Cart.addToCart)
                .flatMap(Cart.checkWarehouse)
                .flatMap(Cart.moveToPayment)
                .flatMap(Cart.approveToPayment)
                .flatMap(Cart.buyToCart);

        if(!result.isSuccess()) throw new RuntimeException(result.getErrorMessage());

        return result;
    }

}
