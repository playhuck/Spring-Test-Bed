package com.side.springtestbed.utils;

import org.hibernate.StatelessSession;

import java.util.function.Function;

@FunctionalInterface
public interface HibernateStatelessTransactionFunction<T> extends Function<StatelessSession, T> {
    default void beforeTransactionCompletion() {

    }

    default void afterTransactionCompletion() {

    }
}
