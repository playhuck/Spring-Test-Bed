package com.side.springtestbed.utils;

import org.hibernate.Session;

import java.util.function.Function;

@FunctionalInterface
public interface HibernateTransactionFunction<T> extends Function<Session, T> {
    default void beforeTransactionCompletion() {

    }

    default void afterTransactionCompletion() {

    }
}
