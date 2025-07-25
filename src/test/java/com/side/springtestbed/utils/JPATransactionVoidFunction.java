package com.side.springtestbed.utils;

import jakarta.persistence.EntityManager;

import java.util.function.Consumer;

@FunctionalInterface
public interface JPATransactionVoidFunction extends Consumer<EntityManager> {
    default void beforeTransactionCompletion() {

    }

    default void afterTransactionCompletion() {

    }
}
