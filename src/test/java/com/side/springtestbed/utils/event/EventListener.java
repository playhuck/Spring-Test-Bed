package com.side.springtestbed.utils.event;

public abstract class EventListener<E extends Event> {
    private final Class<E> eventClass;

    protected EventListener(Class<E> eventClass) {
        this.eventClass = eventClass;
    }

    public Class<E> listensTo() {
        return this.eventClass;
    }

    public abstract void on(E var1);
}
