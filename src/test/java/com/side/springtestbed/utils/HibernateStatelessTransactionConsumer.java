package com.side.springtestbed.utils;

import org.hibernate.StatelessSession;

import java.util.function.Consumer;

@FunctionalInterface
public interface HibernateStatelessTransactionConsumer extends Consumer<StatelessSession> {
    default void beforeTransactionCompletion() {

    }

    default void afterTransactionCompletion() {

    }
}
